<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:output method="xml" cdata-section-elements="example" indent="yes" />

<!--**************
Setting Variables
**************-->
<xsl:variable name="libraryBasePath" select="//libraries/@basePath"/>
<xsl:variable name="documents">
	<xsl:apply-templates mode="copyDocument"/>
</xsl:variable>
<xsl:variable name="scrub" select="//scrubList/scrub/text()"/>
<xsl:variable name="libraryFileName" select="//settings/libraryName/text()"/>

<!--**************
Templates
**************-->
<!-- The main template -->
<xsl:template match="manifest">
	<library>
		<features>
			<xsl:apply-templates select="$documents/library[@isWhitelist='true']/*" mode="features"/>
		</features>
		<xsl:apply-templates select="$documents/library/*" mode="objCopy"/>
	</library>
</xsl:template>

<!-- Called by the variable "documents" to copy all library.xml to itself -->
<xsl:template match="libraryPath" mode="copyDocument">
	<xsl:apply-templates select="document(concat($libraryBasePath, text(), '/', $libraryFileName))" mode="copy"/>
</xsl:template>

<!-- Create feature node -->
<xsl:template match="*" mode="features"/>
<xsl:template match="class|namespace|object" mode="features">
	<xsl:choose>
		<xsl:when test="@whitelistName">
			<xsl:call-template name="writeFeatureTag">
				<xsl:with-param name="id" select="@whitelistName"/>
				<xsl:with-param name="comment" select="@comment"/>
			</xsl:call-template>
		</xsl:when>
		<xsl:otherwise>
			<xsl:call-template name="writeFeatureTag">
				<xsl:with-param name="id" select="@name"/>
				<xsl:with-param name="comment" select="@comment"/>
			</xsl:call-template>
		</xsl:otherwise>
	</xsl:choose>
</xsl:template>

<xsl:template name="writeFeatureTag">
	<xsl:param name="id"/>
	<xsl:param name="comment"/>
	<feature id="{$id}" required="true" version="1.0.0.0">
		<xsl:value-of select="$comment"/>
	</feature>
</xsl:template>
<!-- Change the name of all entities to object, but copy everything else -->
<xsl:template match="*" mode="objCopy"/>
<xsl:template match="class|namespace|object" mode="objCopy">
	<object>
		<xsl:copy-of select="@*"/>	
		<xsl:apply-templates mode="copy"/>
	</object>
</xsl:template>

<!-- Copy everything except the tests -->
<xsl:template match="@*|node()" mode="copy">
	<xsl:copy>
		<xsl:copy-of select="@*"/>
		<xsl:if test="empty(index-of($scrub, name()))">
			<xsl:apply-templates mode="copy"/>
		</xsl:if>
	</xsl:copy>
</xsl:template>
</xsl:stylesheet>