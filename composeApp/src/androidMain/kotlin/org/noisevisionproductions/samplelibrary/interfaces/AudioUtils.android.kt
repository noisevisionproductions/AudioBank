package org.noisevisionproductions.samplelibrary.interfaces

import android.util.Log
import be.tarsos.dsp.AudioDispatcher
import be.tarsos.dsp.io.jvm.AudioDispatcherFactory
import be.tarsos.dsp.onsets.OnsetHandler
import be.tarsos.dsp.onsets.PercussionOnsetDetector
import be.tarsos.dsp.pitch.PitchDetectionHandler
import be.tarsos.dsp.pitch.PitchProcessor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.math.log2

actual suspend fun getPitchFromAudioFile(audioUrl: String): Float? = withContext(Dispatchers.IO) {
    try {
        val file = File(audioUrl)
        if (!file.exists()) {
            throw IllegalArgumentException("Plik nie istnieje: $audioUrl")
        }

        // Android specific AudioDispatcherFactory
        val dispatcher: AudioDispatcher =
            AudioDispatcherFactory.fromPipe(audioUrl, 44100, 1024, 512)
        var detectedPitch: Float? = null
        val pitchDetectionHandler = PitchDetectionHandler { result, _ ->
            val pitchInHz = result.pitch
            if (pitchInHz > 0) {
                detectedPitch = pitchInHz
            }
        }
        val pitchProcessor = PitchProcessor(
            PitchProcessor.PitchEstimationAlgorithm.YIN,
            44100F,
            1024,
            pitchDetectionHandler
        )
        dispatcher.addAudioProcessor(pitchProcessor)

        dispatcher.run()

        return@withContext detectedPitch
    } catch (e: Exception) {
        Log.e(
            "Error getting pitch from audio file",
            "Error getting pitch from audio file " + e.message
        )
        return@withContext null
    }
}

actual fun hzToMusicalNoteFrequency(frequency: Float): String {
    val noteNames = arrayOf("C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B")
    val noteNumber = 12 * (log2(frequency / 440.0F)).toInt() + 69
    return noteNames[noteNumber % 12] + (noteNumber / 12 - 1)
}

actual fun getBpmFromAudioFile(audioFilePath: String): Float? {
    val audioFile = File(audioFilePath)
    if (!audioFile.exists()) {
        Log.e("BPM", "Plik audio nie istnieje: $audioFilePath")
        return null
    }

    return try {
        val sampleRate = 44100.0  // Dostosuj do częstotliwości próbkowania pliku audio
        val bufferSize = 1024
        val bufferOverlap = 512

        val dispatcher = AudioDispatcherFactory.fromFile(audioFile, bufferSize, bufferOverlap)
        val onsetTimes = mutableListOf<Double>()

        val onsetHandler = OnsetHandler { time, _ ->
            onsetTimes.add(time)
        }

        val threshold = 8.0  // Dostosuj próg detekcji w razie potrzeby
        val sensitivity = 20.0  // Dostosuj czułość detektora
        val onsetDetector = PercussionOnsetDetector(
            sampleRate.toFloat(),
            bufferSize,
            onsetHandler,
            sensitivity,
            threshold
        )
        dispatcher.addAudioProcessor(onsetDetector)

        dispatcher.run()

        if (onsetTimes.size < 2) {
            Log.w("BPM", "Zbyt mało uderzeń do obliczenia BPM")
            null
        } else {
            val intervals = onsetTimes.zipWithNext { a, b -> b - a }
            val averageInterval = intervals.average()
            val bpm = 60.0 / averageInterval
            Log.d("BPM", "Obliczone BPM dla $audioFilePath: $bpm")
            bpm.toFloat()
        }
    } catch (e: Exception) {
        Log.e("BPM", "Błąd przetwarzania pliku: ${e.message}")
        null
    }
}
