package org.sitebay.android.models.wrappers

import android.content.Context
import dagger.Reusable
import org.sitebay.android.fluxc.model.RoleModel
import org.sitebay.android.fluxc.model.SiteModel
import org.sitebay.android.fluxc.store.SiteStore
import org.sitebay.android.models.RoleUtils
import javax.inject.Inject

@Reusable
class RoleUtilsWrapper @Inject constructor() {
    fun getInviteRoles(
        siteStore: SiteStore,
        siteModel: SiteModel,
        context: Context
    ): List<RoleModel> = RoleUtils.getInviteRoles(siteStore, siteModel, context)
}
