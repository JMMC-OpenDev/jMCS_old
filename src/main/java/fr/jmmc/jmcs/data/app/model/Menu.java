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

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for Menu complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="Menu"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="menu" type="{}Menu" maxOccurs="unbounded" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="label" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="classpath" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="action" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="checkbox" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="radiogroup" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="accelerator" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="description" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="icon" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Menu", propOrder = {
    "menus"
})
public class Menu {

    @XmlElement(name = "menu")
    protected List<Menu> menus;
    @XmlAttribute(name = "label")
    protected String label;
    @XmlAttribute(name = "classpath", required = true)
    protected String classpath;
    @XmlAttribute(name = "action", required = true)
    protected String action;
    @XmlAttribute(name = "checkbox")
    protected String checkbox;
    @XmlAttribute(name = "radiogroup")
    protected String radiogroup;
    @XmlAttribute(name = "accelerator")
    protected String accelerator;
    @XmlAttribute(name = "description")
    protected String description;
    @XmlAttribute(name = "icon")
    protected String icon;

    /**
     * Gets the value of the menus property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the menus property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getMenus().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Menu }
     * 
     * 
     */
    public List<Menu> getMenus() {
        if (menus == null) {
            menus = new ArrayList<Menu>();
        }
        return this.menus;
    }

    public boolean isSetMenus() {
        return ((this.menus!= null)&&(!this.menus.isEmpty()));
    }

    public void unsetMenus() {
        this.menus = null;
    }

    /**
     * Gets the value of the label property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLabel() {
        return label;
    }

    /**
     * Sets the value of the label property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLabel(String value) {
        this.label = value;
    }

    public boolean isSetLabel() {
        return (this.label!= null);
    }

    /**
     * Gets the value of the classpath property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getClasspath() {
        return classpath;
    }

    /**
     * Sets the value of the classpath property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setClasspath(String value) {
        this.classpath = value;
    }

    public boolean isSetClasspath() {
        return (this.classpath!= null);
    }

    /**
     * Gets the value of the action property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAction() {
        return action;
    }

    /**
     * Sets the value of the action property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAction(String value) {
        this.action = value;
    }

    public boolean isSetAction() {
        return (this.action!= null);
    }

    /**
     * Gets the value of the checkbox property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCheckbox() {
        return checkbox;
    }

    /**
     * Sets the value of the checkbox property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCheckbox(String value) {
        this.checkbox = value;
    }

    public boolean isSetCheckbox() {
        return (this.checkbox!= null);
    }

    /**
     * Gets the value of the radiogroup property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRadiogroup() {
        return radiogroup;
    }

    /**
     * Sets the value of the radiogroup property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRadiogroup(String value) {
        this.radiogroup = value;
    }

    public boolean isSetRadiogroup() {
        return (this.radiogroup!= null);
    }

    /**
     * Gets the value of the accelerator property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAccelerator() {
        return accelerator;
    }

    /**
     * Sets the value of the accelerator property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAccelerator(String value) {
        this.accelerator = value;
    }

    public boolean isSetAccelerator() {
        return (this.accelerator!= null);
    }

    /**
     * Gets the value of the description property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the value of the description property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDescription(String value) {
        this.description = value;
    }

    public boolean isSetDescription() {
        return (this.description!= null);
    }

    /**
     * Gets the value of the icon property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getIcon() {
        return icon;
    }

    /**
     * Sets the value of the icon property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setIcon(String value) {
        this.icon = value;
    }

    public boolean isSetIcon() {
        return (this.icon!= null);
    }

}
