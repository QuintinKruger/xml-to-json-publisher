package org.example.xmltojsonpublisher.core.saver;

import org.example.xmltojsonpublisher.domain.NormalizedJudgment;

public interface Saver {

    void save(NormalizedJudgment normalizedJudgment, String ragText, String identifier) throws Exception;

    boolean exists(String identifier);
}
