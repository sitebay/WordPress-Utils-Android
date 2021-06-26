package org.sitebay.android.ui.prefs;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;

import org.sitebay.android.R;
import org.sitebay.android.WordPress;
import org.sitebay.android.fluxc.Dispatcher;
import org.sitebay.android.fluxc.store.AccountStore;
import org.sitebay.android.ui.LocaleAwareActivity;

import javax.inject.Inject;

public class MyProfileActivity extends LocaleAwareActivity {
    @Inject Dispatcher mDispatcher;
    @Inject AccountStore mAccountStore;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((WordPress) getApplication()).component().inject(this);

        setContentView(R.layout.my_profile_activity);

        Toolbar toolbar = findViewById(R.id.toolbar_main);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(R.string.my_profile);
        }
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
