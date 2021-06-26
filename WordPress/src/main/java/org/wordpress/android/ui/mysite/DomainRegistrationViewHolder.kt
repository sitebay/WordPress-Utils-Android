package org.sitebay.android.ui.mysite

import android.view.ViewGroup
import org.sitebay.android.databinding.DomainRegistrationBlockBinding
import org.sitebay.android.ui.mysite.MySiteItem.DomainRegistrationBlock
import org.sitebay.android.util.viewBinding

class DomainRegistrationViewHolder(
    parent: ViewGroup
) : MySiteItemViewHolder<DomainRegistrationBlockBinding>(parent.viewBinding(DomainRegistrationBlockBinding::inflate)) {
    fun bind(item: DomainRegistrationBlock) = with(binding) {
        mySiteRegisterDomainCta.setOnClickListener { item.onClick.click() }
    }
}
