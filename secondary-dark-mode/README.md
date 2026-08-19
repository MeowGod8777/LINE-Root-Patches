# LINE Secondary Native Dark Mode

> Status: **selective bytecode patch runtime-verified and cold-boot stable on Y700**
>
> Baseline: LINE `26.11.0` (`261100124`), Android Secondary / additional device, Lenovo Y700 4th Gen, 2026-08.

## What this is

LINE 26.11.0 on Android already ships the native dark-theme assets and rendering path on an **additional / Secondary device**. The feature is not missing from the tablet client: an eligibility path rejects Secondary devices before the normal auto-dark/theme checks finish.

The first working proof was made with SimpleHook:

```text
class: q28.n
method: b
parameters: android.content.Context
mode: hook return value
return: true
```

This immediately enabled LINE's **own native dark theme** on the Secondary device. It was verified in fullscreen, split-screen and floating-window use, with Secondary-device behavior otherwise retained.

## Exact 26.11.0 veto

Analysis of the actual Y700-exported LINE 26.11.0 `base.apk` resolved the relevant control flow in:

```text
Lq28/n;->b(Landroid/content/Context;)Z
```

The Secondary-role branch is:

```text
invoke-interface {v0}, Lk40/d0;->f()Z
move-result v0
if-eqz v0, :continue_normal_dark_checks
goto :reject_false
```

The final selective patch replaces **only** the last `goto :reject_false` with `nop`.

That means both device-role results continue into LINE's original dark-mode checks while the following remain intact:

- registration/init prerequisites;
- `THEME_AUTO_DARK_MODE` logic;
- native theme readiness/resource checks;
- the actual Secondary device identity everywhere else.

This is deliberately narrower than the original `q28.n.b(Context) -> true` PoC.

## Runtime validation — 2026-08-19

The combined Root Mount module was hot-installed on the Y700. First, LINE was opened with the original SimpleHook workaround still enabled to verify that the new mounted build itself launched normally. The app opened normally and chat remained functional with no crash.

The SimpleHook rule was then disabled:

```text
q28.n
b(android.content.Context)
return true
```

After force-stopping and reopening LINE, the native dark theme **remained active without the runtime hook**.

Observed with the selective static patch active and the SimpleHook dark-mode rule disabled:

- native dark mode: OK;
- floating window: OK;
- split screen: OK;
- chat: OK;
- app launch: OK;
- VOOM tab: removed;
- no crash observed.

The Y700 was then fully rebooted with the SimpleHook dark-mode rule still disabled. After cold boot, LINE again launched normally with native dark mode intact; floating-window and split-screen rendering remained correct, VOOM remained removed, and no regression was observed in the tested paths. This rules out the earlier success being only a hot-mount / stale-process artifact.

No advertisements were observed during these passes, but this is **not attributed as new evidence for the dark-mode patch**, because the Y700 was already effectively ad-free before this build.

The selective dark-mode patch and combined Root Mount build are therefore considered a **stable baseline for the tested Y700 / LINE 26.11.0 configuration**. Broader paths such as calls and file/image handling remain ordinary regression coverage rather than blockers for the core patch result.

## Relation to other projects

- **Andrew's Patches**: engineering reference for semantic fingerprints, minimal bytecode edits and fail-closed handling. The Y700 build combines Andrew's selected LINE cleanup patches with this one-instruction Secondary dark-mode bypass.
- **Knot**: useful as a reference for respecting LINE's own runtime theme semantics. Knot adapts its injected UI to LINE's active theme; it does not remove this Secondary eligibility veto.

This project does **not** recreate a dark palette, force Android WebView darkening, or globally spoof the device as Primary. LINE's own theme engine remains responsible for colors, icons and layout.

## Safety constraints

- Do **not** globally spoof `isSecondary()` / device type.
- Do not force arbitrary theme files or colors.
- Patch only the dark-mode eligibility path.
- Current compatibility remains pinned to LINE 26.11.0 until another version is statically and functionally verified.
- The selective patch validates the verified `INVOKE_INTERFACE -> MOVE_RESULT -> IF_EQZ -> GOTO` shape and fails closed if it changes.
- Root Mount is used for the Y700 build so the installed LINE package/signing identity is not replaced by a separately signed app install.

## Current stages

| Stage | Method | Status |
|---|---|---|
| PoC-1 | SimpleHook `q28.n.b(Context) -> true` | ✅ runtime verified |
| PoC-2 | static Morphe equivalent of PoC-1 | ✅ CI build verified; retained as research baseline |
| Selective | replace only Secondary reject `goto` with `nop` | ✅ CI + post-build DEX + Y700 runtime verified |
| Combined Y700 | Andrew cleanup patches + selective dark patch, Root Mount | ✅ hot-install + cold-boot verified |
| Stable baseline | SimpleHook disabled; reboot persistence; dark/split/floating/VOOM/chat pass | ✅ verified |

## Stable build

The current combined Y700 module applies:

```text
Disable VOOM
Hide Home modules
Hide VOOM tab
Hide ad views
Remove banner ads
Unlock Secondary native dark mode
```

Module SHA-256:

```text
49be8afdbffc4542ae1a482af8d9ddba1e7b757c1dbeb42755d22ef4e5a3cdb5
```

It deliberately reuses the existing Andrew module ID (`line-andrew-arm64`) so it acts as a replacement/update rather than leaving two Root Mount modules competing over LINE's `base.apk`.

See:

- [`research.md`](research.md) — discovery history and design rationale
- [`dex-26.11.0-y700.md`](dex-26.11.0-y700.md) — exact Y700 DEX control flow
- [`build-verification-0.2.0.md`](build-verification-0.2.0.md) — CI, hashes and post-build bytecode verification
