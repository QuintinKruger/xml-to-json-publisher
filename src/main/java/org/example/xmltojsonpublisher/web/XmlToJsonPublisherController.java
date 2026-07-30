package org.example.xmltojsonpublisher.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class XmlToJsonPublisherController {

    @GetMapping("/hello-world")
    ResponseEntity<String> getHelloWorld(){
        return new ResponseEntity<>("Hello world!", HttpStatus.OK);
    }
}
