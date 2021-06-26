package org.sitebay.android.modules;

import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import org.sitebay.android.ui.accounts.LoginEpilogueViewModel;
import org.sitebay.android.ui.accounts.login.jetpack.LoginSiteCheckErrorViewModel;
import org.sitebay.android.ui.accounts.login.LoginPrologueViewModel;
import org.sitebay.android.ui.comments.unified.UnifiedCommentListViewModel;
import org.sitebay.android.ui.deeplinks.DeepLinkingIntentReceiverViewModel;
import org.sitebay.android.ui.JetpackRemoteInstallViewModel;
import org.sitebay.android.ui.accounts.LoginViewModel;
import org.sitebay.android.ui.accounts.login.jetpack.LoginNoSitesViewModel;
import org.sitebay.android.ui.activitylog.list.filter.ActivityLogTypeFilterViewModel;
import org.sitebay.android.ui.domains.DomainRegistrationMainViewModel;
import org.sitebay.android.ui.engagement.EngagedPeopleListViewModel;
import org.sitebay.android.ui.engagement.UserProfileViewModel;
import org.sitebay.android.ui.jetpack.backup.download.BackupDownloadViewModel;
import org.sitebay.android.ui.jetpack.restore.RestoreViewModel;
import org.sitebay.android.ui.jetpack.scan.ScanViewModel;
import org.sitebay.android.ui.jetpack.scan.details.ThreatDetailsViewModel;
import org.sitebay.android.ui.jetpack.scan.history.ScanHistoryListViewModel;
import org.sitebay.android.ui.jetpack.scan.history.ScanHistoryViewModel;
import org.sitebay.android.ui.main.MeViewModel;
import org.sitebay.android.ui.mediapicker.MediaPickerViewModel;
import org.sitebay.android.ui.mysite.MySiteViewModel;
import org.sitebay.android.ui.people.PeopleInviteViewModel;
import org.sitebay.android.ui.mysite.dynamiccards.DynamicCardMenuViewModel;
import org.sitebay.android.ui.photopicker.PhotoPickerViewModel;
import org.sitebay.android.ui.plans.PlansViewModel;
import org.sitebay.android.ui.posts.BasicDialogViewModel;
import org.sitebay.android.ui.posts.EditPostPublishSettingsViewModel;
import org.sitebay.android.ui.posts.PostListMainViewModel;
import org.sitebay.android.ui.posts.PrepublishingAddCategoryViewModel;
import org.sitebay.android.ui.posts.PrepublishingCategoriesViewModel;
import org.sitebay.android.ui.posts.PrepublishingHomeViewModel;
import org.sitebay.android.ui.posts.PrepublishingTagsViewModel;
import org.sitebay.android.ui.posts.PrepublishingViewModel;
import org.sitebay.android.ui.posts.editor.StorePostViewModel;
import org.sitebay.android.ui.posts.prepublishing.PrepublishingPublishSettingsViewModel;
import org.sitebay.android.ui.prefs.categories.CategoriesListViewModel;
import org.sitebay.android.ui.prefs.homepage.HomepageSettingsViewModel;
import org.sitebay.android.ui.prefs.timezone.SiteSettingsTimezoneViewModel;
import org.sitebay.android.ui.reader.ReaderCommentListViewModel;
import org.sitebay.android.ui.reader.discover.ReaderDiscoverViewModel;
import org.sitebay.android.ui.reader.discover.interests.ReaderInterestsViewModel;
import org.sitebay.android.ui.reader.subfilter.SubFilterViewModel;
import org.sitebay.android.ui.reader.viewmodels.ReaderPostDetailViewModel;
import org.sitebay.android.ui.reader.viewmodels.ReaderPostListViewModel;
import org.sitebay.android.ui.reader.viewmodels.ReaderViewModel;
import org.sitebay.android.ui.reader.viewmodels.SubfilterPageViewModel;
import org.sitebay.android.ui.sitecreation.SiteCreationMainVM;
import org.sitebay.android.ui.sitecreation.domains.SiteCreationDomainsViewModel;
import org.sitebay.android.ui.sitecreation.domains.SiteCreationLoginDetailsViewModel;
import org.sitebay.android.ui.sitecreation.previews.SitePreviewViewModel;
import org.sitebay.android.ui.sitecreation.theme.HomePagePickerViewModel;
import org.sitebay.android.ui.stats.refresh.StatsViewModel;
import org.sitebay.android.ui.stats.refresh.lists.DaysListViewModel;
import org.sitebay.android.ui.stats.refresh.lists.InsightsListViewModel;
import org.sitebay.android.ui.stats.refresh.lists.MonthsListViewModel;
import org.sitebay.android.ui.stats.refresh.lists.WeeksListViewModel;
import org.sitebay.android.ui.stats.refresh.lists.YearsListViewModel;
import org.sitebay.android.ui.stats.refresh.lists.detail.DetailListViewModel;
import org.sitebay.android.ui.stats.refresh.lists.detail.StatsDetailViewModel;
import org.sitebay.android.ui.stats.refresh.lists.sections.insights.management.InsightsManagementViewModel;
import org.sitebay.android.ui.stats.refresh.lists.widget.configuration.StatsColorSelectionViewModel;
import org.sitebay.android.ui.stats.refresh.lists.widget.configuration.StatsDataTypeSelectionViewModel;
import org.sitebay.android.ui.stats.refresh.lists.widget.configuration.StatsSiteSelectionViewModel;
import org.sitebay.android.ui.stats.refresh.lists.widget.configuration.StatsWidgetConfigureViewModel;
import org.sitebay.android.ui.stats.refresh.lists.widget.minified.StatsMinifiedWidgetConfigureViewModel;
import org.sitebay.android.ui.stories.StoryComposerViewModel;
import org.sitebay.android.ui.stories.intro.StoriesIntroViewModel;
import org.sitebay.android.ui.suggestion.SuggestionViewModel;
import org.sitebay.android.ui.whatsnew.FeatureAnnouncementViewModel;
import org.sitebay.android.util.config.manual.ManualFeatureConfigViewModel;
import org.sitebay.android.viewmodel.ViewModelFactory;
import org.sitebay.android.viewmodel.ViewModelKey;
import org.sitebay.android.viewmodel.accounts.PostSignupInterstitialViewModel;
import org.sitebay.android.viewmodel.activitylog.ActivityLogDetailViewModel;
import org.sitebay.android.viewmodel.activitylog.ActivityLogViewModel;
import org.sitebay.android.viewmodel.domains.DomainRegistrationDetailsViewModel;
import org.sitebay.android.viewmodel.domains.DomainSuggestionsViewModel;
import org.sitebay.android.viewmodel.history.HistoryViewModel;
import org.sitebay.android.ui.bloggingreminders.BloggingRemindersViewModel;
import org.sitebay.android.viewmodel.main.SitePickerViewModel;
import org.sitebay.android.viewmodel.main.WPMainActivityViewModel;
import org.sitebay.android.viewmodel.mlp.ModalLayoutPickerViewModel;
import org.sitebay.android.viewmodel.pages.PageListViewModel;
import org.sitebay.android.viewmodel.pages.PageParentSearchViewModel;
import org.sitebay.android.viewmodel.pages.PageParentViewModel;
import org.sitebay.android.viewmodel.pages.PagesViewModel;
import org.sitebay.android.viewmodel.pages.SearchListViewModel;
import org.sitebay.android.viewmodel.plugins.PluginBrowserViewModel;
import org.sitebay.android.viewmodel.posts.PostListCreateMenuViewModel;
import org.sitebay.android.viewmodel.posts.PostListViewModel;
import org.sitebay.android.viewmodel.quickstart.QuickStartViewModel;
import org.sitebay.android.viewmodel.storage.StorageUtilsViewModel;
import org.sitebay.android.viewmodel.wpwebview.WPWebViewViewModel;

import dagger.Binds;
import dagger.Module;
import dagger.multibindings.IntoMap;

@Module
abstract class ViewModelModule {
    @Binds
    @IntoMap
    @ViewModelKey(PluginBrowserViewModel.class)
    abstract ViewModel pluginBrowserViewModel(PluginBrowserViewModel viewModel);

    @Binds
    @IntoMap
    @ViewModelKey(ActivityLogViewModel.class)
    abstract ViewModel activityLogViewModel(ActivityLogViewModel viewModel);

    @Binds
    @IntoMap
    @ViewModelKey(ActivityLogDetailViewModel.class)
    abstract ViewModel activityLogDetailViewModel(ActivityLogDetailViewModel viewModel);

    @Binds
    @IntoMap
    @ViewModelKey(PagesViewModel.class)
    abstract ViewModel pagesViewModel(PagesViewModel viewModel);

    @Binds
    @IntoMap
    @ViewModelKey(SearchListViewModel.class)
    abstract ViewModel searchListViewModel(SearchListViewModel viewModel);

    @Binds
    @IntoMap
    @ViewModelKey(PageListViewModel.class)
    abstract ViewModel pageListViewModel(PageListViewModel viewModel);

    @Binds
    @IntoMap
    @ViewModelKey(PageParentViewModel.class)
    abstract ViewModel pageParentViewModel(PageParentViewModel viewModel);

    @Binds
    @IntoMap
    @ViewModelKey(ReaderPostListViewModel.class)
    abstract ViewModel readerPostListViewModel(ReaderPostListViewModel viewModel);

    @Binds
    @IntoMap
    @ViewModelKey(ReaderPostDetailViewModel.class)
    abstract ViewModel readerPostDetailViewModel(ReaderPostDetailViewModel viewModel);

    @Binds
    @IntoMap
    @ViewModelKey(SubFilterViewModel.class)
    abstract ViewModel readerSubFilterViewModel(SubFilterViewModel viewModel);

    @Binds
    @IntoMap
    @ViewModelKey(SubfilterPageViewModel.class)
    abstract ViewModel subfilterPageViewModel(SubfilterPageViewModel viewModel);

    @Binds
    @IntoMap
    @ViewModelKey(JetpackRemoteInstallViewModel.class)
    abstract ViewModel jetpackRemoteInstallViewModel(JetpackRemoteInstallViewModel viewModel);

    @Binds
    @IntoMap
    @ViewModelKey(QuickStartViewModel.class)
    abstract ViewModel quickStartViewModel(QuickStartViewModel viewModel);

    @Binds
    @IntoMap
    @ViewModelKey(InsightsListViewModel.class)
    abstract ViewModel insightsTabViewModel(InsightsListViewModel viewModel);

    @Binds
    @IntoMap
    @ViewModelKey(DaysListViewModel.class)
    abstract ViewModel daysTabViewModel(DaysListViewModel viewModel);

    @Binds
    @IntoMap
    @ViewModelKey(WeeksListViewModel.class)
    abstract ViewModel weeksTabViewModel(WeeksListViewModel viewModel);

    @Binds
    @IntoMap
    @ViewModelKey(MonthsListViewModel.class)
    abstract ViewModel monthsTabViewModel(MonthsListViewModel viewModel);

    @Binds
    @IntoMap
    @ViewModelKey(YearsListViewModel.class)
    abstract ViewModel yearsTabViewModel(YearsListViewModel viewModel);

    @Binds
    @IntoMap
    @ViewModelKey(StatsDetailViewModel.class)
    abstract ViewModel statsDetailViewModel(StatsDetailViewModel viewModel);

    @Binds
    @IntoMap
    @ViewModelKey(DetailListViewModel.class)
    abstract ViewModel detailListViewModel(DetailListViewModel viewModel);

    @Binds
    @IntoMap
    @ViewModelKey(StatsViewModel.class)
    abstract ViewModel statsViewModel(StatsViewModel viewModel);

    @Binds
    @IntoMap
    @ViewModelKey(StatsWidgetConfigureViewModel.class)
    abstract ViewModel statsViewsWidgetViewModel(StatsWidgetConfigureViewModel viewModel);

    @Binds
    @IntoMap
    @ViewModelKey(StatsSiteSelectionViewModel.class)
    abstract ViewModel statsSiteSelectionViewModel(StatsSiteSelectionViewModel viewModel);

    @Binds
    @IntoMap
    @ViewModelKey(StatsDataTypeSelectionViewModel.class)
    abstract ViewModel statsDataTypeSelectionViewModel(StatsDataTypeSelectionViewModel viewModel);

    @Binds
    @IntoMap
    @ViewModelKey(StatsMinifiedWidgetConfigureViewModel.class)
    abstract ViewModel statsMinifiedWidgetViewModel(StatsMinifiedWidgetConfigureViewModel viewModel);

    @Binds
    @IntoMap
    @ViewModelKey(StatsColorSelectionViewModel.class)
    abstract ViewModel statsColorSelectionViewModel(StatsColorSelectionViewModel viewModel);

    @Binds
    @IntoMap
    @ViewModelKey(InsightsManagementViewModel.class)
    abstract ViewModel insightsManagementViewModel(InsightsManagementViewModel viewModel);

    @Binds
    @IntoMap
    @ViewModelKey(HistoryViewModel.class)
    abstract ViewModel historyViewModel(HistoryViewModel viewModel);

    @Binds
    @IntoMap
    @ViewModelKey(SiteCreationDomainsViewModel.class)
    abstract ViewModel siteCreationDomainsViewModel(SiteCreationDomainsViewModel viewModel);

    @Binds
    @IntoMap
    @ViewModelKey(SiteCreationLoginDetailsViewModel.class)
    abstract ViewModel siteCreationLoginDetailsViewModel(SiteCreationLoginDetailsViewModel viewModel);

    @Binds
    @IntoMap
    @ViewModelKey(SiteCreationMainVM.class)
    abstract ViewModel siteCreationMainVM(SiteCreationMainVM viewModel);

    @Binds
    @IntoMap
    @ViewModelKey(SitePreviewViewModel.class)
    abstract ViewModel newSitePreviewViewModel(SitePreviewViewModel viewModel);

    @Binds
    @IntoMap
    @ViewModelKey(PostListViewModel.class)
    abstract ViewModel postListViewModel(PostListViewModel viewModel);

    @Binds
    @IntoMap
    @ViewModelKey(PostListMainViewModel.class)
    abstract ViewModel postListMainViewModel(PostListMainViewModel viewModel);

    @Binds
    @IntoMap
    @ViewModelKey(PlansViewModel.class)
    abstract ViewModel plansViewModel(PlansViewModel viewModel);

    @Binds
    @IntoMap
    @ViewModelKey(DomainSuggestionsViewModel.class)
    abstract ViewModel domainSuggestionsViewModel(DomainSuggestionsViewModel viewModel);

    @Binds
    @IntoMap
    @ViewModelKey(WPWebViewViewModel.class)
    abstract ViewModel wpWebViewViewModel(WPWebViewViewModel viewModel);

    @Binds
    @IntoMap
    @ViewModelKey(DomainRegistrationDetailsViewModel.class)
    abstract ViewModel domainRegistrationDetailsViewModel(DomainRegistrationDetailsViewModel viewModel);

    @Binds
    @IntoMap
    @ViewModelKey(DomainRegistrationMainViewModel.class)
    abstract ViewModel domainRegistrationMainViewModel(DomainRegistrationMainViewModel viewModel);

    @Binds
    @IntoMap
    @ViewModelKey(StorePostViewModel.class)
    abstract ViewModel storePostViewModel(StorePostViewModel viewModel);

    @Binds
    @IntoMap
    @ViewModelKey(EditPostPublishSettingsViewModel.class)
    abstract ViewModel editPostPublishedSettingsViewModel(EditPostPublishSettingsViewModel viewModel);

    @Binds
    @IntoMap
    @ViewModelKey(ReaderCommentListViewModel.class)
    abstract ViewModel readerCommentListViewModel(ReaderCommentListViewModel viewModel);

    @Binds
    @IntoMap
    @ViewModelKey(WPMainActivityViewModel.class)
    abstract ViewModel wpMainActivityViewModel(WPMainActivityViewModel viewModel);

    @Binds
    @IntoMap
    @ViewModelKey(ModalLayoutPickerViewModel.class)
    abstract ViewModel mlpViewModel(ModalLayoutPickerViewModel viewModel);

    @Binds
    @IntoMap
    @ViewModelKey(HomePagePickerViewModel.class)
    abstract ViewModel hppViewModel(HomePagePickerViewModel viewModel);

    @Binds
    @IntoMap
    @ViewModelKey(PostSignupInterstitialViewModel.class)
    abstract ViewModel postSignupInterstitialViewModel(PostSignupInterstitialViewModel viewModel);

    @Binds
    @IntoMap
    @ViewModelKey(PageParentSearchViewModel.class)
    abstract ViewModel pageParentSearchViewModel(PageParentSearchViewModel viewModel);

    @Binds
    @IntoMap
    @ViewModelKey(FeatureAnnouncementViewModel.class)
    abstract ViewModel featureAnnouncementViewModel(FeatureAnnouncementViewModel viewModel);

    @Binds
    @IntoMap
    @ViewModelKey(SitePickerViewModel.class)
    abstract ViewModel sitePickerViewModel(SitePickerViewModel viewModel);

    @Binds
    @IntoMap
    @ViewModelKey(ReaderViewModel.class)
    abstract ViewModel readerParentPostListViewModel(ReaderViewModel viewModel);

    @Binds
    @IntoMap
    @ViewModelKey(ReaderDiscoverViewModel.class)
    abstract ViewModel readerDiscoverViewModel(ReaderDiscoverViewModel viewModel);

    @Binds
    @IntoMap
    @ViewModelKey(ReaderInterestsViewModel.class)
    abstract ViewModel readerInterestsViewModel(ReaderInterestsViewModel viewModel);

    @Binds
    @IntoMap
    @ViewModelKey(HomepageSettingsViewModel.class)
    abstract ViewModel homepageSettingsDialogViewModel(HomepageSettingsViewModel viewModel);

    @Binds
    @IntoMap
    @ViewModelKey(PrepublishingViewModel.class)
    abstract ViewModel prepublishingViewModel(PrepublishingViewModel viewModel);

    @Binds
    @IntoMap
    @ViewModelKey(PrepublishingHomeViewModel.class)
    abstract ViewModel prepublishingOptionsViewModel(PrepublishingHomeViewModel viewModel);

    @Binds
    @IntoMap
    @ViewModelKey(PrepublishingTagsViewModel.class)
    abstract ViewModel prepublishingTagsViewModel(PrepublishingTagsViewModel viewModel);

    @Binds
    @IntoMap
    @ViewModelKey(PrepublishingPublishSettingsViewModel.class)
    abstract ViewModel prepublishingPublishSettingsViewModel(PrepublishingPublishSettingsViewModel viewModel);

    @Binds
    @IntoMap
    @ViewModelKey(MeViewModel.class)
    abstract ViewModel meViewModel(MeViewModel viewModel);

    @Binds
    @IntoMap
    @ViewModelKey(PostListCreateMenuViewModel.class)
    abstract ViewModel postListCreateMenuViewModel(PostListCreateMenuViewModel postListCreateMenuViewModel);

    @Binds
    @IntoMap
    @ViewModelKey(StoryComposerViewModel.class)
    abstract ViewModel storyComposerViewModel(StoryComposerViewModel viewModel);

    @Binds
    @IntoMap
    @ViewModelKey(StoriesIntroViewModel.class)
    abstract ViewModel storiesIntroViewModel(StoriesIntroViewModel viewModel);

    @Binds
    @IntoMap
    @ViewModelKey(PhotoPickerViewModel.class)
    abstract ViewModel photoPickerViewModel(PhotoPickerViewModel viewModel);

    @Binds
    @IntoMap
    @ViewModelKey(MediaPickerViewModel.class)
    abstract ViewModel mediaPickerViewModel(MediaPickerViewModel viewModel);

    @Binds
    @IntoMap
    @ViewModelKey(ManualFeatureConfigViewModel.class)
    abstract ViewModel manualFeatureConfigViewModel(ManualFeatureConfigViewModel viewModel);

    @Binds
    @IntoMap
    @ViewModelKey(PrepublishingCategoriesViewModel.class)
    abstract ViewModel prepublishingCategoriesViewModel(PrepublishingCategoriesViewModel viewModel);

    @Binds
    @IntoMap
    @ViewModelKey(PrepublishingAddCategoryViewModel.class)
    abstract ViewModel prepublishingAddCategoryViewModel(PrepublishingAddCategoryViewModel viewModel);

    @Binds
    @IntoMap
    @ViewModelKey(SuggestionViewModel.class)
    abstract ViewModel suggestionViewModel(SuggestionViewModel viewModel);

    @Binds
    @IntoMap
    @ViewModelKey(ActivityLogTypeFilterViewModel.class)
    abstract ViewModel activityLogTypeFilterViewModel(ActivityLogTypeFilterViewModel viewModel);

    @Binds
    @IntoMap
    @ViewModelKey(ScanViewModel.class)
    abstract ViewModel scanViewModel(ScanViewModel viewModel);

    @Binds
    @IntoMap
    @ViewModelKey(ScanHistoryViewModel.class)
    abstract ViewModel scanHistoryViewModel(ScanHistoryViewModel viewModel);

    @Binds
    @IntoMap
    @ViewModelKey(ScanHistoryListViewModel.class)
    abstract ViewModel scanHistoryListViewModel(ScanHistoryListViewModel viewModel);

    @Binds
    @IntoMap
    @ViewModelKey(ThreatDetailsViewModel.class)
    abstract ViewModel threatDetailsViewModel(ThreatDetailsViewModel viewModel);

    @Binds
    @IntoMap
    @ViewModelKey(MySiteViewModel.class)
    abstract ViewModel mySiteViewModel(MySiteViewModel viewModel);

    @Binds
    @IntoMap
    @ViewModelKey(BasicDialogViewModel.class)
    abstract ViewModel basicDialogViewModel(BasicDialogViewModel viewModel);

    @Binds
    @IntoMap
    @ViewModelKey(BackupDownloadViewModel.class)
    abstract ViewModel backupDownloadViewModel(BackupDownloadViewModel viewModel);

    @Binds
    @IntoMap
    @ViewModelKey(RestoreViewModel.class)
    abstract ViewModel restoreViewModel(RestoreViewModel viewModel);

    @Binds
    @IntoMap
    @ViewModelKey(DynamicCardMenuViewModel.class)
    abstract ViewModel dynamicCardMenuViewModel(DynamicCardMenuViewModel viewModel);

    @Binds
    @IntoMap
    @ViewModelKey(PeopleInviteViewModel.class)
    abstract ViewModel peopleInviteViewModel(PeopleInviteViewModel viewModel);

    @Binds
    @IntoMap
    @ViewModelKey(EngagedPeopleListViewModel.class)
    abstract ViewModel engagedPeopleListViewModel(EngagedPeopleListViewModel viewModel);

    @Binds
    @IntoMap
    @ViewModelKey(UserProfileViewModel.class)
    abstract ViewModel userProfileViewModel(UserProfileViewModel viewModel);

    @Binds
    abstract ViewModelProvider.Factory provideViewModelFactory(ViewModelFactory viewModelFactory);

    @Binds
    @IntoMap
    @ViewModelKey(SiteSettingsTimezoneViewModel.class)
    abstract ViewModel siteSettingsTimezoneViewModel(SiteSettingsTimezoneViewModel viewModel);

    @Binds
    @IntoMap
    @ViewModelKey(LoginNoSitesViewModel.class)
    abstract ViewModel loginNoSitesErrorViewModel(LoginNoSitesViewModel viewModel);

    @Binds
    @IntoMap
    @ViewModelKey(LoginEpilogueViewModel.class)
    abstract ViewModel loginEpilogueViewModel(LoginEpilogueViewModel viewModel);

    @Binds
    @IntoMap
    @ViewModelKey(LoginPrologueViewModel.class)
    abstract ViewModel loginPrologueViewModel(LoginPrologueViewModel viewModel);

    @Binds
    @IntoMap
    @ViewModelKey(LoginViewModel.class)
    abstract ViewModel loginViewModel(LoginViewModel viewModel);

    @Binds
    @IntoMap
    @ViewModelKey(DeepLinkingIntentReceiverViewModel.class)
    abstract ViewModel deepLinkingIntentReceiverViewModel(DeepLinkingIntentReceiverViewModel viewModel);

    @Binds
    @IntoMap
    @ViewModelKey(LoginSiteCheckErrorViewModel.class)
    abstract ViewModel loginSiteCheckErrorViewModel(LoginSiteCheckErrorViewModel viewModel);

    @Binds
    @IntoMap
    @ViewModelKey(StorageUtilsViewModel.class)
    abstract ViewModel storageUtilsViewModel(StorageUtilsViewModel viewModel);

    @Binds
    @IntoMap
    @ViewModelKey(UnifiedCommentListViewModel.class)
    abstract ViewModel unifiedCommentListViewModel(UnifiedCommentListViewModel viewModel);

    @Binds
    @IntoMap
    @ViewModelKey(BloggingRemindersViewModel.class)
    abstract ViewModel bloggingRemindersViewModel(BloggingRemindersViewModel viewModel);

    @Binds
    @IntoMap
    @ViewModelKey(CategoriesListViewModel.class)
    abstract ViewModel categoriesViewModel(CategoriesListViewModel viewModel);
}
