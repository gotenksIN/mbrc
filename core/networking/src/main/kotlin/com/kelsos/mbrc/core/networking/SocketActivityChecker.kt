package com.kelsos.mbrc.core.networking

import com.kelsos.mbrc.core.common.utilities.coroutines.AppCoroutineDispatchers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference

typealias Listener = () -> Unit

class SocketActivityChecker(dispatchers: AppCoroutineDispatchers) {
  private val timeoutJob = AtomicReference<Job?>(null)
  private var listener: Listener? = null
  private val job = SupervisorJob()
  private val scope = CoroutineScope(job + dispatchers.network)

  private val isRunning = AtomicBoolean(false)
  private val consecutiveTimeouts = AtomicInteger(0)

  fun start() {
    if (!isRunning.compareAndSet(false, true)) {
      Timber.v("Activity checker already running")
      return
    }

    consecutiveTimeouts.set(0)
    Timber.v("Starting activity checker")
    schedule()
  }

  private fun schedule() {
    if (!isRunning.get()) return

    val newJob = scope.launch {
      delay(DELAY_MS)
      if (!isRunning.get()) return@launch

      val count = consecutiveTimeouts.incrementAndGet()
      Timber.v("Ping timeout #$count after %d ms", DELAY_MS)

      val result = runCatching {
        listener?.invoke()
      }

      if (result.isFailure) {
        Timber.e(result.exceptionOrNull(), "calling the onTimeout method failed")
      }

      if (result.isSuccess) {
        consecutiveTimeouts.set(0)
      }
    }
    
    timeoutJob.getAndSet(newJob)?.cancel()
  }

  fun stop() {
    if (!isRunning.compareAndSet(true, false)) {
      Timber.v("Activity checker already stopped")
      return
    }

    Timber.v("Stopping activity checker")
    consecutiveTimeouts.set(0)
    timeoutJob.getAndSet(null)?.cancel()
  }

  fun ping() {
    if (!isRunning.get()) {
      Timber.v("Received ping but activity checker is not running")
      return
    }

    Timber.v("Received ping - resetting timeout")
    consecutiveTimeouts.set(0)
    schedule()
  }

  fun setPingTimeoutListener(listener: Listener?) {
    this.listener = listener
  }

  companion object {
    private const val DELAY_MS = 30_000L // Must be less than socket readTimeoutMs (35s)
    private const val MAX_CONSECUTIVE_TIMEOUTS = 3
  }

  fun getTimeoutCount(): Int = consecutiveTimeouts.get()

  fun isHealthy(): Boolean = isRunning.get() && consecutiveTimeouts.get() < MAX_CONSECUTIVE_TIMEOUTS
}
