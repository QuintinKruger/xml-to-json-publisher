package org.example.xmltojsonpublisher.service;

import org.example.xmltojsonpublisher.core.saver.Saver;
import org.example.xmltojsonpublisher.core.transformer.XmlTransformer;
import org.example.xmltojsonpublisher.core.transformer.rag.RagTransformer;
import org.example.xmltojsonpublisher.domain.NormalizedJudgment;
import org.springframework.stereotype.Service;

import javax.xml.transform.stream.StreamSource;
import java.io.InputStream;

@Service
public class XmlProcessorService {

    private final XmlTransformer xmlTransformer;
    private final RagTransformer ragTransformer;
    private final Saver saver;

    public XmlProcessorService(XmlTransformer xmlTransformer, RagTransformer ragTransformer, Saver saver) {
        this.xmlTransformer = xmlTransformer;
        this.saver = saver;
        this.ragTransformer = ragTransformer;
    }

    public void processXmlFile(InputStream inputStream) throws Exception {
        NormalizedJudgment normalizedJudgment = xmlTransformer.transform(new StreamSource(inputStream));
        String ragText = ragTransformer.transform(normalizedJudgment);
        saver.save(normalizedJudgment, ragText, normalizedJudgment.contentId());
    }
}
