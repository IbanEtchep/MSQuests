package com.github.ibanetchep.msquests.bukkit.artisan;

import com.github.ibanetchep.msquests.bukkit.document.ProjectFiles;
import net.artisanmc.modules.api.project.ProjectContentApi;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Every project Artisan knows, its own folder and the add-on roots alike, read from disk through
 * {@code api.getProject()}: the {@code .artisan*} metadata stays out of reach.
 */
public final class ArtisanProjectFiles implements ProjectFiles {

    private final ProjectContentApi content;

    public ArtisanProjectFiles(ProjectContentApi content) {
        this.content = content;
    }

    @Override
    public List<String> projectIds() {
        return content.projectIds().stream().sorted().toList();
    }

    @Override
    public List<String> list(String projectId, String dir) {
        return content.list(projectId, dir);
    }

    @Override
    public @Nullable String read(String projectId, String path) {
        return content.read(projectId, path);
    }
}
