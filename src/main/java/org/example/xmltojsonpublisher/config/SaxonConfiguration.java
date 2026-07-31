package org.example.xmltojsonpublisher.config;

import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.Xslt30Transformer;
import net.sf.saxon.s9api.XsltCompiler;
import net.sf.saxon.s9api.XsltExecutable;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import javax.xml.transform.stream.StreamSource;
import java.io.IOException;
import java.util.ArrayList;

@Configuration
public class SaxonConfiguration {

    @Bean
    public Processor processor() {
        return new Processor(false);
    }

    @Bean
    public XsltCompiler xsltCompiler(Processor processor) {
        return processor.newXsltCompiler();
    }

    @Bean
    public XsltExecutable xsltExecutable(XsltCompiler xsltCompiler) throws IOException, SaxonApiException {
        return xsltCompiler.compile(new StreamSource(new ClassPathResource("judgment-to-json.xsl").getInputStream()));
    }
}
