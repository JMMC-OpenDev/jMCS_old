/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.jmcs.gui.component;

/**
 * This class represents a message (message + state/level). 
 * It may be handled by a MessageContainer and displayed by a MessagePanel.
 * 
 * @author Laurent BOURGES, Guillaume MELLA
 */
public final class Message {

    /** Message levels */
    public enum Level {
        Debug,
        /** information */
        Information,
        /** warning */
        Warning,
        /** error */
        Error
    }
    /* members */
    /** message */
    private final String message;
    /** message level */
    private final Level level;
    /** TODO FIX JAVADOC of this member and methods that uses them: flag indicating if this message was logged in the warning log */
    private boolean logged = false;

    /**
     * Protected Constructor (default level is warning)
     * @param message message
     */
    Message(final String message) {
        this.message = message;
        this.level = Level.Warning;
    }

    /**
     * Protected Constructor
     * @param message message
     * @param level message level
     */
    Message(final String message, final Level level) {
        this.message = message;
        this.level = level;
    }

    /**
     * Return the message
     * @return message
     */
    public String getMessage() {
        return message;
    }

    /**
     * Return the message level 
     * @return message level 
     */
    public Level getLevel() {
        return level;
    }

    /**
     * Return the flag indicating if this message was logged in the warning log
     * @return flag indicating if this message was logged in the warning log
     */
    public boolean isLogged() {
        return logged;
    }

    /**
     * Define the flag indicating if this message was logged in the warning log
     * @param logged flag indicating if this message was logged in the warning log
     */
    public void setLogged(final boolean logged) {
        this.logged = logged;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        return hash;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        // Identity check:
        if (this == obj) {
            return true;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Message other = (Message) obj;
        if ((this.message == null) ? (other.getMessage() != null) : !this.message.equals(other.getMessage())) {
            return false;
        }
        if (this.level != other.getLevel()) {
            return false;
        }
        if (this.logged != other.isLogged()) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "WarningMessage{level=" + level + ", logged=" + logged + ", message='" + message + '}';
    }
}
