package org.noisevisionproductions.samplelibrary.composeUI.screens.dataStatesManagement

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

object SharedSoundEventsManager {
    private val _soundEvents = MutableSharedFlow<SoundEvent>(
        replay = 0,
        extraBufferCapacity = 64
    )
    val soundEvents = _soundEvents.asSharedFlow()

    suspend fun emitEvent(event: SoundEvent) {
        _soundEvents.emit(event)
    }
}