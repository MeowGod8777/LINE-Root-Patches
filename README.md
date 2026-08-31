# LINE 修補與相容性

LINE 在 Root / non-root 環境下的 Andrew 模塊、既有修補、去廣告與日用相容性整理。

## 先看這裡

### 這是什麼

這個專案不是只做「LINE 去廣告」。目前同時保存兩條已實機驗證的路線：

- **Note 12 Turbo Root**：Andrew module + namespace mount 相容性 + LINE Pay / XML Guard 等長期日用研究。
- **iQOO 12 Pro non-root**：重簽 LINE APK + Andrew patches + Morphe MicroG-RE，已完成 Google Drive 聊天復原並結案。

### 進行時間

- **主要修補／日用驗證：2026-08。**
- Andrew `patched-apps` build、LINE 26.11.0、XML Guard、VGuard / LINE Pay、Zygisk Next mount namespace、12 Pro non-root GmsCore restore 都集中在這段時間。
- GitHub 系統化整理：2026-08 起。
- 後面只要 LINE / Andrew patch 更新，Root 日用線仍會繼續延長。

### iQOO 12 Pro non-root：已結案

**🟢 2026-08-31：LINE 26.11.0 non-root 主線已封箱。**

最終架構：

```text
LINE 26.11.0 / jp.naver.line.android
  └─ Andrew morphe-patches
       └─ Fix chat backup sign-in via GmsCore
            └─ Morphe MicroG-RE 6.1.4
                 package = app.revanced.android.gms
                 account type = app.revanced

YouTube ReVanced
  └─ 共用同一顆 Morphe MicroG-RE 6.1.4
```

已確認：

- ✅ patched LINE 正常登入／日用。
- ✅ MicroG 帳號選擇器正常叫出。
- ✅ Note 12 Turbo → Google Drive → iQOO 12 Pro 聊天記錄復原完成。
- ✅ YouTube ReVanced 與 LINE 共用同一顆 `app.revanced.android.gms`，YouTube 播放正常。
- ✅ 去廣告、Home 精簡、VOOM、LINE TODAY、Shopping、Wallet 等 patch 基本行為正常。
- ✅ LINE Pay standalone redirect 保留。
- ℹ️ 復原後貼圖排序等部分本機 UI 狀態沒有完整保留，視為小問題。

中途實驗過 `app.line12pro.android.gms` 第二顆 MicroG 的雙 namespace 方案，已證明沒有必要並廢棄；相關 workflow 已刪除。最終回到 Andrew upstream 已實測的單一 Morphe MicroG-RE 6.1.4 架構。

詳細結案紀錄：[`nonroot-12pro/README.md`](nonroot-12pro/README.md)

最終 build workflow：[`build-line12pro-nonroot.yml`](.github/workflows/build-line12pro-nonroot.yml)

### Note 12 Turbo Root 線目前做到哪

**🟢 LINE 26.11.0 目前長期日用測試中。**

目前已確認：

- ✅ Andrew 模塊可正常 build / 安裝。
- ✅ Zygisk Next `WL + UM` 造成 LINE child namespace 看回 stock APK 的問題已定位；LINE-only mount shim 後已實測 patched payload 真正進 LINE process。
- ✅ 目前 `MASTER / RVHC / LINE PROCESS` 均為 `683853...` patched payload。
- ✅ 去廣告、Home 精簡、VOOM 停用／隱藏已進實機驗證。
- ✅ Andrew `Redirect LINE Pay` 的 merchant `pay/payment/<reserveId>` route 已實測可轉到台灣獨立 LINE Pay App。
- ✅ ColorOS 關聯啟動／不限電不是目前 Pay redirect blocker；`web-tw-pay.line.me` App Link 已 verified，direct ACTION_VIEW 可拉起 `com.linepaytw.upay`。
- 🧪 Wallet → standalone LINE Pay 首頁、聊天室好友轉帳 → standalone 真實轉帳目前繼續補 route。
- ✅ XML Guard 的 boot patch / SHA / `inotifyd` watcher有 runtime 證據，但目前為避免跟 Andrew 驗證互相干擾，日用 Andrew 驗證階段保持停用。

### 現在 Andrew 模塊實際怎麼 build

目前 `patched-apps/config.toml` 不是舊的 `exclusive-patches = true / 只開 5 patch` 模式，而是保留 Andrew default patch，再排除不想要的項目：

```text
LINE 26.11.0
arm64-v8a
build-mode = module
include-stock = auto
enable-module-update = false
```

目前 exclusions：

- `Hide Wallet tab`
- `Hide Transfer button`
- `Keep chats unread`
- `Hide community button`
- `Fix push notifications`
- `Fix chat backup sign-in via GmsCore`

所以 `Redirect LINE Pay` **目前有啟用**；Wallet / Transfer UI 則刻意保留。

### Public repo 注意

這個 repo 是 Public。

真實 LINE preference XML、帳號、token、cookie、聊天 DB、payment reserveId、好友識別資訊、未去識別的私有資料不要丟上來。

---

## 玩機／技術細節

### 現在的兩層結構

#### 1. Andrew / `patched-apps`

負責目前主要 LINE patch / build。

實際 build 內容以 `patched-apps/config.toml`、`nonroot-12pro/config.toml`、Andrew patch source 與當次 payload hash 為準，不用 upstream README 的完整 feature list直接推現在行為。

#### 2. Root / runtime 相容性

這個 repo 記：

- XML Guard。
- Root / security detection。
- LINE Pay / VGuard。
- Zygisk Next mount namespace。
- Andrew module 實機功能回歸。
- 以前自己的設定／patch 是否仍有必要。

### Zygisk Next / namespace mount

Turbo 使用 Magisk Alpha + Zygisk Next。已實測 classic Andrew bind mount 可到 master / zygote，但在 LINE app specialization 後被 `WL + UM` 拔掉，導致 LINE process 看到 stock base.apk。

LINE-only mount shim 修正後，已確認：

```text
MASTER       = 683853...
RVHC         = 683853...
LINE PROCESS = 683853...
```

因此這個 blocker 已封箱；後續不靠 UI 猜 mount 是否有效，以 process namespace hash 為準。

### LINE Pay

詳細 checkpoint：[`line-pay/README.md`](line-pay/README.md)

目前已證實 merchant checkout redirect 正常；正在補：

- Wallet → standalone LINE Pay 首頁。
- 聊天室好友轉帳 → standalone 真實 transfer flow。

Standalone LINE Pay APK 靜態分析目前顯示好友轉帳使用 `epiTransferSendMoney` server-config link；尚未看到公開固定的 `upay://sendmoney` route，所以不猜 scheme 硬做。

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

完整 source 尚未全部回收；目前 Andrew 驗證期間保持 disabled，避免多變數。

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
- LINE Pay merchant checkout / Wallet / transfer。
- 社群。
- 行事曆。
- 外部連結。
- 收回訊息／已讀行為。
- 背景通知。
- process namespace 是否仍吃到 patched payload。

### 跟 `patched-apps` 怎麼分

- `patched-apps`：Andrew 模塊 build workspace / upstream sync / config。
- 這裡：實際日用結果、non-root APK 路線、namespace 相容性、以前自己的 LINE 修補、XML Guard、VGuard / security、LINE Pay route 研究。

### 快速入口

- [`COMPATIBILITY.md`](COMPATIBILITY.md)：目前日用相容性矩陣。
- [`nonroot-12pro/README.md`](nonroot-12pro/README.md)：iQOO 12 Pro non-root 結案。
- [`line-pay/README.md`](line-pay/README.md)：Standalone LINE Pay redirect / transfer 研究。
- [`MIGRATION_BACKLOG.md`](MIGRATION_BACKLOG.md)：舊資料回收。

---

> **附註：** 內容由 AI 按指定格式上傳整理，有錯、缺漏或其他問題請直接私訊。