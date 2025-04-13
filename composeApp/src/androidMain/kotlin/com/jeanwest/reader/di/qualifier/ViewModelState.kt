package com.jeanwest.reader.di.qualifier

import javax.inject.Qualifier

/**
 * Qualifier annotation used to distinguish between different ViewModel state dependencies.
 *
 * This annotation serves as a qualifier for dependency injection, specifically when multiple
 * dependencies of the same type are available and you need to specify which one should be
 * injected into a particular ViewModel.  This helps to avoid ambiguity during dependency
 * resolution by the DI framework.
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ViewModelState