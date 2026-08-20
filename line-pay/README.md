# LINE Pay / Standalone 相容性紀錄

這份記錄 LINE 26.11.0 在 Root + Andrew module 環境下，LINE Pay 從 messenger 轉交給台灣獨立 LINE Pay App 的實機與靜態分析結果。

## 基線

```text
LINE: 26.11.0 (261100124)
LINE package: jp.naver.line.android
LINE Pay TW package: com.linepaytw.upay
Root: Magisk Alpha
Zygisk: Zygisk Next
Andrew patched payload SHA-256:
683853ceecac06964eac7703e5f02c47fd43ac3afbeba2f80630e614fbd14289
Standalone LINE Pay base.apk SHA-256:
fb58101ea4ba0e3d2ed93e0f91cf7b5ee5cdfbf10e416eadbebce28aa6ae9ed5
```

## 2026-08-20 checkpoint

### 1. Andrew module 已真正進 LINE process

先前 classic bind mount 會在 Zygisk Next `WL + UM` 的 app specialization 階段被拔掉；用 LINE-only namespace mount shim 修正後，已實測：

```text
MASTER       = 683853...
RVHC         = 683853...
LINE PROCESS = 683853...
```

LINE process mountinfo 亦確認 `/adb/rvhc/line-andrew-andrew-arm64.apk` bind 到當前 LINE `base.apk`。

因此後續 LINE Pay 行為不是「patched APK 沒有跑到」造成。

### 2. ColorOS / App Link / 背景限制不是 blocker

`com.linepaytw.upay` 的 App Links：

```text
web-tw-pay.line.me: verified
```

Android resolver 能找到：

```text
com.linepaytw.upay/.biz.main.SchemeActivity
```

直接 `ACTION_VIEW` `https://web-tw-pay.line.me/R/iab?...` 可正常拉起：

```text
com.linepaytw.upay/.biz.main.MainActivity
```

所以目前不把 ColorOS「關聯啟動」、鎖後台或不限電當成 LINE Pay redirect blocker。

### 3. Andrew `Redirect LINE Pay` 的 merchant checkout route 正常

Andrew 目前 patch 會攔：

- `com.linecorp.line.pay.base.PayLaunchActivity`
- `com.linecorp.line.pay.impl.liff.common.PayLiffActivity`

對 `.../pay/payment/<reserveId>` 會重建 standalone web-pay URL，再交給獨立 LINE Pay App。

實機直接啟動：

```text
PayLaunchActivity + line://pay/payment/TEST123456
```

實際 transition：

```text
LINE PayLaunchActivity
→ com.linepaytw.upay/.biz.main.SchemeActivity
→ com.linepaytw.upay/.biz.main.MainActivity
→ LINE PayLaunchActivity finish
```

因此 merchant `pay/payment/<reserveId>` redirect 已證實可工作。

`AndrewLinePay` 的 `Log.i` 在目前環境沒有留下輸出，後續不把這個 tag 當成唯一 runtime success criterion。

### 4. 目前缺口：Wallet 首頁與好友轉帳

目前使用需求：

1. `Wallet → LINE Pay`：直接開獨立 LINE Pay App。
2. `聊天室 → 轉帳`：仍能執行真正好友轉帳，最好保留聊天對象。

實機點 Wallet / 聊天室轉帳時，沒有走到上面的 `PayLaunchActivity` / `PayLiffActivity` merchant redirect 鏈，因此 Andrew 現有 merchant redirect 不等於完整替代所有 LINE Pay 功能。

## Standalone LINE Pay APK 靜態分析

針對目前實機安裝的 `com.linepaytw.upay` base.apk 離線分析。

### 對外 scheme / route

已看到的公開 route 包括：

```text
upay://?view=mycode
upay://announcement
upay://cardReg
upay://modifyReg?accountId=...
upay://onlineKycIDUpload/?needForceStart=true
https://line.me/R/pay
https://line.me/R/pay/generateQR/?type=DEPOSIT
https://line.me/R/pay/nv/ipass
https://web-tw-pay.line.me/R
```

入口主要為：

```text
com.linepaytw.upay.biz.main.SchemeActivity
com.linepaytw.upay.biz.main.MainActivity
```

### `view=` 可辨識項目

目前 APK 可確認的 `view=` handler 包括：

```text
einvoice-register
mymembership
settings
fullscreenbarcode
trafficqr
settings-paymentmethod
insurance
fullscreenscanmode
loan
verticalmode
couponrecommended
coupontw
couponkr
creditcard
finance
couponkrrecommended
mycode
coupon
fullscreenqrcode
paymenthistory
myeinvoice
```

**沒有看到公開的 `sendmoney` / `transfer` view。**

因此不採用猜測式的 `upay://?view=sendmoney`。

### 好友轉帳功能確實存在，但目前看起來是內部 server-config route

APK 中可確認：

```text
SEND_MONEY
epiTransferSendMoney
TransferAction
InternalTransferSecureConfirmation
ExternalTransferSecureConfirmation
epiTransferQR
TransferInfo
```

`SEND_MONEY` 的 internal callback 最後會用 key：

```text
epiTransferSendMoney
```

從目前 server configuration 的 `ConfigurationResDto.Info` link map 取 URL / link type，再交給 app 內部 dispatcher 啟動。

目前結論：

> 好友轉帳不是 APK 中明顯公開的固定 `upay://sendmoney` deep link，而是由 server config 提供 `epiTransferSendMoney` 對應 URL。

因此下一步先離線追 `Link.type` / dispatcher；若 APK 本身不能還原實際 URL，再只讀取實機目前 LINE Pay 的 server configuration，而不是亂猜 route。

## 設計方向

### Wallet

Wallet → LINE Pay 首頁可以做成明確 standalone launch；這條不需要偽造付款 reserveId。

### 聊天室好友轉帳

優先順序：

1. 找到官方 standalone app 現行 `epiTransferSendMoney` route，若能帶 recipient 則完整轉譯。
2. 若只能進好友轉帳首頁，至少直接落在 standalone 的 transfer UI。
3. 若 standalone 根本沒有可外部帶 recipient 的 route，再記錄限制，不用 merchant `pay/payment/<reserveId>` 硬套。

## 判讀規則

- 不把 merchant checkout redirect 成功，誤寫成 Wallet / P2P transfer 已完整可用。
- 不用不存在的 `sendmoney` deep link 猜測實作。
- 不因 ColorOS 沒有「關聯啟動」UI 就把問題歸因系統限制；以 resolver / runtime transition 為準。
- 不上傳真實 payment reserveId、好友識別資訊、token、cookie 或私人 DB。

> 此文件只記去識別後的技術 checkpoint；實際 patch 完成前，Wallet/P2P 狀態維持「研究中」。
