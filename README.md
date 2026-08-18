# LINE Root Patches

All LINE-specific root, patch, compatibility and behavior research.

## LINE XML Guard

Historical module ID: `line_xml_guard_turbo_alpha`.

Observed module files:
- `README.txt`
- `action.sh`
- `apply.sh`
- `manual_reapply.sh`
- `module.prop`
- `service.sh`
- `skip_mount`
- `watch.sh`

Observed behavior:
- boot-time patching of selected LINE shared-preference state
- SHA-256 before/after logging
- `/system/bin/toybox inotifyd` watches `/data/user/0/jp.naver.line.android/shared_prefs`
- reapply path after relevant file activity

### Privacy rule

Never commit real LINE preference XML, account identifiers, tokens, cookies, chat databases or other private account state. Publish only generic patch logic, sanitized fixtures and documentation.

## LINE patch / slimming work

Target behavior from current patch testing:
- ads removed
- Smart Channel removed/disabled as intended
- Home-page junk removed
- VOOM removed

Behavior changes that must be documented per patch version:
- Wallet tab may disappear
- LINE Pay in-app flow may be redirected externally
- read/seen behavior can differ from stock
- recalled messages may remain locally depending on patch
- calendar/community/add-on functions may be reduced
- external links may open in the browser

Community functionality is important enough that any patch set removing it must be called out before use.

## Root/security detection

Track LINE version, root stack and exact warning behavior independently from banking-app compatibility. A device passing bank apps does not prove LINE will accept the same environment.

## Relationship to `patched-apps`

`patched-apps` remains the Andrew-derived build workspace. This repository is the curated LINE-specific knowledge/source layer; the entire upstream-derived repository is not mirrored here.
