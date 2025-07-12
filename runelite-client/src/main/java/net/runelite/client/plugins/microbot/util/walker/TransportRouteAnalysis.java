package net.runelite.client.plugins.microbot.util.walker;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import lombok.Builder;
import lombok.Getter;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.plugins.microbot.shortestpath.Transport;
import net.runelite.client.plugins.microbot.shortestpath.TransportType;
import net.runelite.client.plugins.microbot.util.bank.enums.BankLocation;
import net.runelite.client.plugins.microbot.util.poh.data.PoHTeleport;

/**
 * Represents a comparative analysis between different routes to a destination.
 * This class stores information about multiple route options including:
 * - Direct route to destination
 * - Route via bank (for banked transport items)
 * - Route via Player-Owned House (for PoH transport options)
 * Used by Rs2Walker.compareRoutes() to help determine the most efficient path.
 */
@Getter
@Builder
public class TransportRouteAnalysis {
    /** Complete path of WorldPoints representing the direct route to destination */
    @Builder.Default
    private final List<WorldPoint> directPath = new ArrayList<>();
    
    /** Reference to the nearest accessible BankLocation object, null if no bank is accessible */
    private final BankLocation nearestBank;
    
    /** WorldPoint coordinates of the nearest bank location, null if no bank is accessible */
    private final WorldPoint bankLocation;
    
    /** Path of WorldPoints from starting point to the nearest bank */
    @Builder.Default
    private final List<WorldPoint> pathToBank = new ArrayList<>();
    
    /** Path of WorldPoints from bank to destination, accounting for items available in bank */
    @Builder.Default
    private final List<WorldPoint> pathFromBank = new ArrayList<>();
    
    // PoH Route Analysis Fields
    /** WorldPoint coordinates of the PoH portal location (Rimmington for advertisement house, player's house for own house) */
    private final WorldPoint pohPortalLocation;
    
    /** Path of WorldPoints from starting point to PoH portal */
    @Builder.Default
    private final List<WorldPoint> pathToPoH = new ArrayList<>();
    
    /** Path of WorldPoints from PoH portal to destination via PoH transport, null if no PoH transport available */
    @Builder.Default
    private final List<WorldPoint> pathFromPoH = new ArrayList<>();
    
    /** PoH teleport that can be used for this route, null if none available */
    private final PoHTeleport pohTeleport;
    
    /** List of transport items required for the optimal route that are missing from inventory/equipment */
    @Builder.Default
    private final List<Transport> missingTransports = new ArrayList<>();
    
    /** Map of item IDs to quantities that are missing for transport requirements */
    @Builder.Default
    private final Map<Integer, Integer> missingItemsWithQuantities = new HashMap<>();
    
    /** Summary text describing the analysis results and recommendation */
    @Builder.Default
    private final String analysis = "";
    
    /**
     * Legacy constructor for backward compatibility.
     * Creates a TransportRouteAnalysis without PoH route information.
     */
    public TransportRouteAnalysis(List<WorldPoint> directPath, 
                                BankLocation nearestBank, WorldPoint bankLocation,List<WorldPoint> pathToBank,
                                List<WorldPoint> pathFromBank,String analysis) {
        this.directPath = directPath != null ? directPath : new ArrayList<>();
        this.nearestBank = nearestBank;
        this.bankLocation = bankLocation;
        this.pathToBank = pathToBank != null ? pathToBank : new ArrayList<>();
        this.pathFromBank = pathFromBank != null ? pathFromBank : new ArrayList<>();
        this.pohPortalLocation = null;
        this.pathToPoH = new ArrayList<>();
        this.pathFromPoH = new ArrayList<>();
        this.pohTeleport = null;
        this.missingTransports = new ArrayList<>();
        this.missingItemsWithQuantities = new HashMap<>();
        this.analysis = analysis != null ? analysis : "";
    }
    
    /**
     * Convenience constructor for direct route only.
     * Creates a TransportRouteAnalysis with only direct path information.
     */
    public TransportRouteAnalysis(List<WorldPoint> directPath, String analysis) {
        this.directPath = directPath != null ? directPath : new ArrayList<>();
        this.nearestBank = null;
        this.bankLocation = null;
        this.pathToBank = new ArrayList<>();
        this.pathFromBank = new ArrayList<>();
        this.pohPortalLocation = null;
        this.pathToPoH = new ArrayList<>();
        this.pathFromPoH = new ArrayList<>();
        this.pohTeleport = null;
        this.missingTransports = new ArrayList<>();
        this.missingItemsWithQuantities = new HashMap<>();
        this.analysis = analysis != null ? analysis : "";
    }
    
    /**
     * Full constructor for complete route analysis.
     * Creates a TransportRouteAnalysis with all route information.
     */
    public TransportRouteAnalysis(List<WorldPoint> directPath, 
                                BankLocation nearestBank, WorldPoint bankLocation,
                                List<WorldPoint> pathToBank, List<WorldPoint> pathFromBank,
                                WorldPoint pohPortalLocation, List<WorldPoint> pathToPoH, List<WorldPoint> pathFromPoH,
                                PoHTeleport pohTeleport, List<Transport> missingTransports,
                                Map<Integer, Integer> missingItemsWithQuantities, String analysis) {
        this.directPath = directPath != null ? directPath : new ArrayList<>();
        this.nearestBank = nearestBank;
        this.bankLocation = bankLocation;
        this.pathToBank = pathToBank != null ? pathToBank : new ArrayList<>();
        this.pathFromBank = pathFromBank != null ? pathFromBank : new ArrayList<>();
        this.pohPortalLocation = pohPortalLocation;
        this.pathToPoH = pathToPoH != null ? pathToPoH : new ArrayList<>();
        this.pathFromPoH = pathFromPoH != null ? pathFromPoH : new ArrayList<>();
        this.pohTeleport = pohTeleport;
        this.missingTransports = missingTransports != null ? missingTransports : new ArrayList<>();
        this.missingItemsWithQuantities = missingItemsWithQuantities != null ? missingItemsWithQuantities : new HashMap<>();
        this.analysis = analysis != null ? analysis : "";
    }
    
    /**
     * Calculates the direct route distance in tiles from the stored path.
     * @return The direct route distance, or -1 if path is empty or invalid
     */
    public int getDirectDistance() {
        if (directPath == null || directPath.isEmpty()) return -1;
        return directPath.size();
    }
    
    /**
     * Calculates the banking route distance in tiles from the stored paths.
     * @return The total banking route distance (to bank + from bank), or -1 if paths are invalid
     */
    public int getBankingRouteDistance() {
        if (pathToBank == null || pathFromBank == null || 
            pathToBank.isEmpty() || pathFromBank.isEmpty()) return -1;
        return pathToBank.size() + pathFromBank.size();
    }
    
    /**
     * Calculates the PoH route distance in tiles from the stored paths.
     * @return The total PoH route distance (to PoH + from PoH), or -1 if paths are invalid
     */
    public int getPoHRouteDistance() {
        if (pathToPoH == null || pathFromPoH == null || 
            pathToPoH.isEmpty() || pathFromPoH.isEmpty()) return -1;
        return pathToPoH.size() + pathFromPoH.size();
    }
    
    /**
     * Calculates the combined route distance (bank -> PoH -> target) in tiles.
     * This represents the route: start -> bank -> PoH portal -> PoH teleport destination -> target
     * @return The total combined route distance, or -1 if paths are invalid
     */
    public int getCombinedRouteDistance() {
        if (pathToBank == null || pathToBank.isEmpty() || 
            pathToPoH == null || pathToPoH.isEmpty() || 
            pathFromPoH == null || pathFromPoH.isEmpty()) {
            return -1;
        }
        
        // Calculate: start -> bank + bank -> PoH + PoH teleport destination -> target
        int toBankDistance = pathToBank.size() - 1; // -1 because path includes both start and end points
        int bankToPoHDistance = pathToPoH.size() - 1; // In the case of we also have a banking route, the start location is the bank to PoH
        int fromPoHToTargetDistance = pathFromPoH.size() - 1;
        
        
        
        return toBankDistance + bankToPoHDistance + fromPoHToTargetDistance;
    }
    
    /**
     * Determines if the direct route is faster than all alternative routes.
     * When distances are equal, direct route is considered faster.
     * 
     * @return true if direct route is faster or equal to all alternatives
     */
    public boolean isDirectIsFaster() {
        RouteType optimal = getOptimalRouteType();
        return optimal == RouteType.DIRECT;
    }
    
    /**
     * Determines if PoH route is available for this analysis.
     * @return true if PoH route information is available
     */
    public boolean hasPoHRoute() {
        return pohPortalLocation != null && pohTeleport != null && 
               pathToPoH != null && !pathToPoH.isEmpty() &&
               pathFromPoH != null && !pathFromPoH.isEmpty();
    }
    
    /**
     * Gets the optimal route type based on distance comparison.
     * @return RouteType enum indicating the best route
     */
    public RouteType getOptimalRouteType() {
        int directDist = getDirectDistance();
        int bankingDist = getBankingRouteDistance();
        int pohDist = getPoHRouteDistance();
        int combinedDist = getCombinedRouteDistance();
        
        // Find the minimum valid distance
        int minDistance = Integer.MAX_VALUE;
        RouteType optimalType = RouteType.DIRECT; // Default to direct
        
        if (directDist != -1 && directDist < minDistance) {
            minDistance = directDist;
            optimalType = RouteType.DIRECT;
        }
        
        if (bankingDist != -1 && bankingDist < minDistance) {
            minDistance = bankingDist;
            optimalType = RouteType.BANKING;
        }
        
        if (pohDist != -1 && pohDist < minDistance) {
            minDistance = pohDist;
            optimalType = RouteType.POH;
        }
        
        if (combinedDist != -1 && combinedDist < minDistance) {
            minDistance = combinedDist;
            optimalType = RouteType.COMBINED;
        }
        
        return optimalType;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("TransportRouteAnalysis {\n");
        sb.append("\toptimalRoute: ").append(getOptimalRouteType().getDescription()).append("\n");
        sb.append("\tdirectIsFaster: ").append(isDirectIsFaster()).append("\n");
        
        int directDist = getDirectDistance();
        int bankingDist = getBankingRouteDistance();
        int pohDist = getPoHRouteDistance();
        int combinedDist = getCombinedRouteDistance();
        
        sb.append("\tdirectDistance: ").append(directDist == -1 ? "N/A" : directDist).append(" tiles\n");
        sb.append("\tbankingRouteDistance: ").append(bankingDist == -1 ? "N/A" : bankingDist).append(" tiles\n");
        sb.append("\tpohRouteDistance: ").append(pohDist == -1 ? "N/A" : pohDist).append(" tiles\n");
        sb.append("\tcombinedRouteDistance: ").append(combinedDist == -1 ? "N/A" : combinedDist).append(" tiles\n");
        sb.append("\ttileSavings: ").append(getTileSavings()).append(" tiles\n");
        
        sb.append("\tnearestBank: ").append(nearestBank != null ? nearestBank.name() : "None").append("\n");
        sb.append("\tbankLocation: ").append(bankLocation != null ? bankLocation : "N/A").append("\n");
        sb.append("\tpohPortalLocation: ").append(pohPortalLocation != null ? pohPortalLocation : "N/A").append("\n");
        sb.append("\tpohTeleport: ").append(pohTeleport != null ? pohTeleport.toString() : "None").append("\n");
        sb.append("\thasPoHRoute: ").append(hasPoHRoute()).append("\n");
        
        sb.append("\tanalysis: \"").append(analysis != null ? analysis : "No analysis available").append("\"\n");
        sb.append("}");
        return sb.toString();
    }
    
    /**
     * Gets all required transports for the direct path with default parameters.
     * @return List of required transports for direct path
     */
    public List<Transport> getTransportsForDirectPath(){            
        return getTransportsForDirectPath(0, TransportType.TELEPORTATION_ITEM, true);
    }
    
    /**
     * Gets all required transports for the direct path with custom parameters.
     * @param startIndex The index to start from in the path
     * @param prefTransportType The preferred transport type
     * @param applyFiltering Whether to apply filtering
     * @return List of required transports for direct path
     */
    public List<Transport> getTransportsForDirectPath(int startIndex, TransportType prefTransportType, boolean applyFiltering){            
        List<Transport> transports = Rs2Walker.getTransportsForPath(directPath, startIndex, prefTransportType, applyFiltering);
        return transports;
    }
    
    /**
     * Gets all required transports for the banking route with default parameters.
     * @return List of required transports for banking route (to and from bank)
     */
    public List<Transport> getTransportsForBankingPath(){            
        return getTransportsForBankingPath(0, TransportType.TELEPORTATION_ITEM, true);
    }
    
    /**
     * Gets all required transports for the banking route with custom parameters.
     * @param startIndex The index to start from in the path
     * @param prefTransportType The preferred transport type
     * @param applyFiltering Whether to apply filtering
     * @return List of required transports for banking route (to and from bank)
     */
    public List<Transport> getTransportsForBankingPath(int startIndex, TransportType prefTransportType, boolean applyFiltering){            
        List<Transport> transportsToTargetToBank = Rs2Walker.getTransportsForPath(pathToBank, startIndex, prefTransportType, applyFiltering);            
        List<Transport> transportsToTargetFromBank = Rs2Walker.getTransportsForPath(pathFromBank, startIndex, prefTransportType, applyFiltering);            
        return new ArrayList<>(){{
            addAll(transportsToTargetToBank);
            addAll(transportsToTargetFromBank);
        }};
    }
    
    /**
     * Gets missing transports for the direct path.
     * @return List of missing transports for direct path
     */
    public List<Transport> getMissingTransportsForDirectPath(){
        List<Transport> missingTransports = Rs2Walker.getMissingTransports(getTransportsForDirectPath());
        return missingTransports;
    }
    
    /**
     * Gets missing transport items with their quantities for the direct path.
     * @return Map of item IDs to their required quantities
     */
    public Map<Integer, Integer> getMissingTransportsItemsWithQuantitiesForDirectPath(){
        List<Transport> missingTransports = getMissingTransportsForDirectPath();
        Map<Integer, Integer> missingItemsWithQuantities = Rs2Walker.getMissingTransportItemIdsWithQuantities(missingTransports);
        return missingItemsWithQuantities;
    }
    
    /**
     * Gets missing transports for the banking route (to and from bank).
     * @return List of missing transports for the banking route
     */
    public List<Transport> getMissingTransportsForBankingRoute(){
        List<Transport> missingTransports = Rs2Walker.getMissingTransports(getTransportsForBankingPath(0, TransportType.TELEPORTATION_ITEM, true));
        return missingTransports;
    }
    
    /**
     * Gets missing transport items with their quantities for the banking route.
     * @return Map of item IDs to their required quantities
     */
    public Map<Integer, Integer> getMissingTransportsItemsWithQuantitiesForBankingRoute(){
        List<Transport> missingTransports = getMissingTransportsForBankingRoute();
        Map<Integer, Integer> missingItemsWithQuantities = Rs2Walker.getMissingTransportItemIdsWithQuantities(missingTransports);
        return missingItemsWithQuantities;
    }
    
    /**
     * Gets all required transports for the path to bank with default parameters.
     * @return List of required transports for path to bank
     */
    public List<Transport> getTransportsForPathToBank() {
        return getTransportsForPathToBank(0, TransportType.TELEPORTATION_ITEM, true);
    }
    
    /**
     * Gets all required transports for the path to bank with custom parameters.
     * @param startIndex The index to start from in the path
     * @param prefTransportType The preferred transport type
     * @param applyFiltering Whether to apply filtering
     * @return List of required transports for path to bank
     */
    public List<Transport> getTransportsForPathToBank(int startIndex, TransportType prefTransportType, boolean applyFiltering) {
        return Rs2Walker.getTransportsForPath(pathToBank, startIndex, prefTransportType, applyFiltering);
    }
    
    /**
     * Gets all required transports for the path from bank with default parameters.
     * @return List of required transports for path from bank
     */
    public List<Transport> getTransportsForPathFromBank() {
        return getTransportsForPathFromBank(0, TransportType.TELEPORTATION_ITEM, true);
    }
    
    /**
     * Gets all required transports for the path from bank with custom parameters.
     * @param startIndex The index to start from in the path
     * @param prefTransportType The preferred transport type
     * @param applyFiltering Whether to apply filtering
     * @return List of required transports for path from bank
     */
    public List<Transport> getTransportsForPathFromBank(int startIndex, TransportType prefTransportType, boolean applyFiltering) {
        return Rs2Walker.getTransportsForPath(pathFromBank, startIndex, prefTransportType, applyFiltering);
    }
    
    /**
     * Gets missing transports specifically for the path from bank to destination.
     * @return List of missing transports for path from bank
     */
    public List<Transport> getMissingTransportsForPathFromBank() {
        List<Transport> missingTransports = Rs2Walker.getMissingTransports(getTransportsForPathFromBank());
        return missingTransports;
    }
    
    /**
     * Gets missing transport items with their quantities specifically for the path from bank to destination.
     * @return Map of item IDs to their required quantities for path from bank
     */
    public Map<Integer, Integer> getMissingTransportsItemsWithQuantitiesForPathFromBank() {
        List<Transport> missingTransports = getMissingTransportsForPathFromBank();
        Map<Integer, Integer> missingItemsWithQuantities = Rs2Walker.getMissingTransportItemIdsWithQuantities(missingTransports);
        return missingItemsWithQuantities;
    }
    
    // ========== PoH Route Analysis Methods ==========
    
    /**
     * Gets all required transports for the PoH route with default parameters.
     * @return List of required transports for PoH route (to PoH + from PoH)
     */
    public List<Transport> getTransportsForPoHRoute() {
        return getTransportsForPoHRoute(0, TransportType.TELEPORTATION_ITEM, true);
    }
    
    /**
     * Gets all required transports for the PoH route with custom parameters.
     * @param startIndex The index to start from in the path
     * @param prefTransportType The preferred transport type
     * @param applyFiltering Whether to apply filtering
     * @return List of required transports for PoH route (to PoH + from PoH)
     */
    public List<Transport> getTransportsForPoHRoute(int startIndex, TransportType prefTransportType, boolean applyFiltering) {
        List<Transport> transportsToPoH = getTransportsForPathToPoH(startIndex, prefTransportType, applyFiltering);
        List<Transport> transportsFromPoH = getTransportsForPathFromPoH(startIndex, prefTransportType, applyFiltering);
        
        List<Transport> allTransports = new ArrayList<>();
        allTransports.addAll(transportsToPoH);
        allTransports.addAll(transportsFromPoH);
        return allTransports;
    }
    
    /**
     * Gets all required transports for the path to PoH with default parameters.
     * @return List of required transports for path to PoH
     */
    public List<Transport> getTransportsForPathToPoH() {
        return getTransportsForPathToPoH(0, TransportType.TELEPORTATION_ITEM, true);
    }
    
    /**
     * Gets all required transports for the path to PoH with custom parameters.
     * @param startIndex The index to start from in the path
     * @param prefTransportType The preferred transport type
     * @param applyFiltering Whether to apply filtering
     * @return List of required transports for path to PoH
     */
    public List<Transport> getTransportsForPathToPoH(int startIndex, TransportType prefTransportType, boolean applyFiltering) {
        if (pathToPoH == null || pathToPoH.isEmpty()) return new ArrayList<>();
        return Rs2Walker.getTransportsForPath(pathToPoH, startIndex, prefTransportType, applyFiltering);
    }
    
    /**
     * Gets all required transports for the path from PoH with default parameters.
     * @return List of required transports for path from PoH
     */
    public List<Transport> getTransportsForPathFromPoH() {
        return getTransportsForPathFromPoH(0, TransportType.TELEPORTATION_ITEM, true);
    }
    
    /**
     * Gets all required transports for the path from PoH with custom parameters.
     * @param startIndex The index to start from in the path
     * @param prefTransportType The preferred transport type
     * @param applyFiltering Whether to apply filtering
     * @return List of required transports for path from PoH
     */
    public List<Transport> getTransportsForPathFromPoH(int startIndex, TransportType prefTransportType, boolean applyFiltering) {
        if (pathFromPoH == null || pathFromPoH.isEmpty()) return new ArrayList<>();
        return Rs2Walker.getTransportsForPath(pathFromPoH, startIndex, prefTransportType, applyFiltering);
    }
    
    /**
     * Gets missing transports for the PoH route (to and from PoH).
     * @return List of missing transports for the PoH route
     */
    public List<Transport> getMissingTransportsForPoHRoute() {
        List<Transport> missingTransports = Rs2Walker.getMissingTransports(getTransportsForPoHRoute());
        return missingTransports;
    }
    
    /**
     * Gets missing transport items with their quantities for the PoH route.
     * @return Map of item IDs to their required quantities for PoH route
     */
    public Map<Integer, Integer> getMissingTransportsItemsWithQuantitiesForPoHRoute() {
        List<Transport> missingTransports = getMissingTransportsForPoHRoute();
        Map<Integer, Integer> missingItemsWithQuantities = Rs2Walker.getMissingTransportItemIdsWithQuantities(missingTransports);
        return missingItemsWithQuantities;
    }
    
    /**
     * Gets missing transports specifically for the path from PoH to destination.
     * @return List of missing transports for path from PoH
     */
    public List<Transport> getMissingTransportsForPathFromPoH() {
        List<Transport> missingTransports = Rs2Walker.getMissingTransports(getTransportsForPathFromPoH());
        return missingTransports;
    }
    
    /**
     * Gets missing transport items with their quantities specifically for the path from PoH to destination.
     * @return Map of item IDs to their required quantities for path from PoH
     */
    public Map<Integer, Integer> getMissingTransportsItemsWithQuantitiesForPathFromPoH() {
        List<Transport> missingTransports = getMissingTransportsForPathFromPoH();
        Map<Integer, Integer> missingItemsWithQuantities = Rs2Walker.getMissingTransportItemIdsWithQuantities(missingTransports);
        return missingItemsWithQuantities;
    }
    
    // ========== Combined Route Analysis Methods ==========
    
    /**
     * Gets all required transports for the combined route (bank -> PoH -> target).
     * @return List of required transports for combined route
     */
    public List<Transport> getTransportsForCombinedRoute() {
        return getTransportsForCombinedRoute(0, TransportType.TELEPORTATION_ITEM, true);
    }
    
    /**
     * Gets all required transports for the combined route with custom parameters.
     * @param startIndex The index to start from in the path
     * @param prefTransportType The preferred transport type
     * @param applyFiltering Whether to apply filtering
     * @return List of required transports for combined route
     */
    public List<Transport> getTransportsForCombinedRoute(int startIndex, TransportType prefTransportType, boolean applyFiltering) {
        List<Transport> transportsToBank = getTransportsForPathToBank(startIndex, prefTransportType, applyFiltering);
        List<Transport> transportsFromPoH = getTransportsForPathFromPoH(startIndex, prefTransportType, applyFiltering);
        
        List<Transport> allTransports = new ArrayList<>();
        allTransports.addAll(transportsToBank);
        allTransports.addAll(transportsFromPoH);
        return allTransports;
    }
    
    /**
     * Gets missing transports for the combined route (bank -> PoH -> target).
     * @return List of missing transports for the combined route
     */
    public List<Transport> getMissingTransportsForCombinedRoute() {
        List<Transport> missingTransports = Rs2Walker.getMissingTransports(getTransportsForCombinedRoute());
        return missingTransports;
    }
    
    /**
     * Gets missing transport items with their quantities for the combined route.
     * @return Map of item IDs to their required quantities for combined route
     */
    public Map<Integer, Integer> getMissingTransportsItemsWithQuantitiesForCombinedRoute() {
        List<Transport> missingTransports = getMissingTransportsForCombinedRoute();
        Map<Integer, Integer> missingItemsWithQuantities = Rs2Walker.getMissingTransportItemIdsWithQuantities(missingTransports);
        return missingItemsWithQuantities;
    }
    
    // ========== Utility Methods ==========
    
    /**
     * Calculates the tile savings compared to the direct route.
     * @return The number of tiles saved by using the optimal route, negative if longer
     */
    public int getTileSavings() {
        int directDist = getDirectDistance();
        if (directDist == -1) return 0;
        
        RouteType optimal = getOptimalRouteType();
        int optimalDist;
        switch (optimal) {
            case DIRECT:
                optimalDist = directDist;
                break;
            case BANKING:
                optimalDist = getBankingRouteDistance();
                break;
            case POH:
                optimalDist = getPoHRouteDistance();
                break;
            case COMBINED:
                optimalDist = getCombinedRouteDistance();
                break;
            default:
                optimalDist = directDist;
                break;
        }
        
        return optimalDist == -1 ? 0 : directDist - optimalDist;
    }
    
    /**
     * Route type enumeration for the four different route strategies.
     */
    public enum RouteType {
        DIRECT("Direct route"),
        BANKING("Banking route"),
        POH("PoH route"),
        COMBINED("Combined banking + PoH route");

        private final String description;

        RouteType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
}