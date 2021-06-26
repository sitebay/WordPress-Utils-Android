package org.sitebay.android.e2e;

import androidx.test.rule.ActivityTestRule;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.sitebay.android.e2e.flows.SignupFlow;
import org.sitebay.android.support.BaseTest;
import org.sitebay.android.ui.accounts.LoginMagicLinkInterceptActivity;

import static org.sitebay.android.BuildConfig.E2E_SIGNUP_DISPLAY_NAME;
import static org.sitebay.android.BuildConfig.E2E_SIGNUP_EMAIL;
import static org.sitebay.android.BuildConfig.E2E_SIGNUP_PASSWORD;
import static org.sitebay.android.BuildConfig.E2E_SIGNUP_USERNAME;

public class SignUpTests extends BaseTest {
    @Rule
    public ActivityTestRule<LoginMagicLinkInterceptActivity> mMagicLinkActivityTestRule =
            new ActivityTestRule<>(LoginMagicLinkInterceptActivity.class, true, false);

    @Before
    public void setUp() {
        logoutIfNecessary();
    }

    @Test
    public void signUpWithMagicLink() {
        new SignupFlow().chooseContinueWithWpCom()
                        .enterEmail(E2E_SIGNUP_EMAIL)
                        .openMagicLink(mMagicLinkActivityTestRule)
                        .checkEpilogue(
                                E2E_SIGNUP_DISPLAY_NAME,
                                E2E_SIGNUP_USERNAME)
                        .enterPassword(E2E_SIGNUP_PASSWORD)
                        .dismissInterstitial()
                        .confirmSignup();
    }
}
