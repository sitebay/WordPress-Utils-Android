package org.sitebay.android.e2e;

import android.Manifest.permission;

import androidx.test.rule.ActivityTestRule;
import androidx.test.rule.GrantPermissionRule;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.sitebay.android.e2e.pages.BlockEditorPage;
import org.sitebay.android.e2e.pages.MySitesPage;
import org.sitebay.android.e2e.pages.PostPreviewPage;
import org.sitebay.android.e2e.pages.SiteSettingsPage;
import org.sitebay.android.support.BaseTest;
import org.sitebay.android.ui.WPLaunchActivity;

import static androidx.test.espresso.Espresso.pressBack;
import static org.sitebay.android.support.WPSupportUtils.sleep;

public class BlockEditorTests extends BaseTest {
    @Rule
    public ActivityTestRule<WPLaunchActivity> mActivityTestRule = new ActivityTestRule<>(WPLaunchActivity.class);

    @Rule
    public GrantPermissionRule mRuntimeImageAccessRule = GrantPermissionRule.grant(permission.WRITE_EXTERNAL_STORAGE);

    @Before
    public void setUp() {
        logoutIfNecessary();
        wpLogin();
    }

    @Ignore("until startup times are improved or idling resources are made more reliable")
    @Test
    public void testSwitchToClassicAndPreview() {
        String title = "Hello Espresso!";

        MySitesPage mySitesPage = new MySitesPage().go();
        sleep();

        mySitesPage.clickSettingsItem();

        // Set to Gutenberg. Apparently the site is defaulting to Aztec still.
        new SiteSettingsPage().setEditorToGutenberg();

        // exit the Settings page
        pressBack();

        mySitesPage.clickBlogPostsItem();

        mySitesPage.startNewPost();

        BlockEditorPage blockEditorPage = new BlockEditorPage();
        blockEditorPage.waitForTitleDisplayed();

        blockEditorPage.enterTitle(title);

        blockEditorPage.previewPost();
        sleep();

        new PostPreviewPage();
    }
}
