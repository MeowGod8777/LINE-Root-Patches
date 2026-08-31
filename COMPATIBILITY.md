# LINE 日用相容性矩陣

這份分開記錄 iQOO 12 Pro non-root APK 路線與 Note 12 Turbo Root module 路線，避免把兩種安裝／runtime 條件混在一起。

## iQOO 12 Pro non-root — 2026-08-31 結案

基線：

```text
Device = iQOO 12 Pro / V2329A
OS = OriginOS / Android 16
Root = none
LINE = 26.11.0
arch = arm64-v8a
build-mode = apk
patches = Andrew morphe-patches
GmsCore = Morphe MicroG-RE 6.1.4
GmsCore package = app.revanced.android.gms
GmsCore account type = app.revanced
```

| 項目 | 狀態 | 備註 |
|---|---|---|
| LINE 啟動／聊天 | 🟢 | patched APK 正常登入與日用 |
| 去廣告 | 🟢 | Andrew `Hide ad views` + `Remove banner ads` |
| Home modules / content feed | 🟢 | 精簡正常 |
| VOOM | 🟢 | 停用 + tab 隱藏 |
| LINE TODAY | 🟢 | tab 隱藏 |
| Shopping / 逛逛 | 🟢 | tab 隱藏 |
| Wallet tab | 🟢 | 隱藏 |
| LINE Pay redirect | 🟢 | 保留 standalone redirect patch |
| Push notification patch | 🟢 日用基準 | non-root 重簽 APK 使用 `Fix push notifications` |
| Google 帳號 picker | 🟢 | LINE 備份頁可叫出 `app.revanced.android.gms` picker |
| Google Drive restore | 🟢 | Note 12 Turbo backup → 12 Pro restore 已完成 |
| Google Drive account binding | 🟢 | `app.revanced` account 正常 |
| YouTube ReVanced 共用 MicroG | 🟢 | 同一顆 Morphe MicroG-RE 6.1.4，影片可播放 |
| 貼圖排序等本機狀態 | 🟡 | 復原後沒有完整保留；不影響聊天記錄，視為小問題 |

**已廢棄：** 第二顆 `app.line12pro.android.gms` / `app.line12pro` dedicated MicroG 架構。最終不需要雙 MicroG；Andrew `gmscoreauth` 直接配 upstream Morphe MicroG-RE 6.1.4 即可。

詳細：[`nonroot-12pro/README.md`](nonroot-12pro/README.md)

---

## Note 12 Turbo Root 路線

目前基線：

```text
LINE 26.11.0 (261100124)
arm64-v8a
patched-apps build-mode = module
Andrew patched payload = 683853ceecac06964eac7703e5f02c47fd43ac3afbeba2f80630e614fbd14289
Root = Magisk Alpha
Zygisk = Zygisk Next
```

目前 `patched-apps/config.toml` 採 Andrew default patches + exclusions，不再使用舊的 `exclusive-patches = true / 只開 5 patch` 記錄。

## 功能狀態

| 項目 | 狀態 | 備註 |
|---|---|---|
| LINE 啟動／聊天 | 🟢 日用中 | 目前正常 |
| Andrew payload delivery | 🔴 intermittent regression | 曾驗證 `MASTER / RVHC / LINE PROCESS = 683853...`；2026-08-20 後續新 LINE process 實測變成 `MASTER=683853... / RVHC=683853... / LINE PROCESS=fbc229...`，且 process mountinfo 無 LINE Andrew bind mount。v3 shim 不能再視為 persistence 已解 |
| 去廣告 | 🔴 regression | payload 掉出 LINE process 後廣告復活；目前不歸因 server config |
| Home modules 精簡 | 🔴 regression risk | 與 patched payload delivery 綁定，需 mount 恢復後再回歸 |
| VOOM 停用／隱藏 | 🔴 regression | payload 掉出 LINE process 後 VOOM 復活 |
| Smart Channel / Home 雜項 | 🔴 regression risk | 先解 namespace delivery，不另猜 UI/server route |
| XML Guard | 🟡 暫停 | runtime 曾有證據；Andrew 驗證期 disabled，避免 confounder |
| LINE Pay merchant checkout | 🟡 先保留既有證據 | `pay/payment/<reserveId>` → standalone 曾實測成功；但目前 LINE process 已掉回 stock，先不以舊結果代表當前 process |
| LINE Pay App Link | ✅ 正常 | `web-tw-pay.line.me: verified`，direct ACTION_VIEW 可拉起 standalone App |
| Wallet → LINE Pay 首頁 | ⏸️ 延後 | convenience routing；非付款 blocker，先不為此增加 build / 上機輪次 |
| 聊天室好友轉帳 | ⏸️ 延後 | Standalone 有 send-money 功能，但 route 為 server-config `epiTransferSendMoney`；recipient 傳遞尚未定案 |
| 社群 | 🟢 需保留日用驗證 | `Hide community button` 明確 excluded |
| Wallet UI | 🟢 保留 | `Hide Wallet tab` 明確 excluded |
| 聊天室 Transfer UI | 🟢 保留 | `Hide Transfer button` 明確 excluded |
| 收回訊息／已讀行為 | 🟡 按當前 config 驗證 | `Keep chats unread` 明確 excluded |
| 背景通知 | 🟡 長期觀察 | 不用 force-stop / kill 當 persistence |

## 2026-08-20 namespace mount regression checkpoint

在廣告與 VOOM 同時復活的現場直接讀取：

```text
PACKAGE       = LINE 26.11.0
MASTER        = 683853ceecac06964eac7703e5f02c47fd43ac3afbeba2f80630e614fbd14289
RVHC          = 683853ceecac06964eac7703e5f02c47fd43ac3afbeba2f80630e614fbd14289
LINE PROCESS  = fbc229f8563c0980307059bce781f899a455db44ad40579a604f6d7f30ea86ea
LINE MOUNT    = none
Andrew module = active, versionCode 3
shim module   = active, v10-line-andrew-3
```

所以這次 regression 已直接定位到：

> patched payload 仍存在 master/RVHC，但 LINE app specialization 後沒有得到 custom namespace remount；LINE process 實際讀 stock `fbc229...`。

目前禁止把 v3 shim 寫成「已解 persistence」。下一步只查 shim 的 `procs_map` 是否與目前 base path 一致，以及 `rvmm-zygisk-mount` native companion / mount error；不重做 Andrew build、不改 DenyList、不改 Zygisk Next concealment、不把問題誤判成 server config。

## 更新後最低回歸

LINE、Andrew patch bundle、config、mount shim 任一邊更新，至少測：

1. 啟動／登入。
2. 一般聊天／圖片／檔案。
3. Home。
4. VOOM。
5. process namespace hash 是否仍是預期 patched payload；**不能只驗一次 spawn，需至少驗證後續 LINE process 重建仍成立。**
6. LINE Pay merchant checkout。
7. 社群。
8. 行事曆／附加功能。
9. 外部連結。
10. 背景通知。
11. XML Guard 是否仍需要／仍有效。

Wallet → standalone LINE Pay 與聊天室好友轉帳目前屬 optional/deferred convenience routing，不列為每次最低回歸 blocker；若之後重新啟動該 patch 線再納入測試矩陣。

## 判讀規則

- `patched-apps` upstream 有某個 patch ≠ 本 fork 現在的 build 一定有啟用；以 config / patch source / payload 為準。
- UI 看起來有改 ≠ patched APK 一定進 LINE process；mount 以 `/proc/<pid>/root/.../base.apk` hash + mountinfo 為準。
- **一次 process 成功 mount ≠ persistence 已解；必須覆蓋後續 process respawn / zygote lifecycle。**
- merchant checkout redirect 成功 ≠ Wallet / P2P transfer 已完整可用。
- Wallet / P2P 目前是 deliberate defer，不記成付款功能失敗。
- ColorOS 沒有「關聯啟動」UI ≠ 一定是系統 blocker；以 App Links resolver 與 runtime transition 為準。
- 舊版本測到的副作用不能自動套到新版本。
- 這份矩陣只放去識別後結果，不放真實帳號／聊天／private DB／payment reserveId。

> **附註：** 內容由 AI 按指定格式上傳整理，有錯、缺漏或其他問題請直接私訊。