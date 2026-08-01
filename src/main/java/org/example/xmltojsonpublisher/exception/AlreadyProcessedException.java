package org.example.xmltojsonpublisher.exception;

public class AlreadyProcessedException extends RuntimeException{
    public AlreadyProcessedException(String identifier) {
        super("Content with ID %s has already been processed.");
    }

}
