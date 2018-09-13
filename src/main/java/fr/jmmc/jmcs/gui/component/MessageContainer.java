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

import fr.jmmc.jmcs.gui.component.Message.Level;
import java.util.ArrayList;
import java.util.List;

/**
 * This class is a container for messages produced while computation.
 * It stores Message type objects and may be displayed by a MessagePanel
 * 
 * @author Laurent BOURGES, Guillaume MELLA
 */
public final class MessageContainer {

    /** messages */
    private List<Message> messages = null;
    /** message level */
    private Level level = null;

    /**
     * Public constructor
     */
    public MessageContainer() {
        super();
    }

    /**
     * Add the messages of given container only if the new message does not already exist in this container
     * @param container messages to add
     */
    public void addMessages(final MessageContainer container) {
        if (container.hasMessages()) {
            for (Message message : container.getMessages()) {
                addMessage(message);
            }
        }
    }

    /**
     * Add the given message to the (warning) messages
     * @param msg message to add
     */
    public void addMessage(final String msg) {
        addMessage(new Message(msg));
    }

    /**
     * Add the given message to the debug messages
     * @param msg message to add
     */
    public void addDebugMessage(final String msg) {
        addMessage(new Message(msg, Message.Level.Debug));
    }

    /**
     * Add the given message to the information messages
     * @param msg message to add
     */
    public void addInformationMessage(final String msg) {
        addMessage(new Message(msg, Message.Level.Information));
    }

    /**
     * Add the given message to the warning messages
     * @param msg message to add
     */
    public void addWarningMessage(final String msg) {
        addMessage(new Message(msg, Message.Level.Warning));
    }

    /**
     * Add the given message to the error messages
     * @param msg message to add
     */
    public void addErrorMessage(final String msg) {
        addMessage(new Message(msg, Message.Level.Error));
    }

    /**
     * Add the given message to the messages (if not already present)
     * @param message Message to add
     */
    private void addMessage(final Message message) {
        if (this.messages == null) {
            this.messages = new ArrayList<Message>(4);
        }
        if (!this.messages.contains(message)) {
            this.messages.add(message);
          
            if ( message.getLevel() == null){
                // do nothing
            }else if( this.level == null){
                this.level=message.getLevel();
            } else {
               this.level = ( this.level.ordinal() < message.getLevel().ordinal() ) ? message.getLevel() : this.level ;
            }                                
        }
    }

    /**
     * Return true if there are messages
     * @return true if there are messages
     */
    public boolean hasMessages() {
        return this.messages != null && !this.messages.isEmpty();
    }

    /**
     * Return the highest level of messages ( Error &gt; Debug )
     * @return highest level of messages ( Error &gt; Debug ) or null if empty
     */
    public Level getLevel() {
        return this.level;
    }

    /**
     * Return the list of  messages
     * @return messages or null
     */
    public List<Message> getMessages() {
        return this.messages;
    }

    /** 
     * Clear messages     
     */
    public void clear() {
        if (this.messages != null) {
            this.messages.clear();
        }
    }
}
