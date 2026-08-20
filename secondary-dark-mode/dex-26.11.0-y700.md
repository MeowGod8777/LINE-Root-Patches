# LINE 26.11.0 Y700 DEX capture — Secondary native dark mode

Captured from the actual `base.apk` exported from the Lenovo Y700 4th Gen Secondary-device installation on 2026-08-19.

## Input

```text
file: LINE_26.11.0_Y700_base.apk
size: ~160 MiB
SHA-256: 762025b5785bd7cab26a01b0e9644fd972549200e665f607d2593ba34ba261ee
DEX containing q28.n definition: classes.dex
```

The APK contains 13 DEX files (`classes.dex` through `classes13.dex`). The class definition for `Lq28/n;` is in `classes.dex`.

## Target method

```text
Lq28/n;->b(Landroid/content/Context;)Z
registers: 5
ins: 2
outs: 4
code units: 77
```

Relevant resolved control flow:

```text
00  invoke-static {}, Lo28/a;->e()Z
03  move-result v0
04  if-nez v0, +3                 # continue if prerequisite true
06  goto +69                      # -> return false

07  sget-object v0, Lk40/d0;->c5:Lk40/d0$b;
09  invoke-static {v4, v0}, Ly60/g;->a(Landroid/content/Context;Ly60/a;)Ljava/lang/Object;
12  move-result-object v0
13  check-cast v0, Lk40/d0;
15  invoke-interface {v0}, Lk40/d0;->f()Z
18  move-result v0
19  if-eqz v0, +3                 # -> pc22, continue normal dark checks
21  goto +54                      # -> pc75, reject false

22  invoke-static {}, Lx58/g;->a()Lx58/g;
25  move-result-object v0
26  sget-object v1, Lc78/n;->THEME_AUTO_DARK_MODE:Lc78/n;
...
57  if-eqz v3, +18                # -> reject false
...
71  if-eqz v3, +4                 # -> reject false
73  const/4 v3, 0x1
74  return v3
75  const/4 v3, 0x0
76  return v3
```

## Confirmed veto

The device-role predicate is therefore structurally isolated:

```text
invoke-interface {v0}, Lk40/d0;->f()Z
move-result v0
if-eqz v0, :continue_normal_dark_checks
goto :reject_false
```

This matches the earlier static finding that the Secondary role is rejected before `THEME_AUTO_DARK_MODE` and subsequent theme-readiness checks.

## Selective patch

The minimal edit is **not** to force the whole method true and **not** to spoof `k40.d0.f()` globally.

Only replace the one-code-unit reject `goto` immediately after the role predicate with `nop`:

```diff
 invoke-interface {v0}, Lk40/d0;->f()Z
 move-result v0
 if-eqz v0, :continue_normal_dark_checks
-goto :reject_false
+nop
```

Result:

- role predicate false -> existing `if-eqz` goes to normal dark checks;
- role predicate true -> falls through the `nop` into the same normal dark checks;
- registration/init prerequisite remains intact;
- `THEME_AUTO_DARK_MODE` handling remains intact;
- later theme/readiness checks remain intact;
- device identity remains Secondary everywhere else.

The Morphe patch validates `INVOKE_INTERFACE -> MOVE_RESULT -> IF_EQZ -> GOTO` around the matched `Lk40/d0;->f()Z` call and fails closed if that verified 26.11.0 shape changes.

## PoC comparison

Previous SimpleHook/static PoC:

```text
q28.n.b(Context) -> true
```

Selective patch:

```text
neutralize only the Secondary -> reject edge
```

The PoC remains useful as discovery evidence. The selective patch is the candidate for actual Y700 daily-use testing.
