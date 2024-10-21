package org.noisevisionproductions.samplelibrary.composeUI.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.noisevisionproductions.samplelibrary.database.CommentService
import org.noisevisionproductions.samplelibrary.interfaces.getCurrentTimestamp
import org.noisevisionproductions.samplelibrary.utils.CommentState
import org.noisevisionproductions.samplelibrary.utils.models.CommentModel

class CommentViewModel : ViewModel() {
    private val commentService = CommentService()

    private val _commentText = MutableStateFlow("")
    val commentText: StateFlow<String> = _commentText.asStateFlow()

    private val _commentsByPostId = MutableStateFlow<Map<String, List<CommentModel>>>(emptyMap())
    private val _isLoadingCommentsByPostId = MutableStateFlow<Map<String, Boolean>>(emptyMap())

    private val commentsCache = mutableMapOf<String, List<CommentModel>>()

    fun getCommentsStateForPost(postId: String): StateFlow<CommentState> {
        return combine(
            _commentsByPostId,
            _isLoadingCommentsByPostId
        ) { commentsMap, loadingMap ->
            val comments = commentsMap[postId] ?: emptyList()
            val isLoading = loadingMap[postId] ?: false
            val totalCount = comments.sumOf { getCommentAndReplyCount(it) }

            CommentState(
                comments = comments,
                isLoading = isLoading,
                totalCount = totalCount
            )
        }.stateIn(viewModelScope, SharingStarted.Eagerly, CommentState())
    }

    fun updateCommentText(newText: String) {
        _commentText.value = newText
    }

    fun loadComments(postId: String) {
        if (commentsCache.containsKey(postId)) {
            _commentsByPostId.update { currentMap ->
                currentMap.toMutableMap()
                    .apply { put(postId, commentsCache[postId] ?: emptyList()) }
            }
            return
        }

        viewModelScope.launch {
            _isLoadingCommentsByPostId.update { currentMap ->
                currentMap.toMutableMap().apply { put(postId, true) }
            }

            try {
                val commentsForPost = commentService.getCommentsForPost(postId)
                commentsCache[postId] = commentsForPost
                _commentsByPostId.update { currentMap ->
                    currentMap.toMutableMap().apply { put(postId, commentsForPost) }
                }
            } finally {
                _isLoadingCommentsByPostId.update { currentMap ->
                    currentMap.toMutableMap().apply { put(postId, false) }
                }
            }
        }
    }

    private fun getCommentAndReplyCount(comment: CommentModel): Int {
        return 1 + comment.replies.sumOf { getCommentAndReplyCount(it) }
    }

    fun getLastCommentForPost(postId: String): CommentModel? {
        val comments = _commentsByPostId.value[postId] ?: return null
        return comments.maxByOrNull { it.timestamp }
    }

    fun addComment(postId: String, userId: String, username: String) {
        viewModelScope.launch {
            val newComment = CommentModel(
                commentId = "",
                userId = userId,
                username = username,
                content = _commentText.value,
                timestamp = getCurrentTimestamp(),
                replies = emptyList()
            )
            val result = commentService.addCommentToPost(postId, newComment)
            if (result.isSuccess) {
                commentsCache.remove(postId)
                loadComments(postId)
                _commentText.value = ""
            }
        }
    }

    fun addReply(
        postId: String,
        parentCommentId: String,
        replyContent: String,
        userId: String,
        username: String
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val reply = CommentModel(
                commentId = "",
                postId = postId,
                parentCommentId = parentCommentId,
                userId = userId,
                username = username,
                content = replyContent,
                timestamp = getCurrentTimestamp(),
                replies = emptyList()
            )
            val result = commentService.addReplyToComment(postId, parentCommentId, reply)
            if (result.isSuccess) {
                commentsCache.remove(postId)
                loadComments(postId)
            }
        }
    }
}
