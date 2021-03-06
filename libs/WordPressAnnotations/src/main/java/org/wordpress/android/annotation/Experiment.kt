package org.sitebay.android.annotation

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
@MustBeDocumented
annotation class Experiment(val remoteField: String, val defaultVariant: String)
