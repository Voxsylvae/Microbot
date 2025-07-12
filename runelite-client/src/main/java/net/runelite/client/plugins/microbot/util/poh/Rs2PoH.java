package net.runelite.client.plugins.microbot.util.poh;

import com.google.common.collect.ImmutableList;

import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.util.gameobject.Rs2GameObject;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;
import net.runelite.client.plugins.microbot.util.poh.data.PoHTeleport;
import net.runelite.client.plugins.microbot.util.poh.data.Rs2PoHPortal;
import net.runelite.client.plugins.microbot.util.widget.Rs2Widget;
import net.runelite.client.plugins.microbot.util.walker.Rs2Walker;
import net.runelite.api.widgets.Widget;
import net.runelite.api.GameObject;
import net.runelite.api.TileObject;
import net.runelite.api.coords.WorldPoint;
import net.runelite. api.gameval.ObjectID;

import javax.swing.JOptionPane;

import static net.runelite.client.plugins.microbot.util.Global.sleepUntilTrue;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * General utility class for Player-Owned House (POH) functionality.
 * Provides common methods for house detection and navigation.
 */
public class Rs2PoH {
    private final static Integer HOUSE_REGION_ID = 7769; // Region ID for Player-Owned House
    private final static ImmutableList<Integer> HOUSE_PORTAL_OBJECT_IDS =  ImmutableList.of(ObjectID.POH_EXIT_PORTAL);
    
    @SuppressWarnings("unused")
    private final static Integer POH_BOARD_WIDGET_ID = 3407875; // Widget ID of the POH board
    @SuppressWarnings("unused")
    private final static Integer POH_BOARD_WIDGET_SCROLLING_ID = 3407879; // Widget ID of the POH board scrolling part
    // all number of the child widgets of the following widget depends how many advertises ther home -> so we have dynamic childs > Widget[] dynamicChildren = (corspondingWidget).getDynamicChildren();
    @SuppressWarnings("unused")
    private final static Integer POH_BOARD_WIDGET_SCROLLING_NAME_ID = 3407881; // Widget ID dynamic child widgets contains the player names (number of player depands on how many advertise there house at the moment)
    @SuppressWarnings("unused")
    private final static Integer POH_BOARD_WIDGET_SCROLLING_PLAYER_CONSTRCUTION_LEVEL_ID = 3407884;  // Widget ID dynamic child widgets contains the player construction level 
    @SuppressWarnings("unused")
    private final static Integer POH_BOARD_WIDGET_SCROLLING_GILDED_ALTAR_ID = 3407885; // Widget ID dynamic child widgets contains if the player has an prayer gilded altar in their house (text "Y" means yes  or "N" means no) // -> can be used for prayer training
    @SuppressWarnings("unused")
    private final static Integer POH_BOARD_WIDGET_SCROLLING_NEXUS = 3407886; // Widget ID dynamic child widget contains if the player has a portal nexus in their house ( text number tier of the nexus, e.g. "1" means tier 1, "2" means tier 2, etc., 0 means no nexus (max 4)) 
    @SuppressWarnings("unused")
    private final static Integer POH_BOARD_WIDGET_SCROLLING_JEWELLERY_BOX = 3407887;  // Widget ID dynamic child widget contains if the player has a jewellery box in their house ( text number tier of the jewellery box, e.g. "1" means tier 1, "2" means tier 2, etc., 0 means no jewellery box (max 3))
    @SuppressWarnings("unused")
    private final static Integer POH_BOARD_WIDGET_SCROLLING_POOL_ID = 3407888; // Widget ID dynamic child widget contains if the player has a pool in their house ( text number tier of the pool, e.g. "1" means tier 1, "2" means tier 2, etc., 0 means no pool (max 3))
    @SuppressWarnings("unused")
    private final static Integer POH_BOARD_WIDGET_SCROLLING_OCCULT_ALTAR_ID = 3407889; // Widget ID dynamic child widget contains if the player has an occult altar in their house ( text number tier of the occult altar, e.g. "1" means tier 1, "2" means tier 2, etc., 0 means no occult altar (max 4)) ->  can be used for spellbook switching
    @SuppressWarnings("unused")
    private final static Integer POH_BOARD_WIDGET_SCROLLING_AMOUR_STAND_ID = 3407890; // Widget ID dynamic child widget contains if the player has an amour stand in their house ( text "Y" means yes  or "N" means no) -> can be used for repairing armour
    @SuppressWarnings("unused")
    private final static Integer POH_BOARD_WIDGET_SCROLLING_BUTTONS_ENTER_HOUSE_ID = 3407891; // Widget ID for the POH board scrolling, button to click to enter the house of the player

    

    private final static ImmutableList<Integer> POH_BOARD_HOUSE_ADVERTISEMENT_OBJECT_IDS = ImmutableList.of(
            ObjectID.POH_BOARD_RIMMINGTON, // Rimmington POH board
            ObjectID.POH_BOARD_TAVERLY,
            ObjectID.POH_BOARD_POLLNIVNEACH,
            ObjectID.POH_BOARD_KOUREND,
            ObjectID.POH_BOARD_RELLEKKA,
            ObjectID.POH_BOARD_BRIMHAVEN,
            ObjectID.POH_BOARD_YANILLE,
            ObjectID.POH_BOARD_PRIFDDINAS,
            ObjectID.POH_BOARD_ALDARIN
    );
    
    /**
     * Checks if the player is in their house
     * based on the purple portal and if the player is
     * in an instance
     * @return true if player is in their POH
     */
    public static boolean isInHouse() {
        return Rs2Player.IsInInstance() && Rs2GameObject.getTileObject((tileObject) -> HOUSE_PORTAL_OBJECT_IDS.contains(tileObject.getId())) != null && Rs2Player.getWorldLocation() != null && Rs2Player.getWorldLocation().getRegionID() == HOUSE_REGION_ID; 
    }

    /**
     * Checks if a player is in their house
     * sends a microbot log if the player is not in their house
     * @return true if player is in their POH, false otherwise with log message
     */
    public static boolean checkIsInHouse() {
        if (!isInHouse()) {
            Microbot.log("You do not seem to be in a POH.");
            return false;
        }
        return true;
    }

    /**
     * Gets the purple portal object for leaving the house.
     * @return the portal TileObject or null if not found
     */
    public static net.runelite.api.TileObject getExitPortal() {
        return Rs2GameObject.getTileObject(4525);
    }

    /**
     * Exits the house via the purple portal.
     * @return true if successfully clicked the exit portal
     */
    public static boolean exitHouse() {
        if (!checkIsInHouse()) {
            return false;
        }

        net.runelite.api.TileObject portal = getExitPortal();
        if (portal == null) {
            Microbot.log("Exit portal not found.");
            return false;
        }

        return Rs2GameObject.interact(portal, "Enter");
    }

    /**
     * Enters the player's own house through the POH portal.
     * First walks to the player's house portal location, then interacts with it.
     * @return true if successfully entered the house
     */
    public static boolean enterHouse() {
        if (isInHouse()) {
            Microbot.log("Already in house.");
            return true;
        }

        // Get the player's house portal location
        Optional<Rs2PoHPortal> playerPortal = Rs2PoHPortal.getPlayerHousePortal();
        if (playerPortal.isEmpty() || playerPortal.get() == Rs2PoHPortal.UNKNOWN) {
            Microbot.log("Player house portal location is unknown. Make sure you have a house set up.");
            return false;
        }

        WorldPoint portalLocation = playerPortal.get().getWorldPoint();
        if (portalLocation == null) {
            Microbot.log("Portal location not available for " + playerPortal.get().getName());
            return false;
        }

        // Walk to the portal location if not already there
        if (Rs2Player.getWorldLocation().distanceTo(portalLocation) > 10) {
            Microbot.log("Walking to house portal at " + playerPortal.get().getName());
            if (!Rs2Walker.walkTo(portalLocation)) {
                Microbot.log("Failed to walk to house portal location.");
                return false;
            }
        }

        // Find and interact with the house portal
        TileObject housePortal = Rs2GameObject.getTileObject(portalLocation);
        if (housePortal == null) {
            Microbot.log("House portal not found at location: " + portalLocation);
            return false;
        }

        // Interact with the portal to enter
        if (!Rs2GameObject.interact(housePortal, "Enter")) {
            Microbot.log("Failed to interact with house portal.");
            return false;
        }

        // Wait for house to load
        if (waitForHouseLoad(10000)) {
            Microbot.log("Successfully entered house.");
            return true;
        } else {
            Microbot.log("Timeout waiting for house to load.");
            return false;
        }
    }

    /**
     * Waits for the player to be in their house.
     * @param timeoutMs maximum time to wait in milliseconds
     * @return true if player is in house within timeout
     */
    public static boolean waitForHouseLoad(int timeoutMs) {
        long startTime = System.currentTimeMillis();
        while (System.currentTimeMillis() - startTime < timeoutMs) {
            if (isInHouse()) {
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
     * Checks if a specific PoH teleport is available for the player.
     * This method determines the teleport type and delegates to the appropriate utility class.
     * 
     * @param pohTeleport the PoH teleport to check availability for
     * @return true if the teleport is available/unlocked for the player
     */
    public static boolean isTeleportAvailable(PoHTeleport pohTeleport) {
        if (pohTeleport == null) {
            return false;
        }
        
        // Determine the type of PoH teleport and delegate to the appropriate utility class
        if (pohTeleport.isPortalTeleport()) {
            return Rs2PortalNexus.isTeleportAvailableForPlayer(pohTeleport) || Rs2PortalChamber.isTeleportAvailable(pohTeleport);
        } else if (pohTeleport.isJewelleryBoxTeleport()) {
            return Rs2AchievementGallery.isTeleportAvailable(pohTeleport);
        }         
        return false; // Default case if no matching teleport type found
    }

    // ========== POH BOARD INTERACTION METHODS ==========

    /**
     * Represents a player's house advertisement from the POH board
     */
    public static class HouseAdvertisement {
        private final String playerName;
        private final int constructionLevel;
        private final boolean hasGildedAltar;
        private final int nexusTier;
        private final int jewelleryBoxTier;
        private final int poolTier;
        private final int occultAltarTier;
        private final boolean hasArmourStand;
        private final Widget enterButton;

        public HouseAdvertisement(String playerName, int constructionLevel, boolean hasGildedAltar,
                                int nexusTier, int jewelleryBoxTier, int poolTier, int occultAltarTier,
                                boolean hasArmourStand, Widget enterButton) {
            this.playerName = playerName;
            this.constructionLevel = constructionLevel;
            this.hasGildedAltar = hasGildedAltar;
            this.nexusTier = nexusTier;
            this.jewelleryBoxTier = jewelleryBoxTier;
            this.poolTier = poolTier;
            this.occultAltarTier = occultAltarTier;
            this.hasArmourStand = hasArmourStand;
            this.enterButton = enterButton;
        }

        // Getters
        public String getPlayerName() { return playerName; }
        public int getConstructionLevel() { return constructionLevel; }
        public boolean hasGildedAltar() { return hasGildedAltar; }
        public int getNexusTier() { return nexusTier; }
        public int getJewelleryBoxTier() { return jewelleryBoxTier; }
        public int getPoolTier() { return poolTier; }
        public int getOccultAltarTier() { return occultAltarTier; }
        public boolean hasArmourStand() { return hasArmourStand; }
        public Widget getEnterButton() { return enterButton; }

        /**
         * Checks if this house has maximum features (max tiers for all components)
         * Max values: nexus=4, jewellery box=3, pool=3, occult altar=4, plus gilded altar and armor stand
         */
        public boolean hasMaxFeatures() {
            return nexusTier >= 3 && jewelleryBoxTier == 3 && 
                   poolTier == 5 && occultAltarTier >= 4 && hasArmourStand;
        }
        /**
         * Checks if this house has maximum teleport features 
         * Max values: nexus=4, jewellery box=3, 
         */
        public boolean hasAllTeleportFeature() {
            return nexusTier >= 3 && jewelleryBoxTier == 3 && constructionLevel >= 85;
        }

        /**
         * Calculates a score for this house based on feature completeness
         * Higher score means better house
         */
        public int getHouseScore() {
            int score = 0;
            if (hasGildedAltar) score += 10;
            if (hasArmourStand) score += 10;
            score += nexusTier * 5;        // 0-20 points
            score += jewelleryBoxTier * 5; // 0-15 points
            score += poolTier * 5;         // 0-15 points
            score += occultAltarTier * 5;  // 0-20 points
            score += Math.min(constructionLevel, 99); // 1-99 points
            return score;
        }
    }

    /**
     * Checks if the POH board is open and visible
     */
    public static boolean isPohBoardOpen() {
        
        return Rs2Widget.getWidget(POH_BOARD_WIDGET_ID) != null && 
               !Rs2Widget.isHidden(POH_BOARD_WIDGET_ID);
    }

    /**
     * Gets all house advertisements from the POH board
     * @return List of HouseAdvertisement objects representing all advertised houses
     */
    public static List<HouseAdvertisement> getAllHouseAdvertisements() {
        List<HouseAdvertisement> advertisements = new ArrayList<>();
        
        if (!isPohBoardOpen()) {
            Microbot.log("POH board is not open");
            return advertisements;
        }

        try {
            // Get dynamic children for each widget type using the specific widget IDs
            Widget[] playerNames = getDynamicChildren(POH_BOARD_WIDGET_SCROLLING_NAME_ID);
            Widget[] constructionLevels = getDynamicChildren(POH_BOARD_WIDGET_SCROLLING_PLAYER_CONSTRCUTION_LEVEL_ID);
            Widget[] gildedAltars = getDynamicChildren(POH_BOARD_WIDGET_SCROLLING_GILDED_ALTAR_ID);
            Widget[] nexusTiers = getDynamicChildren(POH_BOARD_WIDGET_SCROLLING_NEXUS);
            Widget[] jewelleryBoxTiers = getDynamicChildren(POH_BOARD_WIDGET_SCROLLING_JEWELLERY_BOX);
            Widget[] poolTiers = getDynamicChildren(POH_BOARD_WIDGET_SCROLLING_POOL_ID);
            Widget[] occultAltarTiers = getDynamicChildren(POH_BOARD_WIDGET_SCROLLING_OCCULT_ALTAR_ID);
            Widget[] armourStands = getDynamicChildren(POH_BOARD_WIDGET_SCROLLING_AMOUR_STAND_ID);
            Widget[] enterButtons = getDynamicChildren(POH_BOARD_WIDGET_SCROLLING_BUTTONS_ENTER_HOUSE_ID);

            // Validate that all widget arrays exist and have the same length
            if (!validateWidgetArrays(playerNames, constructionLevels, gildedAltars, nexusTiers, 
                                    jewelleryBoxTiers, poolTiers, occultAltarTiers, armourStands, enterButtons)) {
                Microbot.log("Widget arrays validation failed - inconsistent data");
                return advertisements;
            }

            int numberOfDynamicWidgets = playerNames.length;
            
            
            for (int i = 0; i < numberOfDynamicWidgets; i++) {
                try {
                    HouseAdvertisement advertisement = parseHouseAdvertisementFromWidgets(
                        playerNames[i], constructionLevels[i], gildedAltars[i], nexusTiers[i],
                        jewelleryBoxTiers[i], poolTiers[i], occultAltarTiers[i], armourStands[i], enterButtons[i]
                    );
                    if (advertisement != null) {
                        advertisements.add(advertisement);
                    }
                } catch (Exception e) {
                    Microbot.log("Error parsing house advertisement at index " + i + ": " + e.getMessage());
                }
            }
            Microbot.log("Found " + advertisements.size() + " house advertisements on POH board");
        } catch (Exception e) {
            Microbot.log("Error retrieving house advertisements: " + e.getMessage());
        }

        return advertisements;
    }

    /**
     * Gets dynamic children from a specific widget ID
     * @param widgetId The widget ID to get dynamic children from
     * @return Array of dynamic children widgets, or empty array if none found
     */
    private static Widget[] getDynamicChildren(int widgetId) {
        try {
            Widget widget = Rs2Widget.getWidget(widgetId );
            if (widget != null && widget.getDynamicChildren() != null) {
                return widget.getDynamicChildren();
            }
        } catch (Exception e) {
            Microbot.log("Error getting dynamic children for widget " + widgetId + ": " + e.getMessage());
        }
        return new Widget[0];
    }

    /**
     * Validates that all widget arrays are not null and have the same length
     */
    private static boolean validateWidgetArrays(Widget[]... widgetArrays) {
        if (widgetArrays.length == 0) return false;
        
        int expectedLength = widgetArrays[0].length;
        if (expectedLength == 0) return false;
        
        for (Widget[] array : widgetArrays) {
            if (array == null || array.length != expectedLength) {
                return false;
            }
        }
        return true;
    }

    /**
     * Parses a single house advertisement from individual widgets
     */
    private static HouseAdvertisement parseHouseAdvertisementFromWidgets(
            Widget nameWidget, Widget constructionWidget, Widget gildedAltarWidget, Widget nexusWidget,
            Widget jewelleryBoxWidget, Widget poolWidget, Widget occultAltarWidget, 
            Widget armourStandWidget, Widget enterButtonWidget) {
        try {
            // Extract widget data
            String playerName = getWidgetText(nameWidget);
            int constructionLevel = parseIntSafely(getWidgetText(constructionWidget));
            boolean hasGildedAltar = "Y".equals(getWidgetText(gildedAltarWidget));
            int nexusTier = parseIntSafely(getWidgetText(nexusWidget));
            int jewelleryBoxTier = parseIntSafely(getWidgetText(jewelleryBoxWidget));
            int poolTier = parseIntSafely(getWidgetText(poolWidget));
            int occultAltarTier = parseIntSafely(getWidgetText(occultAltarWidget));
            boolean hasArmourStand = "Y".equals(getWidgetText(armourStandWidget));

            // Validate essential data
            if (playerName == null || playerName.trim().isEmpty()) {
                return null;
            }

            // Check if enter button is valid and not hidden
            if (enterButtonWidget == null || enterButtonWidget.isHidden()) {
                return null;
            }

            return new HouseAdvertisement(playerName, constructionLevel, hasGildedAltar, 
                                        nexusTier, jewelleryBoxTier, poolTier, occultAltarTier, 
                                        hasArmourStand, enterButtonWidget);
        } catch (Exception e) {
            Microbot.log("Error parsing house advertisement from widgets: " + e.getMessage());
            return null;
        }
    }

    /**
     * Safely extracts text from a widget
     */
    private static String getWidgetText(Widget widget) {
        if (widget == null) return "";
        String text = widget.getText();
        return text != null ? text.trim() : "";
    }

    /**
     * Safely parses integer from text, returns 0 if parsing fails
     */
    private static int parseIntSafely(String text) {
        if (text == null || text.trim().isEmpty()) return 0;
        try {
            return Integer.parseInt(text.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /**
     * Filters house advertisements to only include those with maximum features
     * @return List of HouseAdvertisement objects with maximum features
     */
    public static List<HouseAdvertisement> getMaxFeatureHouses() {
        List<HouseAdvertisement> allHouses = getAllHouseAdvertisements();
        List<HouseAdvertisement> maxFeatureHouses = new ArrayList<>();

        for (HouseAdvertisement house : allHouses) {
            if (house.hasMaxFeatures()) {
                maxFeatureHouses.add(house);
            }
        }

        Microbot.log("Found " + maxFeatureHouses.size() + " houses with maximum features out of " + allHouses.size() + " total houses");
        return maxFeatureHouses;
    }
    /**
     * Gets the best available house from those with maximum features
     * @return The best HouseAdvertisement or null if none available
     */
    public static HouseAdvertisement getBestMaxFeatureHouse() {
        List<HouseAdvertisement> maxFeatureHouses = getMaxFeatureHouses();
        
        if (maxFeatureHouses.isEmpty()) {
            return null;
        }

        // Sort by house score (highest first) and then by construction level
        return maxFeatureHouses.stream()
            .sorted(Comparator.comparingInt(HouseAdvertisement::getHouseScore).reversed()
                .thenComparingInt(HouseAdvertisement::getConstructionLevel).reversed())
            .findFirst()
            .orElse(null);
    }

     /**
     * Filters house advertisements to only include those with maximum features
     * @return List of HouseAdvertisement objects with maximum features
     */
    public static List<HouseAdvertisement> getAllTeleportFeatureHouses() {
        List<HouseAdvertisement> allHouses = getAllHouseAdvertisements();
        List<HouseAdvertisement> allTeleportFeatureHouses = new ArrayList<>();

        for (HouseAdvertisement house : allHouses) {
            if (house.hasAllTeleportFeature()) {
                allTeleportFeatureHouses.add(house);
            }
        }

        Microbot.log("Found " + allTeleportFeatureHouses.size() + " houses with maximum teleport features out of " + allHouses.size() + " total houses");
        return allTeleportFeatureHouses;
    }

    public static HouseAdvertisement getBestAllTeleportHouse() {
        List<HouseAdvertisement> maxFeatureHouses = getAllTeleportFeatureHouses();
        
        if (maxFeatureHouses.isEmpty()) {
            return null;
        }

        // Sort by house score (highest first) and then by construction level
        return maxFeatureHouses.stream()
            .sorted(Comparator.comparingInt(HouseAdvertisement::getHouseScore).reversed()
                .thenComparingInt(HouseAdvertisement::getConstructionLevel).reversed())
            .findFirst()
            .orElse(null);
    }

    
    /**
     * Enters the best available house with maximum features
     * Shows a dialog if no houses are available
     * @return true if successfully clicked to enter a house, false otherwise
     */
    public static boolean enterBestMaxFeatureHouse() {
        if (!isPohBoardOpen()) {
            Microbot.log("POH board is not open. Please open the board first.");
            return false;
        }

        HouseAdvertisement bestHouse = getBestMaxFeatureHouse();
        
        if (bestHouse == null) {
            // Show dialog box when no houses are available
            showNoHousesAvailableDialog();
            return false;
        }

        return enterHouse(bestHouse);
    }
    
    public static boolean enterBestTeleportHouse() {
        if (!isPohBoardOpen()) {
            if (!interactWithHouseBoard()){
                Microbot.log("POH board is not open. Please open the board first.");
                return false;
            }
        }

        HouseAdvertisement bestHouse = getBestAllTeleportHouse();
        
        if (bestHouse == null) {
            // Show dialog box when no houses are available
            showNoHousesAvailableDialog();
            return false;
        }

        return enterHouse(bestHouse);
    }
    /**
     * Interacts with the POH board to open it
     * Checks if the board is already open before attempting to interact
     * * This method searches for the POH board object in the game world
     * * @see Rs2GameObject#getGameObject(java.util.function.Predicate)
     * @return true if successfully opened the POH board widget, false otherwise
     */
    public static boolean interactWithHouseBoard() {
        // Check if the POH board is open
        if (isPohBoardOpen()) {
            Microbot.log("POH board is already open.");
            return true;
        }
        GameObject pohBoard = Rs2GameObject.getGameObject((gameObject) -> 
                POH_BOARD_HOUSE_ADVERTISEMENT_OBJECT_IDS.contains(gameObject.getId())&&
                gameObject.getWorldLocation() != null && Rs2Player.getWorldLocation() != null &&
                gameObject.getWorldLocation().distanceTo(Rs2Player.getWorldLocation()) < 10            
            );
        if (pohBoard == null) {
            Microbot.log("POH board not found.");
            return false;
        }
        // Interact with the POH board
        if (Rs2GameObject.interact(pohBoard, "View")) {
            Microbot.log("Interacting with POH board: " + pohBoard.getId());
            // Wait for the POH board to open
            return waitForPohBoardOpen(5000); // Wait up t1 5 seconds for the board to open
        } else {
            Microbot.log("Failed to interact with POH board: " + pohBoard.getId());
            return false;
        }

    }
        /**
        * Waits for the POH board to open
        * @param timeoutMs maximum time to wait in milliseconds
        * @return true if the POH board is open within the timeout
     */
    public static boolean waitForPohBoardOpen(int timeoutMs) {
        return sleepUntilTrue(() -> (isPohBoardOpen() && !Rs2Player.isMoving() && !Rs2Player.isAnimating()), 100, timeoutMs);
    }

    /**
     * Enters a specific house by clicking its enter button
     * @param house The HouseAdvertisement to enter
     * @return true if successfully clicked the enter button
     */
    public static boolean enterHouse(HouseAdvertisement house) {
        if (house == null || house.getEnterButton() == null) {
            Microbot.log("Invalid house advertisement or enter button");
            return false;
        }

        Widget enterButton = house.getEnterButton();

        if (enterButton.isHidden()) {
            Microbot.log("Enter button is hidden for player: " + house.getPlayerName());
            return false;
        }

        try {
            Microbot.log("Entering house of player: " + house.getPlayerName() + 
                        " (Construction: " + house.getConstructionLevel() + 
                        ", Score: " + house.getHouseScore() + ")");
            Widget parentWidgetScrolling =Rs2Widget.getWidget(POH_BOARD_WIDGET_SCROLLING_ID);
            if (parentWidgetScrolling == null) {
                Microbot.log("Parent widget for enter button is null");
                return false;
            }
            //Rs2PortalNexus.interactWithWidgetViaScript(enterButton);
            //Rs2Widget.clickWidget(enterButton);
            Rs2Widget.clickWidgetWithScrolling(enterButton,
            parentWidgetScrolling.getBounds(), parentWidgetScrolling,10);
            return true;
        } catch (Exception e) {
            Microbot.log("Error clicking enter button for house: " + e.getMessage());
            return false;
        }
    }

    /**
     * Shows a dialog box when no houses with maximum features are available
     */
    private static void showNoHousesAvailableDialog() {
        try {
            String message = "No houses with maximum features are currently available.\n\n" +
                           "Maximum features required:\n" +
                           "• Gilded Altar: Yes\n" +
                           "• Portal Nexus: Tier 4\n" +
                           "• Jewellery Box: Tier 3\n" +
                           "• Pool: Tier 3\n" +
                           "• Occult Altar: Tier 4\n" +
                           "• Armour Stand: Yes\n\n" +
                           "Please wait for suitable houses to become available.";
            
            String title = "No Maximum Feature Houses Available";
            
            JOptionPane.showMessageDialog(null, message, title, JOptionPane.INFORMATION_MESSAGE);
            Microbot.log("No houses with maximum features available - dialog shown to user");
        } catch (Exception e) {
            Microbot.log("Error showing no houses dialog: " + e.getMessage());
            Microbot.log("No houses with maximum features are currently available");
        }
    }

    /**
     * Gets detailed information about all available houses for debugging
     * @return String containing detailed house information
     */
    public static String getHouseAdvertisementsInfo() {
        List<HouseAdvertisement> houses = getAllHouseAdvertisements();
        if (houses.isEmpty()) {
            return "No house advertisements found";
        }

        StringBuilder info = new StringBuilder();
        info.append("=== POH Board House Advertisements ===\n");
        info.append("Total houses found: ").append(houses.size()).append("\n\n");

        for (int i = 0; i < houses.size(); i++) {
            HouseAdvertisement house = houses.get(i);
            info.append("House ").append(i + 1).append(":\n");
            info.append("  Player: ").append(house.getPlayerName()).append("\n");
            info.append("  Construction Level: ").append(house.getConstructionLevel()).append("\n");
            info.append("  Gilded Altar: ").append(house.hasGildedAltar() ? "Yes" : "No").append("\n");
            info.append("  Portal Nexus: Tier ").append(house.getNexusTier()).append("\n");
            info.append("  Jewellery Box: Tier ").append(house.getJewelleryBoxTier()).append("\n");
            info.append("  Pool: Tier ").append(house.getPoolTier()).append("\n");
            info.append("  Occult Altar: Tier ").append(house.getOccultAltarTier()).append("\n");
            info.append("  Armour Stand: ").append(house.hasArmourStand() ? "Yes" : "No").append("\n");
            info.append("  Max Features: ").append(house.hasMaxFeatures() ? "Yes" : "No").append("\n");
            info.append("  All Teleport Features: ").append(house.hasAllTeleportFeature() ? "Yes" : "No").append("\n");
            info.append("  House Score: ").append(house.getHouseScore()).append("\n");
            info.append("  Enter Button Available: ").append(house.getEnterButton() != null && !house.getEnterButton().isHidden() ? "Yes" : "No").append("\n");
            info.append("\n");
        }

        return info.toString();
    }

    /**
     * Debug method to show detailed widget information using the specific widget IDs
     * @return String containing detailed widget debug information
     */
    public static String getDetailedWidgetInfo() {
        if (!isPohBoardOpen()) {
            return "POH board is not open";
        }

        StringBuilder info = new StringBuilder();
        info.append("=== POH Board Widget Debug Information ===\n");

        try {
            // Get dynamic children for each widget type
            Widget[] playerNames = getDynamicChildren(POH_BOARD_WIDGET_SCROLLING_NAME_ID);
            Widget[] constructionLevels = getDynamicChildren(POH_BOARD_WIDGET_SCROLLING_PLAYER_CONSTRCUTION_LEVEL_ID);
            Widget[] gildedAltars = getDynamicChildren(POH_BOARD_WIDGET_SCROLLING_GILDED_ALTAR_ID);
            Widget[] nexusTiers = getDynamicChildren(POH_BOARD_WIDGET_SCROLLING_NEXUS);
            Widget[] jewelleryBoxTiers = getDynamicChildren(POH_BOARD_WIDGET_SCROLLING_JEWELLERY_BOX);
            Widget[] poolTiers = getDynamicChildren(POH_BOARD_WIDGET_SCROLLING_POOL_ID);
            Widget[] occultAltarTiers = getDynamicChildren(POH_BOARD_WIDGET_SCROLLING_OCCULT_ALTAR_ID);
            Widget[] armourStands = getDynamicChildren(POH_BOARD_WIDGET_SCROLLING_AMOUR_STAND_ID);
            Widget[] enterButtons = getDynamicChildren(POH_BOARD_WIDGET_SCROLLING_BUTTONS_ENTER_HOUSE_ID);

            info.append("Widget Array Lengths:\n");
            info.append("  Player Names: ").append(playerNames.length).append("\n");
            info.append("  Construction Levels: ").append(constructionLevels.length).append("\n");
            info.append("  Gilded Altars: ").append(gildedAltars.length).append("\n");
            info.append("  Nexus Tiers: ").append(nexusTiers.length).append("\n");
            info.append("  Jewellery Box Tiers: ").append(jewelleryBoxTiers.length).append("\n");
            info.append("  Pool Tiers: ").append(poolTiers.length).append("\n");
            info.append("  Occult Altar Tiers: ").append(occultAltarTiers.length).append("\n");
            info.append("  Armour Stands: ").append(armourStands.length).append("\n");
            info.append("  Enter Buttons: ").append(enterButtons.length).append("\n\n");

            boolean isValid = validateWidgetArrays(playerNames, constructionLevels, gildedAltars, nexusTiers, 
                                                 jewelleryBoxTiers, poolTiers, occultAltarTiers, armourStands, enterButtons);
            info.append("Widget Arrays Valid: ").append(isValid).append("\n\n");

            if (isValid && playerNames.length > 0) {
                info.append("Sample Data (first 3 entries):\n");
                int sampleCount = Math.min(3, playerNames.length);
                for (int i = 0; i < sampleCount; i++) {
                    info.append("  Entry ").append(i + 1).append(":\n");
                    info.append("    Name: '").append(getWidgetText(playerNames[i])).append("'\n");
                    info.append("    Construction: '").append(getWidgetText(constructionLevels[i])).append("'\n");
                    info.append("    Gilded Altar: '").append(getWidgetText(gildedAltars[i])).append("'\n");
                    info.append("    Nexus: '").append(getWidgetText(nexusTiers[i])).append("'\n");
                    info.append("    Jewellery Box: '").append(getWidgetText(jewelleryBoxTiers[i])).append("'\n");
                    info.append("    Pool: '").append(getWidgetText(poolTiers[i])).append("'\n");
                    info.append("    Occult Altar: '").append(getWidgetText(occultAltarTiers[i])).append("'\n");
                    info.append("    Armour Stand: '").append(getWidgetText(armourStands[i])).append("'\n");
                    info.append("    Enter Button Hidden: ").append(enterButtons[i] != null && enterButtons[i].isHidden()).append("\n");
                    info.append("\n");
                }
            }

        } catch (Exception e) {
            info.append("Error getting widget information: ").append(e.getMessage()).append("\n");
        }

        return info.toString();
    }
    
    // ========== WIDGET-BASED CONFIGURATION EXTRACTION METHODS ==========
    
    /**
     * Extracts Portal Nexus configuration from widget data when visiting another player's house.
     * This method analyzes the Portal Nexus interface to determine which teleports are available.
     * 
     * @return Map of slot numbers to their configured PoHTeleports, or empty map if not accessible
     */
    public static Map<Integer, PoHTeleport> extractPortalNexusConfigurationFromWidget() {
        Map<Integer, PoHTeleport> configuration = new HashMap<>();
        
        // Check if Portal Nexus interface is open
        Widget nexusInterface = Rs2Widget.getWidget(Rs2PortalNexus.PORTAL_NEXUS_INTERFACE_ID, 0);
        if (nexusInterface == null) {
            Microbot.log("Portal Nexus interface not open");
            return configuration;
        }
        
        try {
            // Get the scrolling container with teleport options
            Widget scrollingContainer = Rs2Widget.getWidget(Rs2PortalNexus.PORTAL_NEXUS_WIDGET_SCROLLING);
            if (scrollingContainer != null) {
                Widget[] teleportWidgets = scrollingContainer.getDynamicChildren();
                
                int slotIndex = 1;
                for (Widget teleportWidget : teleportWidgets) {
                    if (teleportWidget != null && !teleportWidget.isHidden()) {
                        String teleportText = teleportWidget.getText();
                        if (teleportText != null && !teleportText.isEmpty()) {
                            PoHTeleport teleport = PoHTeleport.getByDisplayName(teleportText);
                            if (teleport != null) {
                                configuration.put(slotIndex, teleport);
                                slotIndex++;
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            Microbot.log("Error extracting Portal Nexus configuration from widget: " + e.getMessage());
        }
        
        return configuration;
    }
    
    /**
     * Extracts Jewellery Box configuration from widget data when visiting another player's house.
     * This method analyzes the Jewellery Box interface to determine which teleports are available.
     * 
     * @return List of available PoHTeleports from the Jewellery Box, or empty list if not accessible
     */
    public static List<PoHTeleport> extractJewelleryBoxConfigurationFromWidget() {
        List<PoHTeleport> availableTeleports = new ArrayList<>();
        
        // Check if Jewellery Box interface is open
        Widget jewelleryInterface = Rs2Widget.getWidget(Rs2AchievementGallery.JEWELLERY_BOX_INTERFACE_ID, 0);
        if (jewelleryInterface == null) {
            Microbot.log("Jewellery Box interface not open");
            return availableTeleports;
        }
        
        try {
            // Analyze the interface structure to find teleport options
            Widget[] childWidgets = jewelleryInterface.getChildren();
            if (childWidgets != null) {
                for (Widget child : childWidgets) {
                    if (child != null && !child.isHidden()) {
                        String widgetText = child.getText();
                        if (widgetText != null && !widgetText.isEmpty()) {
                            PoHTeleport teleport = PoHTeleport.getJewelleryTeleportByText(widgetText);
                            if (teleport != null) {
                                availableTeleports.add(teleport);
                            }
                        }
                        
                        // Check nested children for teleport options
                        Widget[] nestedChildren = child.getChildren();
                        if (nestedChildren != null) {
                            for (Widget nested : nestedChildren) {
                                if (nested != null && !nested.isHidden()) {
                                    String nestedText = nested.getText();
                                    if (nestedText != null && !nestedText.isEmpty()) {
                                        PoHTeleport nestedTeleport = PoHTeleport.getJewelleryTeleportByText(nestedText);
                                        if (nestedTeleport != null && !availableTeleports.contains(nestedTeleport)) {
                                            availableTeleports.add(nestedTeleport);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            Microbot.log("Error extracting Jewellery Box configuration from widget: " + e.getMessage());
        }
        
        return availableTeleports;
    }
    
    /**
     * Extracts Portal Chamber configuration from widget data when visiting another player's house.
     * This method analyzes visible portal frames to determine which teleports are configured.
     * 
     * @return Map of portal locations to their configured PoHTeleports
     */
    public static Map<String, PoHTeleport> extractPortalChamberConfigurationFromObjects() {
        Map<String, PoHTeleport> configuration = new HashMap<>();
        
        if (!Rs2PortalChamber.isInPortalChamber()) {
            Microbot.log("Not in Portal Chamber");
            return configuration;
        }
        
        try {
            List<net.runelite.api.TileObject> frames = Rs2PortalChamber.getPortalFrames();
            
            int portalIndex = 1;
            for (net.runelite.api.TileObject frame : frames) {
                if (frame != null) {
                    int objectId = frame.getId();
                    PoHTeleport teleport = PoHTeleport.getByPortalFrameObjectId(objectId);
                    if (teleport != null) {
                        configuration.put("portal" + portalIndex, teleport);
                    } else {
                        configuration.put("portal" + portalIndex, null); // Empty portal
                    }
                    portalIndex++;
                }
            }
        } catch (Exception e) {
            Microbot.log("Error extracting Portal Chamber configuration: " + e.getMessage());
        }
        
        return configuration;
    }
    
    /**
     * Extracts Xeric's Talisman configuration from widget data when visiting another player's house.
     * This method analyzes the Xeric's Talisman interface to determine which teleports are available.
     * 
     * @return List of available Xeric PoHTeleports, or empty list if not accessible
     */
    public static List<PoHTeleport> extractXericsTalismanConfigurationFromWidget() {
        List<PoHTeleport> availableTeleports = new ArrayList<>();
        
        // Check if Xeric's Talisman interface is open (uses adventure log container)
        Widget xericInterface = Rs2Widget.getWidget(Rs2PortalNexus.XERIC_TALISMAN_WIDGET_ID);
        if (xericInterface == null) {
            Microbot.log("Xeric's Talisman interface not open");
            return availableTeleports;
        }
        
        try {
            // Get all Xeric teleports and check which are available through the interface
            PoHTeleport[] allXericTeleports = PoHTeleport.getXericsTalismanTeleports();
            
            for (PoHTeleport teleport : allXericTeleports) {
                // Check if this teleport option exists in the widget
                if (isXericTeleportAvailableInWidget(xericInterface, teleport)) {
                    availableTeleports.add(teleport);
                }
            }
        } catch (Exception e) {
            Microbot.log("Error extracting Xeric's Talisman configuration from widget: " + e.getMessage());
        }
        
        return availableTeleports;
    }
    
    /**
     * Extracts Digsite Pendant configuration from widget data when visiting another player's house.
     * This method analyzes the Digsite Pendant interface to determine which teleports are available.
     * 
     * @return List of available Digsite PoHTeleports, or empty list if not accessible  
     */
    public static List<PoHTeleport> extractDigsitePendantConfigurationFromWidget() {
        List<PoHTeleport> availableTeleports = new ArrayList<>();
        
        // Check if Digsite Pendant interface is open (uses adventure log container)
        Widget digsiteInterface = Rs2Widget.getWidget(Rs2PortalNexus.DIGSITE_PENDANT_WIDGET_ID);
        if (digsiteInterface == null) {
            Microbot.log("Digsite Pendant interface not open");
            return availableTeleports;
        }
        
        try {
            // Get all Digsite teleports and check which are available through the interface
            PoHTeleport[] allDigsiteTeleports = PoHTeleport.getDigsitePendantTeleports();
            
            for (PoHTeleport teleport : allDigsiteTeleports) {
                // Check if this teleport option exists in the widget
                if (isDigsiteTeleportAvailableInWidget(digsiteInterface, teleport)) {
                    availableTeleports.add(teleport);
                }
            }
        } catch (Exception e) {
            Microbot.log("Error extracting Digsite Pendant configuration from widget: " + e.getMessage());
        }
        
        return availableTeleports;
    }
    
    /**
     * Helper method to check if a specific Xeric teleport is available in the widget interface.
     * 
     * @param widgetInterface The Xeric's Talisman widget interface
     * @param teleport The teleport to check for
     * @return true if the teleport is available in the interface
     */
    private static boolean isXericTeleportAvailableInWidget(Widget widgetInterface, PoHTeleport teleport) {
        if (widgetInterface == null || teleport == null) {
            return false;
        }
        
        // Search through widget children for teleport text
        return searchWidgetForTeleportText(widgetInterface, teleport.getDestination());
    }
    
    /**
     * Helper method to check if a specific Digsite teleport is available in the widget interface.
     * 
     * @param widgetInterface The Digsite Pendant widget interface  
     * @param teleport The teleport to check for
     * @return true if the teleport is available in the interface
     */
    private static boolean isDigsiteTeleportAvailableInWidget(Widget widgetInterface, PoHTeleport teleport) {
        if (widgetInterface == null || teleport == null) {
            return false;
        }
        
        // Search through widget children for teleport text
        return searchWidgetForTeleportText(widgetInterface, teleport.getDestination());
    }
    
    /**
     * Helper method to recursively search widget hierarchy for specific teleport text.
     * 
     * @param widget The widget to search
     * @param teleportText The teleport text to search for
     * @return true if the text is found in the widget hierarchy
     */
    private static boolean searchWidgetForTeleportText(Widget widget, String teleportText) {
        if (widget == null || teleportText == null) {
            return false;
        }
        
        // Check current widget text
        String widgetText = widget.getText();
        if (widgetText != null && widgetText.contains(teleportText)) {
            return true;
        }
        
        // Check children recursively
        Widget[] children = widget.getChildren();
        if (children != null) {
            for (Widget child : children) {
                if (searchWidgetForTeleportText(child, teleportText)) {
                    return true;
                }
            }
        }
        
        // Check dynamic children
        Widget[] dynamicChildren = widget.getDynamicChildren();
        if (dynamicChildren != null) {
            for (Widget child : dynamicChildren) {
                if (searchWidgetForTeleportText(child, teleportText)) {
                    return true;
                }
            }
        }
        
        return false;
    }

    /**
     * Checks if the player is near their own house portal within a specified distance.
     * @param maxDistance maximum distance in tiles
     * @return true if player is near their house portal
     */
    private static boolean isNearOwnHousePortal(int maxDistance) {
        WorldPoint portalLocation = Rs2PoHPortal.getPlayerHousePortalLocation();
        if (portalLocation == null) {
            return false;
        }
        
        WorldPoint playerLocation = Rs2Player.getWorldLocation();
        if (playerLocation == null) {
            return false;
        }
        
        return playerLocation.distanceTo(portalLocation) <= maxDistance;
    }
}
