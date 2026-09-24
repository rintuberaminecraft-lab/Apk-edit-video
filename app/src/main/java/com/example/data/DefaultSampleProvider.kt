package com.example.data

import com.example.model.*
import java.util.UUID

object DefaultSampleProvider {

    fun getSampleAudioTracks(): List<AudioTrack> {
        val now = System.currentTimeMillis()
        return listOf(
            AudioTrack(
                id = "audio_1",
                title = "Neon Drift (YouTube Safe)",
                artist = "TubeCut Studio Beats",
                addedTimestamp = now - 60_000,
                durationMs = 35_000,
                volume = 120f,
                equalizerPreset = EqualizerPreset.POP,
                eqBands = listOf(1.5f, 2.0f, 1.0f, 0.5f, 0f, 1.0f, 2.5f, 3.0f, 2.0f, 1.0f)
            ),
            AudioTrack(
                id = "audio_2",
                title = "Lofi Study Session",
                artist = "ChillHop Creators",
                addedTimestamp = now - 180_000,
                durationMs = 28_000,
                volume = 100f,
                equalizerPreset = EqualizerPreset.BASS_BOOST,
                eqBands = listOf(4.0f, 3.5f, 2.0f, 1.0f, 0f, -0.5f, 0f, 1.0f, 1.5f, 2.0f)
            ),
            AudioTrack(
                id = "audio_3",
                title = "Cyber Pulse 140",
                artist = "Electro Vlog Audio",
                addedTimestamp = now - 360_000,
                durationMs = 45_000,
                volume = 150f,
                equalizerPreset = EqualizerPreset.ROCK,
                eqBands = listOf(3.0f, 2.0f, -1.0f, -2.0f, 1.0f, 2.5f, 3.0f, 4.0f, 3.5f, 2.0f)
            ),
            AudioTrack(
                id = "audio_4",
                title = "Acoustic Morning Sunlight",
                artist = "Folk Beats",
                addedTimestamp = now - 720_000,
                durationMs = 30_000,
                volume = 110f,
                equalizerPreset = EqualizerPreset.VOCAL,
                eqBands = listOf(-1.0f, 0f, 1.0f, 2.0f, 3.5f, 4.0f, 3.0f, 1.5f, 0.5f, 0f)
            ),
            AudioTrack(
                id = "audio_5",
                title = "Cinematic YouTube Intro",
                artist = "Hollywood Orchestra",
                addedTimestamp = now - 1_440_000,
                durationMs = 22_000,
                volume = 100f,
                equalizerPreset = EqualizerPreset.FLAT,
                eqBands = listOf(0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f)
            )
        )
    }

    fun createInitialSampleProject(ratio: AspectRatio = AspectRatio.RATIO_9_16): EditorProject {
        val projectId = UUID.randomUUID().toString()
        val clipId1 = UUID.randomUUID().toString()
        val clipId2 = UUID.randomUUID().toString()
        val pipId1 = UUID.randomUUID().toString()
        val pipId2 = UUID.randomUUID().toString()

        val mainClips = listOf(
            Clip(
                id = clipId1,
                name = "Main Clip 1 - Studio Intro",
                layerType = LayerType.MAIN_TRACK,
                mediaType = MediaType.VIDEO,
                previewColor = 0xFF1B5E20, // Rich forest green
                startMs = 0L,
                durationMs = 6000L,
                transform = TransformConfig(scale = 1.0f),
                adjust = AdjustConfig(saturation = 15f, contrast = 10f),
                keyframes = listOf(
                    Keyframe(
                        timeMs = 0L,
                        scale = 1.0f,
                        rotation = 0f,
                        curvePreset = CurvePreset.CUSTOM_BEZIER,
                        rightHandleX = 0.4f,
                        rightHandleY = 0.2f
                    ),
                    Keyframe(
                        timeMs = 3000L,
                        scale = 1.25f,
                        rotation = 3f,
                        curvePreset = CurvePreset.CUSTOM_BEZIER,
                        leftHandleX = -0.4f,
                        leftHandleY = -0.2f,
                        rightHandleX = 0.3f,
                        rightHandleY = 0.1f
                    ),
                    Keyframe(
                        timeMs = 6000L,
                        scale = 1.05f,
                        rotation = 0f,
                        curvePreset = CurvePreset.EASE,
                        leftHandleX = -0.3f,
                        leftHandleY = 0.0f
                    )
                )
            ),
            Clip(
                id = clipId2,
                name = "Main Clip 2 - Motion Action",
                layerType = LayerType.MAIN_TRACK,
                mediaType = MediaType.VIDEO,
                previewColor = 0xFF0D47A1, // Rich oceanic blue
                startMs = 6000L,
                durationMs = 8000L,
                transform = TransformConfig(scale = 1.0f),
                transition = TransitionConfig(type = TransitionType.CROSS_DISSOLVE, durationMs = 800L)
            )
        )

        val pipLayers = listOf(
            Clip(
                id = pipId1,
                name = "PIP Layer 1 - Chroma Overlay",
                layerType = LayerType.PIP_LAYER,
                mediaType = MediaType.COLOR,
                previewColor = 0xFF00E676,
                startMs = 1500L,
                durationMs = 5000L,
                transform = TransformConfig(
                    scale = 0.65f,
                    positionX = 0.2f,
                    positionY = -0.25f,
                    positionZ = 1
                ),
                chromaKey = ChromaKeyConfig(isEnabled = true, strength = 50f, shadow = 30f),
                mask = MaskConfig(type = MaskType.ROUNDED_RECTANGLE, cornerRadius = 32f, feather = 10f)
            ),
            Clip(
                id = pipId2,
                name = "PIP Layer 2 - YouTube Title Text",
                layerType = LayerType.PIP_LAYER,
                mediaType = MediaType.TEXT,
                previewColor = 0xFFFF1744,
                startMs = 500L,
                durationMs = 6000L,
                text = TextConfig(
                    text = if (ratio.isShorts) "#SHORTS VIRAL EDIT" else "YOUTUBE MASTERCLASS",
                    fontFamilyName = "Bebas Neue",
                    fontSizeSp = 30f,
                    textColor = 0xFFFFFFFF,
                    backgroundColor = 0xAA000000,
                    strokeColor = 0xFFFF0033,
                    strokeWidth = 3f,
                    hasShadow = true
                ),
                transform = TransformConfig(
                    scale = 0.95f,
                    positionX = 0f,
                    positionY = 0.6f,
                    positionZ = 2
                ),
                transition = TransitionConfig(type = TransitionType.ENTRY_UP, durationMs = 500L)
            )
        )

        return EditorProject(
            id = projectId,
            name = if (ratio.isShorts) "Trending YouTube Shorts #1" else "YouTube 4K Epic Edit",
            aspectRatio = ratio,
            durationMs = 14000L,
            mainClips = mainClips,
            pipLayers = pipLayers,
            audioTracks = listOf(getSampleAudioTracks().first()),
            background = BackgroundConfig(type = BgType.BLUR, blurIntensity = 50f)
        )
    }
}
