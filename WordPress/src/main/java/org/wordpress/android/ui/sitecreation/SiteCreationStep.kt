package org.sitebay.android.ui.sitecreation

import org.sitebay.android.util.wizard.WizardStep
import javax.inject.Inject
import javax.inject.Singleton

enum class SiteCreationStep : WizardStep {
    SEGMENTS, DOMAINS, LOGIN_DETAILS, SITE_PREVIEW;

    companion object {
        fun fromString(input: String): SiteCreationStep {
            return when (input) {
                "site_creation_segments" -> SEGMENTS
                "site_creation_domains" -> DOMAINS
                "site_creation_login_details" -> LOGIN_DETAILS
                "site_creation_site_preview" -> SITE_PREVIEW
                else -> throw IllegalArgumentException("SiteCreationStep not recognized: \$input")
            }
        }
    }
}

@Singleton
class SiteCreationStepsProvider @Inject constructor() {
    fun getSteps(): List<SiteCreationStep> {
        return listOf(
                SiteCreationStep.fromString("site_creation_segments"),
                SiteCreationStep.fromString("site_creation_domains"),
                SiteCreationStep.fromString("site_creation_login_details"),
                SiteCreationStep.fromString("site_creation_site_preview")
        )
    }
}
