package org.example.xmltojsonpublisher.web;

import org.example.xmltojsonpublisher.validation.XmlValidator;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.xml.sax.SAXException;

import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Validator;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RestController
public class XmlToJsonPublisherController {

    private final XmlValidator xmlValidator;


    private record FileOutcome(String fileName, boolean processedSuccessfully, String exceptionMessage) {
    }

    public XmlToJsonPublisherController(XmlValidator xmlValidator) {
        this.xmlValidator = xmlValidator;
    }

    @PostMapping(value = "/upload-xml", consumes = "multipart/form-data")
    ResponseEntity<List<FileOutcome>> uploadXml(@RequestParam("file") MultipartFile[] multipartFiles) {
        List<FileOutcome> fileOutcomes = new ArrayList<>();
        for (MultipartFile multipartFile : multipartFiles) {
            try {
                xmlValidator.validate(multipartFile);
                fileOutcomes.add(new FileOutcome(multipartFile.getOriginalFilename(), true, null));
            } catch (SAXException | IOException e) {
                fileOutcomes.add(new FileOutcome(multipartFile.getOriginalFilename(), false, e.getMessage()));
            }

        }
        return new ResponseEntity<>(fileOutcomes, HttpStatus.OK);
    }
}
