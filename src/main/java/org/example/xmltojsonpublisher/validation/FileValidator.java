package org.example.xmltojsonpublisher.validation;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.xml.sax.SAXException;

import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Validator;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

@Component
public class FileValidator {

    private final Validator xmlValidator;

    public FileValidator(Validator xmlValidator) {
        this.xmlValidator = xmlValidator;
    }

    public void validate(MultipartFile multipartFile) {
        InputStream inputStream = getInputStream(multipartFile);
        validateXmlContent(inputStream);
    }

    private InputStream getInputStream(MultipartFile multipartFile) {
        try {
            return multipartFile.getInputStream();
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to read file contents", e);
        }
    }

    private void validateXmlContent(InputStream inputStream) {
        try {
            xmlValidator.validate(new StreamSource(inputStream));
        } catch (SAXException | IOException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
