<?xml version="1.0" encoding="UTF-8" ?>

<xsl:stylesheet version="2.0" 
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
	xmlns:xs="http://www.w3.org/2001/XMLSchema"
	xmlns:fn="http://www.w3.org/2005/xpath-functions"
	xmlns:xdt="http://www.w3.org/2005/xpath-datatypes"
	xmlns:err="http://www.w3.org/2005/xqt-errors"
	exclude-result-prefixes="xs xdt err fn">

	<xsl:output method="xml" encoding="utf-8" indent="yes" />
	
	<xsl:template match="/">
		<summaries>
			<xsl:apply-templates/>
		</summaries>
	</xsl:template>
	
	<xsl:template match="toc/section/item">
		<summary>
			<xsl:attribute name="shortName">
    				<xsl:value-of select="@topic" />
  			</xsl:attribute>
  			<xsl:attribute name="safeName">
    				<xsl:value-of select='@safeName' />
  			</xsl:attribute>
			<xsl:attribute name="apiJarName">
    				<xsl:value-of select='@apiJarName' />
  			</xsl:attribute>
			
			<xsl:if test="@extension">
				<xsl:attribute name="isExtension">
						<xsl:value-of select='@extension' />
				</xsl:attribute>
			</xsl:if>
			
			<xsl:apply-templates />
		</summary>
	</xsl:template>
	
	<xsl:template match="api">
		<xsl:variable name="apiName" select="text()"/>
		<xsl:apply-templates select="document('library.xml')/library/object[@name=$apiName]"/>
	</xsl:template>

	<xsl:template match="library/object">
		<xsl:copy-of select="."/>
	</xsl:template>

</xsl:stylesheet>
