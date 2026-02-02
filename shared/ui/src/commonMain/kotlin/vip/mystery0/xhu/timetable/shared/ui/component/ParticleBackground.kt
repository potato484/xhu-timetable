package vip.mystery0.xhu.timetable.shared.ui.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import kotlin.random.Random

data class Particle(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    var radius: Float,
    var alpha: Float
)

@Composable
fun ParticleBackground(
    modifier: Modifier = Modifier,
    particleCount: Int = 80,
    particleColor: Color = Color.White.copy(alpha = 0.5f) // Default subtle white
) {
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val density = LocalDensity.current
        val widthPx = with(density) { maxWidth.toPx() }
        val heightPx = with(density) { maxHeight.toPx() }

        // Only initialize particles once we have valid dimensions
        if (widthPx > 0 && heightPx > 0) {
            var particles by remember {
                mutableStateOf(
                    List(particleCount) {
                        Particle(
                            x = Random.nextFloat() * widthPx,
                            y = Random.nextFloat() * heightPx,
                            vx = (Random.nextFloat() - 0.5f) * 1.5f,
                            vy = (Random.nextFloat() - 0.5f) * 1.5f,
                            radius = Random.nextFloat() * 5f + 2f,
                            alpha = Random.nextFloat() * 0.4f + 0.1f
                        )
                    }
                )
            }

            var lastFrameTime by remember { mutableStateOf(0L) }

            LaunchedEffect(Unit) {
                while (true) {
                    withFrameNanos { frameTime ->
                        if (lastFrameTime != 0L) {
                            // Update particles
                            val newParticles = particles.map { p ->
                                var newX = p.x + p.vx
                                var newY = p.y + p.vy

                                // Wrap around
                                if (newX < 0) newX = widthPx
                                else if (newX > widthPx) newX = 0f

                                if (newY < 0) newY = heightPx
                                else if (newY > heightPx) newY = 0f

                                p.copy(x = newX, y = newY)
                            }
                            particles = newParticles
                        }
                        lastFrameTime = frameTime
                    }
                }
            }

            Canvas(modifier = Modifier.fillMaxSize()) {
                particles.forEach { particle ->
                    drawCircle(
                        color = particleColor.copy(alpha = particle.alpha),
                        radius = particle.radius,
                        center = Offset(particle.x, particle.y)
                    )
                }
            }
        }
    }
}
