# Development Contract — LINE Root Patches

先讀本檔、`TEST_LEDGER.md`、`COMPATIBILITY.md`、`line-pay/README.md` 再改 patch / mount / XML Guard。

## Frozen facts

- LINE 26.11.0 的 Andrew module 可 build / 安裝並進入日用驗證。
- Zygisk Next `WL + UM` 曾讓 LINE child namespace 看回 stock APK；LINE-only mount shim 後，MASTER / RVHC / LINE PROCESS 已確認看到同一 patched payload。
- mount 成功必須以 LINE process consumer namespace hash 為準，不以 root/master view 或 UI 猜。
- `Redirect LINE Pay` merchant `pay/payment/<reserveId>` route 已實測可拉起台灣 standalone LINE Pay App。
- XML Guard 有 boot patch / SHA / inotifyd runtime 證據，但 Andrew 日用驗證期間保持 disabled，避免多變數。

## 更新規則

每次 LINE / Andrew / Morphe / Root stack 更新都視為新 baseline，至少保存：LINE version、stock/patched hash、patch bundle/config、module SHA、Root/Zygisk/LSPosed 狀態、consumer namespace hash、功能矩陣。

## 單一變數

- mount/namespace 修復不得同時改 LINE patch set。
- LINE Pay route 研究不得同時啟用 XML Guard。
- UI patch 調整不得順帶改 notification / security workaround。

## 新測試門檻

實機測試前先查 ledger，寫明 baseline、唯一變數、目標功能、成功/失敗判準。已證實 merchant redirect、namespace payload hash 等項目不得無版本變更重測。

## Offline-first

先做 APK/patch source/config diff、intent filter / App Link / static route 分析、module mount script diff。scheme/route 沒證據時不猜 URI 叫使用者試。

## Security / privacy

- 銀行 App 正常 ≠ LINE / LINE Pay security 一定正常。
- 不提交真實 preference XML、token、cookie、聊天 DB、reserveId、好友識別資訊。

## Artifact discipline

每個 known-good build 保存 config、patch source revision、stock/patched SHA、module artifact、signer/provenance、consumer-view verification、功能 regression matrix。
