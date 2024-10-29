package org.noisevisionproductions.samplelibrary.composeUI.screens.forum.likes

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.noisevisionproductions.samplelibrary.utils.models.PostModel

class LikeManager {
    private val _postLikeStates = MutableStateFlow<Map<String, LikeState>>(emptyMap())
    val postLikeStates: StateFlow<Map<String, LikeState>> = _postLikeStates.asStateFlow()

    private val _likedPostsIds = MutableStateFlow<Set<String>>(emptySet())
    val likedPostsIds: StateFlow<Set<String>> = _likedPostsIds.asStateFlow()

    private val _commentLikeStates =
        MutableStateFlow<Map<String, Map<String, LikeState>>>(emptyMap())
    val commentLikeStates: StateFlow<Map<String, Map<String, LikeState>>> =
        _commentLikeStates.asStateFlow()

    data class LikeState(
        val isLiked: Boolean,
        val likesCount: Int
    )

    fun updateLikedPosts(posts: List<PostModel>) {
        _likedPostsIds.value = posts.map { it.postId }.toSet()
        // Update like states for all posts
        posts.forEach { post ->
            updatePostLike(post.postId, LikeState(isLiked = true, likesCount = post.likesCount))
        }
    }

    fun updatePostLike(postId: String, likeState: LikeState) {
        _postLikeStates.update { currentStates ->
            currentStates.toMutableMap().apply {
                put(postId, likeState)
            }
        }
        // Update likedPostsIds based on the new like state
        if (likeState.isLiked) {
            _likedPostsIds.update { it + postId }
        } else {
            _likedPostsIds.update { it - postId }
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

    fun getPostLikeState(postId: String): LikeState {
        return _postLikeStates.value[postId] ?: LikeState(isLiked = false, likesCount = 0)
    }


    fun initializePostLikeState(post: PostModel, isLiked: Boolean) {
        updatePostLike(
            postId = post.postId,
            LikeState(
                isLiked = isLiked,
                likesCount = post.likesCount  // Using likesCount from PostModel
            )
        )
    }

    fun getCommentLikeState(postId: String, commentId: String): LikeState {
        return _commentLikeStates.value[postId]?.get(commentId) ?: LikeState(
            isLiked = false,
            likesCount = 0
        )
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