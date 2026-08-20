# LINE Andrew namespace mount shim

這份記錄 Note 12 Turbo / POCO F5 在 Magisk Alpha + Zygisk Next `WL + UM` 環境下，如何讓 Andrew patched LINE APK 真正進入 LINE app process，以及目前 shim 的間歇性 regression。

## 基線

```text
LINE: 26.11.0 (261100124)
LINE package: jp.naver.line.android
Andrew patched payload SHA-256:
683853ceecac06964eac7703e5f02c47fd43ac3afbeba2f80630e614fbd14289
Stock base SHA-256:
fbc229f8563c0980307059bce781f899a455db44ad40579a604f6d7f30ea86ea
Root: Magisk Alpha
Zygisk: Zygisk Next 1.4.2
```

## 為什麼需要 shim

Zygisk Next 的 `WL + UM` 在 app specialization 階段會把 module / classic bind mount 從非 root app namespace 移除。

已證實過：

```text
master namespace = patched
zygote namespace = patched
LINE child namespace = stock
```

所以 Andrew module 本身可正常掛到 master / zygote，但 LINE process 最後仍會看到 stock base.apk。

## 自訂 v3 shim

v3：

```text
id=rvmm-zygisk-mount
version=v10-line-andrew-3
versionCode=1003
```

v3 ZIP SHA-256：

```text
8a8a16d50a875ad3da660ddd895f15878e1ed940241b6bcacac73f5ed0daece3
```

Native Zygisk binary 沿用 upstream `j-hc/rvmm-zygisk-mount` v10，arm64 `.so` SHA-256：

```text
dc2ded2627a7ec121c59a0f166cf666b707fec04abda1b2ea344e70a631fc89e
```

v3 shell 只負責：

- target 固定 `jp.naver.line.android`
- source 固定 `/data/adb/rvhc/line-andrew-andrew-arm64.apk`
- destination 依 `pm path` 產生目前 LINE `base.apk`
- 不改 DenyList / Zygisk Next WL / UM
- 不 grant LINE root
- 不 mutate RVHC payload
- 不 force-stop LINE

## 2026-08-20 regression

曾有成功 checkpoint：

```text
MASTER       = 683853...
RVHC         = 683853...
LINE PROCESS = 683853...
LINE process mountinfo = RVHC -> current base.apk bind mount exists
```

但之後日用中廣告與 VOOM 一起回來，現場抓到：

```text
MASTER       = 683853...
RVHC         = 683853...
LINE PROCESS = fbc229...
LINE MOUNT   = none
```

因此這不是 Andrew patch / server config 本身失效，而是新 LINE process spawn 時 shim 沒有成功 remount。

### stale path 假說已排除

失敗現場的 `procs_map` destination 與當下 `pm path jp.naver.line.android` 完全一致。

所以目前不是 APK install path 改掉造成。

### upstream v10 native code defect

對照 upstream commit `02bb8adf9735e8f9ca06c1babe8e9fe853ed6c7b` 的 `zygisk/jni/module.cpp`，`receiveProcInfo()`：

1. 讀入 `procs_map` 到 heap buffer。
2. `getMountSrcDst()` 回傳的 `src` / `dst` 只是指向該 heap buffer 內部的 pointer。
3. 函式在 return 前直接 `free(procs_map)`。
4. `companionHandler()` 隨後才 fork child，並把已失效的 `src` / `dst` 傳給 `injectMount()`。

即：

```text
getMountSrcDst(procs_map, ..., &src, &dst)
free(procs_map)
return
...
fork()
injectMount(src, dst, pid)
```

這是典型 use-after-free / dangling pointer，能解釋同一 `procs_map` / 同一路徑下的間歇性成功與失敗。

## v4：arm64 success-path UAF fix

v4 不改 Zygisk Next、Andrew payload、DenyList 或任何 LINE private data，只修 LINE 命中成功路徑的 native lifetime。

```text
id=rvmm-zygisk-mount
version=v10-line-andrew-4
versionCode=1004
```

v4 ZIP SHA-256：

```text
f761c0272e03cd5e43575fd1161441fd82578c222e466bcb4b2372051280f6ed
```

v4 arm64 `.so` SHA-256：

```text
2ed0f9cd8c346906c4a6652aff3d87d8e26020ba6ad1c71c4edd7cbfa5160996
```

### 修法

因目前工作環境沒有 Android NDK r27d，v4 採可驗證的最小 binary patch：

- upstream / v3 arm64 `.so` 在 `zygisk_companion_entry + 0x258`、ELF file offset / VA `0x11fc` 的 `bl free@plt`
- 原始 4 bytes：`0d 01 00 94`
- 改成 AArch64 `nop`：`1f 20 03 d5`

反組譯驗證：

```text
11ec: str x8, [x19]      ; src
11f4: add x22, x9, #0x2 ; dst
11f8: str x22, [x20]
11fc: nop                ; v3 這裡是 free(procs_map)
1200: ldr x5, [x19]
...
1224: __android_log_print
```

這樣 `src` / `dst` 在後續 `fork()` 與 `injectMount()` 時仍指向有效 buffer。

副作用是每次「命中 LINE target」會保留約一個 `procs_map` 大小的 heap allocation（目前 map 163 bytes，另加 allocator overhead），companion lifetime 內不釋放。就 LINE process recreation 頻率而言屬可接受的小量 leak；非 LINE process 的 miss / error path free 行為維持原樣。

### 離線驗證

- ZIP `unzip -t`：通過。
- v3 → v4 除 `module.prop` 外，只有 `zygisk/arm64-v8a.so` 改變。
- arm64 `.so` 精確只有 offset `0x11fc..0x11ff` 四個 byte 不同。
- patched site 反組譯為單一 `nop`。
- `customize.sh` / `service.sh` / `util.sh` / `procs_map` 產生邏輯均未更動。

## v4 A/B 驗證規則

一次成功不再視為 persistence 已解。

上機後先確認一次：

```text
MASTER       = 683853...
RVHC         = 683853...
LINE PROCESS = 683853...
LINE mountinfo = RVHC -> current base.apk bind mount
```

之後至少跨多次自然 LINE process recreation / 一段日用時間，再重查 process hash；廣告與 VOOM 不應再因 process 重建一起回來。

## 判讀規則

- `MASTER / RVHC` patched 不代表 LINE process patched。
- `procs_map` path 正確不代表 native mount 一定成功。
- 一次成功 spawn 不再視為 persistence 已解；至少要跨多次自然 LINE process recreation 驗證。
- 不為了修 mount 改全域 Zygisk Next WL / UM、DenyList、PIF / Tricky Store。
