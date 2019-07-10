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
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 *                 jMCS application meta data
 *             
 * 
 * <p>Java class for ApplicationData complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ApplicationData"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="company" type="{}Company"/&gt;
 *         &lt;element name="program" type="{}Program"/&gt;
 *         &lt;element name="compilation" type="{}Compilation"/&gt;
 *         &lt;element name="text" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="jnlp" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="sampdescription" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="logo_resource" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="authors" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="distribution" type="{}Distribution" minOccurs="0"/&gt;
 *         &lt;element name="dependences" type="{}Dependences" minOccurs="0"/&gt;
 *         &lt;element name="menubar" type="{}Menubar" minOccurs="0"/&gt;
 *         &lt;element name="releasenotes" type="{}ReleaseNotes"/&gt;
 *         &lt;element name="acknowledgment" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="link" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="iconlink" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="faqlink" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="rsslink" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="documentationlink" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ApplicationData", propOrder = {
    "company",
    "program",
    "compilation",
    "text",
    "jnlp",
    "sampdescription",
    "logoResource",
    "authors",
    "distribution",
    "dependences",
    "menubar",
    "releasenotes",
    "acknowledgment"
})
@XmlRootElement(name = "ApplicationData")
public class ApplicationData {

    @XmlElement(required = true)
    protected Company company;
    @XmlElement(required = true)
    protected Program program;
    @XmlElement(required = true)
    protected Compilation compilation;
    protected String text;
    protected String jnlp;
    protected String sampdescription;
    @XmlElement(name = "logo_resource")
    protected String logoResource;
    protected String authors;
    protected Distribution distribution;
    protected Dependences dependences;
    protected Menubar menubar;
    @XmlElement(required = true)
    protected ReleaseNotes releasenotes;
    protected String acknowledgment;
    @XmlAttribute(name = "link", required = true)
    protected String link;
    @XmlAttribute(name = "iconlink", required = true)
    protected String iconlink;
    @XmlAttribute(name = "faqlink")
    protected String faqlink;
    @XmlAttribute(name = "rsslink")
    protected String rsslink;
    @XmlAttribute(name = "documentationlink")
    protected String documentationlink;

    /**
     * Gets the value of the company property.
     * 
     * @return
     *     possible object is
     *     {@link Company }
     *     
     */
    public Company getCompany() {
        return company;
    }

    /**
     * Sets the value of the company property.
     * 
     * @param value
     *     allowed object is
     *     {@link Company }
     *     
     */
    public void setCompany(Company value) {
        this.company = value;
    }

    public boolean isSetCompany() {
        return (this.company!= null);
    }

    /**
     * Gets the value of the program property.
     * 
     * @return
     *     possible object is
     *     {@link Program }
     *     
     */
    public Program getProgram() {
        return program;
    }

    /**
     * Sets the value of the program property.
     * 
     * @param value
     *     allowed object is
     *     {@link Program }
     *     
     */
    public void setProgram(Program value) {
        this.program = value;
    }

    public boolean isSetProgram() {
        return (this.program!= null);
    }

    /**
     * Gets the value of the compilation property.
     * 
     * @return
     *     possible object is
     *     {@link Compilation }
     *     
     */
    public Compilation getCompilation() {
        return compilation;
    }

    /**
     * Sets the value of the compilation property.
     * 
     * @param value
     *     allowed object is
     *     {@link Compilation }
     *     
     */
    public void setCompilation(Compilation value) {
        this.compilation = value;
    }

    public boolean isSetCompilation() {
        return (this.compilation!= null);
    }

    /**
     * Gets the value of the text property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getText() {
        return text;
    }

    /**
     * Sets the value of the text property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setText(String value) {
        this.text = value;
    }

    public boolean isSetText() {
        return (this.text!= null);
    }

    /**
     * Gets the value of the jnlp property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getJnlp() {
        return jnlp;
    }

    /**
     * Sets the value of the jnlp property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setJnlp(String value) {
        this.jnlp = value;
    }

    public boolean isSetJnlp() {
        return (this.jnlp!= null);
    }

    /**
     * Gets the value of the sampdescription property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSampdescription() {
        return sampdescription;
    }

    /**
     * Sets the value of the sampdescription property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSampdescription(String value) {
        this.sampdescription = value;
    }

    public boolean isSetSampdescription() {
        return (this.sampdescription!= null);
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
     * Gets the value of the authors property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAuthors() {
        return authors;
    }

    /**
     * Sets the value of the authors property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAuthors(String value) {
        this.authors = value;
    }

    public boolean isSetAuthors() {
        return (this.authors!= null);
    }

    /**
     * Gets the value of the distribution property.
     * 
     * @return
     *     possible object is
     *     {@link Distribution }
     *     
     */
    public Distribution getDistribution() {
        return distribution;
    }

    /**
     * Sets the value of the distribution property.
     * 
     * @param value
     *     allowed object is
     *     {@link Distribution }
     *     
     */
    public void setDistribution(Distribution value) {
        this.distribution = value;
    }

    public boolean isSetDistribution() {
        return (this.distribution!= null);
    }

    /**
     * Gets the value of the dependences property.
     * 
     * @return
     *     possible object is
     *     {@link Dependences }
     *     
     */
    public Dependences getDependences() {
        return dependences;
    }

    /**
     * Sets the value of the dependences property.
     * 
     * @param value
     *     allowed object is
     *     {@link Dependences }
     *     
     */
    public void setDependences(Dependences value) {
        this.dependences = value;
    }

    public boolean isSetDependences() {
        return (this.dependences!= null);
    }

    /**
     * Gets the value of the menubar property.
     * 
     * @return
     *     possible object is
     *     {@link Menubar }
     *     
     */
    public Menubar getMenubar() {
        return menubar;
    }

    /**
     * Sets the value of the menubar property.
     * 
     * @param value
     *     allowed object is
     *     {@link Menubar }
     *     
     */
    public void setMenubar(Menubar value) {
        this.menubar = value;
    }

    public boolean isSetMenubar() {
        return (this.menubar!= null);
    }

    /**
     * Gets the value of the releasenotes property.
     * 
     * @return
     *     possible object is
     *     {@link ReleaseNotes }
     *     
     */
    public ReleaseNotes getReleasenotes() {
        return releasenotes;
    }

    /**
     * Sets the value of the releasenotes property.
     * 
     * @param value
     *     allowed object is
     *     {@link ReleaseNotes }
     *     
     */
    public void setReleasenotes(ReleaseNotes value) {
        this.releasenotes = value;
    }

    public boolean isSetReleasenotes() {
        return (this.releasenotes!= null);
    }

    /**
     * Gets the value of the acknowledgment property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAcknowledgment() {
        return acknowledgment;
    }

    /**
     * Sets the value of the acknowledgment property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAcknowledgment(String value) {
        this.acknowledgment = value;
    }

    public boolean isSetAcknowledgment() {
        return (this.acknowledgment!= null);
    }

    /**
     * Gets the value of the link property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLink() {
        return link;
    }

    /**
     * Sets the value of the link property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLink(String value) {
        this.link = value;
    }

    public boolean isSetLink() {
        return (this.link!= null);
    }

    /**
     * Gets the value of the iconlink property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getIconlink() {
        return iconlink;
    }

    /**
     * Sets the value of the iconlink property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setIconlink(String value) {
        this.iconlink = value;
    }

    public boolean isSetIconlink() {
        return (this.iconlink!= null);
    }

    /**
     * Gets the value of the faqlink property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFaqlink() {
        return faqlink;
    }

    /**
     * Sets the value of the faqlink property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFaqlink(String value) {
        this.faqlink = value;
    }

    public boolean isSetFaqlink() {
        return (this.faqlink!= null);
    }

    /**
     * Gets the value of the rsslink property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRsslink() {
        return rsslink;
    }

    /**
     * Sets the value of the rsslink property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRsslink(String value) {
        this.rsslink = value;
    }

    public boolean isSetRsslink() {
        return (this.rsslink!= null);
    }

    /**
     * Gets the value of the documentationlink property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDocumentationlink() {
        return documentationlink;
    }

    /**
     * Sets the value of the documentationlink property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDocumentationlink(String value) {
        this.documentationlink = value;
    }

    public boolean isSetDocumentationlink() {
        return (this.documentationlink!= null);
    }

}
