/*******************************************************************************
 *                 jMCS project ( http://www.jmmc.fr/dev/jmcs )
 *******************************************************************************
 * Copyright (c) 2016, CNRS. All rights reserved.
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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for Distribution complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="Distribution"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="application_data_file" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="public_url" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="beta_url" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="alpha_url" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Distribution", propOrder = {
    "applicationDataFile",
    "publicUrl",
    "betaUrl",
    "alphaUrl"
})
public class Distribution {

    @XmlElement(name = "application_data_file", required = true)
    protected String applicationDataFile;
    @XmlElement(name = "public_url", required = true)
    protected String publicUrl;
    @XmlElement(name = "beta_url")
    protected String betaUrl;
    @XmlElement(name = "alpha_url")
    protected String alphaUrl;

    /**
     * Gets the value of the applicationDataFile property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getApplicationDataFile() {
        return applicationDataFile;
    }

    /**
     * Sets the value of the applicationDataFile property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setApplicationDataFile(String value) {
        this.applicationDataFile = value;
    }

    public boolean isSetApplicationDataFile() {
        return (this.applicationDataFile!= null);
    }

    /**
     * Gets the value of the publicUrl property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPublicUrl() {
        return publicUrl;
    }

    /**
     * Sets the value of the publicUrl property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPublicUrl(String value) {
        this.publicUrl = value;
    }

    public boolean isSetPublicUrl() {
        return (this.publicUrl!= null);
    }

    /**
     * Gets the value of the betaUrl property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getBetaUrl() {
        return betaUrl;
    }

    /**
     * Sets the value of the betaUrl property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setBetaUrl(String value) {
        this.betaUrl = value;
    }

    public boolean isSetBetaUrl() {
        return (this.betaUrl!= null);
    }

    /**
     * Gets the value of the alphaUrl property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAlphaUrl() {
        return alphaUrl;
    }

    /**
     * Sets the value of the alphaUrl property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAlphaUrl(String value) {
        this.alphaUrl = value;
    }

    public boolean isSetAlphaUrl() {
        return (this.alphaUrl!= null);
    }

}
