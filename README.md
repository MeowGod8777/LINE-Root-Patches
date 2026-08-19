# LINE Root 修補與相容性研究

這個 repository 集中保存所有 LINE 專屬的 Root、patch、相容性與行為差異研究。

## LINE XML Guard

歷史 module ID：`line_xml_guard_turbo_alpha`

已知 module 檔案：

- `README.txt`
- `action.sh`
- `apply.sh`
- `manual_reapply.sh`
- `module.prop`
- `service.sh`
- `skip_mount`
- `watch.sh`

已驗證到的行為：

- 開機時 patch 指定的 LINE shared preference 狀態。
- 修改前後記錄 SHA-256。
- 使用 `/system/bin/toybox inotifyd` 監看 `/data/user/0/jp.naver.line.android/shared_prefs`。
- 偵測到相關檔案變化後，可觸發重新套用。

### 隱私規則

**不得上傳真實 LINE preference XML、帳號識別資料、token、cookie、聊天資料庫或其他私人帳號狀態。**

GitHub 只保存通用 patch 邏輯、去識別的測試 fixture 與說明文件。此 repository 目前為 Public，因此任何 log、截圖、XML 或資料庫片段在 commit 前都必須先人工確認已去識別。

## LINE 去廣告／精簡 patch

### 目前 `patched-apps` fork 的實際 build 設定

目前 `MeowGod8777/patched-apps` 的 `config.toml` 為固定且可核對的設定：

- LINE version：`26.11.0`
- arch：`arm64-v8a`
- build mode：`module`
- `exclusive-patches = true`
- 目前只包含：
  - `Hide ad views`
  - `Remove banner ads`
  - `Hide Home modules`
  - `Disable VOOM`
  - `Hide VOOM tab`

因此目前 fork 的 build 目標是**去廣告、清理 Home 模組與停用／隱藏 VOOM**。過去實測中 Smart Channel / Home 雜項也有被清掉的結果，但目前 config 並沒有一個獨立名為 Smart Channel 的 patch；這項效果應視為實測觀察，後續版本仍要重新確認。

### 不應誤認為目前 build 已啟用的 Andrew 其他功能

Andrew patch set 還可能提供或曾討論過其他行為，例如：

- 移除 Wallet / LINE TODAY tab。
- keep chats unread / 改變已讀／seen 行為。
- 收回訊息在本機保留。
- 外部連結改走瀏覽器。
- 其他行事曆、社群、附加工具相關精簡。

**這些目前不在上述 `exclusive-patches` 清單內，因此不得寫成目前 `MeowGod8777/patched-apps` build 的既定行為。** 如果未來把它們加入 config，再另外記錄版本、副作用與實測結果。

其中**社群功能屬於重要功能**；若未來某套 patch 會移除或破壞社群，必須在使用前明確標示。

### 版本邊界

目前 fork 明確鎖在 LINE `26.11.0`，而 repository 尚未完成「LINE 版本 × patch 版本 × Root stack」的完整相容性矩陣。因此上述結果只能視為**目前版本與已整理測試狀態**，不能直接外推到所有新版本。LINE、patch bundle 或遠端配置更新後都要重新驗證。

## Root / Security Detection

LINE 的 Root／安全性警告要獨立記錄 LINE 版本、Root stack 與實際提示內容。銀行 App 可以正常使用，**不代表 LINE 一定會接受同一套 Root 環境**。

## 與 `patched-apps` 的關係

`patched-apps` 保留作為 Andrew 衍生的 build / patch 工作區；這個 repository 則保存整理過的 LINE 專屬知識、驗證結果與可維護的 source，不把整份 upstream-derived 專案複製一遍。

## 語言規則

README、patch 行為說明、相容性結論與風險提示以繁體中文為主；module ID、指令、檔名、package、shared-preference 路徑與其他技術字串維持原文。

---

> **附註：** 本專案資料由 AI 透過 GitHub 外掛協助整理；若內容有誤、缺漏或其他問題，請私訊聯絡。
