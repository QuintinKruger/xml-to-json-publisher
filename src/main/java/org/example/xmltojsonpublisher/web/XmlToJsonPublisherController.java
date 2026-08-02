package org.example.xmltojsonpublisher.web;

import org.example.xmltojsonpublisher.domain.FileOutcome;
import org.example.xmltojsonpublisher.service.XmlProcessorService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
public class XmlToJsonPublisherController {

    private final XmlProcessorService xmlProcessorService;

    public XmlToJsonPublisherController(XmlProcessorService xmlProcessorService) {
        this.xmlProcessorService = xmlProcessorService;
    }

    @PostMapping(value = "/upload-xml", consumes = "multipart/form-data")
    ResponseEntity<List<FileOutcome>> uploadXml(@RequestParam("file") MultipartFile[] multipartFiles) throws IOException {
        List<CompletableFuture<FileOutcome>> futures = new ArrayList<>();
        for (MultipartFile multipartFile : multipartFiles) {
            futures.add(xmlProcessorService.processXmlContent(multipartFile.getOriginalFilename(), multipartFile.getBytes()));
        }
        return new ResponseEntity<>(futures.stream().map(CompletableFuture::join).toList(), HttpStatus.OK);
    }
}
