package org.codehaus.mojo.versions;

import com.google.gson.GsonBuilder;
import org.apache.maven.artifact.versioning.ArtifactVersion;
import org.apache.maven.model.Dependency;
import org.codehaus.mojo.versions.api.ArtifactVersions;
import org.codehaus.mojo.versions.api.UpdateScope;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class DependencyUpdatesJsonRenderer {

    private final Map<Dependency, ArtifactVersions> dependencyUpdates;
    private final Map<Dependency, ArtifactVersions> dependencyManagementUpdates;
    private final String outputFileName;
    private final boolean allowSnapshots;


    public DependencyUpdatesJsonRenderer(
            Map<Dependency, ArtifactVersions> dependencyUpdates,
            Map<Dependency, ArtifactVersions> dependencyManagementUpdates,
            String outputFileName,
            boolean allowSnapshots
    ) {
        this.dependencyUpdates = dependencyUpdates;
        this.dependencyManagementUpdates = dependencyManagementUpdates;
        this.outputFileName = outputFileName;
        this.allowSnapshots = allowSnapshots;
    }

    public void render() {
        Report report = new Report();
        report.setReportDate(new Date().toString());
        report.setDependencyManagementUpdates(new Dependencies(buildList(dependencyManagementUpdates)));
        report.setDependencyUpdates(new Dependencies(buildList(dependencyUpdates)));

        try (FileWriter fileWriter = new FileWriter(outputFileName)) {
            GsonBuilder builder = new GsonBuilder().setPrettyPrinting();
            builder.create().toJson(report, fileWriter);
            fileWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private List<DependencyEntry> buildList(Map<Dependency, ArtifactVersions> updates) {

        List<DependencyEntry> result = new ArrayList<>();

        Iterator i = updates.values().iterator();
        while (i.hasNext()) {
            ArtifactVersions versions = (ArtifactVersions) i.next();
            final ArtifactVersion current;
            ArtifactVersion latest;
            if (versions.isCurrentVersionDefined()) {
                current = versions.getCurrentVersion();
                latest = versions.getNewestUpdate(UpdateScope.ANY, allowSnapshots);
            } else {
                ArtifactVersion newestVersion =
                        versions.getNewestVersion(versions.getArtifact().getVersionRange(), allowSnapshots);
                current = versions.getArtifact().getVersionRange().getRecommendedVersion();
                latest = newestVersion == null ? null
                        : versions.getNewestUpdate(newestVersion, UpdateScope.ANY, allowSnapshots);
                if (latest != null
                        && ArtifactVersions.isVersionInRange(latest, versions.getArtifact().getVersionRange())) {
                    latest = null;
                }
            }
            DependencyEntry entry = new DependencyEntry();
            entry.setGroupId(versions.getGroupId());
            entry.setArtifactId(versions.getArtifactId());
            entry.setNewVersion(latest.toString());
            entry.setCurrentVersion(current.toString());
            result.add(entry);
        }
        return result;
    }
}

class Report {

    private String reportDate;
    private Dependencies dependencyUpdates;
    private Dependencies dependencyManagementUpdates;

    public Dependencies getDependencyUpdates() {
        return dependencyUpdates;
    }

    public void setDependencyUpdates(Dependencies dependencyUpdates) {
        this.dependencyUpdates = dependencyUpdates;
    }

    public Dependencies getDependencyManagementUpdates() {
        return dependencyManagementUpdates;
    }

    public void setDependencyManagementUpdates(Dependencies dependencyManagementUpdates) {
        this.dependencyManagementUpdates = dependencyManagementUpdates;
    }

    public String getReportDate() {
        return reportDate;
    }

    public void setReportDate(String reportDate) {
        this.reportDate = reportDate;
    }
}

class Dependencies {

    private List<DependencyEntry> dependencies;

    public Dependencies(List<DependencyEntry> dependencies) {
        this.dependencies = dependencies;
    }

    public List<DependencyEntry> getDependencies() {
        return dependencies;
    }

    public void setDependencies(List<DependencyEntry> dependencies) {
        this.dependencies = dependencies;
    }
}


class DependencyEntry {

    private String currentVersion;
    private String newVersion;
    private String artifactId;
    private String groupId;

    public String getCurrentVersion() {
        return currentVersion;
    }

    public void setCurrentVersion(String currentVersion) {
        this.currentVersion = currentVersion;
    }

    public String getNewVersion() {
        return newVersion;
    }

    public void setNewVersion(String newVersion) {
        this.newVersion = newVersion;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public void setArtifactId(String artifactId) {
        this.artifactId = artifactId;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }
}
