package org.example.xmltojsonpublisher.web;

import org.example.xmltojsonpublisher.core.saver.Saver;
import org.example.xmltojsonpublisher.core.transformer.XmlTransformer;
import org.example.xmltojsonpublisher.core.transformer.rag.RagTransformer;
import org.example.xmltojsonpublisher.domain.NormalizedJudgment;
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
    private final XmlTransformer xmlTransformer;
    private final RagTransformer ragTransformer;
    private final Saver saver;


    private record FileOutcome(String fileName, boolean processedSuccessfully, String exceptionMessage) {
    }

    public XmlToJsonPublisherController(XmlValidator xmlValidator, XmlTransformer xmlTransformer, RagTransformer ragTransformer, Saver saver) {
        this.xmlValidator = xmlValidator;
        this.xmlTransformer = xmlTransformer;
        this.saver = saver;
        this.ragTransformer = ragTransformer;
    }

    @PostMapping(value = "/upload-xml", consumes = "multipart/form-data")
    ResponseEntity<List<FileOutcome>> uploadXml(@RequestParam("file") MultipartFile[] multipartFiles) {
        List<FileOutcome> fileOutcomes = new ArrayList<>();
        for (MultipartFile multipartFile : multipartFiles) {
            try {
                xmlValidator.validate(multipartFile);
                NormalizedJudgment normalizedJudgment = xmlTransformer.transform(new StreamSource(multipartFile.getInputStream()));
                String ragText = ragTransformer.transform(normalizedJudgment);
                saver.saveNormalizedJudgement(normalizedJudgment, normalizedJudgment.contentId());
                saver.saveRagText(ragText, normalizedJudgment.contentId());
                fileOutcomes.add(new FileOutcome(multipartFile.getOriginalFilename(), true, null));
            }  catch (Exception e) {
                fileOutcomes.add(new FileOutcome(multipartFile.getOriginalFilename(), false, "%s - %s".formatted(e.getClass().getSimpleName(), e.getMessage())));
            }

        }
        return new ResponseEntity<>(fileOutcomes, HttpStatus.OK);
    }
}
