# LINE 舊資料回收清單

這份記以前做過、但現在 repo 還沒完整收進來的 LINE 專屬資料。

## XML Guard

- [x] module 結構與 runtime 行為摘要。
- [ ] `apply.sh` 原始完整 source。
- [ ] `service.sh` 原始完整 source。
- [ ] `watch.sh` 原始完整 source。
- [ ] `action.sh` 原始完整 source。
- [ ] `manual_reapply.sh` 原始完整 source。
- [ ] 原始 `README.txt` / `module.prop`。

## Root / Security Detection

- [ ] `basic.txt`
- [ ] `linepay_OK_logcat.txt`
- [ ] `process_OK.txt`
- [ ] `activity_OK.txt`
- [ ] `vguard_activity_dump.txt`
- [ ] `vguard_private_files.txt`
- [ ] LINE Pay「使用的裝置不安全」畫面與當時 Root stack 的去識別紀錄。
- [ ] LINE 26.11.0 的警告條件／裝置鎖定／vbmeta／Root manager 相關整理。

## Wallet / Home / Smart Channel / VOOM 行為

- [ ] `wallet_v4_diff.txt`
  - 只整理哪些 DB / WAL 有變化，不把 `/data/user/0/...` 的完整 raw diff 直接丟進 Public repo。
- [ ] Smart Channel / Home 雜項實測結果。
- [ ] 社群、行事曆、Wallet / LINE Pay 等功能回歸矩陣。
- [ ] patch 前後功能 checklist。

## patched-apps 對應

- [x] 目前 config：LINE `26.11.0`、`arm64-v8a`、`module`、`exclusive-patches = true`。
- [x] 目前實際啟用的 5 個 patch 已寫清楚。
- [ ] 之後若 config 變更，要同步更新版本／副作用矩陣。

## Public repo 隱私規則

以下不直接上傳：

- 真實 shared preference XML。
- LINE DB / WAL 內容。
- token / cookie / account ID。
- 聊天內容、好友／社群資料。
- 未去識別的完整 app-private dump。

> **附註：** 內容由 AI 按指定格式上傳整理，有錯、缺漏或其他問題請直接私訊。
