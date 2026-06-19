package com.ugurbuga.learningmath.model

import com.ugurbuga.learningmath.shared.generated.resources.*
import org.jetbrains.compose.resources.StringResource

enum class InputMode(val titleRes: StringResource) {
    DIRECT(Res.string.direct_mode),
    STEP_BY_STEP(Res.string.step_by_step_mode)
}
