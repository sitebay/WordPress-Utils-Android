package org.sitebay.android.ui.stats;

import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.sitebay.android.R;
import org.sitebay.android.WordPress;
import org.sitebay.android.fluxc.Dispatcher;
import org.sitebay.android.fluxc.action.AccountAction;
import org.sitebay.android.fluxc.generated.AccountActionBuilder;
import org.sitebay.android.fluxc.model.SiteModel;
import org.sitebay.android.fluxc.store.AccountStore;
import org.sitebay.android.fluxc.store.AccountStore.OnAccountChanged;
import org.sitebay.android.ui.JetpackConnectionWebViewActivity;
import org.sitebay.android.ui.LocaleAwareActivity;
import org.sitebay.android.ui.WPWebViewActivity;
import org.sitebay.android.util.AppLog;
import org.sitebay.android.util.AppLog.T;
import org.sitebay.android.util.WPUrlUtils;

import javax.inject.Inject;

import static org.sitebay.android.WordPress.SITE;
import static org.sitebay.android.ui.JetpackConnectionSource.STATS;

/**
 * An activity that shows when user tries to open Stats without Jetpack connected.
 * It offers a link to the Jetpack connection flow.
 */
public class StatsConnectJetpackActivity extends LocaleAwareActivity {
    public static final String ARG_CONTINUE_JETPACK_CONNECT = "ARG_CONTINUE_JETPACK_CONNECT";
    public static final String FAQ_URL = "https://sitebay.org/plugins/jetpack/#faq";

    private boolean mIsJetpackConnectStarted;

    @Inject AccountStore mAccountStore;
    @Inject Dispatcher mDispatcher;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((WordPress) getApplication()).component().inject(this);

        setContentView(R.layout.stats_jetpack_connection_activity);

        Toolbar toolbar = findViewById(R.id.toolbar_main);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(R.string.stats);
            actionBar.setDisplayShowTitleEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        setTitle(R.string.stats);

        // Continue Jetpack connect flow if coming from login/signup magic link.
        if (savedInstanceState == null && getIntent() != null && getIntent().getExtras() != null
            && getIntent().getExtras().getBoolean(ARG_CONTINUE_JETPACK_CONNECT, false)) {
            if (TextUtils.isEmpty(mAccountStore.getAccount().getUserName())) {
                mDispatcher.dispatch(AccountActionBuilder.newFetchAccountAction());
            } else {
                startJetpackConnectionFlow((SiteModel) getIntent().getSerializableExtra(SITE));
            }
        }

        Button setupButton = findViewById(R.id.jetpack_setup);
        setupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startJetpackConnectionFlow(
                        (SiteModel) StatsConnectJetpackActivity.this.getIntent().getSerializableExtra(SITE));
            }
        });
        Button jetpackFaq = findViewById(R.id.jetpack_faq);
        jetpackFaq.setOnClickListener(new OnClickListener() {
            @Override public void onClick(View v) {
                WPWebViewActivity.openURL(StatsConnectJetpackActivity.this, FAQ_URL);
            }
        });
        TextView jetpackTermsAndConditions = findViewById(R.id.jetpack_terms_and_conditions);
        jetpackTermsAndConditions.setText(Html.fromHtml(String.format(
                getResources().getString(R.string.jetpack_connection_terms_and_conditions), "<u>", "</u>")));
        jetpackTermsAndConditions.setOnClickListener(new OnClickListener() {
            @Override public void onClick(View v) {
                WPWebViewActivity.openURL(StatsConnectJetpackActivity.this,
                        WPUrlUtils.buildTermsOfServiceUrl(StatsConnectJetpackActivity.this));
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        mDispatcher.register(this);
    }

    @Override
    protected void onStop() {
        mDispatcher.unregister(this);
        super.onStop();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void startJetpackConnectionFlow(SiteModel siteModel) {
        mIsJetpackConnectStarted = true;
        JetpackConnectionWebViewActivity
                .startJetpackConnectionFlow(this, STATS, siteModel, mAccountStore.hasAccessToken());
        finish();
    }

    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onAccountChanged(OnAccountChanged event) {
        if (!isFinishing()) {
            if (event.isError()) {
                AppLog.e(T.API, "StatsConnectJetpackActivity.onAccountChanged error: "
                                + event.error.type + " - " + event.error.message);
            } else if (!mIsJetpackConnectStarted && event.causeOfChange == AccountAction.FETCH_ACCOUNT
                       && !TextUtils.isEmpty(mAccountStore.getAccount().getUserName())) {
                startJetpackConnectionFlow((SiteModel) getIntent().getSerializableExtra(SITE));
            }
        }
    }
}
