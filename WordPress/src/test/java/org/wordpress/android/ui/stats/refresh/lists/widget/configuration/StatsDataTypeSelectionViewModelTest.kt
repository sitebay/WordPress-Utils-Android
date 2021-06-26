package org.sitebay.android.ui.stats.refresh.lists.widget.configuration

import com.nhaarman.mockitokotlin2.whenever
import kotlinx.coroutines.Dispatchers
import org.assertj.core.api.Assertions
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.sitebay.android.BaseUnitTest
import org.sitebay.android.R
import org.sitebay.android.fluxc.store.AccountStore
import org.sitebay.android.ui.prefs.AppPrefsWrapper
import org.sitebay.android.viewmodel.Event

class StatsDataTypeSelectionViewModelTest : BaseUnitTest() {
    @Mock private lateinit var appPrefsWrapper: AppPrefsWrapper
    @Mock private lateinit var accountStore: AccountStore
    private lateinit var viewModel: StatsDataTypeSelectionViewModel
    @Before
    fun setUp() {
        viewModel = StatsDataTypeSelectionViewModel(Dispatchers.Unconfined, accountStore, appPrefsWrapper)
    }

    @Test
    fun `opens dialog when access token present`() {
        whenever(accountStore.hasAccessToken()).thenReturn(true)
        var event: Event<Unit>? = null
        viewModel.dialogOpened.observeForever {
            event = it
        }

        viewModel.openDataTypeDialog()

        Assertions.assertThat(event).isNotNull
    }

    @Test
    fun `shows notification when access token not present`() {
        whenever(accountStore.hasAccessToken()).thenReturn(false)
        var notification: Event<Int>? = null
        viewModel.notification.observeForever {
            notification = it
        }

        viewModel.openDataTypeDialog()

        Assertions.assertThat(notification).isNotNull
        Assertions.assertThat(notification?.getContentIfNotHandled()).isEqualTo(R.string.stats_widget_log_in_message)
    }
}
