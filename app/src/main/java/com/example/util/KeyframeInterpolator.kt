package com.example.util

import com.example.model.Clip
import com.example.model.CurvePreset
import com.example.model.Keyframe
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

data class InterpolatedTransform(
    val scale: Float,
    val rotation: Float,
    val positionX: Float,
    val positionY: Float,
    val opacity: Float
)

object KeyframeInterpolator {

    fun interpolate(clip: Clip, currentTimeMs: Long): InterpolatedTransform {
        val keyframes = clip.keyframes.sortedBy { it.timeMs }
        val baseScale = clip.transform.scale
        val baseRotation = clip.transform.rotation
        val baseX = clip.transform.positionX
        val baseY = clip.transform.positionY
        val baseOpacity = clip.adjust.opacity / 100f

        if (keyframes.isEmpty()) {
            return InterpolatedTransform(
                scale = baseScale,
                rotation = baseRotation,
                positionX = baseX,
                positionY = baseY,
                opacity = baseOpacity
            )
        }

        if (currentTimeMs <= keyframes.first().timeMs) {
            val k = keyframes.first()
            return InterpolatedTransform(k.scale, k.rotation, k.positionX, k.positionY, k.opacity)
        }

        if (currentTimeMs >= keyframes.last().timeMs) {
            val k = keyframes.last()
            return InterpolatedTransform(k.scale, k.rotation, k.positionX, k.positionY, k.opacity)
        }

        // Find bounding keyframes
        var k1 = keyframes.first()
        var k2 = keyframes.last()
        for (i in 0 until keyframes.size - 1) {
            if (currentTimeMs >= keyframes[i].timeMs && currentTimeMs <= keyframes[i + 1].timeMs) {
                k1 = keyframes[i]
                k2 = keyframes[i + 1]
                break
            }
        }

        val duration = (k2.timeMs - k1.timeMs).toFloat().coerceAtLeast(1f)
        val rawT = ((currentTimeMs - k1.timeMs) / duration).coerceIn(0f, 1f)

        // Evaluate curve easing
        val easedT = evaluateCurve(rawT, k1.curvePreset, k1, k2)

        return InterpolatedTransform(
            scale = lerp(k1.scale, k2.scale, easedT),
            rotation = lerp(k1.rotation, k2.rotation, easedT),
            positionX = lerp(k1.positionX, k2.positionX, easedT),
            positionY = lerp(k1.positionY, k2.positionY, easedT),
            opacity = lerp(k1.opacity, k2.opacity, easedT)
        )
    }

    fun evaluateCurve(t: Float, preset: CurvePreset, k1: Keyframe, k2: Keyframe): Float {
        return when (preset) {
            CurvePreset.EASE -> 3 * t * t - 2 * t * t * t
            CurvePreset.EASE_IN -> t * t
            CurvePreset.CUBIC -> t * t * t
            CurvePreset.CUBIC_IN -> t * t * t
            CurvePreset.CUBIC_OUT -> 1f - (1f - t) * (1f - t) * (1f - t)
            CurvePreset.CIRC_OUT -> sqrt(max(0f, 1f - (t - 1f) * (t - 1f)))
            CurvePreset.CUSTOM_BEZIER -> evaluateCubicBezier(t, k1, k2)
        }
    }

    /**
     * Solves cubic Bezier with handles:
     * P0 = (0, 0)
     * P1 = (0 + rightHandleX, 0 + rightHandleY)
     * P2 = (1 + leftHandleX, 1 + leftHandleY)
     * P3 = (1, 1)
     */
    fun evaluateCubicBezier(t: Float, k1: Keyframe, k2: Keyframe): Float {
        val p1x = (k1.rightHandleX).coerceIn(0f, 1f)
        val p1y = (k1.rightHandleY + 0.3f).coerceIn(-0.5f, 1.5f)
        val p2x = (1f + k2.leftHandleX).coerceIn(0f, 1f)
        val p2y = (1f + k2.leftHandleY - 0.3f).coerceIn(-0.5f, 1.5f)

        // Binary search or Newton-Raphson approximation for parametric X(u) = t
        var uLow = 0f
        var uHigh = 1f
        var u = t
        for (iter in 0 until 8) {
            val xu = bezierCoordinate(u, 0f, p1x, p2x, 1f)
            if (kotlin.math.abs(xu - t) < 0.001f) break
            if (xu < t) {
                uLow = u
            } else {
                uHigh = u
            }
            u = (uLow + uHigh) * 0.5f
        }
        val yu = bezierCoordinate(u, 0f, p1y, p2y, 1f)
        return yu.coerceIn(-0.5f, 1.5f)
    }

    private fun bezierCoordinate(u: Float, p0: Float, p1: Float, p2: Float, p3: Float): Float {
        val oneMinusU = 1f - u
        return oneMinusU * oneMinusU * oneMinusU * p0 +
                3f * oneMinusU * oneMinusU * u * p1 +
                3f * oneMinusU * u * u * p2 +
                u * u * u * p3
    }

    private fun lerp(start: Float, stop: Float, fraction: Float): Float {
        return start + (stop - start) * fraction
    }
}
