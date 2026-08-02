package org.example.xmltojsonpublisher;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class XmlToJsonPublisherApplication {


    public static void main(String[] args) {
        SpringApplication.run(XmlToJsonPublisherApplication.class, args);
    }

}
