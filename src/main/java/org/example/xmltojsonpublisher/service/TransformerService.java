package org.example.xmltojsonpublisher.service;

import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.Serializer;
import net.sf.saxon.s9api.Xslt30Transformer;
import net.sf.saxon.s9api.XsltExecutable;
import org.springframework.stereotype.Service;

import javax.xml.transform.Source;
import java.io.File;

@Service
public class TransformerService {

    private final XsltExecutable xsltExecutable;
    private final Processor processor;

    public TransformerService(Processor processor, XsltExecutable xsltExecutable) {
        this.processor = processor;
        this.xsltExecutable = xsltExecutable;
    }

    public void transform(Source xmlSource) throws SaxonApiException {
        Serializer destination = processor.newSerializer(new File("converted.json"));
        destination.setOutputProperty(Serializer.Property.METHOD, "json");
        destination.setOutputProperty(Serializer.Property.INDENT, "yes");
        Xslt30Transformer transformer = xsltExecutable.load30(); // not thread safe thus create new transformer for each new xmlSource to transform
        transformer.transform(xmlSource, destination);
    }


}
