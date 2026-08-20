package dev.meowgod.line.secondarydark

import dev.meowgod.line.shared.Constants.COMPATIBILITY_LINE_26110
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch

/**
 * Static equivalent of the verified SimpleHook PoC:
 *
 *     q28.n.b(android.content.Context) -> true
 *
 * This removes the runtime-hook dependency, but deliberately preserves the PoC's known
 * over-breadth. It is kept as a reproducible baseline while the selective Secondary-veto-only
 * patch is developed.
 */
@Suppress("unused")
val secondaryNativeDarkModePocPatch = bytecodePatch(
    name = "Unlock Secondary native dark mode (PoC)",
    description = "Enables LINE's native dark theme on Secondary devices using the verified " +
        "26.11.0 eligibility-method PoC. Experimental: bypasses the whole predicate.",
    default = false,
) {
    compatibleWith(COMPATIBILITY_LINE_26110)

    execute {
        SecondaryDarkModeEligibility26110Fingerprint.method.addInstructions(
            0,
            """
                const/4 v0, 0x1
                return v0
            """.trimIndent(),
        )
    }
}
