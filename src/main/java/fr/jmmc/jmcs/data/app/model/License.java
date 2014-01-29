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
package fr.jmmc.jmcs.data.app.model;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for License.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="License">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="AFL v2.1"/>
 *     &lt;enumeration value="Apache v2"/>
 *     &lt;enumeration value="BSD"/>
 *     &lt;enumeration value="Eclipse v1"/>
 *     &lt;enumeration value="GPL v2"/>
 *     &lt;enumeration value="GPL v3"/>
 *     &lt;enumeration value="LGPL"/>
 *     &lt;enumeration value="LGPL v2"/>
 *     &lt;enumeration value="LGPL v2.1"/>
 *     &lt;enumeration value="MIT"/>
 *     &lt;enumeration value="Proprietary"/>
 *     &lt;enumeration value="UNKNOWN"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "License")
@XmlEnum
public enum License {

    @XmlEnumValue("AFL v2.1")
    AFL_V_2_1("AFL v2.1"),
    @XmlEnumValue("Apache v2")
    APACHE_V_2("Apache v2"),
    BSD("BSD"),
    @XmlEnumValue("Eclipse v1")
    ECLIPSE_V_1("Eclipse v1"),
    @XmlEnumValue("GPL v2")
    GPL_V_2("GPL v2"),
    @XmlEnumValue("GPL v3")
    GPL_V_3("GPL v3"),
    LGPL("LGPL"),
    @XmlEnumValue("LGPL v2")
    LGPL_V_2("LGPL v2"),
    @XmlEnumValue("LGPL v2.1")
    LGPL_V_2_1("LGPL v2.1"),
    MIT("MIT"),
    @XmlEnumValue("Proprietary")
    PROPRIETARY("Proprietary"),
    UNKNOWN("UNKNOWN");
    private final String value;

    License(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static License fromValue(String v) {
        for (License c: License.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
