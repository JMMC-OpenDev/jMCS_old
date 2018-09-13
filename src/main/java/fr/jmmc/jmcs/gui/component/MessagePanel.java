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
package fr.jmmc.jmcs.gui.component;

import static fr.jmmc.jmcs.gui.component.Message.Level.Information;
import fr.jmmc.jmcs.gui.util.ResourceImage;
import fr.jmmc.jmcs.util.StringUtils;
import javax.swing.Icon;
import org.slf4j.Logger;

/**
 * This panel aims to display the content of a given MessageContainer.
 *
 * @author Guillaume MELLA
 */
public final class MessagePanel extends javax.swing.JPanel {

    /** default serial UID for Serializable interface */
    private static final long serialVersionUID = 1;

    /** Logger */
    private Logger logger = null;

    /**
     * Creates new form MessagePanel.
     */
    public MessagePanel() {
        initComponents();
    }

    /**
     * Provide a logger reference to log message content on update call.
     * @param l logger reference or null (to remove previous one)
     */
    public void setLogger(Logger l) {
        logger = l;
    }

    /**
     * update content of panel with icon and text associated to the given messages
     * @param messageContainer messages container
     */
    public void update(MessageContainer messageContainer) {
        Icon icon = null;
        String message = null;

        if (messageContainer.getLevel() != null) {
            switch (messageContainer.getLevel()) {
                case Information:
                    icon = ResourceImage.INFO_ICON.icon();
                    break;
                case Warning:
                    icon = ResourceImage.WARNING_ICON.icon();
                    break;
                case Error:
                    icon = ResourceImage.ERROR_ICON.icon();
                    break;
                default:
            }

            // update text content
            final StringBuilder sb = new StringBuilder(100 * messageContainer.getMessages().size());
            sb.append("<html>");

            String msg;
            for (Message m : messageContainer.getMessages()) {
                msg = m.getMessage();

                sb.append(msg).append("<br>");

                // avoid redundant logs of the same message :
                if (!m.isLogged()) {
                    msg = StringUtils.removeTags(msg);
                    // if logger is not null, log message :
                    if (logger != null) {
                        switch (m.getLevel()) {
                            case Debug:
                                logger.debug(msg);
                                break;
                            case Information:
                                logger.info(msg);
                                break;
                            case Warning:
                                logger.warn(msg);
                                break;
                            case Error:
                                logger.error(msg);
                                break;
                        }
                    }
                    // flag message as logged :
                    m.setLogged(true);
                }
            }

            sb.append("</html>");
            message = sb.toString();
        }

        messageLabel.setText(message);
        iconLabel.setIcon(icon);

    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        iconLabel = new javax.swing.JLabel();
        messageLabel = new javax.swing.JLabel();

        setLayout(new java.awt.GridBagLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        add(iconLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        add(messageLabel, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel iconLabel;
    private javax.swing.JLabel messageLabel;
    // End of variables declaration//GEN-END:variables
}
