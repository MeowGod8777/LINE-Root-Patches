# LINE 日用相容性矩陣

這份只看「目前這套 LINE + Andrew 模塊 + Root runtime 相容性處理，實際日用怎樣」。

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
| Andrew payload delivery | ✅ 已證實 | `MASTER / RVHC / LINE PROCESS = 683853...`；LINE-only namespace mount shim |
| 去廣告 | ✅ 正常 | Andrew patch |
| Home modules 精簡 | ✅ 正常 | Andrew patch |
| VOOM 停用／隱藏 | ✅ 正常 | Andrew patch |
| Smart Channel / Home 雜項 | 🟢 實測有變化 | 以當前 Andrew default patch + 實機結果判定 |
| XML Guard | 🟡 暫停 | runtime 曾有證據；Andrew 驗證期 disabled，避免 confounder |
| LINE Pay merchant checkout | ✅ redirect 可工作 | `pay/payment/<reserveId>` → standalone `com.linepaytw.upay` 已實測 |
| LINE Pay App Link | ✅ 正常 | `web-tw-pay.line.me: verified`，direct ACTION_VIEW 可拉起 standalone App |
| Wallet → LINE Pay 首頁 | ⏸️ 延後 | convenience routing；非付款 blocker，先不為此增加 build / 上機輪次 |
| 聊天室好友轉帳 | ⏸️ 延後 | Standalone 有 send-money 功能，但 route 為 server-config `epiTransferSendMoney`；recipient 傳遞尚未定案 |
| 社群 | 🟢 需保留日用驗證 | `Hide community button` 明確 excluded |
| Wallet UI | 🟢 保留 | `Hide Wallet tab` 明確 excluded |
| 聊天室 Transfer UI | 🟢 保留 | `Hide Transfer button` 明確 excluded |
| 收回訊息／已讀行為 | 🟡 按當前 config 驗證 | `Keep chats unread` 明確 excluded |
| 背景通知 | 🟡 長期觀察 | 不用 force-stop / kill 當 persistence |

## 更新後最低回歸

LINE、Andrew patch bundle、config、mount shim 任一邊更新，至少測：

1. 啟動／登入。
2. 一般聊天／圖片／檔案。
3. Home。
4. VOOM。
5. process namespace hash 是否仍是預期 patched payload。
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
- merchant checkout redirect 成功 ≠ Wallet / P2P transfer 已完整可用。
- Wallet / P2P 目前是 deliberate defer，不記成付款功能失敗。
- ColorOS 沒有「關聯啟動」UI ≠ 一定是系統 blocker；以 App Links resolver 與 runtime transition 為準。
- 舊版本測到的副作用不能自動套到新版本。
- 這份矩陣只放去識別後結果，不放真實帳號／聊天／private DB／payment reserveId。

> **附註：** 內容由 AI 按指定格式上傳整理，有錯、缺漏或其他問題請直接私訊。