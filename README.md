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

目前整理到的 patch 測試曾達到：

- 去除廣告。
- Smart Channel 依目標移除／停用。
- 移除首頁雜項內容。
- 移除 VOOM。

每一版 patch 都必須另外記錄可能的副作用：

- Wallet tab 可能消失。
- LINE Pay 的 App 內流程可能改成外部開啟。
- 已讀／seen 行為可能與原版不同。
- 收回訊息可能仍保留在本機。
- 行事曆、社群與其他附加工具可能被精簡。
- 外部連結可能改走瀏覽器。

其中**社群功能屬於重要功能**；若某套 patch 會移除或破壞社群，必須在使用前明確標示。

### 版本邊界

目前 repository 尚未完成「LINE 版本 × patch 版本 × Root stack」的完整相容性矩陣。因此上述行為只能視為**已整理到的特定測試結果**，不能直接外推到所有新版本。LINE、patch bundle 或遠端配置更新後都要重新驗證。

## Root / Security Detection

LINE 的 Root／安全性警告要獨立記錄 LINE 版本、Root stack 與實際提示內容。銀行 App 可以正常使用，**不代表 LINE 一定會接受同一套 Root 環境**。

## 與 `patched-apps` 的關係

`patched-apps` 保留作為 Andrew 衍生的 build / patch 工作區；這個 repository 則保存整理過的 LINE 專屬知識、驗證結果與可維護的 source，不把整份 upstream-derived 專案複製一遍。

## 語言規則

README、patch 行為說明、相容性結論與風險提示以繁體中文為主；module ID、指令、檔名、package、shared-preference 路徑與其他技術字串維持原文。

---

> **附註：** 本專案資料由 AI 透過 GitHub 外掛協助整理；若內容有誤、缺漏或其他問題，請私訊聯絡。
