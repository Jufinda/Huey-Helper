package com.hueyhelper;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.inject.Provides;
import net.runelite.api.*;
import net.runelite.api.coords.*;
import net.runelite.api.events.*;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.events.NpcLootReceived;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.ItemStack;
import net.runelite.client.plugins.*;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.ui.overlay.infobox.InfoBox;
import net.runelite.client.ui.overlay.infobox.InfoBoxManager;
import net.runelite.client.util.Text;
import net.runelite.http.api.item.ItemPrice;

import javax.imageio.ImageIO;
import javax.inject.Inject;
import javax.swing.SwingUtilities;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.BasicStroke;
import java.awt.RenderingHints;
import java.awt.AlphaComposite;
import java.awt.image.BufferedImage;
import java.io.*;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@PluginDescriptor(name = "Huey Helper", description = "Tracks Huey damage and loot")
public class HueyHelperPlugin extends Plugin {
	private static final int HUEY_FINISHED = 14012;
	private static final int HUEY_BODY_START_ID = 14017;

	private static final Set<Integer> ARENA_TILES = new HashSet<>();
	static {
		int[][] tiles = {
				{56,21}, {57,21}, {61,20}, {60,20}, {59,20}, {57,20}, {58,20}, {56,20}, {55,20}, {54,20},
				{53,18}, {53,19}, {54,19}, {55,19}, {54,18}, {55,18}, {56,19}, {57,19}, {57,18}, {56,18},
				{58,19}, {58,18}, {60,19}, {59,19}, {59,18}, {61,19}, {60,18}, {62,19}, {61,18}, {60,17},
				{59,17}, {58,17}, {57,17}, {56,17}, {55,17}, {54,17}, {53,17}, {53,16}, {54,16}, {55,16},
				{56,16}, {57,16}, {58,16}, {59,16}, {59,15}, {58,15}, {57,15}, {56,15}, {55,15}, {54,15},
				{53,15}, {53,14}, {54,14}, {55,14}, {56,14}, {57,14}, {58,14}, {53,13}, {54,13}, {55,13},
				{56,13}, {57,13}, {57,12}, {57,11}, {56,12}, {55,12}, {55,11}, {56,11}, {54,12}, {54,11},
				{53,12}, {53,11}, {52,11}, {52,10}, {51,10}, {50,9}, {51,9}, {52,9}, {53,10}, {54,10},
				{55,10}, {56,10}, {57,10}, {53,9}, {54,9}, {54,8}, {53,8}, {52,8}, {51,8}, {50,8}, {48,8},
				{49,8}, {47,8}, {46,8}, {45,7}, {54,7}, {53,7}, {52,7}, {51,7}, {50,7}, {49,7}, {48,7}, {47,7},
				{46,7}, {47,6}, {46,6}, {45,6}, {44,7}, {44,6}, {43,7}, {43,6}, {42,5}, {42,6}, {42,7}, {41,7},
				{41,6}, {41,5}, {40,5}, {40,6}, {40,7}, {39,6}, {39,7}, {39,8}, {38,8}, {38,7}, {38,6}, {37,7},
				{37,8}, {37,9}, {38,9}, {38,10}, {39,10}, {36,11}, {37,11}, {38,11}, {39,11}, {40,11}, {41,11},
				{41,10}, {42,11}, {43,11}, {44,11}, {45,11}, {46,11}, {46,12}, {45,12}, {44,12}, {43,12},
				{42,12}, {41,12}, {40,12}, {36,12}, {37,12}, {38,12}, {39,12}, {35,12}, {34,12}, {33,12},
				{32,13}, {33,13}, {34,13}, {35,13}, {36,13}, {37,13}, {38,13}, {39,13}, {40,13}, {41,13},
				{42,13}, {43,13}, {44,13}, {45,13}, {46,13}, {47,13}, {48,13}, {48,14}, {47,14}, {46,14},
				{45,14}, {44,14}, {43,14}, {42,14}, {41,14}, {40,14}, {39,14}, {38,14}, {37,14}, {36,14},
				{35,14}, {34,14}, {33,14}, {33,15}, {33,16}, {33,17}, {33,18}, {33,19}, {33,20}, {32,22},
				{33,22}, {33,23}, {34,23}, {34,24}, {35,24}, {35,23}, {35,25}, {36,25}, {36,24}, {36,23},
				{34,22}, {33,21}, {47,15}, {46,15}, {45,15}, {44,15}, {43,15}, {43,16}, {42,15}, {41,15},
				{40,15}, {39,15}, {38,15}, {37,15}, {34,15}, {35,15}, {36,15}, {34,16}, {35,16}, {36,16},
				{37,16}, {38,16}, {39,16}, {40,16}, {41,16}, {42,16}, {44,16}, {45,16}, {46,16}, {47,16},
				{47,17}, {46,17}, {46,18}, {47,18}, {46,19}, {47,19}, {47,20}, {46,20}, {46,21}, {47,21},
				{47,22}, {46,22}, {46,23}, {46,24}, {46,25}, {47,25}, {45,25}, {45,24}, {45,23}, {45,22},
				{45,21}, {45,20}, {45,19}, {45,18}, {45,17}, {44,18}, {44,17}, {36,26}, {37,26}, {38,26},
				{39,26}, {40,26}, {41,26}, {42,26}, {43,26}, {44,25}, {43,25}, {42,25}, {41,25}, {40,25},
				{37,25}, {38,25}, {39,25}, {37,24}, {37,23}, {38,24}, {38,23}, {39,24}, {39,23}, {40,24},
				{40,23}, {41,24}, {41,23}, {42,24}, {42,23}, {43,24}, {43,23}, {44,24}, {44,23}, {44,22},
				{43,22}, {42,22}, {41,22}, {40,22}, {39,22}, {38,22}, {37,22}, {35,22}, {36,22}, {34,21},
				{34,19}, {34,20}, {34,18}, {34,17}, {35,21}, {44,21}, {44,20}, {44,19}, {43,17}, {42,17},
				{41,17}, {40,17}, {39,17}, {38,17}, {37,17}, {35,17}, {36,17}, {35,18}, {36,18}, {37,18},
				{38,18}, {39,18}, {40,18}, {41,18}, {42,18}, {43,18}, {35,20}, {35,19}, {36,21}, {37,21},
				{36,20}, {36,19}, {37,20}, {37,19}, {38,21}, {38,20}, {38,19}, {39,21}, {39,20}, {39,19},
				{40,21}, {40,20}, {40,19}, {41,21}, {42,21}, {43,21}, {43,20}, {42,20}, {41,20}, {41,19},
				{42,19}, {43,19}, {32,14}, {32,15}, {31,15}, {31,16}, {38,27}, {39,27}, {40,27}, {41,27},
				{42,27}, {43,27}, {52,13}, {58,12}, {48,9}, {52,6}
		};
		for (int[] tile : tiles) {
			int wx = 1472 + tile[0];
			int wy = 3264 + tile[1];
			ARENA_TILES.add((wx << 16) | wy);
		}
	}

	@Inject private Client client;
	@Inject private ItemManager itemManager;
	@Inject private HueyHelperConfig config;
	@Inject private OverlayManager overlayManager;
	@Inject private InfoBoxManager infoBoxManager;
	@Inject private ClientToolbar clientToolbar;
	@Inject private Gson gson;
	@Inject private ConfigManager configManager;
	@Inject private HueyHelperOverlay overlay;
	@Inject private HueyHelperDebugOverlay debugOverlay;
	@Inject private ClientThread clientThread;

	@Inject private HueyHelperSceneOverlay sceneOverlay; // The new one we just made!

	private HueyPanel panel;
	private NavigationButton navButton;
	private boolean fightEnded;
	private boolean wasInArena;

	private InfoBox arenaInfoBox;
	private InfoBox eligibilityInfoBox;
	private BufferedImage greenTickImg;
	private BufferedImage redCrossImg;
	private BufferedImage customIcon;
	private final BufferedImage dynamicEligibilityCanvas = new BufferedImage(35, 35, BufferedImage.TYPE_INT_ARGB);
	private int cachedP1 = -1;
	private int cachedP2 = -1;
	private int cachedParts = -1;

	private long sessionStartTime = -1;
	private String lastKillTimeStr = "00:00";
	private long lastKillDurationMs = 0;

	private int currentFightPhase = 0;
	private int bodyHitsplatDamage = 0;
	private int headTailHitsplatDamage = 0;
	private int bodyXpAccumulator = 0;
	private int headXpAccumulator = 0;
	private int tailXpAccumulator = 0;
	private int previousHpXp = -1;
	private int lastHueyId = -1;

	private boolean currentFightIneligible = false;
	private boolean currentFightPenalized = false;

	public final List<KillRecord> killLog = new ArrayList<>();
	public final Map<Integer, Integer> sessionLootTracker = new ConcurrentHashMap<>();
	public final Map<String, Integer> itemLookupCache = new ConcurrentHashMap<>();
	public final Map<Integer, String> itemNameCache = new ConcurrentHashMap<>();
	private final Map<String, Integer> pendingLootDrops = new ConcurrentHashMap<>();
	private int pendingLootDropsTimer = 0;

	public int persistentKillCount = 0;
	private KillRecord lastLoggedKill = null;
	private int currentKc = 0;
	private final List<String> currentPersonalLoot = new ArrayList<>();
	private final List<String> currentGroupLoot = new ArrayList<>();

	@Provides HueyHelperConfig provideConfig(ConfigManager cm) { return cm.getConfig(HueyHelperConfig.class); }
	public HueyHelperConfig getConfig() { return config; }

	// --- GETTERS ---
	public int getCurrentFightPhase() { return currentFightPhase; }
	public Set<Integer> getArenaTiles() { return ARENA_TILES; }

	// --- NEW: BOSS SCANNER FOR OVERLAY ---
	public List<NPC> getHueycoatlBodyParts() {
		List<NPC> bodyParts = new ArrayList<>();
		for (NPC npc : client.getNpcs()) {
			if (npc.getName() != null && npc.getName().toLowerCase().contains("hueycoatl")) {
				// >= HUEY_BODY_START_ID ensures it grabs ALL body pieces, but ignores the head/tail
				if (npc.getId() >= HUEY_BODY_START_ID) {
					bodyParts.add(npc);
				}
			}
		}
		return bodyParts;
	}

	// --- DYNAMIC SESSION STATS ---
	public int getSessionKills() {
		int count = 0;
		for (KillRecord r : killLog) {
			if (r.eligible) count++;
		}
		return count;
	}

	public long getAvgKillTime() {
		long total = 0;
		int count = 0;
		for (KillRecord r : killLog) {
			if (r.eligible && r.killDurationMs > 0) {
				total += r.killDurationMs;
				count++;
			}
		}
		return count > 0 ? (total / count) : 0;
	}

	public long getFastestKillTime() {
		long fastest = Long.MAX_VALUE;
		for (KillRecord r : killLog) {
			if (r.eligible && r.killDurationMs > 0 && r.killDurationMs < fastest) {
				fastest = r.killDurationMs;
			}
		}
		return fastest;
	}

	public double getKph() {
		int kills = getSessionKills();
		if (kills == 0 || sessionStartTime == -1) return 0.0;
		long duration = System.currentTimeMillis() - sessionStartTime;
		if (duration <= 0) return 0.0;
		return ((double) kills / duration) * 3600000.0;
	}

	public String formatTime(long ms) {
		if (ms <= 0 || ms == Long.MAX_VALUE) return "00:00";
		long seconds = (ms / 1000) % 60;
		long minutes = (ms / (1000 * 60)) % 60;
		return String.format("%02d:%02d", minutes, seconds);
	}

	public int getBodyDamage() {
		int xpDmg = (int) Math.round(bodyXpAccumulator / 1.533333);
		return Math.max(xpDmg, bodyHitsplatDamage);
	}
	public int getHeadTailTotal() {
		int headXpDmg = (int) Math.round(headXpAccumulator / 2.133333);
		int tailXpDmg = (int) Math.round(tailXpAccumulator / 1.566666);
		return Math.max(headXpDmg + tailXpDmg, headTailHitsplatDamage);
	}

	public ItemManager getItemManager() { return itemManager; }
	public ClientThread getClientThread() { return clientThread; }

	public String getAccountName() {
		Player p = client.getLocalPlayer();
		if (p != null && p.getName() != null) {
			return p.getName().replaceAll("[^a-zA-Z0-9 ]", "").replace(" ", "_");
		}
		return "Unknown_Account";
	}

	public File getLogDir() {
		return new File(System.getProperty("user.home"), ".runelite/Huey_Helper_Session_Logs/" + getAccountName());
	}

	@Override
	protected void startUp() {
		loadPersistentData();
		overlayManager.add(overlay);
		overlayManager.add(debugOverlay);
		overlayManager.add(sceneOverlay);

		panel = new HueyPanel(this);
		greenTickImg = createTickCrossIcon(Color.GREEN, true);
		redCrossImg = createTickCrossIcon(Color.RED, false);
		try (InputStream stream = getClass().getResourceAsStream("/infobox_icon.png")) {
			if (stream != null) customIcon = ImageIO.read(stream);
		} catch (Exception ignored) {}
		BufferedImage navIcon = null;
		try (InputStream stream = getClass().getResourceAsStream("/huey_icon.png")) {
			if (stream != null) navIcon = ImageIO.read(stream);
		} catch (Exception ignored) {}
		if (navIcon == null) navIcon = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
		navButton = NavigationButton.builder().tooltip("Huey Helper").icon(navIcon).panel(panel).build();
		clientToolbar.addNavigation(navButton);
		updateInfoBoxes();
	}

	@Override
	protected void shutDown() {
		overlayManager.remove(overlay);
		overlayManager.remove(debugOverlay);
		overlayManager.remove(sceneOverlay);

		clientToolbar.removeNavigation(navButton);
		if (arenaInfoBox != null) infoBoxManager.removeInfoBox(arenaInfoBox);
		if (eligibilityInfoBox != null) infoBoxManager.removeInfoBox(eligibilityInfoBox);
	}

	private BufferedImage createTickCrossIcon(Color color, boolean isTick) {
		BufferedImage img = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = img.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setColor(color);
		g.setStroke(new BasicStroke(4, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
		if (isTick) { g.drawLine(8, 16, 14, 24); g.drawLine(14, 24, 26, 8); }
		else { g.drawLine(8, 8, 24, 24); g.drawLine(24, 8, 8, 24); }
		g.dispose();
		return img;
	}

	private void redrawEligibilityCanvas(int p1, int p2, int parts) {
		Graphics2D g = dynamicEligibilityCanvas.createGraphics();
		g.setComposite(AlphaComposite.Clear);
		g.fillRect(0, 0, 35, 35);
		g.setComposite(AlphaComposite.SrcOver);
		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

		if (customIcon != null) g.drawImage(customIcon, 3, 3, 29, 29, null);

		Color c1 = Color.RED; Color c2 = Color.RED;
		if (parts >= 6) { if (p1 >= 25) { c1 = Color.GREEN; c2 = Color.GREEN; } else if (p2 >= 75) { c2 = Color.GREEN; } }
		else { if (p1 >= 25) c1 = Color.GREEN; if (p2 >= 75) c2 = Color.GREEN; }

		g.setFont(FontManager.getRunescapeSmallFont());
		FontMetrics fm = g.getFontMetrics();
		String text1 = "P 1"; String text2 = "P 2";
		int x1 = (35 - fm.stringWidth(text1)) / 2; int x2 = (35 - fm.stringWidth(text2)) / 2;

		g.setColor(Color.BLACK); g.drawString(text1, x1 + 1, 16 + 1); g.drawString(text2, x2 + 1, 31 + 1);
		g.setColor(c1); g.drawString(text1, x1, 16); g.setColor(c2); g.drawString(text2, x2, 31);
		g.dispose();
	}

	private void updateInfoBoxes() {
		boolean inArena = isInArena();
		if (config.debugArenaInfoBox()) {
			if (arenaInfoBox == null) {
				arenaInfoBox = new InfoBox(inArena ? greenTickImg : redCrossImg, this) {
					@Override public String getText() { return ""; }
					@Override public Color getTextColor() { return Color.WHITE; }
					@Override public String getTooltip() { return isInArena() ? "Inside Arena" : "Outside Arena"; }
				};
				infoBoxManager.addInfoBox(arenaInfoBox);
			}
			arenaInfoBox.setImage(inArena ? greenTickImg : redCrossImg);
		} else if (arenaInfoBox != null) { infoBoxManager.removeInfoBox(arenaInfoBox); arenaInfoBox = null; }

		if (config.showEligibilityInfoBox() && inArena) {
			int p1 = getBodyDamage(); int p2 = getHeadTailTotal(); int parts = getParticipantCount();
			if (cachedP1 != p1 || cachedP2 != p2 || cachedParts != parts) {
				redrawEligibilityCanvas(p1, p2, parts);
				cachedP1 = p1; cachedP2 = p2; cachedParts = parts;
			}
			if (eligibilityInfoBox == null) {
				eligibilityInfoBox = new InfoBox(dynamicEligibilityCanvas, this) {
					@Override public String getText() { return ""; }
					@Override public Color getTextColor() { return Color.WHITE; }
					@Override public String getTooltip() {
						boolean isElig = isEligible(getParticipantCount(), getBodyDamage(), getHeadTailTotal());
						DecimalFormat df = new DecimalFormat("#.##");
						return "Huey Helper</br>" +
								"Phase 1: " + getBodyDamage() + "</br>" +
								"Phase 2: " + getHeadTailTotal() + "</br>" +
								"Status: " + (isElig ? "Qualified" : "Not Qualified") + "</br></br>" +
								"KPH: " + df.format(getKph()) + "</br>" +
								"Kills: " + getSessionKills() + "</br>" +
								"Avg Kill: " + formatTime(getAvgKillTime()) + "</br>" +
								"Fastest Kill: " + formatTime(getFastestKillTime());
					}
				};
				infoBoxManager.addInfoBox(eligibilityInfoBox);
			}
		} else if (eligibilityInfoBox != null) { infoBoxManager.removeInfoBox(eligibilityInfoBox); eligibilityInfoBox = null; cachedP1 = -1; }
	}

	public void loadPersistentData() {
		String json = configManager.getConfiguration("hueyhelper", "savedLoot");
		if (json != null) { try { sessionLootTracker.putAll(gson.fromJson(json, new TypeToken<Map<Integer, Integer>>(){}.getType())); } catch (Exception ignored) {} }
		String kcStr = configManager.getConfiguration("hueyhelper", "savedKc");
		persistentKillCount = Integer.parseInt(kcStr != null ? kcStr : "0");
	}

	public void savePersistentData() {
		if (config.persistLoot()) {
			configManager.setConfiguration("hueyhelper", "savedLoot", gson.toJson(sessionLootTracker));
			configManager.setConfiguration("hueyhelper", "savedKc", persistentKillCount);
		}
	}

	public void clearLoot() {
		sessionLootTracker.clear();
		persistentKillCount = 0;
		configManager.unsetConfiguration("hueyhelper", "savedLoot");
		configManager.unsetConfiguration("hueyhelper", "savedKc");
	}

	public void clearLog() {
		killLog.clear(); sessionStartTime = -1; lastKillTimeStr = "00:00"; lastKillDurationMs = 0;
	}

	private void startFightClock() {
		if (sessionStartTime == -1) sessionStartTime = System.currentTimeMillis();
	}

	public void resetFight() {
		bodyHitsplatDamage = 0; headTailHitsplatDamage = 0; bodyXpAccumulator = 0; headXpAccumulator = 0; tailXpAccumulator = 0;
		fightEnded = false; currentFightIneligible = false; currentFightPenalized = false;
		currentPersonalLoot.clear(); currentGroupLoot.clear(); currentFightPhase = 0; lastHueyId = -1; cachedP1 = -1;
		lastKillTimeStr = "00:00"; lastKillDurationMs = 0;
	}

	@SuppressWarnings("unused")
	@Subscribe public void onConfigChanged(ConfigChanged event) {
		if (event.getGroup().equals("hueyhelper")) {
			if (event.getKey().equals("panelFont") && panel != null) panel.updateAllFonts();
			updateInfoBoxes();
		}
	}

	@SuppressWarnings("unused")
	@Subscribe public void onGameStateChanged(GameStateChanged event) {
		if (event.getGameState() == GameState.LOGGED_IN) previousHpXp = client.getSkillExperience(Skill.HITPOINTS);
		else previousHpXp = -1;
	}

	@SuppressWarnings("unused")
	@Subscribe public void onInteractingChanged(InteractingChanged event) {
		if (event.getSource() == client.getLocalPlayer() && event.getTarget() instanceof NPC) {
			NPC npc = (NPC) event.getTarget();
			if (npc.getName() != null && npc.getName().toLowerCase().contains("hueycoatl")) {
				lastHueyId = npc.getId();
				if (currentFightPhase == 2 && lastHueyId >= 14009 && lastHueyId <= 14016) currentFightPhase = 3;
			}
		}
	}

	@SuppressWarnings("unused")
	@Subscribe public void onGameTick(GameTick event) {
		boolean currentlyInArena = isInArena();
		// We removed the resetFight() tripwire here so dodging doesn't wipe your damage!
		wasInArena = currentlyInArena;
		updateInfoBoxes();

		Player p = client.getLocalPlayer();
		if (p != null) {
			if (previousHpXp == -1) previousHpXp = client.getSkillExperience(Skill.HITPOINTS);
			Actor interacting = p.getInteracting();
			if (interacting instanceof NPC) {
				NPC npc = (NPC) interacting;
				if (npc.getName() != null && npc.getName().toLowerCase().contains("hueycoatl")) {
					lastHueyId = npc.getId();
				}
			}
		}

		if (pendingLootDropsTimer > 0) {
			pendingLootDropsTimer--;
			Iterator<Map.Entry<String, Integer>> it = pendingLootDrops.entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry<String, Integer> entry = it.next();
				Integer id = itemLookupCache.get(entry.getKey());
				if (id != null) { processPersonalLoot(id, entry.getValue(), itemNameCache.get(id)); it.remove(); }
			}
		}
	}

	@SuppressWarnings("unused")
	@Subscribe public void onStatChanged(StatChanged event) {
		if (event.getSkill() == Skill.HITPOINTS) {
			int currentXp = event.getXp();
			if (previousHpXp != -1) {
				int diff = currentXp - previousHpXp;
				if (diff > 0 && currentFightPhase > 0) addXpDamage(diff); // Swapped wasInArena for currentFightPhase
			}
			previousHpXp = currentXp;
		}
	}

	@SuppressWarnings("unused")
	@Subscribe public void onFakeXpDrop(FakeXpDrop event) {
		if (event.getSkill() == Skill.HITPOINTS && currentFightPhase > 0) addXpDamage(event.getXp()); // Swapped here too
	}

	private void addXpDamage(int hpXpDrop) {
		if (currentFightPhase == 0) {
			if (lastHueyId >= HUEY_BODY_START_ID) { currentFightPhase = 1; startFightClock(); }
			else if (lastHueyId >= 14009) { currentFightPhase = 3; startFightClock(); }
		}
		if (currentFightPhase == 1) bodyXpAccumulator += hpXpDrop;
		else if (currentFightPhase == 3) { if (lastHueyId >= 14014 && lastHueyId <= 14016) tailXpAccumulator += hpXpDrop; else headXpAccumulator += hpXpDrop; }
	}

	@SuppressWarnings("unused")
	@Subscribe public void onHitsplatApplied(HitsplatApplied e) {
		if (e.getHitsplat().isMine() && e.getActor() instanceof NPC) {
			NPC npc = (NPC) e.getActor();
			int id = npc.getId();
			if (npc.getName() != null && npc.getName().toLowerCase().contains("hueycoatl")) {
				if (currentFightPhase <= 2 && id >= 14009 && id <= 14016) { currentFightPhase = 3; startFightClock(); }
				else if (currentFightPhase == 0 && id >= HUEY_BODY_START_ID) { currentFightPhase = 1; startFightClock(); }
				if (currentFightPhase == 1 && id >= HUEY_BODY_START_ID) bodyHitsplatDamage += e.getHitsplat().getAmount();
				else if (currentFightPhase == 3 && id >= 14009 && id <= 14016) headTailHitsplatDamage += e.getHitsplat().getAmount();
			}
		}
	}

	@SuppressWarnings("unused")
	@Subscribe public void onItemSpawned(ItemSpawned event) {
		TileItem item = event.getItem();
		if (item != null) {
			int id = item.getId();
			ItemComposition comp = itemManager.getItemComposition(id);
			if (comp.getName() != null) {
				String cleanName = comp.getName().toLowerCase().trim();
				itemLookupCache.put(cleanName, id);
				itemNameCache.put(id, comp.getName());
			}
		}
	}

	@SuppressWarnings({"unused", "deprecation"})
	@Subscribe public void onItemContainerChanged(ItemContainerChanged event) {
		if (event.getContainerId() == InventoryID.INVENTORY.getId()) {
			for (Item item : event.getItemContainer().getItems()) {
				if (item.getId() != -1 && item.getId() != 0) {
					ItemComposition comp = itemManager.getItemComposition(item.getId());
					if (comp.getName() != null) {
						itemLookupCache.put(comp.getName().toLowerCase().trim(), item.getId());
						itemNameCache.put(item.getId(), comp.getName());
					}
				}
			}
		}
	}

	@SuppressWarnings("unused")
	@Subscribe public void onNpcChanged(NpcChanged e) {
		if (e.getNpc().getId() == HUEY_FINISHED && !fightEnded) {
			fightEnded = true;
			logKill();
		} else if (fightEnded && e.getNpc().getId() >= HUEY_BODY_START_ID) {
			// If we ALREADY have damage, we are clearly mid-fight. Don't reset!
			if (bodyXpAccumulator > 0 || headTailHitsplatDamage > 0) {
				fightEnded = false;
			} else {
				resetFight();
				currentFightPhase = 1;
				startFightClock();
			}
		}
	}

	private void logKill() {
		int finalBodyDmg = getBodyDamage(); int finalHeadTailDmg = getHeadTailTotal();
		if ((finalBodyDmg + finalHeadTailDmg) == 0) return;

		int pCount = getParticipantCount();
		KillRecord r = new KillRecord();
		r.date = new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date());
		r.participants = pCount; r.bodyDmg = finalBodyDmg; r.headTailDmg = finalHeadTailDmg;
		r.eligible = isEligible(pCount, finalBodyDmg, finalHeadTailDmg);
		r.killTime = lastKillTimeStr;
		r.killDurationMs = lastKillDurationMs;

		if (currentFightIneligible) r.eligible = false;
		else if (currentFightPenalized) r.eligible = true;
		r.penaltyMessageReceived = currentFightPenalized;

		r.contribution = Math.min(Math.max((finalBodyDmg + finalHeadTailDmg)/4050.0, 0.05), 1.0);
		r.kc = currentKc;
		r.loot = currentPersonalLoot.isEmpty() ? "Waiting..." : String.join(" | ", currentPersonalLoot);
		r.totalLoot = currentGroupLoot.isEmpty() ? "Waiting..." : String.join(" | ", currentGroupLoot);

		killLog.add(r); lastLoggedKill = r;
		if (r.eligible) persistentKillCount++;
		savePersistentData();

		SwingUtilities.invokeLater(() -> panel.updateKillLogUI());
		panel.updateLootTrackerUI();
		new Thread(() -> {
			try { Thread.sleep(15000); } catch (InterruptedException ignored) {}
			if (!r.eligible) {
				if (r.loot.equals("Waiting...")) r.loot = "None";
				if (r.totalLoot.equals("Waiting...")) r.totalLoot = "N/A";
			} else {
				if (r.loot.equals("Waiting...")) r.loot = "None";
				if (r.totalLoot.equals("Waiting...")) r.totalLoot = "None";
			}
			if (config.autoSaveKills()) writeToCSV("HueyHelperLog_" + getAccountName() + "_autosave.csv", r, true);
			SwingUtilities.invokeLater(() -> panel.updateKillLogUI());
		}).start();
	}

	@SuppressWarnings("unused")
	@Subscribe public void onNpcLootReceived(NpcLootReceived event) {
		NPC npc = event.getNpc();
		if (npc != null && (npc.getId() == HUEY_FINISHED || (npc.getName() != null && npc.getName().toLowerCase().contains("hueycoatl")))) {
			for (ItemStack item : event.getItems()) {
				int id = item.getId();
				int qty = item.getQuantity();
				String name = itemManager.getItemComposition(id).getName();
				itemNameCache.put(id, name);
				processPersonalLoot(id, qty, name);
			}
		}
	}

	@SuppressWarnings("unused")
	@Subscribe public void onChatMessage(ChatMessage e) {
		if (e.getType() == ChatMessageType.PUBLICCHAT || e.getType() == ChatMessageType.PRIVATECHAT) return;
		String cleanMessage = Text.removeTags(e.getMessage());
		String lowerMsg = cleanMessage.toLowerCase();

		if (lowerMsg.contains("fight duration:")) {
			lastKillTimeStr = cleanMessage.substring(cleanMessage.indexOf(":") + 1).split("\\.")[0].trim();
			String timeOnly = cleanMessage.replaceAll(".*uration: ([0-9:]+).*", "$1");
			String[] parts = timeOnly.split(":");
			long duration = 0;
			if (parts.length == 2) duration = (Long.parseLong(parts[0]) * 60000) + (Long.parseLong(parts[1]) * 1000);
			else if (parts.length == 3) duration = (Long.parseLong(parts[0]) * 3600000) + (Long.parseLong(parts[1]) * 60000) + (Long.parseLong(parts[2]) * 1000);

			lastKillDurationMs = duration;

			if (lastLoggedKill != null && System.currentTimeMillis() - lastLoggedKill.timestamp < 30000) {
				lastLoggedKill.killTime = lastKillTimeStr;
				lastLoggedKill.killDurationMs = duration;
				SwingUtilities.invokeLater(() -> panel.updateKillLogUI());
			}
		}

		if (lowerMsg.contains("begins to awaken")) { resetFight(); currentFightPhase = 1; startFightClock(); }
		if (lowerMsg.contains("hueycoatl shifts") || lowerMsg.contains("opening a path up")) currentFightPhase = 2;

		if (lowerMsg.contains("not eligible to receive")) {
			currentFightIneligible = true;
			if (lastLoggedKill != null && System.currentTimeMillis() - lastLoggedKill.timestamp < 30000) {
				if (lastLoggedKill.eligible) persistentKillCount = Math.max(0, persistentKillCount - 1);
				lastLoggedKill.eligible = false; lastLoggedKill.loot = "None";
				if (lastLoggedKill.totalLoot.equals("Waiting...")) lastLoggedKill.totalLoot = "N/A";
				savePersistentData(); panel.updateLootTrackerUI(); SwingUtilities.invokeLater(() -> panel.updateKillLogUI());
			}
		} else if (lowerMsg.contains("fewer rewards")) {
			currentFightPenalized = true;
			if (lastLoggedKill != null && System.currentTimeMillis() - lastLoggedKill.timestamp < 30000) {
				lastLoggedKill.penaltyMessageReceived = true;
				if (!lastLoggedKill.eligible) persistentKillCount++;
				lastLoggedKill.eligible = true;
				savePersistentData(); panel.updateLootTrackerUI(); SwingUtilities.invokeLater(() -> panel.updateKillLogUI());
			}
		}

		if (lowerMsg.contains("hueycoatl kill count is")) {
			try { currentKc = Integer.parseInt(cleanMessage.replaceAll("[^0-9]", ""));
				if (lastLoggedKill != null && System.currentTimeMillis() - lastLoggedKill.timestamp < 30000) { lastLoggedKill.kc = currentKc; lastLoggedKill.eligible = true; SwingUtilities.invokeLater(() -> panel.updateKillLogUI()); }
			} catch (Exception ignored) {}
		}

		if (lowerMsg.contains("hueycoatl dropped:")) {
			int idx = lowerMsg.indexOf("dropped:");
			String groupDrop = cleanMessage.substring(idx + 8).trim().replace(",", "");
			currentGroupLoot.add(groupDrop);
			if (lastLoggedKill != null && System.currentTimeMillis() - lastLoggedKill.timestamp < 30000) {
				if (lastLoggedKill.totalLoot.equals("Waiting...") || lastLoggedKill.totalLoot.equals("None") || lastLoggedKill.totalLoot.equals("N/A")) lastLoggedKill.totalLoot = groupDrop;
				else if (!lastLoggedKill.totalLoot.contains(groupDrop)) lastLoggedKill.totalLoot += " | " + groupDrop;
			}
			SwingUtilities.invokeLater(() -> panel.updateKillLogUI());
		}

		int receivedIdx = lowerMsg.indexOf("you received:");
		if (receivedIdx != -1) {
			String drop = cleanMessage.substring(receivedIdx + 13).trim();
			int qty = 1;
			if (drop.toLowerCase().contains(" x ")) {
				String[] parts = drop.split("(?i) x ", 2);
				try { qty = Integer.parseInt(parts[0].replaceAll("[^0-9]", "")); } catch (Exception ignored) {}
				if (parts.length > 1) drop = parts[1].trim();
			}
			String nameKey = drop.toLowerCase();
			Integer cachedId = itemLookupCache.get(nameKey);
			if (cachedId != null) {
				processPersonalLoot(cachedId, qty, drop);
			} else {
				try {
					List<ItemPrice> results = itemManager.search(drop);
					if (results != null && !results.isEmpty()) {
						int searchId = -1;

						// Loop through the search results to find the EXACT match
						for (ItemPrice result : results) {
							if (result.getName().equalsIgnoreCase(drop)) {
								searchId = result.getId();
								break;
							}
						}

						// If it somehow can't find an exact match, fallback to the first result
						if (searchId == -1) {
							searchId = results.get(0).getId();
						}

						itemLookupCache.put(nameKey, searchId);
						itemNameCache.put(searchId, drop);
						processPersonalLoot(searchId, qty, drop);
						return;
					}
				} catch (Exception ignored) {}
				if (nameKey.contains("huberte")) processPersonalLoot(29489, qty, "Huberte");
				else if (nameKey.contains("clue scroll")) processPersonalLoot(12158, qty, "Clue scroll");
				else { pendingLootDrops.put(nameKey, pendingLootDrops.getOrDefault(nameKey, 0) + qty); pendingLootDropsTimer = 100; }
			}
		}
	}

	private void processPersonalLoot(int rawId, int qty, String exactName) {
		int id = itemManager.canonicalize(rawId);
		String name = itemManager.getItemComposition(id).getName().replace(",", "");
		String dropStr = qty + " x " + name;

		if (currentPersonalLoot.contains(dropStr)) return;

		currentPersonalLoot.add(dropStr);
		sessionLootTracker.put(id, sessionLootTracker.getOrDefault(id, 0) + qty);

		if (lastLoggedKill != null && System.currentTimeMillis() - lastLoggedKill.timestamp < 30000) {
			if (!lastLoggedKill.eligible) persistentKillCount++;
			lastLoggedKill.eligible = true;
			if (lastLoggedKill.loot.equals("Waiting...") || lastLoggedKill.loot.equals("None")) lastLoggedKill.loot = dropStr;
			else if (!lastLoggedKill.loot.contains(dropStr)) lastLoggedKill.loot += " | " + dropStr;
		}
		savePersistentData();
		panel.updateLootTrackerUI();
		SwingUtilities.invokeLater(() -> panel.updateKillLogUI());
	}

	public void writeToCSV(String fileName, KillRecord singleRecord, boolean append) {
		boolean ignored = getLogDir().mkdirs();
		File file = new File(getLogDir(), fileName);
		boolean isNewFile = !file.exists();
		try (PrintWriter pw = new PrintWriter(new FileWriter(file, append))) {
			if (isNewFile || !append) pw.println("Date,KC,Participants,Phase1Dmg,Phase2Dmg,KillTime,Contribution%,EligibleForLoot,MVP,PersonalLoot,GroupLoot,RareDropRate");
			if (singleRecord != null) pw.println(singleRecord.toCSV());
			else for (KillRecord r : killLog) pw.println(r.toCSV());
		} catch (IOException ignored2) {}
	}

	public boolean isEligible(int pCount, int bDmg, int htDmg) {
		if (pCount < 6) return bDmg >= 25 && htDmg >= 75;
		else return bDmg >= 25 || htDmg >= 75;
	}

	public boolean isInArena() {
		Player lp = client.getLocalPlayer(); if (lp == null) return false;
		LocalPoint localLocation = lp.getLocalLocation(); if (localLocation == null) return false;
		WorldPoint templateWp = WorldPoint.fromLocalInstance(client, localLocation); if (templateWp == null) return false;
		return ARENA_TILES.contains((templateWp.getX() << 16) | templateWp.getY());
	}

	@SuppressWarnings("deprecation")
	public int getParticipantCount() {
		int count = 0;
		for (Player p : client.getPlayers()) {
			LocalPoint loc = p.getLocalLocation();
			if (loc != null) { WorldPoint wp = WorldPoint.fromLocalInstance(client, loc); if (wp != null && ARENA_TILES.contains((wp.getX() << 16) | wp.getY())) count++; }
		}
		return Math.min(count, 20);
	}

	public static class KillRecord {
		public String date; public long timestamp = System.currentTimeMillis(); public int kc = 0;
		public int participants; public int bodyDmg; public int headTailDmg; public String killTime = "00:00";
		public long killDurationMs = 0;
		public double contribution; public boolean eligible; public String loot = "Waiting...";
		public String totalLoot = "Waiting..."; public boolean penaltyMessageReceived = false;

		public String getContributionString() {
			if (!eligible) return "0% (Ineligible)";
			if (penaltyMessageReceived) return "5% (95% Penalty)";
			boolean isMvp = participants > 1 && loot.toLowerCase().contains("big bones");
			DecimalFormat df = new DecimalFormat("#.##");
			double calc = Math.min((bodyDmg + headTailDmg) / 4050.0, 1.0);
			boolean passedBoth = (bodyDmg >= 25 && headTailDmg >= 75);

			if (passedBoth) {
				if (calc <= 0.05 && !isMvp) return "5% (Minimum floor)";
				else return df.format(calc * 100) + "% " + (isMvp ? "(MVP 10% Bonus)" : "(Assumed)");
			} else {
				if (participants >= 6) {
					if (calc <= 0.05 && !isMvp) return "5% (Mass Protection)";
					else return df.format(calc * 100) + "% " + (isMvp ? "(MVP 10% Bonus)" : "(Assumed)");
				} else return "5% (95% Penalty)";
			}
		}

		public String getRareDropRateString() {
			if (!eligible) return "N/A";
			String lowerLoot = loot.toLowerCase();
			boolean hasHide = lowerLoot.contains("hueycoatl hide"); boolean hasTome = lowerLoot.contains("tome of earth");
			boolean hasWand = lowerLoot.contains("hueycoatl wand"); boolean hasPet = lowerLoot.contains("huberte") || lowerLoot.contains("funny feeling");
			if (!hasHide && !hasTome && !hasWand && !hasPet) return "N/A";

			double dropMathContrib = Math.max(Math.min((bodyDmg + headTailDmg) / 4050.0, 1.0), 0.05);
			DecimalFormat df = new DecimalFormat("#.##");
			List<String> rates = new ArrayList<>();
			if (hasHide) rates.add("Hide: 1/" + df.format(28.64 / dropMathContrib));
			if (hasTome) rates.add("Tome: 1/" + df.format(90.0 / dropMathContrib));
			if (hasWand) rates.add("Wand: 1/" + df.format(105.0 / dropMathContrib));
			if (hasPet) {
				double petPenalty = (bodyDmg >= 25 && !penaltyMessageReceived) ? 1.0 : 20.0;
				rates.add("Pet: 1/" + df.format((400.0 / dropMathContrib) * petPenalty));
			}
			return String.join(" | ", rates);
		}

		public String toCSV() {
			String actualPercent = getContributionString().split(" ")[0];
			String isMvp = (participants > 1 && loot.toLowerCase().contains("big bones")) ? "Yes" : "No";
			String safePersonalLoot = loot.replace(",", "");
			String safeGroupLoot = totalLoot.replace(",", "");

			return date + "," +
					(eligible ? kc : "Failed") + "," +
					participants + "," +
					bodyDmg + "," +
					headTailDmg + "," +
					killTime + "," +
					actualPercent + "," +
					eligible + "," +
					isMvp + "," +
					safePersonalLoot + "," +
					safeGroupLoot + "," +
					getRareDropRateString();
		}
	}
}