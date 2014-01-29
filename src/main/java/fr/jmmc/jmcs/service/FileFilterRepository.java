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
package fr.jmmc.jmcs.service;

import fr.jmmc.jmcs.util.GenericFileFilter;
import java.util.Arrays;
import java.util.HashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * FileFilterRepository singleton class.
 * 
 * @author Sylvain LAFRASSE, Laurent BOURGES, Guillaume MELLA.
 */
public final class FileFilterRepository {

    /** Logger */
    private static final Logger _logger = LoggerFactory.getLogger(FileFilterRepository.class.getName());
    /** Singleton instance */
    private static FileFilterRepository _instance = null;
    /** HashMap to associate mime type identifier keys to FileFilterRepository instances */
    private static final HashMap<Object, GenericFileFilter> _repository = new HashMap<Object, GenericFileFilter>(16);

    /** Hidden constructor */
    private FileFilterRepository() {
        super();
    }

    /** 
     * Return the singleton instance.
     * @return singleton instance 
     */
    public static synchronized FileFilterRepository getInstance() {
        // DO NOT MODIFY !!!
        if (_instance == null) {
            _instance = new FileFilterRepository();
        }

        return _instance;

        // DO NOT MODIFY !!!
    }

    /**
     * Register a file filter in the repository.
     *
     * @param mimeType the mime type identifier of the file.
     * @param fileExtension the file extensions associated to the mime type.
     * @param description the humanly readable description for the mime type.
     *
     * @return the previous registered file filter, null otherwise.
     */
    public GenericFileFilter put(final Object mimeType, final String fileExtension, final String description) {
        if (_logger.isDebugEnabled()) {
            _logger.debug("FileFilterRepository - put(mimeType = '{}', fileExtension = '{}', description = '{}')",
                    mimeType, fileExtension, description);
        }

        return put(mimeType, new String[]{fileExtension}, description);
    }

    /**
     * Register a file filter in the repository.
     *
     * @param mimeType the mime type identifier of the file.
     * @param fileExtensions an array of file extensions associated to the mime type.
     * @param description the humanly readable description for the mime type.
     *
     * @return the previous registered file filter, null otherwise.
     */
    public GenericFileFilter put(final Object mimeType, final String[] fileExtensions, final String description) {
        if (_logger.isDebugEnabled()) {
            _logger.debug("FileFilterRepository - put(mimeType = '{}', fileExtensions[] = '{}', description = '{}')",
                    mimeType, Arrays.toString(fileExtensions), description);
        }

        final GenericFileFilter filter = new GenericFileFilter(fileExtensions, description);

        final GenericFileFilter previousFilter = _repository.put(mimeType, filter);

        if (previousFilter == null) {
            _logger.trace("Registered '{}' filter for the first time.", mimeType);
        } else if (previousFilter != filter) {
            _logger.debug("Overwritten the previously registered '{}' file filter.", mimeType);
        } else {
            if (_logger.isDebugEnabled()) {
                _logger.debug("Registered '{}' mimeType associated with file extensions '{}' ({}) succesfully.",
                        mimeType, Arrays.toString(fileExtensions), description);
            }
        }

        return previousFilter;
    }

    /**
     * Return the previously registered file filter for the given mime type.
     *
     * @param mimeType the mime type identifier of the file.
     *
     * @return the retrieved registered file filter, null otherwise.
     */
    public static GenericFileFilter get(final Object mimeType) {
        final GenericFileFilter retrievedFilter = _repository.get(mimeType);

        if (retrievedFilter == null) {
            _logger.error("Cannot find '{}' file filter.", mimeType);
        } else {
            _logger.debug("Retrieved '{}' file filter succesfully.", mimeType);
        }

        return retrievedFilter;
    }

    /**
     * Return the content of the object as a String for output.
     *
     * @return the content of the object as a String for output.
     */
    @Override
    public String toString() {
        if (_repository == null) {
            return "No file filter registered yet.";
        }

        return _repository.toString();
    }
}
/*___oOo___*/
