package org.sitebay.android.e2e.pages;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.sitebay.android.support.WPSupportUtils.clickOn;

public class SitePickerPage {
    public SitePickerPage() {
    }

    public void chooseSiteWithURL(String url) {
        clickOn(onView(withText(url)));
    }
}
