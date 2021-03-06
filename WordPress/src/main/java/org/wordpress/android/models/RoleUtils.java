package org.sitebay.android.models;

import android.content.Context;

import org.sitebay.android.R;
import org.sitebay.android.fluxc.model.RoleModel;
import org.sitebay.android.fluxc.model.SiteModel;
import org.sitebay.android.fluxc.store.SiteStore;
import org.sitebay.android.util.StringUtils;

import java.util.List;

public class RoleUtils {
    public static String getDisplayName(String userRole, List<RoleModel> siteUserRoles) {
        if (siteUserRoles != null) {
            for (RoleModel roleModel : siteUserRoles) {
                if (roleModel.getName().equalsIgnoreCase(userRole)) {
                    return roleModel.getDisplayName();
                }
            }
        }
        return StringUtils.capitalize(userRole);
    }

    public static List<RoleModel> getInviteRoles(SiteStore siteStore, SiteModel siteModel, Context context) {
        // Setup invite roles
        List<RoleModel> inviteRoles = siteStore.getUserRoles(siteModel);
        // The API doesn't return the follower/viewer role, so we need to manually add it for invites
        RoleModel viewerOrFollowerRole = new RoleModel();
        // the remote expects "follower" as the role parameter even if the role is "viewer"
        viewerOrFollowerRole.setName("follower");
        int displayNameRes = siteModel.isPrivate() ? R.string.role_viewer : R.string.role_follower;
        viewerOrFollowerRole.setDisplayName(context.getString(displayNameRes));
        inviteRoles.add(viewerOrFollowerRole);
        return inviteRoles;
    }
}
