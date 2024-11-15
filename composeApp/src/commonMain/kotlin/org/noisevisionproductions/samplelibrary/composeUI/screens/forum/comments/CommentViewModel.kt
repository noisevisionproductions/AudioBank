package org.noisevisionproductions.samplelibrary.composeUI.screens.forum.comments

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.noisevisionproductions.samplelibrary.auth.AuthService
import org.noisevisionproductions.samplelibrary.database.UserRepository
import org.noisevisionproductions.samplelibrary.composeUI.screens.forum.likes.LikeManager
import org.noisevisionproductions.samplelibrary.database.LikeRepository
import org.noisevisionproductions.samplelibrary.database.CommentRepository
import org.noisevisionproductions.samplelibrary.errors.AppError
import org.noisevisionproductions.samplelibrary.errors.ErrorHandler
import org.noisevisionproductions.samplelibrary.errors.UserErrorAction
import org.noisevisionproductions.samplelibrary.errors.UserErrorInfo
import org.noisevisionproductions.samplelibrary.errors.handleGivenErrors.SharedErrorViewModel
import org.noisevisionproductions.samplelibrary.interfaces.getCurrentTimestamp
import org.noisevisionproductions.samplelibrary.utils.dataClasses.CommentState
import org.noisevisionproductions.samplelibrary.utils.models.CommentModel

class CommentViewModel(
    private val commentRepository: CommentRepository,
    private val authService: AuthService,
    private val likeManager: LikeManager,
    private val likeRepository: LikeRepository,
    private val userRepository: UserRepository,
    private val sharedErrorViewModel: SharedErrorViewModel,
    private val errorHandler: ErrorHandler
) : ViewModel() {
    private val _commentText = MutableStateFlow("")
    val commentText: StateFlow<String> = _commentText.asStateFlow()

    private val _commentsByPostId = MutableStateFlow<Map<String, List<CommentModel>>>(emptyMap())
    private val _isLoadingCommentsByPostId = MutableStateFlow<Map<String, Boolean>>(emptyMap())

    private val _isCommentFieldVisible = MutableStateFlow(false)
    val isCommentFieldVisible: StateFlow<Boolean> = _isCommentFieldVisible.asStateFlow()

    private val _replyFieldVisibilityMap = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    val replyFieldVisibilityMap: StateFlow<Map<String, Boolean>> =
        _replyFieldVisibilityMap.asStateFlow()

    private val _replyTextsMap = MutableStateFlow<Map<String, String>>(emptyMap())
    val replyTextsMap: StateFlow<Map<String, String>> = _replyTextsMap.asStateFlow()

    private val _isSendingReply = MutableStateFlow(false)
    val isSendingReply: StateFlow<Boolean> = _isSendingReply.asStateFlow()

    private val commentsCache = mutableMapOf<String, List<CommentModel>>()

    fun loadComments(postId: String) {
        viewModelScope.launch {
            try {
                val commentsForPost = commentRepository.getCommentsForPost(postId)

                commentsForPost.forEach { comment ->
                    initializeLikeStateForCommentAndReplies(postId, comment)
                }

                _commentsByPostId.update { currentMap ->
                    currentMap.toMutableMap().apply { put(postId, commentsForPost) }
                }
            } catch (e: Exception) {
                UserErrorInfo(
                    message = "Nie udało się załadować komentarzy\n${e.message}",
                    actionType = UserErrorAction.RETRY,
                    errorId = "UPDATE_SOUND_METADATA_ERROR",
                    retryAction = { loadComments(postId) }
                )
                println(e)
            }
        }
    }

    private suspend fun initializeLikeStateForCommentAndReplies(
        postId: String,
        comment: CommentModel
    ) {
        val isLikedResult = likeRepository.isCommentLiked(comment.commentId)
        isLikedResult.fold(
            onSuccess = { isLiked ->
                likeManager.initializeCommentLikeState(
                    postId,
                    comment.commentId,
                    isLiked,
                    comment.likesCount
                )
            },
            onFailure = { exception ->
                println(exception)
                likeManager.initializeCommentLikeState(
                    postId,
                    comment.commentId,
                    isLiked = false,
                    comment.likesCount
                )
            }
        )

        comment.replies.forEach { reply ->
            initializeLikeStateForCommentAndReplies(postId, reply)
        }
    }

    fun toggleCommentFieldVisibility() {
        _isCommentFieldVisible.value = !_isCommentFieldVisible.value
    }

    fun toggleReplyFieldVisibility(commentId: String) {
        _replyFieldVisibilityMap.update { currentMap ->
            val currentVisibility = currentMap[commentId] ?: false
            currentMap.toMutableMap().apply {
                put(commentId, !currentVisibility)
            }
        }
    }

    fun toggleCommentLike(postId: String, commentId: String) {
        viewModelScope.launch {
            val currentState = likeManager.getCommentLikeState(postId, commentId)
            val newLikeState = !(currentState.isLiked)
            val newLikesCount = (currentState.likesCount) + (if (newLikeState) 1 else -1)

            likeManager.updateCommentLike(
                postId,
                commentId,
                LikeManager.LikeState(newLikeState, newLikesCount.coerceAtLeast(0))
            )

            try {
                val result = likeRepository.toggleLikeComment(commentId)
                if (result.isFailure) {
                    likeManager.updateCommentLike(
                        postId,
                        commentId,
                        LikeManager.LikeState(
                            currentState.isLiked,
                            currentState.likesCount
                        )
                    )
                }
            } catch (e: Exception) {
                likeManager.updateCommentLike(
                    postId,
                    commentId,
                    LikeManager.LikeState(
                        currentState.isLiked,
                        currentState.likesCount
                    )
                )
                UserErrorInfo(
                    message = "Nie udało się zmienić polubienia\n${e.message}",
                    actionType = UserErrorAction.RETRY,
                    errorId = "TOGGLE_COMMENT_LIKE_ERROR",
                    retryAction = { toggleCommentLike(postId, commentId) }
                )
                println(e)
            }
        }
    }

    fun isLoadingCommentsForPost(postId: String): StateFlow<Boolean> {
        return _isLoadingCommentsByPostId.map { loadingMap ->
            loadingMap[postId] ?: false
        }.stateIn(viewModelScope, SharingStarted.Lazily, false)
    }

    fun updateReplyText(commentId: String, newText: String) {
        _replyTextsMap.update { currentMap ->
            currentMap.toMutableMap().apply {
                put(commentId, newText)
            }
        }
    }

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

    private fun getCommentAndReplyCount(comment: CommentModel): Int {
        return 1 + comment.replies.sumOf { getCommentAndReplyCount(it) }
    }

    fun getLastCommentForPost(postId: String): CommentModel? {
        val comments = _commentsByPostId.value[postId] ?: return null
        return comments.maxByOrNull { it.timestamp }
    }

    fun addComment(postId: String) {
        viewModelScope.launch {
            try {
                val userId = authService.getCurrentUserId() ?: "unknown"
                val username = getUsernameForUserId(userId) ?: "unknown"

                val newComment = CommentModel(
                    commentId = "",
                    userId = userId,
                    username = username,
                    content = _commentText.value,
                    timestamp = getCurrentTimestamp(),
                    replies = emptyList()
                )

                commentRepository.addCommentToPost(postId, newComment).fold(
                    onSuccess = {
                        commentsCache.remove(postId)
                        loadComments(postId)
                        _commentText.value = ""
                        _isCommentFieldVisible.value = false
                    },
                    onFailure = { throwable ->
                        val error =
                            throwable as AppError
                        val userErrorInfo = errorHandler.handleUserError(
                            error = error,
                            errorId = "add_comment",
                            retryAction = { addComment(postId) }
                        )
                        sharedErrorViewModel.showError(userErrorInfo)
                    }
                )
            } catch (e: Exception) {
                // Ten catch zostaje tylko dla błędów z getCurrentUserId i getUsernameForUserId
                val error = AppError.UnexpectedError(
                    throwable = e,
                    message = "Błąd podczas przygotowywania komentarza"
                )
                val userErrorInfo = errorHandler.handleUserError(
                    error = error,
                    errorId = "ADD_COMMENT_ERROR",
                    retryAction = { addComment(postId) }
                )
                sharedErrorViewModel.showError(userErrorInfo)
            }
        }
    }

    private suspend fun getUsernameForUserId(userId: String): String? {
        return userRepository.getUsernameById(userId).fold(
            onSuccess = { username -> username },
            onFailure = { error ->
                print(error)
                null
            }
        )
    }

    fun addReply(
        postId: String,
        parentCommentId: String
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val replyContent = _replyTextsMap.value[parentCommentId] ?: ""
            if (replyContent.isNotEmpty()) {
                _isSendingReply.value = true
                try {
                    userRepository.getCurrentUserId().fold(
                        onSuccess = { userId ->
                            userRepository.getUsernameById(userId).fold(
                                onSuccess = { username ->
                                    val reply = CommentModel(
                                        commentId = "",
                                        postId = postId,
                                        parentCommentId = parentCommentId,
                                        userId = userId,
                                        username = username ?: "unknown",
                                        content = replyContent,
                                        timestamp = getCurrentTimestamp(),
                                        replies = emptyList()
                                    )

                                    commentRepository.addReplyToComment(
                                        postId,
                                        parentCommentId,
                                        reply
                                    )
                                        .onSuccess {
                                            _replyTextsMap.update { currentMap ->
                                                currentMap.toMutableMap().apply {
                                                    put(parentCommentId, "")
                                                }
                                            }
                                            _replyFieldVisibilityMap.update { currentMap ->
                                                currentMap.toMutableMap().apply {
                                                    put(parentCommentId, false)
                                                }
                                            }
                                            commentsCache.remove(postId)
                                            loadComments(postId)
                                        }
                                        .onFailure { error ->
                                            UserErrorInfo(
                                                message = "Failed to add reply\n${error.message}",
                                                actionType = UserErrorAction.RETRY,
                                                errorId = "ADD_REPLY_ERROR",
                                                retryAction = { addReply(postId, parentCommentId) }
                                            )
                                            println("Error adding reply: ${error.message}")
                                        }
                                },
                                onFailure = { error ->
                                    UserErrorInfo(
                                        message = "Failed to fetch username\n${error.message}",
                                        actionType = UserErrorAction.RETRY,
                                        errorId = "FETCH_USERNAME_ERROR",
                                        retryAction = { addReply(postId, parentCommentId) }
                                    )
                                    println("Error fetching username: ${error.message}")
                                }
                            )
                        },
                        onFailure = { error ->
                            UserErrorInfo(
                                message = "Failed to fetch current user ID\n${error.message}",
                                actionType = UserErrorAction.RETRY,
                                errorId = "FETCH_USER_ID_ERROR",
                                retryAction = { addReply(postId, parentCommentId) }
                            )
                            println("Error fetching current user ID: ${error.message}")
                        }
                    )
                } catch (e: Exception) {
                    UserErrorInfo(
                        message = "Unexpected error while adding reply\n${e.message}",
                        actionType = UserErrorAction.RETRY,
                        errorId = "ADD_REPLY_UNEXPECTED_ERROR",
                        retryAction = { addReply(postId, parentCommentId) }
                    )
                    println("Unexpected error: ${e.message}")
                } finally {
                    _isSendingReply.value = false
                }
            }
        }
    }
}