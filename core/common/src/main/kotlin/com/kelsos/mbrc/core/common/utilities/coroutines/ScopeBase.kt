package com.kelsos.mbrc.core.common.utilities.coroutines

import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob

open class ScopeBase(private val dispatcher: CoroutineDispatcher) : CoroutineScope {
  private val lock = Any()
  private var supervisorJob: Job? = null
  private val job: Job
    get() = synchronized(lock) {
      supervisorJob ?: SupervisorJob().also { supervisorJob = it }
    }

  override val coroutineContext: CoroutineContext
    get() = job + dispatcher

  fun onStart() {
    synchronized(lock) {
      if (supervisorJob?.isCancelled == true) {
        supervisorJob = SupervisorJob()
      }
    }
  }

  fun onStop() {
    job.cancel()
  }
}
