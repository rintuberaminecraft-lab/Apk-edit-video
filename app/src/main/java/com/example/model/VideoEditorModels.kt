package com.example.model

import androidx.compose.ui.graphics.Color
import java.util.UUID

enum class AspectRatio(val displayName: String, val ratioStr: String, val ratio: Float, val isShorts: Boolean) {
    RATIO_9_16("YouTube Shorts", "9:16", 9f / 16f, true),
    RATIO_16_9("YouTube Video", "16:9", 16f / 9f, false),
    RATIO_1_1("Square", "1:1", 1f, false),
    RATIO_4_5("Portrait Feed", "4:5", 4f / 5f, false);

    val label: String get() = displayName
}

enum class MediaType {
    VIDEO,
    IMAGE,
    COLOR,
    DOODLE,
    TEXT
}

enum class LayerType {
    MAIN_TRACK,
    PIP_LAYER
}

enum class CurvePreset(val label: String) {
    EASE("Ease"),
    EASE_IN("Ease In"),
    CUBIC("Cubic"),
    CUBIC_IN("Cubic In"),
    CUBIC_OUT("Cubic Out"),
    CIRC_OUT("Circ Out"),
    CUSTOM_BEZIER("Custom Bezier")
}

data class Keyframe(
    val id: String = UUID.randomUUID().toString(),
    val timeMs: Long,
    val scale: Float = 1.0f,
    val rotation: Float = 0f,
    val positionX: Float = 0f,
    val positionY: Float = 0f,
    val opacity: Float = 1.0f,
    val curvePreset: CurvePreset = CurvePreset.EASE,
    // Editable Bezier tangent handles for custom graph:
    // Left handle (dx, dy) relative to keyframe point (-1.0 to 0.0 for x, -1.0 to 1.0 for y)
    val leftHandleX: Float = -0.3f,
    val leftHandleY: Float = 0.0f,
    // Right handle (dx, dy) relative to keyframe point (0.0 to 1.0 for x, -1.0 to 1.0 for y)
    val rightHandleX: Float = 0.3f,
    val rightHandleY: Float = 0.0f
)

enum class MaskType(val label: String) {
    NONE("None"),
    RECTANGLE("Rectangle"),
    SPLIT_RECTANGLE("Split Rect"),
    SQUARE("Square"),
    CIRCLE("Circle"),
    ROUNDED_RECTANGLE("Rounded Rect")
}

data class MaskConfig(
    val type: MaskType = MaskType.NONE,
    val feather: Float = 0.0f, // 0..100
    val inverted: Boolean = false,
    val widthPercent: Float = 80f, // % of layer
    val heightPercent: Float = 80f,
    val cornerRadius: Float = 24f,
    val splitGap: Float = 16f
)

enum class BlurType(val label: String) {
    NONE("None"),
    MOSAIC("Mosaic"),
    GAUSSIAN("Gaussian"),
    MAGNIFIER("Magnifier")
}

data class BlurConfig(
    val type: BlurType = BlurType.NONE,
    val strength: Float = 20f, // 1..100
    val magnification: Float = 1.8f // For magnifier 1.0..4.0
)

enum class DoodleAnimType(val label: String) {
    NONE("None"),
    DRAW_IN("Draw In"),
    FADE("Fade"),
    ZOOM_IN("Zoom In"),
    UP("Slide Up"),
    DOWN("Slide Down"),
    LEFT("Slide Left"),
    RIGHT("Slide Right")
}

data class OffsetPoint(val x: Float, val y: Float)

data class DoodlePath(
    val points: List<OffsetPoint>,
    val color: Long,
    val strokeWidth: Float
)

data class DrawDoodle(
    val paths: List<DoodlePath> = emptyList(),
    val animation: DoodleAnimType = DoodleAnimType.DRAW_IN,
    val animDurationMs: Long = 800L,
    val currentColor: Long = 0xFFFF0055,
    val currentStrokeWidth: Float = 10f
)

data class TextConfig(
    val text: String = "Your Text Here",
    val fontFamilyName: String = "Roboto",
    val fontSizeSp: Float = 26f,
    val textColor: Long = 0xFFFFFFFF,
    val backgroundColor: Long = 0x88000000,
    val strokeColor: Long = 0xFF000000,
    val strokeWidth: Float = 0f,
    val hasShadow: Boolean = true,
    val alignment: Int = 1, // 0: Left, 1: Center, 2: Right
    val isCustomFont: Boolean = false,
    val customFontName: String? = null
)

enum class EqualizerPreset(val label: String) {
    FLAT("Flat"),
    BASS_BOOST("Bass Boost"),
    VOCAL("Vocal"),
    POP("Pop"),
    ROCK("Rock"),
    CUSTOM("Custom 10-Band")
}

data class AudioTrack(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val artist: String = "YouTube Audio Library",
    val uri: String? = null,
    val isBuiltIn: Boolean = true,
    val addedTimestamp: Long = System.currentTimeMillis(),
    val startMs: Long = 0L,
    val durationMs: Long = 30000L,
    val volume: Float = 100f, // 100% to 300%!
    val fadeInMs: Long = 500L,
    val fadeOutMs: Long = 500L,
    val equalizerPreset: EqualizerPreset = EqualizerPreset.FLAT,
    // 10-band sliders: 31Hz, 62Hz, 125Hz, 250Hz, 500Hz, 1kHz, 2kHz, 4kHz, 8kHz, 16kHz (-12dB to +12dB)
    val eqBands: List<Float> = listOf(0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f)
)

enum class BgType(val label: String) {
    BLUR("Default Blur"),
    BLACK("Black"),
    WHITE("White"),
    COLOR("Color"),
    MEDIA("Custom Media")
}

data class BackgroundConfig(
    val type: BgType = BgType.BLUR,
    val blurIntensity: Float = 40f, // 0..100
    val solidColor: Long = 0xFF000000,
    val customMediaUri: String? = null
)

data class TransformConfig(
    val rotation: Float = 0f, // -180..180
    val scale: Float = 1.0f, // 0.1..10.0
    val flipHorizontal: Boolean = false,
    val flipVertical: Boolean = false,
    val mirror: Boolean = false,
    val positionX: Float = 0f,
    val positionY: Float = 0f,
    val positionZ: Int = 1 // 1..70 (depth/layer stack)
)

data class CropConfig(
    val isEnabled: Boolean = false,
    val left: Float = 0f, // 0..1 normalized
    val top: Float = 0f,
    val right: Float = 1f,
    val bottom: Float = 1f
)

data class HslAdjust(
    val hue: Float = 0f, // -180..180
    val saturation: Float = 0f, // -100..100
    val lightness: Float = 0f // -100..100
)

data class AdjustConfig(
    val opacity: Float = 100f, // 0..100%
    val fade: Float = 0f, // 0..100%
    val exposure: Float = 0f, // -100..100
    val saturation: Float = 0f, // -100..100
    val contrast: Float = 0f, // -100..100
    val sharpness: Float = 0f, // 0..100
    val hdr: Float = 0f, // 0..100 dynamic range enhancement
    val vignette: Float = 0f, // 0..100
    val temperature: Float = 0f, // -100 (cold) .. 100 (warm)
    val hue: Float = 0f, // -180..180
    val hslChannels: Map<String, HslAdjust> = mapOf(
        "Red" to HslAdjust(),
        "Orange" to HslAdjust(),
        "Yellow" to HslAdjust(),
        "Green" to HslAdjust(),
        "Cyan" to HslAdjust(),
        "Blue" to HslAdjust(),
        "Purple" to HslAdjust(),
        "Magenta" to HslAdjust()
    )
)

enum class TransitionType(val label: String) {
    NONE("None"),
    FADE("Fade"),
    ZOOM("Zoom"),
    ENTRY_UP("Entry Up"),
    ENTRY_DOWN("Entry Down"),
    ENTRY_LEFT("Entry Left"),
    ENTRY_RIGHT("Entry Right"),
    SPIN_LEFT("Spin Left"),
    SPIN_RIGHT("Spin Right"),
    FLIP_H("Horizontal Flip"),
    FLIP_V("Vertical Flip"),
    DISSOLVE("Dissolve"),
    CROSS_DISSOLVE("Cross Dissolve")
}

data class TransitionConfig(
    val type: TransitionType = TransitionType.NONE,
    val durationMs: Long = 600L
)

enum class BlendModeType(val label: String) {
    NORMAL("Normal"),
    SCREEN("Screen"),
    MULTIPLY("Multiply"),
    OVERLAY("Overlay"),
    HARD_LIGHT("Hard Light"),
    DIVIDE("Divide")
}

data class ChromaKeyConfig(
    val isEnabled: Boolean = false,
    val targetColor: Long = 0xFF00FF00, // Default pure green
    val strength: Float = 45f, // 0..100
    val shadow: Float = 30f, // 0..100
    val spillThreshold: Float = 25f // 0..100
)

data class Clip(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val layerType: LayerType = LayerType.MAIN_TRACK,
    val mediaType: MediaType = MediaType.VIDEO,
    val uri: String? = null,
    val previewColor: Long = 0xFF2A85FF,
    val startMs: Long = 0L,
    val durationMs: Long = 5000L,
    val speed: Float = 1.0f, // 0.20x to 5.0x
    val isReversed: Boolean = false,
    val isFrozen: Boolean = false,
    val freezeDurationMs: Long = 2000L,
    val isLocked: Boolean = false,
    val isVisible: Boolean = true,
    val transform: TransformConfig = TransformConfig(),
    val crop: CropConfig = CropConfig(),
    val mask: MaskConfig = MaskConfig(),
    val blur: BlurConfig = BlurConfig(),
    val doodle: DrawDoodle = DrawDoodle(),
    val text: TextConfig = TextConfig(),
    val adjust: AdjustConfig = AdjustConfig(),
    val transition: TransitionConfig = TransitionConfig(),
    val blendMode: BlendModeType = BlendModeType.NORMAL,
    val chromaKey: ChromaKeyConfig = ChromaKeyConfig(),
    val keyframes: List<Keyframe> = emptyList()
)

data class EditorProject(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "New YouTube Project",
    val aspectRatio: AspectRatio = AspectRatio.RATIO_9_16,
    val durationMs: Long = 15000L,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val thumbnailUri: String? = null,
    val mainClips: List<Clip> = emptyList(),
    val pipLayers: List<Clip> = emptyList(), // Up to 70 layers supported
    val audioTracks: List<AudioTrack> = emptyList(),
    val background: BackgroundConfig = BackgroundConfig()
) {
    val title: String get() = name
}

enum class ExportResolution(val label: String, val width: Int, val height: Int, val defaultBitrateMbps: Float) {
    RES_720P("720P HD", 1280, 720, 7.5f),
    RES_1080P("1080P FHD", 1920, 1080, 12.0f)
}

enum class ExportFps(val fps: Int) {
    FPS_30(30),
    FPS_60(60)
}

data class ExportConfig(
    val resolution: ExportResolution = ExportResolution.RES_1080P,
    val fps: ExportFps = ExportFps.FPS_60,
    val bitrateMbps: Float = 12.0f // 7.5 for 720P, 12 for 1080P
)

enum class PreviewLayoutStyle(val label: String, val description: String) {
    SHORT_VERTICAL(
        "Short Video (Vertical)",
        "2 layers visible at top, scroll down to see all 70 layers. Enlarges preview screen."
    ),
    LONG_HORIZONTAL(
        "Long Video (Horizontal)",
        "Horizontal workspace arrangement with side-by-side timeline controls."
    )
}

enum class AppThemeSetting(val label: String) {
    DARK("Dark"),
    LIGHT("White / Light"),
    SYSTEM("System Default")
}

data class AppSettings(
    val previewLayoutStyle: PreviewLayoutStyle = PreviewLayoutStyle.SHORT_VERTICAL,
    val appTheme: AppThemeSetting = AppThemeSetting.DARK,
    val losslessDecodeEnabled: Boolean = true
)
