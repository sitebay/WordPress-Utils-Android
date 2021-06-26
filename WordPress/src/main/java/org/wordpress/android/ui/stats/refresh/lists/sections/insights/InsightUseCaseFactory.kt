package org.sitebay.android.ui.stats.refresh.lists.sections.insights

import org.sitebay.android.ui.stats.refresh.lists.sections.BaseStatsUseCase
import org.sitebay.android.ui.stats.refresh.lists.sections.BaseStatsUseCase.UseCaseMode

interface InsightUseCaseFactory {
    fun build(useCaseMode: UseCaseMode): BaseStatsUseCase<*, *>
}
