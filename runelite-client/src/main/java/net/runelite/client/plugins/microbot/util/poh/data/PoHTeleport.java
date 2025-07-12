package net.runelite.client.plugins.microbot.util.poh.data;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.plugins.worldmap.TeleportType;
import net.runelite.client.plugins.microbot.shortestpath.Transport;
import net.runelite.client.plugins.microbot.shortestpath.TransportType;
/**
 * Comprehensive enum for Player-owned House teleports including both Portal Nexus and Jewellery Box destinations.
 * This enum contains the core teleport data, while varbit mapping and availability detection is handled by utility classes.
 */
@AllArgsConstructor
@Getter
public enum PoHTeleport {

    // ===============================
    // PORTAL NEXUS TELEPORTS
    // ===============================
    // These are available via Portal Nexus - varbit slot mapping handled by Rs2PortalNexus

    // Varbit Slot 1
    VARROCK(TeleportType.NORMAL_MAGIC, "Portal to Varrok", "Varrock", new WorldPoint(3213, 3424, 0), 25),
    
    // Varbit Slot 2  
    LUMBRIDGE(TeleportType.NORMAL_MAGIC, "Portal to Lumbridge", "Lumbridge", new WorldPoint(3225, 3218, 0), 31),
    
    // Varbit Slot 3
    FALADOR(TeleportType.NORMAL_MAGIC, "Portal to Falador", "Falador", new WorldPoint(2966, 3379, 0), 37),
    
    // Varbit Slot 4
    CAMELOT(TeleportType.NORMAL_MAGIC, "Portal to Camelot", "Camelot", new WorldPoint(2757, 3477, 0), 45),
    
    // Varbit Slot 5
    ARDOUGNE(TeleportType.NORMAL_MAGIC, "Portal to Ardougne", "Ardougne", new WorldPoint(2661, 3300, 0), 51),
    
    // Varbit Slot 6
    WATCHTOWER(TeleportType.NORMAL_MAGIC, "Portal Nexus", "Watchtower", new WorldPoint(2549, 3112, 0), 58),
    
    // Varbit Slot 7
    TROLLHEIM(TeleportType.NORMAL_MAGIC, "Portal Nexus", "Trollheim", new WorldPoint(2888, 3676, 0), 61),
    
    // Varbit Slot 8
    APE_ATOLL(TeleportType.NORMAL_MAGIC, "Portal Nexus", "Ape Atoll", new WorldPoint(2754, 2784, 0), 64),
    
    // Varbit Slot 9
    KOUREND_CASTLE(TeleportType.NORMAL_MAGIC, "Portal Nexus", "Kourend Castle", new WorldPoint(1645, 3673, 0), 69),
    
    // Varbit Slot 10
    LUNAR_ISLE(TeleportType.LUNAR_MAGIC, "Portal Nexus", "Lunar Isle", new WorldPoint(2113, 3863, 0), 69),
    
    // Varbit Slot 11
    SENNTISTEN(TeleportType.ANCIENT_MAGICKS, "Portal Nexus", "Senntisten", new WorldPoint(3320, 3336, 0), 60),
    
    // Varbit Slot 12
    KHARYRLL(TeleportType.ANCIENT_MAGICKS, "Portal Nexus", "Kharyrll (Canifis)", new WorldPoint(3493, 3471, 0), 66),
    
    // Varbit Slot 13
    LASSAR(TeleportType.ANCIENT_MAGICKS, "Portal Nexus", "Lassar (Ice Mountain)", new WorldPoint(3006, 3471, 0), 72),
    
    // Varbit Slot 14
    DAREEYAK(TeleportType.ANCIENT_MAGICKS, "Portal Nexus", "Dareeyak (Ruins west of Canifis)", new WorldPoint(2966, 3696, 0), 78),
    
    // Varbit Slot 15
    CARRALLANGER(TeleportType.ANCIENT_MAGICKS, "Portal Nexus", "Carrallanger (Graveyard of Shadows)", new WorldPoint(3158, 3666, 0), 84),
    
    // Varbit Slot 16
    ANNAKARL(TeleportType.ANCIENT_MAGICKS, "Portal Nexus", "Annakarl (Demonic Ruins)", new WorldPoint(3287, 3886, 0), 90),
    
    // Varbit Slot 17
    GHORROCK(TeleportType.ANCIENT_MAGICKS, "Portal Nexus", "Ghorrock (Frozen Waste Plateau)", new WorldPoint(2977, 3873, 0), 96),
    
    // Varbit Slot 18
    BARBARIAN_TELEPORT(TeleportType.LUNAR_MAGIC, "Portal Nexus", "Barbarian Outpost", new WorldPoint(2544, 3568, 0), 75),
    
    // Varbit Slot 19
    KHAZARD_TELEPORT(TeleportType.LUNAR_MAGIC, "Portal Nexus", "Khazard", new WorldPoint(2635, 3167, 0), 78),
    
    // Varbit Slot 20
    FISHING_GUILD(TeleportType.LUNAR_MAGIC, "Portal Nexus", "Fishing Guild", new WorldPoint(2613, 3390, 0), 85),
    
    // Varbit Slot 21
    CATHERBY_TELEPORT(TeleportType.LUNAR_MAGIC, "Portal Nexus", "Catherby", new WorldPoint(2757, 3448, 0), 87),
    
    // Varbit Slot 22
    ICE_PLATEAU(TeleportType.LUNAR_MAGIC, "Portal Nexus", "Ice Plateau", new WorldPoint(2972, 3873, 0), 89),
    
    // Varbit Slot 23
    WEISS_TELEPORT(TeleportType.ARCEUUS_MAGIC, "Portal Nexus", "Weiss", new WorldPoint(2847, 3933, 0), 72),
    
    // Varbit Slot 24
    HARMONY_ISLAND(TeleportType.LUNAR_MAGIC, "Portal Nexus", "Harmony Island", new WorldPoint(3796, 2865, 0), 92),
    
    // Varbit Slot 25
    CEMETERY_TELEPORT(TeleportType.ARCEUUS_MAGIC, "Portal Nexus", "Cemetery", new WorldPoint(1741, 3597, 0), 71),
    
    // Varbit Slot 26
    RESURRECT_CROPS(TeleportType.ARCEUUS_MAGIC, "Portal Nexus", "Resurrect Crops", new WorldPoint(1307, 3739, 0), 78),
    
    // Varbit Slot 27
    SALVE_GRAVEYARD(TeleportType.ARCEUUS_MAGIC, "Portal Nexus", "Salve Graveyard", new WorldPoint(3431, 3460, 0), 83),
    
    // Varbit Slot 28
    FENKENSTRAIN_CASTLE(TeleportType.ARCEUUS_MAGIC, "Portal Nexus", "Fenkenstrain's Castle", new WorldPoint(3548, 3521, 0), 88),
    
    // Varbit Slot 29
    WEST_ARDOUGNE(TeleportType.ARCEUUS_MAGIC, "Portal Nexus", "West Ardougne", new WorldPoint(2500, 3290, 0), 61),
    
    // Varbit Slot 30
    MARIM(TeleportType.ARCEUUS_MAGIC, "Portal Nexus", "Marim", new WorldPoint(2760, 2793, 0), 90),
    
    // Varbit Slot 31
    BATTLEFRONT_TELEPORT(TeleportType.ARCEUUS_MAGIC, "Portal Nexus", "Battlefront", new WorldPoint(1349, 3739, 0), 23),

    // ===============================
    // JEWELLERY BOX TELEPORTS
    // ===============================
    // These use bit positions within POH_JEWELLERYBOX_MULTI varbit for availability detection

    // GAMES NECKLACE (bits 0-5)
    BARBARIAN_ASSAULT(TeleportType.JEWELLERY, "Games Necklace", "Barbarian Assault", new WorldPoint(2520, 3571, 0), 0),
                      
    BURTHORPE_GAMES_ROOM(TeleportType.JEWELLERY, "Games Necklace", "Burthorpe Games Room", new WorldPoint(2898, 3554, 0), 0),
                         
    TEARS_OF_GUTHIX(TeleportType.JEWELLERY, "Games Necklace", "Tears of Guthix", new WorldPoint(3245, 9500, 0), 0),
                    
    CORPOREAL_BEAST(TeleportType.JEWELLERY, "Games Necklace", "Corporeal Beast", new WorldPoint(2967, 4384, 0), 0),
                    
    WINTERTODT_CAMP(TeleportType.JEWELLERY, "Games Necklace", "Wintertodt Camp", new WorldPoint(1624, 3938, 0), 0),

    // RING OF DUELING (bits 6-9)
    PVP_ARENA(TeleportType.JEWELLERY, "Ring of Dueling", "Al Kharid PvP Arena", new WorldPoint(3315, 3235, 0), 0),
              
    CASTLE_WARS(TeleportType.JEWELLERY, "Ring of Dueling", "Castle Wars", new WorldPoint(2441, 3091, 0), 0),
                
    FEROX_ENCLAVE(TeleportType.JEWELLERY, "Ring of Dueling", "Ferox Enclave", new WorldPoint(3151, 3636, 0), 0),
                  
    FORTIS_COLOSSEUM(TeleportType.JEWELLERY, "Ring of Dueling", "Fortis Colosseum", new WorldPoint(1797, 1223, 0), 0),

    // COMBAT BRACELET (bits 10-14)
    WARRIORS_GUILD(TeleportType.JEWELLERY, "Combat Bracelet", "Warriors' Guild", new WorldPoint(2883, 3549, 0), 0),
                   
    CHAMPIONS_GUILD(TeleportType.JEWELLERY, "Combat Bracelet", "Champions' Guild", new WorldPoint(3189, 3368, 0), 0),
                    
    EDGEVILLE_MONASTERY(TeleportType.JEWELLERY, "Combat Bracelet", "Edgeville Monastery", new WorldPoint(3053, 3487, 0), 0),
                        
    RANGING_GUILD(TeleportType.JEWELLERY, "Combat Bracelet", "Ranging Guild", new WorldPoint(2654, 3441, 0), 0),

    // SKILLS NECKLACE (bits 15-21)
    FISHING_GUILD_NECK(TeleportType.JEWELLERY, "Skills Necklace", "Fishing Guild", new WorldPoint(2613, 3390, 0), 0),
                       
    MINING_GUILD(TeleportType.JEWELLERY, "Skills Necklace", "Mining Guild", new WorldPoint(3049, 9762, 0), 0),
                 
    CRAFTING_GUILD(TeleportType.JEWELLERY, "Skills Necklace", "Crafting Guild", new WorldPoint(2934, 3294, 0), 0),
                   
    COOKING_GUILD(TeleportType.JEWELLERY, "Skills Necklace", "Cooking Guild", new WorldPoint(3145, 3438, 0), 0),
                  
    WOODCUTTING_GUILD(TeleportType.JEWELLERY, "Skills Necklace", "Woodcutting Guild", new WorldPoint(1662, 3505, 0), 0),
                      
    FARMING_GUILD(TeleportType.JEWELLERY, "Skills Necklace", "Farming Guild", new WorldPoint(1249, 3717, 0), 0),

    // AMULET OF GLORY (bits 22-25)
    EDGEVILLE(TeleportType.JEWELLERY, "Amulet of Glory", "Edgeville", new WorldPoint(3087, 3496, 0), 0),
              
    KARAMJA(TeleportType.JEWELLERY, "Amulet of Glory", "Karamja", new WorldPoint(2918, 3176, 0), 0),
            
    DRAYNOR_VILLAGE(TeleportType.JEWELLERY, "Amulet of Glory", "Draynor Village", new WorldPoint(3105, 3251, 0), 0),
                    
    AL_KHARID(TeleportType.JEWELLERY, "Amulet of Glory", "Al Kharid", new WorldPoint(3293, 3163, 0), 0),

    // RING OF WEALTH (bits 26-29)
    MISCELLANIA(TeleportType.JEWELLERY, "Ring of Wealth", "Miscellania", new WorldPoint(2535, 3862, 0), 0),
                
    GRAND_EXCHANGE(TeleportType.JEWELLERY, "Ring of Wealth", "Grand Exchange", new WorldPoint(3162, 3480, 0), 0),
                   
    FALADOR_PARK(TeleportType.JEWELLERY, "Ring of Wealth", "Falador Park", new WorldPoint(2995, 3375, 0), 0),
                 
    DONDAKAN(TeleportType.JEWELLERY, "Ring of Wealth", "Dondakan", new WorldPoint(2831, 10165, 0), 0),

    // XERIC'S TALISMAN (bits 27-31) - Mountable jewelry in Portal Nexus
    XERICS_LOOKOUT(TeleportType.JEWELLERY, "Xeric's Talisman", "Xeric's Lookout", new WorldPoint(1590, 3479, 0), 0),
                   
    XERICS_GLADE(TeleportType.JEWELLERY, "Xeric's Talisman", "Xeric's Glade", new WorldPoint(1764, 3624, 0), 0),
                 
    XERICS_INFERNO(TeleportType.JEWELLERY, "Xeric's Talisman", "Xeric's Inferno", new WorldPoint(1505, 3809, 0), 0),
                   
    XERICS_HEART(TeleportType.JEWELLERY, "Xeric's Talisman", "Xeric's Heart", new WorldPoint(1773, 3658, 0), 0),
                 
    XERICS_HONOUR(TeleportType.JEWELLERY, "Xeric's Talisman", "Xeric's Honour", new WorldPoint(1561, 3602, 0), 0),

    // DIGSITE PENDANT (bits 32-34) - Mountable jewelry in Portal Nexus  
    DIGSITE(TeleportType.JEWELLERY, "Digsite Pendant", "Digsite", new WorldPoint(3346, 3445, 0), 0),
            
    FOSSIL_ISLAND(TeleportType.JEWELLERY, "Digsite Pendant", "Fossil Island", new WorldPoint(3724, 3808, 0), 0),
                  
    LITHKREN(TeleportType.JEWELLERY, "Digsite Pendant", "Lithkren", new WorldPoint(3548, 4004, 0), 0);

    // Core teleport data - only essential fields
    private final TeleportType type;
    private final String tooltip;
    private final String destination;
    private final WorldPoint location;
    private final int magicLevel;

    /**
     * Check if this teleport is a Portal Nexus teleport
     * @return true if this is a Portal Nexus teleport
     */
    public boolean isPortalTeleport() {
        return tooltip.equals("Portal");
    }

    /**
     * Check if this teleport is a Jewellery Box teleport
     * @return true if this is a Jewellery Box teleport
     */
    public boolean isJewelleryBoxTeleport() {
        return type == TeleportType.JEWELLERY;
    }

    public boolean isXericsTalismanTeleport() {
         return this == PoHTeleport.XERICS_LOOKOUT ||  this == PoHTeleport.XERICS_GLADE ||
                this == PoHTeleport.XERICS_INFERNO || this == PoHTeleport.XERICS_HEART ||
                this == PoHTeleport.XERICS_HONOUR;        
    }
    public boolean isDigsitePendantTeleport() {
        return this == PoHTeleport.DIGSITE || this == PoHTeleport.FOSSIL_ISLAND ||
               this == PoHTeleport.LITHKREN;
    }

    /**
     * Get all Portal Nexus teleports
     * @return array of Portal Nexus teleports
     */
    public static PoHTeleport[] getPortalNexusTeleports() {
        return java.util.Arrays.stream(values())
                .filter(PoHTeleport::isPortalTeleport)
                .toArray(PoHTeleport[]::new);
    }

    /**
     * Get all Jewellery Box teleports
     * @return array of Jewellery Box teleports
     */
    public static PoHTeleport[] getJewelleryBoxTeleports() {
        return java.util.Arrays.stream(values())
                .filter(PoHTeleport::isJewelleryBoxTeleport)
                .toArray(PoHTeleport[]::new);
    }

    /**
     * Get all Xeric's Talisman teleports
     * @return array of Xeric's Talisman teleports
     */
    public static PoHTeleport[] getXericsTalismanTeleports() {
        return java.util.Arrays.stream(values())
                .filter(PoHTeleport::isXericsTalismanTeleport)
                .toArray(PoHTeleport[]::new);
    }

    /**
     * Get all Digsite Pendant teleports
     * @return array of Digsite Pendant teleports
     */
    public static PoHTeleport[] getDigsitePendantTeleports() {
        return java.util.Arrays.stream(values())
                .filter(PoHTeleport::isDigsitePendantTeleport)
                .toArray(PoHTeleport[]::new);
    }

    /**
     * Get teleports by jewellery type
     * @param jewelryType the jewellery type name (e.g., "Games Necklace", "Ring of Dueling")
     * @return array of teleports for that jewellery type
     */
    public static PoHTeleport[] getByJewelleryType(String jewelryType) {
        return java.util.Arrays.stream(values())
                .filter(teleport -> teleport.tooltip.equals(jewelryType))
                .toArray(PoHTeleport[]::new);
    }

    /**
     * Get the Portal Nexus varbit slot for a teleport
     * @param teleport the teleport to get the slot for
     * @return varbit slot number (1-31) or -1 if not a Portal Nexus teleport
     */
    public static int getNexusVarbitSlot(PoHTeleport teleport) {
        if (teleport == null || !teleport.isPortalTeleport()) {
            return -1; // Not a Portal Nexus teleport
        }
       
        switch (teleport) {
            case VARROCK: return 1;
            case LUMBRIDGE: return 2;
            case FALADOR: return 3;
            case CAMELOT: return 4;
            case ARDOUGNE: return 5;
            case WATCHTOWER: return 6;
            case TROLLHEIM: return 7;
            case APE_ATOLL: return 8;
            case KOUREND_CASTLE: return 9;
            case LUNAR_ISLE: return 10;
            case SENNTISTEN: return 11;
            case KHARYRLL: return 12;
            case LASSAR: return 13;
            case DAREEYAK: return 14;
            case CARRALLANGER: return 15;
            case ANNAKARL: return 16;
            case GHORROCK: return 17;
            case BARBARIAN_TELEPORT: return 18;
            case KHAZARD_TELEPORT: return 19;
            case FISHING_GUILD: return 20;
            case CATHERBY_TELEPORT: return 21;
            case ICE_PLATEAU: return 22;
            case WEISS_TELEPORT: return 23;
            case HARMONY_ISLAND: return 24;
            case CEMETERY_TELEPORT: return 25;
            case RESURRECT_CROPS: return 26;
            case SALVE_GRAVEYARD: return 27;
            case FENKENSTRAIN_CASTLE: return 28;
            case WEST_ARDOUGNE: return 29;
            case MARIM: return 30;
            case BATTLEFRONT_TELEPORT: return 31;
            default: return -1;
        }
    }

    /**
     * Get the Jewellery Box bit position for a teleport
     * @param teleport the teleport to get the bit position for
     * @return bit position (0-29) or -1 if not a Jewellery Box teleport
     */
    public static int getJewelleryBitPosition(PoHTeleport teleport) {
        if (teleport == null || !teleport.isJewelleryBoxTeleport()) {
            return -1; // Not a Jewellery Box teleport
        }
       
        // TODO  we need to confirm the bit positions for each jewellery typ >> for now returning hardcoded values 
        
        switch (teleport) {
            // GAMES NECKLACE (bits 0-4)
            case BARBARIAN_ASSAULT: return 0;
            case BURTHORPE_GAMES_ROOM: return 1;
            case TEARS_OF_GUTHIX: return 2;
            case CORPOREAL_BEAST: return 3;
            case WINTERTODT_CAMP: return 4;
            
            // RING OF DUELING (bits 5-8)
            case PVP_ARENA: return 5;
            case CASTLE_WARS: return 6;
            case FEROX_ENCLAVE: return 7;
            case FORTIS_COLOSSEUM: return 8;
            
            // COMBAT BRACELET (bits 9-12)
            case WARRIORS_GUILD: return 9;
            case CHAMPIONS_GUILD: return 10;
            case EDGEVILLE_MONASTERY: return 11;
            case RANGING_GUILD: return 12;
            
            // SKILLS NECKLACE (bits 13-18)
            case FISHING_GUILD_NECK: return 13;
            case MINING_GUILD: return 14;
            case CRAFTING_GUILD: return 15;
            case COOKING_GUILD: return 16;
            case WOODCUTTING_GUILD: return 17;
            case FARMING_GUILD: return 18;
            
            // AMULET OF GLORY (bits 19-22)
            case EDGEVILLE: return 19;
            case KARAMJA: return 20;
            case DRAYNOR_VILLAGE: return 21;
            case AL_KHARID: return 22;
            
            // RING OF WEALTH (bits 23-26)
            case MISCELLANIA: return 23;
            case GRAND_EXCHANGE: return 24;
            case FALADOR_PARK: return 25;
            case DONDAKAN: return 26;
            
            // XERIC'S TALISMAN (bits 27-31) - Mountable jewelry in Portal Nexus
            case XERICS_LOOKOUT: return 27;
            case XERICS_GLADE: return 28;
            case XERICS_INFERNO: return 29;
            case XERICS_HEART: return 30;
            case XERICS_HONOUR: return 31;
            
            // DIGSITE PENDANT (bits 32-34) - Mountable jewelry in Portal Nexus
            case DIGSITE: return 32;
            case FOSSIL_ISLAND: return 33;
            case LITHKREN: return 34;
            
            default: return -1;
        }
    }

    /**
     * Get the jewellery type for a teleport
     * @param teleport the teleport to get the jewellery type for
     * @return jewellery type string or null if not a Jewellery Box teleport
     */
    public static String getJewelleryType(PoHTeleport teleport) {
        if (teleport == null || !teleport.isJewelleryBoxTeleport()) {
            return null; // Not a Jewellery Box teleport
        }        
        return teleport.getTooltip(); // The tooltip contains the jewellery type for jewellery teleports
    }
    
    /**
     * Maps a Transport to the corresponding PoHTeleports enum by checking destination coordinates.
     * This method determines if a given transport corresponds to a PoH teleport option.
     * 
     * @param transport the transport to check
     * @return the PoHTeleports enum corresponding to that transport destination, or null if not a PoH teleport
     */
    public static PoHTeleport fromTransport(Transport transport) {
        if (transport == null || transport.getDestination() == null) {
            return null;
        }
        
        // Only check teleportation spells and items as they are the ones that could match PoH teleports
        if (transport.getType() != TransportType.TELEPORTATION_SPELL &&
            transport.getType() != TransportType.TELEPORTATION_ITEM) {
            return null;
        }
        
        WorldPoint transportDestination = transport.getDestination();
        String transportDisplayInfo = transport.getDisplayInfo() != null ? transport.getDisplayInfo() : "";
        
        // Check all PoH teleports to see if any destination matches
        for (PoHTeleport pohTeleport : values()) {
            if (pohTeleport.location.equals(transportDestination)) {
                return pohTeleport;
            }
            if (transportDisplayInfo.toLowerCase().contains(pohTeleport.getDestination().toLowerCase())) {
                // If the transport's display info contains the teleport destination name, return it
                return pohTeleport;
            }
        }
        
        
        return null; // No matching PoH teleport found
    }
    
    /**
     * Maps a Transport to the corresponding PoHTeleports enum by checking destination coordinates with tolerance.
     * Some teleports might have slight coordinate variations, so this provides a fuzzy match.
     * 
     * @param transport the transport to check
     * @param tolerance the coordinate tolerance (default: 0 for exact match)
     * @return the PoHTeleports enum corresponding to that transport destination, or null if not a PoH teleport
     */
    public static PoHTeleport fromTransportWithTolerance(Transport transport, int tolerance) {
        if (transport == null || transport.getDestination() == null) {
            return null;
        }
        
        // Only check teleportation spells and items as they are the ones that could match PoH teleports
        if (transport.getType() != TransportType.TELEPORTATION_SPELL &&
            transport.getType() != TransportType.TELEPORTATION_ITEM) {
            return null;
        }
        
        WorldPoint transportDestination = transport.getDestination();
        String transportDisplayInfo = transport.getDisplayInfo() != null ? transport.getDisplayInfo() : "";
        // Check all PoH teleports to see if any destination matches within tolerance
        for (PoHTeleport pohTeleport : values()) {
            if (isWithinTolerance(pohTeleport.location, transportDestination, tolerance)) {
                return pohTeleport;
            }
            if (transportDisplayInfo.toLowerCase().contains(pohTeleport.getDestination().toLowerCase())) {
                // If the transport's display info contains the teleport destination name, return it
                return pohTeleport;
            }
        }
        
        return null; // No matching PoH teleport found
    }
    
    /**
     * Checks if two WorldPoints are within a given tolerance distance
     */
    private static boolean isWithinTolerance(WorldPoint point1, WorldPoint point2, int tolerance) {
        if (point1.getPlane() != point2.getPlane()) {
            return false;
        }
        
        int deltaX = Math.abs(point1.getX() - point2.getX());
        int deltaY = Math.abs(point1.getY() - point2.getY());
        
        return deltaX <= tolerance && deltaY <= tolerance;
    }
    
    /**
     * Maps a Portal Nexus varbit value to the corresponding PoHTeleports enum.
     * This method needs to be implemented based on actual game data research.
     * For now, this is a placeholder that returns null.
     * 
     * @param varbitValue the varbit value from Portal Nexus slot configuration
     * @return the PoHTeleports enum corresponding to that varbit value, or null if unknown
     */
    public static PoHTeleport fromNexusVarbitValue(int varbitValue) {
        // TODO: This mapping needs to be researched from actual game data
        // Each varbit value represents a specific teleport type that can be configured
        // in a Portal Nexus slot. The values would need to be determined by:
        // 1. Reading varbit values while different teleports are configured in-game
        // 2. Using MCP OSRS tools to research the varbit value mappings
        // 3. Cross-referencing with RuneLite constants if available
        
        // For now, return null until we have the actual mapping data
        // Example of what this might look like once researched:
        switch (varbitValue) {
            case 1: return VARROCK;
            case 2: return LUMBRIDGE;
            case 3: return FALADOR;
            case 4: return CAMELOT;
            case 5: return ARDOUGNE;
            case 6: return WATCHTOWER;
            case 7: return TROLLHEIM;
            case 8: return APE_ATOLL;
            case 9: return KOUREND_CASTLE;
            case 10: return LUNAR_ISLE;
            case 11: return SENNTISTEN;
            case 12: return KHARYRLL;
            case 13: return LASSAR;
            case 14: return DAREEYAK;
            case 15: return CARRALLANGER;
            case 16: return ANNAKARL;
            case 17: return GHORROCK;
            case 18: return BARBARIAN_TELEPORT;
            case 19: return KHAZARD_TELEPORT;
            case 20: return FISHING_GUILD;
            case 21: return CATHERBY_TELEPORT;
            case 22: return ICE_PLATEAU;
            case 23: return WEISS_TELEPORT;
            case 24: return HARMONY_ISLAND;
            case 25: return CEMETERY_TELEPORT;
            case 26: return RESURRECT_CROPS;
            case 27: return SALVE_GRAVEYARD;
            case 28: return FENKENSTRAIN_CASTLE;
            case 29: return WEST_ARDOUGNE;
            case 30: return MARIM;
            case 31: return BATTLEFRONT_TELEPORT;
            // Add more cases as needed for other teleports
            default: return null; // Unknown varbit value
        }
    }
    
    // ===============================
    // UTILITY METHODS FOR WIDGET EXTRACTION
    // ===============================
    
    /**
     * Gets a PoHTeleport by its destination name.
     * @param displayName the destination name to search for
     * @return the matching PoHTeleport or null if not found
     */
    public static PoHTeleport getByDisplayName(String displayName) {
        if (displayName == null) {
            return null;
        }
        
        for (PoHTeleport teleport : values()) {
            if (teleport.getDestination().equalsIgnoreCase(displayName)) {
                return teleport;
            }
        }
        return null;
    }
    
    /**
     * Gets a jewellery box teleport by widget text.
     * @param widgetText the text from the widget
     * @return the matching PoHTeleport or null if not found
     */
    public static PoHTeleport getJewelleryTeleportByText(String widgetText) {
        if (widgetText == null) {
            return null;
        }
        
        for (PoHTeleport teleport : getJewelleryBoxTeleports()) {
            if (widgetText.contains(teleport.getDestination()) || 
                teleport.getDestination().contains(widgetText)) {
                return teleport;
            }
        }
        return null;
    }
    
    /**
     * Gets a PoHTeleport by its portal frame object ID.
     * @param objectId the object ID of the portal frame
     * @return the matching PoHTeleport or null if not found
     */
    public static PoHTeleport getByPortalFrameObjectId(int objectId) {
        // This would need to be implemented based on the mapping between
        // object IDs and teleport destinations in portal frames
        // For now, return null as this requires detailed object ID mapping
        return null;
    }
}
