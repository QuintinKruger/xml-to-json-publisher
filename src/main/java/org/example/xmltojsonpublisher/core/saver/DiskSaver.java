package org.example.xmltojsonpublisher.core.saver;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.xmltojsonpublisher.domain.NormalizedJudgment;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;

@Component
public class DiskSaver implements Saver {

    private final File directory;
    private final ObjectMapper objectMapper;

    public DiskSaver(@Value("${disk-saver.path}") String path, ObjectMapper objectMapper) {
        directory = new File(path);
        directory.mkdirs();
        this.objectMapper = objectMapper;
    }

    @Override
    public void saveNormalizedJudgement(NormalizedJudgment normalizedJudgment) throws IOException {
        File file = new File(directory, "%s.json".formatted(normalizedJudgment.contentId()));
        if (file.exists()) {
            throw new FileAlreadyExistsException(file.getAbsolutePath());
        }
        objectMapper.writeValue(file, normalizedJudgment);
    }
}
