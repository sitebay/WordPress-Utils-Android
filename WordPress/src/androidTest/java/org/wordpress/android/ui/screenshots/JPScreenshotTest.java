package org.sitebay.android.ui.screenshots;

import android.provider.Settings;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.rule.ActivityTestRule;

import com.google.android.libraries.cloudtesting.screenshots.ScreenShotter;

import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.sitebay.android.BuildConfig;
import org.sitebay.android.R;
import org.sitebay.android.e2e.pages.MySitesPage;
import org.sitebay.android.e2e.pages.SitePickerPage;
import org.sitebay.android.support.BaseTest;
import org.sitebay.android.support.DemoModeEnabler;
import org.sitebay.android.ui.WPLaunchActivity;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.sitebay.android.support.WPSupportUtils.clickOn;
import static org.sitebay.android.support.WPSupportUtils.getCurrentActivity;
import static org.sitebay.android.support.WPSupportUtils.idleFor;
import static org.sitebay.android.support.WPSupportUtils.isElementDisplayed;
import static org.sitebay.android.support.WPSupportUtils.pressBackUntilElementIsDisplayed;
import static org.sitebay.android.support.WPSupportUtils.setNightMode;
import static org.sitebay.android.support.WPSupportUtils.swipeLeftOnViewPager;
import static org.sitebay.android.support.WPSupportUtils.swipeRightOnViewPager;
import static org.sitebay.android.support.WPSupportUtils.waitForElementToBeDisplayedWithoutFailure;

import tools.fastlane.screengrab.Screengrab;
import tools.fastlane.screengrab.UiAutomatorScreenshotStrategy;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class JPScreenshotTest extends BaseTest {
    @ClassRule
    public static final WPLocaleTestRule LOCALE_TEST_RULE = new WPLocaleTestRule();

    @Rule
    public ActivityTestRule<WPLaunchActivity> mActivityTestRule = new ActivityTestRule<>(WPLaunchActivity.class,
            false, false);

    private DemoModeEnabler mDemoModeEnabler = new DemoModeEnabler();

    @Test
    public void jPScreenshotTest() {
        if (BuildConfig.IS_JETPACK_APP) {
            mActivityTestRule.launchActivity(null);
            Screengrab.setDefaultScreenshotStrategy(new UiAutomatorScreenshotStrategy());

            // Enable Demo Mode
            mDemoModeEnabler.enable();
            wpLogin();

            navigateMySite();
            navigateActivityLog();
            navigateScan();
            navigateBackupDownload();
            navigateStats();

            // Turn Demo Mode off on the emulator when we're done
            mDemoModeEnabler.disable();
            logoutIfNecessary();
        }
    }

    public void navigateMySite() {
        // Click on the "Sites" tab and take a screenshot
        clickOn(R.id.nav_sites);

        // Choose "Switch Site"
        clickOn(R.id.switch_site);

        (new SitePickerPage()).chooseSiteWithURL("yourjetpack.blog");
        waitForElementToBeDisplayedWithoutFailure(R.id.row_blog_posts);

        setNightModeAndWait(false);
        takeScreenshot("1-bring-your-jetpack-with-you");
    }

    private void navigateActivityLog() {
        moveToActivityLog();

        setNightModeAndWait(false);
        takeScreenshot("2-keep-tabs-on-your-site-activity");

        // Exit the Activity Log Activity
        pressBackUntilElementIsDisplayed(R.id.nav_sites);
    }

    private void navigateScan() {
        moveToScan();

        setNightModeAndWait(false);
        takeScreenshot("3-scan-for-issues-on-the-go");

        // Exit the Activity scan activity
        pressBackUntilElementIsDisplayed(R.id.nav_sites);
    }

    private void navigateBackupDownload() {
        moveToBackup();

        onView(withId(R.id.log_list_view))
                .check(matches(hasDescendant(withText(containsString("plugins"))))).perform(click());

        waitForElementToBeDisplayedWithoutFailure(R.id.activityDownloadBackupButton);

        if (isElementDisplayed(R.id.activityDownloadBackupButton)) {
            clickOn(R.id.activityDownloadBackupButton);
        }

        setNightModeAndWait(false);
        takeScreenshot("4-back-up-your-site-at-any-moment");

        // Exit the backup download activity
        pressBackUntilElementIsDisplayed(R.id.nav_sites);
    }

    private void navigateStats() {
        moveToStats();
        swipeToAvoidGrayOverlay(R.id.statsPager);

        if (isElementDisplayed(R.id.button_negative)) {
            clickOn(R.id.button_negative);
        }

        // click on the Month tab
        onView(allOf(withText(R.string.stats_timeframe_months),
                isDescendantOfA(withId(R.id.tabLayout)))).perform(click());

        idleFor(8000);

        setNightModeAndWait(false);
        takeScreenshot("5-site-stats-in-your-pocket");

        // Exit the Stats Activity
        pressBackUntilElementIsDisplayed(R.id.nav_sites);
    }

    private void moveToActivityLog() {
        // Click on the "Sites" tab in the nav, then choose "Activity Log"
        clickOn(R.id.nav_sites);
        (new MySitesPage()).clickActivityLog();

        waitForElementToBeDisplayedWithoutFailure(R.id.swipe_refresh_layout);

        // Wait for the activity log to load
        idleFor(8000);
    }

    private void moveToScan() {
        // Click on the "Sites" tab in the nav, then choose "Scan"
        clickOn(R.id.nav_sites);
        (new MySitesPage()).clickScan();

        waitForElementToBeDisplayedWithoutFailure(R.id.recycler_view);

        // Wait for scan to load
        idleFor(8000);
    }

    private void moveToBackup() {
        clickOn(R.id.nav_sites);
        (new MySitesPage()).clickBackup();

        waitForElementToBeDisplayedWithoutFailure(R.id.log_list_view);

        // Wait for backup to load
        idleFor(8000);
    }

    private void moveToStats() {
        // Click on the "Sites" tab in the nav, then choose "Stats"
        clickOn(R.id.nav_sites);
        (new MySitesPage()).clickStats();

        waitForElementToBeDisplayedWithoutFailure(R.id.image_thumbnail);

        // Wait for the stats to load
        idleFor(8000);
    }

    private void takeScreenshot(String screenshotName) {
        try {
            if (runningInTestLab()) {
                ScreenShotter.takeScreenshot(screenshotName, getCurrentActivity());
            } else {
                // Fallback to screengrab
                Screengrab.screenshot(screenshotName);
            }
        } catch (RuntimeException r) {
            // Screenshots will fail when running outside of Fastlane or FTL, so this is safe to ignore.
        }
    }

    private boolean runningInTestLab() {
        // https://firebase.google.com/docs/test-lab/android/android-studio#modify_instrumented_test_behavior_for
        String testLabSetting = Settings.System.getString(
                getCurrentActivity().getContentResolver(),
                "firebase.test.lab"
        );
        return "true".equals(testLabSetting);
    }

    // In some cases there's a gray overlay on view pager screens when taking screenshots
    // this function swipes left and then right as a workaround to clear it
    // resourceID should be the ID of the viewPager
    private void swipeToAvoidGrayOverlay(int resourceID) {
        // Workaround to avoid gray overlay
        swipeLeftOnViewPager(resourceID);
        idleFor(1000);
        swipeRightOnViewPager(resourceID);
        idleFor(1000);
    }

    private void setNightModeAndWait(boolean isNightMode) {
        setNightMode(isNightMode);
        idleFor(5000);
    }
}
