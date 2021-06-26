package org.sitebay.android.ui.accounts

import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.button.MaterialButton
import org.sitebay.android.R
import org.sitebay.android.WordPress
import org.sitebay.android.databinding.PostSignupInterstitialActivityBinding
import org.sitebay.android.ui.ActivityLauncher
import org.sitebay.android.ui.LocaleAwareActivity
import org.sitebay.android.viewmodel.accounts.PostSignupInterstitialViewModel
import org.sitebay.android.viewmodel.accounts.PostSignupInterstitialViewModel.NavigationAction
import org.sitebay.android.viewmodel.accounts.PostSignupInterstitialViewModel.NavigationAction.DISMISS
import org.sitebay.android.viewmodel.accounts.PostSignupInterstitialViewModel.NavigationAction.START_SITE_CONNECTION_FLOW
import org.sitebay.android.viewmodel.accounts.PostSignupInterstitialViewModel.NavigationAction.START_SITE_CREATION_FLOW
import javax.inject.Inject

class PostSignupInterstitialActivity : LocaleAwareActivity() {
    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory
    private lateinit var viewModel: PostSignupInterstitialViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (application as WordPress).component().inject(this)

        LoginFlowThemeHelper.injectMissingCustomAttributes(theme)

        viewModel = ViewModelProvider(this, viewModelFactory)
                .get(PostSignupInterstitialViewModel::class.java)
        val binding = PostSignupInterstitialActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        with(binding) {
            viewModel.onInterstitialShown()
            createNewSiteButton().setOnClickListener { viewModel.onCreateNewSiteButtonPressed() }
            addSelfHostedSiteButton().setOnClickListener { viewModel.onAddSelfHostedSiteButtonPressed() }
            dismissButton().setOnClickListener { viewModel.onDismissButtonPressed() }
        }

        viewModel.navigationAction.observe(this, { executeAction(it) })
    }

    private fun PostSignupInterstitialActivityBinding.createNewSiteButton() =
            root.findViewById<MaterialButton>(R.id.create_new_site_button)

    private fun PostSignupInterstitialActivityBinding.addSelfHostedSiteButton() =
            root.findViewById<MaterialButton>(R.id.add_self_hosted_site_button)

    private fun PostSignupInterstitialActivityBinding.dismissButton() =
            root.findViewById<MaterialButton>(R.id.dismiss_button)

    override fun onBackPressed() {
        viewModel.onBackButtonPressed()
    }

    private fun executeAction(navigationAction: NavigationAction) = when (navigationAction) {
        START_SITE_CREATION_FLOW -> startSiteCreationFlow()
        START_SITE_CONNECTION_FLOW -> startSiteConnectionFlow()
        DISMISS -> dismiss()
    }

    private fun startSiteCreationFlow() {
        ActivityLauncher.showMainActivityAndSiteCreationActivity(this)
        finish()
    }

    private fun startSiteConnectionFlow() {
        ActivityLauncher.addSelfHostedSiteForResult(this)
        finish()
    }

    private fun dismiss() {
        ActivityLauncher.viewReader(this)
        finish()
    }
}
