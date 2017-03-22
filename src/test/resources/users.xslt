<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output method="html" indent="yes"/>
    <xsl:strip-space elements="*"/>
    <xsl:template match="/">

        <html>
            <body>
                <xsl:param name="projectName" />
                <h1><xsl:value-of select="$projectName"/></h1>
                <table border="1">
                    <tr bgcolor="#9acd32">
                        <th style="text-align:left">Full name</th>
                        <th style="text-align:left">Email</th>
                    </tr>
                    <xsl:for-each select="/*[name()='Payload']/*[name()='Users']/*[name()='User']">
                        <tr>
                            <td><xsl:value-of select="."/></td>
                            <td><xsl:value-of select="@email"/></td>
                        </tr>
                    </xsl:for-each>
                </table>
            </body>
        </html>

    </xsl:template>
    <xsl:template match="text()"/>
</xsl:stylesheet>