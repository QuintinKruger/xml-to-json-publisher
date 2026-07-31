package org.example.xmltojsonpublisher.web;

import net.sf.saxon.s9api.SaxonApiException;
import org.example.xmltojsonpublisher.service.TransformerService;
import org.example.xmltojsonpublisher.validation.XmlValidator;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.xml.sax.SAXException;

import javax.xml.transform.stream.StreamSource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RestController
public class XmlToJsonPublisherController {

    private final XmlValidator xmlValidator;
    private final TransformerService transformerService;


    private record FileOutcome(String fileName, boolean processedSuccessfully, String exceptionMessage) {
    }

    public XmlToJsonPublisherController(XmlValidator xmlValidator, TransformerService transformerService) {
        this.xmlValidator = xmlValidator;
        this.transformerService = transformerService;
    }

    @PostMapping(value = "/upload-xml", consumes = "multipart/form-data")
    ResponseEntity<List<FileOutcome>> uploadXml(@RequestParam("file") MultipartFile[] multipartFiles) {
        List<FileOutcome> fileOutcomes = new ArrayList<>();
        for (MultipartFile multipartFile : multipartFiles) {
            try {
                xmlValidator.validate(multipartFile);
                transformerService.transform(new StreamSource(multipartFile.getInputStream()));
                fileOutcomes.add(new FileOutcome(multipartFile.getOriginalFilename(), true, null));
            } catch (SAXException | IOException | SaxonApiException e) {
                fileOutcomes.add(new FileOutcome(multipartFile.getOriginalFilename(), false, e.getMessage()));
            }

        }
        return new ResponseEntity<>(fileOutcomes, HttpStatus.OK);
    }
}
