package org.sitebay.android.ui.stats.refresh.lists.sections.granular

import org.sitebay.android.fluxc.network.utils.StatsGranularity
import org.sitebay.android.ui.stats.refresh.lists.sections.BaseStatsUseCase
import org.sitebay.android.ui.stats.refresh.lists.sections.BaseStatsUseCase.UseCaseMode

interface GranularUseCaseFactory {
    fun build(granularity: StatsGranularity, useCaseMode: UseCaseMode): BaseStatsUseCase<*, *>
}
