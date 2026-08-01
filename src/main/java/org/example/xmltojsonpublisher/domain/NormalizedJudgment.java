package org.example.xmltojsonpublisher.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record NormalizedJudgment (@JsonProperty("content_id") String contentId,
                                  String title,
                                  String court,
                                  String jurisdiction,
                                  @JsonProperty("decision_date") String decisionDate,
                                  List<Citation> citations,
                                  List<Party> parties,
                                  List<Paragraph> paragraphs,
                                  @JsonProperty("full_text") String fullText) {
    public record Citation(String type, String value) {}
    public record Party(String role, String name) {}
    public record Paragraph(String id, String section, String text) {}

}
