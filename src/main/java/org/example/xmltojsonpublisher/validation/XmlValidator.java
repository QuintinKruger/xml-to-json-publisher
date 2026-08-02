package org.example.xmltojsonpublisher.validation;

import org.example.xmltojsonpublisher.core.saver.Saver;
import org.example.xmltojsonpublisher.exception.AlreadyProcessedException;
import org.example.xmltojsonpublisher.util.XmlUtil;
import org.springframework.stereotype.Component;
import org.xml.sax.SAXException;

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

    public void validateCanProcess(String contentId, byte[] xmlBytes) throws IOException, SAXException {
        validateNotPreviouslyProcessed(contentId);
        validateSchema(new ByteArrayInputStream(xmlBytes));
    }

    public void validateSchema(InputStream inputStream) throws IOException, SAXException {
        getSchemaValidator().validate(new StreamSource(inputStream));
    }

    public void validateNotPreviouslyProcessed(String contentId) {
        if (saver.exists(contentId)) {
            throw new AlreadyProcessedException(contentId);
        }

    }

    private Validator getSchemaValidator() {
        return schema.newValidator();
    }


    public String validateContentId(byte[] bytes) {
        Optional<String> optionalContentId = XmlUtil.getXmlAttributeValueFromStream("content_id", new ByteArrayInputStream(bytes));
        if (optionalContentId.isEmpty() || optionalContentId.get().isBlank()) {
            throw new IllegalArgumentException("Content ID is empty.");
        }
        return optionalContentId.get();

    }
}
