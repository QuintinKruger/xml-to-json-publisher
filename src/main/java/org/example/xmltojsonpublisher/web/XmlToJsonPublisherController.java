package org.example.xmltojsonpublisher.web;

import org.example.xmltojsonpublisher.validation.FileValidator;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.xml.sax.SAXException;

import java.io.IOException;

@RestController
public class XmlToJsonPublisherController {

    private final FileValidator fileValidator;

    public XmlToJsonPublisherController(FileValidator fileValidator) {
        this.fileValidator = fileValidator;
    }

    @PostMapping(value = "/upload-xml", consumes = "multipart/form-data")
    ResponseEntity<Void> uploadXml(@RequestParam("file") MultipartFile multipartFile) {
        fileValidator.validate(multipartFile);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
