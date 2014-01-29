/*******************************************************************************
 *                 jMCS project ( http://www.jmmc.fr/dev/jmcs )
 *******************************************************************************
 * Copyright (c) 2013, CNRS. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     - Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     - Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     - Neither the name of the CNRS nor the names of its contributors may be
 *       used to endorse or promote products derived from this software without
 *       specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL CNRS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 * OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 ******************************************************************************/
package fr.jmmc.jmcs.util;

import java.util.Observable;

/**
 * This class enable a third class to be considered as Observable.
 * To delegate feature, the class just has to forward addObserver(Observer o) to this class and call
 * notifyObservers methods. The Observer can get the original modified object using getSource method
 * on the casted observable field.
 * 
 * @author Guillaume MELLA.
 */
public class ObservableDelegate extends Observable {

    /** object to be observed */
    private final Object _source;

    /**
     * Build a new delegate object to be observable.
     *
     * @param source the object to be observed;
     */
    public ObservableDelegate(final Object source) {
        super();
        _source = source;
    }

    /**
     * Notify observers without argument
     */
    @Override
    public void notifyObservers() {
        setChanged();
        super.notifyObservers();
    }

    /**
     * Notify observers with the given argument
     *
     * @param arg optional argument
     */
    @Override
    public void notifyObservers(final Object arg) {
        setChanged();
        super.notifyObservers(arg);
    }

    /**
     * Return the notified source
     * @return the source
     */
    public Object getSource() {
        return _source;
    }
}
