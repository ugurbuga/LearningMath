package com.ugurbuga.learningmath.ui.components

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.vinceglb.confettikit.core.Party
import io.github.vinceglb.confettikit.core.Position
import io.github.vinceglb.confettikit.core.emitter.Emitter
import io.github.vinceglb.confettikit.compose.ConfettiKit
import androidx.compose.ui.tooling.preview.Preview
import com.ugurbuga.learningmath.ui.theme.LearningMathTheme
import kotlin.time.Duration.Companion.milliseconds

enum class ConfettiType {
    SINGLE_EXPLOSION,
    FIREWORKS,
    FESTIVAL;

    companion object {
        fun random() = entries.random()
    }
}

@Composable
fun ConfettiEffect(
    type: ConfettiType = ConfettiType.FESTIVAL,
    onAnimationFinished: () -> Unit = {}
) {
    val parties = when (type) {
        ConfettiType.SINGLE_EXPLOSION -> listOf(
            Party(
                speed = 0f,
                maxSpeed = 30f,
                damping = 0.9f,
                spread = 360,
                colors = listOf(0xFFFFB74D.toInt(), 0xFF81C784.toInt(), 0xFF64B5F6.toInt(), 0xFFBA68C8.toInt()),
                position = Position.Relative(0.5, 0.3),
                emitter = Emitter(duration = 100.milliseconds).max(100)
            )
        )

        ConfettiType.FIREWORKS -> listOf(
            Party(
                speed = 10f,
                maxSpeed = 30f,
                damping = 0.9f,
                spread = 360,
                colors = listOf(0xFFF44336.toInt(), 0xFFFFEB3B.toInt(), 0xFF2196F3.toInt()),
                position = Position.Relative(0.5, 0.2),
                emitter = Emitter(duration = 100.milliseconds).max(100)
            ),
            Party(
                speed = 10f,
                maxSpeed = 25f,
                damping = 0.9f,
                spread = 360,
                colors = listOf(0xFFE91E63.toInt(), 0xFF00BCD4.toInt(), 0xFF4CAF50.toInt()),
                position = Position.Relative(0.2, 0.4),
                emitter = Emitter(duration = 100.milliseconds).max(80)
            ),
            Party(
                speed = 10f,
                maxSpeed = 25f,
                damping = 0.9f,
                spread = 360,
                colors = listOf(0xFFFF9800.toInt(), 0xFF9C27B0.toInt(), 0xFFCDDC39.toInt()),
                position = Position.Relative(0.8, 0.3),
                emitter = Emitter(duration = 100.milliseconds).max(80)
            )
        )

        ConfettiType.FESTIVAL -> listOf(
            Party(
                speed = 30f,
                maxSpeed = 45f,
                damping = 0.9f,
                angle = 225,
                spread = 45,
                colors = listOf(0xFFF44336.toInt(), 0xFFFFEB3B.toInt(), 0xFF2196F3.toInt(), 0xFF4CAF50.toInt()),
                position = Position.Relative(1.0, 0.5),
                emitter = Emitter(duration = 100.milliseconds).max(100)
            ),
            Party(
                speed = 30f,
                maxSpeed = 45f,
                damping = 0.9f,
                angle = 315,
                spread = 45,
                colors = listOf(0xFFF44336.toInt(), 0xFFFFEB3B.toInt(), 0xFF2196F3.toInt(), 0xFF4CAF50.toInt()),
                position = Position.Relative(0.0, 0.5),
                emitter = Emitter(duration = 100.milliseconds).max(100)
            )
        )
    }

    ConfettiKit(
        modifier = Modifier.fillMaxSize(),
        parties = parties,
        onParticleSystemEnded = { _, _ ->
            onAnimationFinished()
        }
    )
}

@Preview
@Composable
fun ConfettiSinglePreview() {
    LearningMathTheme {
        ConfettiEffect(type = ConfettiType.SINGLE_EXPLOSION)
    }
}

@Preview
@Composable
fun ConfettiFireworksPreview() {
    LearningMathTheme {
        ConfettiEffect(type = ConfettiType.FIREWORKS)
    }
}

@Preview
@Composable
fun ConfettiFestivalPreview() {
    LearningMathTheme {
        ConfettiEffect(type = ConfettiType.FESTIVAL)
    }
}
