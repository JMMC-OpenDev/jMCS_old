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

import fr.jmmc.jmcs.data.preference.CommonPreferences;
import fr.jmmc.jmcs.data.preference.Preferences;
import fr.jmmc.jmcs.gui.component.ResizableTextViewFactory;
import org.apache.commons.lang.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;

/**
 * This class gathers few utility methods related to JVM (System or Runtime properties)
 * @author Laurent BOURGES.
 */
public final class JVMUtils {

    /** Logger */
    private static final Logger _logger = LoggerFactory.getLogger(JVMUtils.class.getName());

    /** minimal java version required for JMCS */
    public static final float JAVA_MIN_VERSION = 1.7f;

    /* JVM Heap information */
    /**
     * Return the memory information (heap) 
     * @return memory information as string
     */
    public static String getMemoryInfo() {
        return String.format("JVM Memory: free=%,d - total=%,d - max=%,d bytes", freeMemory(), totalMemory(), maxMemory());
    }

    /**
     * Returns the amount of free memory in the Java Virtual Machine.
     * Calling the
     * <code>gc</code> method may result in increasing the value returned
     * by <code>freeMemory.</code>
     *
     * @return  an approximation to the total amount of memory currently
     *          available for future allocated objects, measured in bytes.
     */
    public static long freeMemory() {
        return Runtime.getRuntime().freeMemory();
    }

    /**
     * Returns the total amount of memory in the Java virtual machine.
     * The value returned by this method may vary over time, depending on
     * the host environment.
     * <p>
     * Note that the amount of memory required to hold an object of any
     * given type may be implementation-dependent.
     *
     * @return  the total amount of memory currently available for current
     *          and future objects, measured in bytes.
     */
    public static long totalMemory() {
        return Runtime.getRuntime().totalMemory();
    }

    /**
     * Returns the maximum amount of memory that the Java virtual machine will
     * attempt to use.  If there is no inherent limit then the value {@link
     * java.lang.Long#MAX_VALUE} will be returned.
     *
     * @return  the maximum amount of memory that the virtual machine will
     *          attempt to use, measured in bytes
     */
    public static long maxMemory() {
        return Runtime.getRuntime().maxMemory();
    }

    /* CPU information */
    /**
     * Returns the number of processors available to the Java virtual machine.
     *
     * <p> This value may change during a particular invocation of the virtual
     * machine.  Applications that are sensitive to the number of available
     * processors should therefore occasionally poll this property and adjust
     * their resource usage appropriately. </p>
     *
     * @return  the maximum number of processors available to the virtual
     *          machine; never smaller than one
     */
    public static int availableProcessors() {
        return Runtime.getRuntime().availableProcessors();
    }

    /**
     * Check the JVM version and show a warning message if it is unsupported
     */
    public static boolean showUnsupportedJdkWarning() {
        final float javaRuntime = SystemUtils.JAVA_VERSION_FLOAT;
        final String jvmVendor = SystemUtils.JAVA_VM_VENDOR;
        final String jvmName = SystemUtils.JAVA_VM_NAME;
        final String javaVersion = SystemUtils.JAVA_VERSION;
        final String jvmVersion = SystemUtils.JAVA_VM_VERSION;
        final String jvmHome = SystemUtils.getJavaHome().getAbsolutePath();

        _logger.info("JVM runtime environment: {} {} ({} {}) [{}]", jvmVendor, jvmName, javaVersion, jvmVersion, jvmHome);

        Level level = null;
        int timeoutMillis = 0; // disabled by default
        String message = "<HTML><BODY>";

        if (javaRuntime < JAVA_MIN_VERSION) {
            level = Level.ERROR;
            _logger.warn("Detected JDK {} runtime environment: {} {} {} - {}", javaRuntime, jvmVendor, jvmName, javaVersion, jvmVersion);

            message += "<FONT COLOR='RED'>WARNING</FONT>: ";
            message += "Your Java Virtual Machine is not supported anymore.";
        } else if (jvmName != null && jvmName.toLowerCase().contains("openjdk") && (javaRuntime < 1.8f)) {
            level = Level.WARN;
            _logger.warn("Detected OpenJDK runtime environment: {} {} {} - {}", jvmVendor, jvmName, javaVersion, jvmVersion);

            message += "<FONT COLOR='ORANGE'>WARNING</FONT>: ";
            message += "Your Java Virtual Machine is OpenJDK 7, which may have known bugs (SWING look and feel,"
                    + " fonts, time zone, PDF issues...) on several Linux distributions.";

            // If OpenJDK 1.7+, set auto-hide delay to 5s:
            timeoutMillis = 5000;
        }

        if (level != null) {
            message += "<BR><BR><B>JMMC recommends</B> Java 8, available at:"
                    + "<BR><CENTER><A HREF='http://www.java.com/'>http://www.java.com/</A></CENTER>"
                    + "<BR><BR><I>Your current JVM Information :</I><BR><TT>"
                    + "java.vm.name    = '" + jvmName + "'<BR>"
                    + "java.vm.vendor  = '" + jvmVendor + "'<BR>"
                    + "java.version    = '" + javaVersion + "'<BR>"
                    + "java.vm.version = '" + jvmVersion + "'<BR>"
                    + "Java Home:<BR>'" + jvmHome + "'" + "</TT>";
            message += "</BODY></HTML>";

            final Preferences preferences = (level == Level.WARN) ? CommonPreferences.getInstance() : null;

            // modal dialog (wait)
            ResizableTextViewFactory.createHtmlWindow(message, "Deprecated Java environment detected !",
                    true, timeoutMillis, preferences, CommonPreferences.SHOW_UNSUPPORTED_JDK_WARNING);
        }

        return (level != Level.ERROR);
    }

    /**
     * Forbidden constructor
     */
    private JVMUtils() {
        super();
    }
}
