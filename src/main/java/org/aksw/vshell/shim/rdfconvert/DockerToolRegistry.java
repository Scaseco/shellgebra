package org.aksw.vshell.shim.rdfconvert;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

class ImageEntry {

    private String toolName;
    private String image;

    // private boolean isAbsent;

    private Set<String> knownPresentLocations = new LinkedHashSet<>();
    private Set<String> knownAbsentLocations = new LinkedHashSet<>();

    public ImageEntry(String toolName, String image) {
        super();
        this.toolName = toolName;
        this.image = image;
    }

    public String getToolName() {
        return toolName;
    }

    public String getImage() {
        return image;
    }

    public Set<String> getKnownPresentLocations() {
        return knownPresentLocations;
    }

    public Set<String> getKnownAbsentLocations() {
        return knownAbsentLocations;
    }

    public ImageEntry declarePresence(String location) {
        this.knownPresentLocations.add(location);
        return this;
    }

    public ImageEntry declareAbsence(String location) {
        this.knownAbsentLocations.add(location);
        return this;
    }
}

record ToolEntry(String toolName, String imageName, String command) {}

public class DockerToolRegistry {

    protected Map<String, ToolEntry> primaryEntry;

    public DockerToolRegistry declarePrimary(String toolName, String imageName, String command) {
        primaryEntry.put(toolName, new ToolEntry(toolName, imageName, command));
        return this;
    }

    protected Table<String, String, ImageEntry> toolTable = HashBasedTable.create();

    public DockerToolRegistry declarePresence(String programName, String imageName, String command) {
        toolTable.row(programName).computeIfAbsent(imageName, in -> new ImageEntry(programName, in)).declarePresence(command);
        return this;
    }

    public DockerToolRegistry declareAbsence(String programName, String imageName, String command) {
        toolTable.row(programName).computeIfAbsent(imageName, in -> new ImageEntry(programName, in)).declareAbsence(command);
        return this;
    }

}
