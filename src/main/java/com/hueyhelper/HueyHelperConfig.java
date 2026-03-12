package com.hueyhelper;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

@ConfigGroup("hueyhelper")
public interface HueyHelperConfig extends Config {

	// --- ENUMS FOR DROPDOWNS ---
	public enum PanelFontType {
		SMALL("RS Small"),
		REGULAR("RS Regular"),
		BOLD("RS Bold"),
		QUILL("Quill"),
		QUILL_CAPS("Quill Caps"),
		BARBARIAN("Barb Assault"),
		BEBAS_NEUE("Bebas Neue");

		private final String name;
		PanelFontType(String name) { this.name = name; }
		@Override public String toString() { return name; }
	}

	// --- SECTIONS ---

	@ConfigSection(name = "Overlay", description = "Settings for the live fight overlay", position = 0)
	String overlaySection = "overlay";

	@ConfigSection(name = "Side Panel", description = "Settings for the RuneLite side panel", position = 1)
	String sidePanelSection = "sidePanel";

	@ConfigSection(name = "Other", description = "Other general settings", position = 2)
	String otherSection = "other";

	@ConfigSection(name = "Debug", description = "Debugging tools for development", position = 3)
	String debugSection = "debug";

	// --- OVERLAY SETTINGS ---

	@ConfigItem(keyName = "showOverlay", name = "Show Live Fight Stats", description = "Shows the live fight statistics overlay", position = 1, section = overlaySection)
	default boolean showOverlay() { return true; }

	@ConfigItem(keyName = "showEligibilityInfoBox", name = "Show Eligibility InfoBox", description = "Shows a green/red marker box indicating if you have qualified for loot", position = 2, section = overlaySection)
	default boolean showEligibilityInfoBox() { return true; }

	@ConfigItem(keyName = "showParticipants", name = "Show Participants", description = "Shows the number of players in the arena", position = 3, section = overlaySection)
	default boolean showParticipants() { return true; }

	@ConfigItem(keyName = "showPhaseDamage", name = "Show Phase Damage", description = "Shows your damage for Phase 1 and Phase 2", position = 4, section = overlaySection)
	default boolean showPhaseDamage() { return true; }

	@ConfigItem(keyName = "showPercentages", name = "Show Damage Percentages", description = "Shows the % of the boss's total HP you have dealt", position = 5, section = overlaySection)
	default boolean showPercentages() { return true; }

	@ConfigItem(keyName = "showContribution", name = "Show Reward %", description = "Shows your calculated loot contribution percentage", position = 6, section = overlaySection)
	default boolean showContribution() { return true; }

	@ConfigItem(keyName = "showDropRates", name = "Show Rare Drop Rates", description = "Shows your dynamic drop rates for the unique items", position = 7, section = overlaySection)
	default boolean showDropRates() { return true; }

	// --- SIDE PANEL SETTINGS ---
	@ConfigItem(keyName = "panelFont", name = "Panel Font", description = "Choose the font style for the entire side panel", position = 1, section = sidePanelSection)
	default PanelFontType panelFont() { return PanelFontType.BEBAS_NEUE; }

	// --- OTHER SETTINGS ---

	@ConfigItem(keyName = "persistLoot", name = "Save Loot Tracker on Close", description = "Remembers your loot and KC even if you close RuneLite", position = 1, section = otherSection)
	default boolean persistLoot() { return true; }

	@ConfigItem(keyName = "autoSaveKills", name = "Auto-save CSV Log", description = "Automatically updates the CSV file after every kill", position = 2, section = otherSection)
	default boolean autoSaveKills() { return true; }

	// --- DEBUG SETTINGS ---

	@ConfigItem(keyName = "debugStateBox", name = "Show Arena and State", description = "Shows the live State Machine data box in the top right", position = 1, section = debugSection)
	default boolean debugStateBox() { return false; }

	@ConfigItem(keyName = "debugArenaInfoBox", name = "Show Arena InfoBox", description = "Shows a green tick / red X InfoBox based on your arena status", position = 2, section = debugSection)
	default boolean debugArenaInfoBox() { return false; }

	@ConfigItem(keyName = "debugHighlightedArena", name = "Show Highlighted arena", description = "Draws the green safe zone tiles on the floor and minimap", position = 3, section = debugSection)
	default boolean debugHighlightedArena() { return false; }
}