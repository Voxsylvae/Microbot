package net.runelite.client.plugins.microbot.util.walker;

import lombok.Builder;
import lombok.Value;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.plugins.microbot.shortestpath.Transport;
import net.runelite.client.plugins.microbot.shortestpath.TransportType;
import java.util.List;
import java.util.Map;

/**
 * Encapsulates the result of a pathfinding operation, including the path, total tile distance,
 * transports used, missing transports, and performance metrics.
 */
@Value
@Builder(toBuilder = true)
public class PathAnalysisResult {
    /** The calculated path as a list of world points. */
    List<WorldPoint> path;

    /** The total number of tiles from start to target along the path. */
    int totalTiles;

    /** The list of transports used or available along the path. */
    @Builder.Default
    List<Transport> transports = List.of();

    /** The list of missing transports required for the path. */
    @Builder.Default
    List<Transport> missingTransports = List.of();

    /** Map of missing transport item IDs with their required quantities. */
    @Builder.Default
    Map<Integer, Integer> missingItemsWithQuantities = Map.of();

    /** The start point of the path. */
    WorldPoint startPoint;

    /** The target point of the path. */
    WorldPoint targetPoint;

    /** Time taken to calculate the path in milliseconds. */
    double calculationTimeMs;

    /** Whether bank items were considered in pathfinding. */
    boolean usedBankItems;

    /** Whether PoH transports were considered in pathfinding. */
    boolean usedPoHTransports;

    /** Whether advertisement house was used for PoH calculations. */
    boolean usedAdvertisementHouse;

    /** Performance breakdown of the calculation. */
    String performanceDetails;

    /**
     * Checks if the path is valid (not empty and reaches the destination).
     */
    public boolean isValid() {
        return path != null && !path.isEmpty() && totalTiles != Integer.MAX_VALUE;
    }

    /**
     * Gets the efficiency score based on total tiles (lower is better).
     */
    public int getEfficiencyScore() {
        return isValid() ? totalTiles : Integer.MAX_VALUE;
    }

    /**
     * Checks if any transports are missing requirements.
     */
    public boolean hasMissingTransports() {
        return missingTransports != null && !missingTransports.isEmpty();
    }

    /**
     * Gets a summary string of the path analysis.
     */
    public String getSummary() {
        if (!isValid()) {
            return "Invalid path";
        }
        return String.format("Path: %d tiles, %d transports (%d missing)", 
                           totalTiles, transports.size(), missingTransports.size());
    }
}