package org.sitebay.android.networking;

import org.sitebay.android.fluxc.network.rest.wpcom.auth.AccessToken;
import org.sitebay.android.util.StringUtils;

// TODO: kill this when we don't need any other rest client than the one in FluxC
public class OAuthAuthenticator implements Authenticator {
    private AccessToken mAccessToken;

    public OAuthAuthenticator(AccessToken accessToken) {
        mAccessToken = accessToken;
    }

    @Override
    public void authenticate(final AuthenticatorRequest request) {
        request.sendWithAccessToken(StringUtils.notNullStr(mAccessToken.get()));
    }
}
