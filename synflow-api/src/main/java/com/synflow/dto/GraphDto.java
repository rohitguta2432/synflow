package com.synflow.dto;

import java.util.List;
import java.util.UUID;

public record GraphDto(
        List<GraphNode> nodes,
        List<GraphEdge> edges
) {
    public record GraphNode(UUID id, String name, String type, String industry) {}
    public record GraphEdge(UUID source, UUID target, String connectionType) {}
}
