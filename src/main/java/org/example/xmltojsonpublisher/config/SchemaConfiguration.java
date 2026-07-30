package org.example.xmltojsonpublisher.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.File;
import java.io.IOException;

@Configuration
public class SchemaConfiguration {

    /**
     * Thread safe according to documentation @see <a href="https://docs.oracle.com/javase/7/docs/api/javax/xml/validation/Schema.html">Schema</a>
     * thus define a bean of this type to be used for any schema validation instead of {@link Validator} which is not thread safe
     */
    @Bean
    public Schema schema() throws IOException, SAXException {
        SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        File schemaFile = new ClassPathResource("xsd.xml").getFile();
        return schemaFactory.newSchema(schemaFile);
    }
}
