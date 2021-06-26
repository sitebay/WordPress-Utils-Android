package org.sitebay.android.ui.suggestion

import android.view.View
import androidx.lifecycle.LiveData
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import junit.framework.Assert.assertEquals
import junit.framework.Assert.assertFalse
import junit.framework.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.ArgumentMatchers.anyString
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.sitebay.android.R
import org.sitebay.android.analytics.AnalyticsTracker
import org.sitebay.android.fluxc.model.SiteModel
import org.sitebay.android.networking.ConnectionChangeReceiver.ConnectionChangeEvent
import org.sitebay.android.ui.suggestion.FinishAttempt.NotExactlyOneAvailable
import org.sitebay.android.ui.suggestion.FinishAttempt.OnlyOneAvailable
import org.sitebay.android.ui.suggestion.SuggestionType.Users
import org.sitebay.android.ui.suggestion.SuggestionType.XPosts
import org.sitebay.android.util.NetworkUtilsWrapper
import org.sitebay.android.util.analytics.AnalyticsTrackerWrapper
import org.sitebay.android.viewmodel.ResourceProvider

@RunWith(MockitoJUnitRunner::class)
class SuggestionViewModelTest {
    @Mock lateinit var mockSuggestionSourceProvider: SuggestionSourceProvider
    @Mock lateinit var mockResourceProvider: ResourceProvider
    @Mock lateinit var mockNetworkUtils: NetworkUtilsWrapper
    @Mock lateinit var mockAnalyticsTracker: AnalyticsTrackerWrapper
    @Mock lateinit var mockSite: SiteModel
    @Mock lateinit var mockLiveData: LiveData<SuggestionResult>
    @Mock lateinit var mockSuggestionSource: SuggestionSource

    @InjectMocks lateinit var viewModel: SuggestionViewModel

    private val xpostSuggestionTypeString = "xpost_suggestion_type_string"
    private val userSuggestionTypeString = "user_suggestion_type_string"

    @Before
    fun setUp() {
        setSuggestionsSupported(true)

        whenever(mockResourceProvider.getString(R.string.suggestion_xpost)).thenReturn(xpostSuggestionTypeString)
        whenever(mockResourceProvider.getString(R.string.suggestion_user)).thenReturn(userSuggestionTypeString)

        whenever(mockSuggestionSource.suggestionData).thenReturn(mockLiveData)
    }

    @Test
    fun `init when suggestions not supported`() {
        setSuggestionsSupported(false)
        val anySuggestionType = XPosts
        assertFalse(viewModel.init(anySuggestionType, mockSite))
    }

    @Test
    fun `init with xpost suggestions`() {
        assertTrue(initViewModel(XPosts))
        verifyViewModelSuggestionType(XPosts)
    }

    @Test
    fun `init with user suggestions`() {
        assertTrue(initViewModel(Users))
        verifyViewModelSuggestionType(Users)
    }

    @Test
    fun `onConnectionChanged not connected`() {
        initViewModel()
        viewModel.onConnectionChanged(ConnectionChangeEvent(false))
        verify(mockSuggestionSource, never()).refreshSuggestions()
    }

    @Test
    fun `onConnectionChanged connected`() {
        initViewModel()
        viewModel.onConnectionChanged(ConnectionChangeEvent(true))
        verify(mockSuggestionSource).refreshSuggestions()
    }

    @Test
    fun `getEmptyViewState visibility gone if displaying any suggestions`() {
        initViewModel()
        stubEmptyViewStateText()

        val nonEmptyList = listOf(mock<Suggestion>())
        val actual = viewModel.getEmptyViewState(nonEmptyList)
        assertEquals(View.GONE, actual.visibility)
    }

    @Test
    fun `getEmptyViewState visibility visible if not displaying any suggestions`() {
        initViewModel()
        stubEmptyViewStateText()

        val actual = viewModel.getEmptyViewState(emptyList())
        assertEquals(View.VISIBLE, actual.visibility)
    }

    private fun stubEmptyViewStateText() {
        whenever(mockNetworkUtils.isNetworkAvailable()).thenReturn(true)
        whenever(mockResourceProvider.getString(anyInt(), anyString())).thenReturn("")
    }

    @Test
    fun `getEmptyViewState text no matching suggestions if suggestions available`() {
        initViewModel()
        val nonEmptyList = listOf(mock<Suggestion>())
        whenever(mockLiveData.value).thenReturn(SuggestionResult(nonEmptyList, false))
        val expectedText = "expected_text"
        whenever(mockResourceProvider.getString(R.string.suggestion_no_matching, viewModel.suggestionTypeString))
                .thenReturn(expectedText)

        val actual = viewModel.getEmptyViewState(emptyList())
        assertEquals(expectedText, actual.string)
    }

    @Test
    fun `getEmptyViewState problem text if has network, suggestions list empty, and fetch error`() {
        initViewModel()
        whenever(mockNetworkUtils.isNetworkAvailable()).thenReturn(true)
        whenever(mockLiveData.value).thenReturn(SuggestionResult(emptyList(), true))
        val expectedText = "expected_text"
        whenever(mockResourceProvider.getString(R.string.suggestion_problem)).thenReturn(expectedText)

        val actual = viewModel.getEmptyViewState(emptyList())
        assertEquals(expectedText, actual.string)
    }

    @Test
    fun `getEmptyViewState text no suggestions of type if has network, suggestions list empty, and NO fetch error`() {
        initViewModel()
        whenever(mockNetworkUtils.isNetworkAvailable()).thenReturn(true)
        whenever(mockLiveData.value).thenReturn(SuggestionResult(emptyList(), false))
        val expectedText = "expected_text"
        whenever(mockResourceProvider.getString(R.string.suggestion_none, viewModel.suggestionTypeString))
                .thenReturn(expectedText)

        val actual = viewModel.getEmptyViewState(emptyList())
        assertEquals(expectedText, actual.string)
    }

    @Test
    fun `getEmptyViewState text loading if has network and suggestions have never been received`() {
        initViewModel()
        whenever(mockNetworkUtils.isNetworkAvailable()).thenReturn(true)
        whenever(mockSuggestionSource.isFetchInProgress()).thenReturn(true)
        val expectedText = "expected_text"
        whenever(mockResourceProvider.getString(R.string.loading))
                .thenReturn(expectedText)

        val actual = viewModel.getEmptyViewState(emptyList())
        assertEquals(expectedText, actual.string)
    }

    @Test
    fun `getEmptyViewState text no internet if no suggestions available and network unavailable`() {
        initViewModel()
        whenever(mockLiveData.value).thenReturn(SuggestionResult(emptyList(), false))
        whenever(mockNetworkUtils.isNetworkAvailable()).thenReturn(false)
        val expectedText = "expected_text"
        whenever(mockResourceProvider.getString(R.string.suggestion_no_connection))
                .thenReturn(expectedText)

        val actual = viewModel.getEmptyViewState(emptyList())
        assertEquals(expectedText, actual.string)
    }

    @Test
    fun `onAttemptToFinish no displayed suggestions`() {
        initViewModel()
        val userInput = "user_input"
        val expectedMesage = "expected_message"
        whenever(
                mockResourceProvider.getString(
                        R.string.suggestion_invalid,
                        userInput, viewModel.suggestionTypeString
                )
        )
                .thenReturn(expectedMesage)

        val actual = viewModel.onAttemptToFinish(emptyList(), userInput)

        val expected = NotExactlyOneAvailable(expectedMesage)
        assertEquals(expected, actual)
    }

    @Test
    fun `onAttemptToFinish no filter text from user`() {
        initViewModel(XPosts)
        val emptyUserInput = "+"
        val expectedMesage = "expected_message"
        whenever(mockResourceProvider.getString(R.string.suggestion_selection_needed))
                .thenReturn(expectedMesage)

        val listWithMoreThanOne = listOf<Suggestion>(mock(), mock())
        val actual = viewModel.onAttemptToFinish(listWithMoreThanOne, emptyUserInput)

        val expected = NotExactlyOneAvailable(expectedMesage)
        assertEquals(expected, actual)
    }

    @Test
    fun `onAttemptToFinish multiple displayed suggestions`() {
        initViewModel()
        val userInput = "user_input"
        val expectedMesage = "expected_message"
        whenever(
                mockResourceProvider.getString(
                        R.string.suggestion_invalid,
                        userInput, viewModel.suggestionTypeString
                )
        )
                .thenReturn(expectedMesage)

        val listWithMoreThanOne = listOf<Suggestion>(mock(), mock())
        val actual = viewModel.onAttemptToFinish(listWithMoreThanOne, userInput)

        val expected = NotExactlyOneAvailable(expectedMesage)
        assertEquals(expected, actual)
    }

    @Test
    fun `onAttemptToFinish exactly 1 displayed suggestion`() {
        initViewModel()

        val mockSuggestion = Suggestion("", "expected_value", "")
        val listWithExactlyOne = listOf<Suggestion>(mockSuggestion)
        val actual = viewModel.onAttemptToFinish(listWithExactlyOne, "")

        val expected = OnlyOneAvailable(mockSuggestion.value)
        assertEquals(expected, actual)
    }

    @Test
    fun `trackExit xpost suggestion true`() {
        initViewModel(XPosts)

        val withSuggestion = true
        viewModel.trackExit(withSuggestion)

        val props = mapOf(
                "did_select_suggestion" to withSuggestion,
                "suggestion_type" to "xpost"
        )
        verify(mockAnalyticsTracker).track(AnalyticsTracker.Stat.SUGGESTION_SESSION_FINISHED, props)
    }

    @Test
    fun `trackExit xpost suggestion false`() {
        initViewModel(XPosts)

        val withSuggestion = false
        viewModel.trackExit(withSuggestion)

        val props = mapOf(
                "did_select_suggestion" to withSuggestion,
                "suggestion_type" to "xpost"
        )
        verify(mockAnalyticsTracker).track(AnalyticsTracker.Stat.SUGGESTION_SESSION_FINISHED, props)
    }

    @Test
    fun `trackExit user suggestion true`() {
        initViewModel(Users)

        val withSuggestion = true
        viewModel.trackExit(withSuggestion)

        val props = mapOf(
                "did_select_suggestion" to withSuggestion,
                "suggestion_type" to "user"
        )
        verify(mockAnalyticsTracker).track(AnalyticsTracker.Stat.SUGGESTION_SESSION_FINISHED, props)
    }

    @Test
    fun `trackExit user suggestion false`() {
        initViewModel(Users)

        val withSuggestion = false
        viewModel.trackExit(withSuggestion)

        val props = mapOf(
                "did_select_suggestion" to withSuggestion,
                "suggestion_type" to "user"
        )
        verify(mockAnalyticsTracker).track(AnalyticsTracker.Stat.SUGGESTION_SESSION_FINISHED, props)
    }

    private fun initViewModel(type: SuggestionType = XPosts): Boolean {
        whenever(mockSuggestionSourceProvider.get(type, mockSite)).thenReturn(mockSuggestionSource)
        return viewModel.init(type, mockSite)
    }

    private fun verifyViewModelSuggestionType(type: SuggestionType) {
        verifySuggestionPrefix(type)
        verifySuggestionTypeString(type)
    }

    private fun verifySuggestionPrefix(type: SuggestionType) {
        val expectedPrefix = when (type) {
            XPosts -> '+'
            Users -> '@'
        }
        val actualPrefix = viewModel.suggestionPrefix
        assertEquals(expectedPrefix, actualPrefix)
    }

    private fun verifySuggestionTypeString(type: SuggestionType) {
        val expectedTypeString = when (type) {
            XPosts -> xpostSuggestionTypeString
            Users -> userSuggestionTypeString
        }
        val actualTypeString = viewModel.suggestionTypeString
        assertEquals(expectedTypeString, actualTypeString)
    }

    private fun setSuggestionsSupported(areSupported: Boolean) {
        val origin = if (areSupported) {
            SiteModel.ORIGIN_WPCOM_REST
        } else {
            SiteModel.ORIGIN_UNKNOWN
        }
        whenever(mockSite.origin).thenReturn(origin)
    }
}
