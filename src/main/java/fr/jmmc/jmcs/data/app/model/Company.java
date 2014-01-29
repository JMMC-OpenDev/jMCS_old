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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for Company complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="Company">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="short_name" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="logo_resource" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="homepage_url" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="legal_name" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="user_support_url" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="feedback_form_url" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Company", propOrder = {
    "shortName",
    "logoResource",
    "homepageUrl",
    "legalName",
    "userSupportUrl",
    "feedbackFormUrl"
})
public class Company {

    @XmlElement(name = "short_name", required = true)
    protected String shortName;
    @XmlElement(name = "logo_resource", required = true)
    protected String logoResource;
    @XmlElement(name = "homepage_url", required = true)
    protected String homepageUrl;
    @XmlElement(name = "legal_name")
    protected String legalName;
    @XmlElement(name = "user_support_url")
    protected String userSupportUrl;
    @XmlElement(name = "feedback_form_url")
    protected String feedbackFormUrl;

    /**
     * Gets the value of the shortName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getShortName() {
        return shortName;
    }

    /**
     * Sets the value of the shortName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setShortName(String value) {
        this.shortName = value;
    }

    public boolean isSetShortName() {
        return (this.shortName!= null);
    }

    /**
     * Gets the value of the logoResource property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLogoResource() {
        return logoResource;
    }

    /**
     * Sets the value of the logoResource property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLogoResource(String value) {
        this.logoResource = value;
    }

    public boolean isSetLogoResource() {
        return (this.logoResource!= null);
    }

    /**
     * Gets the value of the homepageUrl property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getHomepageUrl() {
        return homepageUrl;
    }

    /**
     * Sets the value of the homepageUrl property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setHomepageUrl(String value) {
        this.homepageUrl = value;
    }

    public boolean isSetHomepageUrl() {
        return (this.homepageUrl!= null);
    }

    /**
     * Gets the value of the legalName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLegalName() {
        return legalName;
    }

    /**
     * Sets the value of the legalName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLegalName(String value) {
        this.legalName = value;
    }

    public boolean isSetLegalName() {
        return (this.legalName!= null);
    }

    /**
     * Gets the value of the userSupportUrl property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getUserSupportUrl() {
        return userSupportUrl;
    }

    /**
     * Sets the value of the userSupportUrl property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUserSupportUrl(String value) {
        this.userSupportUrl = value;
    }

    public boolean isSetUserSupportUrl() {
        return (this.userSupportUrl!= null);
    }

    /**
     * Gets the value of the feedbackFormUrl property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFeedbackFormUrl() {
        return feedbackFormUrl;
    }

    /**
     * Sets the value of the feedbackFormUrl property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFeedbackFormUrl(String value) {
        this.feedbackFormUrl = value;
    }

    public boolean isSetFeedbackFormUrl() {
        return (this.feedbackFormUrl!= null);
    }

}
