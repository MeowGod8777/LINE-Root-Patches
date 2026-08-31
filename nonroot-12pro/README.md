# iQOO 12 Pro / LINE 26.11.0 non-root 結案

狀態：**🟢 2026-08-31 結案，已進入日用。**

目標是在 iQOO 12 Pro（V2329A / OriginOS / Android 16 / 無 Root）使用重簽 LINE 26.11.0，同時保留聊天備份／復原、背景通知與 LINE Pay standalone redirect。

## 最終可用架構

```text
LINE 26.11.0 (jp.naver.line.android)
  └─ Andrew / morphe-patches
       └─ Fix chat backup sign-in via GmsCore
            └─ app.revanced.android.gms
                 └─ Morphe MicroG-RE 6.1.4

YouTube ReVanced
  └─ 同一顆 app.revanced.android.gms / Morphe MicroG-RE 6.1.4
```

**重點：只需要一顆 MicroG。**

Morphe MicroG-RE 6.1.4 保持 upstream package / account type：

- package：`app.revanced.android.gms`
- account type：`app.revanced`

LINE 與 YouTube ReVanced 已實測可共用。

## 最終 LINE patch 組合

實際 build 設定在 [`config.toml`](config.toml)。目前包含：

- `Hide ad views`
- `Remove banner ads`
- `Disable VOOM`
- `Hide VOOM tab`
- `Hide Home modules`
- `Hide Home content feed`
- `Hide LINE TODAY tab`
- `Hide Shopping tab`
- `Hide Wallet tab`
- `Redirect LINE Pay`
- `Fix push notifications`
- `Fix chat backup sign-in via GmsCore`

Build mode：`apk`；LINE：`26.11.0`；arch：`arm64-v8a`。

## 實機驗證

2026-08-31 在 iQOO 12 Pro 完成：

- ✅ patched LINE 可正常登入與日用。
- ✅ `Fix chat backup sign-in via GmsCore` 能叫出 `app.revanced.android.gms` 的帳號選擇器。
- ✅ Google Drive 聊天記錄可從 Note 12 Turbo 備份並在 12 Pro 復原。
- ✅ Morphe MicroG-RE 6.1.4 與 YouTube ReVanced 共存／共用；YouTube 影片可正常播放。
- ✅ 去廣告／Home 精簡／VOOM／LINE TODAY／Shopping／Wallet 等 patch 基本行為正常。
- ✅ LINE Pay standalone redirect 保留。
- ℹ️ 復原後貼圖排序等部分本機 UI 狀態沒有完整保留；不影響聊天記錄復原，視為小問題。

## 為什麼不是兩顆 MicroG

中途曾嘗試把 MicroG-RE 改成：

- package：`app.line12pro.android.gms`
- account type：`app.line12pro`

並把 Andrew `gmscoreauth` 常數 retarget 到該 namespace。這條路已**廢棄**。

原因不是 Android 不能同時裝兩顆，而是 Andrew 的 LINE GmsCore auth patch 本來就針對 `app.revanced.android.gms` 的 MicroG-RE 實作，備份流程涉及多個 picker / account type / auth service / explicit `GetToken` ComponentName site。自行換 namespace 會額外產生多個必須同步修改的 redirect site，沒有必要。

實驗中確認：

- `app.line12pro` AccountManager authenticator 可註冊、帳號可新增。
- shell 裸啟動 `AccountPickerActivity` 因缺少正常 caller extras 會在 `Intent.putExtras()` 出現 NPE；這不是正常 LINE caller 的有效測試。
- 更重要的是，LINE 實際 UI 沒有走完整的 custom namespace auth path。

因此最終回到 upstream Andrew 設計：**單一 Morphe MicroG-RE 6.1.4 / `app.revanced.android.gms`。**

已刪除兩個 obsolete workflow：

- `build-line12pro-dedicated-microg.yml`
- `build-microg-line12pro.yml`

避免之後誤跑錯誤架構。

## MicroG 版本

目前鎖定 **Morphe MicroG-RE 6.1.4**，因為 Andrew 的 LINE 26.11.0 GmsCore chat backup/restore 路徑就是以此版本完成 Android 16 實機驗證。

目前不因 YouTube 的「GmsCore 有新版」提示主動升級；先以 LINE restore + YouTube playback 都正常的 6.1.4 當穩定基準。

## Build

最終 workflow：

[`../.github/workflows/build-line12pro-nonroot.yml`](../.github/workflows/build-line12pro-nonroot.yml)

它直接使用 upstream `andrewliang25/patched-apps` + `nonroot-12pro/config.toml`，**不再修改 MicroG namespace，也不再生成自訂 MicroG APK**。

已知成功的 upstream Andrew LINE artifact baseline：

- run `33366029969`
- LINE artifact `9748342254`
- Actions digest `sha256:9491b62053df3de560860f9ee4b1b0a947a101eb1d8a024575e874f912e0ea79`

注意：該 run 同時曾產生一顆 CI 自簽 MicroG artifact；**不要使用那顆 MicroG**。MicroG 應使用 Morphe 官方 6.1.4 release。

## 安裝／移轉要點

1. 使用 Morphe MicroG-RE 6.1.4，package 保持 `app.revanced.android.gms`。
2. 在 MicroG 加入與舊機 LINE Drive 備份相同的 Google 帳號。
3. 安裝 upstream Andrew patch 的 LINE 26.11.0。
4. LINE 本身用正常 LINE 登入方式登入；Google Credential Manager 的 LINE 帳號登入／綁定不是本 patch 的目標。
5. 到 LINE「備份及復原聊天記錄」內選 Google 帳號；此處應叫出 MicroG picker。
6. 完成 Drive restore 後再確認舊機聊天記錄，不要提早清除舊機。

## 結論

12 Pro non-root LINE 主線已封箱：

> **LINE 26.11.0 + Andrew patches + Morphe MicroG-RE 6.1.4（單一 `app.revanced.android.gms`）= 可日用，Google Drive restore 已實機完成。**
