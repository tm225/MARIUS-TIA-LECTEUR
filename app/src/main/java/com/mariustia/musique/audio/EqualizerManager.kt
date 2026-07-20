package com.mariustia.musique.audio

import android.media.audiofx.Equalizer
import androidx.compose.runtime.mutableStateOf

data class EqBand(
    val index: Short,
    val minLevel: Short,
    val maxLevel: Short,
    val centerFreqHz: Int,
    val currentLevel: Short
)

object EqualizerManager {
    private var equalizer: Equalizer? = null
    private var attachedSessionId: Int = -1

    val isAvailable = mutableStateOf(false)
    val isEnabled = mutableStateOf(false)
    val bands = mutableStateOf<List<EqBand>>(emptyList())
    val presets = mutableStateOf<List<String>>(emptyList())
    val currentPreset = mutableStateOf<Short>(-1)

    fun attach(audioSessionId: Int) {
        if (audioSessionId == 0 || audioSessionId == attachedSessionId) return
        try {
            equalizer?.release()
            val eq = Equalizer(0, audioSessionId)
            equalizer = eq
            attachedSessionId = audioSessionId
            isAvailable.value = true
            isEnabled.value = eq.enabled

            val bandCount = eq.numberOfBands
            bands.value = (0 until bandCount).map { i ->
                val idx = i.toShort()
                val range = eq.bandLevelRange
                EqBand(
                    index = idx,
                    minLevel = range[0],
                    maxLevel = range[1],
                    centerFreqHz = eq.getCenterFreq(idx) / 1000,
                    currentLevel = eq.getBandLevel(idx)
                )
            }
            presets.value = (0 until eq.numberOfPresets).map { eq.getPresetName(it.toShort()) }
            currentPreset.value = try { eq.currentPreset } catch (e: Exception) { -1 }
        } catch (e: Exception) {
            isAvailable.value = false
        }
    }

    fun setEnabled(enabled: Boolean) {
        equalizer?.enabled = enabled
        isEnabled.value = enabled
    }

    fun setBandLevel(band: Short, level: Short) {
        equalizer?.setBandLevel(band, level)
        bands.value = bands.value.map { if (it.index == band) it.copy(currentLevel = level) else it }
        currentPreset.value = -1
    }

    fun usePreset(preset: Short) {
        val eq = equalizer ?: return
        eq.usePreset(preset)
        currentPreset.value = preset
        bands.value = bands.value.map { it.copy(currentLevel = eq.getBandLevel(it.index)) }
    }

    fun release() {
        equalizer?.release()
        equalizer = null
        attachedSessionId = -1
        isAvailable.value = false
    }
}
