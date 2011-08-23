<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:import href="common.xslt"/>

  <xsl:param name="outputFolder">jsdocs_output</xsl:param>
  
  <xsl:output method="html" encoding="utf-8"
  doctype-public="-//W3C//DTD XHTML 1.0 Transitional//EN"
  doctype-system="http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd"/>
  
  <xsl:preserve-space elements='a root'/>
  
  <xsl:template match="/">
	<xsl:apply-templates select="//object"/>
  </xsl:template>
  <!-- ================================================================== -->
  <!-- Apply styles for namespace definitions                             -->
  <!-- ================================================================== -->
  <xsl:template match="//object">
	<xsl:result-document href="{$outputFolder}/{@name}.html">
    <html>
      <xsl:call-template name="head"/>
      <body text="#000000" bgColor="#ffffff">
        <xsl:call-template name="title"/>

        <xsl:call-template name="main"/>
      </body>
    </html>
	</xsl:result-document>
  </xsl:template>

  <!-- ================================================================== -->
  <!-- General Page layout                                                -->
  <!-- ================================================================== -->
  <xsl:template name="main">
    <xsl:call-template name="features"/>

    <xsl:call-template name="codeexample"/>

    <br/>
    <div class="title">Summary</div>
    <!-- Call the main summary -->
    <xsl:choose>
      <xsl:when test="count(subclasses/object) > 0">
        <xsl:call-template name="summary">
          <xsl:with-param name="href">#<xsl:value-of select="@name"/></xsl:with-param>
        </xsl:call-template>

        <!-- Generate summary for any nested Classes -->
        <xsl:for-each select="subclasses/object">
          <xsl:sort select="@shortName"/>
          <xsl:choose>
            <xsl:when test="@interface = 'true'">
              <xsl:call-template name="interfaceSummary">
                <xsl:with-param name="href">#<xsl:value-of select="@name"/></xsl:with-param>
              </xsl:call-template>
            </xsl:when>
            <xsl:otherwise>
              <xsl:call-template name="summary">
                <xsl:with-param name="href">#<xsl:value-of select="@name"/></xsl:with-param>
              </xsl:call-template>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:for-each>
        <h3>
          <a>
            <xsl:attribute name="name">
              <xsl:value-of select="@name"/>
            </xsl:attribute>
            <xsl:value-of select="@shortName"/>&#160;Details
          </a>
        </h3>
        <xsl:call-template name="content"/>

        <!-- Generate contents for any nested Classes -->
        <xsl:for-each select="subclasses/object">
          <xsl:sort select="@shortName"/>
          <h3>
            <a>
              <xsl:attribute name="name">
                <xsl:value-of select="@name"/>
              </xsl:attribute>
              <xsl:value-of select="@shortName"/>&#160;Details
            </a>
          </h3>
          <xsl:choose>
            <xsl:when test="@interface = 'true'">
              <xsl:call-template name="functionDetails"/>
            </xsl:when>
            <xsl:otherwise>
              <xsl:call-template name="fieldDetails"/>
            </xsl:otherwise>
          </xsl:choose>
          
          
        </xsl:for-each>
      </xsl:when>
      <xsl:otherwise>
        
        <xsl:call-template name="summary"/>

        <xsl:call-template name="content"/>
      </xsl:otherwise>
    </xsl:choose>

  </xsl:template>


  <!-- ================================================================== -->
  <!-- This Template will show the feature id's of the page               -->
  <!-- ================================================================== -->
  <xsl:template name="features">
    <div class="title">Permissions</div>
    <!--First see if there are any features declared -->
    <xsl:choose>
      <xsl:when test="count(features/feature) > 0">
        To use <u>all</u> of the API described for this object, you must declare the below feature element(s) in your configuration document: <br/>
        <table class="scriptTable">
          <tr>
            <td class="scriptTd">
              <xsl:for-each select="features/feature">
                &lt;feature id=&quot;<xsl:value-of select="@id"/>&quot; version=&quot;<xsl:value-of select="@version"/>&quot;/&gt;
                <br/>
              </xsl:for-each>
            </td>
          </tr>
        </table>
      </xsl:when>
      <xsl:otherwise>
        This API does not require a &lt;feature&gt; element to be declared in the configuration document of your BlackBerry WebWorks.<br/>
      </xsl:otherwise>
    </xsl:choose>
    <br/>
  </xsl:template>


  

  <!-- ================================================================== -->
  <!-- This template will display the title of the page                   -->
  <!-- ================================================================== -->
  <xsl:template name="title">
    <h1>
      <xsl:value-of select="@shortName"/>&#160;Object
    </h1>
    <p>
      <div class="title">Overview</div>
      <i>
        <font color="navy">
          Fully qualified name:&#160;
          <xsl:value-of select="@name"/>
          <xsl:if test="count(@introduced) > 0">
            <br/>BlackBerry WebWorks API Version: <xsl:value-of select="@introduced"/>
          </xsl:if>
          <br/>
        </font>
      </i>
      <xsl:call-template name="extensionAlert" />
      
      <br/>
      <xsl:value-of select="@comment"/>
      <xsl:for-each select="details">
        <p>
          <xsl:if test="@title != ''">
            <div class="smallTitle">
              <xsl:value-of select="@title"/>
            </div>
          </xsl:if>
          <xsl:value-of select="."/>
        </p>
      </xsl:for-each>
      <xsl:if test="count(../import) > 0">
        <b>See Also</b>
        <br/>
        <xsl:for-each select="../import">
          -&#160;
          <a>
            <xsl:attribute name="href">
              <xsl:value-of select="@src"/>
            </xsl:attribute>
            <xsl:value-of select="@name"/>
          </a>
          <br/>
        </xsl:for-each>
      </xsl:if>
              
    </p>
  </xsl:template>


  <!-- ================================================================== -->
  <!-- This template will display the code example                        -->
  <!-- ================================================================== -->
  <xsl:template name="codeexample">

    <xsl:if test="count(//example) != 0">
      <div class="title">Code Example(s)</div>
      <xsl:for-each select="example">
        <table class="scriptTable">
          <tr>
            <td class="scriptTd">
              <pre>
                <xsl:value-of select="."/>
              </pre>
            </td>
          </tr>
        </table>
      </xsl:for-each>
    </xsl:if>
  </xsl:template>

  <!-- ================================================================== -->
  <!-- This template will display the main content of the page            -->
  <!-- ================================================================== -->
  <xsl:template name="content">
    <!--First get the constructors -->
    <xsl:if test="count(constructors) > 0">
      <div class="smallTitle">Constructors:</div>
      <xsl:variable name="ctorName" select="@name" />
      <xsl:for-each select="constructors/ctor">
		<xsl:variable name="supportedIn" select="@supportedIn" />
        <xsl:call-template name="functionDetails">
          <xsl:with-param name="ctor">
            <xsl:value-of select="$ctorName"/>
          </xsl:with-param>
		  <xsl:with-param name="supportedIn">
            <xsl:value-of select="$supportedIn"/>
          </xsl:with-param>
        </xsl:call-template>
        <br/>
      </xsl:for-each>
    </xsl:if>

    <!-- Next get the functions -->
    <xsl:if test="count(functions/func) > 0">
      <div class="smallTitle">Functions:</div>
      <xsl:for-each select="functions/func">
        <xsl:sort select="@name"/>
		<xsl:variable name="supportedIn" select="@supportedIn" />
        <xsl:call-template name="functionDetails">
		  <xsl:with-param name="supportedIn">
            <xsl:value-of select="$supportedIn"/>
          </xsl:with-param>
        </xsl:call-template>
        <br/>
      </xsl:for-each>
    </xsl:if>

    <!-- Next get the properties -->
    <xsl:choose>
      <xsl:when test="count(properties/property) > 0">
        <div class="smallTitle">Properties:</div>
        <xsl:call-template name="fieldDetails"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:if test="count(constants/const) > 0">
          <div class="smallTitle">Properties:</div>
          <xsl:call-template name="fieldDetails"/>
        </xsl:if>
      </xsl:otherwise>
    </xsl:choose>
    
  </xsl:template>


 

  <!-- ================================================================== -->
  <!-- This template will display the details of a field                  -->
  <!-- ================================================================== -->
  <xsl:template name="fieldDetails">
    <table class="confluenceTable">
      <tr>
        <th class="confluenceTh">
          Property
        </th>
        <th class="confluenceTh">
          Type
        </th>
        <th class="confluenceTh">
          Description
        </th>
      </tr>

      <xsl:for-each select="constants/const">
        <tr>
          <td class="confluenceTd">
            <b>
              <xsl:value-of select="@name"/>
            </b>
          </td>
          <td class="confluenceTd">
              constant<br/>
            <xsl:value-of select="@type"/>
          </td>
          <td class="confluenceTd">
            <xsl:value-of select="@comment"/>
            <xsl:if test="count(details) > 0">
              <xsl:for-each select="details">
                <p>
                  <xsl:value-of select="."/>
                </p>
              </xsl:for-each>
            </xsl:if>
          </td>
        </tr>
      </xsl:for-each>
      
      <xsl:for-each select="properties/property">
        <xsl:sort select="@name"/>
        <tr>
          <td class="confluenceTd">
              <b>
                <xsl:value-of select="@name"/>
              </b>
          </td>
          <td class="confluenceTd">
            <xsl:if test="@static = 'true'">
              static<br/>
            </xsl:if>
            <xsl:choose>
              <xsl:when test="@readonly = 'true'">
                readonly
              </xsl:when>
              <xsl:otherwise>
                readwrite
              </xsl:otherwise>
            </xsl:choose>
            <br/>
            <xsl:value-of select="@type"/>
            <xsl:if test="@array = 'true'">
              [ ]
            </xsl:if>
          </td>
          <td class="confluenceTd">
            <xsl:value-of select="@comment"/>
            <xsl:if test="count(details) > 0">
              <xsl:for-each select="details">
                <p>
                  <xsl:value-of select="."/>
                </p>
              </xsl:for-each>
            </xsl:if>
          </td>
        </tr>
      </xsl:for-each>
    </table>

  </xsl:template>

  <!-- ================================================================== -->
  <!-- This Template will show the summary of the page                    -->
  <!-- ================================================================== -->
  <xsl:template name="interfaceSummary">
    <xsl:param name="href" />
   
    <table class="scriptTable">
      <tr>
        <td class="scriptTd">
          <xsl:if test="$href != ''">
            <a>
              <xsl:attribute name="href">
                <xsl:value-of select="$href"/>
              </xsl:attribute>
              <xsl:attribute name="class">details</xsl:attribute>
              <xsl:value-of select="@shortName"/>
            </a>
          </xsl:if>
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
        </td>
      </tr>
    </table>
    <br/>
  </xsl:template>
</xsl:stylesheet>
