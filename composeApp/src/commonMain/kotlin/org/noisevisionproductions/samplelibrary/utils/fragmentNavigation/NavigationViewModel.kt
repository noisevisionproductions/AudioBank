package org.noisevisionproductions.samplelibrary.utils.fragmentNavigation

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.noisevisionproductions.samplelibrary.composeUI.screens.FragmentsTabs

class NavigationViewModel : ViewModel() {
    private val _navigationEvent = MutableStateFlow<String?>(null)
    val navigationEvent = _navigationEvent.asStateFlow()

    private val _selectedTab = MutableStateFlow(FragmentsTabs.Tab1)
    val selectedTab = _selectedTab.asStateFlow()

    fun navigateToPost(postId: String) {
        _selectedTab.value = FragmentsTabs.Tab3
        _navigationEvent.value = postId
    }

    fun updateSelectedTab(tab: FragmentsTabs) {
        _selectedTab.value = tab
    }

    fun navigationHandled() {
        _navigationEvent.value = null
    }
}