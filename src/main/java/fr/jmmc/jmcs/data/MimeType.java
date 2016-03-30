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

import fr.jmmc.jmcs.service.FileFilterRepository;
import fr.jmmc.jmcs.util.FileUtils;
import fr.jmmc.jmcs.util.GenericFileFilter;
import java.io.File;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Mime type registry that are used by multiples applications using jMCS.
 * It is also possible to register the mime types specific to one application.
 *
 * @author Sylvain LAFRASSE, Guillaume MELLA, Laurent BOURGES.
 */
public final class MimeType {

    /** Mime type registry keyed by identifier ordered by insertion order */
    private final static Map<String, MimeType> _registry = new LinkedHashMap<String, MimeType>(32);

    /* Predefined Mime types */
    /** MimeType associated to VOTable */
    public final static MimeType VOTABLE = MimeType.add("VOTABLE", "application/x-votable+xml", "VOTable document", "vot", "xml");
    /** MimeType associated to SearchCal calibrator list */
    public final static MimeType SEARCHCAL_CALIBRATORLIST = MimeType.add("SEARCHCAL_CALIBRATORLIST", "application/x-searchcal+votable+xml", "SearchCal Calibrator List", "scvot.gz", "scvot");
    /** MimeType associated to Observation settings */
    public final static MimeType ASPRO_OBSERVATION = MimeType.add("ASPRO_OBSERVATION", "application/x-aspro+xml", "Aspro Observation Settings", "asprox");
    /** MimeType associated to XML LITpro settings */
    public final static MimeType LITPRO_SETTINGS = MimeType.add("LITPRO_SETTINGS", "application/vnd.jmmc.litpro+xml", "LITpro XML Settings", "litprox", "xml");
    /** MimeType associated to OiFits explorer collection */
    public final static MimeType OIFITS_EXPLORER_COLLECTION = MimeType.add("OIFITS_EXPLORER_COLLECTION", "application/x-oifits-explorer+xml", "OIFits Explorer Collection", "oixp");
    /** MimeType associated to P2PP Observing blocks */
    public final static MimeType OBX = MimeType.add("OBX", "application/obx", "Observing Blocks", "obx");
    /** MimeType associated to OIFITS format */
    public final static MimeType OIFITS = MimeType.add("OIFITS", "application/oifits", "Optical Interferometry FITS", "fits", "fits.gz", "oifits", "oifits.gz");
    /** MimeType associated to FITS format */
    public final static MimeType FITS_IMAGE = MimeType.add("FITS_IMAGE", "application/fits", "FITS Image", "fits", "fits.gz");
    /** MimeType associated to JPG documents */
    public final static MimeType JPG = MimeType.add("JPG", "image/jpeg", "Joint Photographic Experts Group", "jpg");
    /** MimeType associated to PDF documents */
    public final static MimeType PDF = MimeType.add("PDF", "application/pdf", "Portable Document Format", "pdf");
    /** MimeType associated to PNG documents */
    public final static MimeType PNG = MimeType.add("PNG", "image/x-png", "Portable Network Graphics", "png");
    /** MimeType associated to VEGA Star Lists */
    public final static MimeType STAR_LIST = MimeType.add("STAR_LIST", "text/plain", "Star Lists", "txt");
    /** MimeType associated to Character-Separated Values format */
    public final static MimeType CSV = MimeType.add("CSV", "text/csv", "CSV", "csv", "txt");
    /** MimeType associated to HTML format */
    public final static MimeType HTML = MimeType.add("HTML", "text/html", "HTML", "html");
    /** MimeType associated to Text files */
    public final static MimeType PLAIN_TEXT = MimeType.add("PLAIN_TEXT", "text/plain", "Text files", "txt");
    /** MimeType associated to URL */
    public final static MimeType URL = MimeType.add("URL", "text/plain", "URL", "url");
    // Members
    /** mime-type identifier */
    private final String _id;
    /** mime-type */
    private final String _mimeType;
    /** mime-type name */
    private final String _name;
    /** full mime-type description */
    private final String _fullDescription;
    /** list of accepted extensions */
    private final List<String> _extensions;

    /**
     * Factory pattern: add this mime type in the Mime type registry
     * @param identifier mime type identifier like "PLAIN_TEXT"
     * @param mimeType mime type as string like "text/plain"
     * @param name short description like "Text files"
     * @param extensions accepted extensions "txt"
     * @return new mime type
     * @throws IllegalArgumentException if the mime type is already present
     */
    public static MimeType add(final String identifier, final String mimeType, final String name, final String... extensions) {
        if (_registry.containsKey(identifier)) {
            throw new IllegalArgumentException("MimeType[" + identifier + "] already registered !");
        }

        final MimeType mimeTypeEntry = new MimeType(identifier, mimeType, name, extensions);
        _registry.put(identifier, mimeTypeEntry);
        FileFilterRepository.getInstance().put(identifier, extensions, mimeTypeEntry.getDescription());

        return mimeTypeEntry;
    }

    /**
     * @return the mime type associated to the given mime type identifier
     * @param identifier mime type identifier
     * @throws IllegalArgumentException if the mime type is NOT present
     */
    public static MimeType get(final String identifier) {
        final MimeType mimeTypeEntry = _registry.get(identifier);

        if (mimeTypeEntry == null) {
            throw new IllegalArgumentException("MimeType[" + identifier + "] not registered !");
        }

        return mimeTypeEntry;
    }

    /**
     * @return registered mime types as array
     */
    public static MimeType[] values() {
        final int len = _registry.size();

        final MimeType[] values = new MimeType[len];
        _registry.values().toArray(values);

        return values;
    }

    /**
     * Private constructor
     * @param identifier mime type identifier
     * @param mimeType mime type name
     * @param name short description
     * @param extensions accepted extensions
     */
    private MimeType(final String identifier, final String mimeType, final String name, final String... extensions) {
        _id = identifier;
        _mimeType = mimeType;
        _name = name;
        _fullDescription = name + ' ' + Arrays.toString(extensions);
        _extensions = Arrays.asList(extensions);
    }

    /**
     * @return mime-type identifier
     */
    public String getId() {
        return _id;
    }

    /**
     * @return mime-type name
     */
    public String getName() {
        return _name;
    }

    /**
     * @return short mime-type description
     */
    public String getDescription() {
        return _fullDescription;
    }

    /**
     * @return accepted extensions as list
     */
    public List<String> getExtensions() {
        return _extensions;
    }

    /**
     * @return first accepted extension
     */
    public String getExtension() {
        if (_extensions.isEmpty()) {
            return null;
        }
        return _extensions.get(0);
    }

    /**
     * @return mime-type
     */
    public String getMimeType() {
        return _mimeType;
    }

    /**
     * @return the retrieved registered file filter.
     */
    public GenericFileFilter getFileFilter() {
        return FileFilterRepository.get(getId());
    }

    /**
     * Check if the given file has an accepted extension.
     * If not, return a new file with the first accepted extension
     * @param file file to check
     * @return given file or new file with the first accepted extension
     */
    public File checkFileExtension(final File file) {
        // Use GenericFileFilter that supports multiple extensions (with dot):
        if (!FileFilterRepository.get(getId()).checkExtensions(file, false)) {
            // add or replace current extension by the first accepted extension:
            final String fileNamePart = FileUtils.getFileNameWithoutExtension(file);
            return new File(file.getParentFile(), fileNamePart + '.' + getExtension());
        }
        return file;
    }

    @Override
    public String toString() {
        return _mimeType + ", matching " + _fullDescription + " file extension(s)";
    }

    /**
     * For test and debug purpose only.
     * @param args unused
     */
    public static void main(String[] args) {
        // For each catalog in the enum
        for (MimeType mimeType : MimeType.values()) {
            System.out.println("MimeType '" + mimeType.getName() + "' = '" + mimeType.toString() + "'.");
        }
    }
}
