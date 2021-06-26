package org.sitebay.android.ui.stats.refresh.lists.widget.configuration

import com.nhaarman.mockitokotlin2.whenever
import kotlinx.coroutines.Dispatchers
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.sitebay.android.BaseUnitTest
import org.sitebay.android.R
import org.sitebay.android.fluxc.store.AccountStore
import org.sitebay.android.ui.prefs.AppPrefsWrapper
import org.sitebay.android.ui.stats.refresh.lists.widget.configuration.StatsColorSelectionViewModel.Color
import org.sitebay.android.ui.stats.refresh.lists.widget.configuration.StatsColorSelectionViewModel.Color.DARK
import org.sitebay.android.ui.stats.refresh.lists.widget.configuration.StatsColorSelectionViewModel.Color.LIGHT
import org.sitebay.android.viewmodel.Event

class StatsColorSelectionViewModelTest : BaseUnitTest() {
    @Mock private lateinit var appPrefsWrapper: AppPrefsWrapper
    @Mock private lateinit var accountStore: AccountStore
    private lateinit var viewModel: StatsColorSelectionViewModel
    @Before
    fun setUp() {
        viewModel = StatsColorSelectionViewModel(Dispatchers.Unconfined, accountStore, appPrefsWrapper)
    }

    @Test
    fun `updated model on view mode click`() {
        var viewMode: Color? = null
        viewModel.viewMode.observeForever {
            viewMode = it
        }

        viewModel.selectColor(DARK)

        Assertions.assertThat(viewMode).isEqualTo(DARK)

        viewModel.selectColor(LIGHT)

        Assertions.assertThat(viewMode).isEqualTo(LIGHT)
    }

    @Test
    fun `opens dialog when access token present`() {
        whenever(accountStore.hasAccessToken()).thenReturn(true)
        var event: Event<Unit>? = null
        viewModel.dialogOpened.observeForever {
            event = it
        }

        viewModel.openColorDialog()

        assertThat(event).isNotNull
    }

    @Test
    fun `shows notification when access token not present`() {
        whenever(accountStore.hasAccessToken()).thenReturn(false)
        var notification: Event<Int>? = null
        viewModel.notification.observeForever {
            notification = it
        }

        viewModel.openColorDialog()

        assertThat(notification).isNotNull
        assertThat(notification?.getContentIfNotHandled()).isEqualTo(R.string.stats_widget_log_in_message)
    }
}
