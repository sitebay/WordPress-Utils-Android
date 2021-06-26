package org.sitebay.android.ui.sitecreation.domains

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.TextView
import androidx.annotation.LayoutRes
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import org.sitebay.android.R
import org.sitebay.android.ui.sitecreation.domains.SiteCreationDomainsViewModel.DomainsListItemUiState
import org.sitebay.android.ui.sitecreation.domains.SiteCreationDomainsViewModel.DomainsListItemUiState.DomainsFetchSuggestionsErrorUiState
import org.sitebay.android.ui.sitecreation.domains.SiteCreationDomainsViewModel.DomainsListItemUiState.DomainsModelUiState
import org.sitebay.android.ui.utils.UiHelpers

sealed class SiteCreationLoginDetailsViewHolder(internal val parent: ViewGroup, @LayoutRes layout: Int) :
        RecyclerView.ViewHolder(
                LayoutInflater.from(parent.context).inflate(
                        layout,
                        parent,
                        false
                )
        ) {
    abstract fun onBind(uiState: DomainsListItemUiState)

    class DomainSuggestionItemViewHolder(
        parentView: ViewGroup,
        private val uiHelpers: UiHelpers
    ) : SiteCreationLoginDetailsViewHolder(parentView, R.layout.site_creation_domains_item) {
        private val container = itemView.findViewById<ViewGroup>(R.id.container)
        private val nameSuggestion = itemView.findViewById<TextView>(R.id.name_suggestion)
        private val domainSuggestion = itemView.findViewById<TextView>(R.id.domain_suggestion)
        private val suggestionRadioButton = itemView.findViewById<RadioButton>(R.id.domain_suggestion_radio_button)
        private val domainUnavailability = itemView.findViewById<TextView>(R.id.domain_unavailability)
        private var onDomainSelected: (() -> Unit)? = null

        init {
            suggestionRadioButton.buttonTintList = ContextCompat.getColorStateList(
                    parentView.context,
                    R.color.neutral_10_primary_40_selector
            )
            container.setOnClickListener {
                onDomainSelected?.invoke()
            }
        }

        override fun onBind(uiState: DomainsListItemUiState) {
            uiState as DomainsModelUiState
            if (uiState.clickable) {
                onDomainSelected = requireNotNull(uiState.onItemTapped) { "OnItemTapped is required." }
            }
            nameSuggestion.text = uiState.name
            domainSuggestion.text = uiState.domain
            suggestionRadioButton.isChecked = uiState.checked
            suggestionRadioButton.visibility = if (uiState.radioButtonVisibility) View.VISIBLE else View.INVISIBLE
            container.isEnabled = uiState.clickable
            uiHelpers.setTextOrHide(domainUnavailability, uiState.subTitle)
        }
    }
}
