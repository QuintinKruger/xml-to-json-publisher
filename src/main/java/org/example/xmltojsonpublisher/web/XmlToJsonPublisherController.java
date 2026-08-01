package org.example.xmltojsonpublisher.web;

import net.sf.saxon.s9api.SaxonApiException;
import org.example.xmltojsonpublisher.core.saver.DiskSaver;
import org.example.xmltojsonpublisher.core.saver.Saver;
import org.example.xmltojsonpublisher.domain.NormalizedJudgment;
import org.example.xmltojsonpublisher.service.TransformerService;
import org.example.xmltojsonpublisher.validation.XmlValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private final Saver saver;


    private record FileOutcome(String fileName, boolean processedSuccessfully, String exceptionMessage) {
    }

    public XmlToJsonPublisherController(XmlValidator xmlValidator, TransformerService transformerService, Saver saver) {
        this.xmlValidator = xmlValidator;
        this.transformerService = transformerService;
        this.saver = saver;
    }

    @PostMapping(value = "/upload-xml", consumes = "multipart/form-data")
    ResponseEntity<List<FileOutcome>> uploadXml(@RequestParam("file") MultipartFile[] multipartFiles) {
        List<FileOutcome> fileOutcomes = new ArrayList<>();
        for (MultipartFile multipartFile : multipartFiles) {
            try {
                xmlValidator.validate(multipartFile);
                NormalizedJudgment normalizedJudgment = transformerService.transform(new StreamSource(multipartFile.getInputStream()));
                saver.saveNormalizedJudgement(normalizedJudgment);
                fileOutcomes.add(new FileOutcome(multipartFile.getOriginalFilename(), true, null));
            }  catch (Exception e) {
                fileOutcomes.add(new FileOutcome(multipartFile.getOriginalFilename(), false, "%s - %s".formatted(e.getClass().getSimpleName(), e.getMessage())));
            }

        }
        return new ResponseEntity<>(fileOutcomes, HttpStatus.OK);
    }
}
