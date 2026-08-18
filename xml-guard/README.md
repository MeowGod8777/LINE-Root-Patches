# LINE XML Guard

Historical module ID: `line_xml_guard_turbo_alpha`.

## Recovered evidence

A 2026-08-18 audit captured the installed module directory and active watcher. The module contained:

- `README.txt`
- `action.sh`
- `apply.sh`
- `manual_reapply.sh`
- `module.prop`
- `service.sh`
- `skip_mount`
- `watch.sh`

The active process was:

```text
toybox inotifyd /data/adb/modules/line_xml_guard_turbo_alpha/watch.sh /data/user/0/jp.naver.line.android/shared_prefs wcmn
```

The module log also showed a boot-time patch event with before/after SHA-256 values.

## Migration status

The audit proves the module structure and runtime behavior, but it does not contain the full original source bodies for all scripts. Until the exact source files are recovered, this repository must not fabricate replacements and call them the originals.

## Privacy boundary

The guarded LINE shared-preference XML is user/account data. Do not commit the live XML, LINE account identifiers, tokens, chat databases or other private state to this public repository. Any future tests should use sanitized fixtures.
