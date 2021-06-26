package org.sitebay.android.testing
/**
 * This annotation allows us to open some classes in debug builds only for mocking purposes while they are final in
 * release builds.
 */
@Target(AnnotationTarget.CLASS)
annotation class OpenForTesting
