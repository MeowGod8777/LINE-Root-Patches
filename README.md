# LINE Root 修補與相容性

LINE 在 Root 環境下的 Andrew 模塊、既有修補、去廣告與日用相容性整理。

## 先看這裡

### 這是什麼

這個專案不是只做「LINE 去廣告」。現在實際日用是：

> **Andrew 模塊 + 之前自己做過的修補／相容性處理，一起長期測。**

主要目標是讓 LINE 在 Root 環境下能正常日用，同時知道哪些功能是 Andrew 模塊改的、哪些是以前自己的修補、更新後又壞了什麼。

### 進行時間

- **主要修補／日用驗證：2026-08。**
- Andrew `patched-apps` build、LINE 26.11.0、XML Guard、VGuard / LINE Pay 等整理都集中在這段時間。
- GitHub 系統化整理：2026-08 起。
- 後面只要 LINE / Andrew patch 更新，這條線就會繼續延長，不把 2026-08 當永久封箱點。

### 目前做到哪

**🟢 LINE 26.11.0 目前長期日用測試中。**

目前已確認：

- ✅ Andrew 模塊可正常 build / 安裝／日用。
- ✅ 目前主要效果：去廣告、清 Home、關／藏 VOOM。
- ✅ XML Guard 的 boot patch / SHA / `inotifyd` watcher 有 runtime 證據。
- ✅ LINE / LINE Pay 的 security detection 有獨立行為，不能拿其他 App 是否正常來代替判斷。

還在補：

- 📦 XML Guard 完整原始 scripts 還沒全部找回。
- 🧪 以前自己的修補哪些現在仍必要、哪些已被 Andrew 模塊取代，還要慢慢對。
- 🟢 LINE 更新、patch bundle 或遠端配置變動後都要重新驗證。

### 現在 Andrew 模塊實際開什麼

目前 `patched-apps/config.toml`：

```text
LINE 26.11.0
arm64-v8a
module
exclusive-patches = true
```

只開 5 個 patch：

- `Hide ad views`
- `Remove banner ads`
- `Hide Home modules`
- `Disable VOOM`
- `Hide VOOM tab`

**Andrew upstream 其他功能不等於現在這套也有開。**

### Public repo 注意

這個 repo 是 Public。

真實 LINE preference XML、帳號、token、cookie、聊天 DB、未去識別的私有資料不要丟上來。

---

## 玩機／技術細節

### 現在的兩層結構

#### 1. Andrew / `patched-apps`

負責目前主要 LINE patch / build。

現在 config 只開上面 5 個 patch，所以 Wallet、keep unread、收回訊息保留、外部連結瀏覽器等 Andrew 其他能力，**不能直接寫成目前 build 已啟用**。

#### 2. 以前自己的修補

包含：

- XML Guard。
- Root / security detection 研究。
- LINE Pay / VGuard 診斷。
- 以前為日用問題做過的設定／patch。

後面會逐項確認哪些仍必要。

### LINE XML Guard

歷史 module ID：

`line_xml_guard_turbo_alpha`

已知 module 檔案：

- `README.txt`
- `action.sh`
- `apply.sh`
- `manual_reapply.sh`
- `module.prop`
- `service.sh`
- `skip_mount`
- `watch.sh`

目前能確認：

- boot 時 patch 指定 shared preference 狀態。
- 修改前後記 SHA-256。
- `/system/bin/toybox inotifyd` 監看 LINE shared prefs。
- 變動後可重新套用。

完整 source 尚未全部回收。

### Root / Security Detection

舊失敗 log 有進到：

`com.linecorp.line.pay.base.common.security.VGuardDetectionActivity`

所以 LINE / LINE Pay 的 security detection 要獨立追版本與環境。

銀行 App 正常 ≠ LINE 一定正常。

### 功能回歸

每次 LINE / module 更新至少要測：

- 首頁／廣告。
- Home modules。
- VOOM。
- LINE Pay / Wallet。
- 社群。
- 行事曆。
- 外部連結。
- 收回訊息／已讀行為。

沒在目前 config 裡的 patch，不預設它有作用。

### 跟 `patched-apps` 怎麼分

- `patched-apps`：Andrew 模塊 build workspace。
- 這裡：實際日用結果、以前自己的 LINE 修補、XML Guard、VGuard / security 研究。

### 快速入口

- `COMPATIBILITY.md`：目前日用相容性矩陣。
- `MIGRATION_BACKLOG.md`：舊資料回收。

---

> **附註：** 內容由 AI 按指定格式上傳整理，有錯、缺漏或其他問題請直接私訊。
