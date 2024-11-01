package org.noisevisionproductions.samplelibrary.composeUI.screens.dataStatesManagement

import org.noisevisionproductions.samplelibrary.utils.metadata.AudioMetadata

sealed class SoundEvent {
    data class SoundUploaded(val sound: AudioMetadata) : SoundEvent()
    data class SoundDeleted(val soundId: String) : SoundEvent()
    data class SoundLiked(val soundId: String) : SoundEvent()
    data class SoundUnliked(val soundId: String) : SoundEvent()
    data class SoundMetadataUpdated(val soundId: String, val updates: Map<String, Any>) :
        SoundEvent()
}