package net.runelite.client.plugins.microbot.util.poh;


import net.runelite.api.GameObject;
import net.runelite.api.TileObject;
import net.runelite.api.gameval.VarbitID;
import net.runelite.api.gameval.ObjectID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.util.gameobject.Rs2GameObject;
import net.runelite.client.plugins.microbot.util.keyboard.Rs2Keyboard;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;
import net.runelite.client.plugins.microbot.util.poh.data.PoHTeleport;
import net.runelite.client.plugins.microbot.util.poh.navigation.Rs2PoHNavigation;
import net.runelite.client.plugins.microbot.util.walker.Rs2Walker;
import net.runelite.client.plugins.microbot.util.widget.Rs2Widget;
import java.util.*;
import java.util.stream.Collectors;
import com.google.common.collect.ImmutableList;
import lombok.extern.slf4j.Slf4j;

import static net.runelite.client.plugins.microbot.util.Global.sleepUntil;
import static net.runelite.client.plugins.microbot.util.Global.sleepUntilTrue;

/**
 * Utility class for Player-Owned House Portal Nexus functionality.
 * Handles portal nexus interactions, configuration reading, and teleport management.
 */
@Slf4j
public class Rs2PortalNexus {
    
    // Portal Nexus Object IDs 
    private static final ImmutableList<Integer> PORTAL_NEXUS_IDS =ImmutableList.of(
        22705, // poh_portal_nexus_1
        22706, // poh_portal_nexus_2
        22707, // poh_portal_nexus_3
        30497  // poh_portal_nexus_league_5
    );
     private static final ImmutableList<Integer> MOUNTED_XERIC_IDS =ImmutableList.of(
        ObjectID.POH_AMULET_XERIC_LOOKOUT, 
        ObjectID.POH_AMULET_XERIC_GLADE, 
        ObjectID.POH_AMULET_XERIC_INFERNO, 
        ObjectID.POH_AMULET_XERIC_HEART,  
        ObjectID.POH_AMULET_XERIC_HONOUR,
        ObjectID.POH_AMULET_XERIC // poh_portal_nexus_xerics_talisman
    );
    private static final ImmutableList<Integer> MOUNTED_DIGSITE_IDS =ImmutableList.of(
        ObjectID.POH_AMULET_DIG_DIGSITE,
        ObjectID.POH_AMULET_DIG_FOSSIL, 
        ObjectID.POH_AMULET_DIG_LITHKREN, 
        ObjectID.POH_AMULET_DIGSITE
    );
        
    
    // Portal Nexus Configuration VarBits: Maps slot number (1-32) to VarbitID
    // The varbit value tells us WHICH teleport is configured in that slot
    private static final Map<Integer, Integer> PORTAL_NEXUS_SLOT_TO_VARBIT;
    static {
        Map<Integer, Integer> tempMap = new HashMap<>();
        tempMap.put(1, VarbitID.POH_NEXUS_TELE_1);
        tempMap.put(2, VarbitID.POH_NEXUS_TELE_2);
        tempMap.put(3, VarbitID.POH_NEXUS_TELE_3);
        tempMap.put(4, VarbitID.POH_NEXUS_TELE_4);
        tempMap.put(5, VarbitID.POH_NEXUS_TELE_5);
        tempMap.put(6, VarbitID.POH_NEXUS_TELE_6);
        tempMap.put(7, VarbitID.POH_NEXUS_TELE_7);
        tempMap.put(8, VarbitID.POH_NEXUS_TELE_8);
        tempMap.put(9, VarbitID.POH_NEXUS_TELE_9);
        tempMap.put(10, VarbitID.POH_NEXUS_TELE_10);
        tempMap.put(11, VarbitID.POH_NEXUS_TELE_11);
        tempMap.put(12, VarbitID.POH_NEXUS_TELE_12);
        tempMap.put(13, VarbitID.POH_NEXUS_TELE_13);
        tempMap.put(14, VarbitID.POH_NEXUS_TELE_14);
        tempMap.put(15, VarbitID.POH_NEXUS_TELE_15);
        tempMap.put(16, VarbitID.POH_NEXUS_TELE_16);
        tempMap.put(17, VarbitID.POH_NEXUS_TELE_17);
        tempMap.put(18, VarbitID.POH_NEXUS_TELE_18);
        tempMap.put(19, VarbitID.POH_NEXUS_TELE_19);
        tempMap.put(20, VarbitID.POH_NEXUS_TELE_20);
        tempMap.put(21, VarbitID.POH_NEXUS_TELE_21);
        tempMap.put(22, VarbitID.POH_NEXUS_TELE_22);
        tempMap.put(23, VarbitID.POH_NEXUS_TELE_23);
        tempMap.put(24, VarbitID.POH_NEXUS_TELE_24);
        tempMap.put(25, VarbitID.POH_NEXUS_TELE_25);
        tempMap.put(26, VarbitID.POH_NEXUS_TELE_26);
        tempMap.put(27, VarbitID.POH_NEXUS_TELE_27);
        tempMap.put(28, VarbitID.POH_NEXUS_TELE_28);
        tempMap.put(29, VarbitID.POH_NEXUS_TELE_29);
        tempMap.put(30, VarbitID.POH_NEXUS_TELE_30);        
        tempMap.put(31, VarbitID.POH_NEXUS_TELE_31);
        // Note: VarbitID.POH_NEXUS_TELE_32-35 are for Portal Chamber, not Portal Nexus
        PORTAL_NEXUS_SLOT_TO_VARBIT = Collections.unmodifiableMap(tempMap);
    }
    
    // Special Portal Nexus VarBits for mounted items
    private static final int NEXUS_XERIC_VARBIT = VarbitID.POH_NEXUS_XERIC;     // mounted xeric's talisman
    private static final int NEXUS_DIGSITE_VARBIT = VarbitID.POH_NEXUS_DIGSITE; // mounted digsite pendant
    
    // Portal Nexus Temporary Configuration VarBits: Maps slot number (1-32) to temp VarbitID
    // Used during construction/configuration of portal nexus
    private static final Map<Integer, Integer> PORTAL_NEXUS_SLOT_TO_TEMP_VARBIT;
    static {
        Map<Integer, Integer> tempMap = new HashMap<>();
        tempMap.put(1, VarbitID.POH_NEXUS_TELE_1_TEMP);
        tempMap.put(2, VarbitID.POH_NEXUS_TELE_2_TEMP);
        tempMap.put(3, VarbitID.POH_NEXUS_TELE_3_TEMP);
        tempMap.put(4, VarbitID.POH_NEXUS_TELE_4_TEMP);
        tempMap.put(5, VarbitID.POH_NEXUS_TELE_5_TEMP);
        tempMap.put(6, VarbitID.POH_NEXUS_TELE_6_TEMP);
        tempMap.put(7, VarbitID.POH_NEXUS_TELE_7_TEMP);
        tempMap.put(8, VarbitID.POH_NEXUS_TELE_8_TEMP);
        tempMap.put(9, VarbitID.POH_NEXUS_TELE_9_TEMP);
        tempMap.put(10, VarbitID.POH_NEXUS_TELE_10_TEMP);
        tempMap.put(11, VarbitID.POH_NEXUS_TELE_11_TEMP);
        tempMap.put(12, VarbitID.POH_NEXUS_TELE_12_TEMP);
        tempMap.put(13, VarbitID.POH_NEXUS_TELE_13_TEMP);
        tempMap.put(14, VarbitID.POH_NEXUS_TELE_14_TEMP);
        tempMap.put(15, VarbitID.POH_NEXUS_TELE_15_TEMP);
        tempMap.put(16, VarbitID.POH_NEXUS_TELE_16_TEMP);
        tempMap.put(17, VarbitID.POH_NEXUS_TELE_17_TEMP);
        tempMap.put(18, VarbitID.POH_NEXUS_TELE_18_TEMP);
        tempMap.put(19, VarbitID.POH_NEXUS_TELE_19_TEMP);
        tempMap.put(20, VarbitID.POH_NEXUS_TELE_20_TEMP);
        tempMap.put(21, VarbitID.POH_NEXUS_TELE_21_TEMP);
        tempMap.put(22, VarbitID.POH_NEXUS_TELE_22_TEMP);
        tempMap.put(23, VarbitID.POH_NEXUS_TELE_23_TEMP);
        tempMap.put(24, VarbitID.POH_NEXUS_TELE_24_TEMP);
        tempMap.put(25, VarbitID.POH_NEXUS_TELE_25_TEMP);
        tempMap.put(26, VarbitID.POH_NEXUS_TELE_26_TEMP);
        tempMap.put(27, VarbitID.POH_NEXUS_TELE_27_TEMP);
        tempMap.put(28, VarbitID.POH_NEXUS_TELE_28_TEMP);
        tempMap.put(29, VarbitID.POH_NEXUS_TELE_29_TEMP);
        tempMap.put(30, VarbitID.POH_NEXUS_TELE_30_TEMP);        
        tempMap.put(31, VarbitID.POH_NEXUS_TELE_31_TEMP);       
        PORTAL_NEXUS_SLOT_TO_TEMP_VARBIT = Collections.unmodifiableMap(tempMap);
    }
    final static int ADVENTURE_LOG_CONTAINER_WIDGET_OPTIONS = 12255235;
    // Portal Nexus Interface Widget ID
    public static final int PORTAL_NEXUS_INTERFACE_ID = 17;
    public static final int PORTAL_NEXUS_WIDGET_SCROLLING = 1114123; // Widget ID for the scrolling container in the portal nexus interface
    public static final int PORTAL_NEXUS_WIDGET_TEXT1 = 1114124; // Widget ID which contains the dynamic childs with the teleport dynamic widgets
    // Digsite Pendant Interface Widget ID
    public static final int DIGSITE_PENDANT_WIDGET_ID = ADVENTURE_LOG_CONTAINER_WIDGET_OPTIONS;
    // Xeric's Talisman Interface Widget ID
    public static final int XERIC_TALISMAN_WIDGET_ID = ADVENTURE_LOG_CONTAINER_WIDGET_OPTIONS; 

    /**
     * Checks if a portal nexus is present in the current area.
     * @return true if portal nexus is found
     */
    public static boolean hasPortalNexus(int distance) {
        Optional<GameObject> nexus = getPortalNexus(distance);
        return nexus != null && nexus.isPresent();
    }

    /**
     * Gets the portal nexus object in the current area.
     * @return portal nexus TileObject or null if not found
     */
    public static Optional<GameObject> getPortalNexus(int distance) {          
        GameObject nexus = Rs2GameObject.getGameObject(tileObject -> 
                PORTAL_NEXUS_IDS.contains(tileObject.getId()) 
                && tileObject.getWorldLocation().distanceTo(Rs2Player.getWorldLocation()) < distance);
        if (nexus != null) {
            return Optional.of(nexus);
        }
        return Optional.empty(); // No portal nexus found
    }
    public static boolean hasMountedXericsTalisman(int distance) {
        // Check if either Xeric's Talisman or Digsite Pendant is mounted within the specified distance
        TileObject xericsTalisman = getMountedXericsTalisman(distance);
        return xericsTalisman != null;               
    }
    public static boolean hasMountedDigsitePendant(int distance) {
        // Check if either Xeric's Talisman or Digsite Pendant is mounted within the specified distance
        TileObject digsitePendant = getMountedDigsitePandant(distance);
        return digsitePendant != null;               
    }
    /**
       * @param distance maximum distance to search
       ** @return the mounted digsite pendant GameObject if found, null otherwise
       */
    public static TileObject getMountedDigsitePandant(int distance) {
        // Check for the mounted Digsite Pendant object
        return Rs2GameObject.getTileObject(gameObject ->  (MOUNTED_DIGSITE_IDS.contains(gameObject.getId()) 
                && gameObject.getWorldLocation().distanceTo(Rs2Player.getWorldLocation()) < distance));
    }
    /**
     * Gets the mounted Xeric's Talisman GameObject within a specified distance.
     * @param distance maximum distance to search
     * @return the mounted Xeric's Talisman GameObject if found, null otherwise
     */
    public static TileObject getMountedXericsTalisman(int distance) {
        // Check for the mounted Xeric's Talisman object
        return Rs2GameObject.getTileObject(gameObject -> (MOUNTED_XERIC_IDS.contains(gameObject.getId()) 
                && gameObject.getWorldLocation().distanceTo(Rs2Player.getWorldLocation()) < distance));
    }

   
    /**
     * Gets the portal nexus teleport configuration from varbits.
     * @return map of slot numbers to their configured PoHTeleports
     */
    public static Map<Integer, PoHTeleport> getPortalNexusConfigurationOfPlayer() {
        Map<Integer, PoHTeleport> config = new HashMap<>();

        for (Map.Entry<Integer, Integer> entry : PORTAL_NEXUS_SLOT_TO_VARBIT.entrySet()) {
           
            int slot = entry.getKey();
            int varbitId = entry.getValue();
            int varbitValue = Microbot.getVarbitValue(varbitId);
            
            if (varbitValue != 0) { // Only include configured slots
                PoHTeleport teleport = PoHTeleport.fromNexusVarbitValue(varbitValue);
                if (teleport != null) {
                    config.put(slot, teleport);
                }
            }
        }                       
        return config;
    }

    /**
     * Gets the temporary portal nexus configuration from varbits (used during construction).
     * @return map of slot numbers to their temporary varbit values
     */
    public static Map<Integer, Integer> getPortalNexusTempConfigurationOfPlayer() {
        Map<Integer, Integer> config = new HashMap<>();
        
        for (Map.Entry<Integer, Integer> entry : PORTAL_NEXUS_SLOT_TO_TEMP_VARBIT.entrySet()) {
            int slot = entry.getKey();
            int varbitId = entry.getValue();
            int varbitValue = Microbot.getVarbitValue(varbitId);
            
            if (varbitValue != 0) { // Only include non-zero values
                config.put(slot, varbitValue);
            }
        }
        
        return config;
    }

    /**
     * Checks if a specific teleport is configured in any portal nexus slot.
     * @param teleport the PoHTeleports teleport to check for
     * @return true if the teleport is configured in any slot
     */
    public static boolean isTeleportAvailableForPlayer(PoHTeleport teleport) {
        if (teleport == null) {
            return false; // Null teleport is not available
        }
        Map<Integer, PoHTeleport> config = getPortalNexusConfigurationOfPlayer();
        boolean isXercsTeleport = getXericsTalismanDestinations().contains(teleport);
        boolean isDigsiteTeleport = getDigsitePendantDestinations().contains(teleport);
        if ( config.containsValue(teleport)) {
            return true; // Teleport is configured in a slot
        }
        if (isXercsTeleport){
            return hasXericsTalismanMounted();
        }
        if (isDigsiteTeleport) {
            return hasDigsitePendantMounted();
        }             
        return false; // Teleport not found in configuration    
    }

    /**
     * Gets the slot number where a specific teleport is configured.
     * @param teleport the PoHTeleports teleport to find
     * @return the slot number (1-32) where the teleport is configured, or -1 if not found
     */
    public static int getSlotForTeleportOfPlayer(PoHTeleport teleport) {
        
        if (teleport == null) {
            return -1; // Null teleport has no slot
        }
        if (!teleport.isPortalTeleport()) {
            return -1; // Only portal teleports have slots in the nexus
        }
        Map<Integer, PoHTeleport> config = getPortalNexusConfigurationOfPlayer();
        for (Map.Entry<Integer, PoHTeleport> entry : config.entrySet()) {
            if (entry.getValue() == teleport) {
                return entry.getKey();
            }
        }
        return -1; // Not found
    }

    /**
     * Gets the varbit value for a specific portal nexus slot.
     * @param slot the slot number (1-31) to get the varbit value for
     * @return the varbit value for that slot, or -1 if slot is invalid
     */
    public static int getSlotVarbitValue(int slot) {
        if (slot < 1 || slot > 31) {
            return -1; // Invalid slot
        }
        
        Integer varbitId = PORTAL_NEXUS_SLOT_TO_VARBIT.get(slot);
        if (varbitId == null) {
            return -1; // No varbit mapping for this slot
        }
        
        return Microbot.getVarbitValue(varbitId);
    }

    /**
     * Gets the temporary varbit value for a specific portal nexus slot.
     * Used during construction/configuration of portal nexus.
     * @param slot the slot number (1-31) to get the temporary varbit value for
     * @return the temporary varbit value for that slot, or -1 if slot is invalid
     */
    public static int getSlotTempVarbitValue(int slot) {
        if (slot < 1 || slot > 31) {
            return -1; // Invalid slot
        }
        
        Integer varbitId = PORTAL_NEXUS_SLOT_TO_TEMP_VARBIT.get(slot);
        if (varbitId == null) {
            return -1; // No varbit mapping for this slot
        }
        
        return Microbot.getVarbitValue(varbitId);
    }

    /**
     * Gets the teleport configured in a specific slot.
     * @param slot the slot number (1-32)
     * @return the PoHTeleports configured in that slot, or null if slot is empty or invalid
     */
    public static PoHTeleport getTeleportInSlotForPlayer(int slot) {
        if (slot < 1 || slot > 31) {
            return null;
        }
        
        Integer varbitId = PORTAL_NEXUS_SLOT_TO_VARBIT.get(slot);
        if (varbitId == null) {
            return null;
        }
        
        int varbitValue = Microbot.getVarbitValue(varbitId);
        if (varbitValue == 0) {
            return null; // Slot is empty
        }
        
        return PoHTeleport.fromNexusVarbitValue(varbitValue);
    }

    /**
     * Gets the varbit ID for a specific portal nexus slot.
     * @param slot the slot number (1-32)
     * @return the varbit ID for that slot, or -1 if invalid slot
     */
    public static int getVarbitIdForSlot(int slot) {
        Integer varbitId = PORTAL_NEXUS_SLOT_TO_VARBIT.get(slot);
        return varbitId != null ? varbitId : -1;
    }

    /**
     * Gets all configured teleports from the portal nexus.
     * @return list of all PoHTeleports currently configured
     */
    public static List<PoHTeleport> getConfiguredTeleportsOfPlayer() {
        Map<Integer, PoHTeleport> config = getPortalNexusConfigurationOfPlayer();
        return new ArrayList<>(config.values());
    }

    // ===============================
    // MOUNTED JEWELRY DESTINATIONS
    // ===============================

    /**
     * Checks if Xeric's Talisman is mounted in the Portal Nexus.
     * @return true if Xeric's Talisman is mounted
     */
    public static boolean hasXericsTalismanMounted() {
        return Microbot.getVarbitValue(NEXUS_XERIC_VARBIT) != 0;
    }

    /**
     * Checks if Digsite Pendant is mounted in the Portal Nexus.
     * @return true if Digsite Pendant is mounted
     */
    public static boolean hasDigsitePendantMounted() {
        return Microbot.getVarbitValue(NEXUS_DIGSITE_VARBIT) != 0;
    }

    /**
     * Gets all available Xeric's Talisman destinations if mounted.
     * @return list of Xeric's Talisman teleport destinations, or empty list if not mounted
     */
    public static List<PoHTeleport> getXericsTalismanDestinations() {
        if (!hasXericsTalismanMounted()) {
            return new ArrayList<>();
        }

        return Arrays.asList(
                PoHTeleport.XERICS_LOOKOUT,
                PoHTeleport.XERICS_GLADE,
                PoHTeleport.XERICS_INFERNO,
                PoHTeleport.XERICS_HEART,
                PoHTeleport.XERICS_HONOUR
        );
    }

    /**
     * Gets all available Digsite Pendant destinations if mounted.
     * @return list of Digsite Pendant teleport destinations, or empty list if not mounted
     */
    public static List<PoHTeleport> getDigsitePendantDestinations() {
        if (!hasDigsitePendantMounted()) {
            return new ArrayList<>();
        }

        return Arrays.asList(
                PoHTeleport.DIGSITE,
                PoHTeleport.FOSSIL_ISLAND,
                PoHTeleport.LITHKREN
        );
    }

    /**
     * Gets all available mounted jewelry destinations from the Portal Nexus.
     * This includes destinations from both Xeric's Talisman and Digsite Pendant if mounted.
     * @return list of all mounted jewelry teleport destinations
     */
    public static List<PoHTeleport> getAllMountedJewelryDestinations() {
        List<PoHTeleport> destinations = new ArrayList<>();
        destinations.addAll(getXericsTalismanDestinations());
        destinations.addAll(getDigsitePendantDestinations());
        return destinations;
    }

    /**
     * Gets all teleport destinations available from the Portal Nexus.
     * This includes both configured portal teleports and mounted jewelry destinations.
     * @return list of all available teleport destinations
     */
    public static List<PoHTeleport> getAllAvailableDestinations() {
        List<PoHTeleport> destinations = new ArrayList<>();
        destinations.addAll(getConfiguredTeleportsOfPlayer());
        destinations.addAll(getAllMountedJewelryDestinations());
        return destinations;
    }

    /**
     * Checks if a specific teleport destination is available from the Portal Nexus.
     * This checks both configured portal teleports and mounted jewelry destinations.
     * @param teleport the teleport destination to check
     * @return true if the destination is available
     */
    public static boolean isDestinationAvailable(PoHTeleport teleport) {
        if (teleport == null) {
            return false; // Null teleport is not available
        }
        return getAllAvailableDestinations().contains(teleport);
    }



    /**
     * Gets the portal nexus interface widget.
     * @return the portal nexus interface widget or null if not open
     */
    private static Widget getDigSiteWidget() {
        return Rs2Widget.getWidget(DIGSITE_PENDANT_WIDGET_ID);

    }
     /**
     * Waits for the digsite pendant interface to be loaded.
     * @param timeoutMs maximum time to wait in milliseconds
     * @return true if interface is loaded within timeout
     */
    private static boolean waitForDigSiteWidget(int timeoutMs) {
        return sleepUntilTrue(() ->  Rs2Widget.isWidgetVisible(DIGSITE_PENDANT_WIDGET_ID) && getDigSiteWidget()!=null, 100, timeoutMs);
    }
    /**
     * Will interact with the mounted Digsite Pendant if it's available.
     * Uses long widget interaction via script with fallback to keyboard shortcuts.
     * @param pohTeleport the teleport destination
     * @return true if interaction was successful
     */
  public static boolean performDigsitePendantInteraction(PoHTeleport pohTeleport) {
        
        if (pohTeleport == null || !pohTeleport.isDigsitePendantTeleport()) {
            Microbot.log("Cannot interact with Digsite Pendant, teleport is null or not a Digsite Pendant teleport");
            return false;
        } else {
            Microbot.log("Interacting with Digsite Pendant for teleport: " + pohTeleport.getDestination());            
        }
        if (!waitForDigSiteWidget(5000)){
            String action = pohTeleport.getDestination();
            TileObject digsitePendant = getMountedDigsitePandant(10);
        
            if (digsitePendant == null) {
                Microbot.log("Mounted Digsite Pendant not found");
                return false;
            }        
            
            Rs2Walker.walkNextTo((GameObject)digsitePendant);
            if (!Rs2GameObject.interact(digsitePendant, "Teleport menu")) {
                Microbot.log("Failed to interact with Mounted Digsite Pendant");
                return false;
            }
        }
        Microbot.log("Interacting with Mounted Digsite Pendant");
        // Wait for the portal nexus interface to appear
        if(waitForDigSiteWidget(5000)){
            Microbot.log("Digsite Pendant interface appeared");
        } else {
            Microbot.log("Digsite Pendant interface did not appear");
            return false;
        }
        Widget digSiteWiget = getDigSiteWidget();            
        boolean interactSucces = interactWithWidget(digSiteWiget,digSiteWiget, pohTeleport);
        if (interactSucces) {
            Microbot.log("Successfully interacted with Digsite Pendant widget");
        } else {
            Microbot.log("Failed to interact with Digsite Pendant widget, falling back to keyboard shortcut");                
        }
        return sleepUntilTrue(() -> Rs2Player.getWorldLocation().distanceTo(pohTeleport.getLocation()) < 10, 100, 5000);
            
    }

    /**
     * Gets the Xeric's Talisman interface widget.
     * @return the Xeric's Talisman interface widget or null if not open
     */
    private static Widget getXericsTalismanWidget() {
        return Rs2Widget.getWidget(XERIC_TALISMAN_WIDGET_ID);
    }

    /**
     * Waits for the Xeric's Talisman interface to be loaded.
     * @param timeoutMs maximum time to wait in milliseconds
     * @return true if interface is loaded within timeout
     */
    private static boolean waitForXericsTalismanWidget(int timeoutMs) {
        return sleepUntilTrue(() -> Rs2Widget.isWidgetVisible(XERIC_TALISMAN_WIDGET_ID) && getXericsTalismanWidget() != null, 100, timeoutMs);
    }

    /**
     * Will interact with the mounted Xeric's Talisman if it's available.
     * Uses long widget interaction via script with fallback to keyboard shortcuts.
     * @param pohTeleport the teleport destination
     * @return true if interaction was successful
     */
    public static boolean performXericsTalismanInteraction(PoHTeleport pohTeleport) {
        if (!Rs2PoH.isInHouse()) {
            Microbot.log("Cannot interact with Xeric's Talisman, not in house");
            return false;
        }
        if (pohTeleport == null || !pohTeleport.isXericsTalismanTeleport()) {
            Microbot.log("Cannot interact with Xeric's Talisman, teleport is null or not a Xeric's Talisman teleport");
            return false;
        } else {
            Microbot.log("Interacting with Xeric's Talisman for teleport: " + pohTeleport.getDestination());            
        }
        String action = pohTeleport.getDestination();
        TileObject xericsTalisman = getMountedXericsTalisman(10);
       
        if (xericsTalisman == null) {
            Microbot.log("Mounted Xeric's Talisman not found");
            return false;
        }        
        Rs2Walker.walkNextTo((GameObject)xericsTalisman);
        if (Rs2GameObject.interact(xericsTalisman, "Teleport menu")) {
            Microbot.log("Interacting with Mounted Xeric's Talisman");
            // Wait for the Xeric's Talisman interface to appear
            if (waitForXericsTalismanWidget(5000)){
                Microbot.log("Xeric's Talisman interface appeared");
            } else {
                Microbot.log("Xeric's Talisman interface did not appear");
                return false;
            }
            Widget xericsTalismanWidget = getXericsTalismanWidget();            
            boolean interactSucces = interactWithWidget(xericsTalismanWidget,xericsTalismanWidget, pohTeleport);
            if (interactSucces) {
                Microbot.log("Successfully interacted with Xeric's Talisman widget");
            } else {
                Microbot.log("Failed to interact with Xeric's Talisman widget, falling back to keyboard shortcut");                
            }
            return sleepUntilTrue(() -> Rs2Player.getWorldLocation().distanceTo(pohTeleport.getLocation()) < 10, 100, 5000);
        }
        Microbot.log("Failed to interact with Mounted Xeric's Talisman");
        
        return false;
    }
    /**
     * Interacts with the portal nexus widget to perform a teleport.
     * Uses long widget interaction via script with fallback to keyboard shortcuts.
     * @param interactWidget the widget to interact with
     * @param pohTeleport the teleport destination
     * @return true if interaction was successful
     */
    private static boolean interactWithWidget( Widget scrollWidget, Widget textWidget, PoHTeleport pohTeleport) {        
        if (pohTeleport == null || (!pohTeleport.isPortalTeleport() && !pohTeleport.isXericsTalismanTeleport() && !pohTeleport.isDigsitePendantTeleport())) {
            Microbot.log("Cannot interact with widget, teleport is null or not a valid teleport type");
            return false;
        }        
        Widget teleportTargetWidget = Rs2Widget.searchChildren(pohTeleport.getDestination().toLowerCase(),  textWidget, false);                
        if (teleportTargetWidget == null) {
            Microbot.log("Teleport target widget not found for: " + pohTeleport.getDestination());
            return false;
        }
        Microbot.log("Interacting with Widget via scrolling -> interacting with: " + teleportTargetWidget.getText());
        Rs2Widget.clickWidgetWithScrolling(teleportTargetWidget,scrollWidget.getBounds(), scrollWidget,10);
        boolean isWildernessInterfaceOpen = sleepUntilTrue(Rs2Widget::isWildernessInterfaceOpen, 100, 1000);
        if (isWildernessInterfaceOpen) {
            Rs2Widget.enterWilderness();
        }
        return sleepUntil(() -> Rs2Player.getWorldLocation().distanceTo(pohTeleport.getLocation()) < 10,4000);
    }
   
    /**
     * Gets the portal nexus interface widget.
     * @return the portal nexus interface widget or null if not open
     */
    private  static Widget getPortalNexusWidget() {
        return Rs2Widget.getWidget(PORTAL_NEXUS_INTERFACE_ID, 0);

    }

    /**
     * Waits for the portal nexus interface to be loaded.
     * @param timeoutMs maximum time to wait in milliseconds
     * @return true if interface is loaded within timeout
     */
    private static boolean waitForPortalNexusWidget(int timeoutMs) {
        return sleepUntilTrue(() -> Rs2Widget.isWidgetVisible(PORTAL_NEXUS_WIDGET_SCROLLING) && getPortalNexusWidget() != null, 100, timeoutMs);
    }
    /**
     * Will interact with the portal nexus widget if it's open.
     * Uses long widget interaction via script with fallback to keyboard shortcuts.
     * @param pohTeleport the teleport destination
     * @return true if interaction was successful
     */
    public static boolean performPortalNexusInteraction(PoHTeleport pohTeleport) {
        if (!Rs2PoH.isInHouse()){
            Microbot.log("Cannot interact with portal nexus, not in house");
            return false;
        }
        if ( pohTeleport == null || !pohTeleport.isPortalTeleport()){
            Microbot.log("Cannot interact with portal nexus, teleport is not a portal teleport");
            return false;
        } 
        String action = pohTeleport.getDestination();
        GameObject nexObject = getPortalNexus(10).orElse(null);
       
        if (nexObject == null) {
            Microbot.log("Mounted Digsite Pendant not found");
            return false;
        }        
        Rs2Walker.walkNextToInstance(nexObject);        
        if (Rs2GameObject.interact(nexObject, "Teleport Menu")) {
            Microbot.log("Interacting with Portal Nexus for teleport: " + pohTeleport.getDestination());

            // Wait for the portal nexus interface to appear
            if (!waitForPortalNexusWidget(5000)) {
                Microbot.log("Portal Nexus interface did not appear");
                return false;
            }            
            Widget scrollWidget = Rs2Widget.getWidget(PORTAL_NEXUS_WIDGET_SCROLLING);
            Widget textWidget = Rs2Widget.getWidget(PORTAL_NEXUS_WIDGET_TEXT1);
            boolean interactSucces = interactWithWidget(scrollWidget,textWidget, pohTeleport);

            return interactSucces;
        }
        Microbot.log("Failed to interact with Portal Nexus");        
        return false;
  
        
    }



    /**
     * Interacts with the portal nexus widget to perform a teleport.
     * Uses long widget interaction via script with fallback to keyboard shortcuts.
     * @param interactWidget the widget to interact with
     * @param pohTeleport the teleport destination
     * @return true if interaction was successful
     */
    public static boolean interactWithWidgetViaKeyBoard(Widget interactWidget, String interactText) {        
       
       
        if (interactWidget == null || interactText == null || interactText.isEmpty()) {
            Microbot.log("Cannot interact with widget, widget is null or interact text is empty");
            return false;
        }

        Widget childInteractWidget = Rs2Widget.findWidget(interactText.toLowerCase(), 
                Arrays.stream(interactWidget.getStaticChildren()).collect(Collectors.toList()));
        Widget childInteractBasedOnBestMatch = findBestMatchingWidget(interactWidget.getId(), interactText);
        if (childInteractBasedOnBestMatch == null || childInteractBasedOnBestMatch.isHidden()) {
            Microbot.log("Child widget not found or is hidden for text: " + interactText);
            return false;
        }

        // if (childInteractBasedOnBestMatch != null) {
        //     //Rs2Widget.clickWidget(widget);
        //     if (Rs2Widget.isWildernessInterfaceOpen()) {
        //         Rs2Widget.enterWilderness();
        //     }
        //     Microbot.log("Traveling to " + pohTeleport.getTooltip());
        //     return sleepUntilTrue(() -> Rs2Player.getWorldLocation().distanceTo2D(pohTeleport.getLocation()) < 2, 100, 5000);
        // }                    
        // // Fallback to keyboard shortcut method
        return useKeyboardShortcutForTeleport(childInteractBasedOnBestMatch, interactText);
    }
   
    /**
     * Fallback method using keyboard shortcuts for portal nexus teleports.
     * @param widget the teleport widget
     * @param pohTeleport the teleport destination
     * @return true if keyboard shortcut was successful
     */
    private static boolean useKeyboardShortcutForTeleport(Widget widget, String pohTeleport) {
        // Regular expression to capture text between <col=ffffff> and </col>
        String regex = "<col=ffffff>(.*?)</col>";

        // Use regex to extract the shortcut key
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(regex);
        java.util.regex.Matcher matcher = pattern.matcher(widget.getText());

        if (!matcher.find()) {
            Microbot.log("Could not find keyboard shortcut for teleport: " + pohTeleport);
            return false;
        }

        // Extract the matched text
        String shortKey = matcher.group(1);
        
        // The reason we use the shortkeys instead of clicking the menu is to avoid scrolling
        // Some of the teleports are not visible in the ui to click on
        // Using shortkeys should always work even if the teleport is not visible on the screen
        Rs2Keyboard.typeString(String.valueOf(shortKey));

        boolean isWildernessInterfaceOpen = sleepUntilTrue(Rs2Widget::isWildernessInterfaceOpen, 100, 1000);

        if (isWildernessInterfaceOpen) {
            Rs2Widget.enterWilderness();
        }
        
        return sleepUntilTrue(() -> !Rs2Widget.isWidgetVisible(widget.getId()), 100, 5000);
    }

    public static boolean interactWithWidgetViaScript(Widget childInteractBasedOnBestMatch) { 
        int targetWidgetId = childInteractBasedOnBestMatch.getId();
        int targetWidgetParentId = Microbot.getClientThread().runOnClientThread(()-> childInteractBasedOnBestMatch.getParentId());
        int targetWidgetIndex =  Microbot.getClientThread().runOnClientThread(()->childInteractBasedOnBestMatch.getIndex());
        log.info("\n\ttargetWidget: " + childInteractBasedOnBestMatch + "\n\ttargetWidget.getId(): " + targetWidgetId + "\n\ttargetWidget.getParentId(): " +targetWidgetParentId + " targetWidget.getIndex(): " + targetWidgetIndex);

        assert targetWidgetParentId == targetWidgetId;
        
		// we are abusing this cs2 to just do a cc_find + cc_resume_pausebutton for us
        final int SOMETHING_THAT_CC_RESUME_PAUSEBUTTON = 1437;
        if(!Microbot.getClient().isClientThread()){
            Microbot.getClientThread().runOnClientThread(() ->{
                    Microbot.getClient().runScript(SOMETHING_THAT_CC_RESUME_PAUSEBUTTON, childInteractBasedOnBestMatch.getId(), childInteractBasedOnBestMatch.getIndex());
                    return true;
                }
            );        
        } else {
            Microbot.getClient().runScript(SOMETHING_THAT_CC_RESUME_PAUSEBUTTON, childInteractBasedOnBestMatch.getId(), childInteractBasedOnBestMatch.getIndex());
        }
        sleepUntil(() -> !Rs2Widget.isWidgetVisible(targetWidgetParentId), 5000); // Wait until the widget is no longer visible
        return !Rs2Widget.isWidgetVisible(targetWidgetParentId);
    }


    /**
     * Checks if the player is currently configuring the portal nexus.
     * @return true if temporary configuration varps have values
     */
    public static boolean isConfiguringPortalNexus() {
        return !getPortalNexusTempConfigurationOfPlayer().isEmpty();
    }




     /**
     * Finds the best matching widget based on exact match, contains match, or word similarity
     * @param widgetId Parent widget ID
     * @param targetText Text to match
     * @return Best matching widget or null if none found
     */
    public static Widget findBestMatchingWidget(int widgetId, String targetText) {
        Widget parent = Rs2Widget.getWidget(widgetId);
        if (parent == null) return null;
        
        List<Widget> children = Arrays.stream(parent.getDynamicChildren())
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
        
        // Try exact match first
        Widget exactMatch = findExactMatch(children, targetText);
        if (exactMatch != null) return exactMatch;
        
        // Try contains match second
        Widget containsMatch = findContainsMatch(children, targetText);
        if (containsMatch != null) return containsMatch;
        
        // Finally try word similarity matching
        Widget bestMatch = findBestWordSimilarityMatch(children, targetText);
        if(bestMatch != null) Microbot.log("Found best matching widget with Similarity Match for: " + targetText + " with text: " + bestMatch.getText());
        return bestMatch; 
    }


    /**
     * Finds widget with exact text match
     */
    private static Widget findExactMatch(List<Widget> widgets, String targetText) {
        return widgets.stream()
            .filter(w -> w.getText().toLowerCase().equals(targetText.toLowerCase()))
            .findFirst()
            .orElse(null);
    }

    /**
     * Finds widget containing the target text
     */
    private static Widget findContainsMatch(List<Widget> widgets, String targetText) {
        return widgets.stream()
            .filter(w -> w.getText().toLowerCase().contains(targetText.toLowerCase()))
            .findFirst()
            .orElse(null);
    }

    /**
     * Finds the widget with the highest number of matching words
     */
    private static Widget findBestWordSimilarityMatch(List<Widget> widgets, String targetText) {
        String[] targetWords = targetText.toLowerCase().split("\\s+");
        
        // Calculate match scores for each widget
        Map<Widget, Integer> matchScores = new HashMap<>();
        
        for (Widget widget : widgets) {
            String widgetText = widget.getText().toLowerCase();
            String[] widgetWords = widgetText.split("\\s+");
            
            int matchCount = countMatchingWords(targetWords, widgetWords);
            if (matchCount > 0) {
                matchScores.put(widget, matchCount);
            }
        }
        
        // Find widget with highest match score
        return matchScores.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse(null);
    }

    /**
     * Counts how many words from source array exist in target array
     */
    private static int countMatchingWords(String[] sourceWords, String[] targetWords) {
        return (int) Arrays.stream(sourceWords)
            .filter(sourceWord -> 
                Arrays.stream(targetWords)
                    .anyMatch(targetWord -> 
                        targetWord.contains(sourceWord) || sourceWord.contains(targetWord)
                    )
            )
            .count();
    }

    /**
     * Gets enhanced information about the current portal nexus setup.
     * @return formatted string with portal nexus details including teleport configuration
     */
    public static String getPortalNexusInfoDetailed() {        
    
        
        Map<Integer, PoHTeleport> config = getPortalNexusConfigurationOfPlayer();
        int configuredCount = config.size();
        
        StringBuilder info = new StringBuilder();
        info.append(String.format("Portal Nexus: "));
        
        if (configuredCount > 0) {
            info.append(String.format(", %d teleport%s configured", 
                    configuredCount, configuredCount == 1 ? "" : "s"));
            
            // Add details about configured teleports by slot order
            List<Integer> sortedSlots = config.keySet().stream()
                    .sorted()
                    .collect(Collectors.toList());
            
            for (Integer slot : sortedSlots) {
                PoHTeleport teleport = config.get(slot);
                info.append(String.format("\n\t[Slot %d: %s]", slot, teleport.getTooltip()));
            }
            
            // Add special mounted items if present
            if (Microbot.getVarbitValue(NEXUS_XERIC_VARBIT) != 0) {
                info.append("\n\t[Xeric's Talisman mounted - Destinations: ");
                List<PoHTeleport> xericsDestinations = getXericsTalismanDestinations();
                for (int i = 0; i < xericsDestinations.size(); i++) {
                    if (i > 0) info.append(", ");
                    info.append(xericsDestinations.get(i).getDestination());
                }
                info.append("]");
            }
            
            if (Microbot.getVarbitValue(NEXUS_DIGSITE_VARBIT) != 0) {
                info.append("\n\t[Digsite Pendant mounted - Destinations: ");
                List<PoHTeleport> digsiteDestinations = getDigsitePendantDestinations();
                for (int i = 0; i < digsiteDestinations.size(); i++) {
                    if (i > 0) info.append(", ");
                    info.append(digsiteDestinations.get(i).getDestination());
                }
                info.append("]");
            }
        } else {
            info.append(", no teleports configured");
        }
        
        return info.toString();
    }
    
    // ========== NAVIGATION CONVENIENCE METHODS ==========
    
    /**
     * Navigates to the Portal Nexus in the POH using Rs2PoHNavigation.
     * 
     * @return true if successfully navigated to the Portal Nexus
     */
    public static boolean navigateToPortalNexus() {
        for (int nexusId : PORTAL_NEXUS_IDS) {
            if (Rs2PoHNavigation.navigateToObject(nexusId)) {
                Microbot.log("Successfully navigated to Portal Nexus");
                return true;
            }
        }
        Microbot.log("Portal Nexus not found or unreachable");
        return false;
    }
    
    /**
     * Navigates to the Portal Nexus and interacts with it to open teleport menu.
     * 
     * @return true if successfully opened Portal Nexus teleport menu
     */
    public static boolean openPortalNexusMenu() {
        if (!navigateToPortalNexus()) {
            return false;
        }
        
        // Try different interaction options
        for (int nexusId : PORTAL_NEXUS_IDS) {
            if (Rs2GameObject.interact(nexusId, "Teleport")) {
                Microbot.log("Portal Nexus teleport menu opened");
                return true;
            }
        }
        
        Microbot.log("Failed to open Portal Nexus teleport menu");
        return false;
    }
    
    /**
     * Enhanced navigation to Portal Nexus using optimal pathfinding.
     * Uses door state detection and multi-room path calculation.
     * 
     * @return true if successfully navigated to the Portal Nexus
     */
    public static boolean navigateToPortalNexusOptimal() {
        Optional<GameObject> nexus = getPortalNexus(15);
        if (nexus.isPresent()) {
            return Rs2PoHNavigation.navigateToObjectOptimal(nexus.get());
        }
        Microbot.log("Portal Nexus not found or unreachable via optimal path");
        return false;
    }

    // ===============================
    // DIGSITE PENDANT NAVIGATION METHODS
    // ===============================

    /**
     * Navigates to the mounted Digsite Pendant in the POH.
     * 
     * @return true if successfully navigated to the Digsite Pendant
     */
    public static boolean navigateToDigsitePendant() {
        for (int digsiteId : MOUNTED_DIGSITE_IDS) {
            if (Rs2PoHNavigation.navigateToObject(digsiteId)) {
                Microbot.log("Successfully navigated to Digsite Pendant");
                return true;
            }
        }
        Microbot.log("Digsite Pendant not found or unreachable");
        return false;
    }
    
    /**
     * Navigates to the Digsite Pendant and interacts with it to open teleport menu.
     * 
     * @return true if successfully opened Digsite Pendant teleport menu
     */
    public static boolean openDigsitePendantMenu() {
        if (!navigateToDigsitePendant()) {
            return false;
        }
        
        TileObject digsitePendant = getMountedDigsitePandant(10);
        if (digsitePendant == null) {
            Microbot.log("Digsite Pendant not found after navigation");
            return false;
        }
        
        return Rs2GameObject.interact(digsitePendant, "Teleport");
    }
    
    /**
     * Enhanced navigation to Digsite Pendant using optimal pathfinding.
     * Uses door state detection and multi-room path calculation.
     * 
     * @return true if successfully navigated to the Digsite Pendant
     */
    public static boolean navigateToDigsitePendantOptimal() {
        TileObject digsitePendant = getMountedDigsitePandant(15);
        if (digsitePendant != null) {
            return Rs2PoHNavigation.navigateToObjectOptimal(digsitePendant);
        }
        Microbot.log("Digsite Pendant not found or unreachable via optimal path");
        return false;
    }

    // ===============================
    // XERIC'S TALISMAN NAVIGATION METHODS
    // ===============================

    /**
     * Navigates to the mounted Xeric's Talisman in the POH.
     * 
     * @return true if successfully navigated to the Xeric's Talisman
     */
    public static boolean navigateToXericsTalisman() {
        for (int xericId : MOUNTED_XERIC_IDS) {
            if (Rs2PoHNavigation.navigateToObject(xericId)) {
                Microbot.log("Successfully navigated to Xeric's Talisman");
                return true;
            }
        }
        Microbot.log("Xeric's Talisman not found or unreachable");
        return false;
    }
    
    /**
     * Navigates to the Xeric's Talisman and interacts with it to open teleport menu.
     * 
     * @return true if successfully opened Xeric's Talisman teleport menu
     */
    public static boolean openXericsTalismanMenu() {
        if (!navigateToXericsTalisman()) {
            return false;
        }
        
        TileObject xericsTalisman = getMountedXericsTalisman(10);
        if (xericsTalisman == null) {
            Microbot.log("Xeric's Talisman not found after navigation");
            return false;
        }
        
        return Rs2GameObject.interact(xericsTalisman, "Teleport");
    }
    
    /**
     * Enhanced navigation to Xeric's Talisman using optimal pathfinding.
     * Uses door state detection and multi-room path calculation.
     * 
     * @return true if successfully navigated to the Xeric's Talisman
     */
    public static boolean navigateToXericsTalismanOptimal() {
        TileObject xericsTalisman = getMountedXericsTalisman(15);
        if (xericsTalisman != null) {
            return Rs2PoHNavigation.navigateToObjectOptimal(xericsTalisman);
        }
        Microbot.log("Xeric's Talisman not found or unreachable via optimal path");
        return false;
    }
}
