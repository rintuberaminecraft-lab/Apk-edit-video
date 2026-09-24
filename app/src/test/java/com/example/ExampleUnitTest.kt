package com.example

import com.example.model.*
import com.example.util.KeyframeInterpolator
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun keyframeInterpolator_handlesEndpointsCorrectly() {
    val kf1 = Keyframe(timeMs = 0L, scale = 1.0f, rotation = 0f)
    val kf2 = Keyframe(timeMs = 1000L, scale = 2.0f, rotation = 90f)
    val clip = Clip(
      name = "Test Clip",
      keyframes = listOf(kf1, kf2)
    )

    val atStart = KeyframeInterpolator.interpolate(clip, 0L)
    assertEquals(1.0f, atStart.scale, 0.01f)
    assertEquals(0f, atStart.rotation, 0.01f)

    val atEnd = KeyframeInterpolator.interpolate(clip, 1000L)
    assertEquals(2.0f, atEnd.scale, 0.01f)
    assertEquals(90f, atEnd.rotation, 0.01f)

    val atMid = KeyframeInterpolator.interpolate(clip, 500L)
    assertTrue("Scale should interpolate smoothly between 1.0 and 2.0", atMid.scale in 1.0f..2.0f)
    assertTrue("Rotation should interpolate smoothly between 0 and 90", atMid.rotation in 0f..90f)
  }

  @Test
  fun exportBitrate_matchesYouTubeSpecifications() {
    assertEquals(7.5f, ExportResolution.RES_720P.defaultBitrateMbps, 0.01f)
    assertEquals(12.0f, ExportResolution.RES_1080P.defaultBitrateMbps, 0.01f)
  }
}

