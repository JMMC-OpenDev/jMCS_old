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
package fr.jmmc.jmcs.gui.action;

import javax.swing.Action;
import javax.swing.KeyStroke;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Action class customized to auto-register in ActionRegistrar when created.
 * 
 * @author Sylvain LAFRASSE, Laurent BOURGES.
 */
public abstract class RegisteredAction extends ResourcedAction {

    /** default serial UID for Serializable interface */
    private static final long serialVersionUID = 1;
    /** Logger */
    private static final Logger _logger = LoggerFactory.getLogger(RegisteredAction.class.getName());
    /** Action Registrar */
    private static final ActionRegistrar _registrar = ActionRegistrar.getInstance();

    /**
     * Constructor, that automatically register the action in RegisteredAction.
     * Action name, icon, accelerator and description is first initiated using
     * fieldName to build a ResourcedAction.
     * @param classPath the path of the class containing the field pointing to
     * the action, in the form returned by 'getClass().getName();'.
     * @param fieldName the name of the field pointing to the action.
     */
    public RegisteredAction(final String classPath, final String fieldName) {
        super(fieldName);

        _registrar.put(classPath, fieldName, this);
    }

    /**
     * Constructor, that automatically register the action in RegisteredAction.
     * Action name, icon, accelerator and description is first initiated using
     * fieldName to build a ResourcedAction.
     * @param classPath the path of the class containing the field pointing to
     * the action, in the form returned by 'getClass().getName();'.
     * @param fieldName the name of the field pointing to the action.
     * @param deferedInitialization true indicates to perform deferred initialization i.e. after application startup
     */
    public RegisteredAction(final String classPath, final String fieldName, final boolean deferedInitialization) {
        super(fieldName);

        _registrar.put(classPath, fieldName, this);

        _registrar.flagAsDeferedInitAction(classPath, fieldName);
    }

    /**
     * Perform deferred initialization i.e. executed after the application startup.
     * This method must be overridden in sub classes
     */
    protected void performDeferedInitialization() {
        // not implemented
    }

    /**
     * Constructor, that automatically register the action in RegisteredAction,
     * and assign it a name.
     * Action name, icon, accelerator and description is first initiated following ResourcedAction.
     * Then actionName set or overwrite action name.
     * @param classPath the path of the class containing the field pointing to
     * the action, in the form returned by 'getClass().getName();'.
     * @param fieldName the name of the field pointing to the action.
     * @param actionName the name of the action.
     */
    public RegisteredAction(final String classPath, final String fieldName, final String actionName) {
        this(classPath, fieldName);

        // Define action name and accelerator
        putValue(Action.NAME, actionName);
    }

    /**
     * Constructor, that automatically register the action in RegisteredAction,
     * and assign it a name and an accelerator.
     * Action name, icon, accelerator and description is first initiated following ResourcedAction.
     * Then actionName and actionAccelerator set or overwrite action name and action accelerator.
     *
     * @param classPath the path of the class containing the field pointing to
     * the action, in the form returned by 'getClass().getName();'.
     * @param fieldName the name of the field pointing to the action.
     * @param actionName the name of the action.
     * @param actionAccelerator the accelerator of the action, like "ctrl Q".
     */
    public RegisteredAction(final String classPath, final String fieldName,
            final String actionName, final String actionAccelerator) {
        this(classPath, fieldName, actionName);

        // Define action name and accelerator
        putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(actionAccelerator));
    }

    /**
     * Flag the action as the one dedicated to handle Preference panel display.
     */
    public void flagAsPreferenceAction() {
        // Force the preference action name
        putValue(Action.NAME, "Preferences...");

        _registrar.putPreferenceAction(this);
    }

    /**
     * Flag the action as the one dedicated to file opening sequence.
     */
    public void flagAsOpenAction() {
        // Force the 'open' action name
        putValue(Action.NAME, "Open");

        // Force the 'open' keyboard shortcut
        putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("ctrl O"));

        _registrar.putOpenAction(this);
    }

    /**
     * Flag the action as the one dedicated to handle Quit sequence.
     */
    public void flagAsQuitAction() {
        // Force the 'quit' action name
        putValue(Action.NAME, "Quit");

        // Force the 'quit' keyboard shortcut
        putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("ctrl Q"));

        _registrar.putQuitAction(this);
    }
}
/*___oOo___*/
