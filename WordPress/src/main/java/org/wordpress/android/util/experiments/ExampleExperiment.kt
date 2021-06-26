package org.sitebay.android.util.experiments

import javax.inject.Inject

class ExampleExperiment
@Inject constructor(
    exPlat: ExPlat
) : Experiment(
        "example_experiment",
        exPlat
)
