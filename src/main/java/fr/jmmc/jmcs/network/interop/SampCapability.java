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

import java.util.HashMap;
import java.util.Map;

/**
 * Enumeration of all different SAMP capabilities, a.k.a mTypes.
 * 
 * @author Sylvain LAFRASSE, Guillaume MELLA, Laurent BOURGES.
 */
public enum SampCapability {

    // Standard SAMP mTypes
    // Hub Administrative SAMP capabilities:
    HUB_EVENT_SHUTDOWN("samp.hub.event.shutdown", ExclusionReason.SAMP_INTERNAL),
    HUB_EVENT_REGISTER("samp.hub.event.register", ExclusionReason.SAMP_INTERNAL),
    HUB_EVENT_UNREGISTER("samp.hub.event.unregister", ExclusionReason.SAMP_INTERNAL),
    HUB_EVENT_METADATA("samp.hub.event.metadata", ExclusionReason.SAMP_INTERNAL),
    HUB_EVENT_SUBSCRIPTIONS("samp.hub.event.subscriptions", ExclusionReason.SAMP_INTERNAL),
    HUB_DISCONNECT("samp.hub.disconnect", ExclusionReason.SAMP_INTERNAL),
    // Client Administrative SAMP capabilities:
    CLIENT_APP_PING("samp.app.ping", ExclusionReason.SAMP_INTERNAL),
    CLIENT_APP_STATUS("samp.app.status", ExclusionReason.SAMP_INTERNAL),
    CLIENT_EVENT_SHUTDOWN("samp.app.event.shutdown", ExclusionReason.SAMP_INTERNAL),
    CLIENT_EVENT_PROGRESS("samp.app.event.progress", ExclusionReason.SAMP_INTERNAL),
    // Application SAMP capabilities:
    /** Load VOTable MType */
    LOAD_VO_TABLE("table.load.votable"),
    /** Load fits table MType */
    LOAD_FITS_TABLE("table.load.fits"),
    /** Load fits image MType */
    LOAD_FITS_IMAGE("image.load.fits"),
    /** Load spectrum MType */
    LOAD_SPECTRUM("spectrum.load.ssa-generic"),
    /** Load BibCode MType */
    LOAD_BIBCODE("bibcode.load"),
    /** Highlight row MType */
    HIGHLIGHT_ROW("table.highlight.row", ExclusionReason.LIKELY_BROADCASTED),
    /** Select rows MType */
    SELECT_LIST("table.select.rowList", ExclusionReason.LIKELY_BROADCASTED),
    /** Point at given coordinates MType */
    POINT_COORDINATES("coord.pointAt.sky", ExclusionReason.LIKELY_BROADCASTED),
    /** Get environment variable MType */
    GET_ENV_VAR("client.env.get", ExclusionReason.SAMP_INTERNAL),
    // VOResource SAMP capabilities:
    /** VOResource list loading MType */
    VORESOURCE_LOAD_LIST("voresource.loadlist"),
    /** VOResource cone list loading MType */
    VORESOURCE_LOAD_LIST_CONE("voresource.loadlist.cone"),
    /** VOResource SIAP list loading MType */
    VORESOURCE_LOAD_LIST_SIAP("voresource.loadlist.siap"),
    /** VOResource SSAP list loading MType */
    VORESOURCE_LOAD_LIST_SSAP("voresource.loadlist.ssap"),
    /** VOResource TAP list loading MType */
    VORESOURCE_LOAD_LIST_TAP("voresource.loadlist.tap"),
    /** VOResource VOSpace list loading MType */
    VORESOURCE_LOAD_LIST_VOSPACE("voresource.loadlist.vospace"),
    // Private JMMC SAMP capabilities are prefixed with application name:
    /** JMMC SearchCal Start Query MType */
    APPLAUNCHERTESTER_TRY_LAUNCH("fr.jmmc.applaunchertester.try.launch"),
    /** JMMC SearchCal Start Query MType */
    SEARCHCAL_START_QUERY("fr.jmmc.searchcal.start.query"),
    /** JMMC LITpro open settings file MType */
    LITPRO_START_SETTING("fr.jmmc.litpro.start.setting"),
    /** JMMC LITpro load usermodel MType */
    LITPRO_LOAD_USERMODEL("fr.jmmc.litpro.load.usermodel"),
    /** OCA Pivot load star list MType */
    LOAD_STAR_LIST("starlist.load"),
    /** A2P2 load OB MType */
    LOAD_OB_DATA("ob.load.data"),
    /** Aladin script loading MType */
    ALADIN_LOAD_SCRIPT("script.aladin.send"),
    /** TOPCAT STIL loading MType */
    TOPCAT_LOAD_STIL("table.load.stil"),
    /** Undefined MType */
    UNKNOWN("UNKNOWN", ExclusionReason.SAMP_INTERNAL);
    /** Blanking value for undefined Strings (null, ...) */
    public static final String UNKNOWN_MTYPE = "UNKNOWN";
    /* members */
    /** Store the SAMP 'cryptic' mType */
    private final String _mType;
    /** true if the SAMP capability is highly likely to be broadcasted, false otherwise */
    private final ExclusionReason _exclusionReason;

    /**
     * Constructor
     * @param mType samp message type (MTYPE)
     */
    SampCapability(final String mType) {
        this(mType, ExclusionReason.NONE);
    }

    /**
     * Constructor
     * @param mType samp message type (MTYPE)
     * @param true if the SAMP capability is highly likely to be broadcasted, false otherwise
     */
    SampCapability(final String mType, final ExclusionReason exclusionReason) {
        _mType = (mType == null) ? UNKNOWN_MTYPE : mType;
        _exclusionReason = exclusionReason;
        SampCapabilityNastyTrick.TYPES.put(_mType, this);
    }

    /**
     * Return the samp message type (MTYPE)
     * @return samp message type (MTYPE)
     */
    public String mType() {
        return _mType;
    }

    /**
     * @return true if the SAMP capability is likely to be broadcasted, false otherwise.
     */
    public boolean isLikelyBroadcastable() {
        return (_exclusionReason == ExclusionReason.LIKELY_BROADCASTED);
    }

    /**
     * @return true if the SAMP capability is likely internal, false otherwise.
     */
    public boolean isLikelyInternal() {
        return (_exclusionReason == ExclusionReason.SAMP_INTERNAL);
    }

    /**
     * @return true if the SAMP capability is flagged whatsoever, false otherwise.
     */
    public boolean isFlagged() {
        return (_exclusionReason != ExclusionReason.NONE);
    }

    /**
     * Gives back the SAMP capability of the corresponding mType.
     *
     * For example:
     * SampCapability.fromMType("client.env.get") == SampCapability.GET_ENV_VAR;
     * SampCapability.fromMType("toto") == SampCapability.UNKNOWN;
     * SampCapability.fromMType(null) == SampCapability.UNKNOWN;
     *
     * @param mType mType of the sought SampCapability.
     *
     * @return a String containing the given catalog title, the reference if not found, SampCapability.UNKNOWN otherwise.
     */
    public static SampCapability fromMType(final String mType) {
        if (mType == null) {
            return UNKNOWN;
        }

        final SampCapability capability = SampCapabilityNastyTrick.TYPES.get(mType);
        if (capability == null) {
            return UNKNOWN;
        }

        return capability;
    }

    /**
     * For test and debug purpose only.
     * @param args unused
     */
    public static void main(String[] args) {
        // For each catalog in the enum
        for (SampCapability capability : SampCapability.values()) {
            String mType = capability.mType();
            System.out.println("Capability '" + capability + "' has mType '" + mType + "' (flag = " + capability._exclusionReason + ") : match '" + (capability == SampCapability.fromMType(mType) ? "OK" : "FAILED") + "'.");
        }

        SampCapability tmp;
        String mType;

        mType = "toto";
        tmp = SampCapability.fromMType(mType);
        System.out.println("'" + mType + "' => '" + tmp + "'.");
        mType = null;
        tmp = SampCapability.fromMType(mType);
        System.out.println("'" + mType + "' => '" + tmp + "'.");
    }

    /**
     * To get over Java 1.5 limitation prohibiting static members in enum (initialization order hazard).
     *
     * @sa http://www.velocityreviews.com/forums/t145807-an-enum-mystery-solved.html
     * @sa http://www.jroller.com/ethdsy/entry/static_fields_in_enum
     */
    private final static class SampCapabilityNastyTrick {

        /** cached map of SampCapability keyed by mType */
        static final Map<String, SampCapability> TYPES = new HashMap<String, SampCapability>(16);

        /**
         * Forbidden constructor : utility class
         */
        private SampCapabilityNastyTrick() {
            super();
        }
    }
}

enum ExclusionReason {

    LIKELY_BROADCASTED,
    SAMP_INTERNAL,
    NONE
}
