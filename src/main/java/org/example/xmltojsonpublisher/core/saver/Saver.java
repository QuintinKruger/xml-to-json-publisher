package org.example.xmltojsonpublisher.core.saver;

import org.example.xmltojsonpublisher.domain.NormalizedJudgment;

public interface Saver {

    void saveNormalizedJudgement(NormalizedJudgment normalizedJudgment, String identifier) throws Exception;

    void saveRagText(String  text, String identifier) throws Exception;
}
