/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
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
     * Return the highest level of messages ( Error > Debug )
     * @return highest level of messages ( Error > Debug ) or null if empty
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
