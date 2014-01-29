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
package com.yourcompany.example;

import fr.jmmc.jmcs.gui.action.ActionRegistrar;
import fr.jmmc.jmcs.gui.action.RegisteredAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class containing all application actions.
 */
public class Actions {

    /** Logger */
    private static final Logger _logger = LoggerFactory.getLogger(Actions.class.getName());

    public GenericLoggedAction _scaction1;
    public GenericLoggedAction _scaction2;
    public GenericLoggedAction _scaction3;
    public GenericLoggedAction _scaction5;
    public GenericLoggedAction _mfaction3;
    public GenericLoggedAction _mfaction4;
    public GenericLoggedAction _mfaction5;
    public GenericLoggedAction _radio1;
    public GenericLoggedAction _radio2;
    public GenericLoggedAction _radio3;

    /** Creates a new Actions object. */
    public Actions() {
        _logger.info(ActionRegistrar.getInstance().toString());

        _scaction1 = new GenericLoggedAction("scaction1");
        _scaction2 = new GenericLoggedAction("scaction2");
        _scaction3 = new GenericLoggedAction("scaction3");
        _scaction5 = new GenericLoggedAction("scaction5");

        _mfaction3 = new GenericLoggedAction("mfaction3");
        _mfaction4 = new GenericLoggedAction("mfaction4");
        _mfaction5 = new GenericLoggedAction("mfaction5");

        _radio1 = new GenericLoggedAction("radio1");
        _radio2 = new GenericLoggedAction("radio2");
        _radio3 = new GenericLoggedAction("radio3");

        _logger.info(ActionRegistrar.getInstance().toString());
    }

    protected class GenericLoggedAction extends RegisteredAction {

        String _fieldName = null;

        public GenericLoggedAction(String fieldName) {
            super(Actions.class.getName(), fieldName);
            _logger.info("GenericLoggedAction('" + fieldName + "').");
            _fieldName = fieldName;
        }

        @Override
        public void actionPerformed(java.awt.event.ActionEvent e) {
            _logger.info("GenericLoggedAction.actionPerformed('" + _fieldName + "').");
        }
    }
}
/*___oOo___*/
