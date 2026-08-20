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

目前 shim：

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

### upstream v10 native code 的高風險 defect candidate

對照 upstream commit `02bb8adf9735e8f9ca06c1babe8e9fe853ed6c7b` 的 `zygisk/jni/module.cpp`，`receiveProcInfo()`：

1. 讀入 `procs_map` 到 heap buffer。
2. `getMountSrcDst()` 回傳的 `src` / `dst` 只是指向該 heap buffer 內部的 pointer。
3. 函式在 return 前直接 `free(procs_map)`。
4. `companionHandler()` 隨後才 fork child，並把已失效的 `src` / `dst` 傳給 `injectMount()`。

也就是存在典型 **use-after-free / dangling pointer**：

```text
getMountSrcDst(procs_map, ..., &src, &dst)
free(procs_map)
return
...
fork()
injectMount(src, dst, pid)
```

這可以合理解釋「有時剛好 mount 成功、有時同一 map / 同一路徑卻沒有 mount」的間歇性行為。

目前狀態：

> 高信心 code-level root-cause candidate，尚未用修正後 native binary 做 A/B 實機驗證，因此還不標成最終定案。

## 下一版修正方向

v4 不再只改 shell；要修 native lifetime：

- 在 `free(procs_map)` 前複製 `src` / `dst` 成獨立 owned buffers；或
- 把 `procs_map` lifetime 延長到 `injectMount()` 完成之後。

另外增加 success / failure breadcrumb，讓每次 LINE specialization 都能判斷：

```text
matched target
setns success/fail
mount success/fail
```

上機前先離線重建並檢查 `.so`，避免再用 upstream v10 原 binary 做 persistence 結論。

## 判讀規則

- `MASTER / RVHC` patched 不代表 LINE process patched。
- `procs_map` path 正確不代表 native mount 一定成功。
- 一次成功 spawn 不再視為 persistence 已解；至少要跨多次自然 LINE process recreation 驗證。
- 不為了修 mount 改全域 Zygisk Next WL / UM、DenyList、PIF / Tricky Store。
