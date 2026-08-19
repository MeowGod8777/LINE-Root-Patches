# LINE Root 修補與相容性

這裡集中放 LINE 相關的 Root、patch、相容性和實際行為差異。LINE 的東西就盡量不要再散到其他 repo。

## 這個專案要幹嘛

目標是讓 Root 環境下的 LINE 能正常日用，同時把去廣告／精簡、以前自己做的修補、XML Guard、Root detection、LINE Pay 和版本差異留清楚。

不是只看「能不能安裝」，而是要知道：更新後哪些功能正常、哪些被模塊改掉、哪些舊修補還需要、哪裡可能跳安全警告。

## 目前進度

**狀態：Andrew 模塊 + 之前做過的修補，目前長期日用測試中。**

現在不是單獨靠其中一條路線：

1. **Andrew / `patched-apps` 模塊**：負責目前主要的 LINE 去廣告／Home 精簡／VOOM 處理。
2. **之前自己做的修補與相容性處理**：包含 XML Guard、Root / security detection 研究，以及當時為日用問題做過的補丁／設定。

目前 LINE `26.11.0` 這套環境已能正常日用，但我會把它標成**日用測試中**，不是「永久完成」。LINE 版本、Andrew patch bundle、遠端配置或 Root stack 一變都可能要重測。

目前已確認：

- `patched-apps` 現在鎖 LINE `26.11.0`、`arm64-v8a`、`module`、`exclusive-patches = true`。
- 目前實際只開 5 個 Andrew patch：`Hide ad views`、`Remove banner ads`、`Hide Home modules`、`Disable VOOM`、`Hide VOOM tab`。
- 去廣告、Home 精簡、VOOM 停用／隱藏目前可正常使用。
- XML Guard 的 module 結構、boot patch、SHA 紀錄和 `inotifyd` watcher 行為已有 runtime 證據。
- LINE / LINE Pay 的 Root detection 至少有一條會進 `VGuardDetectionActivity`，銀行 App 能用不代表 LINE 一定會放行。

還在補：

- XML Guard 的 `apply.sh`、`service.sh`、`watch.sh` 等完整原始 source。
- 之前各修補到底哪些現在仍必要、哪些已經被 Andrew 模塊取代。
- LINE Pay / VGuard 成功與失敗狀態的完整對照。
- Wallet、社群、行事曆等功能按每次實際 patch config 做版本驗證。

## LINE XML Guard

歷史 module ID：`line_xml_guard_turbo_alpha`

當時 module 裡有：

- `README.txt`
- `action.sh`
- `apply.sh`
- `manual_reapply.sh`
- `module.prop`
- `service.sh`
- `skip_mount`
- `watch.sh`

目前能確認的行為：

- 開機時會 patch 指定的 LINE shared preference 狀態。
- 修改前後有記 SHA-256。
- 用 `/system/bin/toybox inotifyd` 監看 `/data/user/0/jp.naver.line.android/shared_prefs`。
- 相關檔案變動後可以重新套用。

完整 script source 還沒全部找回來，細節看 `xml-guard/`。

### Public repo 要注意

這個 repo 是 Public。

**真實 LINE preference XML、帳號資料、token、cookie、聊天 DB 之類不要丟上來。** log、截圖、XML、DB 片段在 commit 前先看過有沒有個資。

## Andrew 模塊目前開了什麼

目前 `MeowGod8777/patched-apps/config.toml`：

- LINE：`26.11.0`
- arch：`arm64-v8a`
- build mode：`module`
- `exclusive-patches = true`
- 實際只開：
  - `Hide ad views`
  - `Remove banner ads`
  - `Hide Home modules`
  - `Disable VOOM`
  - `Hide VOOM tab`

所以現在 Andrew 這層主要就是**去廣告、清 Home、關／藏 VOOM**。

過去實測 Smart Channel / Home 雜項也有一起消失，但 config 裡沒有一個獨立叫 Smart Channel 的 patch，所以仍按實測記，不當永久保證。

### Andrew 其他 patch 不等於現在有開

Andrew patch set 還有 Wallet / LINE TODAY、keep chats unread、收回訊息本機保留、外部連結瀏覽器等其他能力。

**這些不在現在的 `exclusive-patches` 清單，就不要當成本機現在已開。**

## Root / Security Detection

LINE 的安全警告獨立看 LINE 版本和 Root stack。

銀行 App 正常 ≠ LINE 一定正常。

## 跟 `patched-apps` 怎麼分

- `patched-apps`：Andrew 模塊的 build workspace。
- 這裡：Andrew 模塊實際日用結果 + 以前自己做的 LINE 修補 + Root / VGuard / XML Guard 研究。

說明用繁中；module ID、指令、檔名、package、shared-preference 路徑等保留原文。

---

> **附註：** 內容由 AI 按指定格式上傳整理，有錯、缺漏或其他問題請直接私訊。
