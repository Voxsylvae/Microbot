package net.runelite.client.plugins.microbot.util.widget;

import net.runelite.api.MenuAction;
import net.runelite.api.annotations.Component;
import java.awt.Rectangle;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.util.menu.NewMenuEntry;
import net.runelite.client.plugins.microbot.util.misc.Rs2UiHelper;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.extern.slf4j.Slf4j;

import static net.runelite.client.plugins.microbot.util.Global.sleepUntil;
import static net.runelite.client.plugins.microbot.util.Global.sleepUntilTrue;

@Slf4j
public class Rs2Widget {

    public static boolean sleepUntilHasWidgetText(String text, int widgetId, int childId, boolean exact, int sleep) {
        return sleepUntilTrue(() -> hasWidgetText(text, widgetId, childId, exact), 300, sleep);
    }

    public static boolean sleepUntilHasNotWidgetText(String text, int widgetId, int childId, boolean exact, int sleep) {
        return sleepUntilTrue(() -> !hasWidgetText(text, widgetId, childId, exact), 300, sleep);
    }

    public static boolean sleepUntilHasWidget(String text) {
        sleepUntil(() -> findWidget(text, null, false) != null);
        return findWidget(text, null, false) != null;
    }

    public static boolean clickWidget(String text, Optional<Integer> widgetId, int childId, boolean exact) {
        return Microbot.getClientThread().runOnClientThreadOptional(() -> {

            Widget widget;
            if (!widgetId.isPresent()) {
                widget = findWidget(text, null, exact);
            } else {
                Widget rootWidget = getWidget(widgetId.get(), childId);
                List<Widget> rootWidgets = new ArrayList<>();
                rootWidgets.add(rootWidget);
                widget = findWidget(text, rootWidgets, exact);
            }

            if (widget != null) {
                clickWidget(widget);
            }

            return widget != null;

        }).orElse(false);
    }

    public static boolean clickWidget(Widget widget) {
        if (widget != null) {
            Microbot.getMouse().click(widget.getBounds());
            return true;
        }
        return false;
    }

    public static boolean clickWidget(String text) {
        return clickWidget(text, Optional.empty(), 0, false);
    }

    public static boolean clickWidget(String text, boolean exact) {
        return clickWidget(text, Optional.empty(), 0, exact);
    }

    public static boolean clickWidget(int parentId, int childId) {
        Widget widget = getWidget(parentId, childId);
        return clickWidget(widget);
    }

    public static boolean isWidgetVisible(@Component int id) {
        return Microbot.getClientThread().runOnClientThreadOptional(() -> {
            Widget widget = getWidget(id);
            if (widget == null) return false;
            return !widget.isHidden();
        }).orElse(false);
    }

    public static boolean isWidgetVisible(int widgetId, int childId) {
       return  Microbot.getClientThread().runOnClientThreadOptional(() -> {
            Widget widget = getWidget(widgetId, childId);
            if (widget == null) return false;
            return !widget.isHidden();
        }).orElse(false);
    }

    public static Widget getWidget(@Component int id) {
        return Microbot.getClientThread().runOnClientThreadOptional(() -> Microbot.getClient().getWidget(id)).orElse(null);
    }

    public static boolean isHidden(int parentId, int childId) {
        return Microbot.getClientThread().runOnClientThreadOptional(() -> {
            Widget widget = Microbot.getClient().getWidget(parentId, childId);
            if (widget == null) return true;
            return widget.isHidden();
        }).orElse(false);
    }

    public static boolean isHidden(@Component int id) {
        return Microbot.getClientThread().runOnClientThreadOptional(() -> {
            Widget widget = Microbot.getClient().getWidget(id);
            if (widget == null) return true;
            return widget.isHidden();
        }).orElse(false);
    }

    public static Widget getWidget(int id, int child) {
        return Microbot.getClientThread().runOnClientThreadOptional(() -> Microbot.getClient().getWidget(id, child))
                .orElse(null);
    }

    public static int getChildWidgetSpriteID(int id, int childId) {
        return Microbot.getClientThread().runOnClientThreadOptional(() -> Microbot.getClient().getWidget(id, childId).getSpriteId())
                .orElse(0);
    }

    public static String getChildWidgetText(int id, int childId) {
        Widget widget = getWidget(id, childId);
        if (widget != null) {
            return widget.getText();
        }
        return "";
    }

    public static boolean clickWidget(int id) {
        Widget widget = Microbot.getClientThread().runOnClientThreadOptional(() -> Microbot.getClient().getWidget(id)).orElse(null);;
        if (widget == null || isHidden(id)) return false;
        Microbot.getMouse().click(widget.getBounds());
        return true;
    }

    public static boolean clickChildWidget(int id, int childId) {
        Widget widget = Microbot.getClientThread().runOnClientThreadOptional(() -> Microbot.getClient().getWidget(id)).orElse(null);;
        if (widget == null) return false;
        Microbot.getMouse().click(widget.getChild(childId).getBounds());
        return true;
    }

    public static Widget findWidget(String text, List<Widget> children) {
        return findWidget(text, children, false);
    }

	public static boolean hasWidgetText(String text, int componentId, boolean exact) {
		return Microbot.getClientThread().runOnClientThreadOptional(() -> {
			Widget rootWidget = getWidget(componentId);
			if (rootWidget == null) return false;

			// Use findWidget to perform the search on all child types
			Widget foundWidget = findWidget(text, List.of(rootWidget), exact);
			return foundWidget != null;
		}).orElse(false);
	}

    public static boolean hasWidgetText(String text, int widgetId, int childId, boolean exact) {
        return Microbot.getClientThread().runOnClientThreadOptional(() -> {
            Widget rootWidget = getWidget(widgetId, childId);
            if (rootWidget == null) return false;

            // Use findWidget to perform the search on all child types
            Widget foundWidget = findWidget(text, List.of(rootWidget), exact);
            return foundWidget != null;
        }).orElse(false);
    }

    public static Widget findWidget(String text) {
        return findWidget(text, null, false);
    }

    public static Widget findWidget(String text, boolean exact) {
        return findWidget(text, null, exact);
    }

    public static boolean hasWidget(String text) {
        return findWidget(text, null, false) != null;
    }

    /**
     * Searches for a widget with text that matches the specified criteria, either in the provided child widgets
     * or across all root widgets if children are not specified.
     *
     * @param text     The text to search for within the widgets.
     * @param children A list of child widgets to search within. If null, searches through all root widgets.
     * @param exact    Whether the search should match the text exactly or allow partial matches.
     * @return The widget containing the specified text, or null if no match is found.
     */
    public static Widget findWidget(String text, List<Widget> children, boolean exact) {
        return Microbot.getClientThread().runOnClientThreadOptional(() -> {
            Widget foundWidget = null;
            if (children == null) {
                // Search through root widgets if no specific children are provided
                List<Widget> rootWidgets = Arrays.stream(Microbot.getClient().getWidgetRoots())
                        .filter(x -> x != null && !x.isHidden()).collect(Collectors.toList());
                for (Widget rootWidget : rootWidgets) {
                    if (rootWidget == null) continue;
                    if (matchesText(rootWidget, text, exact)) {
                        return rootWidget;
                    }
                    foundWidget = searchChildren(text, rootWidget, exact);
                    if (foundWidget != null) return foundWidget;
                }
            } else {
                // Search within provided child widgets
                for (Widget child : children) {
                    foundWidget = searchChildren(text, child, exact);
                    if (foundWidget != null) break;
                }
            }
            return foundWidget;
        }).orElse(null);
    }

    /**
     * Recursively searches through all child widgets of the specified widget for a match with the given text.
     *
     * @param text  The text to search for within the widget and its children.
     * @param child The widget to search within.
     * @param exact Whether the search should match the text exactly or allow partial matches.
     * @return The widget containing the specified text, or null if no match is found.
     */
    public static Widget searchChildren(String text, Widget child, boolean exact) {
        if (matchesText(child, text, exact)) return child;

        List<Widget[]> childGroups = Stream.of(child.getChildren(), child.getNestedChildren(), child.getDynamicChildren(), child.getStaticChildren())
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        for (Widget[] childGroup : childGroups) {
            if (childGroup != null) {
                for (Widget nestedChild : Arrays.stream(childGroup).filter(w -> w != null && !w.isHidden()).collect(Collectors.toList())) {
                    Widget found = searchChildren(text, nestedChild, exact);
                    if (found != null) return found;
                }
            }
        }
        return null;
    }

    /**
     * Checks if the text or any action in the widget matches the search criteria.
     *
     * @param widget The widget to check for the specified text or action.
     * @param text   The text to match within the widget’s content.
     * @param exact  Whether the match should be exact or allow partial matches.
     * @return True if the widget's text or any action matches the search criteria, false otherwise.
     */
    private static boolean matchesText(Widget widget, String text, boolean exact) {
        String cleanText = Rs2UiHelper.stripColTags(widget.getText());
        String cleanName = Rs2UiHelper.stripColTags(widget.getName());

        if (exact) {
            if (cleanText.equalsIgnoreCase(text) || cleanName.equalsIgnoreCase(text)) return true;
        } else {
            if (cleanText.toLowerCase().contains(text.toLowerCase()) || cleanName.toLowerCase().contains(text.toLowerCase()))
                return true;
        }

        if (widget.getActions() != null) {
            for (String action : widget.getActions()) {
                if (action != null) {
                    String cleanAction = Rs2UiHelper.stripColTags(action);
                    if (exact ? cleanAction.equalsIgnoreCase(text) : cleanAction.toLowerCase().contains(text.toLowerCase())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Searches for a widget with the specified sprite ID among root widgets or the specified child widgets.
     *
     * @param spriteId The sprite ID to search for.
     * @param children A list of child widgets to search within. If null, searches root widgets.
     * @return The widget with the specified sprite ID, or null if not found.
     */
    public static Widget findWidget(int spriteId, List<Widget> children) {
        return Microbot.getClientThread().runOnClientThreadOptional(() -> {
            Widget foundWidget = null;

            if (children == null) {
                // Search through root widgets if no specific children are provided
                List<Widget> rootWidgets = Arrays.stream(Microbot.getClient().getWidgetRoots())
                        .filter(widget -> widget != null && !widget.isHidden())
                        .collect(Collectors.toList());
                for (Widget rootWidget : rootWidgets) {
                    if (rootWidget == null) continue;
                    if (matchesSpriteId(rootWidget, spriteId)) {
                        return rootWidget;
                    }
                    foundWidget = searchChildren(spriteId, rootWidget);
                    if (foundWidget != null) return foundWidget;
                }
            } else {
                // Search within provided child widgets
                for (Widget child : children) {
                    foundWidget = searchChildren(spriteId, child);
                    if (foundWidget != null) break;
                }
            }
            return foundWidget;
        }).orElse(null);
    }

    /**
     * Recursively searches through the child widgets of the given widget for a match with the specified sprite ID.
     *
     * @param spriteId The sprite ID to search for.
     * @param child    The widget to search within.
     * @return The widget with the specified sprite ID, or null if not found.
     */
    public static Widget searchChildren(int spriteId, Widget child) {
        if (matchesSpriteId(child, spriteId)) return child;

        List<Widget[]> childGroups = Stream.of(child.getChildren(), child.getNestedChildren(), child.getDynamicChildren(), child.getStaticChildren())
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        for (Widget[] childGroup : childGroups) {
            if (childGroup != null) {
                for (Widget nestedChild : Arrays.stream(childGroup).filter(w -> w != null && !w.isHidden()).collect(Collectors.toList())) {
                    Widget found = searchChildren(spriteId, nestedChild);
                    if (found != null) return found;
                }
            }
        }
        return null;
    }

    /**
     * Checks if a widget's sprite ID matches the specified sprite ID.
     *
     * @param widget   The widget to check.
     * @param spriteId The sprite ID to match.
     * @return True if the widget's sprite ID matches the specified sprite ID, false otherwise.
     */
    private static boolean matchesSpriteId(Widget widget, int spriteId) {
        return widget != null && widget.getSpriteId() == spriteId;
    }

    public static void clickWidgetFast(int packetId, int identifier) {
        Widget widget = getWidget(packetId);
        clickWidgetFast(widget, -1, identifier);
    }

    public static void clickWidgetFast(Widget widget, int param0, int identifier) {
        int param1 = widget.getId();
        String target = "";
        MenuAction menuAction = MenuAction.CC_OP;
        Microbot.doInvoke(new NewMenuEntry(param0 != -1 ? param0 : widget.getType(), param1, menuAction.getId(), identifier, widget.getItemId(), target), widget.getBounds());
    }

    public static void clickWidgetFast(Widget widget, int param0) {
        clickWidgetFast(widget, param0, 1);
    }

    public static void clickWidgetFast(Widget widget) {
        clickWidgetFast(widget, -1, 1);
    }

    // check if production widget is open
    public static boolean isProductionWidgetOpen() {
        return isWidgetVisible(270, 0);
    }

    // check if GoldCrafting widget is open
    public static boolean isGoldCraftingWidgetOpen() {
        return isWidgetVisible(446, 0);
    }

    // check if SilverCrafting widget is open
    public static boolean isSilverCraftingWidgetOpen() {
        return isWidgetVisible(6, 0);
    }

    // check if smithing widget is open
    public static boolean isSmithingWidgetOpen() {
        return isWidgetVisible(InterfaceID.SMITHING, 0);
    }

    // check if deposit box widget is open
    public static boolean isDepositBoxWidgetOpen() {
        return isWidgetVisible(192, 0);
    }

    public static boolean isWildernessInterfaceOpen() {
        return isWidgetVisible(475, 11);
    }

    public static boolean enterWilderness() {
        if (!isWildernessInterfaceOpen()) return false;

        Microbot.log("Detected Wilderness warning, interacting...");
        Rs2Widget.clickWidget(475, 11);

        return true;
    }



    
    public static boolean checkBoundsOverlapWidgetInMainModal( Rectangle overlayBoundsCanvas, int viewportXOffset, int viewportYOffset) {
        final int MAIN_MODAL_TOPLEVEL_CHILD_ID = 40; // Main modal child ID
        final int MAIN_MODAL_STRECH_CHILD_ID = 16; // Main modal child ID
        Widget mainModalWidget = getWidget(net.runelite.api.gameval.InterfaceID.TOPLEVEL, MAIN_MODAL_TOPLEVEL_CHILD_ID);
        if (mainModalWidget == null || mainModalWidget.isHidden()) {
            mainModalWidget  = getWidget(net.runelite.api.gameval.InterfaceID.TOPLEVEL_OSRS_STRETCH, MAIN_MODAL_STRECH_CHILD_ID);
            
        }
        if (mainModalWidget == null ) {            
            mainModalWidget  = getWidget(net.runelite.api.gameval.InterfaceID.TOPLEVEL_PRE_EOC, MAIN_MODAL_STRECH_CHILD_ID);
        }
        return checkWidgetAndDescendantsForOverlapCanvas(mainModalWidget, overlayBoundsCanvas, viewportXOffset, viewportYOffset);
    }
    /**
	* Recursively iterates all descendants, but only checks bounds for nested containers 
	* This matches the requirement: only nested containers within the static container are checked for overlap.
	*/    
    private static boolean checkWidgetAndDescendantsForOverlapCanvas(Widget widget, Rectangle overlayBoundsCanvas, int viewportXOffset, int viewportYOffset) {
	    if (widget == null || widget.isHidden()) {
		   return false;
	    }       	   
	    List<Widget[]> nestedAndDynamicWidgets = new java.util.ArrayList<>();
	    if (widget.getDynamicChildren() != null) nestedAndDynamicWidgets.add(widget.getDynamicChildren());
		if (widget.getNestedChildren() != null) nestedAndDynamicWidgets.add(widget.getNestedChildren());
	    for (Widget[] widgetArray : nestedAndDynamicWidgets) {
		   for (Widget nestedOrDynamic : widgetArray) {
			   if (nestedOrDynamic == null || nestedOrDynamic.isHidden()) {
				   continue;
			   }
               int groupId = nestedOrDynamic.getId() >>> 16; // upper 16 bits
			   if(  nestedOrDynamic.getCanvasLocation() == null) {				   
				   continue;
			   }
			   Rectangle widgetBounds = nestedOrDynamic.getBounds();
			   if (widgetBounds != null) {
				   Rectangle widgetCanvasBounds = new Rectangle(
					   widgetBounds.x + viewportXOffset,
					   widgetBounds.y + viewportYOffset,
					   widgetBounds.width,
					   widgetBounds.height
				   );
				   if (widgetCanvasBounds.intersects(overlayBoundsCanvas)) {
					   Rectangle intersection = widgetCanvasBounds.intersection(overlayBoundsCanvas);
					   if (intersection.width > 8 && intersection.height > 8) {
                            log.debug("Widget with group ID {} and child ID {} overlaps with the overlay bounds.\n" +
                                 "Widget ID: {}, Title: {}, Canvas Location: {}, Bounds: {}, Intersection: {}",
                                 groupId, nestedOrDynamic.getId() & 0xFFFF, nestedOrDynamic.getId(),
                                 nestedOrDynamic.getName(), nestedOrDynamic.getCanvasLocation(),
                                 widgetCanvasBounds, intersection);
						   return true;
					   }
				   }
			   }
		   }
	   }
	   

	   // Recursively check all children for nested containers
	   List<Widget[]> childGroups = new java.util.ArrayList<>();
	   
	   if (widget.getStaticChildren() != null) childGroups.add(widget.getStaticChildren());
	   

	   for (Widget[] childGroup : childGroups) {
		   for (Widget child : childGroup) {
			   if (child != null && !child.isHidden()) {					
					int widgetId = child.getId();
					int groupId = widgetId >>> 16; // upper 16 bits
					int childId = widgetId & 0xFFFF; // lower 16 bits	
                    if (child.getCanvasLocation() == null || (child.getCanvasLocation().getX() == 0 && child.getCanvasLocation().getY() == 0)) {
                        continue;
                    }				
				   if (checkWidgetAndDescendantsForOverlapCanvas(child, overlayBoundsCanvas, viewportXOffset, viewportYOffset)) {
                        Widget parentWidget = child.getParent();
                        String title = parentWidget != null ? parentWidget.getName() : "Unknown";
                        int parentId = parentWidget != null ? parentWidget.getId() : -1;
                        int parentGoupID = parentId >>> 16; // upper 16 bits
                        int parentChildID = parentId & 0xFFFF; // lower 16 bits

                        log.debug("Widget with group ID {} and child ID {} overlaps with the overlay bounds.\n" +
                                 "Parent Widget ID: {}, Group ID: {}, Child ID: {}, Title: {}",
                                 groupId, childId, parentId, parentGoupID, parentChildID, title);
					   return true;
				   }
			   }
		   }
	   }
	   return false;
   }
    // ========== SCROLLING FUNCTIONALITY ==========
    
    /**
     * Checks if a widget is within the specified canvas bounds
     * @param widget The widget to check
     * @param canvasBounds The canvas bounds to check against
     * @return true if the widget is within bounds and clickable, false otherwise
     */
    public static boolean isWidgetWithinCanvasBounds(Widget widget, Rectangle canvasBounds) {
        if (widget == null || widget.isHidden() || canvasBounds == null) {
            return false;
        }
        
        net.runelite.api.Point widgetLocation = widget.getCanvasLocation();
        if (widgetLocation == null) {
            return false;
        }
        
        Rectangle widgetBounds = new Rectangle(widgetLocation.getX(), widgetLocation.getY(), 
                                             widget.getWidth(), widget.getHeight());
        
        // Check if the widget is at least partially within canvas bounds
        return canvasBounds.intersects(widgetBounds);
    }
    
    /**
     * Clicks a widget with automatic scrolling if the widget is not within the canvas bounds.
     * This method will attempt to scroll the widget into view before clicking it.
     * Uses widget IDs to refresh widget references after each scroll attempt to handle stale widgets.
     * @param widgetId The widget ID
     * @param childId The child ID
     * @param canvasBounds The canvas bounds to scroll within
     * @param scrollContainer The container widget to scroll (can be null to use parent)
     * @param maxScrollAttempts Maximum number of scroll attempts before giving up
     * @return true if the widget was successfully clicked, false otherwise
     */
    public static boolean clickWidgetWithScrolling(int widgetId, int childId, Rectangle canvasBounds, 
                                                 Widget scrollContainer, int maxScrollAttempts) {
        if (canvasBounds == null) {
            Microbot.log("Invalid canvas bounds for scrolling click");
            return false;
        }
        
        // Get fresh widget reference
        Widget targetWidget = getWidget(widgetId, childId);
        if (targetWidget == null || targetWidget.isHidden()) {
            Microbot.log("Target widget not found or hidden: " + widgetId + "," + childId);
            return false;
        }
        
        // If widget is already within bounds, click it directly
        if (isWidgetWithinCanvasBounds(targetWidget, canvasBounds)) {
            clickWidget(targetWidget);
            return true;
        }
        
        // Determine scroll container (use parent if not specified)
        Widget container = scrollContainer;
        if (container == null) {
            container = targetWidget.getParent();
        }
        
        if (container == null) {
            Microbot.log("No scroll container available for scrolling click");
            return false;
        }
        
        // Get container center point for scrolling
        net.runelite.api.Point containerLocation = container.getCanvasLocation();
        if (containerLocation == null) {
            Microbot.log("Container canvas location not available for scrolling");
            return false;
        }
        
        net.runelite.api.Point containerCenter = new net.runelite.api.Point(
            containerLocation.getX() + container.getWidth() / 2,
            containerLocation.getY() + container.getHeight() / 2
        );
        
        // Determine initial scroll direction based on widget position relative to canvas
        net.runelite.api.Point targetLocation = targetWidget.getCanvasLocation();
        if (targetLocation == null) {
            Microbot.log("Target widget canvas location not available");
            return false;
        }
        
        boolean scrollDown = targetLocation.getY() > (canvasBounds.y + canvasBounds.height);
        
        // Perform scrolling attempts
        for (int attempt = 0; attempt < maxScrollAttempts; attempt++) {
            // Move mouse to container center before scrolling
            Microbot.getNaturalMouse().moveTo(containerCenter.getX(), containerCenter.getY());
            
            // Perform scroll action
            if (scrollDown) {
                Microbot.getMouse().scrollDown(containerCenter);
            } else {
                Microbot.getMouse().scrollUp(containerCenter);
            }
            
            // Wait for scroll to complete
            sleepUntil(() -> true, 300);
            
            // Refresh widget reference after scroll (important for stale widget handling)
            targetWidget = getWidget(widgetId, childId);
            if (targetWidget == null || targetWidget.isHidden()) {
                Microbot.log("Target widget became null/hidden after scroll attempt " + (attempt + 1));
                continue;
            }
            
            // Check if widget is now within bounds
            if (isWidgetWithinCanvasBounds(targetWidget, canvasBounds)) {
                clickWidget(targetWidget);
                return true;
            }
            
            // Update target location and scroll direction for next attempt
            targetLocation = targetWidget.getCanvasLocation();
            if (targetLocation != null) {
                scrollDown = targetLocation.getY() > (canvasBounds.y + canvasBounds.height);
            }
        }
        
        Microbot.log("Failed to scroll widget into view after " + maxScrollAttempts + " attempts");
        return false;
    }

    /**
     * Scrolls a container until the target widget is within the canvas bounds, then clicks it
     * WARNING: This method uses the same widget reference throughout - prefer the widgetId/childId version for reliability.
     * @param targetWidget The widget to click
     * @param canvasBounds The canvas bounds to scroll within
     * @param scrollContainer The container widget to scroll (can be null to use targetWidget's parent)
     * @param maxScrollAttempts Maximum number of scroll attempts before giving up
     * @return true if the widget was successfully clicked, false otherwise
     */
    public static boolean clickWidgetWithScrolling(Widget targetWidget, Rectangle canvasBounds, 
                                                 Widget scrollContainer, int maxScrollAttempts) {
        if (targetWidget == null || targetWidget.isHidden() || canvasBounds == null) {
            Microbot.log("Invalid widget or canvas bounds for scrolling click");
            return false;
        }
        
        // If widget is already within bounds, click it directly
        if (isWidgetWithinCanvasBounds(targetWidget, canvasBounds)) {
            clickWidget(targetWidget);
            return true;
        }
        
        // Determine scroll container (use parent if not specified)
        Widget container = scrollContainer;
        if (container == null) {
            container = targetWidget.getParent();
        }
        
        if (container == null) {
            Microbot.log("No scroll container available for scrolling click");
            return false;
        }
        
        // Get container center point for scrolling
        net.runelite.api.Point containerLocation = container.getCanvasLocation();
        if (containerLocation == null) {
            Microbot.log("Container canvas location not available for scrolling");
            return false;
        }
        
        net.runelite.api.Point containerCenter = new net.runelite.api.Point(
            containerLocation.getX() + container.getWidth() / 2,
            containerLocation.getY() + container.getHeight() / 2
        );
        
        // Determine scroll direction based on widget position relative to canvas
        net.runelite.api.Point targetLocation = targetWidget.getCanvasLocation();
        if (targetLocation == null) {
            Microbot.log("Target widget canvas location not available");
            return false;
        }
        
        boolean scrollDown = targetLocation.getY() > (canvasBounds.y + canvasBounds.height);
        int targetWidgetId = targetWidget.getId();
        // Perform scrolling attempts
        for (int attempt = 0; attempt < maxScrollAttempts; attempt++) {
            // Move mouse to container center before scrolling
            Microbot.getNaturalMouse().moveTo(containerCenter.getX(), containerCenter.getY());
            
            // Perform scroll action
            if (scrollDown) {
                Microbot.getMouse().scrollDown(containerCenter);
            } else {
                Microbot.getMouse().scrollUp(containerCenter);
            }
            
            // Wait for scroll to complete
            sleepUntil(() -> true, 300);
             // Refresh widget reference after scroll (important for stale widget handling)
            targetWidget = getWidget(targetWidgetId);
            if (targetWidget == null || targetWidget.isHidden()) {
                Microbot.log("Target widget became null/hidden after scroll attempt " + (attempt + 1));
                continue;
            }
            // Check if widget is now within bounds
            if (isWidgetWithinCanvasBounds(targetWidget, canvasBounds)) {
                clickWidget(targetWidget);
                return true;
            }
            
            // Update target location and scroll direction for next attempt
            targetLocation = targetWidget.getCanvasLocation();
            if (targetLocation != null) {
                scrollDown = targetLocation.getY() > (canvasBounds.y + canvasBounds.height);
            }
        }
        
        Microbot.log("Failed to scroll widget into view after " + maxScrollAttempts + " attempts");
        return false;
    }
    
    /**
     * Convenience method for clickWidgetWithScrolling with default scroll attempts
     * @param targetWidget The widget to click
     * @param canvasBounds The canvas bounds to scroll within
     * @param scrollContainer The container widget to scroll
     * @return true if the widget was successfully clicked, false otherwise
     */
    public static boolean clickWidgetWithScrolling(Widget targetWidget, Rectangle canvasBounds, 
                                                 Widget scrollContainer) {
        return clickWidgetWithScrolling(targetWidget, canvasBounds, scrollContainer, 10);
    }
    
    /**
     * Convenience method for clickWidgetWithScrolling using widget IDs with default scroll attempts
     * @param widgetId The widget ID
     * @param childId The child ID
     * @param canvasBounds The canvas bounds to scroll within
     * @param scrollContainer The container widget to scroll
     * @return true if the widget was successfully clicked, false otherwise
     */
    public static boolean clickWidgetWithScrolling(int widgetId, int childId, Rectangle canvasBounds, 
                                                 Widget scrollContainer) {
        return clickWidgetWithScrolling(widgetId, childId, canvasBounds, scrollContainer, 10);
    }
    
    /**
     * Convenience method for clickWidgetWithScrolling using widget IDs and parent bounds for scrolling
     * @param widgetId The widget ID
     * @param childId The child ID
     * @param maxScrollAttempts Maximum number of scroll attempts before giving up
     * @return true if the widget was successfully clicked, false otherwise
     */
    public static boolean clickWidgetWithScrolling(int widgetId, int childId, int maxScrollAttempts) {
        Widget targetWidget = getWidget(widgetId, childId);
        if (targetWidget == null || targetWidget.isHidden()) {
            Microbot.log("Target widget not found or hidden: " + widgetId + "," + childId);
            return false;
        }
        
        Widget parent = targetWidget.getParent();
        if (parent == null) {
            Microbot.log("Target widget has no parent for canvas bounds");
            return false;
        }
        
        Rectangle canvasBounds = parent.getBounds();
        if (canvasBounds == null) {
            Microbot.log("Failed to get canvas bounds from parent widget");
            return false;
        }
        
        return clickWidgetWithScrolling(widgetId, childId, canvasBounds, parent, maxScrollAttempts);
    }
    
    /**
     * Convenience method for clickWidgetWithScrolling using widget IDs and parent bounds with default attempts
     * @param widgetId The widget ID
     * @param childId The child ID
     * @return true if the widget was successfully clicked, false otherwise
     */
    public static boolean clickWidgetWithScrolling(int widgetId, int childId) {
        return clickWidgetWithScrolling(widgetId, childId, 10);
    }
    
    /**
     * Convenience method for clickWidgetWithScrolling using widget's parent bounds for scrolling
     * @param targetWidget The widget to click
     * @param maxScrollAttempts Maximum number of scroll attempts before giving up
     * @return true if the widget was successfully clicked, false otherwise
     */
    public static boolean clickWidgetWithScrolling(Widget targetWidget, int maxScrollAttempts) {
        if (targetWidget == null) {
            Microbot.log("Target widget is null for scrolling click");
            return false;
        }
        
        Widget parent = targetWidget.getParent();
        if (parent == null) {
            Microbot.log("Target widget has no parent for canvas bounds");
            return false;
        }
        
        Rectangle canvasBounds = parent.getBounds();
        if (canvasBounds == null) {
            Microbot.log("Failed to get canvas bounds from parent widget");
            return false;
        }
        
        return clickWidgetWithScrolling(targetWidget, canvasBounds, parent, maxScrollAttempts);
    }
    
    /**
     * Convenience method for clickWidgetWithScrolling using widget's parent bounds with default attempts
     * @param targetWidget The widget to click
     * @return true if the widget was successfully clicked, false otherwise
     */
    public static boolean clickWidgetWithScrolling(Widget targetWidget) {
        return clickWidgetWithScrolling(targetWidget, 10);
    }
}
