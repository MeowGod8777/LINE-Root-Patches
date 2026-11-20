# Y700 / ColorOS 16 — LINE 26.11.0 codePath recovery — 2026-11-20

## Incident class

After an unrelated Y700 WFD runtime-restart experiment, LINE disappeared from the launcher.

This was not a normal app uninstall.

PackageManager still retained:

```text
package = jp.naver.line.android
versionCode = 261100124
versionName = 26.11.0
appId = 10353
uid = u0_a353
installed = true
CE inode = 53745
DE inode = 53742
dataDir = /data/user/0/jp.naver.line.android
private data ≈153M
installer = com.android.vending
```

Decoded `packages.xml` also retained the current LINE signing certificate and past-signing lineage.

The recorded old `/data/app/.../jp.naver.line.android-...` path no longer existed.

Therefore the failure was:

> PackageSetting/private data survived, but the physical installed APK code path disappeared.

## Rescue-first rule

Before any install attempt the following were backed up and SHA-verified:

- user 0 CE data;
- user 0 DE data;
- `/data/system/packages.xml`;
- user 0 package restrictions.

No uninstall was allowed.

## Stock source

Used only as a source of original stock splits:

```text
MeowGod8777/patched-apps Release 8
line-andrew-module-v26.11.0-arm64-v8a.zip
SHA256 a8251c364988bf27c14c42f4de725e80fc0cff5a92a3bdad9c8da67b14a59892
```

Stock split hashes:

```text
base.apk
3dad00933c2f3071565efb9b533187cd6c37c779ea92c8989e53dfe095680077

split_config.arm64_v8a.apk
b73f4c6ae72a715abdde71cf7296c6af8ab90284b4840496f3cbe2028ca7d694

split_config.xxhdpi.apk
b4e93ead10f9613b2ef11fd8775f5d577a259bc029ecdc082586b8bdc96594f9

split_config.xxxhdpi.apk
d6e05dbbf3ed831bde9834675b51b05861397514d486c863f61b05cb02ba26ad
```

Note: this Y700 stock base hash differs from the older Note 12 Turbo stock baseline and must not be conflated with it.

## Safe install path

`pm install-multiple` is not available in this ColorOS `pm` wrapper.

The working route was:

```text
copy splits to /data/local/tmp
restore readable shell_data_file context
pm install-create --user 0 -i com.android.vending -r -S <total>
pm install-write -S <size> <session> <split-name> <file>
...
pm install-commit <session>
```

On any write failure the session is abandoned.

On commit failure: STOP. No uninstall fallback.

Result:

```text
install-commit = Success
new /data/app codePath created
appId remained 10353
CE inode remained 53745
DE inode remained 53742
private data remained ≈153M
version remained 26.11.0
```

## Andrew payload restoration

Current installed Y700 module:

```text
id=line-andrew-arm64
name=LINE MeowGod-Andrew
version=v26.11.0 (patches 1.6.0.mpp)
versionCode=20260819
```

Current payload:

```text
/data/adb/rvhc/line-andrew-arm64.apk
SHA256 8e65e24b06827d533309acea681e3f06b867e9cf8e84363fbf8444904bedd19a
```

Before Action:

```text
stock base hash = 3dad0093...
LINE mount = none
```

After Action:

```text
Enabled successfully
module description = LINE MeowGod-Andrew module
master-visible base hash = 8e65e24b...
appId/data unchanged
```

This proves the module can re-bind the preserved patched payload after the stock package code path is safely reconstructed.

## Safety implications

Do not reflash the module ZIP as a first rescue step.

The historical installer contains logic that may uninstall on certain install failures. That behavior is unacceptable while the original PackageSetting/UID/private data still survive.

For this failure class:

```text
backup
→ verify PackageSetting/signature/UID
→ exact stock replace-only recovery
→ verify data identity
→ re-bind patched payload
```

Never:

```text
uninstall first
clear data
blind module reflash
runtime service restart as "recovery"
```

## Related Y700 system incident

The same whole-device audit found YouTube Music 9.15.51 with an analogous missing physical `/data/app` code path while its PackageSetting/private data survived.

This indicates the LINE event was part of a broader device/package-code incident, not enough evidence to blame the LINE patch itself.

System-level record:

`MeowGod8777/Lenovo-Y700-4th-ColorOS16/diagnostics/wfd/2026-11-20-hwc-vds-runtime-restart-incident.md`


## Controlled reboot verification

After the full Y700 recovery was complete, a normal reboot was performed.

Post-boot audit:

```text
BOOT_ID=1fc770fe-4cc7-40e3-89c9-c8f8fc485683

LINE
appId=10353
CE inode=53745
DE inode=53742
installed=true
```

The user confirmed LINE opened normally and data remained intact after reboot.

Status: **RECOVERY COMPLETE / POST-REBOOT VERIFIED**.

Temporary rescue copies were removed only after this verification. The active Andrew payload/module state was retained.
