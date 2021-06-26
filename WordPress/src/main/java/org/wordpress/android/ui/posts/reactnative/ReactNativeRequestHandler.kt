package org.sitebay.android.ui.posts.reactnative

import android.os.Bundle
import androidx.core.util.Consumer
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.sitebay.android.fluxc.model.SiteModel
import org.sitebay.android.fluxc.network.BaseRequest
import org.sitebay.android.fluxc.network.rest.wpcom.WPComGsonRequest
import org.sitebay.android.fluxc.store.ReactNativeFetchResponse
import org.sitebay.android.fluxc.store.ReactNativeFetchResponse.Error
import org.sitebay.android.fluxc.store.ReactNativeFetchResponse.Success
import org.sitebay.android.fluxc.store.ReactNativeStore
import org.sitebay.android.modules.BG_THREAD
import javax.inject.Inject
import javax.inject.Named

class ReactNativeRequestHandler @Inject constructor(
    private val reactNativeStore: ReactNativeStore,
    @Named(BG_THREAD) private val bgDispatcher: CoroutineDispatcher
) : CoroutineScope {
    override val coroutineContext = bgDispatcher + Job()

    fun performGetRequest(
        pathWithParams: String,
        mSite: SiteModel,
        enableCaching: Boolean,
        onSuccess: Consumer<String>,
        onError: Consumer<Bundle>
    ) {
        launch {
            val response = reactNativeStore.executeRequest(mSite, pathWithParams, enableCaching)
            handleResponse(response, onSuccess::accept, onError::accept)
        }
    }

    /**
     * A given instance of this class may not be used after [destroy] is called because:
     *     (1) this class's coroutineContext has a single job instance that is created on initialization;
     *     (2) calling `destroy()` cancels that job; and
     *     (3) jobs cannot be reused once cancelled.
     */
    fun destroy() {
        coroutineContext[Job]!!.cancel()
    }

    private fun handleResponse(
        response: ReactNativeFetchResponse,
        onSuccess: (String) -> Unit,
        onError: (Bundle) -> Unit
    ) {
        when (response) {
            is Success -> onSuccess(response.result.toString())
            is Error -> {
                val bundle = Bundle().apply {
                    response.error.volleyError?.networkResponse?.statusCode?.let {
                        putInt("code", it)
                    }
                    extractErrorMessage(response.error)?.let {
                        putString("message", it)
                    }
                }
                onError(bundle)
            }
        }
    }

    private fun extractErrorMessage(networkError: BaseRequest.BaseNetworkError): String? {
        val volleyError = networkError.volleyError?.message
        val wpComError = (networkError as? WPComGsonRequest.WPComGsonNetworkError)?.apiError
        val baseError = networkError.message
        val errorType = networkError.type?.toString()
        return when {
            volleyError?.isNotBlank() == true -> volleyError
            wpComError?.isNotBlank() == true -> wpComError
            baseError?.isNotBlank() == true -> baseError
            errorType?.isNotBlank() == true -> errorType
            else -> "Unknown ${networkError.javaClass.simpleName} Error"
        }
    }
}
