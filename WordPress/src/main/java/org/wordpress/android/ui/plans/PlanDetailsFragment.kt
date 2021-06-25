package org.wordpress.android.ui.plans

import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import android.widget.ToggleButton
import androidx.fragment.app.Fragment
import com.android.volley.VolleyError
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.wallet.AutoResolveHelper
import com.google.android.gms.wallet.IsReadyToPayRequest
import com.google.android.gms.wallet.PaymentDataRequest
import com.google.android.gms.wallet.PaymentsClient
import com.google.android.gms.wallet.Wallet
import com.google.android.gms.wallet.WalletConstants
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.stripe.android.GooglePayConfig
import com.stripe.android.PaymentConfiguration
import com.stripe.android.Stripe
import com.stripe.android.paymentsheet.PaymentOptionCallback
import com.stripe.android.paymentsheet.PaymentSheet
import com.stripe.android.paymentsheet.PaymentSheetResult
import com.stripe.android.paymentsheet.PaymentSheetResultCallback
import com.stripe.android.paymentsheet.model.PaymentOption
import com.wordpress.rest.RestRequest
import org.json.JSONArray
import org.json.JSONObject
import org.wordpress.android.R
import org.wordpress.android.WordPress
import org.wordpress.android.fluxc.model.plans.PlanOffersModel
import org.wordpress.android.ui.FullScreenDialogFragment.FullScreenDialogContent
import org.wordpress.android.ui.FullScreenDialogFragment.FullScreenDialogController
import org.wordpress.android.util.StringUtils
import org.wordpress.android.util.image.ImageManager
import org.wordpress.android.util.image.ImageType
import javax.inject.Inject

class PlanDetailsFragment : Fragment(), FullScreenDialogContent {
    private var plan: PlanOffersModel? = null
    @Inject lateinit var imageManager: ImageManager
    private lateinit var dialogController: FullScreenDialogController

    private lateinit var flowController: PaymentSheet.FlowController
    private lateinit var paymentMethodButton: Button
    private lateinit var buyButton: Button
    private var interval: String? = "month"
    companion object {
        const val EXTRA_PLAN = "EXTRA_PLAN"
        const val KEY_PLAN = "KEY_PLAN"

        fun newBundle(planOffersModel: PlanOffersModel): Bundle {
            val bundle = Bundle()
            bundle.putParcelable(EXTRA_PLAN, planOffersModel)
            return bundle
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        (requireActivity().application as WordPress).component().inject(this)

        plan = if (savedInstanceState != null) {
            savedInstanceState.getParcelable(KEY_PLAN)
        } else {
            arguments?.getParcelable(EXTRA_PLAN)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable(KEY_PLAN, plan)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.plan_details_fragment, container, false) as ViewGroup

        val buyPlanHeader = rootView.findViewById<TextView>(R.id.buy_plan_header)
        val planCostSubtitle = rootView.findViewById<TextView>(R.id.plan_cost_subtitle)
        val planIcon = rootView.findViewById<ImageView>(R.id.image_plan_icon)
        val planName = rootView.findViewById<TextView>(R.id.plan_name)
        val planTagline = rootView.findViewById<TextView>(R.id.plan_tagline)
        val featuresContainer = rootView.findViewById<ViewGroup>(R.id.plan_features_container)

        if (!TextUtils.isEmpty(plan!!.iconUrl)) {
            imageManager.loadIntoCircle(
                    planIcon, ImageType.PLAN,
                    StringUtils.notNullStr(plan!!.iconUrl)
            )
        }

        buyPlanHeader.text = "Buy Plan"
        planCostSubtitle.text = "${plan?.getFormattedCost("month", false)} per month"
        planName.text = plan!!.name
        planTagline.text = plan!!.tagline

        plan!!.features?.forEach { feature ->
            val view = inflater.inflate(R.layout.plan_feature_item, featuresContainer, false) as ViewGroup

            val featureTitle = view.findViewById<TextView>(R.id.feature_title)
            val featureDescription = view.findViewById<TextView>(R.id.feature_description)

            featureTitle.text = feature.name
            featureDescription.text = feature.description

            featuresContainer.addView(view)
        }
        val discountTV: TextView? = rootView.findViewById(R.id.discountText);

        val buyBtnView = inflater.inflate(R.layout.plan_buy_plan, featuresContainer, false) as ViewGroup
        featuresContainer.addView(buyBtnView)
        paymentMethodButton = rootView.findViewById(R.id.addCardButton)

        val planCost: TextView? = rootView.findViewById(R.id.planCost)
        planCost?.text = plan?.getFormattedCost("month", true)

        val taxCost: TextView? = rootView.findViewById(R.id.taxCost)
        taxCost?.text = plan?.calculateTaxFormatted(interval!!)

        val totalCostVal: TextView? = rootView.findViewById(R.id.totalCostVal)
        totalCostVal?.text = plan?.getFormattedCostWithTax(interval!!)

        val toggle: ToggleButton? = rootView.findViewById(R.id.planToggleButton)
        toggle?.setOnCheckedChangeListener { _, isChecked ->
            interval = if (isChecked) "year" else "month"
            if (interval == "year") {
                discountTV?.text = "Yearly discount applied"
            } else {
                discountTV?.text = "Switch to Yearly billing to get 2 months free!"
            }
            planCost?.text = plan?.getFormattedCost(interval!!, true)
            totalCostVal?.text = plan?.getFormattedCostWithTax(interval!!)
            taxCost?.text = plan?.calculateTaxFormatted(interval!!)
        }

        buyButton = rootView.findViewById(R.id.buyPlanButton)

        PaymentConfiguration.init(
                requireContext(),
                "pk_test_rIdCZTKO2nfUKzNrCzdXt5B800lOJxFDy3"

        )

        //paymentMethodButton.isEnabled = false
        //buyButton.isEnabled = false

        val paymentOptionCallback = PaymentOptionCallback { paymentOption ->
            onPaymentOption(paymentOption)
        }

        val paymentSheetResultCallback = PaymentSheetResultCallback { paymentSheetResult ->
            onPaymentSheetResult(paymentSheetResult)
        }


        flowController = PaymentSheet.FlowController.create(
                this,
                paymentOptionCallback,
                paymentSheetResultCallback
        )
        fetchInitData()
        return rootView
    }

    override fun onConfirmClicked(controller: FullScreenDialogController): Boolean {
        return true
    }

    override fun onDismissClicked(controller: FullScreenDialogController): Boolean {
        dialogController.dismiss()
        return true
    }

    override fun setController(controller: FullScreenDialogController) {
        dialogController = controller
    }


    private fun buyPlan() {
        var path = "/plans"

        val listener =  RestRequest.Listener { jsonObject: JSONObject? ->
            val gson = Gson()
            val type = object : TypeToken<Map<String?, String?>?>() {}.type
            val responseJson = gson.fromJson<Map<String, String>>(jsonObject.toString(), type)
            val customerId = responseJson["customer"].toString()
            val ephemeralKeySecret = responseJson["ephemeralKey"].toString()
            val setupIntentClientSecret = responseJson["clientSecret"].toString()

            Log.i(
                    "MYLOG SECRET",
                    "SECRET"
            )
            Log.i(
                    setupIntentClientSecret,
                    "SECRET"
            )
            Log.i(
                    customerId,
                    ephemeralKeySecret
            )
            configureFlowController(
                    setupIntentClientSecret,
                    customerId,
                    ephemeralKeySecret
            )
        }

        val errorListener = RestRequest.ErrorListener { volleyError: VolleyError? -> Log.e(
                "ERROR",
                volleyError.toString()
        ) }


        val json = JSONObject("{\"plan-type-name\":\"${plan?.shortName}\",\"additional-sites\":\"0\",\"interval\":\"$interval\",\"currency\":\"${plan?.currency}\"}")
        WordPress.getRestClientUtils().post(path, json, null, listener, errorListener)
    }


    private fun fetchInitData() {
        val listener =  RestRequest.Listener { jsonObject: JSONObject? ->
            val gson = Gson()
            val type = object : TypeToken<Map<String?, String?>?>() {}.type
            val responseJson = gson.fromJson<Map<String, String>>(jsonObject.toString(), type)
            val customerId = responseJson["customer"].toString()
            val ephemeralKeySecret = responseJson["ephemeralKey"].toString()
            val setupIntentClientSecret = responseJson["clientSecret"].toString()

            Log.i(
                    "MYLOG SECRET",
                    "SECRET"
            )
            Log.i(
                    setupIntentClientSecret,
                    "SECRET"
            )
            Log.i(
                    customerId,
                    ephemeralKeySecret
            )
            configureFlowController(
                    setupIntentClientSecret,
                    customerId,
                    ephemeralKeySecret
            )
        }
        val errorListener = RestRequest.ErrorListener { volleyError: VolleyError? -> Log.e(
                "ERROR",
                volleyError.toString()
        ) }

        var path = "me/cards"

        WordPress.getRestClientUtils().post(path, listener, errorListener)
    }

    private fun configureFlowController(
        setupIntentClientSecret: String,
        customerId: String,
        ephemeralKeySecret: String
    ) {
        val googlePayConfig = PaymentSheet.GooglePayConfiguration(
                environment = PaymentSheet.GooglePayConfiguration.Environment.Test,
                countryCode = "CA"
        )
        val configuration = PaymentSheet.Configuration(
                merchantDisplayName = "Site Bay Limited",
                customer = PaymentSheet.CustomerConfiguration(
                        id = customerId,
                        ephemeralKeySecret = ephemeralKeySecret
                ),
                googlePay = googlePayConfig
        )

        flowController.configureWithSetupIntent(
                setupIntentClientSecret = setupIntentClientSecret,
                configuration = configuration,
        ) { isReady, error ->
            if (isReady) {
                Log.i("MYLOG", "FLOW CONTROLLER CONFIG SUCCESS")
                onFlowControllerReady()
            } else {
                Log.i("MYLOG", error.toString())
                // handle FlowController configuration failure
            }
        }
    }

    private fun onFlowControllerReady() {
        Log.i("MYLOG", "FLOW CONTROLLER READY")
        paymentMethodButton.setOnClickListener {
            flowController.presentPaymentOptions()
        }
        buyButton.setOnClickListener {
            onCheckout()
        }
        paymentMethodButton.isEnabled = true
        onPaymentOption(flowController.getPaymentOption())
    }

    private fun onPaymentOption(paymentOption: PaymentOption?) {
        if (paymentOption != null) {
            paymentMethodButton.text = paymentOption.label
            paymentMethodButton.setCompoundDrawablesRelativeWithIntrinsicBounds(
                    paymentOption.drawableResourceId,
                    0,
                    0,
                    0
            )
            buyButton.isEnabled = true
        } else {
            paymentMethodButton.text = "Select"
            paymentMethodButton.setCompoundDrawablesRelativeWithIntrinsicBounds(
                    null,
                    null,
                    null,
                    null
            )
            buyButton.isEnabled = false
        }
    }

    private fun onCheckout() {
        Log.i("MYLOG", flowController.getPaymentOption().toString())
        flowController.confirm()
    }

    private fun onPaymentSheetResult(
        paymentSheetResult: PaymentSheetResult
    ) {
        when (paymentSheetResult) {
            is PaymentSheetResult.Canceled -> {
                Toast.makeText(
                        requireContext(),
                        "Payment Canceled",
                        Toast.LENGTH_LONG
                ).show()
            }
            is PaymentSheetResult.Failed -> {
                Toast.makeText(
                        requireContext(),
                        "Payment Failed. See logcat for details",
                        Toast.LENGTH_LONG
                ).show()
                Log.e("App", "Got error: ${paymentSheetResult.error}")
            }
            is PaymentSheetResult.Completed -> {
                Toast.makeText(
                        requireContext(),
                        "Payment Complete",
                        Toast.LENGTH_LONG
                ).show()
                buyPlan()
            }
        }
    }
}
