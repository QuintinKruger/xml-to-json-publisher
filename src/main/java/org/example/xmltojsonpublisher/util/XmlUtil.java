package org.example.xmltojsonpublisher.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.InputStream;
import java.util.Optional;

public class XmlUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(XmlUtil.class);

    public static Optional<String> getXmlAttributeValueFromStream(String attribute, InputStream inputStream) {
        try {
            XMLStreamReader xmlStreamReader = getXmlInputFactory().createXMLStreamReader(inputStream);
            try {
                while (xmlStreamReader.hasNext()) {
                    if (xmlStreamReader.next() == XMLStreamConstants.START_ELEMENT
                            && attribute.equals(xmlStreamReader.getLocalName())) {
                        return Optional.of(xmlStreamReader.getElementText());
                    }
                }
                return Optional.empty();
            } finally {
                xmlStreamReader.close();
            }
        } catch (XMLStreamException e) {
            LOGGER.error("Failed to get attribute {} from XML inputstream.", attribute, e);
            return Optional.empty();
        }

    }

    private static XMLInputFactory getXmlInputFactory() {
        // thread safety is not guaranteed due to internal caching, construct new factory rather than use singleton bean
        XMLInputFactory xmlInputFactory = XMLInputFactory.newFactory();
        // disable DTD support to prevent against XML External Entity (XXE) injection attack and the exponential entity expansion attack, also know as the XML bomb or billion laughs attack.
        xmlInputFactory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
        xmlInputFactory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
        return xmlInputFactory;
    }
}
