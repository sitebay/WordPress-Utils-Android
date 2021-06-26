package org.sitebay.android.models

import dagger.Reusable
import org.sitebay.android.fluxc.model.SiteModel
import org.sitebay.android.fluxc.store.SiteStore
import org.sitebay.android.models.wrappers.RoleUtilsWrapper
import org.sitebay.android.models.wrappers.SimpleDateFormatWrapper
import org.sitebay.android.ui.people.InviteLinksApiCallsProvider.InviteLinksItem
import org.sitebay.android.ui.people.InviteLinksUiItem
import org.sitebay.android.util.AppLog
import org.sitebay.android.util.AppLog.T
import org.sitebay.android.util.DateTimeUtilsWrapper
import org.sitebay.android.viewmodel.ContextProvider
import javax.inject.Inject

@Reusable
class InvitePeopleUtils @Inject constructor(
    private val siteStore: SiteStore,
    private val contextProvider: ContextProvider,
    private val dateTimeUtilsWrapper: DateTimeUtilsWrapper,
    private val roleUtilsWrapper: RoleUtilsWrapper,
    private val simpleDateFormatWrapper: SimpleDateFormatWrapper
) {
    fun getInviteLinkDataFromRoleDisplayName(
        inviteLinksData: MutableList<InviteLinksItem>,
        siteModel: SiteModel,
        roleDisplayName: String
    ): InviteLinksItem? {
        val roles = getMappedLinksUiItems(inviteLinksData, siteModel)

        return inviteLinksData.firstOrNull { linksItem ->
            roles.firstOrNull { linksUiItem ->
                linksItem.role.equals(linksUiItem.roleName, ignoreCase = true) &&
                        linksUiItem.roleDisplayName.equals(roleDisplayName, ignoreCase = true)
            } != null
        }
    }

    fun getDisplayNameForRole(
        siteModel: SiteModel,
        roleName: String
    ): String {
        val roles = roleUtilsWrapper.getInviteRoles(siteStore, siteModel, contextProvider.getContext())

        return roles.firstOrNull { roleModel ->
            roleModel.name.equals(roleName, ignoreCase = true)
        }?.displayName ?: ""
    }

    fun getMappedLinksUiItems(
        inviteLinksData: MutableList<InviteLinksItem>,
        siteModel: SiteModel
    ): List<InviteLinksUiItem> {
        val formatter = simpleDateFormatWrapper.getDateInstance()
        val roles = roleUtilsWrapper.getInviteRoles(siteStore, siteModel, contextProvider.getContext())

        AppLog.d(T.PEOPLE, "getMappedLinksUiItems > ${siteModel.siteId}")
        AppLog.d(
                T.PEOPLE,
                "getMappedLinksUiItems > roles: ${roles.map { "DisplayName: ${it.displayName} Name: ${it.name}" }}"
        )
        AppLog.d(
                T.PEOPLE,
                "getMappedLinksUiItems > " +
                        "inviteLinksData: ${inviteLinksData.map { "DisplayName: ${it.role} Expiry: ${it.expiry}" }}"
        )

        return roles.let {
            it.filter { role ->
                inviteLinksData.firstOrNull { linksItem ->
                    role.name.equals(linksItem.role, ignoreCase = true)
                } != null
            }.map { role ->
                val linksData = inviteLinksData.first { linksItem ->
                    role.name.equals(linksItem.role, ignoreCase = true)
                }

                InviteLinksUiItem(
                        roleName = role.name,
                        roleDisplayName = role.displayName,
                        expiryDate = formatter.format(dateTimeUtilsWrapper.dateFromTimestamp(linksData.expiry))
                )
            }
        } ?: listOf()
    }

    fun getInviteLinksRoleDisplayNames(
        inviteLinksData: MutableList<InviteLinksItem>,
        siteModel: SiteModel
    ): List<String> {
        val mappedRolesItems = getMappedLinksUiItems(inviteLinksData, siteModel)

        return mappedRolesItems.map { linksItem ->
            linksItem.roleDisplayName
        }
    }
}
