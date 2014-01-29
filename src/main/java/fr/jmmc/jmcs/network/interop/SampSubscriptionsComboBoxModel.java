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
package fr.jmmc.jmcs.network.interop;

import javax.swing.DefaultComboBoxModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import org.astrogrid.samp.Client;
import org.astrogrid.samp.gui.SubscribedClientListModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provide a combo box model that contains the up-to-date list of SAMP applications capable of a given SAMP capability.
 * @author Sylvain Lafrasse, Guillaume Mella
 */
public class SampSubscriptionsComboBoxModel extends DefaultComboBoxModel {

    /** Logger */
    private final static Logger _logger = LoggerFactory.getLogger(SampSubscriptionsComboBoxModel.class.getName());
    /** default serial UID for Serializable interface */
    private static final long serialVersionUID = 1L;
    /* members */
    /** Contains the list of capable application for a given mType */
    private final SubscribedClientListModel _clientListModel;

    /**
     * Constructor.
     * @param sampCapability SAMP capability against which the combo box model should be sync.
     */
    public SampSubscriptionsComboBoxModel(final SampCapability sampCapability) {

        _clientListModel = SampManager.createSubscribedClientListModel(sampCapability.mType());
        _clientListModel.addListDataListener(new ListDataListener() {
            @Override
            public void contentsChanged(final ListDataEvent e) {
                _logger.trace("ListDataListener.contentsChanged");
                updateModelOnHubEvent();
            }

            @Override
            public void intervalAdded(final ListDataEvent e) {
                _logger.trace("ListDataListener.intervalAdded");
                updateModelOnHubEvent();
            }

            @Override
            public void intervalRemoved(final ListDataEvent e) {
                _logger.trace("ListDataListener.intervalRemoved");
                // note: this event is never invoked by JSamp code (1.3) !
                updateModelOnHubEvent();
            }
        });
    }

    /** Update the combo box model content with the refreshed list of capable applications. */
    private void updateModelOnHubEvent() {
        // First flush the combo box model
        removeAllElements();

        // Then fill it with the current list of capable clients
        final int size = _clientListModel.getSize();
        for (int i = 0; i < size; i++) {
            addElement((Client) _clientListModel.getElementAt(i));
        }
    }
}
