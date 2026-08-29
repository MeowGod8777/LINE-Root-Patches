# 2026-08-29 — intermittent LINE mount loss / swipe-restart recovery

## Observed regression

After a period of normal daily use with v4, the patched UI regressed again:

- VOOM returned.
- Community ad returned.
- Community notebook ad returned.

This matches the previously observed failure state where LINE sees the stock base.apk and has no RVHC bind mount.

## New recovery observation

Without rebooting, reinstalling any module, changing Zygisk Next, or touching Andrew payload:

> Swiping LINE away from Recents and reopening it is sufficient to restore the patched UI.

This is materially different from a persistent payload/config failure. The recovery mechanism is process/task recreation only.

## Frozen facts

- Andrew patched payload remains known-good (`683853ce...`).
- Stock LINE base hash remains `fbc229f8...`.
- v4 can successfully create `RVHC -> LINE base.apk` mounts on some LINE process creations.
- v4 still fails intermittently over time; the prior UAF-only fix is therefore insufficient to explain/fix the entire problem.
- A simple process/task recreation can immediately produce a good patched instance again.

## Revised hypothesis

High-confidence hypothesis, not yet final proof:

The remaining defect is a process-specialization timing race between rvmm's asynchronous companion remount and Zygisk Next's `WL + UM` unmount stage.

Upstream rvmm v10 sends process info from `preAppSpecialize()`. The root companion then forks and performs `setns()` + bind mount asynchronously. Zygisk API documentation states denylist/unmount work occurs during process specialization. On this device, Zygisk Next is running `WL + UM`, and non-root LINE is subject to the unmount path.

Therefore two orderings are possible:

```text
success:
Zygisk Next UM completes
-> rvmm companion bind-mounts patched base
-> mount survives

failure:
rvmm companion bind-mounts patched base
-> Zygisk Next UM runs afterwards
-> mount is stripped
-> LINE sees stock base
```

This model explains:

- same payload / same path / same module can succeed or fail across process recreations;
- v3 and v4 both work immediately sometimes but regress later;
- swipe-away + reopen can recover without reinstall/reboot;
- v4 UAF fix improves pointer lifetime but does not solve timing ordering.

## Upstream compatibility note

Upstream rvmm issue discussion explicitly advises disabling target-app "unmount modules" behavior (KernelSU case) or keeping the target app out of denylist-style unmount behavior (Magisk case). That is directly relevant because this setup intentionally keeps Zygisk Next `WL + UM` enabled globally.

## Next implementation candidate

Do not add another shell watcher or rebuild Andrew.

Preferred v5 design: make the remount request happen after app specialization/unmount, while preserving root companion access safely.

Because `connectCompanion()` is only available in pre-specialize, v5 should:

1. `preAppSpecialize()`:
   - identify target LINE process;
   - connect to companion;
   - exempt/preserve the companion fd across specialization;
   - retain the process identity in child-local state;
2. `postAppSpecialize()`:
   - send the mount request only after specialization is complete;
   - close the preserved fd;
3. companion:
   - perform `setns()` + bind mount with correct owned `src/dst` lifetime;
   - emit explicit matched / setns / mount success-failure breadcrumbs.

This is one architectural timing variable, directly falsifiable, and does not require changing:

- Andrew payload,
- LINE data,
- DenyList,
- global Zygisk Next WL/UM,
- PIF / Tricky Store.

## Do not conclude yet

- Do not mark v4 as persistence-fixed.
- Do not treat UAF as the sole root cause.
- Do not attribute the UI regression to remote config when LINE process hash/mount proves stock view.
