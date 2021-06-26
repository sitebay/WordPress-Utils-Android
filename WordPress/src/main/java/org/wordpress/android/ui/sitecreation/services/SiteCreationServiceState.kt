package org.sitebay.android.ui.sitecreation.services

import org.sitebay.android.ui.sitecreation.services.SiteCreationServiceState.SiteCreationStep.FAILURE
import org.sitebay.android.ui.sitecreation.services.SiteCreationServiceState.SiteCreationStep.IDLE
import org.sitebay.android.ui.sitecreation.services.SiteCreationServiceState.SiteCreationStep.SUCCESS
import org.sitebay.android.util.AutoForeground

data class SiteCreationServiceState internal constructor(
    val step: SiteCreationStep,
    val payload: Any? = null
) : AutoForeground.ServiceState {
    override fun isIdle(): Boolean {
        return step == IDLE
    }

    override fun isInProgress(): Boolean {
        return step != IDLE && !isTerminal
    }

    override fun isError(): Boolean {
        return step == FAILURE
    }

    override fun isTerminal(): Boolean {
        return step == SUCCESS || isError
    }

    override fun getStepName(): String {
        return step.name
    }

    enum class SiteCreationStep {
        IDLE,
        CREATE_SITE,
        SUCCESS,
        FAILURE;
    }
}
