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
import java.nio.file.Paths;

@Component
public class DiskSaver implements Saver {

    private final File directory;
    private final ObjectMapper objectMapper;

    public DiskSaver(@Value("${disk-saver.path}") String path, ObjectMapper objectMapper) {
        directory = new File(path);
        this.objectMapper = objectMapper;
    }

    @Override
    public void save(NormalizedJudgment normalizedJudgment, String ragText, String identifier) throws IOException {
        File judgementDirectory = new File(directory, identifier);
        Files.createDirectories(judgementDirectory.toPath());
        File jsonFile = new File(judgementDirectory, "%s.json".formatted(identifier));
        File ragFile = new File(judgementDirectory, "%s.txt".formatted(identifier));
        objectMapper.writeValue(jsonFile, normalizedJudgment);
        try (FileWriter fileWriter = new FileWriter(ragFile, StandardCharsets.UTF_8)) {
            fileWriter.write(ragText);
        }
    }
    // todo: perhaps move out to saveRagText and saveNormalizedJudgment
    // move save to interface, update interface to be abstract, it should call
    // saveRagText and saveNormalized - accept that  File judgementDirectory = new File(directory, identifier);
    // Files.createDirectories(judgementDirectory.toPath()); will be duplicated - in context of both these functions
    // both are unaware that the directory exists - one will create it and the other one wont require creation
    // that we know from context of how the save is setup - not something the individual methods know which is fine I guess

    @Override
    public boolean exists(String identifier) {
        File file = new File(directory, identifier);
        return file.exists();
    }
}
