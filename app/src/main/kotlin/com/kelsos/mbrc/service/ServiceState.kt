package com.kelsos.mbrc.service

import java.util.concurrent.atomic.AtomicBoolean

/**
 * Singleton that tracks the foreground service state.
 */
object ServiceState {
  private val _isRunning = AtomicBoolean(false)
  private val _isStopping = AtomicBoolean(false)

  val isRunning: Boolean
    get() = _isRunning.get()

  val isStopping: Boolean
    get() = _isStopping.get()

  fun setRunning(running: Boolean) {
    _isRunning.set(running)
  }

  fun setStopping(stopping: Boolean) {
    _isStopping.set(stopping)
  }
}
