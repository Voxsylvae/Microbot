package net.runelite.client.plugins.microbot.util.poh.navigation;

/**
 * Direction enum for POH room navigation.
 * Handles directional calculations with delta coordinates for room-to-room movement.
 */
public enum Direction {
    NORTH(0, 1), 
    SOUTH(0, -1), 
    EAST(1, 0), 
    WEST(-1, 0);
    
    private final int deltaX;
    private final int deltaY;
    
    Direction(int deltaX, int deltaY) {
        this.deltaX = deltaX;
        this.deltaY = deltaY;
    }
    
    public int getDeltaX() { 
        return deltaX; 
    }
    
    public int getDeltaY() { 
        return deltaY; 
    }
    
    /**
     * Determines the direction based on X and Y deltas.
     * 
     * @param deltaX the X direction difference
     * @param deltaY the Y direction difference
     * @return the corresponding Direction or null if no match
     */
    public static Direction fromDeltas(int deltaX, int deltaY) {
        for (Direction dir : values()) {
            if (Integer.signum(dir.deltaX) == Integer.signum(deltaX) && 
                Integer.signum(dir.deltaY) == Integer.signum(deltaY)) {
                return dir;
            }
        }
        return null;
    }
    
    /**
     * Gets the opposite direction.
     * 
     * @return the opposite Direction
     */
    public Direction getOpposite() {
        switch (this) {
            case NORTH: return SOUTH;
            case SOUTH: return NORTH;
            case EAST: return WEST;
            case WEST: return EAST;
            default: return null;
        }
    }
}
