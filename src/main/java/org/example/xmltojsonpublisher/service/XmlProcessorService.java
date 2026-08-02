package org.example.xmltojsonpublisher.service;

import org.example.xmltojsonpublisher.core.ContentLockRegistry;
import org.example.xmltojsonpublisher.core.saver.Saver;
import org.example.xmltojsonpublisher.core.transformer.XmlTransformer;
import org.example.xmltojsonpublisher.core.transformer.rag.RagTransformer;
import org.example.xmltojsonpublisher.domain.FileOutcome;
import org.example.xmltojsonpublisher.domain.NormalizedJudgment;
import org.example.xmltojsonpublisher.util.XmlUtil;
import org.example.xmltojsonpublisher.validation.XmlValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.xml.stream.XMLStreamException;
import javax.xml.transform.stream.StreamSource;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class XmlProcessorService {

    private final static Logger LOGGER = LoggerFactory.getLogger(XmlProcessorService.class);
    private final XmlValidator xmlValidator;
    private final XmlTransformer xmlTransformer;
    private final RagTransformer ragTransformer;
    private final Saver saver;
    private final ContentLockRegistry contentLockRegistry;

    public XmlProcessorService(XmlTransformer xmlTransformer, RagTransformer ragTransformer, Saver saver, XmlValidator xmlValidator, ContentLockRegistry contentLockRegistry) {
        this.xmlTransformer = xmlTransformer;
        this.saver = saver;
        this.ragTransformer = ragTransformer;
        this.xmlValidator = xmlValidator;
        this.contentLockRegistry = contentLockRegistry;
    }

    @Async
    public CompletableFuture<FileOutcome> processXmlContent(String fileName, byte[] bytes){
        Optional<String> optionalContentId;
        try {
            optionalContentId = XmlUtil.getXmlAttributeValueFromStream("content_id", new ByteArrayInputStream(bytes));
        } catch (XMLStreamException e) {
            LOGGER.error("Content ID could not be determined for {}",fileName, e);
            throw new IllegalArgumentException(e);
        }

        if (optionalContentId.isPresent()) {
            ReentrantLock reentrantLock = contentLockRegistry.getLock(optionalContentId.get());
            try {
                reentrantLock.lock();
                xmlValidator.validate(bytes);
                NormalizedJudgment normalizedJudgment = xmlTransformer.transform(new StreamSource(new ByteArrayInputStream(bytes)));
                String ragText = ragTransformer.transform(normalizedJudgment);
                saver.save(normalizedJudgment, ragText, normalizedJudgment.contentId());
                return CompletableFuture.completedFuture(new FileOutcome(fileName, true, null));
            } catch (Exception e) {
                LOGGER.error("Failed to process file {}",fileName, e);
                return CompletableFuture.completedFuture(new FileOutcome(fileName, false, e.getMessage()));
            } finally {
                reentrantLock.unlock();
            }
        } else {
            IllegalArgumentException illegalArgumentException = new IllegalArgumentException("No content ID");
            LOGGER.error("No content ID for {}",fileName, illegalArgumentException);
            throw illegalArgumentException;
        }
    }
}
