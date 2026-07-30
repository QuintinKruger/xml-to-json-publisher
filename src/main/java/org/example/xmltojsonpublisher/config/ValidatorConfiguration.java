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
public class ValidatorConfiguration {

    @Bean
    public Validator validator() throws IOException, SAXException {
        SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        File schemaFile = new ClassPathResource("xsd.xml").getFile();
        Schema schema = schemaFactory.newSchema(schemaFile);
        return schema.newValidator();
    }
}
