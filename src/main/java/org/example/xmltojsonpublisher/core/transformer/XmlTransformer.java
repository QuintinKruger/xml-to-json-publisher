package org.example.xmltojsonpublisher.core.transformer;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.Serializer;
import net.sf.saxon.s9api.Xslt30Transformer;
import net.sf.saxon.s9api.XsltExecutable;
import org.example.xmltojsonpublisher.domain.NormalizedJudgment;
import org.springframework.stereotype.Service;

import javax.xml.transform.Source;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Service
public class XmlTransformer {

    private final XsltExecutable xsltExecutable;
    private final Processor processor;
    private final ObjectMapper objectMapper;

    public XmlTransformer(Processor processor, XsltExecutable xsltExecutable, ObjectMapper objectMapper) {
        this.processor = processor;
        this.xsltExecutable = xsltExecutable;
        this.objectMapper = objectMapper;
    }

    public NormalizedJudgment transform(Source xmlSource) throws SaxonApiException, IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        Serializer destination = processor.newSerializer(byteArrayOutputStream);
        Xslt30Transformer transformer = xsltExecutable.load30(); // not thread safe thus create new transformer for each new xmlSource to transform
        transformer.transform(xmlSource, destination);
        return objectMapper.readValue(byteArrayOutputStream.toByteArray(), NormalizedJudgment.class);
    }


}
