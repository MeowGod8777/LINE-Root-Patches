# Build verification — Secondary native dark mode 0.2.0-dev

Date: 2026-08-19

Status: **build verified / bytecode verified / device validation pending**

## Device-exported analysis input

```text
LINE_26.11.0_Y700_base.apk
SHA-256 762025b5785bd7cab26a01b0e9644fd972549200e665f607d2593ba34ba261ee
```

This input was used to resolve the exact `q28.n.b(Context)` control flow and identify the single Secondary-role reject edge.

## Standalone Morphe bundle

```text
patches-0.2.0-dev.mpp
size     11289 bytes
SHA-256  fc1c12957f4df7ed9d4eeb2ba1942bda9d7c46f60511a00c20591c70859fe31f
```

The standalone bundle contains the retained broad PoC plus the new selective patch. CI compilation completed successfully.

## Combined Andrew + MeowGod Y700 build

Pinned upstream inputs:

```text
Andrew patches: andrewliang25/morphe-patches
commit: 01824ca7983f53c79e3f88e86b5818e9f9318ab2
release line: 1.6.0

patched-apps builder: MeowGod8777/patched-apps
commit: a855e8ed2e66f8b330959925713feecf33efdf9f
```

The selective patch source was injected into the pinned Andrew 1.6.0 patch project and compiled as one Morphe bundle.

```text
combined patches-1.6.0.mpp
SHA-256 f09bdc2b90bab89a96bb61545ae2d38d7013e8a4645da6881e80df7db03a621d
```

Morphe reported all six requested patches as applied:

```text
Disable VOOM
Hide Home modules
Hide VOOM tab
Hide ad views
Remove banner ads
Unlock Secondary native dark mode
```

## Root Mount test module

```text
file:
line-meowgod-andrew-module-v26.11.0-arm64-v8a.zip

size:
210530492 bytes

SHA-256:
49be8afdbffc4542ae1a482af8d9ddba1e7b757c1dbeb42755d22ef4e5a3cdb5
```

Module identity:

```text
id=line-andrew-arm64
name=LINE MeowGod-Andrew
version=v26.11.0 (patches 1.6.0.mpp)
versionCode=20260819
```

The module deliberately reuses the existing Andrew module ID so installing it acts as a replacement/update rather than leaving two independent modules racing to mount `base.apk`.

Internal APK hashes:

```text
patched base.apk
SHA-256 8e65e24b06827d533309acea681e3f06b867e9cf8e84363fbf8444904bedd19a

module stock/base.apk
SHA-256 3dad00933c2f3071565efb9b533187cd6c37c779ea92c8989e53dfe095680077
```

## Post-build bytecode verification

The original stock control-flow code units around the Secondary veto are:

```text
pc15  1072 ...      invoke-interface Lk40/d0;->f()Z
pc18  000a          move-result v0
pc19  0038 0003     if-eqz v0, pc22
pc21  3628          goto pc75       # reject false
pc22  0071 ...      continue normal dark-mode checks
```

The final Root Mount module's patched `base.apk` contains:

```text
pc15  1072 ...      invoke-interface Lk40/d0;->f()Z
pc18  000a          move-result v0
pc19  0038 0003     if-eqz v0, pc22
pc21  0000          nop             # selective change
pc22  0071 ...      continue normal dark-mode checks
```

Method reference indexes differ after Morphe's DEX rewrite, as expected. The relevant opcode/control-flow shape is otherwise preserved. This confirms that the final artifact does **not** implement the old `return true` PoC; it neutralizes only the verified Secondary -> reject edge.

## Builder warning

`patched-apps` emitted its existing non-fatal `Patching revanced-integrations failed` helper warning while preprocessing the locally built combined MPP. Morphe subsequently loaded the bundle, applied all six requested patches, compiled/signed the APK and produced the Root Mount module successfully. No patch application failure was reported.

## Remaining device validation

Before promoting this from test candidate to daily/stable:

1. Install the combined module over the existing `line-andrew-arm64` module.
2. Disable the SimpleHook `q28.n.b(Context) -> true` rule.
3. Confirm LINE remains native-dark after force-stop/reopen.
4. Confirm Secondary registration/behavior remains intact.
5. Recheck fullscreen, split-screen and floating-window rendering.
6. Recheck chat, media/file handling and calls.
7. Confirm VOOM remains removed and Andrew ad/Home patches still behave as before.

Only after that device pass should 0.2.0 be marked tested/stable.
