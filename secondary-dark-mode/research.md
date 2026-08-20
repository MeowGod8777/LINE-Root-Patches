# Research log — Secondary native dark mode

## 2026-08 — symptom

On an Android **additional / Secondary device**, LINE 26.11.0 did not expose/use the same native dark mode that was available on a normal Primary Android installation. ColorOS force-dark was not considered a satisfactory fix because the goal was LINE's own theme implementation.

## Static finding

Offline inspection of LINE 26.11.0 identified a dark-mode eligibility method at the then-obfuscated symbol:

```text
q28.n.b(android.content.Context): boolean
```

The relevant path included a Secondary-device rejection before the remaining auto-dark/theme checks. The device-role path ultimately involved the Secondary device-type state (`SECONDARY`; 26.11.0 symbols observed around `k40.d0` / `k40.i0`).

Important: these obfuscated names are **version evidence, not a cross-version API**.

The useful semantic observation is:

```text
normal eligibility preconditions
        ↓
Secondary device?
        ├─ yes -> reject dark-mode eligibility
        └─ no  -> continue normal dark-mode checks
                    ↓
              auto-dark preference / theme readiness
```

## Runtime proof — original SimpleHook PoC

SimpleHook scope: `jp.naver.line.android`

```text
Class          q28.n
Method         b
Parameter      android.content.Context
Hook action    return value
Return value   true
```

Observed result on Lenovo Y700 4th Gen / ColorOS 16 port:

- native LINE dark theme rendered correctly;
- fullscreen: OK;
- split screen: OK;
- floating window: OK;
- ordinary Secondary-device behavior remained usable;
- no need to globally falsify the device role;
- no polling or script loop was required.

This is strong evidence that the Secondary client already contains the required native theme assets and renderer and that eligibility, rather than missing UI resources, is the blocker.

## Why `return true` is not the preferred final patch

The PoC forces the **whole predicate** true. That is broader than the actual intended change. It can skip conditions that should remain authoritative, including readiness/preference checks around the native dark-theme path.

Therefore the PoC is intentionally retained as a reproducible discovery method, not presented as the clean final implementation.

## Morphe PoC build checkpoint — 2026-08-19

A standalone Morphe patch bundle contains a static equivalent of the verified SimpleHook PoC. It is pinned to LINE 26.11.0 and uses the exact known symbol fingerprint (`Lq28/n;->b(Landroid/content/Context;)Z`).

CI initially exposed two build-environment omissions and was fixed rather than worked around:

1. missing Gradle version catalog required by the Morphe Gradle plugin;
2. missing Kotlin `-Xcontext-parameters` compiler flag required by the current Morphe API.

After those corrections, GitHub Actions run `32239248900` compiled successfully and produced:

```text
patches-0.1.0-dev.mpp
size: 7615 bytes
SHA-256: a63de30502ede5e50934eab3d8eb49f7a683514e8f545c6072b38c4226560e12
```

This checkpoint proves the custom patch project itself builds. It is retained as the static counterpart of the first runtime proof, not as the preferred final implementation.

## Selective patch design

### Goal

Change only:

```text
Secondary -> reject
```

to:

```text
Secondary -> continue normal eligibility checks
```

### Preserve

- LINE registration/init prerequisites;
- the user's normal auto-dark/theme preference logic;
- native dark-theme resource/readiness checks;
- the real Secondary device identity everywhere else.

### Verified LINE 26.11.0 control flow

Using the Y700-exported LINE 26.11.0 APK, the Secondary-veto path was resolved to:

```text
invoke-interface {v0}, Lk40/d0;->f()Z
move-result v0
if-eqz v0, :continue_normal_dark_checks
goto :reject_false
```

The selective Morphe patch validates that instruction shape and replaces only the final `goto :reject_false` with `nop`. The patch therefore turns the Secondary result into normal fall-through rather than forcing the whole method true.

The build remains intentionally pinned to LINE 26.11.0. If the target shape changes, the patch should fail rather than guess.

## Selective runtime validation — 2026-08-19

Test build:

```text
LINE 26.11.0
arm64-v8a
Root Mount
module SHA-256:
49be8afdbffc4542ae1a482af8d9ddba1e7b757c1dbeb42755d22ef4e5a3cdb5
```

Combined patches:

```text
Disable VOOM
Hide Home modules
Hide VOOM tab
Hide ad views
Remove banner ads
Unlock Secondary native dark mode
```

Test procedure:

1. hot-install the replacement Root Mount module;
2. leave the known-working SimpleHook rule enabled and launch LINE once;
3. verify the mounted build launches and chat works without crashing;
4. disable only the SimpleHook `q28.n.b(Context) -> true` rule;
5. force-stop LINE;
6. relaunch LINE and evaluate native theme behavior.

Observed result with the SimpleHook dark-mode rule **disabled**:

```text
LINE launch          OK
Chat                 OK
Native dark mode     OK
Floating window      OK
Split screen         OK
VOOM tab             removed
Crash                none observed
```

This is the key runtime result: the native dark theme survives after the original runtime hook is disabled, so the selective static patch is sufficient on the tested Y700 / LINE 26.11.0 environment.

No advertisements were seen during the same pass. This is recorded only as an observation, **not as causal evidence for this build**, because the Y700 had already shown an effectively ad-free state before the selective-dark-mode test.

## Cold-boot validation — 2026-08-19

The Y700 was then fully rebooted with the SimpleHook dark-mode rule still disabled. After boot, LINE again launched normally and the tested behavior remained unchanged:

```text
Native dark mode     OK
Floating window      OK
Split screen         OK
VOOM tab             removed
Chat / launch        OK
Crash                none observed
```

This excludes a hot-mount-only or stale-process explanation for the successful first pass. The selective Root Mount implementation is therefore promoted to the **stable baseline for the tested Y700 / LINE 26.11.0 configuration**.

Broader image/file/call use remains useful regression coverage, but it is no longer treated as a blocker for the core dark-mode conclusion because the selective edit touches only the verified Secondary reject edge and has survived both process restart and full device reboot.

## Andrew / Knot comparison

### Andrew's Patches

Andrew's `Hide VOOM tab` is the main engineering reference. It fingerprints a semantic main-tab builder and removes only the instruction pair that adds `TIMELINE`, leaving unrelated feature-flag logic alone. The same minimal-edit philosophy is used here: one verified reject edge is neutralized instead of forcing the entire dark-mode predicate.

### Knot

Knot's theme support samples LINE's active theme semantics for Knot-added UI. This reinforces the preference to let LINE own the palette and rendering. It is not an implementation of this Secondary eligibility bypass.

## Publication wording

Do not claim a worldwide first. The defensible statement is:

> Independently discovered and verified on LINE Android 26.11.0; no equivalent public Secondary-native-dark-mode patch was found during the 2026-08 research pass.

Private/unindexed implementations may exist.

## Regression checklist

1. LINE launches normally as a Secondary device. — ✅ verified before and after reboot
2. Dark mode remains active without the SimpleHook return-true rule. — ✅ verified
3. Fullscreen UI is correct. — ✅ verified
4. Split-screen UI is correct. — ✅ verified before and after reboot
5. Floating-window UI is correct. — ✅ verified before and after reboot
6. Chat list and chat room are correct. — ✅ verified
7. Images/files/calls continue to work. — ◻ ordinary follow-up regression coverage
8. Device remains registered/treated as Secondary. — ✅ no role break observed through tested reboot cycle
9. VOOM patch does not affect the dark-mode result. — ✅ both work together
10. SimpleHook `q28.n.b -> true` can be disabled without losing dark mode. — ✅ verified
11. Cold reboot preserves the same result. — ✅ verified
