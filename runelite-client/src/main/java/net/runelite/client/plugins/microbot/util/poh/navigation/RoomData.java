package net.runelite.client.plugins.microbot.util.poh.navigation;

import net.runelite.api.coords.WorldPoint;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Room navigation data structure to track door states and connections in POH rooms.
 * Caches information about doors in each direction and blocked paths for efficient navigation.
 */
public class RoomData {
    private final WorldPoint center;
    private final Map<Direction, DoorState> doors;
    private final Set<Direction> blockedDirections;
    private long lastScanned;
    
    /**
     * Creates a new RoomData instance for the specified room center.
     * 
     * @param center the center point of the room
     */
    public RoomData(WorldPoint center) {
        this.center = center;
        this.doors = new HashMap<>();
        this.blockedDirections = new HashSet<>();
        this.lastScanned = System.currentTimeMillis();
    }
    
    /**
     * Gets the center point of this room.
     * 
     * @return the room center WorldPoint
     */
    public WorldPoint getCenter() { 
        return center; 
    }
    
    /**
     * Gets all door states for this room.
     * 
     * @return map of directions to door states
     */
    public Map<Direction, DoorState> getDoors() { 
        return doors; 
    }
    
    /**
     * Gets all blocked directions for this room.
     * 
     * @return set of blocked directions
     */
    public Set<Direction> getBlockedDirections() { 
        return blockedDirections; 
    }
    
    /**
     * Gets the timestamp when this room was last scanned.
     * 
     * @return last scanned timestamp in milliseconds
     */
    public long getLastScanned() {
        return lastScanned;
    }
    
    /**
     * Sets the door state for a specific direction.
     * 
     * @param direction the direction of the door
     * @param state the state of the door
     */
    public void setDoorState(Direction direction, DoorState state) {
        doors.put(direction, state);
        this.lastScanned = System.currentTimeMillis();
    }
    
    /**
     * Gets the door state for a specific direction.
     * 
     * @param direction the direction to check
     * @return the door state, or UNKNOWN if not set
     */
    public DoorState getDoorState(Direction direction) {
        return doors.getOrDefault(direction, DoorState.UNKNOWN);
    }
    
    /**
     * Marks a direction as blocked (impassable).
     * 
     * @param direction the direction to block
     */
    public void blockDirection(Direction direction) {
        blockedDirections.add(direction);
        this.lastScanned = System.currentTimeMillis();
    }
    
    /**
     * Checks if a direction is blocked.
     * 
     * @param direction the direction to check
     * @return true if the direction is blocked
     */
    public boolean isDirectionBlocked(Direction direction) {
        return blockedDirections.contains(direction);
    }
    
    /**
     * Clears all cached door states for this room.
     * Forces a fresh scan on next access.
     */
    public void clearCache() {
        doors.clear();
        blockedDirections.clear();
        this.lastScanned = 0;
    }
    
    /**
     * Checks if the cached data is stale and needs refresh.
     * 
     * @param maxAge maximum age in milliseconds
     * @return true if data is older than maxAge
     */
    public boolean isStale(long maxAge) {
        return (System.currentTimeMillis() - lastScanned) > maxAge;
    }
    
    @Override
    public String toString() {
        return "RoomData{" +
                "center=" + center +
                ", doors=" + doors.size() +
                ", blocked=" + blockedDirections.size() +
                ", lastScanned=" + lastScanned +
                '}';
    }
}
