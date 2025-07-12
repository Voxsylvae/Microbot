package net.runelite.client.plugins.microbot.util.poh;

import net.runelite.api.GameObject;
import net.runelite.api.TileObject;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.gameval.VarbitID;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.util.gameobject.Rs2GameObject;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;
import net.runelite.client.plugins.microbot.util.poh.data.PoHTeleport;
import net.runelite.client.plugins.microbot.util.poh.navigation.Rs2PoHNavigation;
import net.runelite.api.gameval.ObjectID;

import java.util.*;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableList;
import static net.runelite.client.plugins.microbot.util.Global.sleepUntilTrue;

/**
 * Utility class for Player-Owned House Portal Chamber room functionality.
 * Provides methods for detecting and interacting with portal frames.
 * Key features:
 * - Detection of portal frames in the current room
 * - Navigation to portal chamber within POH
 * - Interaction with portal frames by index or teleport destination
 * - Mapping of portal frames to their teleport destinations
 * 
 * Note: Portal nexus functionality has been moved to Rs2PortalNexus.java
 */
public class Rs2PortalChamber {
    // Portal Frame Object IDs (using ObjectID constants)
    private static final ImmutableList<Integer> PORTAL_TEAK_FRAME_IDS = ImmutableList.of(
        ObjectID.POH_PORTAL_TEAK_EMPTY,   //  Teak portal frame empty
        ObjectID.POH_PORTAL_TEAK_VARROCK, //  Teak portal frame with Varrock portal
        ObjectID.POH_PORTAL_TEAK_FALADOR, //  Teak portal frame with Falador portal
        ObjectID.POH_PORTAL_TEAK_LUMBRIDGE, // - Teak portal frame with Lumbridge portal
        ObjectID.POH_PORTAL_TEAK_ARDOUGNE, // - Teak portal frame with Ardougne portal
        ObjectID.POH_PORTAL_TEAK_CATHERBY, // - Teak portal frame with Catherby portal
        ObjectID.POH_PORTAL_TEAK_YANILLE, // - Teak portal frame with Yanille portal
        ObjectID.POH_PORTAL_TEAK_KHARYRLL, // - Teak portal frame with Kharyrll portal
        ObjectID.POH_PORTAL_TEAK_LUNARISLE, // - Teak portal frame with Lunar Isle portal
        ObjectID.POH_PORTAL_TEAK_SENNTISTEN, // - Teak portal frame with Senntisten portal
        ObjectID.POH_PORTAL_TEAK_KOUREND, // - Teak portal frame with Kourend portal
        ObjectID.POH_PORTAL_TEAK_ANNAKARL,// - Teak portal frame with Annekarl portal
        ObjectID.POH_PORTAL_TEAK_WATERBIRTH, // - Teak portal frame with Waterbirth portal
        ObjectID.POH_PORTAL_TEAK_FISHINGGUILD, // - Teak portal frame with Fishing Guild portal
        ObjectID.POH_PORTAL_TEAK_MARIM
    );

    // Portal Frame Object IDs (using ObjectID constants)
    private static final ImmutableList<Integer> PORTAL_MAG_FRAME_IDS = ImmutableList.of(                
        ObjectID.POH_PORTAL_MAG_EMPTY,  //  - mahogany portal frame
        ObjectID.POH_PORTAL_MAG_ANNAKARL, //  - mahogany portal frame with Annekarl portal
        ObjectID.POH_PORTAL_MAG_ARDOUGNE, //  - mahogany portal frame with Ardougne portal
        ObjectID.POH_PORTAL_MAG_CATHERBY, //  - mahogany portal frame with Catherby portal
        ObjectID.POH_PORTAL_MAG_FALADOR, //  - mahogany portal frame with Falador portal          
        ObjectID.POH_PORTAL_MAG_KHARYRLL, //  - mahogany portal frame with Kharyrll portal
        ObjectID.POH_PORTAL_MAG_LUMBRIDGE, //  - mahogany portal frame with Lumbridge portal        
        ObjectID.POH_PORTAL_MAG_VARROCK, //  - mahogany portal frame with Varrock portal
        ObjectID.POH_PORTAL_MAG_YANILLE, //  - mahogany portal frame with Yanille portal
        ObjectID.POH_PORTAL_MAG_LUNARISLE, //  - mahogany portal frame with Lunar Isle portal
        ObjectID.POH_PORTAL_MAG_SENNTISTEN, //  - mahogany portal frame with Senntisten portal
        ObjectID.POH_PORTAL_MAG_WATERBIRTH, //  - mahogany portal frame with Waterbirth portal
        ObjectID.POH_PORTAL_MAG_KOUREND, //  - mahogany portal frame with Kourend portal
        ObjectID.POH_PORTAL_MAG_FISHINGGUILD //  - mahogany portal frame with Fishing Guild portal
    );
    // Portal Frame Object IDs (using ObjectID constants)
    private static final ImmutableList<Integer> PORTAL_MARBLE_FRAME_IDS = ImmutableList.of(
        ObjectID.POH_PORTAL_MARBLE_EMPTY ,  //  Marble portal frame empty
        ObjectID.POH_PORTAL_MARBLE_VARROCK, //  Marble portal frame with Varrock portal
        ObjectID.POH_PORTAL_MARBLE_FALADOR, //  Marble portal frame with Falador portal
        ObjectID.POH_PORTAL_MARBLE_LUMBRIDGE, // - Marble portal frame with Lumbridge portal
        ObjectID.POH_PORTAL_MARBLE_ARDOUGNE, // - Marble portal frame with Ardougne portal
        ObjectID.POH_PORTAL_MARBLE_CATHERBY, // - Marble portal frame with Catherby portal
        ObjectID.POH_PORTAL_MARBLE_YANILLE, // - Marble portal frame with Yanille portal
        ObjectID.POH_PORTAL_MARBLE_KHARYRLL, // - Marble portal frame with Kharyrll portal
        ObjectID.POH_PORTAL_MARBLE_LUNARISLE, // - Marble portal frame with Lunar Isle portal
        ObjectID.POH_PORTAL_MARBLE_SENNTISTEN, // - Marble portal frame with Senntisten portal
        ObjectID.POH_PORTAL_MARBLE_KOUREND, // - Marble portal frame with Kourend portal
        ObjectID.POH_PORTAL_MARBLE_ANNAKARL, // - Marble portal frame with Annekarl portal
        ObjectID.POH_PORTAL_MARBLE_WATERBIRTH, // - Marble portal frame with Waterbirth portal
        ObjectID.POH_PORTAL_MARBLE_FISHINGGUILD, // - Marble portal frame with Fishing Guild portal
        ObjectID.POH_PORTAL_MARBLE_MARIM // - Marble portal frame with Marim portal
        
    );
    private static final ImmutableList<Integer> PORTAL_FRAME_IDS = ImmutableList.<Integer>builder()
            .addAll(PORTAL_TEAK_FRAME_IDS)
            .addAll(PORTAL_MAG_FRAME_IDS)
            .addAll(PORTAL_MARBLE_FRAME_IDS)
            .build();

    // Portal Chamber Configuration VarBits: Maps location to VarbitID
    // Portal Chamber uses slots: Centrepiece (32), Portal Location 1 (33), Portal Location 2 (34), Portal Location 3 (35)
    private static final Map<String, Integer> PORTAL_CHAMBER_LOCATION_TO_VARBIT;
    static {
        Map<String, Integer> tempMap = new HashMap<>();
        tempMap.put("centrepiece", VarbitID.POH_NEXUS_TELE_32);     // Portal Chamber Centrepiece
        tempMap.put("portal1", VarbitID.POH_NEXUS_TELE_33);         // Portal Location 1
        tempMap.put("portal2", VarbitID.POH_NEXUS_TELE_34);         // Portal Location 2
        tempMap.put("portal3", VarbitID.POH_NEXUS_TELE_35);         // Portal Location 3
        PORTAL_CHAMBER_LOCATION_TO_VARBIT = Collections.unmodifiableMap(tempMap);
    }

    // Portal Chamber Temporary Configuration VarBits (used during construction)
    private static final Map<String, Integer> PORTAL_CHAMBER_LOCATION_TO_TEMP_VARBIT;
    static {
        Map<String, Integer> tempMap = new HashMap<>();
        tempMap.put("centrepiece", VarbitID.POH_NEXUS_TELE_32_TEMP); // Portal Chamber Centrepiece Temp
        tempMap.put("portal1", VarbitID.POH_NEXUS_TELE_33_TEMP);     // Portal Location 1 Temp
        tempMap.put("portal2", VarbitID.POH_NEXUS_TELE_34_TEMP);     // Portal Location 2 Temp
        tempMap.put("portal3", VarbitID.POH_NEXUS_TELE_35_TEMP);     // Portal Location 3 Temp
        PORTAL_CHAMBER_LOCATION_TO_TEMP_VARBIT = Collections.unmodifiableMap(tempMap);
    }

    /**
     * Checks if the player is currently in a portal chamber room.
     * @return true if portal frames are detected nearby
     */
    public static boolean isInPortalChamber() {
        return hasPortalFrames();
    }
    
    /**
     * Checks if any portal frames are present in the current area.
     * @return true if portal frames are found
     */
    public static boolean hasPortalFrames() {
        return !getPortalFrames().isEmpty();
    }
    
    /**
     * Gets all portal frame objects in the current area.
     * @return list of portal frame TileObjects
     */
    public static List<TileObject> getPortalFrames() {
        return PORTAL_FRAME_IDS.stream()
                .map(Rs2GameObject::getTileObject)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
    
    /**
     * Gets all portal frame objects within a specified distance.
     * @param distance maximum distance to search
     * @return list of portal frame TileObjects within distance
     */
    public static List<TileObject> getPortalFrames(int distance) {
        return PORTAL_FRAME_IDS.stream()
                .map(id -> Rs2GameObject.getTileObject(id, distance))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
    
    /**
     * Gets the closest portal frame to the player.
     * @return closest portal frame TileObject or null if none found
     */
    public static TileObject getClosestPortalFrame() {
        List<TileObject> frames = getPortalFrames();
        if (frames.isEmpty()) {
            return null;
        }
        
        WorldPoint playerLocation = Rs2Player.getWorldLocation();
        return frames.stream()
                .min(Comparator.comparingInt(frame -> 
                    frame.getWorldLocation().distanceTo(playerLocation)))
                .orElse(null);
    }
    
    /**
     * Interacts with the closest portal frame.
     * @param action the action to perform (e.g., "Enter", "Configure")
     * @return true if interaction was successful
     */
    public static boolean interactWithClosestPortalFrame(String action) {
        TileObject frame = getClosestPortalFrame();
        if (frame == null) {
            return false;
        }
        
        return Rs2GameObject.interact(frame, action);
    }
    
    /**
     * Interacts with a specific portal frame by index.
     * @param frameIndex the index of the frame (0-based)
     * @param action the action to perform
     * @return true if interaction was successful
     */
    public static boolean interactWithPortalFrame(int frameIndex, String action) {
        List<TileObject> frames = getPortalFrames();
        if (frameIndex < 0 || frameIndex >= frames.size()) {
            return false;
        }
        
        TileObject frame = frames.get(frameIndex);
        return Rs2GameObject.interact(frame, action);
    }
    
    /**
     * Counts the number of portal frames in the current portal chamber.
     * @return number of portal frames found
     */
    public static int getPortalFrameCount() {
        return getPortalFrames().size();
    }
    
    /**
     * Checks if the portal chamber has the maximum number of portal frames.
     * @return true if 3 or more portal frames are present
     */
    public static boolean hasMaxPortalFrames() {
        return getPortalFrameCount() >= 3;
    }
    
    /**
     * Gets information about the current portal chamber setup.
     * @return formatted string with portal chamber details
     */
    public static String getPortalChamberInfo() {
        int frameCount = getPortalFrameCount();
        
        if (frameCount == 0) {
            return "No portal chamber found";
        }
        
        return String.format("Portal Chamber: %d portal frame%s", 
                frameCount, frameCount == 1 ? "" : "s");
    }
    
    /**
     * Waits for portal chamber objects to be loaded.
     * @param timeoutMs maximum time to wait in milliseconds
     * @return true if portal frames are loaded within timeout
     */
    public static boolean waitForPortalChamberLoad(int timeoutMs) {
        long startTime = System.currentTimeMillis();
        
        while (System.currentTimeMillis() - startTime < timeoutMs) {
            if (hasPortalFrames()) {
                return true;
            }
            
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }
        
        return false;
    }
    
    /**
     * Gets a portal frame by its object ID.
     * @param objectId the specific portal frame object ID to find
     * @return the portal frame TileObject or null if not found
     */
    public static TileObject getPortalFrameById(int objectId) {
        return Rs2GameObject.getTileObject(objectId);
    }
    
    /**
     * Checks if a specific portal frame object ID is present.
     * @param objectId the portal frame object ID to check for
     * @return true if the portal frame is found
     */
    public static boolean hasPortalFrameId(int objectId) {
        return getPortalFrameById(objectId) != null;
    }

  
   
    
    // ========== PORTAL MAPPING AND TELEPORT METHODS ==========

    /**
     * Maps portal frames to their corresponding teleport destinations.
     * @return map of PoHTeleport to TileObject (portal frame)
     */
    public static Map<PoHTeleport, TileObject> getPortalTeleportMap() {
        Map<PoHTeleport, TileObject> portalMap = new HashMap<>();
        Map<String, PoHTeleport> config = getPortalChamberConfiguration();
        List<TileObject> frames = getPortalFrames();
        
        // For each configured portal location
        for (Map.Entry<String, PoHTeleport> entry : config.entrySet()) {
            if (!entry.getKey().startsWith("portal")) {
                continue; // Skip non-portal entries like centrepiece
            }
            
            PoHTeleport teleport = entry.getValue();
            if (teleport == null) {
                continue;
            }
            
            // Find matching portal frame by object ID pattern
            int portalNumber = Integer.parseInt(entry.getKey().substring(6));
            if (portalNumber < 1 || portalNumber > frames.size()) {
                continue;
            }
            
            // Use index-1 because portal numbers are 1-based
            TileObject frame = frames.get(portalNumber - 1);
            portalMap.put(teleport, frame);
        }
        
        return portalMap;
    }
    
    /**
     * Teleports to a specific destination using the portal chamber.
     * @param teleport the PoHTeleport destination
     * @return true if teleport was successful
     */
    public static boolean usePortalTeleport(PoHTeleport teleport) {
        if (!Rs2PoH.isInHouse()) {
            Microbot.log("Cannot use portal teleport, not in house");
            return false;
        }
        
        if (teleport == null || !teleport.isPortalTeleport()) {
            Microbot.log("Invalid teleport destination for portal chamber");
            return false;
        }
        
        String location = getLocationForTeleport(teleport);
        if (location == null) {
            Microbot.log("Teleport " + teleport.getDestination() + " not found in any portal");
            return false;
        }
        
        int portalNumber = Integer.parseInt(location.substring(6));
        List<TileObject> frames = getPortalFrames();
        
        if (portalNumber < 1 || portalNumber > frames.size()) {
            Microbot.log("Invalid portal number: " + portalNumber);
            return false;
        }
        
        TileObject frame = frames.get(portalNumber - 1);
        if (frame == null) {
            Microbot.log("Portal frame not found for portal " + portalNumber);
            return false;
        }
        
        Microbot.log("Using portal " + portalNumber + " to teleport to " + teleport.getDestination());
        
        // Interact with the portal frame
        if (Rs2GameObject.interact(frame, "Enter")) {
            // Wait for teleport to complete
            return sleepUntilTrue(() -> 
                Rs2Player.getWorldLocation().distanceTo(teleport.getLocation()) < 10, 
                100, 5000);
        }
        
        Microbot.log("Failed to interact with portal frame");
        return false;
    }
    
    /**
     * Gets the portal chamber configuration (which teleport is in which slot).
     * @return map of portal location to PoHTeleport
     */
    public static Map<String, PoHTeleport> getPortalChamberConfiguration() {
        Map<String, PoHTeleport> config = new HashMap<>();
        
        // Get varbit values for each portal location
        for (Map.Entry<String, Integer> entry : PORTAL_CHAMBER_LOCATION_TO_VARBIT.entrySet()) {
            String location = entry.getKey();
            int varbitId = entry.getValue();
            int varbitValue = Microbot.getVarbitValue(varbitId);
            
            // Map varbit value to PoHTeleport
            PoHTeleport teleport = getTeleportForVarbitValue(varbitValue);
            config.put(location, teleport);
        }
        
        return config;
    }
    
    /**
     * Maps a varbit value to its corresponding PoHTeleport destination.
     * @param varbitValue the varbit value to map
     * @return the corresponding PoHTeleport or null if not found
     */
    private static PoHTeleport getTeleportForVarbitValue(int varbitValue) {
        // Each varbit value maps to a specific teleport destination
        // These values are based on analysis of game mechanics
        switch (varbitValue) {
            case 1: return PoHTeleport.VARROCK;
            case 2: return PoHTeleport.LUMBRIDGE;
            case 3: return PoHTeleport.FALADOR;
            case 4: return PoHTeleport.CAMELOT;
            case 5: return PoHTeleport.ARDOUGNE;
            case 6: return PoHTeleport.WATCHTOWER;
            case 7: return PoHTeleport.KHARYRLL;
            case 8: return PoHTeleport.LUNAR_ISLE;
            case 9: return PoHTeleport.KOUREND_CASTLE;
            case 10: return PoHTeleport.FISHING_GUILD;
            case 11: return PoHTeleport.SENNTISTEN;
            case 12: return PoHTeleport.ICE_PLATEAU;
            case 13: return PoHTeleport.ANNAKARL;
            default: return null; // No teleport configured or unknown varbit value
        }
    }
    
    /**
     * Gets the location (portal1, portal2, portal3) for a specific teleport destination.
     * @param teleport the PoHTeleport to find
     * @return the portal location or null if not found
     */
    public static String getLocationForTeleportOfPlayer(PoHTeleport teleport) {
        if (teleport == null) {
            return null;
        }
        
        Map<String, PoHTeleport> config = getPortalChamberConfiguration();
        
        for (Map.Entry<String, PoHTeleport> entry : config.entrySet()) {
            if (teleport.equals(entry.getValue())) {
                return entry.getKey();
            }
        }
        
        return null; // Teleport not found in any portal
    }
    
    // ========== NAVIGATION CONVENIENCE METHODS ==========
    
    /**
     * Navigates to the Portal Chamber in the POH.
     * 
     * @return true if successfully navigated to the Portal Chamber
     */
    public static boolean navigateToPortalChamber() {
        List<TileObject> frames = getPortalFrames(20);
        if (frames.isEmpty()) {
            Microbot.log("No portal frames found within range");
            return false;
        }
        
        // Find the closest portal frame
        TileObject closestFrame = frames.stream()
                .min(Comparator.comparingInt(frame -> 
                    frame.getWorldLocation().distanceTo(Rs2Player.getWorldLocation())))
                .orElse(null);
        
        if (closestFrame == null) {
            Microbot.log("Failed to find closest portal frame");
            return false;
        }
        
        // Use navigation to reach the portal frame
        if (Rs2PoHNavigation.navigateToObject(closestFrame)) {
            Microbot.log("Successfully navigated to Portal Chamber");
            return true;
        }
        
        Microbot.log("Portal Chamber not reachable via navigation");
        return false;
    }
    
    /**
     * Navigates to a specific portal in the Portal Chamber.
     * 
     * @param portalNumber the portal number (1-3)
     * @return true if successfully navigated to the portal
     */
    public static boolean navigateToPortal(int portalNumber) {
        if (portalNumber < 1 || portalNumber > 3) {
            Microbot.log("Invalid portal number: " + portalNumber);
            return false;
        }
        
        List<TileObject> frames = getPortalFrames();
        if (frames.size() < portalNumber) {
            Microbot.log("Portal " + portalNumber + " not found (only " + frames.size() + " portals available)");
            return false;
        }
        
        TileObject portal = frames.get(portalNumber - 1);
        if (portal == null) {
            Microbot.log("Portal " + portalNumber + " not found");
            return false;
        }
        
        // Use navigation to reach the portal
        if (Rs2PoHNavigation.navigateToObject(portal)) {
            Microbot.log("Successfully navigated to Portal " + portalNumber);
            return true;
        }
        
        Microbot.log("Portal " + portalNumber + " not reachable via navigation");
        return false;
    }
    
    /**
     * Navigates to a portal with a specific teleport destination.
     * 
     * @param teleport the teleport destination to navigate to
     * @return true if successfully navigated to the portal
     */
    public static boolean navigateToPortalWithTeleport(PoHTeleport teleport) {
        if (teleport == null) {
            Microbot.log("Teleport destination is null");
            return false;
        }
        
        String location = getLocationForTeleport(teleport);
        if (location == null) {
            Microbot.log("Teleport " + teleport.getDestination() + " not found in any portal");
            return false;
        }
        
        int portalNumber = Integer.parseInt(location.substring(6));
        return navigateToPortal(portalNumber);
    }
    
    /**
     * Enhanced navigation to Portal Chamber using optimal pathfinding.
     * Uses door state detection and multi-room path calculation.
     * 
     * @return true if successfully navigated to the Portal Chamber
     */
    public static boolean navigateToPortalChamberOptimal() {
        List<TileObject> frames = getPortalFrames(20);
        if (frames.isEmpty()) {
            Microbot.log("No portal frames found within range");
            return false;
        }
        
        // Find the closest portal frame
        TileObject closestFrame = frames.stream()
                .min(Comparator.comparingInt(frame -> 
                    frame.getWorldLocation().distanceTo(Rs2Player.getWorldLocation())))
                .orElse(null);
        
        if (closestFrame == null) {
            Microbot.log("Failed to find closest portal frame");
            return false;
        }
        
        // Use optimal navigation to reach the portal frame
        if (Rs2PoHNavigation.navigateToObjectOptimal(closestFrame)) {
            Microbot.log("Successfully navigated to Portal Chamber using optimal path");
            return true;
        }
        
        Microbot.log("Portal Chamber not reachable via optimal navigation");
        return false;
    }



    // ========== Portal Chamber Varbit Utility Methods ==========

    /**
     * Gets the teleport destination associated with a specific portal object ID.
     * @param objectId the object ID of the portal frame
     * @return the PoHTeleport destination or null if not recognized
     */
    private static PoHTeleport getPoHTeleportFromObjectId(int objectId) {
        // Map object IDs to their respective teleport destinations
        switch (objectId) {
            // Teak portal frames
            case ObjectID.POH_PORTAL_TEAK_VARROCK:
                return PoHTeleport.VARROCK;
            case ObjectID.POH_PORTAL_TEAK_LUMBRIDGE:
                return PoHTeleport.LUMBRIDGE;
            case ObjectID.POH_PORTAL_TEAK_FALADOR:
                return PoHTeleport.FALADOR;
            case ObjectID.POH_PORTAL_TEAK_CATHERBY:
                return PoHTeleport.CATHERBY_TELEPORT;
            case ObjectID.POH_PORTAL_TEAK_ARDOUGNE:
                return PoHTeleport.ARDOUGNE;
            case ObjectID.POH_PORTAL_TEAK_YANILLE:
                return PoHTeleport.WATCHTOWER;
            case ObjectID.POH_PORTAL_TEAK_KHARYRLL:
                return PoHTeleport.KHARYRLL;
            case ObjectID.POH_PORTAL_TEAK_LUNARISLE:
                return PoHTeleport.LUNAR_ISLE;
            case ObjectID.POH_PORTAL_TEAK_SENNTISTEN:
                return PoHTeleport.SENNTISTEN;
            case ObjectID.POH_PORTAL_TEAK_KOUREND:
                return PoHTeleport.KOUREND_CASTLE;
            case ObjectID.POH_PORTAL_TEAK_ANNAKARL:
                return PoHTeleport.ANNAKARL;
            case ObjectID.POH_PORTAL_TEAK_WATERBIRTH:
                return PoHTeleport.BARBARIAN_TELEPORT;
            case ObjectID.POH_PORTAL_TEAK_FISHINGGUILD:
                return PoHTeleport.FISHING_GUILD;
            case ObjectID.POH_PORTAL_TEAK_MARIM:
                return PoHTeleport.MARIM;
                
            // Mahogany portal frames    
            case ObjectID.POH_PORTAL_MAG_VARROCK:
                return PoHTeleport.VARROCK;
            case ObjectID.POH_PORTAL_MAG_LUMBRIDGE:
                return PoHTeleport.LUMBRIDGE;
            case ObjectID.POH_PORTAL_MAG_FALADOR:
                return PoHTeleport.FALADOR;
            case ObjectID.POH_PORTAL_MAG_CATHERBY:
                return PoHTeleport.CATHERBY_TELEPORT;
            case ObjectID.POH_PORTAL_MAG_ARDOUGNE:
                return PoHTeleport.ARDOUGNE;
            case ObjectID.POH_PORTAL_MAG_YANILLE:
                return PoHTeleport.WATCHTOWER;
            case ObjectID.POH_PORTAL_MAG_KHARYRLL:
                return PoHTeleport.KHARYRLL;
            case ObjectID.POH_PORTAL_MAG_LUNARISLE:
                return PoHTeleport.LUNAR_ISLE;
            case ObjectID.POH_PORTAL_MAG_SENNTISTEN:
                return PoHTeleport.SENNTISTEN;
            case ObjectID.POH_PORTAL_MAG_KOUREND:
                return PoHTeleport.KOUREND_CASTLE;
            case ObjectID.POH_PORTAL_MAG_ANNAKARL:
                return PoHTeleport.ANNAKARL;
            case ObjectID.POH_PORTAL_MAG_WATERBIRTH:
                return PoHTeleport.BARBARIAN_TELEPORT;
            case ObjectID.POH_PORTAL_MAG_FISHINGGUILD:
                return PoHTeleport.FISHING_GUILD;
                
            // Marble portal frames
            case ObjectID.POH_PORTAL_MARBLE_VARROCK:
                return PoHTeleport.VARROCK;
            case ObjectID.POH_PORTAL_MARBLE_LUMBRIDGE:
                return PoHTeleport.LUMBRIDGE;
            case ObjectID.POH_PORTAL_MARBLE_FALADOR:
                return PoHTeleport.FALADOR;
            case ObjectID.POH_PORTAL_MARBLE_CATHERBY:
                return PoHTeleport.CATHERBY_TELEPORT;
            case ObjectID.POH_PORTAL_MARBLE_ARDOUGNE:
                return PoHTeleport.ARDOUGNE;
            case ObjectID.POH_PORTAL_MARBLE_YANILLE:
                return PoHTeleport.WATCHTOWER;
            case ObjectID.POH_PORTAL_MARBLE_KHARYRLL:
                return PoHTeleport.KHARYRLL;
            case ObjectID.POH_PORTAL_MARBLE_LUNARISLE:
                return PoHTeleport.LUNAR_ISLE;
            case ObjectID.POH_PORTAL_MARBLE_SENNTISTEN:
                return PoHTeleport.SENNTISTEN;
            case ObjectID.POH_PORTAL_MARBLE_KOUREND:
                return PoHTeleport.KOUREND_CASTLE;
            case ObjectID.POH_PORTAL_MARBLE_ANNAKARL:
                return PoHTeleport.ANNAKARL;
            case ObjectID.POH_PORTAL_MARBLE_WATERBIRTH:
                return PoHTeleport.BARBARIAN_TELEPORT;
            case ObjectID.POH_PORTAL_MARBLE_FISHINGGUILD:
                return PoHTeleport.FISHING_GUILD;
            case ObjectID.POH_PORTAL_MARBLE_MARIM:
                return PoHTeleport.MARIM;
                
            // Empty portal frames have no teleport destination
            case ObjectID.POH_PORTAL_TEAK_EMPTY:
            case ObjectID.POH_PORTAL_MAG_EMPTY:
            case ObjectID.POH_PORTAL_MARBLE_EMPTY:
                return null;
                
            default:
                return null; // Unrecognized portal ID
        }
    }
    
    /**
     * Gets the location (portal1, portal2, portal3) for a specific teleport destination
     * based on examining the actual portal objects in the house.
     * This method does not rely on player varbit values, but examines the objects directly.
     * 
     * @param teleport the PoHTeleport to find
     * @return the portal location or null if not found
     */
    public static String getLocationForTeleport(PoHTeleport teleport) {
        if (teleport == null || !isInPortalChamber()) {
            return null;
        }
        
        List<TileObject> frames = getPortalFrames();
        if (frames.isEmpty()) {
            return null;
        }
        
        // Examine each portal frame and check if it matches the requested teleport
        for (int i = 0; i < frames.size(); i++) {
            TileObject frame = frames.get(i);
            if (frame == null) continue;
            
            // Get the object ID of this portal frame
            int objectId = -1;
            if (frame instanceof GameObject) {
                objectId = ((GameObject) frame).getId();
            }
            
            // Skip if we couldn't get the object ID
            if (objectId == -1) continue;
            
            // Get the teleport destination for this object ID
            PoHTeleport portalTeleport = getPoHTeleportFromObjectId(objectId);
            
            // If this portal matches the requested teleport, return its location
            if (teleport.equals(portalTeleport)) {
                return "portal" + (i + 1); // Portal numbers are 1-based
            }
        }
        
        return null; // Teleport not found in any portal
    }

    /**
     * Gets the varbit value for the Portal Chamber centrepiece.
     * @return the varbit value for the centrepiece (varbit 32)
     */
    public static int getCentrepieceVarbitValue() {
        return Microbot.getVarbitValue(PORTAL_CHAMBER_LOCATION_TO_VARBIT.get("centrepiece"));
    }

    /**
     * Gets the varbit value for Portal Chamber portal 1.
     * @return the varbit value for portal 1 (varbit 33)
     */
    public static int getPortal1VarbitValue() {
        return Microbot.getVarbitValue(PORTAL_CHAMBER_LOCATION_TO_VARBIT.get("portal1"));
    }

    /**
     * Gets the varbit value for Portal Chamber portal 2.
     * @return the varbit value for portal 2 (varbit 34)
     */
    public static int getPortal2VarbitValue() {
        return Microbot.getVarbitValue(PORTAL_CHAMBER_LOCATION_TO_VARBIT.get("portal2"));
    }

    /**
     * Gets the varbit value for Portal Chamber portal 3.
     * @return the varbit value for portal 3 (varbit 35)
     */
    public static int getPortal3VarbitValue() {
        return Microbot.getVarbitValue(PORTAL_CHAMBER_LOCATION_TO_VARBIT.get("portal3"));
    }

    /**
     * Gets the varbit value for a specific Portal Chamber location.
     * @param location the location name ("centrepiece", "portal1", "portal2", "portal3")
     * @return the varbit value for that location, or -1 if location is invalid
     */
    public static int getLocationVarbitValue(String location) {        
        Integer varbitId = PORTAL_CHAMBER_LOCATION_TO_VARBIT.get(location);
        if (varbitId == null) {
            return -1; // Invalid location
        }
        return Microbot.getVarbitValue(varbitId);
    }

    /**
     * Gets the temporary varbit value for a specific Portal Chamber location.
     * Used during construction/configuration of portal chamber.
     * @param location the location name ("centrepiece", "portal1", "portal2", "portal3")
     * @return the temporary varbit value for that location, or -1 if location is invalid
     */
    public static int getLocationTempVarbitValue(String location) {
        Integer varbitId = PORTAL_CHAMBER_LOCATION_TO_TEMP_VARBIT.get(location);
        if (varbitId == null) {
            return -1; // Invalid location
        }
        return Microbot.getVarbitValue(varbitId);
    }

    // ========== Portal Chamber Varbit Configuration Methods ==========

    /**
     * Gets the portal chamber teleport configuration from varbits.
     * @return map of location names to their configured PoHTeleports
     */
    public static Map<String, PoHTeleport> getPortalChamberConfigurationOfPlayer() {
        Map<String, PoHTeleport> config = new HashMap<>();

        for (Map.Entry<String, Integer> entry : PORTAL_CHAMBER_LOCATION_TO_VARBIT.entrySet()) {
             // skip center pice teleport as it is not a location
            if (entry.getKey().equals("centrepiece")) {
                continue;
            }
            String location = entry.getKey();
            int varbitId = entry.getValue();
            int varbitValue = Microbot.getVarbitValue(varbitId);
            
            if (varbitValue != 0) { // Only include configured locations
                PoHTeleport teleport = PoHTeleport.fromNexusVarbitValue(varbitValue);
                if (teleport != null) {
                    config.put(location, teleport);
                }
            }
        }

        return config;
    }

    /**
     * Gets the temporary portal chamber configuration from varbits (used during construction).
     * @return map of location names to their temporary varbit values
     */
    public static Map<String, Integer> getPortalChamberTempConfigurationOfPlayer() {
        Map<String, Integer> config = new HashMap<>();
        
        for (Map.Entry<String, Integer> entry : PORTAL_CHAMBER_LOCATION_TO_TEMP_VARBIT.entrySet()) {
            String location = entry.getKey();
            int varbitId = entry.getValue();
            int varbitValue = Microbot.getVarbitValue(varbitId);
            
            if (varbitValue != 0) { // Only include non-zero values
                config.put(location, varbitValue);
            }
        }
        
        return config;
    }



    /**
     * Gets the teleport configured in a specific portal location.
     * @param portalNumber the portal number (1-3)
     * @return the PoHTeleports configured in that portal location, or null if not configured
     */
    public static PoHTeleport getPortalTeleportOfPlayer(int portalNumber) {
        if (portalNumber < 1 || portalNumber > 3) {
            return null;
        }
        return getTeleportInLocationOfPlayer("portal" + portalNumber);
    }

    /**
     * Gets the teleport configured in a specific location.
     * @param location the location name ("portal1", "portal2", "portal3")
     * @return the PoHTeleports configured in that location, or null if not configured
     */
    public static PoHTeleport getTeleportInLocationOfPlayer(String location) {
        Integer varbitId = PORTAL_CHAMBER_LOCATION_TO_VARBIT.get(location);
        if (varbitId == null) {
            return null;
        }
        
        int varbitValue = Microbot.getVarbitValue(varbitId);
        if (varbitValue == 0) {
            return null; // Location is empty
        }
        
        return PoHTeleport.fromNexusVarbitValue(varbitValue);
    }

    /**
     * Checks if a specific teleport is configured in any portal chamber location.
     * @param teleport the PoHTeleports teleport to check for
     * @return true if the teleport is configured in any location
     */
    public static boolean isTeleportAvailable(PoHTeleport teleport) {
        if (teleport == null) {
            return false; // Null teleport is not valid
        }
        Map<String, PoHTeleport> config = getPortalChamberConfiguration();
        return config.containsValue(teleport);
    }


    /**
     * Gets all configured teleports from the portal chamber.
     * @return list of all PoHTeleports currently configured
     */
    public static List<PoHTeleport> getConfiguredTeleportsForPlayer() {
        Map<String, PoHTeleport> config = getPortalChamberConfiguration();
        return new ArrayList<>(config.values());
    }

    /**
     * Gets the varbit ID for a specific portal chamber location.
     * @param location the location name ("portal1", "portal2", "portal3")
     * @return the varbit ID for that location, or -1 if invalid location
     */
    public static int getVarbitIdForLocation(String location) {
        Integer varbitId = PORTAL_CHAMBER_LOCATION_TO_VARBIT.get(location);
        return varbitId != null ? varbitId : -1;
    }

    /**
     * Checks if the player is currently configuring the portal chamber.
     * @return true if temporary configuration varps have values
     */
    public static boolean isPlayerConfiguringPortalChamber() {
        return !getPortalChamberTempConfigurationOfPlayer().isEmpty();
    }

    /**
     * Gets enhanced information about the current portal chamber setup.
     * @return formatted string with portal chamber details including teleport configuration
     */
    public static String getPortalChamberInfoDetailed() {
        int frameCount = getPortalFrameCount();
        
        if (frameCount == 0) {
            return "No portal chamber found";
        }
        
        Map<String, PoHTeleport> config = getPortalChamberConfiguration();
        int configuredCount = config.size();
        
        StringBuilder info = new StringBuilder();
        info.append(String.format("Portal Chamber: %d portal frame%s", 
                frameCount, frameCount == 1 ? "" : "s"));
        
        if (configuredCount > 0) {
            info.append(String.format(", %d teleport%s configured", 
                    configuredCount, configuredCount == 1 ? "" : "s"));
            
            // Add details about configured teleports
            if (config.containsKey("centrepiece")) {
                info.append(String.format("\n\t[Centrepiece: %s]", config.get("centrepiece").getTooltip()));
            }
            
            for (int i = 1; i <= 3; i++) {
                String portalKey = "portal" + i;
                if (config.containsKey(portalKey)) {
                    info.append(String.format("\n\t[Portal %d: %s]", i, config.get(portalKey).getTooltip()));
                }
            }
        }
        
        return info.toString();
    }
    
    /**
     * Interacts with a portal frame that contains the specified teleport destination.
     * Finds the portal frame that matches the teleport and enters it.
     *
     * @param teleport the PoHTeleport destination to enter
     * @return true if successfully interacted with the portal frame
     */
    public static boolean enterPortalFrameByTeleport(PoHTeleport teleport) {
        if (!Rs2PoH.isInHouse()) {
            Microbot.log("Cannot use portal teleport, not in house");
            return false;
        }
        
        if (teleport == null || !teleport.isPortalTeleport()) {
            Microbot.log("Invalid teleport destination for portal chamber");
            return false;
        }
        
        // First check if we're already in a portal chamber
        if (!isInPortalChamber()) {
            Microbot.log("Not in portal chamber, attempting to navigate there");
            if (!navigateToPortalChamber()) {
                Microbot.log("Failed to navigate to portal chamber");
                return false;
            }
        }
        
        // Get the location of the portal that has this teleport
        String location = getLocationForTeleport(teleport);
        if (location == null) {
            Microbot.log("Teleport " + teleport.getDestination() + " not found in any portal");
            return false;
        }
        
        // Extract the portal number from the location (portal1, portal2, portal3)
        int portalNumber = Integer.parseInt(location.substring(6));
        List<TileObject> frames = getPortalFrames();
        
        if (portalNumber < 1 || portalNumber > frames.size()) {
            Microbot.log("Invalid portal number: " + portalNumber);
            return false;
        }
        
        // Get the portal frame at this index (0-based list, 1-based portal number)
        TileObject frame = frames.get(portalNumber - 1);
        if (frame == null) {
            Microbot.log("Portal frame not found for portal " + portalNumber);
            return false;
        }
        
        Microbot.log("Entering portal " + portalNumber + " to teleport to " + teleport.getDestination());
        
        // Interact with the portal frame
        if (Rs2GameObject.interact(frame, "Enter")) {
            // Wait for teleport to complete
            return sleepUntilTrue(() -> 
                Rs2Player.getWorldLocation().distanceTo(teleport.getLocation()) < 10, 
                100, 5000);
        }
        
        Microbot.log("Failed to interact with portal frame");
        return false;
    }
}
