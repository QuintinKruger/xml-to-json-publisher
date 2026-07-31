package org.example.xmltojsonpublisher.service;

import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.Serializer;
import net.sf.saxon.s9api.XmlProcessingError;
import net.sf.saxon.s9api.Xslt30Transformer;
import net.sf.saxon.s9api.XsltCompiler;
import org.springframework.stereotype.Service;

import javax.xml.transform.Source;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Service
public class TransformerService {

    private final Xslt30Transformer transformer;
    private final Processor processor;

    public TransformerService(Processor processor, Xslt30Transformer transformer) {
        this.processor = processor;
        this.transformer = transformer;
    }

    public void transform(Source xmlSource) {
        // todo: see if we can set the error list to new empty list to capture any errors that
        // resulted from the transform that happens below (this was also why the XsltCompiler bean was defined)
        // this is to combat the Javadoc statement regarding error pollution for new processes using singletone bean
        // of compiler
        try {
            Serializer destination  = processor.newSerializer(new File("converted.json"));
            destination.setOutputProperty(Serializer.Property.METHOD, "json");
            destination.setOutputProperty(Serializer.Property.INDENT, "yes");
            transformer.transform(xmlSource, destination);
        } catch (SaxonApiException e) {
            throw new RuntimeException(e);
        }
    }



}
