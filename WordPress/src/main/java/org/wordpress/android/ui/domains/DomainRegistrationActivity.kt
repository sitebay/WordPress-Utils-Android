package org.sitebay.android.ui.domains

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import org.sitebay.android.R
import org.sitebay.android.WordPress
import org.sitebay.android.databinding.DomainSuggestionsActivityBinding
import org.sitebay.android.ui.LocaleAwareActivity
import org.sitebay.android.ui.ScrollableViewInitializedListener
import org.sitebay.android.ui.domains.DomainRegistrationActivity.DomainRegistrationPurpose.CTA_DOMAIN_CREDIT_REDEMPTION
import javax.inject.Inject

class DomainRegistrationActivity : LocaleAwareActivity(), ScrollableViewInitializedListener {
    enum class DomainRegistrationPurpose {
        AUTOMATED_TRANSFER,
        CTA_DOMAIN_CREDIT_REDEMPTION
    }

    companion object {
        const val DOMAIN_REGISTRATION_PURPOSE_KEY = "DOMAIN_REGISTRATION_PURPOSE_KEY"
    }

    @Inject internal lateinit var viewModelFactory: ViewModelProvider.Factory
    private lateinit var viewModel: DomainRegistrationMainViewModel
    private lateinit var domainRegistrationPurpose: DomainRegistrationPurpose
    private lateinit var binding: DomainSuggestionsActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (application as WordPress).component().inject(this)
        with(DomainSuggestionsActivityBinding.inflate(layoutInflater)) {
            setContentView(root)
            binding = this

            domainRegistrationPurpose = intent.getSerializableExtra(DOMAIN_REGISTRATION_PURPOSE_KEY)
                    as DomainRegistrationPurpose

            setSupportActionBar(toolbarDomain)
            supportActionBar?.let {
                it.setHomeButtonEnabled(true)
                it.setDisplayHomeAsUpEnabled(true)
            }
            setupViewModel()
        }
    }

    private fun setupViewModel() {
        viewModel = ViewModelProvider(this, viewModelFactory)
                .get(DomainRegistrationMainViewModel::class.java)
        viewModel.start()

        viewModel.domainSuggestionsVisible.observe(this, { isVisible ->
            if (isVisible == true) {
                var fragment = supportFragmentManager.findFragmentByTag(DomainSuggestionsFragment.TAG)
                if (fragment == null) {
                    fragment = DomainSuggestionsFragment.newInstance()
                    showFragment(
                            fragment,
                            DomainSuggestionsFragment.TAG,
                            slideIn = false,
                            isRootFragment = true
                    )
                }
            }
        })

        viewModel.selectedDomain.observe(this, { selectedDomain ->
            selectedDomain?.let {
                var fragment = supportFragmentManager.findFragmentByTag(
                        DomainRegistrationDetailsFragment.TAG
                )

                if (fragment == null) {
                    fragment = DomainRegistrationDetailsFragment.newInstance(it)
                    showFragment(fragment!!, DomainRegistrationDetailsFragment.TAG)
                }
            }
        })

        viewModel.domainRegistrationCompleted.observe(this, { event ->
            event?.let {
                if (shouldShowCongratsScreen()) {
                    var fragment = supportFragmentManager.findFragmentByTag(
                            DomainRegistrationResultFragment.TAG
                    )

                    if (fragment == null) {
                        fragment = DomainRegistrationResultFragment.newInstance(
                                it.domainName,
                                it.email
                        )
                        showFragment(fragment!!, DomainRegistrationResultFragment.TAG)
                    }
                } else {
                    val intent = Intent()
                    intent.putExtra(
                            DomainRegistrationResultFragment.RESULT_REGISTERED_DOMAIN_EMAIL,
                            it.email
                    )
                    setResult(Activity.RESULT_OK, intent)
                    finish()
                }
            }
        })
    }

    private fun showFragment(
        fragment: Fragment,
        tag: String,
        slideIn: Boolean = true,
        isRootFragment: Boolean = false
    ) {
        val transaction = supportFragmentManager.beginTransaction()

        if (slideIn) {
            transaction.setCustomAnimations(
                    R.anim.activity_slide_in_from_right, R.anim.activity_slide_out_to_left,
                    R.anim.activity_slide_in_from_left, R.anim.activity_slide_out_to_right
            )
        }
        if (!isRootFragment) {
            transaction.addToBackStack(null)
        }

        transaction.replace(R.id.fragment_container, fragment, tag).commit()
    }

    private fun shouldShowCongratsScreen(): Boolean {
        return domainRegistrationPurpose == CTA_DOMAIN_CREDIT_REDEMPTION
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onScrollableViewInitialized(containerId: Int) {
        binding.appbarMain.apply {
            if (containerId == R.id.domain_suggestions_list) {
                post {
                    isLiftOnScroll = false
                    setLifted(false)
                    elevation = 0F
                    requestLayout()
                }
            } else {
                post {
                    isLiftOnScroll = true
                    liftOnScrollTargetViewId = containerId
                    requestLayout()
                }
            }
        }
    }
}
