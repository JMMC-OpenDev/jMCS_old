/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.jmcs.gui.task;

import fr.jmmc.jmcs.network.http.Http;

/**
 * Custom TaskSwingWorker implementation that handles properly http query cancellation
 * (abort ie socket close) made with the Http class:
 * @see Http
 *
 * @param <T> the result type returned by this {@code TaskSwingWorker}
 * 
 * @author Laurent BOURGES.
 */
public abstract class HttpTaskSwingWorker<T> extends TaskSwingWorker<T> {

    /**
     * Create a new HttpTaskSwingWorker instance
     * @param task related task
     */
    public HttpTaskSwingWorker(final Task task) {
        super(task);
    }

    /**
     * Perform cancellation preparation to abort any Http request
     * @see Http#abort(java.lang.String) 
     */
    @Override
    protected final void beforeCancel() {
        final String threadName = getThreadName();
        if (threadName != null) {
            // background task in progress:
            Http.abort(threadName);
        }
    }

}
