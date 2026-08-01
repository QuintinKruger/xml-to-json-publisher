package org.example.xmltojsonpublisher.validation;

import org.example.xmltojsonpublisher.core.saver.Saver;
import org.example.xmltojsonpublisher.exception.AlreadyProcessedException;
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
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

@Component
public class XmlValidator {

    private final Schema schema;
    private final XMLInputFactory xmlInputFactory;
    private final Saver saver;

    public XmlValidator(Schema schema, XMLInputFactory xmlInputFactory, Saver saver) {
        this.schema = schema;
        this.xmlInputFactory = xmlInputFactory;
        this.saver = saver;
    }

    public void validate(MultipartFile multipartFile) throws IOException, XMLStreamException, SAXException {
        validateNotPreviouslyProcessed(multipartFile.getInputStream());
        validateSchema(multipartFile.getInputStream());
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
        XMLStreamReader xmlStreamReader = xmlInputFactory.createXMLStreamReader(inputStream);
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

    private Validator getSchemaValidator() {
        return schema.newValidator();
    }


}
