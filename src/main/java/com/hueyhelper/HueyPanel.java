package com.hueyhelper;

import net.runelite.api.ItemComposition;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.PluginPanel;
import net.runelite.client.util.AsyncBufferedImage;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class HueyPanel extends PluginPanel {
    private final HueyHelperPlugin plugin;

    private final JLabel participantsLabel = new JLabel("01/20 (Solo)");
    private final JLabel bodyLabel = new JLabel("0 (0%)");
    private final JLabel headTailLabel = new JLabel("0 (0%)");

    private final JLabel teamLeftLabel = new JLabel("Team Size:");
    private final JLabel teamRightLabel = new JLabel("25 Ph1 AND 75 Ph2 dmg");
    private final JLabel p1WarnLeftLabel = new JLabel("Phase 1:");
    private final JLabel p1WarnRightLabel = new JLabel("25+ dmg needed");
    private final JLabel p2WarnLeftLabel = new JLabel("Phase 2:");
    private final JLabel p2WarnRightLabel = new JLabel("75+ dmg needed");

    private final JLabel hideLabel = new JLabel("N/A");
    private final JLabel tomeLabel = new JLabel("N/A");
    private final JLabel wandLabel = new JLabel("N/A");
    private final JLabel petLabel = new JLabel("N/A");

    private final JLabel kphLabel = new JLabel("0.0");
    private final JLabel killsLabel = new JLabel("0");
    private final JLabel avgKillLabel = new JLabel("00:00");
    private final JLabel fastestKillLabel = new JLabel("00:00");

    private final JTextArea killLogArea = new JTextArea();
    private final JPanel lootContainer = new JPanel();

    private final List<Component> dynamicFontComponents = new ArrayList<>();

    // IntelliJ warning fix: Variables declared as final
    private final Font quillFont;
    private final Font quillCapsFont;
    private final Font barbarianFont;
    private final Font bebasNeueFont;

    private static final int MAX_BODY_HP = 1250;
    private static final int MAX_HEAD_TAIL_HP = 2800;
    private final DecimalFormat df = new DecimalFormat("#.##");

    private static class LootItemData {
        int id, qty;
        long stackValue;
        AsyncBufferedImage icon;
        String name;
        LootItemData(int id, int qty, long stackValue, AsyncBufferedImage icon, String name) {
            this.id = id; this.qty = qty; this.stackValue = stackValue; this.icon = icon; this.name = name;
        }
    }

    public HueyPanel(HueyHelperPlugin plugin) {
        this.plugin = plugin;

        // Load the custom fonts without triggering the 'float size' warning
        quillFont = loadCustomFontFromResource("/fonts/Quill.ttf");
        quillCapsFont = loadCustomFontFromResource("/fonts/Quill_Caps.ttf");
        barbarianFont = loadCustomFontFromResource("/fonts/Barbarian_Assault.ttf");
        bebasNeueFont = loadCustomFontFromResource("/fonts/Bebas_Neue.ttf");

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(new EmptyBorder(10, 10, 10, 10));
        setBackground(ColorScheme.DARK_GRAY_COLOR);

        try {
            InputStream stream = getClass().getResourceAsStream("/huey_logo.png");
            if (stream != null) {
                BufferedImage logoImg = ImageIO.read(stream);
                if (logoImg != null) {
                    JLabel logoLabel = new JLabel(new ImageIcon(logoImg));
                    logoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                    logoLabel.setBorder(new EmptyBorder(0, 0, 15, 0));
                    add(logoLabel);
                }
            }
        } catch (Exception ignored) {}

        JPanel statsPanel = new JPanel();
        statsPanel.setLayout(new BoxLayout(statsPanel, BoxLayout.Y_AXIS));
        statsPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        statsPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        statsPanel.add(buildRow("Participants:", participantsLabel, Color.WHITE));
        statsPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        statsPanel.add(buildRow("Phase 1:", bodyLabel, ColorScheme.LIGHT_GRAY_COLOR));
        statsPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        statsPanel.add(buildRow("Phase 2:", headTailLabel, ColorScheme.LIGHT_GRAY_COLOR));
        statsPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        statsPanel.add(buildDynamicRow(teamLeftLabel, teamRightLabel, ColorScheme.BRAND_ORANGE));
        statsPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        statsPanel.add(buildDynamicRow(p1WarnLeftLabel, p1WarnRightLabel, ColorScheme.PROGRESS_ERROR_COLOR));
        statsPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        statsPanel.add(buildDynamicRow(p2WarnLeftLabel, p2WarnRightLabel, ColorScheme.PROGRESS_ERROR_COLOR));

        add(createCollapsibleHeader("Live Fight Stats", statsPanel));
        add(statsPanel);
        add(Box.createRigidArea(new Dimension(0, 5)));

        JPanel ratesPanel = new JPanel();
        ratesPanel.setLayout(new BoxLayout(ratesPanel, BoxLayout.Y_AXIS));
        ratesPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        ratesPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        ratesPanel.add(buildRow("Hide:", hideLabel, ColorScheme.LIGHT_GRAY_COLOR));
        ratesPanel.add(buildRow("Tome:", tomeLabel, ColorScheme.LIGHT_GRAY_COLOR));
        ratesPanel.add(buildRow("Wand:", wandLabel, ColorScheme.LIGHT_GRAY_COLOR));
        ratesPanel.add(buildRow("Pet:", petLabel, ColorScheme.LIGHT_GRAY_COLOR));

        add(createCollapsibleHeader("Rare Drop Rates", ratesPanel));
        add(ratesPanel);
        add(Box.createRigidArea(new Dimension(0, 5)));

        JPanel sessionStatsPanel = new JPanel();
        sessionStatsPanel.setLayout(new BoxLayout(sessionStatsPanel, BoxLayout.Y_AXIS));
        sessionStatsPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        sessionStatsPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        sessionStatsPanel.add(buildRow("KPH:", kphLabel, ColorScheme.LIGHT_GRAY_COLOR));
        sessionStatsPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        sessionStatsPanel.add(buildRow("Kills:", killsLabel, ColorScheme.LIGHT_GRAY_COLOR));
        sessionStatsPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        sessionStatsPanel.add(buildRow("Avg Kill:", avgKillLabel, ColorScheme.LIGHT_GRAY_COLOR));
        sessionStatsPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        sessionStatsPanel.add(buildRow("Fastest Kill:", fastestKillLabel, ColorScheme.LIGHT_GRAY_COLOR));

        add(createCollapsibleHeader("Session Stats", sessionStatsPanel));
        add(sessionStatsPanel);
        add(Box.createRigidArea(new Dimension(0, 5)));

        lootContainer.setLayout(new BorderLayout());
        lootContainer.setBackground(ColorScheme.DARK_GRAY_COLOR);
        add(createCollapsibleHeader("Loot Tracker", lootContainer));
        add(lootContainer);
        add(Box.createRigidArea(new Dimension(0, 5)));

        JPanel logContainer = new JPanel(new BorderLayout(0, 5));
        logContainer.setBackground(ColorScheme.DARK_GRAY_COLOR);

        killLogArea.setEditable(false);
        killLogArea.setLineWrap(true);
        killLogArea.setWrapStyleWord(true);
        killLogArea.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        killLogArea.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
        killLogArea.setBorder(new EmptyBorder(8, 8, 8, 8));
        dynamicFontComponents.add(killLogArea);

        JScrollPane scrollPane = new JScrollPane(killLogArea);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setPreferredSize(new Dimension(225, 200));
        logContainer.add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new GridLayout(1, 3, 5, 5));
        buttonPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);
        JButton exportBtn = new JButton("Export"), folderBtn = new JButton("Folder"), clearBtn = new JButton("Clear");

        dynamicFontComponents.add(exportBtn);
        dynamicFontComponents.add(folderBtn);
        dynamicFontComponents.add(clearBtn);

        exportBtn.addActionListener(e -> exportLog());
        folderBtn.addActionListener(e -> openLogFolder());
        clearBtn.addActionListener(e -> confirmClear());
        buttonPanel.add(exportBtn);
        buttonPanel.add(folderBtn);
        buttonPanel.add(clearBtn);
        logContainer.add(buttonPanel, BorderLayout.SOUTH);

        add(createCollapsibleHeader("Session Log", logContainer));
        add(logContainer);

        updateAllFonts();

        Timer timer = new Timer(500, e -> updateStats());
        timer.start();
    }

    private Font loadCustomFontFromResource(String path) {
        try (InputStream in = getClass().getResourceAsStream(path)) {
            if (in != null) {
                return Font.createFont(Font.TRUETYPE_FONT, in).deriveFont(16f);
            }
        } catch (Exception ignored) {
            // Failsafe triggers built-in RuneScape font if file is missing
        }
        return FontManager.getRunescapeSmallFont();
    }

    private Font getCustomFontChoice() {
        switch (plugin.getConfig().panelFont()) {
            case REGULAR: return FontManager.getRunescapeFont();
            case BOLD: return FontManager.getRunescapeBoldFont();
            case QUILL: return quillFont;
            case QUILL_CAPS: return quillCapsFont;
            case BARBARIAN: return barbarianFont;
            case BEBAS_NEUE: return bebasNeueFont;
            case SMALL:
            default: return FontManager.getRunescapeSmallFont();
        }
    }

    public void updateAllFonts() {
        Font f = getCustomFontChoice();
        for (Component c : dynamicFontComponents) {
            c.setFont(f);
        }
        revalidate();
        repaint();
        updateLootTrackerUI();
    }

    private JPanel createCollapsibleHeader(String titleText, JComponent contentComponent) {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(ColorScheme.DARK_GRAY_COLOR);
        header.setBorder(new EmptyBorder(5, 0, 5, 0));

        JLabel title = new JLabel(titleText);
        title.setForeground(Color.WHITE);
        title.setHorizontalAlignment(SwingConstants.CENTER);
        dynamicFontComponents.add(title);

        JLabel collapseIcon = new JLabel("▼");
        collapseIcon.setForeground(Color.WHITE);
        collapseIcon.setFont(new Font("SansSerif", Font.PLAIN, 12));
        collapseIcon.setBorder(new EmptyBorder(0, 0, 0, 10));

        JLabel leftSpacer = new JLabel("▼");
        leftSpacer.setForeground(new Color(0,0,0,0));
        leftSpacer.setFont(new Font("SansSerif", Font.PLAIN, 12));
        leftSpacer.setBorder(new EmptyBorder(0, 10, 0, 0));

        header.add(leftSpacer, BorderLayout.WEST);
        header.add(title, BorderLayout.CENTER);
        header.add(collapseIcon, BorderLayout.EAST);

        header.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                boolean isVisible = !contentComponent.isVisible();
                contentComponent.setVisible(isVisible);
                collapseIcon.setText(isVisible ? "▼" : "▶");
                leftSpacer.setText(isVisible ? "▼" : "▶");
                contentComponent.revalidate();
                contentComponent.repaint();
            }
            @Override
            public void mouseEntered(MouseEvent e) {
                header.setCursor(new Cursor(Cursor.HAND_CURSOR));
            }
        });

        return header;
    }

    private JPanel buildRow(String left, JLabel rightLabel, Color rightColor) {
        JPanel row = new JPanel(new BorderLayout());
        row.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        JLabel leftLabel = new JLabel(left);
        leftLabel.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
        rightLabel.setForeground(rightColor);

        dynamicFontComponents.add(leftLabel);
        dynamicFontComponents.add(rightLabel);

        row.add(leftLabel, BorderLayout.WEST);
        row.add(rightLabel, BorderLayout.EAST);
        return row;
    }

    private JPanel buildDynamicRow(JLabel leftLabel, JLabel rightLabel, Color rightColor) {
        JPanel row = new JPanel(new BorderLayout());
        row.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        leftLabel.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
        rightLabel.setForeground(rightColor);

        dynamicFontComponents.add(leftLabel);
        dynamicFontComponents.add(rightLabel);

        row.add(leftLabel, BorderLayout.WEST);
        row.add(rightLabel, BorderLayout.EAST);
        return row;
    }

    private String formatGp(long gp) {
        if (gp >= 1_000_000_000) return df.format(gp / 1_000_000_000.0) + "B";
        if (gp >= 1_000_000) return df.format(gp / 1_000_000.0) + "M";
        if (gp >= 100_000) return (gp / 1000) + "K";
        return String.valueOf(gp);
    }

    private void updateStats() {
        int p = plugin.getParticipantCount();
        int mb = plugin.getBodyDamage();
        int mht = plugin.getHeadTailTotal();
        int mt = mb + mht;

        String teamType = p <= 1 ? "Solo" : (p < 6 ? "Group" : "Mass");
        participantsLabel.setText(String.format("%02d/20 (%s)", p == 0 ? 1 : p, teamType));

        bodyLabel.setText(mb + " (" + df.format(Math.min((double)mb/MAX_BODY_HP, 1) * 100) + "%)");
        headTailLabel.setText(mht + " (" + df.format(Math.min((double)mht/MAX_HEAD_TAIL_HP, 1) * 100) + "%)");

        boolean phase1Passed = mb >= 25;
        boolean phase2Passed = mht >= 75;
        boolean phase2Started = mht > 0;

        teamLeftLabel.setText(teamType + ":");
        teamRightLabel.setForeground(ColorScheme.BRAND_ORANGE);

        if (phase1Passed) {
            p1WarnRightLabel.setText("Passed");
            p1WarnRightLabel.setForeground(Color.GREEN);
        } else if (phase2Started) {
            p1WarnRightLabel.setText("Failed");
            p1WarnRightLabel.setForeground(ColorScheme.PROGRESS_ERROR_COLOR);
        } else {
            p1WarnRightLabel.setText("25+ dmg needed");
            p1WarnRightLabel.setForeground(ColorScheme.PROGRESS_ERROR_COLOR);
        }

        boolean isEligible;

        if (p < 6) {
            teamRightLabel.setText("25 Ph1 AND 75 Ph2 dmg");
            if (phase2Passed) {
                p2WarnRightLabel.setText("Passed");
                p2WarnRightLabel.setForeground(Color.GREEN);
            } else {
                p2WarnRightLabel.setText("75+ dmg needed");
                p2WarnRightLabel.setForeground(ColorScheme.PROGRESS_ERROR_COLOR);
            }
            isEligible = phase1Passed && phase2Passed;
        } else {
            teamRightLabel.setText("25 Ph1 OR 75 Ph2 dmg");
            if (phase1Passed) {
                p2WarnRightLabel.setText("Ignored (Mass)");
                p2WarnRightLabel.setForeground(Color.GREEN);
            } else {
                if (phase2Passed) {
                    p2WarnRightLabel.setText("Passed");
                    p2WarnRightLabel.setForeground(Color.GREEN);
                } else {
                    p2WarnRightLabel.setText("75+ dmg needed");
                    p2WarnRightLabel.setForeground(ColorScheme.PROGRESS_ERROR_COLOR);
                }
            }
            isEligible = phase1Passed || phase2Passed;
        }

        if (mt > 0 && isEligible) {
            double c = Math.max(Math.min((double)mt/4050.0, 1), 0.05);
            hideLabel.setText("~1/" + df.format(28.64/c)); tomeLabel.setText("~1/" + df.format(90.0/c)); wandLabel.setText("~1/" + df.format(105.0/c)); petLabel.setText("~1/" + df.format((400.0/c)*(mb < 25 ? 20 : 1)));
        } else {
            hideLabel.setText("N/A"); tomeLabel.setText("N/A"); wandLabel.setText("N/A"); petLabel.setText("N/A");
        }

        kphLabel.setText(df.format(plugin.getKph()));
        killsLabel.setText(String.valueOf(plugin.getSessionKills()));
        avgKillLabel.setText(plugin.formatTime(plugin.getAvgKillTime()));
        fastestKillLabel.setText(plugin.formatTime(plugin.getFastestKillTime()));

        boolean missingNames = false;
        for (Integer id : plugin.sessionLootTracker.keySet()) {
            String cachedName = plugin.itemNameCache.get(id);
            if (cachedName == null || cachedName.equals("Unknown Item") || cachedName.equals("null")) {
                missingNames = true;
                break;
            }
        }
        if (missingNames) {
            updateLootTrackerUI();
        }
    }

    public void updateKillLogUI() {
        StringBuilder sb = new StringBuilder();
        for (int i = plugin.killLog.size()-1; i>=0; i--) {
            var r = plugin.killLog.get(i);

            String killTitle = r.eligible ? ("Kill: #" + (r.kc > 0 ? r.kc : (i + 1))) : "Kill: Failed";

            sb.append(killTitle).append("\n")
                    .append("Contribution:\n")
                    .append("P1: ").append(r.bodyDmg).append("\n")
                    .append("P2: ").append(r.headTailDmg).append("\n")
                    .append("Percentage: ").append(r.getContributionString()).append("\n")
                    .append("Loot\n")
                    .append("Total Drop: ").append(r.totalLoot).append("\n")
                    .append("Your Loot: ").append(r.loot).append("\n\n");
        }
        killLogArea.setText(sb.toString());
        killLogArea.setCaretPosition(0);
    }

    public void updateLootTrackerUI() {
        if (plugin.getClientThread() == null) return;

        plugin.getClientThread().invokeLater(() -> {
            long calculatedTotalGp = 0;
            java.util.List<LootItemData> itemDataList = new ArrayList<>();

            try {
                for (Map.Entry<Integer, Integer> e : plugin.sessionLootTracker.entrySet()) {
                    int id = e.getKey();
                    int qty = e.getValue();
                    int itemPrice = 0;

                    try { itemPrice = plugin.getItemManager().getItemPrice(id); } catch (Exception ignored) {}

                    long stackValue = (long) itemPrice * qty;
                    calculatedTotalGp += stackValue;

                    AsyncBufferedImage icon = null;
                    try { icon = plugin.getItemManager().getImage(id, qty, qty > 1); } catch (Exception ignored) {}

                    String itemName = plugin.itemNameCache.get(id);
                    if (itemName == null || itemName.equals("Unknown Item") || itemName.equals("null")) {
                        try {
                            ItemComposition comp = plugin.getItemManager().getItemComposition(id);
                            if (comp.getName() != null && !comp.getName().equals("null")) {
                                itemName = comp.getName();
                                plugin.itemNameCache.put(id, itemName);
                            } else {
                                itemName = "Unknown Item";
                            }
                        } catch (Exception ex) {
                            itemName = "Unknown Item";
                        }
                    }

                    itemDataList.add(new LootItemData(id, qty, stackValue, icon, itemName));
                }
            } catch (Exception e) {
                // Ignored
            }

            final long finalTotalGp = calculatedTotalGp;

            SwingUtilities.invokeLater(() -> {
                try {
                    lootContainer.removeAll();

                    JPanel lootBox = new JPanel(new BorderLayout(0, 1));
                    lootBox.setBackground(ColorScheme.DARK_GRAY_COLOR);

                    JPanel headerBox = new JPanel(new BorderLayout());
                    headerBox.setBackground(ColorScheme.DARKER_GRAY_COLOR);
                    headerBox.setBorder(new EmptyBorder(7, 7, 7, 7));

                    Font dynamicRsFont = getCustomFontChoice();

                    JLabel titleLabel = new JLabel("The Hueycoatl x " + plugin.persistentKillCount);
                    titleLabel.setFont(dynamicRsFont);
                    titleLabel.setForeground(Color.WHITE);

                    JLabel priceLabel = new JLabel(formatGp(finalTotalGp) + " gp");
                    priceLabel.setFont(dynamicRsFont);
                    priceLabel.setForeground(ColorScheme.LIGHT_GRAY_COLOR);

                    headerBox.add(titleLabel, BorderLayout.WEST);
                    headerBox.add(priceLabel, BorderLayout.EAST);

                    JPanel itemGrid = new JPanel(new GridLayout(0, 5, 1, 1));
                    itemGrid.setBackground(ColorScheme.DARKER_GRAY_COLOR);
                    itemGrid.setBorder(new EmptyBorder(2, 2, 2, 2));

                    for (LootItemData data : itemDataList) {
                        JLabel itemLabel = new JLabel();
                        itemLabel.setPreferredSize(new Dimension(36, 36));
                        itemLabel.setVerticalAlignment(SwingConstants.CENTER);
                        itemLabel.setHorizontalAlignment(SwingConstants.CENTER);

                        itemLabel.setToolTipText("<html>" + data.name + " x " + data.qty + "<br>" + formatGp(data.stackValue) + " gp</html>");

                        if (data.icon != null) data.icon.addTo(itemLabel);
                        itemGrid.add(itemLabel);
                    }

                    lootBox.add(headerBox, BorderLayout.NORTH);
                    lootBox.add(itemGrid, BorderLayout.CENTER);

                    lootContainer.add(lootBox, BorderLayout.NORTH);

                    lootContainer.revalidate();
                    lootContainer.repaint();

                } catch (Throwable t) {
                    // Ignored
                }
            });
        });
    }

    private void openLogFolder() {
        try {
            File dir = plugin.getLogDir();
            boolean ignored = dir.mkdirs();
            java.awt.Desktop.getDesktop().open(dir);
        } catch (Exception ignored) {}
    }

    private void exportLog() {
        String dateStr = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
        String fileName = "HueyHelperLog_" + plugin.getAccountName() + "_export_" + dateStr + ".csv";
        plugin.writeToCSV(fileName, null, false);
    }

    private void confirmClear() {
        Object[] options = {"Clear Log", "Clear Loot", "Clear Both", "Cancel"};
        int choice = JOptionPane.showOptionDialog(this, "What would you like to clear?", "Clear Data", JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, null, options, options[3]);
        if (choice == 0) { plugin.clearLog(); updateKillLogUI(); }
        else if (choice == 1) { plugin.clearLoot(); updateLootTrackerUI(); }
        else if (choice == 2) { plugin.clearLog(); plugin.clearLoot(); updateKillLogUI(); updateLootTrackerUI(); }
    }
}