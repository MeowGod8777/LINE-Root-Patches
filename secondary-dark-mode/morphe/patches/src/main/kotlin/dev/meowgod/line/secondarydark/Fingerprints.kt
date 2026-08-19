package dev.meowgod.line.secondarydark

import app.morphe.patcher.Fingerprint

/**
 * Verified LINE 26.11.0 symbol for the native-dark-mode eligibility predicate used by the
 * original SimpleHook proof-of-concept.
 *
 * This intentionally starts strict and version-specific. The selective final patch will move
 * toward semantic anchors after the 26.11.0 instruction shape is captured and verified.
 */
internal object SecondaryDarkModeEligibility26110Fingerprint : Fingerprint(
    definingClass = "Lq28/n;",
    name = "b",
    returnType = "Z",
    parameters = listOf("Landroid/content/Context;"),
)
