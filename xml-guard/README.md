# LINE XML Guard

歷史 module ID：`line_xml_guard_turbo_alpha`

## 已回收證據

2026-08-18 的 audit 有抓到已安裝 module 目錄與當時正在運作的 watcher。module 內包含：

- `README.txt`
- `action.sh`
- `apply.sh`
- `manual_reapply.sh`
- `module.prop`
- `service.sh`
- `skip_mount`
- `watch.sh`

當時可見的 active process：

```text
toybox inotifyd /data/adb/modules/line_xml_guard_turbo_alpha/watch.sh /data/user/0/jp.naver.line.android/shared_prefs wcmn
```

module log 也有記錄開機時 patch 的事件，以及修改前／後 SHA-256。

## 回收狀態

目前證據足以確認 module 結構與 runtime 行為，但 audit **沒有包含所有腳本的完整原始 source body**。

在真正原始檔找回之前，不靠記憶補出替代腳本再標成 original；若之後需要重寫，必須明確標記為重新實作。

## 隱私邊界

被監看的 LINE shared-preference XML 屬於使用者／帳號資料。這個 public repository **不得 commit 真實 XML、LINE 帳號識別資料、token、聊天資料庫或其他私人狀態**。未來測試資料一律使用去識別 fixture。
