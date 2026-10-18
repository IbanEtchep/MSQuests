package com.github.ibanetchep.msquests.bukkit.document;

import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * The Artisan projects as the quests read them: every project of the server, not only the
 * module's own, since a {@code quest_group} Document may live in any of them. Paths use '/'.
 * Backed by Artisan's {@code ProjectContentApi} on a server ({@code ArtisanProjectFiles}), by
 * plain directories in tests and in the converter.
 */
public interface ProjectFiles {

    /** Ids of the projects, sorted. */
    List<String> projectIds();

    /** Relative paths of the files under {@code <projectId>/<dir>/}, sorted; empty if absent. */
    List<String> list(String projectId, String dir);

    /** UTF-8 content of a project file, or null if absent. */
    @Nullable String read(String projectId, String path);

    /** One project read from {@code dir}. */
    static ProjectFiles directory(String projectId, Path dir) {
        return directories(Map.of(projectId, dir));
    }

    /** Projects read from directories, by id. */
    static ProjectFiles directories(Map<String, Path> dirs) {
        Map<String, Path> sorted = new LinkedHashMap<>();
        dirs.keySet().stream().sorted().forEach(id -> sorted.put(id, dirs.get(id)));
        return new ProjectFiles() {
            @Override
            public List<String> projectIds() {
                return List.copyOf(sorted.keySet());
            }

            @Override
            public List<String> list(String projectId, String dir) {
                Path project = sorted.get(projectId);
                if (project == null || !Files.isDirectory(project.resolve(dir))) return List.of();
                Path root = project.resolve(dir);
                try (Stream<Path> walk = Files.walk(root)) {
                    return walk.filter(Files::isRegularFile)
                            .map(p -> root.relativize(p).toString().replace('\\', '/'))
                            .sorted()
                            .toList();
                } catch (IOException e) {
                    throw new UncheckedIOException("Cannot list " + root, e);
                }
            }

            @Override
            public @Nullable String read(String projectId, String path) {
                Path project = sorted.get(projectId);
                if (project == null) return null;
                Path file = project.resolve(path);
                if (!Files.isRegularFile(file)) return null;
                try {
                    return Files.readString(file, StandardCharsets.UTF_8);
                } catch (IOException e) {
                    throw new UncheckedIOException("Cannot read " + file, e);
                }
            }
        };
    }
}
