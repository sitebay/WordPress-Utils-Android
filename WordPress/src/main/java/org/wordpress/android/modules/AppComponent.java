package org.sitebay.android.modules;

import android.app.Application;

import com.automattic.android.tracks.crashlogging.CrashLogging;

import org.sitebay.android.WordPress;
import org.sitebay.android.fluxc.module.DatabaseModule;
import org.sitebay.android.fluxc.module.OkHttpClientModule;
import org.sitebay.android.fluxc.module.ReleaseNetworkModule;
import org.sitebay.android.fluxc.module.ReleaseToolsModule;
import org.sitebay.android.login.di.LoginFragmentModule;
import org.sitebay.android.login.di.LoginServiceModule;
import org.sitebay.android.push.GCMMessageService;
import org.sitebay.android.push.GCMRegistrationIntentService;
import org.sitebay.android.push.NotificationsProcessingService;
import org.sitebay.android.ui.AddQuickPressShortcutActivity;
import org.sitebay.android.ui.CommentFullScreenDialogFragment;
import org.sitebay.android.ui.JetpackConnectionResultActivity;
import org.sitebay.android.ui.JetpackRemoteInstallFragment;
import org.sitebay.android.ui.ShareIntentReceiverActivity;
import org.sitebay.android.ui.ShareIntentReceiverFragment;
import org.sitebay.android.ui.WPWebViewActivity;
import org.sitebay.android.ui.accounts.HelpActivity;
import org.sitebay.android.ui.accounts.LoginActivity;
import org.sitebay.android.ui.accounts.LoginEpilogueActivity;
import org.sitebay.android.ui.accounts.LoginMagicLinkInterceptActivity;
import org.sitebay.android.ui.accounts.PostSignupInterstitialActivity;
import org.sitebay.android.ui.accounts.SignupEpilogueActivity;
import org.sitebay.android.ui.accounts.login.LoginEpilogueFragment;
import org.sitebay.android.ui.accounts.login.LoginPrologueFragment;
import org.sitebay.android.ui.accounts.login.jetpack.LoginNoSitesFragment;
import org.sitebay.android.ui.accounts.login.jetpack.LoginSiteCheckErrorFragment;
import org.sitebay.android.ui.accounts.signup.SignupEpilogueFragment;
import org.sitebay.android.ui.activitylog.detail.ActivityLogDetailFragment;
import org.sitebay.android.ui.activitylog.list.ActivityLogListActivity;
import org.sitebay.android.ui.activitylog.list.ActivityLogListFragment;
import org.sitebay.android.ui.activitylog.list.filter.ActivityLogTypeFilterFragment;
import org.sitebay.android.ui.bloggingreminders.BloggingReminderBottomSheetFragment;
import org.sitebay.android.ui.comments.CommentAdapter;
import org.sitebay.android.ui.comments.CommentDetailFragment;
import org.sitebay.android.ui.comments.CommentsActivity;
import org.sitebay.android.ui.comments.CommentsDetailActivity;
import org.sitebay.android.ui.comments.CommentsListFragment;
import org.sitebay.android.ui.comments.EditCommentActivity;
import org.sitebay.android.ui.comments.unified.UnifiedCommentListAdapter;
import org.sitebay.android.ui.comments.unified.UnifiedCommentListFragment;
import org.sitebay.android.ui.comments.unified.UnifiedCommentsActivity;
import org.sitebay.android.ui.deeplinks.DeepLinkingIntentReceiverActivity;
import org.sitebay.android.ui.domains.DomainRegistrationActivity;
import org.sitebay.android.ui.domains.DomainRegistrationDetailsFragment;
import org.sitebay.android.ui.domains.DomainSuggestionsFragment;
import org.sitebay.android.ui.engagement.EngagedPeopleListActivity;
import org.sitebay.android.ui.engagement.EngagedPeopleListFragment;
import org.sitebay.android.ui.engagement.UserProfileBottomSheetFragment;
import org.sitebay.android.ui.history.HistoryAdapter;
import org.sitebay.android.ui.history.HistoryDetailContainerFragment;
import org.sitebay.android.ui.jetpack.backup.download.BackupDownloadActivity;
import org.sitebay.android.ui.jetpack.backup.download.BackupDownloadFragment;
import org.sitebay.android.ui.jetpack.restore.RestoreActivity;
import org.sitebay.android.ui.jetpack.restore.RestoreFragment;
import org.sitebay.android.ui.jetpack.scan.ScanFragment;
import org.sitebay.android.ui.jetpack.scan.details.ThreatDetailsFragment;
import org.sitebay.android.ui.jetpack.scan.history.ScanHistoryFragment;
import org.sitebay.android.ui.jetpack.scan.history.ScanHistoryListFragment;
import org.sitebay.android.ui.layoutpicker.LayoutPreviewFragment;
import org.sitebay.android.ui.layoutpicker.LayoutsAdapter;
import org.sitebay.android.ui.main.AddContentAdapter;
import org.sitebay.android.ui.main.MainBottomSheetFragment;
import org.sitebay.android.ui.main.MeFragment;
import org.sitebay.android.ui.main.MySiteFragment;
import org.sitebay.android.ui.main.SitePickerActivity;
import org.sitebay.android.ui.main.SitePickerAdapter;
import org.sitebay.android.ui.main.WPMainActivity;
import org.sitebay.android.ui.media.MediaBrowserActivity;
import org.sitebay.android.ui.media.MediaGridAdapter;
import org.sitebay.android.ui.media.MediaGridFragment;
import org.sitebay.android.ui.media.MediaPreviewActivity;
import org.sitebay.android.ui.media.MediaPreviewFragment;
import org.sitebay.android.ui.media.MediaSettingsActivity;
import org.sitebay.android.ui.media.services.MediaDeleteService;
import org.sitebay.android.ui.mediapicker.MediaPickerActivity;
import org.sitebay.android.ui.mediapicker.MediaPickerFragment;
import org.sitebay.android.ui.mlp.ModalLayoutPickerFragment;
import org.sitebay.android.ui.mysite.ImprovedMySiteFragment;
import org.sitebay.android.ui.mysite.dynamiccards.DynamicCardMenuFragment;
import org.sitebay.android.ui.notifications.NotificationsDetailActivity;
import org.sitebay.android.ui.notifications.NotificationsDetailListFragment;
import org.sitebay.android.ui.notifications.NotificationsListFragment;
import org.sitebay.android.ui.notifications.NotificationsListFragmentPage;
import org.sitebay.android.ui.notifications.adapters.NotesAdapter;
import org.sitebay.android.ui.notifications.receivers.NotificationsPendingDraftsReceiver;
import org.sitebay.android.ui.pages.PageListFragment;
import org.sitebay.android.ui.pages.PageParentFragment;
import org.sitebay.android.ui.pages.PageParentSearchFragment;
import org.sitebay.android.ui.pages.PagesActivity;
import org.sitebay.android.ui.pages.PagesFragment;
import org.sitebay.android.ui.pages.SearchListFragment;
import org.sitebay.android.ui.people.PeopleInviteDialogFragment;
import org.sitebay.android.ui.people.PeopleInviteFragment;
import org.sitebay.android.ui.people.PeopleListFragment;
import org.sitebay.android.ui.people.PeopleManagementActivity;
import org.sitebay.android.ui.people.PersonDetailFragment;
import org.sitebay.android.ui.people.RoleChangeDialogFragment;
import org.sitebay.android.ui.people.RoleSelectDialogFragment;
import org.sitebay.android.ui.photopicker.PhotoPickerActivity;
import org.sitebay.android.ui.photopicker.PhotoPickerFragment;
import org.sitebay.android.ui.plans.PlanDetailsFragment;
import org.sitebay.android.ui.plans.PlansActivity;
import org.sitebay.android.ui.plans.PlansListAdapter;
import org.sitebay.android.ui.plans.PlansListFragment;
import org.sitebay.android.ui.plugins.PluginBrowserActivity;
import org.sitebay.android.ui.plugins.PluginDetailActivity;
import org.sitebay.android.ui.plugins.PluginListFragment;
import org.sitebay.android.ui.posts.AddCategoryFragment;
import org.sitebay.android.ui.posts.EditPostActivity;
import org.sitebay.android.ui.posts.EditPostPublishSettingsFragment;
import org.sitebay.android.ui.posts.EditPostSettingsFragment;
import org.sitebay.android.ui.posts.HistoryListFragment;
import org.sitebay.android.ui.posts.PostDatePickerDialogFragment;
import org.sitebay.android.ui.posts.PostListCreateMenuFragment;
import org.sitebay.android.ui.posts.PostListFragment;
import org.sitebay.android.ui.posts.PostNotificationScheduleTimeDialogFragment;
import org.sitebay.android.ui.posts.PostSettingsTagsFragment;
import org.sitebay.android.ui.posts.PostTimePickerDialogFragment;
import org.sitebay.android.ui.posts.PostsListActivity;
import org.sitebay.android.ui.posts.PrepublishingAddCategoryFragment;
import org.sitebay.android.ui.posts.PrepublishingBottomSheetFragment;
import org.sitebay.android.ui.posts.PrepublishingCategoriesFragment;
import org.sitebay.android.ui.posts.PrepublishingHomeAdapter;
import org.sitebay.android.ui.posts.PrepublishingHomeFragment;
import org.sitebay.android.ui.posts.PrepublishingTagsFragment;
import org.sitebay.android.ui.posts.PublishNotificationReceiver;
import org.sitebay.android.ui.posts.SelectCategoriesActivity;
import org.sitebay.android.ui.posts.adapters.AuthorSelectionAdapter;
import org.sitebay.android.ui.posts.prepublishing.PrepublishingPublishSettingsFragment;
import org.sitebay.android.ui.posts.services.AztecVideoLoader;
import org.sitebay.android.ui.prefs.AccountSettingsFragment;
import org.sitebay.android.ui.prefs.AppSettingsActivity;
import org.sitebay.android.ui.prefs.AppSettingsFragment;
import org.sitebay.android.ui.prefs.BlogPreferencesActivity;
import org.sitebay.android.ui.prefs.MyProfileActivity;
import org.sitebay.android.ui.prefs.MyProfileFragment;
import org.sitebay.android.ui.prefs.ReleaseNotesActivity;
import org.sitebay.android.ui.prefs.SiteSettingsFragment;
import org.sitebay.android.ui.prefs.SiteSettingsInterface;
import org.sitebay.android.ui.prefs.SiteSettingsTagDetailFragment;
import org.sitebay.android.ui.prefs.SiteSettingsTagListActivity;
import org.sitebay.android.ui.prefs.categories.CategoriesListFragment;
import org.sitebay.android.ui.prefs.homepage.HomepageSettingsDialog;
import org.sitebay.android.ui.prefs.notifications.NotificationsSettingsFragment;
import org.sitebay.android.ui.prefs.timezone.SiteSettingsTimezoneBottomSheet;
import org.sitebay.android.ui.publicize.PublicizeAccountChooserListAdapter;
import org.sitebay.android.ui.publicize.PublicizeButtonPrefsFragment;
import org.sitebay.android.ui.publicize.PublicizeDetailFragment;
import org.sitebay.android.ui.publicize.PublicizeListActivity;
import org.sitebay.android.ui.publicize.PublicizeListFragment;
import org.sitebay.android.ui.publicize.PublicizeWebViewFragment;
import org.sitebay.android.ui.publicize.adapters.PublicizeConnectionAdapter;
import org.sitebay.android.ui.publicize.adapters.PublicizeServiceAdapter;
import org.sitebay.android.ui.quickstart.QuickStartFullScreenDialogFragment;
import org.sitebay.android.ui.quickstart.QuickStartReminderReceiver;
import org.sitebay.android.ui.reader.ReaderBlogFragment;
import org.sitebay.android.ui.reader.ReaderCommentListActivity;
import org.sitebay.android.ui.reader.ReaderFragment;
import org.sitebay.android.ui.reader.ReaderPostDetailFragment;
import org.sitebay.android.ui.reader.ReaderPostListActivity;
import org.sitebay.android.ui.reader.ReaderPostListFragment;
import org.sitebay.android.ui.reader.ReaderPostPagerActivity;
import org.sitebay.android.ui.reader.ReaderSearchActivity;
import org.sitebay.android.ui.reader.ReaderSubsActivity;
import org.sitebay.android.ui.reader.SubfilterBottomSheetFragment;
import org.sitebay.android.ui.reader.adapters.ReaderBlogAdapter;
import org.sitebay.android.ui.reader.adapters.ReaderCommentAdapter;
import org.sitebay.android.ui.reader.adapters.ReaderPostAdapter;
import org.sitebay.android.ui.reader.adapters.ReaderTagAdapter;
import org.sitebay.android.ui.reader.adapters.ReaderUserAdapter;
import org.sitebay.android.ui.reader.discover.ReaderDiscoverFragment;
import org.sitebay.android.ui.reader.discover.interests.ReaderInterestsFragment;
import org.sitebay.android.ui.reader.services.discover.ReaderDiscoverJobService;
import org.sitebay.android.ui.reader.services.discover.ReaderDiscoverLogic;
import org.sitebay.android.ui.reader.services.discover.ReaderDiscoverService;
import org.sitebay.android.ui.reader.services.update.ReaderUpdateLogic;
import org.sitebay.android.ui.reader.views.ReaderCommentsPostHeaderView;
import org.sitebay.android.ui.reader.views.ReaderExpandableTagsView;
import org.sitebay.android.ui.reader.views.ReaderLikingUsersView;
import org.sitebay.android.ui.reader.views.ReaderPostDetailHeaderView;
import org.sitebay.android.ui.reader.views.ReaderSimplePostContainerView;
import org.sitebay.android.ui.reader.views.ReaderSiteHeaderView;
import org.sitebay.android.ui.reader.views.ReaderSiteSearchResultView;
import org.sitebay.android.ui.reader.views.ReaderTagHeaderView;
import org.sitebay.android.ui.reader.views.ReaderWebView;
import org.sitebay.android.ui.sitecreation.SiteCreationActivity;
import org.sitebay.android.ui.sitecreation.domains.SiteCreationDomainsFragment;
import org.sitebay.android.ui.sitecreation.domains.SiteCreationLoginDetailsFragment;
import org.sitebay.android.ui.sitecreation.previews.SiteCreationPreviewFragment;
import org.sitebay.android.ui.sitecreation.services.SiteCreationService;
import org.sitebay.android.ui.sitecreation.theme.HomePagePickerFragment;
import org.sitebay.android.ui.stats.StatsConnectJetpackActivity;
import org.sitebay.android.ui.stats.refresh.StatsActivity;
import org.sitebay.android.ui.stats.refresh.StatsModule;
import org.sitebay.android.ui.stats.refresh.lists.StatsListFragment;
import org.sitebay.android.ui.stats.refresh.lists.widget.alltime.AllTimeWidgetBlockListProviderFactory;
import org.sitebay.android.ui.stats.refresh.lists.widget.alltime.AllTimeWidgetListProvider;
import org.sitebay.android.ui.stats.refresh.lists.widget.alltime.StatsAllTimeWidget;
import org.sitebay.android.ui.stats.refresh.lists.widget.minified.StatsMinifiedWidget;
import org.sitebay.android.ui.stats.refresh.lists.widget.today.StatsTodayWidget;
import org.sitebay.android.ui.stats.refresh.lists.widget.today.TodayWidgetBlockListProviderFactory;
import org.sitebay.android.ui.stats.refresh.lists.widget.today.TodayWidgetListProvider;
import org.sitebay.android.ui.stats.refresh.lists.widget.views.StatsViewsWidget;
import org.sitebay.android.ui.stats.refresh.lists.widget.views.ViewsWidgetListProvider;
import org.sitebay.android.ui.stockmedia.StockMediaPickerActivity;
import org.sitebay.android.ui.stories.StoryComposerActivity;
import org.sitebay.android.ui.stories.intro.StoriesIntroDialogFragment;
import org.sitebay.android.ui.suggestion.SuggestionActivity;
import org.sitebay.android.ui.suggestion.SuggestionSourceSubcomponent.SuggestionSourceModule;
import org.sitebay.android.ui.suggestion.adapters.SuggestionAdapter;
import org.sitebay.android.ui.themes.ThemeBrowserActivity;
import org.sitebay.android.ui.themes.ThemeBrowserFragment;
import org.sitebay.android.ui.uploads.MediaUploadHandler;
import org.sitebay.android.ui.uploads.MediaUploadReadyProcessor;
import org.sitebay.android.ui.uploads.PostUploadHandler;
import org.sitebay.android.ui.uploads.UploadService;
import org.sitebay.android.ui.whatsnew.FeatureAnnouncementDialogFragment;
import org.sitebay.android.ui.whatsnew.FeatureAnnouncementListAdapter;
import org.sitebay.android.util.HtmlToSpannedConverter;
import org.sitebay.android.util.WPWebViewClient;
import org.sitebay.android.util.image.getters.WPCustomImageGetter;

import javax.inject.Singleton;

import dagger.BindsInstance;
import dagger.Component;
import dagger.android.AndroidInjector;
import dagger.android.support.AndroidSupportInjectionModule;

@Singleton
@Component(modules = {
        ApplicationModule.class,
        AppConfigModule.class,
        OkHttpClientModule.class,
        ReleaseNetworkModule.class,
        LegacyModule.class,
        ReleaseToolsModule.class,
        DatabaseModule.class,
        AndroidSupportInjectionModule.class,
        ViewModelModule.class,
        StatsModule.class,
        SupportModule.class,
        ThreadModule.class,
        TrackerModule.class,
        SuggestionSourceModule.class,
        ExperimentModule.class,
        // Login flow library
        LoginAnalyticsModule.class,
        LoginFragmentModule.class,
        LoginServiceModule.class,
        CrashLoggingModule.class
})
public interface AppComponent extends AndroidInjector<WordPress> {
    @Override
    void inject(WordPress instance);

    void inject(WPMainActivity object);

    void inject(SiteCreationService object);

    void inject(UploadService object);

    void inject(MediaUploadHandler object);

    void inject(PostUploadHandler object);

    void inject(LoginActivity object);

    void inject(LoginEpilogueActivity object);

    void inject(LoginEpilogueFragment object);

    void inject(LoginMagicLinkInterceptActivity object);

    void inject(SignupEpilogueActivity object);

    void inject(SignupEpilogueFragment object);

    void inject(PostSignupInterstitialActivity object);

    void inject(SiteCreationActivity object);

    void inject(SiteCreationDomainsFragment object);

    void inject(SiteCreationLoginDetailsFragment object);

    void inject(SiteCreationPreviewFragment object);

    void inject(JetpackConnectionResultActivity object);

    void inject(StatsConnectJetpackActivity object);

    void inject(GCMMessageService object);

    void inject(GCMRegistrationIntentService object);

    void inject(DeepLinkingIntentReceiverActivity object);

    void inject(ShareIntentReceiverActivity object);

    void inject(ShareIntentReceiverFragment object);

    void inject(AddQuickPressShortcutActivity object);

    void inject(HelpActivity object);

    void inject(CommentDetailFragment object);

    void inject(CommentFullScreenDialogFragment object);

    void inject(EditCommentActivity object);

    void inject(CommentAdapter object);

    void inject(CommentsListFragment object);

    void inject(CommentsActivity object);

    void inject(CommentsDetailActivity object);

    void inject(MeFragment object);

    void inject(MyProfileActivity object);

    void inject(MyProfileFragment object);

    void inject(AccountSettingsFragment object);

    void inject(MySiteFragment object);

    void inject(SitePickerActivity object);

    void inject(SitePickerAdapter object);

    void inject(SiteSettingsFragment object);

    void inject(SiteSettingsInterface object);

    void inject(BlogPreferencesActivity object);

    void inject(AppSettingsFragment object);

    void inject(PeopleManagementActivity object);

    void inject(PeopleListFragment object);

    void inject(PersonDetailFragment object);

    void inject(RoleChangeDialogFragment object);

    void inject(PeopleInviteFragment object);

    void inject(RoleSelectDialogFragment object);

    void inject(PeopleInviteDialogFragment object);

    void inject(PlansActivity object);

    void inject(MediaBrowserActivity object);

    void inject(MediaGridFragment object);

    void inject(MediaPreviewActivity object);

    void inject(MediaPreviewFragment object);

    void inject(MediaSettingsActivity object);

    void inject(PhotoPickerActivity object);

    void inject(StockMediaPickerActivity object);

    void inject(SiteSettingsTagListActivity object);

    void inject(SiteSettingsTagDetailFragment object);

    void inject(PublicizeListActivity object);

    void inject(PublicizeWebViewFragment object);

    void inject(PublicizeDetailFragment object);

    void inject(PublicizeListFragment object);

    void inject(PublicizeButtonPrefsFragment object);

    void inject(EditPostActivity object);

    void inject(EditPostSettingsFragment object);

    void inject(PostsListActivity object);

    void inject(PagesActivity object);

    void inject(AuthorSelectionAdapter object);

    void inject(PostListFragment object);

    void inject(HistoryListFragment object);

    void inject(HistoryAdapter object);

    void inject(HistoryDetailContainerFragment object);

    void inject(NotificationsListFragment object);

    void inject(NotificationsListFragmentPage object);

    void inject(NotificationsSettingsFragment object);

    void inject(NotificationsDetailActivity object);

    void inject(NotificationsProcessingService object);

    void inject(NotificationsPendingDraftsReceiver object);

    void inject(NotificationsDetailListFragment object);

    void inject(ReaderCommentListActivity object);

    void inject(ReaderSubsActivity object);

    void inject(ReaderUpdateLogic object);

    void inject(ReaderPostDetailFragment object);

    void inject(ReaderPostListFragment object);

    void inject(ReaderCommentAdapter object);

    void inject(ReaderPostAdapter object);

    void inject(ReaderTagAdapter object);

    void inject(PlansListFragment object);

    void inject(ReaderSiteHeaderView object);

    void inject(ReaderSiteSearchResultView object);

    void inject(ReaderTagHeaderView object);

    void inject(ReaderPostDetailHeaderView object);

    void inject(ReaderExpandableTagsView object);

    void inject(ReaderLikingUsersView object);

    void inject(ReaderWebView object);

    void inject(ReaderSimplePostContainerView object);

    void inject(ReaderPostPagerActivity object);

    void inject(ReaderPostListActivity object);

    void inject(ReaderBlogFragment object);

    void inject(ReaderBlogAdapter object);

    void inject(ReaderCommentsPostHeaderView object);

    void inject(ReleaseNotesActivity object);

    void inject(WPWebViewActivity object);

    void inject(WPWebViewClient object);

    void inject(ThemeBrowserActivity object);

    void inject(NotesAdapter object);

    void inject(ThemeBrowserFragment object);

    void inject(MediaDeleteService object);

    void inject(SelectCategoriesActivity object);

    void inject(ReaderUserAdapter object);

    void inject(AddCategoryFragment object);

    void inject(HtmlToSpannedConverter object);

    void inject(PluginBrowserActivity object);

    void inject(ActivityLogListActivity object);

    void inject(ActivityLogListFragment object);

    void inject(ActivityLogDetailFragment object);

    void inject(ScanFragment object);

    void inject(ScanHistoryFragment object);

    void inject(ScanHistoryListFragment object);

    void inject(ThreatDetailsFragment object);

    void inject(PluginListFragment object);

    void inject(PluginDetailActivity object);

    void inject(SuggestionAdapter object);

    void inject(WordPressGlideModule object);

    void inject(QuickStartFullScreenDialogFragment object);

    void inject(QuickStartReminderReceiver object);

    void inject(MediaGridAdapter object);

    void inject(PagesFragment object);

    void inject(PageListFragment object);

    void inject(SearchListFragment object);

    void inject(PageParentFragment object);

    void inject(WPCustomImageGetter object);

    void inject(PublicizeAccountChooserListAdapter object);

    void inject(PublicizeConnectionAdapter object);

    void inject(PublicizeServiceAdapter object);

    void inject(JetpackRemoteInstallFragment jetpackRemoteInstallFragment);

    void inject(PlansListAdapter object);

    void inject(PlanDetailsFragment object);

    void inject(DomainSuggestionsFragment object);

    void inject(DomainRegistrationDetailsFragment object);

    void inject(StatsViewsWidget object);

    void inject(StatsAllTimeWidget object);

    void inject(StatsTodayWidget object);

    void inject(StatsMinifiedWidget object);

    void inject(ViewsWidgetListProvider object);

    void inject(AllTimeWidgetListProvider object);

    void inject(AllTimeWidgetBlockListProviderFactory object);

    void inject(TodayWidgetListProvider object);

    void inject(TodayWidgetBlockListProviderFactory object);

    void inject(StatsActivity object);

    void inject(StatsListFragment object);

    void inject(DomainRegistrationActivity object);

    void inject(EditPostPublishSettingsFragment object);

    void inject(PostDatePickerDialogFragment object);

    void inject(PostTimePickerDialogFragment object);

    void inject(PostNotificationScheduleTimeDialogFragment object);

    void inject(PublishNotificationReceiver object);

    void inject(MainBottomSheetFragment object);

    void inject(ModalLayoutPickerFragment object);

    void inject(HomePagePickerFragment object);

    void inject(SubfilterBottomSheetFragment object);

    void inject(AddContentAdapter object);

    void inject(LayoutsAdapter object);

    void inject(PageParentSearchFragment object);

    void inject(PrepublishingBottomSheetFragment object);

    void inject(PrepublishingHomeFragment object);

    void inject(PrepublishingHomeAdapter object);

    void inject(PrepublishingTagsFragment object);

    void inject(PostSettingsTagsFragment object);

    void inject(PrepublishingPublishSettingsFragment object);

    void inject(AppSettingsActivity object);

    void inject(FeatureAnnouncementDialogFragment object);

    void inject(FeatureAnnouncementListAdapter object);

    void inject(StoryComposerActivity object);

    void inject(StoriesIntroDialogFragment object);

    void inject(ReaderFragment object);

    void inject(ReaderDiscoverFragment object);

    void inject(ReaderSearchActivity object);

    void inject(ReaderInterestsFragment object);

    void inject(HomepageSettingsDialog object);

    void inject(CrashLogging object);

    void inject(AztecVideoLoader object);

    void inject(PhotoPickerFragment object);

    void inject(LoginPrologueFragment object);

    void inject(ReaderDiscoverLogic object);

    void inject(PostListCreateMenuFragment object);

    void inject(ReaderDiscoverJobService object);

    void inject(ReaderDiscoverService object);

    void inject(SuggestionActivity object);

    void inject(MediaPickerActivity object);

    void inject(MediaPickerFragment object);

    void inject(MediaUploadReadyProcessor object);

    void inject(PrepublishingCategoriesFragment object);

    void inject(PrepublishingAddCategoryFragment object);

    void inject(ActivityLogTypeFilterFragment object);

    void inject(ImprovedMySiteFragment object);

    void inject(BackupDownloadActivity object);

    void inject(RestoreActivity object);

    void inject(DynamicCardMenuFragment object);

    void inject(BackupDownloadFragment object);

    void inject(RestoreFragment object);

    void inject(EngagedPeopleListFragment object);

    void inject(SiteSettingsTimezoneBottomSheet object);

    void inject(LoginSiteCheckErrorFragment object);

    void inject(LoginNoSitesFragment object);

    void inject(UserProfileBottomSheetFragment object);

    void inject(EngagedPeopleListActivity object);

    void inject(UnifiedCommentsActivity object);

    void inject(UnifiedCommentListFragment object);

    void inject(UnifiedCommentListAdapter object);

    void inject(BloggingReminderBottomSheetFragment object);

    void inject(CategoriesListFragment object);

    void inject(LayoutPreviewFragment object);

    // Allows us to inject the application without having to instantiate any modules, and provides the Application
    // in the app graph
    @Component.Builder
    interface Builder {
        @BindsInstance
        AppComponent.Builder application(Application application);

        AppComponent build();
    }
}
