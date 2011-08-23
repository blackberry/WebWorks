<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

	<xsl:output method="html" encoding="utf-8"
	doctype-public="-//W3C//DTD XHTML 1.0 Transitional//EN"
	doctype-system="http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd"/>
  
	<xsl:preserve-space elements='a root'/>
	
	<xsl:template match="/">
		<html>
			<head>
				<style type="text/css">
					div.title {
						font-weight : bold;
						font-size: 12pt;
					}

					body, p, td, table, tr, .bodytext, .stepfield {
						font-size: 10pt;
						font-family: Verdana;
						line-height: 1.3;
						color: #484848;
						font-weight: normal;
					}

					A:link {text-decoration: underline; color: #484848}
					A:visited {text-decoration: underline; color: #484848}
					A:hover {text-decoration: underline; color: navy;}
				</style>
			</head>
			
			<body text="#000000" bgColor="#ffffff">
				<xsl:apply-templates select='toc/section' />
				<br/>
			</body>
		</html>
	</xsl:template>
	
	<xsl:template match="toc/section">
		<div class="title">
			<xsl:value-of select="@title"/>
		</div>
		
		<!--First see if there are any topics declared -->
		<xsl:if test="count(item) > 0">
			<table>
				<xsl:for-each select="item">
				<tr>
					<td >
					<xsl:choose>
						<xsl:when test="@external = 'true'">
							<a>
								<xsl:attribute name="href">
									<xsl:value-of select="@src"/>
								</xsl:attribute>
								<xsl:attribute name="target">summary</xsl:attribute>
								<xsl:value-of select="@topic"/>
							</a>
						</xsl:when>
						<xsl:otherwise>
							<a>
								<xsl:attribute name="href">Summary_<xsl:value-of select="@safeName"/>.html</xsl:attribute>
								<xsl:attribute name="target">summary</xsl:attribute>
								<xsl:value-of select="@topic"/>
							</a>
						</xsl:otherwise>
					</xsl:choose>
					</td>
				</tr>
				</xsl:for-each>
			</table>
			<br />
		</xsl:if>	
	</xsl:template>
</xsl:stylesheet>
