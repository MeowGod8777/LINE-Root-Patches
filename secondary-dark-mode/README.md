# LINE Secondary Native Dark Mode

> Status: **verified PoC / workaround; selective bytecode patch in progress**
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

## Why this is only the PoC

For LINE 26.11.0, `q28.n.b(Context)` is not only the Secondary check. The method also participates in other dark-mode eligibility checks. Forcing the whole method to `true` therefore proves the blocker, but bypasses more logic than necessary.

The intended final patch follows the same engineering principle used by Andrew's Morphe patches:

1. locate the eligibility method with a strict fingerprint;
2. identify the **Secondary -> reject** control-flow edge;
3. neutralize only that edge;
4. preserve registration/init, preference and theme-asset checks;
5. fail closed if the fingerprint/control-flow shape is no longer unique.

The result should behave as if LINE simply stopped treating `SECONDARY` as an automatic reason to reject native dark mode.

## Relation to other projects

- **Andrew's Patches**: used as the model for semantic fingerprints, minimal bytecode edits and fail-closed version handling. Andrew currently has no equivalent Secondary-native-dark-mode patch in the published LINE patch set.
- **Knot**: useful as a reference for respecting LINE's own runtime theme semantics. Knot's `LineTheme` adapts Knot-injected UI to LINE's active theme; it does not remove LINE's Secondary dark-mode eligibility veto.

This project does **not** recreate a dark palette, force Android WebView darkening, or globally spoof the device as Primary. LINE's own theme engine remains responsible for colors, icons and layout.

## Safety constraints

- Do **not** globally spoof `isSecondary()` / device type. Secondary state is used by unrelated LINE features.
- Do not force arbitrary theme files or colors.
- Patch only the dark-mode eligibility path.
- The current research is pinned to LINE 26.11.0 until another version is statically and functionally verified.
- Prefer Root Mount for the final Y700 build so the original LINE signing identity is retained.

## Current stages

| Stage | Method | Status |
|---|---|---|
| PoC-1 | SimpleHook `q28.n.b(Context) -> true` | ✅ verified daily workaround |
| PoC-2 | static Morphe equivalent of PoC-1 | 🧪 build/test stage |
| Final | selective Secondary-veto bypass | 🚧 in progress |
| VOOM | Andrew `Hide VOOM tab` via Root Mount | planned for the Y700 bundle |

See [`research.md`](research.md) for the evidence and patch-design notes.
