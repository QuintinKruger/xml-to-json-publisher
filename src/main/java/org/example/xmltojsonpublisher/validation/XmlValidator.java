package org.example.xmltojsonpublisher.validation;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.xml.sax.SAXException;

import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.Validator;
import java.io.IOException;

@Component
public class XmlValidator {

    private final Schema schema;

    public XmlValidator(Schema schema) {
        this.schema = schema;
    }

    public void validate(MultipartFile multipartFile) throws IOException, SAXException {
        getSchemaValidator().validate(new StreamSource(multipartFile.getInputStream()));
    }

    private Validator getSchemaValidator() {
        return schema.newValidator();
    }


}
