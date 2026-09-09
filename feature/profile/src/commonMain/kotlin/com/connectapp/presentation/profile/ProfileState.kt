package com.connectapp.presentation.profile

import com.connectapp.domain.model.User

data class ProfileState(
    val isLoading: Boolean = false,
    val user: User? = null,
    val lastFetchedAt: Long? = null,
) {
    /**
     * FR-006 (specs/010-offline-cache-layer): non-blocking signal that [user] came from the
     * offline cache seeded at the last successful login, not a live server response — there is
     * no network "refresh profile" call to fail here (research.md D6), so this is only ever true
     * while offline/before any login in this app install.
     */
    val isShowingCachedData: Boolean
        get() = user != null && lastFetchedAt != null
}
