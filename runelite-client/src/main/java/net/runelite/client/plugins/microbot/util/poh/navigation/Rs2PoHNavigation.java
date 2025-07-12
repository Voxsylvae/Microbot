package net.runelite.client.plugins.microbot.util.poh.navigation;

import net.runelite.api.TileObject;
import net.runelite.api.WallObject;
import net.runelite.api.GameObject;
import net.runelite.api.ObjectComposition;

import net.runelite.api.coords.WorldPoint;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.util.gameobject.Rs2GameObject;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.inventory.Rs2ItemModel;
import net.runelite.client.plugins.microbot.util.magic.Rs2Magic;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;
import net.runelite.client.plugins.microbot.util.poh.Rs2PoH;
import net.runelite.client.plugins.microbot.util.poh.Rs2PoH.HouseAdvertisement;
import net.runelite.client.plugins.microbot.util.poh.data.Rs2PoHPortal;
import net.runelite.client.plugins.microbot.util.tabs.Rs2Tab;
import net.runelite.client.plugins.microbot.util.walker.Rs2Walker;
import net.runelite.client.plugins.microbot.util.walker.WalkerState;
import net.runelite.client.plugins.skillcalculator.skills.MagicAction;

import static net.runelite.api.Constants.GAME_TICK_LENGTH;
import static net.runelite.client.plugins.microbot.util.Global.sleep;
import static net.runelite.client.plugins.microbot.util.Global.sleepUntil;
import static net.runelite.client.plugins.microbot.util.Global.sleepUntilTrue;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import lombok.extern.slf4j.Slf4j;

/**
 * Advanced POH navigation system with multi-house caching and optimal pathfinding.
 * Handles navigation between rooms, door state detection, and maintains separate caches
 * for own house and advertised houses based on player names.
 */
@Slf4j
public class Rs2PoHNavigation {
    
    // POH house layout constants
    private static final int ROOM_SIZE = 8; // Each room is 8x8 tiles
    private static final int[] DOOR_OBJECT_IDS = {
        8122, 8123, 8124, // POH dungeon doors (oak, steel, marble)
        8367, 8368, 8369  // POH trapdoors (oak, teak, mahogany)
    };
    
    // Cache timeout settings
    private static final long CACHE_TIMEOUT_MS = 5 * 60 * 1000; // 5 minutes
    private static final long STALE_DATA_THRESHOLD_MS = 30 * 1000; // 30 seconds
    
    // Multiple house caches
    private static final Map<String, Map<WorldPoint, RoomData>> houseCaches = new ConcurrentHashMap<>();
    private static final String OWN_HOUSE_KEY = "OWN_HOUSE";
    
    // Current house context
    private static String currentHouseOwner = null;
    private static boolean isInAdvertisedHouse = false;
    
    // ========== CORE NAVIGATION METHODS ==========
    
    /**
     * Navigates to a target object in the POH using basic pathfinding.
     * 
     * @param targetObjectId the object ID to navigate to
     * @return true if successfully navigated to the object
     */
    public static boolean navigateToObject(int targetObjectId) {
        if (!Rs2PoH.checkIsInHouse()) {
            return false;
        }
        
        TileObject targetObject = Rs2GameObject.getTileObject(targetObjectId);
        if (targetObject == null) {
            Microbot.log("Target object not found in house: " + targetObjectId);
            return false;
        }
        
        return navigateToObject(targetObject);
    }
    
    /**
     * Navigates to a target object using basic pathfinding.
     * 
     * @param targetObject the object to navigate to
     * @return true if successfully navigated to the object
     */
    public static boolean navigateToObject(TileObject targetObject) {
        if (!Rs2PoH.checkIsInHouse()) {
            return false;
        }
        
        WorldPoint targetLocation = targetObject.getWorldLocation();
        
        // Use basic RuneLite walking for simple navigation
        return Rs2Walker.walkTo(targetLocation);
    }
    
    /**
     * Navigates to a target object and interacts with it.
     * This is a convenience method that combines navigation and interaction.
     * 
     * @param targetObjectId the object ID to navigate to and interact with
     * @param action the action to perform on the object (e.g., "Use", "Teleport")
     * @return true if successfully navigated to and interacted with the object
     */
    public static boolean navigateToAndInteract(int targetObjectId, String action) {
        if (!navigateToObject(targetObjectId)) {
            return false;
        }
        
        // Try to interact with the object
        return Rs2GameObject.interact(targetObjectId, action);
    }
    
    /**
     * Checks if a target object can be reached in the current POH.
     * This method performs reachability analysis without actually navigating.
     * 
     * @param targetObjectId the object ID to check reachability for
     * @return true if the object can be reached
     */
    public static boolean canReachObject(int targetObjectId) {
        if (!Rs2PoH.checkIsInHouse()) {
            return false;
        }
        
        TileObject targetObject = Rs2GameObject.getTileObject(targetObjectId);
        if (targetObject == null) {
            return false;
        }
        
        return Rs2GameObject.canReach(targetObject.getWorldLocation());
    }
    
    // ========== ENHANCED OPTIMAL NAVIGATION ==========
    
    /**
     * Enhanced navigation that maps door states and calculates optimal paths.
     * This method detects doors blocking paths through multiple rooms and finds alternative routes.
     * 
     * @param targetObjectId the object ID to navigate to
     * @return true if successfully navigated using optimal path
     */
    public static boolean navigateToObjectOptimal(int targetObjectId) {
        if (!Rs2PoH.isInHouse()) {
            return false;
        }
        
        TileObject targetObject = Rs2GameObject.getTileObject(targetObjectId);
        if (targetObject == null) {
            log.error("Target object not found in house: " + targetObjectId);
            return false;
        }
        
        return navigateToObjectOptimal(targetObject);
    }
    
    /**
     * Enhanced navigation with optimal path calculation and door state mapping.
     * 
     * @param targetObject the TileObject to navigate to
     * @return true if successfully navigated using optimal path
     */
    public static boolean navigateToObjectOptimal(TileObject targetObject) {
        if (!Rs2PoH.isInHouse()) {
            return false;
        }
        
        WorldPoint targetLocation = targetObject.getWorldLocation();
        WorldPoint playerLocation = Rs2Player.getWorldLocation();
        WorldPoint exitPortalLocation = getExitPortalLocation();
        
        if (exitPortalLocation == null) {
            log.error("Cannot find exit portal for reference point");
            return false;
        }
        
        // Initialize house context
        initializeHouseContext();
        
        // Calculate room positions
        WorldPoint currentRoom = getRoomCenter(playerLocation, exitPortalLocation);
        WorldPoint targetRoom = getRoomCenter(targetLocation, exitPortalLocation);
        
        // Check if direct path is available
        if (Rs2GameObject.canReach(targetLocation)) {
            Microbot.log("Direct path available to target object");
            return moveToLocation(targetLocation);
        }
        
        // Calculate optimal path through multiple rooms
        List<WorldPoint> optimalPath = calculateOptimalPath(currentRoom, targetRoom, exitPortalLocation);
        
        if (optimalPath.isEmpty()) {
            Microbot.log("No path found to target object");
            return false;
        }
        
        return executeMultiRoomPath(optimalPath, targetLocation);
    }
    
    public static boolean isPlayerInRoom(WorldPoint roomCenter) {
        if (!Rs2PoH.isInHouse()) {
            return false;
        }
        
        WorldPoint playerLocation = Rs2Player.getWorldLocation();
        if (playerLocation == null) {
            Microbot.log("Player location is null, cannot determine room presence");
            return false;
        }
        return playerLocation.distanceTo(roomCenter) <= ROOM_SIZE / 2; // Player is within half room size
    }

    /**
     * Diagnostics method to analyze current room and nearby door states.
     * Useful for debugging navigation issues.
     * 
     * @return formatted string with current room analysis
     */
    public static String analyzeCurrentRoom() {
        if (!Rs2PoH.checkIsInHouse()) {
            return "Not in house";
        }
        
        WorldPoint playerLocation = Rs2Player.getWorldLocation();
        WorldPoint exitPortalLocation = getExitPortalLocation();
        
        if (exitPortalLocation == null) {
            return "Cannot find exit portal for reference";
        }
        
        WorldPoint currentRoom = getRoomCenter(playerLocation, exitPortalLocation);
        return getRoomDebugInfo(currentRoom);
    }
    
    
     /**
     * Handles teleporting to and entering the player's own house
     * @return true if successfully entered own house
     */
    public static boolean navigateToOwnHouse() {
        Microbot.log("Attempting to enter own house for POH teleport");
        
        // Check if already in house
        if (Rs2PoH.isInHouse()) {
            Microbot.log("Already in house, proceeding with POH teleport");
            return true;
        }
        
        // Check if we have house teleport available
        if (!hasAvailableHouseTeleport()) {
            Microbot.log("No house teleport available (spell or tablet)");
            return false;
        }
        
        // Get player's house portal location
        WorldPoint housePortalLocation = Rs2PoHPortal.getPlayerHousePortalLocation();
        if (housePortalLocation == null) {
            Microbot.log("Cannot determine player's house portal location");
            return false;
        }
        
        // Use walker to get to house portal (inside house directly)
        WalkerState walkerState = Rs2Walker.walkWithState(housePortalLocation, 0);
        if (walkerState != WalkerState.ARRIVED) {
            Microbot.log("Failed to travel to house portal, state: " + walkerState);
            return false;
        }
        Rs2PoH.enterHouse();
        // Wait for teleport completion and house loading
        if (!waitForHouseEntry()) {
            Microbot.log("Failed to enter house after teleport");
            return false;
        }
        
        Microbot.log("Successfully entered own house");
        return true;
    }

    /**
     * Handles teleporting to house and entering advertised house on World 330
     * @return true if successfully entered advertised house
     */
    public static boolean navigateToAdvertisementHouse( ) {
        Microbot.log("Attempting to enter advertised house for POH teleport");
        
        // Step 1: Hop to World 330 if not already there
        if (!ensureWorld330()) {
            log.error("\n\tFailed to hop to World 330 for house advertisements");
            return false;
        }
        
        // Step 2: Use walker to get to Rimmington portal (outside)
        WorldPoint rimmingtonPortal = Rs2PoHPortal.RIMMINGTON.getWorldPoint();
        WalkerState walkerState = Rs2Walker.walkWithState(rimmingtonPortal, 0);
        if (walkerState != WalkerState.ARRIVED) {
            log.error("Failed to travel to Rimmington portal, state: " + walkerState);
            return false;
        }
        
        // Step 3: Interact with house advertisement board
        if (!Rs2PoH.interactWithHouseBoard()) {
            Microbot.log("Failed to interact with house advertisement board");
            return false;
        }
        
        // Step 4: Wait for board to open
        if (!Rs2PoH.waitForPohBoardOpen(10000)) {
            Microbot.log("House advertisement board did not open");
            return false;
        }
        HouseAdvertisement bestHouse = Rs2PoH.getBestAllTeleportHouse();
        // String houseHolderName = bestHouse != null ? bestHouse.getPlayerName() : null;
        // Step 5: Enter the best available house
        if (!Rs2PoH.enterHouse(bestHouse)) {
            Microbot.log("No suitable advertised houses available");
            return false;
        }
        
        // Step 6: Wait for house entry
        if (!waitForHouseEntry()) {
            Microbot.log("Failed to enter advertised house");
            return false;
        }
        
        Microbot.log("Successfully entered advertised house");
        return true;
    }

    /**
     * Performs the house teleport using the provided transport
     * @param houseTeleport the house teleport transport
     * @param toOutside whether to teleport outside (for advertisements) or inside
     * @return true if teleport was successful
     */
    private static boolean performHouseTeleport( boolean toOutside) {
        try {                        
            Rs2ItemModel tablet = Rs2Inventory.get("House teleport");
            Microbot.log("Performing house teleport with type: " + (tablet == null ? "Spell" : "Tablet"));
            if (tablet == null && Rs2Magic.canCast(MagicAction.TELEPORT_TO_HOUSE)) {
                return performHouseTeleportSpell(toOutside);
            } else if (tablet != null) {
                return performHouseTeleportTablet(toOutside);
            } else {                
                Microbot.log("No valid house teleport method found");
                return false;
            }
        } catch (Exception e) {
            Microbot.log("Error performing house teleport: " + e.getMessage());
            return false;
        }
    }

    /**
     * Performs house teleport using the House Teleport spell
     * @param toOutside whether to teleport outside
     * @return true if teleport was successful
     */
    private static boolean performHouseTeleportSpell(boolean toOutside) {
        // Check if we have the required magic level and runes
        if (!Rs2Magic.canCast(MagicAction.TELEPORT_TO_HOUSE)) {
            Microbot.log("Cannot cast House Teleport spell - insufficient level or runes");
            return false;
        }
        
        // Open magic tab
        if (!Rs2Tab.switchToMagicTab()) {
            Microbot.log("Failed to open magic tab");
            return false;
        }
        
        sleep(GAME_TICK_LENGTH);
        
        // Cast the spell with appropriate action
        // String action = toOutside ? "Outside" : "Inside";
        if (!Rs2Magic.cast(MagicAction.TELEPORT_TO_HOUSE)) {
            Microbot.log("Failed to cast House Teleport spell");
            return false;
        }
        
        // Wait for teleport animation
        return sleepUntilTrue(() -> Rs2Player.isAnimating(), 100, 3000) &&
               sleepUntilTrue(() -> !Rs2Player.isAnimating(), 100, 10000);
    }

    /**
     * Performs house teleport using House Teleport tablet
     * @param toOutside whether to teleport outside
     * @return true if teleport was successful
     */
    private static boolean performHouseTeleportTablet(boolean toOutside) {
        // Find house teleport tablet in inventory
        Rs2ItemModel tablet = Rs2Inventory.get("House teleport");
        if (tablet == null) {
            Microbot.log("No House Teleport tablet found in inventory");
            return false;
        }
        
        // Use the tablet with appropriate action
        String action = toOutside ? "Outside" : "Break";
        if (!Rs2Inventory.interact(tablet, action)) {
            Microbot.log("Failed to use House Teleport tablet");
            return false;
        }
        
        // Wait for teleport animation
        return sleepUntilTrue(() -> Rs2Player.isAnimating(), 100, 3000) &&
               sleepUntilTrue(() -> !Rs2Player.isAnimating(), 100, 10000);
    }

    /**
     * Ensures the player is on World 330 for house advertisements
     * @return true if successfully on World 330
     */
    private static boolean ensureWorld330() {
        final int ADVERTISEMENT_WORLD = 330;
        
        if (Microbot.getClient().getWorld() == ADVERTISEMENT_WORLD) {
            Microbot.log("Already on World 330");
            return true;
        }
        
        Microbot.log("Hopping to World 330 for house advertisements");
        
        // Use RuneLite's world hopping functionality
        try {
            Microbot.hopToWorld(ADVERTISEMENT_WORLD);
            sleep(5000);
            sleepUntil(()->(Rs2Player.getWorld() == ADVERTISEMENT_WORLD),15000);
            Microbot.hopToWorld(ADVERTISEMENT_WORLD);
            // Wait for world hop to complete
            return sleepUntilTrue(() -> Microbot.getClientThread().runOnClientThreadOptional(() ->Microbot.getClient().getWorld() == ADVERTISEMENT_WORLD).orElse(false), 
                                100, 15000);
        } catch (Exception e) {
            log.error("[Exception] Failed to hop to World 330: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Waits for successful entry into a house instance
     * @return true if successfully entered house
     */
    private static boolean waitForHouseEntry() {
        Microbot.log("Waiting for house entry...");
        
        // Wait for house loading with multiple checks
        boolean enteredHouse = sleepUntilTrue(() -> {
            return Rs2PoH.isInHouse() && 
                   Rs2Player.getWorldLocation() != null &&
                   !Rs2Player.isMoving();
        }, 600, 15000);
        
        if (enteredHouse) {
            Microbot.log("Successfully entered house");
            // Additional wait for house objects to load
            sleep(1200);
            return true;
        } else {
            Microbot.log("Failed to enter house within timeout");
            return false;
        }
    }

    
    
    
    
    
    
    
    
    
    
    
    
    
    
    /**
     * Initializes the house context to determine which cache to use.
     */
    private static void initializeHouseContext() {
        // In a real implementation, this would detect if we're in our own house
        // or an advertised house and set the appropriate context
        // For now, we'll use a simple heuristic
        
        if (currentHouseOwner == null) {
            // Try to detect house owner from game state
            // This would need to be implemented based on how OSRS exposes this information
            currentHouseOwner = detectHouseOwner();
            isInAdvertisedHouse = !OWN_HOUSE_KEY.equals(currentHouseOwner);
        }
    }
    

























    /**
     * Detects the current house owner based on various game indicators.
     * 
     * @return house owner identifier or OWN_HOUSE_KEY for own house
     */
    private static String detectHouseOwner() {
        // TODO: Implement actual detection logic
        // This could be based on:
        // - Widget text showing house owner name
        // - World number (330 for advertisements)
        // - Game chat messages
        // - Other game state indicators
        
        // For now, default to own house
        return OWN_HOUSE_KEY;
    }
    
    /**
     * Gets the appropriate room cache for the current house.
     * 
     * @return the room data cache for the current house
     */
    private static Map<WorldPoint, RoomData> getCurrentHouseCache() {
        String cacheKey = currentHouseOwner != null ? currentHouseOwner : OWN_HOUSE_KEY;
        return houseCaches.computeIfAbsent(cacheKey, k -> new ConcurrentHashMap<>());
    }
    
    /**
     * Calculates the optimal path through multiple rooms considering door states.
     * 
     * @param startRoom the starting room center
     * @param targetRoom the target room center
     * @param exitPortalLocation reference point for room calculations
     * @return list of room centers representing the optimal path
     */
    private static List<WorldPoint> calculateOptimalPath(WorldPoint startRoom, WorldPoint targetRoom, WorldPoint exitPortalLocation) {
        List<WorldPoint> path = new ArrayList<>();
        
        // If same room, no path needed
        if (startRoom.equals(targetRoom)) {
            return path;
        }
        
        // Scan and map door states along potential paths
        mapRoomStatesInPath(startRoom, targetRoom, exitPortalLocation);
        
        // Try direct path first (straight line through rooms)
        List<WorldPoint> directPath = calculateDirectPath(startRoom, targetRoom);
        if (isPathClear(directPath)) {
            Microbot.log("Direct path through rooms is clear");
            return directPath;
        }
        
        // Direct path blocked, calculate alternative path
        Microbot.log("Direct path blocked, calculating alternative route");
        return calculateAlternativePath(startRoom, targetRoom, exitPortalLocation);
    }
    
    /**
     * Calculates a direct path through rooms in a straight line.
     * 
     * @param startRoom the starting room center
     * @param targetRoom the target room center
     * @return list of rooms in direct path
     */
    private static List<WorldPoint> calculateDirectPath(WorldPoint startRoom, WorldPoint targetRoom) {
        List<WorldPoint> path = new ArrayList<>();
        
        int deltaX = targetRoom.getX() - startRoom.getX();
        int deltaY = targetRoom.getY() - startRoom.getY();
        
        // Calculate steps for each axis
        int stepsX = Math.abs(deltaX) / ROOM_SIZE;
        int stepsY = Math.abs(deltaY) / ROOM_SIZE;
        
        int stepDirectionX = deltaX > 0 ? ROOM_SIZE : (deltaX < 0 ? -ROOM_SIZE : 0);
        int stepDirectionY = deltaY > 0 ? ROOM_SIZE : (deltaY < 0 ? -ROOM_SIZE : 0);
        
        WorldPoint currentRoom = startRoom;
        
        // Move horizontally first
        for (int i = 0; i < stepsX; i++) {
            currentRoom = new WorldPoint(
                currentRoom.getX() + stepDirectionX,
                currentRoom.getY(),
                currentRoom.getPlane()
            );
            path.add(currentRoom);
        }
        
        // Then move vertically
        for (int i = 0; i < stepsY; i++) {
            currentRoom = new WorldPoint(
                currentRoom.getX(),
                currentRoom.getY() + stepDirectionY,
                currentRoom.getPlane()
            );
            path.add(currentRoom);
        }
        
        return path;
    }
    
    /**
     * Maps door states for all rooms in the potential path area.
     * 
     * @param startRoom the starting room center
     * @param targetRoom the target room center
     * @param exitPortalLocation reference point for calculations
     */
    private static void mapRoomStatesInPath(WorldPoint startRoom, WorldPoint targetRoom, WorldPoint exitPortalLocation) {
        // Calculate bounding box for path area
        int minX = Math.min(startRoom.getX(), targetRoom.getX()) - ROOM_SIZE;
        int maxX = Math.max(startRoom.getX(), targetRoom.getX()) + ROOM_SIZE;
        int minY = Math.min(startRoom.getY(), targetRoom.getY()) - ROOM_SIZE;
        int maxY = Math.max(startRoom.getY(), targetRoom.getY()) + ROOM_SIZE;
        
        // Scan all rooms in the area
        for (int x = minX; x <= maxX; x += ROOM_SIZE) {
            for (int y = minY; y <= maxY; y += ROOM_SIZE) {
                WorldPoint roomCenter = new WorldPoint(x, y, startRoom.getPlane());
                scanRoomDoorStates(roomCenter);
            }
        }
    }
    
    /**
     * Scans and caches door states for all directions from a room center.
     * Uses intelligent caching to avoid redundant scans.
     * 
     * @param roomCenter the center of the room to scan
     */
    private static void scanRoomDoorStates(WorldPoint roomCenter) {
        Map<WorldPoint, RoomData> cache = getCurrentHouseCache();
        
        // Check if already cached and not stale
        RoomData existingData = cache.get(roomCenter);
        if (existingData != null && !existingData.isStale(STALE_DATA_THRESHOLD_MS)) {
            return; // Use existing cache data
        }
        
        Microbot.log("Scanning door states for room at " + roomCenter + 
                    (existingData != null ? " (refreshing stale data)" : " (new room)"));
        
        RoomData roomData = new RoomData(roomCenter);
        int doorsDetected = 0;
        int openDoors = 0;
        int closedDoors = 0;
        
        // Check each direction for doors
        for (Direction direction : Direction.values()) {
            WorldPoint doorLocation = new WorldPoint(
                roomCenter.getX() + (direction.getDeltaX() * ROOM_SIZE / 2),
                roomCenter.getY() + (direction.getDeltaY() * ROOM_SIZE / 2),
                roomCenter.getPlane()
            );
            
            DoorState doorState = detectDoorState(doorLocation);
            roomData.setDoorState(direction, doorState);
            
            // Enhanced logging and statistics
            if (doorState != DoorState.NOT_PRESENT && doorState != DoorState.UNKNOWN) {
                doorsDetected++;
                if (doorState == DoorState.OPEN) {
                    openDoors++;
                    Microbot.log("Open door found at " + direction + " of room " + roomCenter);
                } else if (doorState == DoorState.CLOSED) {
                    closedDoors++;
                    Microbot.log("Closed door detected at " + direction + " of room " + roomCenter);
                }
            }
        }
        
        // Cache the results
        cache.put(roomCenter, roomData);
        
        // Summary logging
        if (doorsDetected > 0) {
            Microbot.log("Room scan complete for " + roomCenter + ": " + doorsDetected + 
                        " doors (" + openDoors + " open, " + closedDoors + " closed)");
        } else {
            Microbot.log("Room scan complete for " + roomCenter + ": no doors detected");
        }
    }
    
    /**
     * Detects the state of a door at a specific location.
     * 
     * @param doorLocation the location to check for doors
     * @return the state of the door at that location
     */
    private static DoorState detectDoorState(WorldPoint doorLocation) {
        try {
            // First check for wall objects (doors) at the location
            WallObject wallDoor = findNearestWallDoor(doorLocation);
            if (wallDoor != null) {
                return determineDoorStateFromWallObject(wallDoor);
            }
            
            // Check for legacy hardcoded door object IDs as fallback
            for (int doorId : DOOR_OBJECT_IDS) {
                TileObject door = Rs2GameObject.getTileObject(doorId);
                if (door != null && door.getWorldLocation().distanceTo(doorLocation) <= 2) {
                    // For hardcoded doors, check if we can get ObjectComposition for better detection
                    ObjectComposition composition = Rs2GameObject.convertToObjectComposition(door);
                    if (composition != null) {
                        return determineDoorStateFromComposition(composition);
                    }
                    // Fallback: assume hardcoded door is closed
                    return DoorState.CLOSED;
                }
            }
            
            // Check if the path is blocked by other means
            if (!Rs2GameObject.canReach(doorLocation)) {
                return DoorState.CLOSED; // Blocked by something
            }
            
            return DoorState.OPEN; // No obstacles detected
        } catch (Exception e) {
            Microbot.log("Error detecting door state at " + doorLocation + ": " + e.getMessage());
            return DoorState.UNKNOWN;
        }
    }
    
    /**
     * Finds wall objects that might be doors near a location.
     * 
     * @param searchLocation the location to search around
     * @return the nearest wall door or null if not found
     */
    private static WallObject findNearestWallDoor(WorldPoint searchLocation) {
        try {
            // Search for wall objects in a small radius around the search location
            List<WallObject> wallObjects = Rs2GameObject.getWallObjects(wallObject -> {
                // Filter to objects within 2 tiles of search location
                return wallObject.getWorldLocation().distanceTo(searchLocation) <= 2;
            }, searchLocation, 2);
            
            WallObject nearestDoor = null;
            double nearestDistance = Double.MAX_VALUE;
            
            for (WallObject wallObject : wallObjects) {
                // Convert to ObjectComposition to check if it's a door
                ObjectComposition composition = Rs2GameObject.convertToObjectComposition(wallObject);
                if (composition != null && isDoorComposition(composition)) {
                    double distance = wallObject.getWorldLocation().distanceTo(searchLocation);
                    if (distance < nearestDistance) {
                        nearestDistance = distance;
                        nearestDoor = wallObject;
                    }
                }
            }
            
            return nearestDoor;
        } catch (Exception e) {
            Microbot.log("Error finding wall doors near " + searchLocation + ": " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Determines if an ObjectComposition represents a door by checking its actions.
     * 
     * @param composition the ObjectComposition to check
     * @return true if the composition represents a door
     */
    private static boolean isDoorComposition(ObjectComposition composition) {
        if (composition == null) {
            return false;
        }
        
        String[] actions = composition.getActions();
        if (actions == null) {
            return false;
        }
        
        // Check for common door actions
        for (String action : actions) {
            if (action != null) {
                String lowerAction = action.toLowerCase();
                if (lowerAction.contains("open") || lowerAction.contains("close") || 
                    lowerAction.contains("pass") || lowerAction.contains("enter")) {
                    return true;
                }
            }
        }
        
        // Check for common door names
        String name = composition.getName();
        if (name != null) {
            String lowerName = name.toLowerCase();
            if (lowerName.contains("door") || lowerName.contains("gate") || 
                lowerName.contains("barrier") || lowerName.contains("portcullis")) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Determines the door state from a WallObject.
     * 
     * @param wallObject the wall object to analyze
     * @return the determined door state
     */
    private static DoorState determineDoorStateFromWallObject(WallObject wallObject) {
        try {
            ObjectComposition composition = Rs2GameObject.convertToObjectComposition(wallObject);
            if (composition != null) {
                return determineDoorStateFromComposition(composition);
            }
            
            // Fallback: if we can't get composition, assume closed
            return DoorState.CLOSED;
        } catch (Exception e) {
            Microbot.log("Error determining door state from wall object: " + e.getMessage());
            return DoorState.UNKNOWN;
        }
    }
    
    /**
     * Determines the door state from an ObjectComposition by analyzing its actions.
     * 
     * @param composition the ObjectComposition to analyze
     * @return the determined door state
     */
    private static DoorState determineDoorStateFromComposition(ObjectComposition composition) {
        if (composition == null) {
            return DoorState.UNKNOWN;
        }
        
        String[] actions = composition.getActions();
        if (actions == null) {
            return DoorState.UNKNOWN;
        }
        
        // Check for actions that indicate door state
        for (String action : actions) {
            if (action != null) {
                String lowerAction = action.toLowerCase();
                if (lowerAction.contains("open")) {
                    return DoorState.CLOSED; // Has "Open" action, so currently closed
                } else if (lowerAction.contains("close")) {
                    return DoorState.OPEN; // Has "Close" action, so currently open
                } else if (lowerAction.contains("pass") || lowerAction.contains("enter")) {
                    return DoorState.OPEN; // Can pass through, so open
                }
            }
        }
        
        // If no clear action indicates state, assume closed for safety
        return DoorState.CLOSED;
    }
    
    /**
     * Checks if a calculated path through rooms is clear of blocked doors.
     * 
     * @param roomPath the list of rooms in the path
     * @return true if the path is clear
     */
    private static boolean isPathClear(List<WorldPoint> roomPath) {
        if (roomPath.isEmpty()) {
            return true;
        }
        
        WorldPoint currentRoom = Rs2Player.getWorldLocation();
        WorldPoint exitPortalLocation = getExitPortalLocation();
        
        if (exitPortalLocation == null) {
            return false;
        }
        
        currentRoom = getRoomCenter(currentRoom, exitPortalLocation);
        
        // Check each transition between rooms
        for (WorldPoint nextRoom : roomPath) {
            if (!canMoveToRoom(currentRoom, nextRoom)) {
                return false;
            }
            currentRoom = nextRoom;
        }
        
        return true;
    }
    
    /**
     * Checks if movement from one room to another is possible by analyzing door states.
     * 
     * @param fromRoom the source room center
     * @param toRoom the destination room center
     * @return true if movement is possible
     */
    private static boolean canMoveToRoom(WorldPoint fromRoom, WorldPoint toRoom) {
        int deltaX = toRoom.getX() - fromRoom.getX();
        int deltaY = toRoom.getY() - fromRoom.getY();
        
        Direction direction = Direction.fromDeltas(deltaX, deltaY);
        if (direction == null) {
            // Not adjacent rooms or invalid direction
            return false;
        }
        
        Map<WorldPoint, RoomData> cache = getCurrentHouseCache();
        RoomData roomData = cache.get(fromRoom);
        
        // If no room data exists, scan the room to get current door states
        if (roomData == null) {
            scanRoomDoorStates(fromRoom);
            roomData = cache.get(fromRoom);
        }
        
        // If still no room data, assume passable but log warning
        if (roomData == null) {
            Microbot.log("Warning: No room data for " + fromRoom + ", assuming passable to " + toRoom);
            return true;
        }
        
        DoorState doorState = roomData.getDoorState(direction);
        boolean canMove = doorState == DoorState.OPEN || doorState == DoorState.UNKNOWN;
        
        // Enhanced logging for debugging pathfinding
        if (!canMove) {
            Microbot.log("Movement blocked from " + fromRoom + " to " + toRoom + 
                        " via " + direction + " - door state: " + doorState);
        }
        
        return canMove;
    }
    
    /**
     * Calculates an alternative path when the direct path is blocked using breadth-first search.
     * This method considers actual door states and room connectivity for sophisticated pathfinding.
     * 
     * @param startRoom the starting room center
     * @param targetRoom the target room center
     * @param exitPortalLocation reference point for calculations
     * @return list of rooms representing alternative path
     */
    private static List<WorldPoint> calculateAlternativePath(WorldPoint startRoom, WorldPoint targetRoom, WorldPoint exitPortalLocation) {
        Microbot.log("Calculating alternative path from " + startRoom + " to " + targetRoom);
        
        // Use breadth-first search for optimal pathfinding
        Queue<PathNode> queue = new LinkedList<>();
        Set<WorldPoint> visited = new HashSet<>();
        Map<WorldPoint, WorldPoint> parentMap = new HashMap<>();
        
        // Start BFS from the starting room
        queue.offer(new PathNode(startRoom, 0));
        visited.add(startRoom);
        parentMap.put(startRoom, null);
        
        final int MAX_SEARCH_DEPTH = 20; // Prevent infinite loops in large houses
        final int MAX_DISTANCE_FROM_PORTAL = ROOM_SIZE * 10; // Reasonable house size limit
        
        while (!queue.isEmpty()) {
            PathNode current = queue.poll();
            WorldPoint currentRoom = current.room;
            
            // Check if we've reached the target
            if (currentRoom.equals(targetRoom)) {
                return reconstructPath(parentMap, startRoom, targetRoom);
            }
            
            // Prevent excessive search depth
            if (current.depth >= MAX_SEARCH_DEPTH) {
                continue;
            }
            
            // Explore all adjacent rooms in 4 directions
            for (Direction direction : Direction.values()) {
                WorldPoint adjacentRoom = new WorldPoint(
                    currentRoom.getX() + (direction.getDeltaX() * ROOM_SIZE),
                    currentRoom.getY() + (direction.getDeltaY() * ROOM_SIZE),
                    currentRoom.getPlane()
                );
                
                // Skip if already visited
                if (visited.contains(adjacentRoom)) {
                    continue;
                }
                
                // Skip if too far from portal (likely outside house bounds)
                if (exitPortalLocation != null && 
                    adjacentRoom.distanceTo(exitPortalLocation) > MAX_DISTANCE_FROM_PORTAL) {
                    continue;
                }
                
                // Check if movement to this room is possible
                if (canMoveToRoom(currentRoom, adjacentRoom)) {
                    visited.add(adjacentRoom);
                    parentMap.put(adjacentRoom, currentRoom);
                    queue.offer(new PathNode(adjacentRoom, current.depth + 1));
                    
                    Microbot.log("Found accessible room: " + adjacentRoom + " from " + currentRoom + 
                                " via " + direction + " (depth: " + (current.depth + 1) + ")");
                }
            }
        }
        
        // No path found
        Microbot.log("No alternative path found from " + startRoom + " to " + targetRoom);
        return new ArrayList<>();
    }
    
    /**
     * Reconstructs the path from BFS parent map.
     * 
     * @param parentMap the parent mapping from BFS
     * @param startRoom the starting room
     * @param targetRoom the target room
     * @return list of rooms in the path (excluding start room)
     */
    private static List<WorldPoint> reconstructPath(Map<WorldPoint, WorldPoint> parentMap, 
                                                  WorldPoint startRoom, WorldPoint targetRoom) {
        List<WorldPoint> path = new ArrayList<>();
        WorldPoint current = targetRoom;
        
        // Trace back from target to start
        while (current != null && !current.equals(startRoom)) {
            path.add(0, current); // Add to beginning of list
            current = parentMap.get(current);
        }
        
        Microbot.log("Alternative path found with " + path.size() + " room transitions: " + path);
        return path;
    }
    
    /**
     * Helper class for BFS pathfinding to track search depth.
     */
    private static class PathNode {
        final WorldPoint room;
        final int depth;
        
        PathNode(WorldPoint room, int depth) {
            this.room = room;
            this.depth = depth;
        }
    }
    
    /**
     * Executes navigation through multiple rooms following the calculated path.
     * 
     * @param roomPath the list of room centers to traverse
     * @param finalDestination the final destination within the target room
     * @return true if successfully executed the path
     */
    private static boolean executeMultiRoomPath(List<WorldPoint> roomPath, WorldPoint finalDestination) {
        WorldPoint currentLocation = Rs2Player.getWorldLocation();
        WorldPoint exitPortalLocation = getExitPortalLocation();
        
        if (exitPortalLocation == null) {
            return false;
        }
        
        WorldPoint currentRoom = getRoomCenter(currentLocation, exitPortalLocation);
        
        // Execute each step of the path
        for (WorldPoint nextRoom : roomPath) {
            if (!executeRoomTransition(currentRoom, nextRoom)) {
                Microbot.log("Failed to transition from room " + currentRoom + " to " + nextRoom);
                return false;
            }
            currentRoom = nextRoom;
            
            // Brief pause between room transitions
            try {
                Thread.sleep(600); // 1 game tick
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }
        
        // Move to final destination within target room
        return moveToLocation(finalDestination);
    }
    
    /**
     * Executes a single room-to-room transition.
     * 
     * @param fromRoom the source room center
     * @param toRoom the destination room center
     * @return true if successfully transitioned
     */
    private static boolean executeRoomTransition(WorldPoint fromRoom, WorldPoint toRoom) {
        int deltaX = toRoom.getX() - fromRoom.getX();
        int deltaY = toRoom.getY() - fromRoom.getY();
        
        Direction direction = Direction.fromDeltas(deltaX, deltaY);
        if (direction == null) {
            return false;
        }
        
        // Find and interact with door
        WorldPoint doorLocation = new WorldPoint(
            fromRoom.getX() + (direction.getDeltaX() * ROOM_SIZE / 2),
            fromRoom.getY() + (direction.getDeltaY() * ROOM_SIZE / 2),
            fromRoom.getPlane()
        );
        
        TileObject door = findNearestDoor(doorLocation);
        if (door != null) {
            // Open the door
            if (!Rs2GameObject.interact(door, "Open")) {
                Microbot.log("Failed to open door at " + doorLocation);
                return false;
            }
            
            // Wait for door to open
            try {
                Thread.sleep(600);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }
        
        // Move through the doorway
        return moveToLocation(doorLocation);
    }
    
    /**
     * Finds the nearest door to a specified location.
     * 
     * @param searchLocation the location to search around
     * @return the nearest door TileObject or null if not found
     */
    private static TileObject findNearestDoor(WorldPoint searchLocation) {
        // Search for door objects
        for (int doorId : DOOR_OBJECT_IDS) {
            TileObject door = Rs2GameObject.getTileObject(doorId);
            if (door != null && door.getWorldLocation().distanceTo(searchLocation) <= 3) {
                return door;
            }
        }
        
        // Also search for wall objects that might be doors
        return findNearestWallDoor(searchLocation);
    }
    
    /**
     * Moves to a specific location within the POH using canvas/minimap walking.
     * 
     * @param targetLocation the WorldPoint to move to
     * @return true if successfully moved to the location
     */
    private static boolean moveToLocation(WorldPoint targetLocation) {
        WorldPoint playerLocation = Rs2Player.getWorldLocation();
        
        if (playerLocation.distanceTo(targetLocation) <= 2) {
            Microbot.log("Already near target location");
            return true;
        }
        
        // Try canvas walking first for close distances
        if (playerLocation.distanceTo(targetLocation) <= 10) {
            Microbot.log("Using canvas walking for close target");
            return Rs2Walker.walkFastCanvas(targetLocation);
        }
        
        // Use minimap walking for longer distances
        Microbot.log("Using minimap walking for distant target");
        return Rs2Walker.walkMiniMap(targetLocation);
    }
    
    /**
     * Gets the exit portal location as a reference point.
     * 
     * @return the WorldPoint of the exit portal or null if not found
     */
    public static WorldPoint getExitPortalLocation() {
        TileObject exitPortal = Rs2PoH.getExitPortal();
        return exitPortal != null ? exitPortal.getWorldLocation() : null;
    }
    
    /**
     * Calculates the center of the room containing the given location.
     * 
     * @param location the location within a room
     * @param exitPortalLocation the exit portal location as reference
     * @return the center point of the room
     */
    public static WorldPoint getRoomCenter(WorldPoint location, WorldPoint exitPortalLocation) {
        // Calculate relative position from exit portal
        int relativeX = location.getX() - exitPortalLocation.getX();
        int relativeY = location.getY() - exitPortalLocation.getY();
        
        // Determine which room this location is in (8x8 grid)
        int roomGridX = relativeX / ROOM_SIZE;
        int roomGridY = relativeY / ROOM_SIZE;
        
        // Calculate room center
        int roomCenterX = exitPortalLocation.getX() + (roomGridX * ROOM_SIZE) + (ROOM_SIZE / 2);
        int roomCenterY = exitPortalLocation.getY() + (roomGridY * ROOM_SIZE) + (ROOM_SIZE / 2);
        
        return new WorldPoint(roomCenterX, roomCenterY, location.getPlane());
    }
    
    /**
     * Clears the room data cache for the current house.
     * Useful when house layout changes or door states update.
     */
    public static void clearCurrentHouseCache() {
        Map<WorldPoint, RoomData> cache = getCurrentHouseCache();
        cache.clear();
        Microbot.log("Room data cache cleared for current house");
    }
    
    /**
     * Clears all room data caches for all houses.
     */
    public static void clearAllHouseCaches() {
        houseCaches.clear();
        currentHouseOwner = null;
        isInAdvertisedHouse = false;
        Microbot.log("All house caches cleared");
    }
    
    /**
     * Clears stale cache entries across all houses.
     */
    public static void clearStaleEntries() {
        for (Map<WorldPoint, RoomData> cache : houseCaches.values()) {
            cache.entrySet().removeIf(entry -> entry.getValue().isStale(CACHE_TIMEOUT_MS));
        }
        Microbot.log("Stale cache entries cleared");
    }
    
    /**
     * Gets detailed room information for debugging purposes.
     * 
     * @param roomCenter the center of the room to analyze
     * @return formatted string with room door states
     */
    public static String getRoomDebugInfo(WorldPoint roomCenter) {
        scanRoomDoorStates(roomCenter);
        Map<WorldPoint, RoomData> cache = getCurrentHouseCache();
        RoomData roomData = cache.get(roomCenter);
        
        if (roomData == null) {
            return "No room data available for " + roomCenter;
        }
        
        StringBuilder info = new StringBuilder();
        info.append("Room ").append(roomCenter).append(" (House: ").append(currentHouseOwner).append("):\n");
        
        for (Direction direction : Direction.values()) {
            DoorState state = roomData.getDoorState(direction);
            info.append("  ").append(direction).append(": ").append(state).append("\n");
        }
        
        return info.toString();
    }
    
    /**
     * Gets cache statistics for debugging.
     * 
     * @return formatted string with cache information
     */
    public static String getCacheStats() {
        StringBuilder stats = new StringBuilder();
        stats.append("POH Navigation Cache Statistics:\n");
        stats.append("Current House: ").append(currentHouseOwner).append("\n");
        stats.append("Is Advertised House: ").append(isInAdvertisedHouse).append("\n");
        stats.append("Total Houses Cached: ").append(houseCaches.size()).append("\n");
        
        for (Map.Entry<String, Map<WorldPoint, RoomData>> entry : houseCaches.entrySet()) {
            stats.append("  ").append(entry.getKey()).append(": ").append(entry.getValue().size()).append(" rooms\n");
        }
        
        return stats.toString();
    }
    
    /**
     * Sets the current house context manually.
     * Useful for testing or when automatic detection fails.
     * 
     * @param houseOwner the house owner identifier
     * @param isAdvertised whether this is an advertised house
     */
    public static void setHouseContext(String houseOwner, boolean isAdvertised) {
        currentHouseOwner = houseOwner;
        isInAdvertisedHouse = isAdvertised;
        Microbot.log("House context set to: " + houseOwner + " (advertised: " + isAdvertised + ")");
    }
        
    /**
     * Validates the door state cache by re-scanning rooms and comparing results.
     * Useful for ensuring cache accuracy and detecting state changes.
     * 
     * @param roomCenter the room to validate, or null to validate all cached rooms
     * @return true if cache is accurate
     */
    public static boolean validateDoorStateCache(WorldPoint roomCenter) {
        Map<WorldPoint, RoomData> cache = getCurrentHouseCache();
        
        if (roomCenter != null) {
            // Validate specific room
            return validateSingleRoom(roomCenter, cache);
        } else {
            // Validate all cached rooms
            boolean allValid = true;
            int validatedRooms = 0;
            int inaccurateRooms = 0;
            
            Microbot.log("Validating all cached room data...");
            
            for (WorldPoint cachedRoom : cache.keySet()) {
                if (validateSingleRoom(cachedRoom, cache)) {
                    validatedRooms++;
                } else {
                    inaccurateRooms++;
                    allValid = false;
                }
            }
            
            Microbot.log("Cache validation complete: " + validatedRooms + " accurate, " + 
                        inaccurateRooms + " inaccurate rooms");
            return allValid;
        }
    }
    
    /**
     * Validates a single room's cached door states.
     * 
     * @param roomCenter the room to validate
     * @param cache the cache to check against
     * @return true if cached data matches current state
     */
    private static boolean validateSingleRoom(WorldPoint roomCenter, Map<WorldPoint, RoomData> cache) {
        RoomData cachedData = cache.get(roomCenter);
        if (cachedData == null) {
            return true; // No cached data to validate
        }
        
        // Temporarily remove from cache to force re-scan
        cache.remove(roomCenter);
        scanRoomDoorStates(roomCenter);
        RoomData freshData = cache.get(roomCenter);
        
        if (freshData == null) {
            Microbot.log("Failed to re-scan room " + roomCenter + " for validation");
            cache.put(roomCenter, cachedData); // Restore original data
            return false;
        }
        
        // Compare door states
        boolean isValid = true;
        for (Direction direction : Direction.values()) {
            DoorState cachedState = cachedData.getDoorState(direction);
            DoorState freshState = freshData.getDoorState(direction);
            
            if (cachedState != freshState) {
                Microbot.log("Door state mismatch in room " + roomCenter + " " + direction + 
                           ": cached=" + cachedState + ", actual=" + freshState);
                isValid = false;
            }
        }
        
        return isValid;
    }
    
    /**
     * Analyzes room connectivity from a starting point to map accessible areas.
     * Uses flood-fill algorithm to discover all reachable rooms.
     * 
     * @param startRoom the room to start connectivity analysis from
     * @return set of all rooms reachable from the start room
     */
    public static Set<WorldPoint> analyzeRoomConnectivity(WorldPoint startRoom) {
        Set<WorldPoint> reachableRooms = new HashSet<>();
        Queue<WorldPoint> toExplore = new LinkedList<>();
        
        toExplore.offer(startRoom);
        reachableRooms.add(startRoom);
        
        Microbot.log("Starting connectivity analysis from room " + startRoom);
        
        while (!toExplore.isEmpty()) {
            WorldPoint currentRoom = toExplore.poll();
            
            // Ensure this room's doors are scanned
            scanRoomDoorStates(currentRoom);
            
            // Check all 4 directions
            for (Direction direction : Direction.values()) {
                WorldPoint adjacentRoom = new WorldPoint(
                    currentRoom.getX() + (direction.getDeltaX() * ROOM_SIZE),
                    currentRoom.getY() + (direction.getDeltaY() * ROOM_SIZE),
                    currentRoom.getPlane()
                );
                
                // Skip if already explored
                if (reachableRooms.contains(adjacentRoom)) {
                    continue;
                }
                
                // Check if movement to this room is possible
                if (canMoveToRoom(currentRoom, adjacentRoom)) {
                    reachableRooms.add(adjacentRoom);
                    toExplore.offer(adjacentRoom);
                    Microbot.log("Found reachable room: " + adjacentRoom + " via " + direction + 
                                " from " + currentRoom);
                }
            }
        }
        
        Microbot.log("Connectivity analysis complete: " + reachableRooms.size() + 
                    " rooms reachable from " + startRoom);
        return reachableRooms;
    }
    
    /**
     * Performs comprehensive POH mapping by scanning all rooms within a reasonable distance.
     * This method maps the entire house layout and door states.
     * 
     * @return map of all discovered rooms and their data
     */
    public static Map<WorldPoint, RoomData> mapEntirePOH() {
        if (!Rs2PoH.checkIsInHouse()) {
            Microbot.log("Not in a POH - cannot perform house mapping");
            return new HashMap<>();
        }
        
        WorldPoint exitPortalLocation = getExitPortalLocation();
        if (exitPortalLocation == null) {
            Microbot.log("Cannot find exit portal for reference - mapping failed");
            return new HashMap<>();
        }
        
        WorldPoint playerLocation = Rs2Player.getWorldLocation();
        WorldPoint startRoom = getRoomCenter(playerLocation, exitPortalLocation);
        
        Microbot.log("Starting comprehensive POH mapping from " + startRoom);
        
        // Use connectivity analysis to find all reachable rooms
        Set<WorldPoint> reachableRooms = analyzeRoomConnectivity(startRoom);
        
        // Also scan rooms in a grid pattern around the portal for unreachable areas
        Map<WorldPoint, RoomData> allRoomData = new HashMap<>();
        int scannedRooms = 0;
        
        final int SCAN_RADIUS = 5; // Scan 5 rooms in each direction from portal
        
        for (int deltaX = -SCAN_RADIUS; deltaX <= SCAN_RADIUS; deltaX++) {
            for (int deltaY = -SCAN_RADIUS; deltaY <= SCAN_RADIUS; deltaY++) {
                WorldPoint roomCenter = new WorldPoint(
                    exitPortalLocation.getX() + (deltaX * ROOM_SIZE),
                    exitPortalLocation.getY() + (deltaY * ROOM_SIZE),
                    exitPortalLocation.getPlane()
                );
                
                // Scan this room regardless of reachability
                scanRoomDoorStates(roomCenter);
                scannedRooms++;
                
                // Add to our result map
                Map<WorldPoint, RoomData> cache = getCurrentHouseCache();
                RoomData roomData = cache.get(roomCenter);
                if (roomData != null) {
                    allRoomData.put(roomCenter, roomData);
                }
            }
        }
        
        Microbot.log("POH mapping complete: " + scannedRooms + " rooms scanned, " + 
                    allRoomData.size() + " rooms with data, " + reachableRooms.size() + 
                    " rooms reachable from starting position");
        
        return allRoomData;
    }
    
    /**
     * Generates a detailed connectivity report for debugging navigation issues.
     * 
     * @return formatted string with connectivity information
     */
    public static String generateConnectivityReport() {
        if (!Rs2PoH.checkIsInHouse()) {
            return "Not in a POH - cannot generate connectivity report";
        }
        
        WorldPoint playerLocation = Rs2Player.getWorldLocation();
        WorldPoint exitPortalLocation = getExitPortalLocation();
        
        if (exitPortalLocation == null) {
            return "Cannot find exit portal for reference";
        }
        
        WorldPoint currentRoom = getRoomCenter(playerLocation, exitPortalLocation);
        Set<WorldPoint> reachableRooms = analyzeRoomConnectivity(currentRoom);
        Map<WorldPoint, RoomData> cache = getCurrentHouseCache();
        
        StringBuilder report = new StringBuilder();
        report.append("=== POH Connectivity Report ===\n");
        report.append("Current House: ").append(currentHouseOwner).append("\n");
        report.append("Player Location: ").append(playerLocation).append("\n");
        report.append("Current Room: ").append(currentRoom).append("\n");
        report.append("Exit Portal: ").append(exitPortalLocation).append("\n");
        report.append("Reachable Rooms: ").append(reachableRooms.size()).append("\n\n");
        
        report.append("=== Room Details ===\n");
        for (WorldPoint room : reachableRooms) {
            RoomData roomData = cache.get(room);
            if (roomData != null) {
                report.append("Room ").append(room).append(":\n");
                for (Direction direction : Direction.values()) {
                    DoorState state = roomData.getDoorState(direction);
                    report.append("  ").append(direction).append(": ").append(state).append("\n");
                }
                report.append("\n");
            }
        }
        
        return report.toString();
    }
    
    /**
     * Refreshes door states for all cached rooms.
     * Useful when doors have been manually opened/closed.
     */
    public static void refreshAllDoorStates() {
        Map<WorldPoint, RoomData> cache = getCurrentHouseCache();
        Set<WorldPoint> roomsToRefresh = new HashSet<>(cache.keySet());
        
        Microbot.log("Refreshing door states for " + roomsToRefresh.size() + " cached rooms");
        
        // Clear cache and re-scan all rooms
        cache.clear();
        
        for (WorldPoint room : roomsToRefresh) {
            scanRoomDoorStates(room);
        }
        
        Microbot.log("Door state refresh complete");
    }
    
    // ========== PERFORMANCE OPTIMIZATION FEATURES ==========
    
    /**
     * Performance monitoring for navigation operations
     */
    private static final Map<String, Long> performanceMetrics = new ConcurrentHashMap<>();
    private static final int PERFORMANCE_SAMPLE_SIZE = 10;
    
    
    /**
     * Measures execution time for performance analysis.
     * 
     * @param operationName the name of the operation being measured
     * @param operation the operation to execute and measure
     * @return the result of the operation
     */
    private static <T> T measurePerformance(String operationName, java.util.function.Supplier<T> operation) {
        long startTime = System.currentTimeMillis();
        T result = operation.get();
        long duration = System.currentTimeMillis() - startTime;
        
        // Update rolling average
        performanceMetrics.merge(operationName, duration, (oldVal, newVal) -> {
            // Simple moving average over last PERFORMANCE_SAMPLE_SIZE samples
            return (oldVal * (PERFORMANCE_SAMPLE_SIZE - 1) + newVal) / PERFORMANCE_SAMPLE_SIZE;
        });
        
        if (duration > 1000) { // Log slow operations
            Microbot.log("Slow operation: " + operationName + " took " + duration + "ms");
        }
        
        return result;
    }
    
    /**
     * Gets performance statistics for all navigation operations.
     * 
     * @return formatted string with performance data
     */
    public static String getPerformanceStats() {
        StringBuilder stats = new StringBuilder();
        stats.append("=== POH Navigation Performance Stats ===\n");
        
        for (Map.Entry<String, Long> entry : performanceMetrics.entrySet()) {
            stats.append(entry.getKey()).append(": ").append(entry.getValue()).append("ms avg\n");
        }
        
        return stats.toString();
    }
    
    
    // // ========== INTEGRATION WITH OTHER POH SYSTEMS ==========
    
    // /**
    //  * Integration with POH teleport systems.
    //  * Provides enhanced navigation after house teleports.
    //  */
    // public static class POHTeleportIntegration {
        
    //     /**
    //      * Navigates to an object after teleporting to own house.
    //      * 
    //      * @param targetObjectId the target object ID
    //      * @return true if teleport and navigation succeeded
    //      */
    //     public static boolean teleportToOwnHouseAndNavigate(int targetObjectId) {
    //         return measurePerformance("House Teleport + Navigation", () -> {
    //             // Clear cache since we're entering a fresh house instance
    //             clearCurrentHouseCache();
                
    //             if (!navigateToOwnHouse()) {
    //                 return false;
    //             }
                
    //             // Brief wait for house to fully load
    //             sleep(1200);
                
    //             return navigateToObject(targetObjectId);
    //         });
    //     }
        
    //     /**
    //      * Navigates to an object after entering an advertised house.
    //      * 
    //      * @param targetObjectId the target object ID
    //      * @return true if house entry and navigation succeeded
    //      */
    //     public static boolean enterAdvertisedHouseAndNavigate(int targetObjectId) {
    //         return measurePerformance("Advertisement House + Navigation", () -> {
    //             // Clear cache since we're entering a different house
    //             clearCurrentHouseCache();
                
    //             if (!navigateToAdvertisementHouse()) {
    //                 return false;
    //             }
                
    //             // Brief wait for house to fully load
    //             sleep(1200);
                
    //             return navigateToObject(targetObjectId);
    //         });
    //     }
        
    //     /**
    //      * Smart house selection for teleports based on available objects.
    //      * Chooses between own house and advertised houses based on object availability.
    //      * 
    //      * @param requiredObjectIds list of object IDs that must be available
    //      * @return true if suitable house found and entered
    //      */
    //     public static boolean findBestHouseForObjects(int... requiredObjectIds) {
    //         return measurePerformance("Smart House Selection", () -> {
    //             // Try own house first
    //             if (navigateToOwnHouse()) {
    //                 boolean hasAllObjects = true;
    //                 for (int objectId : requiredObjectIds) {
    //                     if (Rs2GameObject.getTileObject(objectId) == null) {
    //                         hasAllObjects = false;
    //                         break;
    //                     }
    //                 }
                    
    //                 if (hasAllObjects) {
    //                     Microbot.log("Own house has all required objects");
    //                     return true;
    //                 }
    //             }
                
    //             // Try advertised houses if own house doesn't have required objects
    //             Microbot.log("Own house lacks required objects, trying advertised houses");
    //             if (navigateToAdvertisementHouse()) {
    //                 boolean hasAllObjects = true;
    //                 for (int objectId : requiredObjectIds) {
    //                     if (Rs2GameObject.getTileObject(objectId) == null) {
    //                         hasAllObjects = false;
    //                         break;
    //                     }
    //                 }
                    
    //                 if (hasAllObjects) {
    //                     Microbot.log("Advertised house has all required objects");
    //                     return true;
    //                 }
    //             }
                
    //             Microbot.log("No suitable house found with required objects");
    //             return false;
    //         });
    //     }
    // }
    
    // /**
    //  * Integration with POH construction and room management.
    //  */
    // public static class POHRoomIntegration {
        
    //     /**
    //      * Identifies the type of room at a given location.
    //      * 
    //      * @param roomCenter the center of the room to identify
    //      * @return room type string or "Unknown Room"
    //      */
    //     public static String identifyRoomType(WorldPoint roomCenter) {
    //         return measurePerformance("Room Type Identification", () -> {
    //             // Scan for characteristic objects in the room
    //             Map<String, Integer> objectCounts = new HashMap<>();
                
    //             // Define room-specific object patterns
    //             final int SCAN_RADIUS = 6; // Within room boundaries
                
    //             for (int deltaX = -SCAN_RADIUS; deltaX <= SCAN_RADIUS; deltaX++) {
    //                 for (int deltaY = -SCAN_RADIUS; deltaY <= SCAN_RADIUS; deltaY++) {
    //                     WorldPoint scanPoint = new WorldPoint(
    //                         roomCenter.getX() + deltaX,
    //                         roomCenter.getY() + deltaY,
    //                         roomCenter.getPlane()
    //                     );
                        
    //                     List<GameObject> objects = Rs2GameObject.getGameObjects(scanPoint);
    //                     for (TileObject obj : objects) {
    //                         ObjectComposition comp = Rs2GameObject.convertToObjectComposition(obj);
    //                         if (comp != null && comp.getName() != null) {
    //                             String name = comp.getName().toLowerCase();
    //                             objectCounts.merge(name, 1, Integer::sum);
    //                         }
    //                     }
    //                 }
    //             }
                
    //             // Identify room type based on object patterns
    //             if (objectCounts.containsKey("portal nexus") || objectCounts.containsKey("teleport focus")) {
    //                 return "Portal Nexus Room";
    //             } else if (objectCounts.containsKey("jewellery box") || objectCounts.containsKey("basic jewellery box")) {
    //                 return "Jewellery Room";
    //             } else if (objectCounts.containsKey("pool of restoration") || objectCounts.containsKey("pool of rejuvenation")) {
    //                 return "Pool Room";
    //             } else if (objectCounts.containsKey("altar") || objectCounts.containsKey("gilded altar")) {
    //                 return "Chapel";
    //             } else if (objectCounts.containsKey("lectern") || objectCounts.containsKey("demon lectern")) {
    //                 return "Study";
    //             } else if (objectCounts.containsKey("larder") || objectCounts.containsKey("kitchen table")) {
    //                 return "Kitchen";
    //             } else if (objectCounts.containsKey("armour stand") || objectCounts.containsKey("cape rack")) {
    //                 return "Costume Room";
    //             } else if (objectCounts.containsKey("portal") && objectCounts.size() == 1) {
    //                 return "Portal Room (Basic)";
    //             } else if (objectCounts.containsKey("exit portal")) {
    //                 return "Garden (Exit)";
    //             } else if (objectCounts.size() == 0) {
    //                 return "Empty Room";
    //             } else {
    //                 return "Mixed/Unknown Room";
    //             }
    //         });
    //     }
        
    //     /**
    //      * Gets a list of all built rooms in the house with their types.
    //      * 
    //      * @return map of room centers to room types
    //      */
    //     public static Map<WorldPoint, String> getAllRoomTypes() {
    //         return measurePerformance("Full House Room Analysis", () -> {
    //             Map<WorldPoint, String> roomTypes = new HashMap<>();
    //             Map<WorldPoint, RoomData> houseMap = mapEntirePOH();
                
    //             for (WorldPoint roomCenter : houseMap.keySet()) {
    //                 String roomType = identifyRoomType(roomCenter);
    //                 if (!"Empty Room".equals(roomType)) {
    //                     roomTypes.put(roomCenter, roomType);
    //                 }
    //             }
                
    //             Microbot.log("Identified " + roomTypes.size() + " built rooms in POH");
    //             return roomTypes;
    //         });
    //     }
        
    //     /**
    //      * Finds the nearest room of a specific type.
    //      * 
    //      * @param targetRoomType the type of room to find
    //      * @return room center of nearest matching room or null
    //      */
    //     public static WorldPoint findNearestRoomOfType(String targetRoomType) {
    //         return measurePerformance("Find Room by Type", () -> {
    //             WorldPoint playerLocation = Rs2Player.getWorldLocation();
    //             WorldPoint exitPortal = getExitPortalLocation();
                
    //             if (exitPortal == null) {
    //                 return null;
    //             }
                
    //             WorldPoint currentRoom = getRoomCenter(playerLocation, exitPortal);
    //             Map<WorldPoint, String> allRooms = getAllRoomTypes();
                
    //             WorldPoint nearestRoom = null;
    //             double nearestDistance = Double.MAX_VALUE;
                
    //             for (Map.Entry<String, String> entry : allRooms.entrySet()) {
    //                 if (targetRoomType.equalsIgnoreCase(entry.getValue())) {
    //                     double distance = currentRoom.distanceTo(entry.getKey());
    //                     if (distance < nearestDistance) {
    //                         nearestDistance = distance;
    //                         nearestRoom = entry.getKey();
    //                     }
    //                 }
    //             }
                
    //             if (nearestRoom != null) {
    //                 Microbot.log("Found " + targetRoomType + " at " + nearestRoom + 
    //                             " (distance: " + (int)nearestDistance + ")");
    //             }
                
    //             return nearestRoom;
    //         });
    //     }
    // }
    
    // /**
    //  * Advanced diagnostics and troubleshooting tools.
    //  */
    // public static class POHDiagnostics {
        
    //     /**
    //      * Performs comprehensive POH navigation diagnostics.
    //      * 
    //      * @return detailed diagnostic report
    //      */
    //     public static String runFullDiagnostics() {
    //         return measurePerformance("Full Diagnostics", () -> {
    //             StringBuilder report = new StringBuilder();
    //             report.append("=== POH Navigation Full Diagnostics ===\n");
    //             report.append("Timestamp: ").append(new Date()).append("\n\n");
                
    //             // Basic status
    //             report.append("=== Basic Status ===\n");
    //             report.append("In House: ").append(Rs2PoH.isInHouse()).append("\n");
    //             report.append("Current House Owner: ").append(currentHouseOwner).append("\n");
    //             report.append("Is Advertised House: ").append(isInAdvertisedHouse).append("\n");
                
    //             if (Rs2PoH.isInHouse()) {
    //                 WorldPoint playerLoc = Rs2Player.getWorldLocation();
    //                 WorldPoint exitPortal = getExitPortalLocation();
                    
    //                 report.append("Player Location: ").append(playerLoc).append("\n");
    //                 report.append("Exit Portal: ").append(exitPortal).append("\n");
                    
    //                 if (exitPortal != null) {
    //                     WorldPoint currentRoom = getRoomCenter(playerLoc, exitPortal);
    //                     report.append("Current Room: ").append(currentRoom).append("\n");
    //                     report.append("Room Type: ").append(POHRoomIntegration.identifyRoomType(currentRoom)).append("\n");
    //                 }
    //             }
                
    //             report.append("\n");
                
    //             // Cache statistics
    //             report.append(getCacheStats()).append("\n");
                
    //             // Performance metrics
    //             report.append(getPerformanceStats()).append("\n");
                
    //             // Room analysis
    //             if (Rs2PoH.isInHouse()) {
    //                 report.append("=== Room Analysis ===\n");
    //                 Map<WorldPoint, String> rooms = POHRoomIntegration.getAllRoomTypes();
    //                 for (Map.Entry<WorldPoint, String> entry : rooms.entrySet()) {
    //                     report.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
    //                 }
    //                 report.append("\n");
                    
    //                 // Connectivity report
    //                 report.append(generateConnectivityReport()).append("\n");
    //             }
                
    //             return report.toString();
    //         });
    //     }
        
    //     /**
    //      * Tests navigation to all major POH objects for debugging.
    //      * 
    //      * @return test results summary
    //      */
    //     public static String testAllNavigationTargets() {
    //         return measurePerformance("Navigation Test Suite", () -> {
    //             StringBuilder results = new StringBuilder();
    //             results.append("=== Navigation Test Results ===\n");
                
    //             if (!Rs2PoH.isInHouse()) {
    //                 results.append("Not in house - cannot run navigation tests\n");
    //                 return results.toString();
    //             }
                
    //             // Test common POH objects
    //             int[] testObjects = {
    //                 22705, 22706, 22707, // Portal Nexus
    //                 20626, 20627, 20628, // Jewellery Box
    //                 29241, 29242, 29243, // Pool of Restoration
    //                 13179, 13180, 13181  // Gilded Altar
    //             };
                
    //             String[] objectNames = {
    //                 "Portal Nexus (Basic)", "Portal Nexus (Advanced)", "Portal Nexus (Master)",
    //                 "Basic Jewellery Box", "Fancy Jewellery Box", "Ornate Jewellery Box", 
    //                 "Pool of Restoration", "Pool of Rejuvenation", "Ornate Pool",
    //                 "Oak Altar", "Teak Altar", "Gilded Altar"
    //             };
                
    //             for (int i = 0; i < testObjects.length; i++) {
    //                 int objectId = testObjects[i];
    //                 String objectName = objectNames[i];
                    
    //                 TileObject object = Rs2GameObject.getTileObject(objectId);
    //                 if (object != null) {
    //                     boolean canReach = canReachObject(objectId);
    //                     results.append(objectName).append(" (").append(objectId).append("): ");
    //                     results.append(canReach ? "REACHABLE" : "BLOCKED").append("\n");
    //                 } else {
    //                     results.append(objectName).append(" (").append(objectId).append("): NOT PRESENT\n");
    //                 }
    //             }
                
    //             return results.toString();
    //         });
    //     }
    // }
    
    /**
     * Checks if house teleport is available (either spell or tablet)
     * @return true if player can use house teleport
     */
    public static boolean hasAvailableHouseTeleport() {
        try {
            // Check for house teleport tablet in inventory
            Rs2ItemModel tablet = Rs2Inventory.get("House teleport");
            if (tablet != null) {
                return true;
            }
            
            // Check if player can cast house teleport spell
            if (Rs2Magic.canCast(MagicAction.TELEPORT_TO_HOUSE)) {
                return true;
            }
            
            return false;
        } catch (Exception e) {
            Microbot.log("Error checking house teleport availability: " + e.getMessage());
            return false;
        }
    }
}
