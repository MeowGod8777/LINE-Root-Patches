# LINE / LINE Pay Root detection 紀錄

這頁只留能公開的判讀，不把 LINE 私有資料、完整 DB diff 或帳號狀態直接丟進 Public repo。

## LINE Pay 警告

舊畫面有出現：

> 使用的裝置不安全

提示內容把 Root、越獄、惡意軟體或來源不明 App 都列成可能原因。

對應 `linepay_fail_logcat.txt` 可以看到流程進到：

```text
com.linecorp.line.pay.base.common.security.VGuardDetectionActivity
```

所以當時 LINE Pay 的阻擋確實有走 VGuard / security detection 這條，不只是一般 Activity 啟動失敗。

## `wallet_v4_diff.txt`

這份是測試前後 LINE 私有資料目錄的 metadata diff，可以看到不少 DB / WAL / SHM timestamp 或大小有變化，包括：

- `follow_state.db`
- `lfl_common`
- `lights_feeds.db`
- `service_chat_database`
- `smart_ch_module_db`
- `square`
- 其他多個 LINE database / WAL

這只能幫忙縮小「操作後哪些資料區有被碰到」，**不能單靠 timestamp 變化就說某個 DB 一定是 Wallet / Root detection 的控制點**。

完整 raw diff 含 `/data/user/0/jp.naver.line.android/...` 私有路徑和大量 App data 結構，所以這裡只留摘要，不直接上原檔。

## 和其他 App 的差別

同一套 Root 環境下，銀行 App 能用不代表 LINE / LINE Pay 一定會放行。LINE 的 security detection 要獨立看版本、Root stack、VGuard 行為和實際提示。

## 待補

- `basic.txt`
- `linepay_OK_logcat.txt`
- `process_OK.txt`
- `activity_OK.txt`
- `vguard_activity_dump.txt`
- `vguard_private_files.txt`
- 成功／失敗狀態的 Root stack 差異表

> **附註：** 內容由 AI 按指定格式上傳整理，有錯、缺漏或其他問題請直接私訊。
