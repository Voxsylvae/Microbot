package net.runelite.client.plugins.microbot.util.poh.navigation;

/**
 * Door state enum for tracking door conditions in POH rooms.
 * Used to determine accessibility between rooms during navigation.
 */
public enum DoorState {
    /** Door is open and passable */
    OPEN,
    
    /** Door is closed and needs to be opened */
    CLOSED,
    
    /** Door state is unknown and needs to be detected */
    UNKNOWN,
    
    /** No door exists in this direction */
    NOT_PRESENT
}
