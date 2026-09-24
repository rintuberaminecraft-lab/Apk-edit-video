package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.DefaultSampleProvider
import com.example.data.ProjectRepository
import com.example.data.SettingsRepository
import com.example.model.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.math.abs

sealed class EditorSheet {
    object NONE : EditorSheet()
    object KEYFRAME_GRAPH : EditorSheet()
    object MASK : EditorSheet()
    object BLUR : EditorSheet()
    object DOODLE : EditorSheet()
    object TEXT : EditorSheet()
    object AUDIO : EditorSheet()
    object BACKGROUND : EditorSheet()
    object TRANSFORM : EditorSheet()
    object CROP : EditorSheet()
    object ADJUST : EditorSheet()
    object TRANSITION : EditorSheet()
    object BLEND : EditorSheet()
    object CHROMA_KEY : EditorSheet()
    object SPEED : EditorSheet()
    object PIP_LAYERS : EditorSheet()
    object EXPORT : EditorSheet()
    object CANVAS_RATIO : EditorSheet()
}

data class ExportProgress(
    val isExporting: Boolean = false,
    val isCompleted: Boolean = false,
    val progressPercent: Int = 0,
    val currentFrame: Int = 0,
    val totalFrames: Int = 0,
    val outputFileName: String = "",
    val outputSizeMb: Float = 0f,
    val estimatedTimeRemainingSec: Int = 0,
    val config: ExportConfig = ExportConfig()
)

class EditorViewModel(application: Application) : AndroidViewModel(application) {
    private val projectRepository = ProjectRepository(application)
    val settingsRepository = SettingsRepository(application)

    val settings: StateFlow<AppSettings> = settingsRepository.settings

    private val _allProjects = MutableStateFlow<List<EditorProject>>(emptyList())
    val allProjects: StateFlow<List<EditorProject>> = _allProjects.asStateFlow()
    val recentProjects: StateFlow<List<EditorProject>> = _allProjects.asStateFlow()

    private val _project = MutableStateFlow<EditorProject?>(null)
    val project: StateFlow<EditorProject?> = _project.asStateFlow()

    private val _playheadMs = MutableStateFlow(0L)
    val playheadMs: StateFlow<Long> = _playheadMs.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _selectedClipId = MutableStateFlow<String?>(null)
    val selectedClipId: StateFlow<String?> = _selectedClipId.asStateFlow()

    private val _selectedAudioId = MutableStateFlow<String?>(null)
    val selectedAudioId: StateFlow<String?> = _selectedAudioId.asStateFlow()

    private val _activeSheet = MutableStateFlow<EditorSheet>(EditorSheet.NONE)
    val activeSheet: StateFlow<EditorSheet> = _activeSheet.asStateFlow()

    private val _showKeyframeTooltip = MutableStateFlow(true)
    val showKeyframeTooltip: StateFlow<Boolean> = _showKeyframeTooltip.asStateFlow()

    private val _exportProgress = MutableStateFlow<ExportProgress?>(null)
    val exportProgress: StateFlow<ExportProgress?> = _exportProgress.asStateFlow()

    // Undo / Redo stacks
    private val undoStack = mutableListOf<EditorProject>()
    private val redoStack = mutableListOf<EditorProject>()

    private var playbackJob: Job? = null
    private var exportJob: Job? = null

    init {
        viewModelScope.launch {
            projectRepository.getAllProjects().collect { projects ->
                _allProjects.value = projects
                if (_project.value == null && projects.isNotEmpty()) {
                    loadProject(projects.first().id)
                }
            }
        }
    }

    fun dismissKeyframeTooltip() {
        _showKeyframeTooltip.value = false
    }

    fun loadProject(id: String) {
        viewModelScope.launch {
            val proj = projectRepository.getProjectById(id)
                ?: _allProjects.value.find { it.id == id }
                ?: DefaultSampleProvider.createInitialSampleProject()
            _project.value = proj
            _playheadMs.value = 0L
            _selectedClipId.value = proj.mainClips.firstOrNull()?.id
            undoStack.clear()
            redoStack.clear()
        }
    }

    fun createNewProject(aspectRatio: AspectRatio, customName: String? = null) {
        viewModelScope.launch {
            val name = customName?.ifBlank { null }
                ?: (if (aspectRatio.isShorts) "Shorts Project #${_allProjects.value.size + 1}" else "YouTube Video #${_allProjects.value.size + 1}")
            val initial = DefaultSampleProvider.createInitialSampleProject(aspectRatio).copy(
                id = UUID.randomUUID().toString(),
                name = name,
                aspectRatio = aspectRatio
            )
            projectRepository.saveProject(initial)
            _project.value = initial
            _playheadMs.value = 0L
            _selectedClipId.value = initial.mainClips.firstOrNull()?.id
        }
    }

    fun createNewProject(title: String, ratio: AspectRatio): String {
        val newId = UUID.randomUUID().toString()
        viewModelScope.launch {
            val initial = DefaultSampleProvider.createInitialSampleProject(ratio).copy(
                id = newId,
                name = title,
                aspectRatio = ratio
            )
            projectRepository.saveProject(initial)
            _project.value = initial
            _playheadMs.value = 0L
            _selectedClipId.value = initial.mainClips.firstOrNull()?.id
        }
        return newId
    }

    fun deleteProjectById(id: String) {
        deleteProject(id)
    }

    fun updatePreviewLayoutStyle(style: PreviewLayoutStyle) {
        updateSettings(settings.value.copy(previewLayoutStyle = style))
    }

    fun updateThemeMode(theme: AppThemeSetting) {
        updateSettings(settings.value.copy(appTheme = theme))
    }

    fun updateLosslessDecode(enabled: Boolean) {
        updateSettings(settings.value.copy(losslessDecodeEnabled = enabled))
    }

    fun deleteProject(id: String) {
        viewModelScope.launch {
            projectRepository.deleteProject(id)
            if (_project.value?.id == id) {
                val remaining = _allProjects.value.filter { it.id != id }
                if (remaining.isNotEmpty()) {
                    loadProject(remaining.first().id)
                }
            }
        }
    }

    fun duplicateProject(projectToDuplicate: EditorProject) {
        viewModelScope.launch {
            val duplicated = projectToDuplicate.copy(
                id = UUID.randomUUID().toString(),
                name = "${projectToDuplicate.name} (Copy)",
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            projectRepository.saveProject(duplicated)
        }
    }

    fun updateSettings(newSettings: AppSettings) {
        settingsRepository.updateSettings(newSettings)
    }

    fun setAspectRatio(ratio: AspectRatio) {
        val current = _project.value ?: return
        recordUndo()
        val updated = current.copy(aspectRatio = ratio, updatedAt = System.currentTimeMillis())
        updateCurrentProject(updated)
    }

    fun openSheet(sheet: EditorSheet) {
        _activeSheet.value = sheet
    }

    fun closeSheet() {
        _activeSheet.value = EditorSheet.NONE
    }

    fun selectClip(id: String?) {
        _selectedClipId.value = id
        _selectedAudioId.value = null
    }

    fun selectAudio(id: String?) {
        _selectedAudioId.value = id
        _selectedClipId.value = null
    }

    fun togglePlayPause() {
        if (_isPlaying.value) {
            pausePlayback()
        } else {
            startPlayback()
        }
    }

    fun pausePlayback() {
        _isPlaying.value = false
        playbackJob?.cancel()
        playbackJob = null
    }

    fun startPlayback() {
        val current = _project.value ?: return
        _isPlaying.value = true
        playbackJob?.cancel()
        playbackJob = viewModelScope.launch {
            val totalDuration = current.durationMs
            var currentPos = _playheadMs.value
            if (currentPos >= totalDuration) {
                currentPos = 0L
            }
            val frameIntervalMs = 33L // ~30 fps update
            while (_isPlaying.value && currentPos < totalDuration) {
                delay(frameIntervalMs)
                currentPos = (currentPos + frameIntervalMs).coerceAtMost(totalDuration)
                _playheadMs.value = currentPos
            }
            if (currentPos >= totalDuration) {
                _isPlaying.value = false
                _playheadMs.value = 0L
            }
        }
    }

    fun seekTo(positionMs: Long) {
        val current = _project.value ?: return
        val clamped = positionMs.coerceIn(0L, current.durationMs)
        _playheadMs.value = clamped
    }

    val selectedClip: StateFlow<Clip?> = combine(_project, _selectedClipId) { proj, clipId ->
        if (proj == null || clipId == null) null
        else proj.mainClips.find { it.id == clipId } ?: proj.pipLayers.find { it.id == clipId }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Keyframe Management
    val hasKeyframeAtPlayhead: StateFlow<Boolean> = combine(selectedClip, _playheadMs) { clip, timeMs ->
        if (clip == null) false
        else clip.keyframes.any { abs(it.timeMs - timeMs) <= 150L }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    fun toggleKeyframeAtPlayhead() {
        val currentProj = _project.value ?: return
        val clip = selectedClip.value ?: return
        val playhead = _playheadMs.value
        recordUndo()

        val existingIndex = clip.keyframes.indexOfFirst { abs(it.timeMs - playhead) <= 150L }
        val updatedKeyframes = clip.keyframes.toMutableList()

        if (existingIndex >= 0) {
            // Remove existing keyframe
            updatedKeyframes.removeAt(existingIndex)
        } else {
            // Add new keyframe with current clip values
            val newKeyframe = Keyframe(
                timeMs = playhead,
                scale = clip.transform.scale,
                rotation = clip.transform.rotation,
                positionX = clip.transform.positionX,
                positionY = clip.transform.positionY,
                opacity = clip.adjust.opacity / 100f,
                curvePreset = CurvePreset.EASE
            )
            updatedKeyframes.add(newKeyframe)
            updatedKeyframes.sortBy { it.timeMs }
        }

        updateClip(clip.copy(keyframes = updatedKeyframes))
        _showKeyframeTooltip.value = false
    }

    fun jumpToPreviousKeyframe() {
        val clip = selectedClip.value ?: return
        val playhead = _playheadMs.value
        val prev = clip.keyframes.filter { it.timeMs < playhead - 50L }.maxByOrNull { it.timeMs }
        if (prev != null) {
            seekTo(prev.timeMs)
        }
    }

    fun jumpToNextKeyframe() {
        val clip = selectedClip.value ?: return
        val playhead = _playheadMs.value
        val next = clip.keyframes.filter { it.timeMs > playhead + 50L }.minByOrNull { it.timeMs }
        if (next != null) {
            seekTo(next.timeMs)
        }
    }

    fun updateSelectedKeyframeCurve(preset: CurvePreset) {
        val clip = selectedClip.value ?: return
        val playhead = _playheadMs.value
        val kf = clip.keyframes.minByOrNull { abs(it.timeMs - playhead) } ?: return
        recordUndo()
        val updated = clip.keyframes.map {
            if (it.id == kf.id) it.copy(curvePreset = preset) else it
        }
        updateClip(clip.copy(keyframes = updated))
    }

    fun updateCustomBezierHandles(
        keyframeId: String,
        leftHandleX: Float,
        leftHandleY: Float,
        rightHandleX: Float,
        rightHandleY: Float
    ) {
        val clip = selectedClip.value ?: return
        val updated = clip.keyframes.map {
            if (it.id == keyframeId) {
                it.copy(
                    curvePreset = CurvePreset.CUSTOM_BEZIER,
                    leftHandleX = leftHandleX.coerceIn(-1.0f, 0.0f),
                    leftHandleY = leftHandleY.coerceIn(-1.0f, 1.0f),
                    rightHandleX = rightHandleX.coerceIn(0.0f, 1.0f),
                    rightHandleY = rightHandleY.coerceIn(-1.0f, 1.0f)
                )
            } else it
        }
        updateClip(clip.copy(keyframes = updated))
    }

    // Clip Transformations & Quick Tools
    fun updateTransform(
        scale: Float? = null,
        rotation: Float? = null,
        posX: Float? = null,
        posY: Float? = null,
        posZ: Int? = null,
        flipH: Boolean? = null,
        flipV: Boolean? = null,
        mirror: Boolean? = null
    ) {
        val clip = selectedClip.value ?: return
        val t = clip.transform
        val newTransform = t.copy(
            scale = scale ?: t.scale,
            rotation = rotation ?: t.rotation,
            positionX = posX ?: t.positionX,
            positionY = posY ?: t.positionY,
            positionZ = posZ ?: t.positionZ,
            flipHorizontal = flipH ?: t.flipHorizontal,
            flipVertical = flipV ?: t.flipVertical,
            mirror = mirror ?: t.mirror
        )

        // If keyframing at current playhead, also update the keyframe
        val playhead = _playheadMs.value
        val kfIndex = clip.keyframes.indexOfFirst { abs(it.timeMs - playhead) <= 150L }
        val updatedKeyframes = if (kfIndex >= 0) {
            clip.keyframes.mapIndexed { idx, kf ->
                if (idx == kfIndex) {
                    kf.copy(
                        scale = scale ?: kf.scale,
                        rotation = rotation ?: kf.rotation,
                        positionX = posX ?: kf.positionX,
                        positionY = posY ?: kf.positionY
                    )
                } else kf
            }
        } else clip.keyframes

        updateClip(clip.copy(transform = newTransform, keyframes = updatedKeyframes))
    }

    fun flipHorizontal() {
        val clip = selectedClip.value ?: return
        recordUndo()
        updateClip(clip.copy(transform = clip.transform.copy(flipHorizontal = !clip.transform.flipHorizontal)))
    }

    fun flipVertical() {
        val clip = selectedClip.value ?: return
        recordUndo()
        updateClip(clip.copy(transform = clip.transform.copy(flipVertical = !clip.transform.flipVertical)))
    }

    fun splitClip() {
        val current = _project.value ?: return
        val clip = selectedClip.value ?: return
        val playhead = _playheadMs.value

        if (playhead <= clip.startMs || playhead >= clip.startMs + clip.durationMs) {
            return
        }

        recordUndo()
        val firstDuration = playhead - clip.startMs
        val secondDuration = clip.durationMs - firstDuration

        val firstClip = clip.copy(durationMs = firstDuration)
        val secondClip = clip.copy(
            id = UUID.randomUUID().toString(),
            name = "${clip.name} (Part 2)",
            startMs = playhead,
            durationMs = secondDuration
        )

        if (clip.layerType == LayerType.MAIN_TRACK) {
            val index = current.mainClips.indexOfFirst { it.id == clip.id }
            if (index >= 0) {
                val newMain = current.mainClips.toMutableList()
                newMain[index] = firstClip
                newMain.add(index + 1, secondClip)
                updateCurrentProject(current.copy(mainClips = newMain))
                _selectedClipId.value = secondClip.id
            }
        } else {
            val index = current.pipLayers.indexOfFirst { it.id == clip.id }
            if (index >= 0) {
                val newPip = current.pipLayers.toMutableList()
                newPip[index] = firstClip
                newPip.add(index + 1, secondClip)
                updateCurrentProject(current.copy(pipLayers = newPip))
                _selectedClipId.value = secondClip.id
            }
        }
    }

    fun duplicateClip() {
        val current = _project.value ?: return
        val clip = selectedClip.value ?: return
        recordUndo()

        val copyClip = clip.copy(
            id = UUID.randomUUID().toString(),
            name = "${clip.name} (Copy)",
            startMs = clip.startMs + clip.durationMs
        )

        if (clip.layerType == LayerType.MAIN_TRACK) {
            val newMain = current.mainClips + copyClip
            updateCurrentProject(current.copy(mainClips = newMain, durationMs = maxOf(current.durationMs, copyClip.startMs + copyClip.durationMs)))
            _selectedClipId.value = copyClip.id
        } else {
            val newPip = current.pipLayers + copyClip
            updateCurrentProject(current.copy(pipLayers = newPip))
            _selectedClipId.value = copyClip.id
        }
    }

    fun deleteClip() {
        val current = _project.value ?: return
        val clip = selectedClip.value ?: return
        recordUndo()

        if (clip.layerType == LayerType.MAIN_TRACK) {
            if (current.mainClips.size <= 1) return // Keep at least one clip
            val newMain = current.mainClips.filter { it.id != clip.id }
            updateCurrentProject(current.copy(mainClips = newMain))
            _selectedClipId.value = newMain.firstOrNull()?.id
        } else {
            val newPip = current.pipLayers.filter { it.id != clip.id }
            updateCurrentProject(current.copy(pipLayers = newPip))
            _selectedClipId.value = current.mainClips.firstOrNull()?.id
        }
    }

    fun freezeFrame(holdDurationMs: Long = 2000L) {
        val current = _project.value ?: return
        val clip = selectedClip.value ?: return
        val playhead = _playheadMs.value
        recordUndo()

        val freezeClip = clip.copy(
            id = UUID.randomUUID().toString(),
            name = "Freeze Frame (${holdDurationMs / 1000}s)",
            mediaType = MediaType.IMAGE,
            startMs = playhead,
            durationMs = holdDurationMs,
            isFrozen = true
        )

        if (clip.layerType == LayerType.MAIN_TRACK) {
            val newMain = current.mainClips + freezeClip
            updateCurrentProject(current.copy(mainClips = newMain, durationMs = current.durationMs + holdDurationMs))
            _selectedClipId.value = freezeClip.id
        } else {
            val newPip = current.pipLayers + freezeClip
            updateCurrentProject(current.copy(pipLayers = newPip))
            _selectedClipId.value = freezeClip.id
        }
    }

    fun toggleReverse() {
        val clip = selectedClip.value ?: return
        recordUndo()
        updateClip(clip.copy(isReversed = !clip.isReversed))
    }

    fun setSpeed(speed: Float) {
        val clip = selectedClip.value ?: return
        recordUndo()
        val clamped = speed.coerceIn(0.20f, 5.0f)
        val newDuration = (clip.durationMs * (clip.speed / clamped)).toLong().coerceAtLeast(500L)
        updateClip(clip.copy(speed = clamped, durationMs = newDuration))
    }

    // PIP Layer Management (Supporting up to 70 layers!)
    fun addPipLayer(
        mediaType: MediaType = MediaType.VIDEO,
        name: String? = null,
        previewColor: Long? = null,
        uri: String? = null
    ) {
        val current = _project.value ?: return
        if (current.pipLayers.size >= 70) return // Respect 70 layer cap
        recordUndo()

        val layerIndex = current.pipLayers.size + 1
        val newPip = Clip(
            id = UUID.randomUUID().toString(),
            name = name ?: "PIP Layer #$layerIndex",
            layerType = LayerType.PIP_LAYER,
            mediaType = mediaType,
            uri = uri,
            previewColor = previewColor ?: getPaletteColor(layerIndex),
            startMs = _playheadMs.value,
            durationMs = 5000L,
            transform = TransformConfig(
                scale = 0.5f,
                positionX = ((layerIndex % 4 - 1.5f) * 0.2f),
                positionY = ((layerIndex % 3 - 1f) * 0.2f),
                positionZ = layerIndex
            )
        )

        val updated = current.copy(pipLayers = current.pipLayers + newPip)
        updateCurrentProject(updated)
        _selectedClipId.value = newPip.id
    }

    fun toggleLayerLock(clipId: String) {
        val clip = findClip(clipId) ?: return
        updateClip(clip.copy(isLocked = !clip.isLocked))
    }

    fun toggleLayerVisibility(clipId: String) {
        val clip = findClip(clipId) ?: return
        updateClip(clip.copy(isVisible = !clip.isVisible))
    }

    // Tools Customization
    fun updateMask(mask: MaskConfig) {
        val clip = selectedClip.value ?: return
        updateClip(clip.copy(mask = mask))
    }

    fun updateBlur(blur: BlurConfig) {
        val clip = selectedClip.value ?: return
        updateClip(clip.copy(blur = blur))
    }

    fun updateDoodle(doodle: DrawDoodle) {
        val clip = selectedClip.value ?: return
        updateClip(clip.copy(doodle = doodle))
    }

    fun updateText(text: TextConfig) {
        val clip = selectedClip.value ?: return
        updateClip(clip.copy(text = text))
    }

    fun updateAdjust(adjust: AdjustConfig) {
        val clip = selectedClip.value ?: return
        updateClip(clip.copy(adjust = adjust))
    }

    fun updateCrop(crop: CropConfig) {
        val clip = selectedClip.value ?: return
        updateClip(clip.copy(crop = crop))
    }

    fun updateTransition(transition: TransitionConfig) {
        val clip = selectedClip.value ?: return
        updateClip(clip.copy(transition = transition))
    }

    fun updateBlendMode(blend: BlendModeType) {
        val clip = selectedClip.value ?: return
        updateClip(clip.copy(blendMode = blend))
    }

    fun updateChromaKey(chroma: ChromaKeyConfig) {
        val clip = selectedClip.value ?: return
        updateClip(clip.copy(chromaKey = chroma))
    }

    fun updateBackground(bg: BackgroundConfig) {
        val current = _project.value ?: return
        recordUndo()
        updateCurrentProject(current.copy(background = bg))
    }

    // Audio Tools
    fun addAudioTrack(track: AudioTrack) {
        val current = _project.value ?: return
        recordUndo()
        val updated = current.copy(audioTracks = current.audioTracks + track)
        updateCurrentProject(updated)
        _selectedAudioId.value = track.id
    }

    fun updateAudioVolume(audioId: String, volume: Float) {
        val current = _project.value ?: return
        val clamped = volume.coerceIn(100f, 300f)
        val updated = current.copy(audioTracks = current.audioTracks.map {
            if (it.id == audioId) it.copy(volume = clamped) else it
        })
        updateCurrentProject(updated)
    }

    fun updateAudioPreset(audioId: String, preset: EqualizerPreset) {
        val current = _project.value ?: return
        val presetBands = when (preset) {
            EqualizerPreset.FLAT -> listOf(0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f)
            EqualizerPreset.BASS_BOOST -> listOf(6f, 5f, 4f, 2f, 0f, 0f, 0f, 1f, 2f, 2f)
            EqualizerPreset.VOCAL -> listOf(-2f, -1f, 1f, 3f, 5f, 5f, 3f, 1f, 0f, -1f)
            EqualizerPreset.POP -> listOf(2f, 3f, 2f, 1f, 0f, 1f, 3f, 4f, 3f, 2f)
            EqualizerPreset.ROCK -> listOf(5f, 4f, 1f, -1f, -1f, 1f, 3f, 5f, 4f, 3f)
            EqualizerPreset.CUSTOM -> return
        }
        val updated = current.copy(audioTracks = current.audioTracks.map {
            if (it.id == audioId) it.copy(equalizerPreset = preset, eqBands = presetBands) else it
        })
        updateCurrentProject(updated)
    }

    fun updateAudioBand(audioId: String, bandIndex: Int, valueDb: Float) {
        val current = _project.value ?: return
        val updated = current.copy(audioTracks = current.audioTracks.map {
            if (it.id == audioId) {
                val bands = it.eqBands.toMutableList()
                if (bandIndex in bands.indices) {
                    bands[bandIndex] = valueDb.coerceIn(-12f, 12f)
                }
                it.copy(equalizerPreset = EqualizerPreset.CUSTOM, eqBands = bands)
            } else it
        })
        updateCurrentProject(updated)
    }

    fun sortAudioTracks(recentToOld: Boolean) {
        val current = _project.value ?: return
        val sorted = if (recentToOld) {
            current.audioTracks.sortedByDescending { it.addedTimestamp }
        } else {
            current.audioTracks.sortedBy { it.addedTimestamp }
        }
        updateCurrentProject(current.copy(audioTracks = sorted))
    }

    // Export Engine
    fun startExport(config: ExportConfig) {
        val current = _project.value ?: return
        pausePlayback()
        _activeSheet.value = EditorSheet.EXPORT

        exportJob?.cancel()
        val totalFrames = ((current.durationMs / 1000f) * config.fps.fps).toInt().coerceAtLeast(30)
        val bitrate = if (config.resolution == ExportResolution.RES_720P) 7.5f else 12.0f
        val calculatedSizeMb = (bitrate * (current.durationMs / 1000f)) / 8f

        _exportProgress.value = ExportProgress(
            isExporting = true,
            isCompleted = false,
            progressPercent = 0,
            currentFrame = 0,
            totalFrames = totalFrames,
            outputFileName = "TubeCut_${current.name.replace(" ", "_")}_${config.resolution.label.split(" ").first()}.mp4",
            outputSizeMb = calculatedSizeMb,
            estimatedTimeRemainingSec = (totalFrames / 30).coerceAtLeast(1),
            config = config
        )

        exportJob = viewModelScope.launch {
            val stepTime = 120L // Simulated frame encoding
            val steps = 25
            for (i in 1..steps) {
                delay(stepTime)
                val percent = (i * 100) / steps
                val frame = (percent * totalFrames) / 100
                val remaining = ((steps - i) * stepTime / 1000L).toInt()
                _exportProgress.value = _exportProgress.value?.copy(
                    progressPercent = percent,
                    currentFrame = frame,
                    estimatedTimeRemainingSec = remaining
                )
            }
            _exportProgress.value = _exportProgress.value?.copy(
                isExporting = false,
                isCompleted = true,
                progressPercent = 100,
                currentFrame = totalFrames,
                estimatedTimeRemainingSec = 0
            )
        }
    }

    fun cancelExport() {
        exportJob?.cancel()
        exportJob = null
        _exportProgress.value = null
        _activeSheet.value = EditorSheet.NONE
    }

    // Undo / Redo
    fun undo() {
        if (undoStack.isNotEmpty()) {
            val current = _project.value ?: return
            redoStack.add(current)
            val previous = undoStack.removeAt(undoStack.lastIndex)
            _project.value = previous
            viewModelScope.launch { projectRepository.saveProject(previous) }
        }
    }

    fun redo() {
        if (redoStack.isNotEmpty()) {
            val current = _project.value ?: return
            undoStack.add(current)
            val next = redoStack.removeAt(redoStack.lastIndex)
            _project.value = next
            viewModelScope.launch { projectRepository.saveProject(next) }
        }
    }

    val canUndo: Boolean get() = undoStack.isNotEmpty()
    val canRedo: Boolean get() = redoStack.isNotEmpty()

    private fun recordUndo() {
        val current = _project.value ?: return
        if (undoStack.size > 20) undoStack.removeAt(0)
        undoStack.add(current)
        redoStack.clear()
    }

    private fun updateCurrentProject(updated: EditorProject) {
        _project.value = updated
        viewModelScope.launch {
            projectRepository.saveProject(updated)
        }
    }

    private fun updateClip(updatedClip: Clip) {
        val current = _project.value ?: return
        if (updatedClip.layerType == LayerType.MAIN_TRACK) {
            val newMain = current.mainClips.map { if (it.id == updatedClip.id) updatedClip else it }
            updateCurrentProject(current.copy(mainClips = newMain))
        } else {
            val newPip = current.pipLayers.map { if (it.id == updatedClip.id) updatedClip else it }
            updateCurrentProject(current.copy(pipLayers = newPip))
        }
    }

    private fun findClip(id: String): Clip? {
        val current = _project.value ?: return null
        return current.mainClips.find { it.id == id } ?: current.pipLayers.find { it.id == id }
    }

    private fun getPaletteColor(index: Int): Long {
        val colors = listOf(
            0xFFFF1744, 0xFF00E5FF, 0xFFFFEA00, 0xFF76FF03,
            0xFFD500F9, 0xFFFF6D00, 0xFF00B0FF, 0xFF1DE9B6
        )
        return colors[(index - 1) % colors.size]
    }
}
