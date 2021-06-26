package org.sitebay.android.ui.domains

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.core.text.HtmlCompat
import androidx.core.text.HtmlCompat.FROM_HTML_MODE_COMPACT
import androidx.fragment.app.Fragment
import org.sitebay.android.R
import org.sitebay.android.WordPress
import org.sitebay.android.databinding.DomainRegistrationResultFragmentBinding

class DomainRegistrationResultFragment : Fragment(R.layout.domain_registration_result_fragment) {
    private var domainName: String? = null
    private var email: String? = null

    companion object {
        private const val EXTRA_REGISTERED_DOMAIN_NAME = "extra_registered_domain_name"
        private const val EXTRA_REGISTERED_DOMAIN_EMAIL = "extra_registered_domain_email"
        const val RESULT_REGISTERED_DOMAIN_EMAIL = "RESULT_REGISTERED_DOMAIN_EMAIL"
        const val TAG = "DOMAIN_REGISTRATION_RESULT_FRAGMENT"

        fun newInstance(domainName: String, email: String?): DomainRegistrationResultFragment {
            val fragment = DomainRegistrationResultFragment()
            val bundle = Bundle()
            bundle.putString(EXTRA_REGISTERED_DOMAIN_NAME, domainName)
            bundle.putString(EXTRA_REGISTERED_DOMAIN_EMAIL, email)
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        domainName = arguments?.getString(EXTRA_REGISTERED_DOMAIN_NAME, "")
        email = arguments?.getString(EXTRA_REGISTERED_DOMAIN_EMAIL, "")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        checkNotNull((activity?.application as WordPress).component())
        with(DomainRegistrationResultFragmentBinding.bind(view)) {
            continueButton.setOnClickListener {
                val intent = Intent()
                intent.putExtra(RESULT_REGISTERED_DOMAIN_EMAIL, email)
                val nonNullActivity = requireActivity()
                nonNullActivity.setResult(RESULT_OK, intent)
                nonNullActivity.finish()
            }

            domainRegistrationResultMessage.text = HtmlCompat.fromHtml(
                    getString(
                            R.string.domain_registration_result_description,
                            domainName
                    ), FROM_HTML_MODE_COMPACT
            )
        }
    }
}
