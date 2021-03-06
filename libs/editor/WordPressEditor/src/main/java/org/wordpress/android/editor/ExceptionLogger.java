package org.sitebay.android.editor;

import androidx.core.util.Consumer;

public interface ExceptionLogger {
    Consumer<Exception> getExceptionLogger();
    Consumer<String> getBreadcrumbLogger();
}
