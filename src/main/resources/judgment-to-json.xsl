<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="3.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:lex="urn:lex:content:1"
    exclude-result-prefixes="lex">

    <xsl:output method="json" indent="yes"/>

    <xsl:template match="/lex:judgment">
        <xsl:variable name="paragraphs" select="lex:body/lex:section/lex:p"/>
        <xsl:map>
            <xsl:map-entry key="'content_id'"    select="lex:header/lex:content_id/string()"/>
            <xsl:map-entry key="'title'"         select="lex:header/lex:title/string()"/>
            <xsl:map-entry key="'court'"         select="lex:header/lex:court/string()"/>
            <xsl:map-entry key="'jurisdiction'"  select="lex:header/lex:jurisdiction/string()"/>
            <xsl:map-entry key="'decision_date'" select="lex:header/lex:decision_date/string()"/>

            <!-- citations/parties are optional in the XSD (minOccurs="0"); an absent
                 element yields an empty sequence here, so these normalize to [] rather
                 than the key being omitted entirely. -->
            <xsl:map-entry key="'citations'"
                select="array {
                    for $c in lex:header/lex:citations/lex:citation
                    return map { 'type': $c/@type/string(), 'value': $c/string() }
                }"/>

            <xsl:map-entry key="'parties'"
                select="array {
                    for $p in lex:header/lex:parties/lex:party
                    return map { 'role': $p/@role/string(), 'name': $p/string() }
                }"/>

            <xsl:map-entry key="'paragraphs'"
                select="array {
                    for $p in $paragraphs
                    return map {
                        'id': $p/@id/string(),
                        'section': $p/parent::lex:section/@type/string(),
                        'text': $p/string()
                    }
                }"/>

            <xsl:map-entry key="'full_text'" select="string-join($paragraphs/string(), ' ')"/>
        </xsl:map>
    </xsl:template>

</xsl:stylesheet>
