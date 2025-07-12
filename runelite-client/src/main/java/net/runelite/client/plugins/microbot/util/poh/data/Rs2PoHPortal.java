package net.runelite.client.plugins.microbot.util.poh.data;

import java.util.Optional;

import net.runelite.api.coords.WorldPoint;
import net.runelite.api.gameval.ObjectID;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.util.gameobject.Rs2GameObject;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;

 /**
     * Enum representing POH portal locations in the game world.
     * Maps between varbit values and portal world locations.
    */
public enum Rs2PoHPortal {    
    UNKNOWN(0, -1,"Unknown", null),
    RIMMINGTON(1, ObjectID.POH_RIMMINGTON_PORTAL,"Rimmington", new WorldPoint(2952, 3224, 0)),
    TAVERLY(2, ObjectID.POH_TAVERLY_PORTAL,"Taverly", new WorldPoint(2892, 3465, 0)),
    POLLNIVNEACH(3,ObjectID.POH_POLLNIVNEACH_PORTAL,"Pollnivneach", new WorldPoint(3339, 3001, 0)),
    RELLEKKA(4,ObjectID.POH_RELLEKKA_PORTAL,"Rellekka", new WorldPoint(2669, 3629, 0)),
    BRIMHAVEN(5,ObjectID.POH_BRIMHAVEN_PORTAL,"Brimhaven", new WorldPoint(2756, 3176, 0)),
    YANILLE(6, ObjectID.POH_YANILLE_PORTAL,"Yanille", new WorldPoint(2545, 3097, 0)),
    PRIFDDINAS(7, -1,"Prifddinas", new WorldPoint(3239, 6077, 0)),
    HOSIDIUS(8,ObjectID.POH_KOUREND_PORTAL, "Hosidius", new WorldPoint(1740, 3517, 0));
    private final static int POH_HOUSE_LOCATION_VARBIT = 2187; // Varbit for player house location
    private final int varbitValue;
    private final int objectID;
    private final String name;
    private final WorldPoint worldPoint;

    Rs2PoHPortal(int varbitValue, int objectID,String name, WorldPoint worldPoint) {
        this.varbitValue = varbitValue;
        this.objectID = objectID;
        this.name = name;
        this.worldPoint = worldPoint;
    }

    public int getVarbitValue() {
        return varbitValue;
    }

    public String getName() {
        return name;
    }

    public WorldPoint getWorldPoint() {
        return worldPoint;
    }

    public static Rs2PoHPortal getByVarbitValue(int value) {
        for (Rs2PoHPortal portal : Rs2PoHPortal.values()) {
            if (portal.getVarbitValue() == value) {
                return portal;
            }
        }
        return UNKNOWN;
    }

    /**
     * Gets information about the player's house portal location.
     * 
     * @return Rs2PoHPortal enum containing information about the player's house portal,
     *         or UNKNOWN if location is not set or invalid
     */
    public static Optional<Rs2PoHPortal> getPlayerHousePortal() {
        try {
            int varbitValue = Microbot.getClientThread().runOnClientThreadOptional( () -> {
                    return Microbot.getClient().getVarbitValue(POH_HOUSE_LOCATION_VARBIT);
                }
                ).orElse(-1);
            if (varbitValue == -1) {
                return Optional.of(Rs2PoHPortal.UNKNOWN);
            }
            return Optional.of(Rs2PoHPortal.getByVarbitValue(varbitValue));
        } catch (Exception e) {
            Microbot.log("Error getting house portal information: " + e.getMessage());
            return Optional.empty();
        }
    }
    /**
     * Gets the player's house portal location in the game world (not inside the house)
     * based on the POH_HOUSE_LOCATION varbit value.
     * 
     * @return WorldPoint of the player's house portal, or null if location is not set or invalid
     */
    public static WorldPoint getPlayerHousePortalLocation() {
        Optional<Rs2PoHPortal> portal = getPlayerHousePortal();
        if( portal.isEmpty() || portal.get() == UNKNOWN) {
            return null;
        }
        return portal.get().getWorldPoint();
    }
    public boolean isNearPortal() {
        if (this == UNKNOWN || this.worldPoint == null) {
            return false;
        }
        return Rs2Player.getWorldLocation().distanceTo(this.worldPoint) < 10 && Rs2GameObject.getAll((object) -> object.getId()==this.objectID).stream().findAny()==null;
    }

}