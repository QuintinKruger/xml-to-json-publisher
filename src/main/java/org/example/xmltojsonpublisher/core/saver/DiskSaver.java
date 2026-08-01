package org.example.xmltojsonpublisher.core.saver;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.xmltojsonpublisher.domain.NormalizedJudgment;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;

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
    public void saveNormalizedJudgement(NormalizedJudgment normalizedJudgment, String identifier) throws IOException {
        File file = new File(directory, "%s.json".formatted(identifier));
        if (file.exists()) {
            throw new FileAlreadyExistsException(file.getAbsolutePath());
        }
        objectMapper.writeValue(file, normalizedJudgment);
    }

    @Override
    public void saveRagText(String text, String identifier) throws IOException {
        File file = new File(directory, "%s.txt".formatted(identifier));
        if (file.exists()) {
            throw new FileAlreadyExistsException(file.getAbsolutePath());
        }
        try (FileWriter fileWriter = new FileWriter(file, StandardCharsets.UTF_8)) {
            fileWriter.write(text);
        }


    }
}
