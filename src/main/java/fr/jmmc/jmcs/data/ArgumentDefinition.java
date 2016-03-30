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
package fr.jmmc.jmcs.data;

import fr.jmmc.jmcs.App;

/**
 * Argument description: name, exec mode (SHELL_MODE or GUI mode), help
 *
 * @author Jean-Philippe GROS.
 */
public final class ArgumentDefinition {

    private final String name;
    private final boolean hasArgument;
    private final App.ExecMode mode;
    private final String help;

    /**
     * Constructor
     * @param name argument's name.
     * @param hasArgument true if an argument value is required, false otherwise.
     * @param help argument's description displayed in the command-line help.
     * @param mode execution mode (GUI or TTY mode).
     */
    public ArgumentDefinition(String name, boolean hasArgument, App.ExecMode mode, String help) {
        this.name = name;
        this.hasArgument = hasArgument;
        this.mode = mode;
        this.help = help;
    }

    /** Return a String
     @return name
     */
    public String getName() {
        return name;
    }

    /** return true or false depending on hasArgument
     * @return hasArgument
     */
    public boolean hasArgument() {
        return hasArgument;
    }

    /** Return the mode
     * @return the mode
     */
    public App.ExecMode getMode() {
        return mode;
    }

    /** return the help
     * @return help
     */
    public String getHelp() {
        return help;
    }

    @Override
    public String toString() {
        return "ArgumentDefinition{" + "name=" + name + ", hasArgument=" + hasArgument
                + ", mode=" + mode + ", help=" + help + '}';
    }

}
