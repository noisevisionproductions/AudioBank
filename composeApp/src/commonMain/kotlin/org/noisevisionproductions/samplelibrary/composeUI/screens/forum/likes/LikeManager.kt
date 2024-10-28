package org.noisevisionproductions.samplelibrary.composeUI.screens.forum.likes

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class LikeManager {
    private val _postLikeStates = MutableStateFlow<Map<String, LikeState>>(emptyMap())
    val postLikeStates: StateFlow<Map<String, LikeState>> = _postLikeStates.asStateFlow()

    private val _commentLikeStates =
        MutableStateFlow<Map<String, Map<String, LikeState>>>(emptyMap())
    val commentLikeStates: StateFlow<Map<String, Map<String, LikeState>>> =
        _commentLikeStates.asStateFlow()

    data class LikeState(
        val isLiked: Boolean,
        val likesCount: Int
    )

    fun updatePostLike(postId: String, likeState: LikeState) {
        _postLikeStates.update { currentStates ->
            currentStates.toMutableMap().apply {
                put(postId, likeState)
            }
        }
    }

    fun updateCommentLike(postId: String, commentId: String, likeState: LikeState) {
        _commentLikeStates.update { currentStates ->
            val postComments = currentStates[postId]?.toMutableMap() ?: mutableMapOf()
            postComments[commentId] = likeState
            currentStates.toMutableMap().apply {
                put(postId, postComments)
            }
        }
    }

    fun getPostLikeState(postId: String): LikeState? {
        return _postLikeStates.value[postId]
    }

    fun initializePostLikeState(postId: String, isLiked: Boolean, likesCount: Int) {
        updatePostLike(
            postId,
            LikeState(isLiked, likesCount)
        )
    }

    fun getCommentLikeState(postId: String, commentId: String): LikeState? {
        return _commentLikeStates.value[postId]?.get(commentId)
    }

    fun initializeCommentLikeState(
        postId: String,
        commentId: String,
        isLiked: Boolean,
        likesCount: Int
    ) {
        updateCommentLike(postId, commentId, LikeState(isLiked, likesCount))
    }
}