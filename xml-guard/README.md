# LINE XML Guard

歷史 module ID：`line_xml_guard_turbo_alpha`

## 現在找回了什麼

2026-08-18 的 audit 有抓到當時已安裝的 module 目錄和正在跑的 watcher。

module 裡有：

- `README.txt`
- `action.sh`
- `apply.sh`
- `manual_reapply.sh`
- `module.prop`
- `service.sh`
- `skip_mount`
- `watch.sh`

當時 active process：

```text
toybox inotifyd /data/adb/modules/line_xml_guard_turbo_alpha/watch.sh /data/user/0/jp.naver.line.android/shared_prefs wcmn
```

module log 也有開機 patch 紀錄，以及修改前／後 SHA-256。

## 還缺什麼

目前足夠確認 module 結構和大概怎麼跑，**但 audit 沒有把所有 script 的完整 source body 留下來**。

所以原始檔沒找回前，不靠記憶補一套再寫成 original。之後如果真的重寫，就直接標成重新實作。

## 隱私

這支會碰 LINE shared-preference XML，而那是帳號資料。

這個 repo 是 Public，所以**不要 commit 真實 XML、LINE 帳號識別資料、token、聊天 DB 或其他私人狀態**。要放測試資料就先去識別。
