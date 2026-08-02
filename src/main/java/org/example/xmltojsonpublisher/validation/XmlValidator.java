package org.example.xmltojsonpublisher.validation;

import org.example.xmltojsonpublisher.core.saver.Saver;
import org.example.xmltojsonpublisher.exception.AlreadyProcessedException;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.xml.sax.SAXException;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.Validator;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

@Component
public class XmlValidator {

    private final Schema schema;
    private final Saver saver;

    public XmlValidator(Schema schema, Saver saver) {
        this.schema = schema;
        this.saver = saver;
    }

    public void validate(byte[] xmlBytes) throws IOException, XMLStreamException, SAXException {
        validateNotPreviouslyProcessed(new ByteArrayInputStream(xmlBytes));
        validateSchema(new ByteArrayInputStream(xmlBytes));
    }

    public void validateSchema(InputStream inputStream) throws IOException, SAXException {
        getSchemaValidator().validate(new StreamSource(inputStream));
    }

    public void validateNotPreviouslyProcessed(InputStream inputStream) throws XMLStreamException {
        Optional<String> optionalContentId = getContentIdFromXmlInputStream(inputStream);
        if (optionalContentId.isPresent() && saver.exists(optionalContentId.get())) {
            throw new AlreadyProcessedException(optionalContentId.get());
        }

    }

    private Optional<String> getContentIdFromXmlInputStream(InputStream inputStream) throws XMLStreamException {
        XMLStreamReader xmlStreamReader = getXmlInputFactory().createXMLStreamReader(inputStream);
        try {
            while (xmlStreamReader.hasNext()) {
                if (xmlStreamReader.next() == XMLStreamConstants.START_ELEMENT
                        && "content_id".equals(xmlStreamReader.getLocalName())) {
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

    private Validator getSchemaValidator() {
        return schema.newValidator();
    }


}
