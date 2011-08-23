<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
     xmlns:xs="http://www.w3.org/2001/XMLSchema"
     version="2.0">
	<xsl:import href="common.xslt"/>
	
	<xsl:param name="outputFolder">jsdocs_output</xsl:param>

	<xsl:output method="html" encoding="utf-8" indent="yes" />
	<xsl:preserve-space elements="a root"/>
	
	<xsl:template match="//summaries">
		<xsl:apply-templates />
	</xsl:template>
	
	<!-- ================================================================== -->
	<!-- Apply styles for namespace definitions                             -->
	<!-- ================================================================== -->
	<xsl:template match="summary">
		<xsl:result-document href="{$outputFolder}/Summary_{@safeName}.html">
		<html>
			<xsl:call-template name="head"/>
			<body text="#000000" bgColor="#ffffff" onload="setDownloadLink();">
				<xsl:call-template name="title"/>
				
				<xsl:if test="@isExtension">
					<xsl:call-template name="download-link"/>
				</xsl:if>
				
				<xsl:for-each select="object">
					<xsl:call-template name="summary">
						<xsl:with-param name="href">
							<xsl:value-of select="@name"/>.html</xsl:with-param>
						<xsl:with-param name="header">true</xsl:with-param>
					</xsl:call-template>
				</xsl:for-each>
			</body>
		</html>
		</xsl:result-document>
	</xsl:template>
	
	<xsl:template name="download-link">
	</xsl:template>

	
	<!-- ================================================================== -->
	<!-- This template will display the title of the page                   -->
	<!-- ================================================================== -->
	<xsl:template name="title">
		<h1>
			<xsl:value-of select="@shortName"/> API Summary
		</h1>
		<p>
      This is the complete list of <xsl:value-of select="@shortName"/> APIs. Click the object names to see the details.
      <br/>
		</p>
	</xsl:template>
</xsl:stylesheet>
