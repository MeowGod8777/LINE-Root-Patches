# Test Ledger — LINE Root Patches

| ID | Baseline | 唯一變數 | 結果 | 結論 | 狀態 |
|---|---|---|---|---|---|
| LINE-26.11-BUILD | LINE 26.11.0 / Andrew module config | module build/install | build、安裝可用 | build chain 成立 | FROZEN；版本/config 改變再測 |
| NS-WLUM | Magisk Alpha + Zygisk Next | child namespace mount behavior | classic mount 到 master/zygote 後，LINE specialization 看回 stock APK | blocker 是 consumer namespace，不是 patch payload 本身 | FROZEN diagnosis |
| NS-LINE-SHIM | 同環境 + LINE-only shim | consumer-view mount | MASTER / RVHC / LINE PROCESS 皆看到 `683853...` patched payload | LINE-only mount shim 已解 namespace blocker | FROZEN；Root/Zygisk 更新後再驗 |
| PAY-MERCHANT | Redirect LINE Pay enabled | merchant `pay/payment/<reserveId>` | 可拉起台灣 standalone LINE Pay | merchant checkout route 已通 | FROZEN |
| PAY-WALLET | 現行 LINE + standalone LINE Pay | Wallet → standalone 首頁 | 尚未完整封箱 | route 仍開放研究 | OPEN |
| PAY-TRANSFER | 聊天室好友轉帳 | standalone transfer flow | static evidence 指向 `epiTransferSendMoney` server-config link；未證實固定公開 scheme | 不猜 `upay://sendmoney` | OPEN / offline-first |
| XML-GUARD | `line_xml_guard_turbo_alpha` 歷史 | boot patch + inotifyd watcher | boot patch/SHA/watcher 有 runtime 證據 | 模組本身曾可運作 | HISTORICAL；Andrew 驗證期間 disabled |
| FUNCTION-MATRIX | 26.11.0 patched payload | 首頁/廣告/Home/VOOM/Pay/社群/行事曆/外鏈/收回/通知 | 部分已日用驗證，Pay Wallet/transfer 仍待補 | 每次 LINE 更新只重測受影響矩陣，不整套無目的重跑 | ACTIVE MATRIX |

Consumer namespace hash 是 mount 成功的必要證據；root/master hash 不能替代。
