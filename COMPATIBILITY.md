# LINE 日用相容性矩陣

這份只看「目前這套 LINE + Andrew 模塊 + 既有修補，實際日用怎樣」。

目前基線：

```text
LINE 26.11.0
arm64-v8a
patched-apps build-mode = module
exclusive-patches = true
```

## 功能狀態

| 項目 | 狀態 | 備註 |
|---|---|---|
| LINE 啟動／聊天 | 🟢 日用中 | 目前正常 |
| 去廣告 | ✅ 正常 | Andrew patch |
| Home modules 精簡 | ✅ 正常 | Andrew patch |
| VOOM 停用／隱藏 | ✅ 正常 | Andrew patch |
| Smart Channel / Home 雜項 | 🟢 實測有變化 | 沒有獨立同名 patch，按實機結果記 |
| XML Guard | 🟢 runtime 有證據 | 完整 source 尚未回收 |
| LINE Pay / security detection | 🧪 持續驗證 | VGuard 行為獨立看 |
| Wallet | 🟡 按版本／config 驗證 | 不預設 Andrew 其他 patch 有開 |
| 社群 | 🟢 需保留日用驗證 | 改 patch 後要重測 |
| 行事曆／附加功能 | 🟡 按版本驗證 | 不沿用 upstream 全功能清單 |
| 收回訊息／已讀行為 | 🟡 未列為目前 Andrew 5 patch 功能 | 有需要再分版本測 |

## 更新後最低回歸

LINE、Andrew patch bundle 或 config 任一邊更新，至少測：

1. 啟動／登入。
2. 一般聊天／圖片／檔案。
3. Home。
4. VOOM。
5. LINE Pay / Wallet。
6. 社群。
7. 行事曆／附加功能。
8. 外部連結。
9. XML Guard 是否仍需要／仍有效。

## 判讀規則

- `patched-apps` upstream 有某個 patch ≠ 本 fork 現在有開。
- config 沒開的功能，不直接寫成目前行為。
- 舊版本測到的副作用不能自動套到新版本。
- 這份矩陣只放去識別後結果，不放真實帳號／聊天／private DB。

> **附註：** 內容由 AI 按指定格式上傳整理，有錯、缺漏或其他問題請直接私訊。
