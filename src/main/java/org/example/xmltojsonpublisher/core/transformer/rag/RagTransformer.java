package org.example.xmltojsonpublisher.core.transformer.rag;

import org.example.xmltojsonpublisher.domain.NormalizedJudgment;

public interface RagTransformer {

    String transform(NormalizedJudgment normalizedJudgment);

}
