package org.sitebay.android.ui;

import android.content.Intent;
import android.os.Bundle;

import org.sitebay.android.R;
import org.sitebay.android.WordPress;
import org.sitebay.android.ui.main.WPMainActivity;
import org.sitebay.android.util.ProfilingUtils;
import org.sitebay.android.util.ToastUtils;

public class WPLaunchActivity extends LocaleAwareActivity {
    /*
     * this the main (default) activity, which does nothing more than launch the
     * previously active activity on startup - note that it's defined in the
     * manifest to have no UI
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ProfilingUtils.split("WPLaunchActivity.onCreate");
        launchWPMainActivity();
    }

    private void launchWPMainActivity() {
        if (WordPress.wpDB == null) {
            ToastUtils.showToast(this, R.string.fatal_db_error, ToastUtils.Duration.LONG);
            finish();
            return;
        }

        Intent intent = new Intent(this, WPMainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction(getIntent().getAction());
        intent.setData(getIntent().getData());
        startActivity(intent);
        finish();
    }
}
