package org.noisevisionproductions.samplelibrary.interfaces

expect suspend fun getPitchFromAudioFile(audioUrl: String): Float?
expect fun hzToMusicalNoteFrequency(frequency: Float): String
expect fun getBpmFromAudioFile(audioFilePath: String): Float?