package com.example.pathsandbox.app.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import java.time.LocalDateTime;

@Entity
public class PathExecutionLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String plannerName;
    private String inputMap;
    private String startCoord;
    private String goalCoord;
    private int pathLength;
    private double actualDistance;
    private long nodesExpanded;
    private long executionTimeMs;
    
    @Column(length = 2000)
    private String generatedUrl;

    private LocalDateTime executionDate;

    public PathExecutionLog() {
        this.executionDate = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getPlannerName() { return plannerName; }
    public void setPlannerName(String plannerName) { this.plannerName = plannerName; }
    public String getInputMap() { return inputMap; }
    public void setInputMap(String inputMap) { this.inputMap = inputMap; }
    public String getStartCoord() { return startCoord; }
    public void setStartCoord(String startCoord) { this.startCoord = startCoord; }
    public String getGoalCoord() { return goalCoord; }
    public void setGoalCoord(String goalCoord) { this.goalCoord = goalCoord; }
    public int getPathLength() { return pathLength; }
    public void setPathLength(int pathLength) { this.pathLength = pathLength; }
    public double getActualDistance() { return actualDistance; }
    public void setActualDistance(double actualDistance) { this.actualDistance = actualDistance; }
    public long getNodesExpanded() { return nodesExpanded; }
    public void setNodesExpanded(long nodesExpanded) { this.nodesExpanded = nodesExpanded; }
    public long getExecutionTimeMs() { return executionTimeMs; }
    public void setExecutionTimeMs(long executionTimeMs) { this.executionTimeMs = executionTimeMs; }
    public String getGeneratedUrl() { return generatedUrl; }
    public void setGeneratedUrl(String generatedUrl) { this.generatedUrl = generatedUrl; }
    public LocalDateTime getExecutionDate() { return executionDate; }
    public void setExecutionDate(LocalDateTime executionDate) { this.executionDate = executionDate; }
}
