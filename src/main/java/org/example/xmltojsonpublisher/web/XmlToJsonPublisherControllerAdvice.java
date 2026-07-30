package org.example.xmltojsonpublisher.web;

import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@ControllerAdvice
public class XmlToJsonPublisherControllerAdvice {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ProblemDetail> handleSAXException(IllegalArgumentException illegalArgumentException){
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(BAD_REQUEST, illegalArgumentException.getMessage());
                return new ResponseEntity<>(problemDetail, BAD_REQUEST);
    }
}
