<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

    <!-- Ключ (для групування по preciousness) -->
    <xsl:key name="byPreciousness" match="Gem" use="preciousness"/>

    <xsl:template match="/Gems">
        <root>

            <!-- Проходимо тільки по перших елементах груп -->
            <xsl:for-each select="Gem[generate-id() = generate-id(key('byPreciousness', preciousness)[1])]">

                <xsl:sort select="preciousness"/>

                <xsl:variable name="group" select="preciousness"/>

                <xsl:element name="{ $group }">

                    <!-- Всі елементи групи -->
                    <xsl:for-each select="key('byPreciousness', $group)">
                        <Gem id="{@id}">
                            <name><xsl:value-of select="name"/></name>
                            <origin><xsl:value-of select="origin"/></origin>
                            <color><xsl:value-of select="visualParameters/color"/></color>
                            <value><xsl:value-of select="value"/></value>
                        </Gem>
                    </xsl:for-each>

                </xsl:element>

            </xsl:for-each>

        </root>
    </xsl:template>
</xsl:stylesheet>
