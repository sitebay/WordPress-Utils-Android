package org.wordpress.android.ui.sitecreation.domains

import android.content.Context
import android.os.Bundle
import android.text.TextUtils
import android.util.Patterns
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.nulabinc.zxcvbn.Zxcvbn
import org.wordpress.android.databinding.SiteCreationLoginDetailsScreenBinding
import org.wordpress.android.R
import org.wordpress.android.R.string
import org.wordpress.android.WordPress
import org.wordpress.android.databinding.SiteCreationFormScreenBinding
import org.wordpress.android.ui.accounts.HelpActivity
import org.wordpress.android.ui.sitecreation.SiteCreationBaseFormFragment
import org.wordpress.android.ui.sitecreation.misc.OnHelpClickedListener
import org.wordpress.android.ui.sitecreation.misc.SearchInputWithHeader
import org.wordpress.android.ui.sitecreation.misc.SiteCreationHeaderUiState
import org.wordpress.android.ui.utils.UiHelpers
import org.wordpress.android.ui.utils.UiString.UiStringRes
import org.wordpress.android.util.DisplayUtilsWrapper
import org.wordpress.android.util.ToastUtils

import javax.inject.Inject

class SiteCreationLoginDetailsFragment: SiteCreationBaseFormFragment() {
    private var searchInputWithHeader: SearchInputWithHeader? = null
    private lateinit var viewModel: SiteCreationLoginDetailsViewModel

    @Inject internal lateinit var viewModelFactory: ViewModelProvider.Factory
    @Inject internal lateinit var uiHelpers: UiHelpers
    @Inject internal lateinit var displayUtils: DisplayUtilsWrapper

    private var binding: SiteCreationLoginDetailsScreenBinding? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context !is LoginDetailsScreenListener) {
            throw IllegalStateException("Parent activity must implement LoginDetailsScreenListener.")
        }
        if (context !is OnHelpClickedListener) {
            throw IllegalStateException("Parent activity must implement OnHelpClickedListener.")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (requireActivity().application as WordPress).component().inject(this)
    }

    @LayoutRes
    override fun getContentLayout(): Int {
        return R.layout.site_creation_login_details_screen
    }

    private fun createHeaderUiState(): SiteCreationHeaderUiState? {
        return SiteCreationHeaderUiState(
                UiStringRes(string.about_me_hint),
                UiStringRes(string.activity_log_activity_type_error_subtitle)
        )
     }

    override fun setBindingViewStubListener(parentBinding: SiteCreationFormScreenBinding) {
        parentBinding.siteCreationFormContentStub.setOnInflateListener { _, inflated ->
            binding = SiteCreationLoginDetailsScreenBinding.bind(inflated)
        }
    }


    private fun SiteCreationLoginDetailsScreenBinding.initRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(activity, RecyclerView.VERTICAL, false)
    }

    override fun setupContent() {
        // Check if user has a plan
        // If not, detour to plan select
        binding?.let {
            it.initRecyclerView()
            initViewModel()
            it.root.findViewById<AppCompatButton>(R.id.create_site_button).setOnClickListener { view ->
                val wpBlogName = view.rootView.findViewById<TextView>(R.id.wpBlogNameInput).text.toString()
                val wpFirstName = view.rootView.findViewById<TextView>(R.id.wpFirstNameInput).text.toString()
                val wpLastName = view.rootView.findViewById<TextView>(R.id.wpLastNameInput).text.toString()
                val wpEmail = view.rootView.findViewById<TextView>(R.id.wpEmailInput).text.toString()
                val wpUsername = view.rootView.findViewById<TextView>(R.id.wpUsernameInput).text.toString()
                val wpPassword = view.rootView.findViewById<TextView>(R.id.wpPasswordInput).text.toString()

                val zxcvbn = Zxcvbn()
                val passCheck = zxcvbn.measure(wpPassword)
                val strength: Int = passCheck.score

                if (!isValidEmail(wpEmail)) {
                    ToastUtils.showToast(activity, "Please enter a valid email")
                } else if (strength <= 2) {
                    ToastUtils.showToast(activity, "Password not strong enough. " + passCheck.feedback.suggestions[0])
                } else if (!validateUsername(wpUsername)) {
                    ToastUtils.showToast(activity, "Please enter a valid username")
                } else if (wpBlogName == "") {
                    ToastUtils.showToast(activity, "Please enter a valid Blog Name")
                } else viewModel.createSiteBtnClicked(
                        wpBlogName,
                        wpFirstName,
                        wpLastName,
                        wpEmail,
                        wpUsername,
                        wpPassword
                )
            }
        }
    }

    fun validateUsername(username: String): Boolean {
        val regularExpression = """^(?=.{4,20}$)(?:[a-zA-Z\d]+(?:(?:\.|-|_)[a-zA-Z\d])*)+$""".toRegex()
        return username.matches(regularExpression)
    }


    fun initViewModel() {
        viewModel = ViewModelProvider(this@SiteCreationLoginDetailsFragment, viewModelFactory)
                .get(SiteCreationLoginDetailsViewModel::class.java)

        viewModel.onHelpClicked.observe(this@SiteCreationLoginDetailsFragment, {
            (requireActivity() as OnHelpClickedListener).onHelpClicked(HelpActivity.Origin.SITE_CREATION_DOMAINS)
        })

        // This sets the domain
        // delete suggestion thing and add a button to check at the end

        viewModel.createSiteBtnClicked.observe(this@SiteCreationLoginDetailsFragment, { wpValues ->
            wpValues?.let { (requireActivity() as LoginDetailsScreenListener).onCreateSiteSelected(wpValues) }
        })
    }

    override val screenTitle: String
        get() = arguments?.getString(EXTRA_SCREEN_TITLE)
                ?: throw IllegalStateException("Required argument screen title is missing.")

    override fun onHelp() {
        viewModel.onHelpClicked()
    }

    private fun isValidEmail(target: CharSequence?): Boolean {
        return !TextUtils.isEmpty(target) && Patterns.EMAIL_ADDRESS.matcher(target).matches()
    }


    override fun onDestroyView() {
        super.onDestroyView()
        searchInputWithHeader = null
        binding = null
    }


    companion object {
        const val TAG = "site_creation_login_details_fragment_tag"

        fun newInstance(screenTitle: String): SiteCreationLoginDetailsFragment {
            val fragment = SiteCreationLoginDetailsFragment()
            val bundle = Bundle()
            bundle.putString(EXTRA_SCREEN_TITLE, screenTitle)
            fragment.arguments = bundle
            return fragment
        }
    }
}

