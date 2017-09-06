<?xml version="1.0"?>
<!--
********************************************************************************
*                  jMCS project ( http://www.jmmc.fr/dev/jmcs )
********************************************************************************
*  Copyright (c) 2013, CNRS. All rights reserved.
*
*  Redistribution and use in source and binary forms, with or without
*  modification, are permitted provided that the following conditions are met:
*      - Redistributions of source code must retain the above copyright
*        notice, this list of conditions and the following disclaimer.
*      - Redistributions in binary form must reproduce the above copyright
*        notice, this list of conditions and the following disclaimer in the
*        documentation and/or other materials provided with the distribution.
*      - Neither the name of the CNRS nor the names of its contributors may be
*        used to endorse or promote products derived from this software without
*        specific prior written permission.
*
*  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
*  AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
*  IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
*  ARE DISCLAIMED. IN NO EVENT SHALL CNRS BE LIABLE FOR ANY DIRECT, INDIRECT,
*  INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
*  LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
*  OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
*  LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
*  NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
*  EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
********************************************************************************
-->

<!--
NAME
jmcLatexIndex2HsTOC.xsl - convert the latex2html index file into java help TOC 
-->

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
xmlns:exslt="http://exslt.org/common"
xmlns:math="http://exslt.org/math"
xmlns:date="http://exslt.org/dates-and-times"
xmlns:func="http://exslt.org/functions"
xmlns:set="http://exslt.org/sets"
xmlns:str="http://exslt.org/strings"
xmlns:dyn="http://exslt.org/dynamic"
xmlns:saxon="http://icl.com/saxon"
xmlns:xalanredirect="org.apache.xalan.xslt.extensions.Redirect"
xmlns:xt="http://www.jclark.com/xt"
xmlns:libxslt="http://xmlsoft.org/XSLT/namespace"
xmlns:test="http://xmlsoft.org/XSLT/"
xmlns:xhtml="http://www.w3.org/1999/xhtml"
extension-element-prefixes="exslt math date func set str dyn saxon xalanredirect xt libxslt test"
exclude-result-prefixes="math str">
<xsl:output omit-xml-declaration="yes" indent="no"/>
<xsl:param name="directory"></xsl:param>
<xsl:template match="/">
<toc>
<xsl:apply-templates select="//xhtml:ul[not(./ancestor::xhtml:ul)]"/>
</toc>
</xsl:template>

<xsl:template match="xhtml:ul">
<xsl:for-each select="xhtml:li">
<xsl:element name="tocitem">
<xsl:attribute name="text">
<xsl:value-of select="normalize-space(xhtml:a)"/>
</xsl:attribute>
<xsl:attribute name="target">
    <xsl:if test="$directory">
        <xsl:value-of select="concat($directory,'.')"/>
    </xsl:if>
<xsl:value-of select="substring-before(xhtml:a/@href, '.htm')"/>
</xsl:attribute>
<xsl:value-of select="'&#10;'"/>
<xsl:apply-templates select="./xhtml:ul"/>
</xsl:element>
</xsl:for-each>
<xsl:value-of select="'&#10;'"/>
<xsl:value-of select="'&#10;'"/>
</xsl:template>

</xsl:stylesheet>

