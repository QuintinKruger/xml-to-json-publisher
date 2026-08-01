package org.example.xmltojsonpublisher.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.stream.XMLInputFactory;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.File;
import java.io.IOException;

@Configuration
public class JavaxXmlConfiguration {

    /**
     * Thread safe according to documentation @see <a href="https://docs.oracle.com/javase/7/docs/api/javax/xml/validation/Schema.html">Schema</a>
     * thus define a bean of this type to be used for any schema validation instead of {@link Validator} which is not thread safe
     */
    @Bean
    public Schema schema() throws IOException, SAXException {
        SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        schemaFactory.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        schemaFactory.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
        File schemaFile = new ClassPathResource("xsd.xml").getFile();
        return schemaFactory.newSchema(schemaFile);
    }

    @Bean
    public XMLInputFactory xmlInputFactory() {
        XMLInputFactory xmlInputFactory = XMLInputFactory.newFactory();
        // disable DTD support to prevent against XML External Entity (XXE) injection attack and the exponential entity expansion attack, also know as the XML bomb or billion laughs attack.
        xmlInputFactory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
        xmlInputFactory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
        return xmlInputFactory;
    }
}
