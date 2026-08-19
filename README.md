# LINE Root 修補與相容性

這裡集中放 LINE 相關的 Root、patch、相容性和實際行為差異。LINE 的東西就盡量不要再散到其他 repo。

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

## LINE 去廣告／精簡

### 現在 `patched-apps` 實際 build 的內容

目前 `MeowGod8777/patched-apps/config.toml` 是：

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

所以現在這套主要就是**去廣告、清 Home、關／藏 VOOM**。

過去實測 Smart Channel / Home 雜項也有一起消失，但 config 裡沒有一個獨立叫 Smart Channel 的 patch，所以這項先當實測結果，不當永久保證。

### Andrew 其他 patch 不等於現在有開

Andrew patch set 還有其他功能，例如：

- Wallet / LINE TODAY tab
- keep chats unread / 已讀行為
- 收回訊息本機保留
- 外部連結走瀏覽器
- 行事曆、社群、其他附加工具精簡

**這些目前不在上面的 `exclusive-patches` 清單裡，所以不要把它們寫成現在這個 build 已經有。** 之後真的加進 config，再分版本記結果。

社群功能我會另外注意；哪版會動到社群，要在使用前直接標出來。

### 版本問題

現在明確鎖 LINE `26.11.0`。LINE、patch bundle 或遠端配置一更新，都要重新測。舊結果不要直接套到新版本。

## Root / Security Detection

LINE 的安全警告獨立看 LINE 版本和 Root stack。

銀行 App 正常 ≠ LINE 一定正常。這兩個不要混著判斷。

## 跟 `patched-apps` 怎麼分

- `patched-apps`：拿來 build Andrew 的 patch。
- 這裡：放 LINE 實測結果、XML Guard、Root 相容性和版本差異。

說明用繁中；module ID、指令、檔名、package、shared-preference 路徑等保留原文。

---

> **附註：** 內容由 AI 按指定格式上傳整理，有錯、缺漏或其他問題請直接私訊。
