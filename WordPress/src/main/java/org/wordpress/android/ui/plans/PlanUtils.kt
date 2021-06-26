package org.sitebay.android.ui.plans

import org.sitebay.android.fluxc.model.PlanModel

fun getCurrentPlan(plans: List<PlanModel>?): PlanModel? = plans?.find { it.isCurrentPlan }

fun isDomainCreditAvailable(plans: List<PlanModel>?): Boolean = getCurrentPlan(plans)?.hasDomainCredit ?: false
