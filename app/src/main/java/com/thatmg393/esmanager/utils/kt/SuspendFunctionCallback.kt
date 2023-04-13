package com.thatmg393.esmanager.utils.kt;

import kotlin.Result;
import kotlin.coroutines.Continuation
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

internal interface SuspendFunctionCallback<Result> {
	companion object {
		@JvmOverloads
		fun <R> call(
			callback: SuspendFunctionCallback<R>,
			dispatcher: CoroutineDispatcher = Dispatchers.Default
		): Continuation<R> {
			return object : Continuation<R> {
				override val context: CoroutineContext get() = dispatcher
					
				override fun resumeWith(result: Result<R>) {
					callback.onComplete(result.getOrNull(), result.exceptionOrNull())
				}
			}
		}
	}	
	fun onComplete(result: Result?, error: Throwable?)
}
