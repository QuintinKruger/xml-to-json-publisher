package org.example.xmltojsonpublisher.util;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.InputStream;
import java.util.Optional;

public class XmlUtil {

    public static Optional<String> getXmlAttributeValueFromStream(String attribute, InputStream inputStream) throws XMLStreamException {
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
