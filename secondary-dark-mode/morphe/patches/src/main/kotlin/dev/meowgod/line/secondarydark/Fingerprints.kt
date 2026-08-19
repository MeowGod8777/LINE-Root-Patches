package dev.meowgod.line.secondarydark

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.methodCall

/**
 * LINE 26.11.0 native-dark-mode eligibility predicate.
 *
 * The exact q28.n.b(Context) symbol is retained as a version-pinned guard, while the
 * instruction match is anchored on the device-role predicate call k40.d0.f(). The Y700
 * 26.11.0 DEX capture verified that this call is immediately followed by:
 *
 *   move-result v0
 *   if-eqz v0, :continue
 *   goto :reject
 *
 * The selective patch validates that shape before neutralizing only the reject goto.
 */
internal object SecondaryDarkModeEligibility26110Fingerprint : Fingerprint(
    definingClass = "Lq28/n;",
    name = "b",
    returnType = "Z",
    parameters = listOf("Landroid/content/Context;"),
    filters = listOf(
        methodCall(definingClass = "Lk40/d0;", name = "f"),
    ),
)
