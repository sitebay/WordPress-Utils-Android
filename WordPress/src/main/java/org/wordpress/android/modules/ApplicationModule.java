package org.sitebay.android.modules;

import android.app.Application;
import android.content.Context;

import androidx.lifecycle.LiveData;

import com.tenor.android.core.network.ApiClient;
import com.tenor.android.core.network.ApiService;
import com.tenor.android.core.network.IApiClient;

import org.sitebay.android.BuildConfig;
import org.sitebay.android.ui.CommentFullScreenDialogFragment;
import org.sitebay.android.ui.accounts.signup.SettingsUsernameChangerFragment;
import org.sitebay.android.ui.accounts.signup.UsernameChangerFullScreenDialogFragment;
import org.sitebay.android.ui.domains.DomainRegistrationDetailsFragment.CountryPickerDialogFragment;
import org.sitebay.android.ui.domains.DomainRegistrationDetailsFragment.StatePickerDialogFragment;
import org.sitebay.android.ui.jetpack.backup.download.BackupDownloadStep;
import org.sitebay.android.ui.jetpack.backup.download.BackupDownloadStepsProvider;
import org.sitebay.android.ui.jetpack.restore.RestoreStep;
import org.sitebay.android.ui.jetpack.restore.RestoreStepsProvider;
import org.sitebay.android.ui.mediapicker.loader.TenorGifClient;
import org.sitebay.android.ui.posts.BasicDialog;
import org.sitebay.android.ui.reader.ReaderPostWebViewCachingFragment;
import org.sitebay.android.ui.reader.subfilter.SubfilterPageFragment;
import org.sitebay.android.ui.sitecreation.SiteCreationStep;
import org.sitebay.android.ui.sitecreation.SiteCreationStepsProvider;
import org.sitebay.android.ui.stats.refresh.StatsFragment;
import org.sitebay.android.ui.stats.refresh.StatsViewAllFragment;
import org.sitebay.android.ui.stats.refresh.lists.StatsListFragment;
import org.sitebay.android.ui.stats.refresh.lists.detail.StatsDetailFragment;
import org.sitebay.android.ui.stats.refresh.lists.sections.insights.management.InsightsManagementFragment;
import org.sitebay.android.ui.stats.refresh.lists.widget.configuration.StatsWidgetColorSelectionDialogFragment;
import org.sitebay.android.ui.stats.refresh.lists.widget.configuration.StatsWidgetConfigureFragment;
import org.sitebay.android.ui.stats.refresh.lists.widget.configuration.StatsWidgetDataTypeSelectionDialogFragment;
import org.sitebay.android.ui.stats.refresh.lists.widget.configuration.StatsWidgetSiteSelectionDialogFragment;
import org.sitebay.android.ui.stats.refresh.lists.widget.minified.StatsMinifiedWidgetConfigureFragment;
import org.sitebay.android.util.config.manual.ManualFeatureConfigFragment;
import org.sitebay.android.util.wizard.WizardManager;
import org.sitebay.android.viewmodel.helpers.ConnectionStatus;
import org.sitebay.android.viewmodel.helpers.ConnectionStatusLiveData;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import dagger.android.ContributesAndroidInjector;

@Module
public abstract class ApplicationModule {
    // Expose Application as an injectable context
    @Binds
    abstract Context bindContext(Application application);

    @ContributesAndroidInjector
    abstract StatsListFragment contributeStatListFragment();

    @ContributesAndroidInjector
    abstract StatsViewAllFragment contributeStatsViewAllFragment();

    @ContributesAndroidInjector
    abstract InsightsManagementFragment contributeInsightsManagementFragment();

    @ContributesAndroidInjector
    abstract StatsFragment contributeStatsFragment();

    @ContributesAndroidInjector
    abstract StatsDetailFragment contributeStatsDetailFragment();

    @ContributesAndroidInjector
    abstract CountryPickerDialogFragment contributeCountryPickerDialogFragment();

    @ContributesAndroidInjector
    abstract StatePickerDialogFragment contributeCStatePickerDialogFragment();

    @ContributesAndroidInjector
    abstract StatsWidgetConfigureFragment contributeStatsViewsWidgetConfigureFragment();

    @ContributesAndroidInjector
    abstract StatsWidgetSiteSelectionDialogFragment contributeSiteSelectionDialogFragment();

    @ContributesAndroidInjector
    abstract StatsWidgetColorSelectionDialogFragment contributeViewModeSelectionDialogFragment();

    @ContributesAndroidInjector
    abstract StatsMinifiedWidgetConfigureFragment contributeStatsMinifiedWidgetConfigureFragment();

    @ContributesAndroidInjector
    abstract StatsWidgetDataTypeSelectionDialogFragment contributeDataTypeSelectionDialogFragment();

    @ContributesAndroidInjector
    abstract CommentFullScreenDialogFragment contributecommentFullScreenDialogFragment();

    @ContributesAndroidInjector
    abstract UsernameChangerFullScreenDialogFragment contributeUsernameChangerFullScreenDialogFragment();

    @ContributesAndroidInjector
    abstract SettingsUsernameChangerFragment contributeSettingsUsernameChangerFragment();

    @ContributesAndroidInjector
    abstract ReaderPostWebViewCachingFragment contributeReaderPostWebViewCachingFragment();

    @ContributesAndroidInjector
    abstract SubfilterPageFragment contributeSubfilterPageFragment();

    @ContributesAndroidInjector
    abstract ManualFeatureConfigFragment contributeManualFeatureConfigFragment();

    @ContributesAndroidInjector
    abstract BasicDialog contributeBasicDialog();

    @Provides
    public static WizardManager<SiteCreationStep> provideWizardManager(
            SiteCreationStepsProvider stepsProvider) {
        return new WizardManager<>(stepsProvider.getSteps());
    }

    @Provides
    static LiveData<ConnectionStatus> provideConnectionStatusLiveData(Context context) {
        return new ConnectionStatusLiveData.Factory(context).create();
    }

    @Provides
    static TenorGifClient provideTenorGifClient(Context context) {
        ApiService.IBuilder<IApiClient> builder = new ApiService.Builder<>(context, IApiClient.class);
        builder.apiKey(BuildConfig.TENOR_API_KEY);
        ApiClient.init(context, builder);
        return new TenorGifClient(context, ApiClient.getInstance(context));
    }

    @Provides
    public static WizardManager<BackupDownloadStep> provideBackupDownloadWizardManager(
            BackupDownloadStepsProvider stepsProvider) {
        return new WizardManager<>(stepsProvider.getSteps());
    }

    @Provides
    public static WizardManager<RestoreStep> provideRestoreWizardManager(
            RestoreStepsProvider stepsProvider) {
        return new WizardManager<>(stepsProvider.getSteps());
    }
}
