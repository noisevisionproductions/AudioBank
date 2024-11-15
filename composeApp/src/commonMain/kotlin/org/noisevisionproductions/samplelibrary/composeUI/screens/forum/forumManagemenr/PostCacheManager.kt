package org.noisevisionproductions.samplelibrary.composeUI.screens.forum.forumManagemenr

import org.noisevisionproductions.samplelibrary.utils.models.PostModel
import org.noisevisionproductions.samplelibrary.utils.models.PostWithCategory

class PostCacheManager {
    private val postsCache = mutableMapOf<String, PostModel>()
    private val categoryCache = mutableMapOf<String, String>()
    private var postsWithCategories = listOf<PostWithCategory>()

    fun cachePost(post: PostModel) {
        postsCache[post.postId] = post
    }

    fun cachePosts(posts: List<PostWithCategory>) {
        posts.forEach { postWithCategory ->
            postsCache[postWithCategory.post.postId] = postWithCategory.post
        }
        postsWithCategories = postsWithCategories + posts
    }

    fun getCachedPost(postId: String): PostModel? = postsCache[postId]

    fun cacheCategories(categories: Map<String, String>) {
        categoryCache.putAll(categories)
    }

    fun getCachedCategory(categoryId: String): String? = categoryCache[categoryId]

    fun getPostsWithCategories(): List<PostWithCategory> = postsWithCategories

    fun clearPostsCache() {
        postsWithCategories = emptyList()
        postsCache.clear()
    }
}