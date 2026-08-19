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

## Runtime proof

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

A standalone Morphe patch bundle now contains a static equivalent of the verified SimpleHook PoC. It is pinned to LINE 26.11.0 and uses the exact known symbol fingerprint (`Lq28/n;->b(Landroid/content/Context;)Z`).

CI initially exposed two build-environment omissions and was fixed rather than worked around:

1. missing Gradle version catalog required by the Morphe Gradle plugin;
2. missing Kotlin `-Xcontext-parameters` compiler flag required by the current Morphe API.

After those corrections, GitHub Actions run `32239248900` compiled successfully and produced:

```text
patches-0.1.0-dev.mpp
size: 7615 bytes
SHA-256: a63de30502ede5e50934eab3d8eb49f7a683514e8f545c6072b38c4226560e12
```

This checkpoint proves the custom patch project itself builds. It does **not** yet promote the PoC to the final selective implementation and does not count as Y700 runtime verification until the generated patch is applied and tested with SimpleHook disabled.

## Final patch design

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

### Fingerprint strategy

Development starts with the exact 26.11.0 symbol fingerprint to reproduce the known PoC safely. The final fingerprint should move toward semantic anchors, inspired by Andrew's Morphe patches, rather than relying on `q28.n.b` forever.

Candidate anchors, subject to build-time inspection of 26.11.0 DEX:

- boolean return type;
- one `android.content.Context` parameter;
- dark-theme preference/config access;
- access/call involving the Secondary device-type path;
- a unique control-flow edge that returns false for Secondary.

The patch must fail if the expected target/control-flow shape is absent or ambiguous.

## Andrew / Knot comparison

### Andrew's Patches

Andrew's `Hide VOOM tab` is the main engineering reference. It fingerprints a semantic main-tab builder and removes only the instruction pair that adds `TIMELINE`, leaving unrelated feature-flag logic alone. The same minimal-edit philosophy is appropriate here.

### Knot

Knot's theme support samples LINE's active theme semantics for Knot-added UI. This reinforces the preference to let LINE own the palette and rendering. It is not an implementation of this Secondary eligibility bypass.

## Publication wording

Do not claim a worldwide first. The defensible statement is:

> Independently discovered and verified on LINE Android 26.11.0; no equivalent public Secondary-native-dark-mode patch was found during the 2026-08 research pass.

Private/unindexed implementations may exist.

## Regression checklist

Before promoting a selective patch from experimental to tested:

1. LINE launches normally as a Secondary device.
2. Dark mode follows the intended system/LINE preference behavior.
3. Fullscreen UI is correct.
4. Split-screen UI is correct.
5. Floating-window UI is correct.
6. Chat list and chat room are correct.
7. Images/files/calls continue to work.
8. Device remains registered/treated as Secondary.
9. VOOM patch, if bundled separately, does not affect the dark-mode result.
10. SimpleHook `q28.n.b -> true` can be disabled without losing dark mode on the statically patched build.
