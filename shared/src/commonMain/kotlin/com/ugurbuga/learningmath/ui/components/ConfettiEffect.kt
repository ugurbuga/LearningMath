package com.ugurbuga.learningmath.ui.components

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.vinceglb.confettikit.core.Party
import io.github.vinceglb.confettikit.core.Position
import io.github.vinceglb.confettikit.core.emitter.Emitter
import io.github.vinceglb.confettikit.compose.ConfettiKit
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun ConfettiEffect(onAnimationFinished: () -> Unit = {}) {
    val party = Party(
        speed = 0f,
        maxSpeed = 30f,
        damping = 0.9f,
        spread = 360,
        colors = listOf(0xFFFFB74D.toInt(), 0xFF81C784.toInt(), 0xFF64B5F6.toInt(), 0xFFBA68C8.toInt()),
        position = Position.Relative(0.5, 0.3),
        emitter = Emitter(duration = 100.milliseconds).max(100)
    )

    ConfettiKit(
        modifier = Modifier.fillMaxSize(),
        parties = listOf(party),
        onParticleSystemEnded = { _, _ ->
            onAnimationFinished()
        }
    )
}
