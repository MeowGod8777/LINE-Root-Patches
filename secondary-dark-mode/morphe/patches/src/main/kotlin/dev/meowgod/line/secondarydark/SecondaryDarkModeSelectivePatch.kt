package dev.meowgod.line.secondarydark

import dev.meowgod.line.shared.Constants.COMPATIBILITY_LINE_26110
import app.morphe.patcher.extensions.InstructionExtensions.getInstruction
import app.morphe.patcher.extensions.InstructionExtensions.replaceInstruction
import app.morphe.patcher.patch.bytecodePatch
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.Instruction

/**
 * Selective LINE 26.11.0 Secondary native-dark-mode unlock.
 *
 * Verified Y700 26.11.0 control flow around k40.d0.f():
 *
 *     invoke-interface {v0}, Lk40/d0;->f()Z
 *     move-result v0
 *     if-eqz v0, :continue_normal_dark_checks
 *     goto :reject_false
 *
 * Only the final reject edge is neutralized. Both true and false results therefore continue
 * into LINE's original THEME_AUTO_DARK_MODE / theme-readiness logic. Registration/init and all
 * later checks remain untouched.
 */
@Suppress("unused")
val secondaryNativeDarkModePatch = bytecodePatch(
    name = "Unlock Secondary native dark mode",
    description = "Allows Android Secondary devices to continue through LINE's normal native " +
        "dark-theme eligibility checks instead of being rejected solely for Secondary status.",
    default = true,
) {
    compatibleWith(COMPATIBILITY_LINE_26110)

    execute {
        val match = SecondaryDarkModeEligibility26110Fingerprint.instructionMatches.single()
        val callIndex = match.index
        val method = SecondaryDarkModeEligibility26110Fingerprint.method

        // Fail closed if the exact verified 26.11.0 control-flow shape is not present.
        check(method.getInstruction<Instruction>(callIndex).opcode == Opcode.INVOKE_INTERFACE) {
            "Secondary role anchor is no longer invoke-interface"
        }
        check(method.getInstruction<Instruction>(callIndex + 1).opcode == Opcode.MOVE_RESULT) {
            "Unexpected instruction after Secondary role predicate"
        }
        check(method.getInstruction<Instruction>(callIndex + 2).opcode == Opcode.IF_EQZ) {
            "Secondary role branch no longer has the verified if-eqz shape"
        }
        check(method.getInstruction<Instruction>(callIndex + 3).opcode == Opcode.GOTO) {
            "Secondary reject edge no longer has the verified goto shape"
        }

        // One-instruction edit: keep the role query and its false-path branch, but make the
        // true-path reject edge fall through to the same normal dark-mode checks.
        method.replaceInstruction(callIndex + 3, "nop")
    }
}
