package org.example.xmltojsonpublisher.core.transformer.rag;

import org.example.xmltojsonpublisher.domain.NormalizedJudgment;
import org.springframework.stereotype.Component;

@Component
public class StrategyOneRagTransformer implements RagTransformer{

    @Override
    public String transform(NormalizedJudgment normalizedJudgment) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(normalizedJudgment.title());
        stringBuilder.append("\n\n");
        for (int i = 0; i< normalizedJudgment.paragraphs().size(); i++) {
            stringBuilder.append(normalizedJudgment.paragraphs().get(i).text());
            if (i < normalizedJudgment.paragraphs().size() - 1) {
                stringBuilder.append("\n\n");
            }
        }
        return stringBuilder.toString();
    }
}
