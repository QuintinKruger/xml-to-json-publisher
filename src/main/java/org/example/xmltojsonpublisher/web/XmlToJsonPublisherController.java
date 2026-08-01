package org.example.xmltojsonpublisher.web;

import org.example.xmltojsonpublisher.core.saver.Saver;
import org.example.xmltojsonpublisher.core.transformer.XmlTransformer;
import org.example.xmltojsonpublisher.core.transformer.rag.RagTransformer;
import org.example.xmltojsonpublisher.domain.NormalizedJudgment;
import org.example.xmltojsonpublisher.service.XmlProcessorService;
import org.example.xmltojsonpublisher.validation.XmlValidator;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.xml.transform.stream.StreamSource;
import java.util.ArrayList;
import java.util.List;

@RestController
public class XmlToJsonPublisherController {

    private final XmlValidator xmlValidator;
    private final XmlProcessorService xmlProcessorService;


    private record FileOutcome(String fileName, boolean processedSuccessfully, String exceptionMessage) {
    }

    public XmlToJsonPublisherController(XmlValidator xmlValidator, XmlProcessorService xmlProcessorService) {
        this.xmlValidator = xmlValidator;
        this.xmlProcessorService = xmlProcessorService;
    }

    @PostMapping(value = "/upload-xml", consumes = "multipart/form-data")
    ResponseEntity<List<FileOutcome>> uploadXml(@RequestParam("file") MultipartFile[] multipartFiles) {
        List<FileOutcome> fileOutcomes = new ArrayList<>();
        for (MultipartFile multipartFile : multipartFiles) {
            try {
                xmlValidator.validate(multipartFile);
                xmlProcessorService.processXmlFile(multipartFile.getInputStream());
                fileOutcomes.add(new FileOutcome(multipartFile.getOriginalFilename(), true, null));
            }  catch (Exception e) {
                fileOutcomes.add(new FileOutcome(multipartFile.getOriginalFilename(), false, "%s - %s".formatted(e.getClass().getSimpleName(), e.getMessage())));
            }

        }
        return new ResponseEntity<>(fileOutcomes, HttpStatus.OK);
    }
}
