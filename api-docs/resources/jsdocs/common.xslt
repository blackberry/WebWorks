<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  
  <xsl:output method="html" encoding="utf-8"
  doctype-public="-//W3C//DTD XHTML 1.0 Transitional//EN"
  doctype-system="http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd"/>
  
  <xsl:preserve-space elements='root'/>
  <!-- ================================================================== -->
  <!-- Main header with the styles                                        -->
  <!-- ================================================================== -->
  <xsl:template name="head">
    <head>
      <title>
        <xsl:value-of select="@shortName"/>&#160;Summary
      </title>
      <link rel="stylesheet" href="common.css" type="text/css"></link>
	  
	  <script src="server.js"></script>
    </head>
  </xsl:template>
  
  <!-- ================================================================== -->
  <!-- This Template will show the summary of the page                    -->
  <!-- ================================================================== -->
  <xsl:template name="summary">
    <xsl:param name="href" />
    <xsl:param name="header" />
    <!-- If it is a constructor then don't show the returns value -->
    
    <xsl:if test="$header = 'true'">
      <div class="title">
        <xsl:value-of select="@shortName"/>
      </div>
      <table width="70%">
        <tr>
          <td>
            <xsl:value-of select="@comment"/>
            <br/>
            <xsl:call-template name="extensionAlert" />
          </td>
        </tr>
      </table>
    </xsl:if>         

    <table class="scriptTable">
      <tr>
        <td class="scriptTd">
          <xsl:if test="$href != ''">
            <a>
              <xsl:attribute name="href">
                <xsl:value-of select="$href"/>
              </xsl:attribute>
              <xsl:attribute name="class">details</xsl:attribute>
              <xsl:value-of select="@shortName"/>&#160;Details
            </a>
            <br/>
          </xsl:if>
          
          <!--First get the constructors -->
          <xsl:if test="count(constructors) > 0">
            <xsl:variable name="ctorName" select="@name" />
            <xsl:for-each select="constructors">
              <xsl:for-each select="ctor">
				<xsl:variable name="supportedIn" select="@supportedIn" />
                <xsl:call-template name="functionSummary">
                  <xsl:with-param name="ctor">
                    <xsl:value-of select="$ctorName"/>
                  </xsl:with-param>
				  <xsl:with-param name="supportedIn">
                    <xsl:value-of select="$supportedIn"/>
                  </xsl:with-param>
                </xsl:call-template>
              </xsl:for-each>
            </xsl:for-each>
          </xsl:if>

          <!--Next get the constants -->
          <xsl:if test="count(constants) > 0">
            
            <xsl:for-each select="constants">
              <xsl:for-each select="const">
                const&#160;<xsl:value-of select="@type"/>&#160;
                <b>
                  <xsl:value-of select="@name"/>
                </b>
                &#160;=&#160;<xsl:value-of select="@value"/>&#160;
                <br/>
              </xsl:for-each>
            </xsl:for-each>
          </xsl:if>

          <!--Next get the functions-->
          <xsl:if test="count(functions) > 0">
            <xsl:for-each select="functions">
              <xsl:for-each select="func">
                <xsl:sort select="@name"/>
				<xsl:variable name="supportedIn" select="@supportedIn" />
                <xsl:call-template name="functionSummary">
					<xsl:with-param name="supportedIn">
						<xsl:value-of select="$supportedIn"/>
					</xsl:with-param>
				</xsl:call-template>
              </xsl:for-each>
            </xsl:for-each>
          </xsl:if>

          <!-- Next do the fields-->
          <xsl:if test="count(properties) > 0">
            <xsl:for-each select="properties">
              <xsl:for-each select="property">
                <xsl:sort select="@name"/>
                <xsl:if test="@static = 'true'">
                  static&#160;
                </xsl:if>
                <xsl:choose>
                  <xsl:when test="@readonly = 'true'">
                    readonly
                  </xsl:when>
                  <xsl:otherwise>
                    readwrite
                  </xsl:otherwise>
                </xsl:choose>
                &#160;property&#160;
                <xsl:value-of select="@type"/>
                <xsl:if test="@array = 'true'">
                  [ ]
                </xsl:if>
                &#160;
                <b>
                  <xsl:value-of select="@name"/>
                </b>
                <br/>
              </xsl:for-each>
            </xsl:for-each>
            <br/>
          </xsl:if>
        </td>
      </tr>
    </table>
    <br/>
  </xsl:template>

  <!-- ================================================================== -->
  <!-- This template will display the summary of a function               -->
  <!-- ================================================================== -->
  <xsl:template name="functionSummary">
    <xsl:param name="ctor" />
	<xsl:param name="supportedIn" />
    <!-- If it is a constructor then don't show the returns value -->
    <xsl:choose>
      <xsl:when test="$ctor = ''">
        <xsl:if test="@static = 'true'">
          static&#160;
        </xsl:if>
        <xsl:value-of select="@returns"/>
        <xsl:if test="@array = 'true'">
          [ ]
        </xsl:if>
        &#160;
        <b>
          <xsl:value-of select="@name"/>
        </b>
      </xsl:when>
      <xsl:otherwise>
        <b>
          <xsl:value-of select="$ctor"/>
        </b>
      </xsl:otherwise>
    </xsl:choose>
    (
    <xsl:for-each select="param">
      <xsl:choose>
        <xsl:when test="@optional = 'true'">
          <i>
            [<xsl:value-of select="@name"/>&#160;:&#160;<xsl:value-of select="@type"/>
            <xsl:if test="@array = 'true'">
              [ ]
            </xsl:if>
            ]
          </i>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="@name"/>&#160;:&#160;<xsl:value-of select="@type"/>
          <xsl:if test="@array = 'true'">
            [ ]
          </xsl:if>
        </xsl:otherwise>
      </xsl:choose>
      <xsl:if test="position() != last( )">
        ,&#160;
      </xsl:if>
    </xsl:for-each>
    )
	
	<xsl:if test="$supportedIn != ''">
        <b><i> - Supported in <xsl:value-of select="$supportedIn"/> only</i></b>
    </xsl:if>
	
    <br/>
  </xsl:template>

  <!-- ================================================================== -->
  <!-- This template will display the alert and hyperlink to the download -->
  <!-- ================================================================== -->
  <xsl:template name="extensionAlert">
    
    
    <xsl:if test="count(extension) > 0">
      <xsl:if test="count(extension/@lastUpdated) > 0">
        <i>
          <font color="navy">
            API Last Updated:&#160;<xsl:value-of select="extension/@lastUpdated"/>
          </font>
        </i>
        <br/>
      </xsl:if>
      <font color="red">
        This API is a BlackBerry WebWorks Extension and must be downloaded separately for inclusion in your BlackBerry WebWork.
      </font>
      <a>
        <xsl:attribute name="href"><xsl:value-of select="extension/@uid"/>.html</xsl:attribute>
        <xsl:attribute name="class">details</xsl:attribute>
        Click here for download instructions
      </a>
      <br/>
    </xsl:if>
    
  </xsl:template>



  <!-- ================================================================== -->
  <!-- This template will display the details of a function               -->
  <!-- ================================================================== -->
  <xsl:template name="functionDetails">
    <xsl:param name="ctor" />
	<xsl:param name="supportedIn" />
    
    <table class="functionTable">
      <tr>
        <td colspan="2" class="functionHeaderTd">
          <b>
            <xsl:call-template name="functionSummary">
              <xsl:with-param name="ctor">
                <xsl:value-of select="$ctor"/>
              </xsl:with-param>
			  <xsl:with-param name="supportedIn">
                <xsl:value-of select="$supportedIn"/>
              </xsl:with-param>
            </xsl:call-template>
          </b>
        </td>
      </tr>
      <tr>
        <td class="functionCol1Td">
            Summary
        </td>
        <td class="functionTd">
          <xsl:value-of select="@comment"/>
        </td>
      </tr>
      <!-- Any parameters for this function -->
      <xsl:if test="count(param) > 0">
        <tr>
          <td class="functionCol1Td">
            Parameters
          </td>
          <td class="functionTd">
            <xsl:for-each select="param">
              <font color="#484848"><b>
                <xsl:value-of select="@name"/>
              </b>
              </font>
              &#160;-&#160;<xsl:value-of select="@comment"/>
              <xsl:for-each select="details">
                <br/>
                  <xsl:value-of select="."/>
              </xsl:for-each>
              <br clear="all" />
            </xsl:for-each>
          </td>
        </tr>
      </xsl:if>
      <!-- Are details provided -->
      <xsl:if test="count(details) > 0">
        <tr>
          <td class="functionCol1Td">
            Details
          </td>
          <td class="functionTd">
            <p>
              <xsl:for-each select="details">
                <p>
                  <xsl:value-of select="."/>
                </p>
              </xsl:for-each>
            </p>
          </td>
        </tr>
      </xsl:if>
    </table>
  </xsl:template>


</xsl:stylesheet>
