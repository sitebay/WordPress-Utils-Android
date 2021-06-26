package org.sitebay.android.ui.accounts;

import org.sitebay.android.fluxc.model.SiteModel;

public interface JetpackCallbacks {
    boolean isJetpackAuth();

    SiteModel getJetpackSite();
}
