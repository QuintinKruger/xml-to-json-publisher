package org.example.xmltojsonpublisher.service;

import org.example.xmltojsonpublisher.core.saver.Saver;
import org.example.xmltojsonpublisher.core.transformer.XmlTransformer;
import org.example.xmltojsonpublisher.core.transformer.rag.RagTransformer;
import org.example.xmltojsonpublisher.domain.FileOutcome;
import org.example.xmltojsonpublisher.domain.NormalizedJudgment;
import org.example.xmltojsonpublisher.validation.XmlValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.xml.transform.stream.StreamSource;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.concurrent.CompletableFuture;

@Service
public class XmlProcessorService {

    private final static Logger LOGGER = LoggerFactory.getLogger(XmlProcessorService.class);
    private final XmlValidator xmlValidator;
    private final XmlTransformer xmlTransformer;
    private final RagTransformer ragTransformer;
    private final Saver saver;

    public XmlProcessorService(XmlTransformer xmlTransformer, RagTransformer ragTransformer, Saver saver, XmlValidator xmlValidator) {
        this.xmlTransformer = xmlTransformer;
        this.saver = saver;
        this.ragTransformer = ragTransformer;
        this.xmlValidator = xmlValidator;
    }

    @Async
    public CompletableFuture<FileOutcome> processXmlFile(String fileName, byte[] bytes)  {
        try {
            InputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
            xmlValidator.validate(bytes);
            NormalizedJudgment normalizedJudgment = xmlTransformer.transform(new StreamSource(byteArrayInputStream));
            String ragText = ragTransformer.transform(normalizedJudgment);
            saver.save(normalizedJudgment, ragText, normalizedJudgment.contentId());
            return CompletableFuture.completedFuture(new FileOutcome(fileName, true, null));
        } catch (Exception e) {
            LOGGER.error("Failed to process file {}",fileName, e);
            return CompletableFuture.completedFuture(new FileOutcome(fileName, false, e.getMessage()));
        }
    }
}
