package org.example.xmltojsonpublisher.domain;

public record FileOutcome(String fileName, boolean processedSuccessfully, String exceptionMessage) {
}
