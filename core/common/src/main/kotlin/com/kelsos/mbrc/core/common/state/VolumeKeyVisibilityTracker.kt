package com.kelsos.mbrc.core.common.state

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class VolumeKeyVisibilityTracker {
  private val _isInterceptVolumeKeys = MutableStateFlow(false)
  val isInterceptVolumeKeys: StateFlow<Boolean> = _isInterceptVolumeKeys.asStateFlow()

  fun setInterceptVolumeKeys(intercept: Boolean) {
    _isInterceptVolumeKeys.value = intercept
  }
}
