package org.sitebay.android.ui.people

import android.content.Context
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.anyLong
import org.mockito.Mock
import org.sitebay.android.BaseUnitTest
import org.sitebay.android.fluxc.model.RoleModel
import org.sitebay.android.fluxc.model.SiteModel
import org.sitebay.android.fluxc.store.SiteStore
import org.sitebay.android.models.InvitePeopleUtils
import org.sitebay.android.models.wrappers.RoleUtilsWrapper
import org.sitebay.android.models.wrappers.SimpleDateFormatWrapper
import org.sitebay.android.ui.people.InviteLinksApiCallsProvider.InviteLinksItem
import org.sitebay.android.util.DateTimeUtilsWrapper
import org.sitebay.android.viewmodel.ContextProvider
import java.text.DateFormat
import java.util.Date

class InvitePeopleUtilsTest : BaseUnitTest() {
    @Mock lateinit var siteStore: SiteStore
    @Mock lateinit var siteModel: SiteModel
    @Mock lateinit var contextProvider: ContextProvider
    @Mock lateinit var dateTimeUtilsWrapper: DateTimeUtilsWrapper
    @Mock lateinit var roleUtilsWrapper: RoleUtilsWrapper
    @Mock lateinit var context: Context
    @Mock lateinit var simpleDateFormatWrapper: SimpleDateFormatWrapper
    @Mock lateinit var dateFormat: DateFormat

    private lateinit var invitePeopleUtils: InvitePeopleUtils

    private val roles = listOf(
            RoleModel().apply {
                name = "administrator"
                displayName = "Administrator"
            },
            RoleModel().apply {
                name = "contributor"
                displayName = "Contributor"
            }
    )

    private val linkItems = mutableListOf(
            InviteLinksItem(
                    role = "administrator",
                    expiry = 0,
                    link = "https://sitebay.com/linkdata"
            ),
            InviteLinksItem(
                    role = "follower",
                    expiry = 0,
                    link = "https://sitebay.com/linkdata"
            )
    )

    @Before
    fun setUp() {
        val date: Date = mock()
        whenever(contextProvider.getContext()).thenReturn(context)
        whenever(roleUtilsWrapper.getInviteRoles(siteStore, siteModel, context)).thenReturn(roles)
        whenever(dateTimeUtilsWrapper.dateFromTimestamp(anyLong())).thenReturn(date)
        whenever(dateFormat.format(eq(date))).thenReturn("")
        whenever(simpleDateFormatWrapper.getDateInstance()).thenReturn(dateFormat)

        invitePeopleUtils = InvitePeopleUtils(
                siteStore,
                contextProvider,
                dateTimeUtilsWrapper,
                roleUtilsWrapper,
                simpleDateFormatWrapper
        )
    }

    @Test
    fun `link data found by available display name`() {
        val item = invitePeopleUtils.getInviteLinkDataFromRoleDisplayName(linkItems, siteModel, "Administrator")

        requireNotNull(item).let {
            assertThat(it.role).isEqualTo("administrator")
        }
    }

    @Test
    fun `link data not found by missing display name`() {
        val item = invitePeopleUtils.getInviteLinkDataFromRoleDisplayName(linkItems, siteModel, "Follower")

        assertThat(item).isNull()
    }

    @Test
    fun `display name found for available role`() {
        val displayName = invitePeopleUtils.getDisplayNameForRole(siteModel, "administrator")

        assertThat(displayName).isEqualTo("Administrator")
    }

    @Test
    fun `display name not found for missing role`() {
        val displayName = invitePeopleUtils.getDisplayNameForRole(siteModel, "follower")

        assertThat(displayName).isEmpty()
    }

    @Test
    fun `ui items list created as expected`() {
        val uiItemsList = invitePeopleUtils.getMappedLinksUiItems(linkItems, siteModel)

        assertThat(uiItemsList.count()).isEqualTo(1)
        assertThat(uiItemsList.get(0).roleName).isEqualTo("administrator")
    }

    @Test
    fun `role display names created as expected`() {
        val displayNamesList = invitePeopleUtils.getInviteLinksRoleDisplayNames(linkItems, siteModel)

        assertThat(displayNamesList.count()).isEqualTo(1)
        assertThat(displayNamesList.get(0)).isEqualTo("Administrator")
    }
}
