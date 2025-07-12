package net.runelite.client.plugins.microbot.util.poh;

import net.runelite.api.GameObject;
import net.runelite.api.NullObjectID;
import net.runelite.api.TileObject;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.gameval.VarbitID;
import net.runelite.api.gameval.ObjectID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.util.gameobject.Rs2GameObject;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;
import net.runelite.client.plugins.microbot.util.poh.data.PoHTeleport;
import net.runelite.client.plugins.microbot.util.poh.navigation.Rs2PoHNavigation;
import net.runelite.client.plugins.microbot.util.widget.Rs2Widget;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static net.runelite.client.plugins.microbot.util.Global.sleepUntil;

/**
 * Utility class for Player-Owned House Achievement Gallery room functionality.
 * Handles jewellery box interactions and configuration detection.
 */
public class Rs2AchievementGallery {
    private static final int BIT_POSITION_OFFSET = 2; // Offset for bit positions in jewellery box varbit
    // Jewellery Box Object IDs
    private static final Integer[] JEWELLERY_BOX_IDS = {
        ObjectID.POH_JEWELLERY_BOX_1, // Basic jewellery box
        ObjectID.POH_JEWELLERY_BOX_2, // Fancy jewellery box  
        ObjectID.POH_JEWELLERY_BOX_3  // Ornate jewellery box
    };
    
    // Jewellery Box Varbit 
    private static final int JEWELLERY_BOX_VARBIT = VarbitID.POH_JEWELLERYBOX_MULTI; // poh_jewellerybox_multi
    
    // Jewellery Box Interface Widget ID
    public static final int JEWELLERY_BOX_INTERFACE_ID = 590;

    /**
     * Checks if the player is currently in an achievement gallery room.
     * @return true if jewellery box is detected nearby
     */
    public static boolean isInAchievementGallery() {
        GameObject jewelleryBox = getJewelleryBox();
        if (jewelleryBox == null ) {
            return false; // No jewellery box found
        }
        WorldPoint achievementGalleryRoomCenter = Rs2PoHNavigation.getRoomCenter(jewelleryBox.getWorldLocation(), Rs2PoHNavigation.getExitPortalLocation());
        if (achievementGalleryRoomCenter == null ) {
            return false; // No achievement gallery room center found
        }
        return Rs2PoHNavigation.isPlayerInRoom(achievementGalleryRoomCenter);
    }

    /**
     * Checks if a jewellery box is present in the current area.
     * @return true if jewellery box is found
     */
    public static boolean hasJewelleryBox() {
        return getJewelleryBox() != null;
    }

    /**
     * Gets the jewellery box object in the current area.
     * @return jewellery box TileObject or null if not found
     */
    public static GameObject getJewelleryBox() {
        return Rs2GameObject.getGameObject(JEWELLERY_BOX_IDS);
    }

    /**
     * Gets the tier of the jewellery box based on varbit value.
     * @return jewellery box tier (1=Basic, 2=Fancy, 3=Ornate) or 0 if not found
     */
    public static int getPlayerJewelleryBoxTier() {
        int varbitValue = Microbot.getVarbitValue(JEWELLERY_BOX_VARBIT);
        
        // Interpret varbit value to determine tier
        // Based on MCP data analysis, the varbit encodes the box type
        if (varbitValue == 0) {
            return 0; // No jewellery box
        } else if (varbitValue <= 1) {
            return 1; // Basic jewellery box
        } else if (varbitValue <= 2) {
            return 2; // Fancy jewellery box
        } else {
            return 3; // Ornate jewellery box
        }
    }

    /**
     * Gets information about the current jewellery box configuration.
     * @return formatted string with jewellery box details
     */
    public static String getPlayerJewelleryBoxInfo() {
        int tier = getPlayerJewelleryBoxTier();
        String tierName;
        
        switch (tier) {
            case 1:
                tierName = "Basic";
                break;
            case 2:
                tierName = "Fancy";
                break;
            case 3:
                tierName = "Ornate";
                break;
            default:
                return "No jewellery box found";
        }
        
        return String.format("Jewellery Box: %s (Tier %d)", tierName, tier);
    }

   

    /**
     * Checks if the jewellery box interface is open.
     * @return the jewellery box interface widget or null if not open
     */
    public static Widget getJewelleryBoxInterface() {
        return Rs2Widget.getWidget(JEWELLERY_BOX_INTERFACE_ID, 0);
    }
     

    /**
     * Gets the raw varbit value for the jewellery box.
     * The varbit encodes both tier information (first 2 bits) and configuration data.
     * @return the raw varbit value from the jewellery box varbit
     */
    public static int getJewelleryBoxVarbitValue() {
        return Microbot.getVarbitValue(JEWELLERY_BOX_VARBIT);
    }

    /**
     * Gets the jewellery box configuration value with tier bits removed.
     * Shifts the varbit value by BIT_POSITION_OFFSET to remove the tier encoding bits.
     * @return the configuration value without tier bits
     */
    public static int getJewelleryBoxConfigValue() {
        int varbitValue = getJewelleryBoxVarbitValue();
        return varbitValue >> BIT_POSITION_OFFSET; // Remove first 2 bits (tier information)
    }


    /**
     * Waits for the jewellery box interface to be loaded.
     * @param timeoutMs maximum time to wait in milliseconds
     * @return true if interface is loaded within timeout
     */
    public static boolean waitForJewelleryBoxInterface(int timeoutMs) {
        long startTime = System.currentTimeMillis();
        while (System.currentTimeMillis() - startTime < timeoutMs) {
            if (getJewelleryBoxInterface() != null) {
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

    // ===============================
    // BIT POSITION MAPPING METHODS FOR PoHTeleports
    // ===============================

    /**
     * Gets the PoHTeleports enum for a specific jewellery box bit position.
     * @param bitPosition the bit position in the jewellery box varbit (0-29)
     * @return the PoHTeleports enum corresponding to that bit position, or null if not found
     */
    public static PoHTeleport fromJewelleryBitPosition(int bitPosition) {
        if (bitPosition < 0 || bitPosition > 29) {
            return null;
        }
        
        // Get all jewellery box teleports and find the one matching this bit position
        PoHTeleport[] jewelleryTeleports = PoHTeleport.getJewelleryBoxTeleports();
        
        // Map bit positions to teleport enum values:
        // Games Necklace: bits 0-4 (5 teleports)
        // Ring of Dueling: bits 5-8 (4 teleports) 
        // Combat Bracelet: bits 9-12 (4 teleports)
        // Skills Necklace: bits 13-18 (6 teleports)
        // Amulet of Glory: bits 19-22 (4 teleports)
        // Ring of Wealth: bits 23-26 (4 teleports)
        
        if (bitPosition >= 0 && bitPosition < jewelleryTeleports.length) {
            return jewelleryTeleports[bitPosition];
        }
        
        return null;
    }

    /**
     * Gets the bit position for a specific PoHTeleports enum value.
     * @param teleport the teleport enum
     * @return the bit position (0-29), or -1 if not a Jewellery Box teleport
     */
    public static int getBitPositionForTeleport(PoHTeleport teleport) {
        if (teleport == null || !teleport.isJewelleryBoxTeleport()) {
            return -1; // Not a jewellery box teleport
        }
        return PoHTeleport.getJewelleryBitPosition(teleport);
    }

    /**
     * Checks if a specific jewellery teleport is available based on the jewellery box varbit.
     * @param teleport the teleport to check
     * @return true if the teleport is available
     */
    public static boolean isTeleportAvailable(PoHTeleport teleport) {
        if (teleport == null) {
            return false; // Null teleport check
        }
        if (!teleport.isJewelleryBoxTeleport()) {
            return false;
        }
        
        int bitPosition = getBitPositionForTeleport(teleport);
        if (bitPosition == -1) {
            return false;
        }
        
        int varbitValue = Microbot.getVarbitValue(JEWELLERY_BOX_VARBIT);
        return (varbitValue & (1 << bitPosition)) != 0;
    }

    /**
     * Gets all currently available jewellery box teleports.
     * @return list of available PoHTeleports
     */
    public static List<PoHTeleport> getAvailableJewelleryTeleports() {
        List<PoHTeleport> availableTeleports = new ArrayList<>();
        
        int varbitValue = Microbot.getVarbitValue(JEWELLERY_BOX_VARBIT);
        PoHTeleport[] jewelleryTeleports = PoHTeleport.getJewelleryBoxTeleports();
        
        for (int i = 0; i < jewelleryTeleports.length && i < 30; i++) {
            if ((varbitValue & (1 << i)) != 0) {
                availableTeleports.add(jewelleryTeleports[i]);
            }
        }
        
        return availableTeleports;
    }

    /**
     * Gets teleports by jewellery type that are available in the current jewellery box.
     * @param jewelryType the jewellery type name (e.g., "Games Necklace", "Ring of Dueling")
     * @return list of available teleports for that jewellery type
     */
    public static List<PoHTeleport> getAvailableTeleportsByType(String jewelryType) {
        List<PoHTeleport> availableTeleports = getAvailableJewelleryTeleports();
        return availableTeleports.stream()
                .filter(teleport -> teleport.getTooltip().equals(jewelryType))
                .collect(Collectors.toList());
    }

    /**
     * Teleports using the jewellery box with the new PoHTeleports enum.
     * @param teleport the destination to teleport to
     * @return true if teleport was successful
     */
    public static boolean useJewelleryBox(PoHTeleport teleport) {
        if (teleport == null) {
            Microbot.log("Teleport is null.");
            return false;
        }
        if (!teleport.isJewelleryBoxTeleport()) {
            Microbot.log("Not a jewellery box teleport: " + teleport.getDestination());
            return false;
        }

        if (!Rs2PoH.checkIsInHouse()) {
            Microbot.log("Not in a Player-Owned House.");
            return false;
        }

        if (!isTeleportAvailable(teleport)) {
            Microbot.log("Teleport not available in jewellery box: " + teleport.getDestination());
            return false;
        }

        if (getJewelleryBoxInterface() == null) {
            TileObject tileObject = getJewelleryBox();
            if (tileObject == null) {
                Microbot.log("Jewellery box not found in achievement gallery.");
                return false;
            }
            Rs2GameObject.interact(tileObject, "Teleport Menu");
        }

        sleepUntil(() -> getJewelleryBoxInterface() != null);

        return interactWithJewelleryBoxWidget(teleport);
    }

    /**
     * Interact with the jewellery box widget using PoHTeleports.
     * @param teleport the destination to teleport to
     * @return true if interaction was successful
     */
    public static boolean interactWithJewelleryBoxWidget(PoHTeleport teleport) {
        if (teleport == null) {
            Microbot.log("Teleport is null.");
            return false;
        }
        if (!teleport.isJewelleryBoxTeleport()) {
            Microbot.log("Not a jewellery box teleport: " + teleport.getDestination());
            return false;
        }
        Widget mainWidget = getJewelleryBoxInterface();

        if (mainWidget == null) {
            return false;
        }

        Widget widget = Rs2Widget.findWidget(teleport.getDestination().toLowerCase(), 
                Arrays.stream(mainWidget.getStaticChildren()).collect(Collectors.toList()));

        if (widget == null) {
            Microbot.log("Teleport option not found: " + teleport.getDestination());
            return false;
        }

        boolean isTeleportDisabled = widget.getText().contains("<str>");

        if (isTeleportDisabled) {
            Microbot.log(teleport.getDestination() + " teleport is not unlocked.");
            return false;
        }

        if (!Rs2Widget.clickWidget(widget)) {
            return false;
        }

        Rs2Player.waitForAnimation();

        return true;
    }

    /**
     * Gets enhanced information about the current jewellery box setup.
     * @return formatted string with jewellery box details including tier and available teleports
     */
    public static String getPlayerJewelleryBoxInfoDetailed() {
        int tier = getPlayerJewelleryBoxTier();
        
        if (tier == 0) {
            return "No jewellery box found";
        }
        
        String tierName;
        switch (tier) {
            case 1:
                tierName = "Basic";
                break;
            case 2:
                tierName = "Fancy";
                break;
            case 3:
                tierName = "Ornate";
                break;
            default:
                tierName = "Unknown";
        }
        
        StringBuilder info = new StringBuilder();
        info.append(String.format("Jewellery Box: %s (Tier %d)", tierName, tier));
        
        // Get varbit values for additional details
        int rawVarbit = getJewelleryBoxVarbitValue();
        int configValue = getJewelleryBoxConfigValue();
        
        if (rawVarbit > 0) {
            info.append(String.format("\n\t[Raw Varbit: %d, Config: %d]", rawVarbit, configValue));
        }
        
        // Count available teleports based on tier
        List<PoHTeleport> availableTeleports = new ArrayList<>();
        for (PoHTeleport teleport : PoHTeleport.values()) {
            if (isTeleportAvailable(teleport)) {
                availableTeleports.add(teleport);
            }
        }
        
        if (!availableTeleports.isEmpty()) {
            info.append(String.format("\n\t%d teleport%s available:", 
                    availableTeleports.size(), availableTeleports.size() == 1 ? "" : "s"));
            
            // Add sample of available teleports (first 3)
            int sampleSize = Math.min(3, availableTeleports.size());
            for (int i = 0; i < sampleSize; i++) {
                info.append(String.format("\n\t\t[%s]", availableTeleports.get(i).getDestination()));
            }
            
            if (availableTeleports.size() > 3) {
                info.append(String.format("\n\t\t[+%d more teleports]", availableTeleports.size() - 3));
            }
        }
        
        return info.toString();
    }

    // ===============================
    // NAVIGATION CONVENIENCE METHODS
    // ===============================

    /**
     * Navigates to the Jewellery Box in the POH Achievement Gallery.
     * 
     * @return true if successfully navigated to the Jewellery Box
     */
    public static boolean navigateToJewelleryBox() {
        GameObject jewelleryBox = getJewelleryBox();
        if (jewelleryBox == null) {
            Microbot.log("Jewellery Box not found in Achievement Gallery");
            return false;
        }
        
        // Check if already near the jewellery box
        if (Rs2Player.getWorldLocation().distanceTo(jewelleryBox.getWorldLocation()) <= 4) {
            Microbot.log("Already at Jewellery Box");
            return true;
        }
        
        // Use navigation to reach the jewellery box
        if (Rs2PoHNavigation.navigateToObject(jewelleryBox.getId())) {
            Microbot.log("Successfully navigated to Jewellery Box");
            return true;
        }
        
        Microbot.log("Jewellery Box not reachable via navigation");
        return false;
    }
    
    /**
     * Navigates to the Jewellery Box and interacts with it to open teleport menu.
     * 
     * @return true if successfully opened Jewellery Box teleport menu
     */
    public static boolean openJewelleryBoxMenu() {
        if (!navigateToJewelleryBox()) {
            return false;
        }
        
        GameObject jewelleryBox = getJewelleryBox();
        if (jewelleryBox == null) {
            Microbot.log("Jewellery Box not found after navigation");
            return false;
        }
        
        if (Rs2GameObject.interact(jewelleryBox, "Teleport Menu")) {
            Microbot.log("Jewellery Box teleport menu opened");
            return true;
        }
        
        Microbot.log("Failed to open Jewellery Box teleport menu");
        return false;
    }
    
    /**
     * Enhanced navigation to Jewellery Box using optimal pathfinding.
     * Uses door state detection and multi-room path calculation.
     * 
     * @return true if successfully navigated to the Jewellery Box
     */
    public static boolean navigateToJewelleryBoxOptimal() {
        GameObject jewelleryBox = getJewelleryBox();
        if (jewelleryBox == null) {
            Microbot.log("Jewellery Box not found in Achievement Gallery");
            return false;
        }
        
        if (Rs2PoHNavigation.navigateToObjectOptimal(jewelleryBox.getId())) {
            Microbot.log("Successfully navigated to Jewellery Box using optimal path");
            return true;
        }
        
        Microbot.log("Jewellery Box not found or unreachable via optimal path");
        return false;
    }

}
