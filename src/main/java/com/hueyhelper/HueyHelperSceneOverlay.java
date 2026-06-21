package com.hueyhelper;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.Polygon;
import java.awt.BasicStroke;
import java.awt.RenderingHints;
import java.awt.FontMetrics;
import java.awt.Font;
import java.util.List;
import java.util.ArrayList;
import java.util.Collection;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.api.Perspective;
import net.runelite.api.Point;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayUtil;
import net.runelite.client.ui.FontManager;

public class HueyHelperSceneOverlay extends Overlay {
    private final Client client;
    private final HueyHelperPlugin plugin;
    private final HueyHelperConfig config;

    @Inject
    private HueyHelperSceneOverlay(Client client, HueyHelperPlugin plugin, HueyHelperConfig config) {
        this.client = client;
        this.plugin = plugin;
        this.config = config;

        setPosition(OverlayPosition.DYNAMIC);
        setLayer(OverlayLayer.ABOVE_SCENE);
    }

    @SuppressWarnings("deprecation")
    @Override
    public Dimension render(Graphics2D graphics) {
        if (!plugin.isInArena()) {
            return null;
        }

        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // ==========================================
        // FEATURE 1: HUEYCOATL BODY HIGHLIGHT
        // ==========================================
        if (config.highlightBody()) {
            List<NPC> bodyParts = plugin.getHueycoatlBodyParts();
            for (NPC bossPart : bodyParts) {
                Shape poly = bossPart.getConvexHull();
                if (poly != null) {
                    OverlayUtil.renderPolygon(graphics, poly, Color.CYAN);
                }
            }
        }

        // ==========================================
        // FEATURE 2: MECHANIC GUIDE TILES ONLY
        // ==========================================
        if (config.showTileMarkers()) {
            for (HueyHelperPlugin.GuideTile tile : plugin.getGuideTiles()) {
                WorldPoint wp = WorldPoint.fromRegion(5939, tile.rx, tile.ry, client.getPlane());
                Collection<WorldPoint> localWorldPoints = WorldPoint.toLocalInstance(client, wp);

                for (WorldPoint iwp : localWorldPoints) {
                    LocalPoint lp = LocalPoint.fromWorld(client, iwp);
                    if (lp == null) continue;

                    Polygon poly = Perspective.getCanvasTilePoly(client, lp);
                    if (poly != null) {
                        Color markerFill = new Color(tile.color.getRed(), tile.color.getGreen(), tile.color.getBlue(), 60);
                        Color markerStroke = new Color(tile.color.getRed(), tile.color.getGreen(), tile.color.getBlue(), 180);

                        graphics.setStroke(new BasicStroke(2));
                        graphics.setColor(markerFill);
                        graphics.fillPolygon(poly);
                        graphics.setColor(markerStroke);
                        graphics.drawPolygon(poly);
                    }

                    if (config.showTileText() && tile.label != null && !tile.label.isEmpty()) {
                        renderWrappedText(graphics, lp, tile.label, tile.color);
                    }
                }
            }
        }

        return null;
    }

    @SuppressWarnings("deprecation")
    private void renderWrappedText(Graphics2D graphics, LocalPoint lp, String text, Color textColor) {
        if (text == null || text.isEmpty()) return;

        Point canvasPoint = Perspective.localToCanvas(client, lp, client.getPlane());
        if (canvasPoint == null) return;

        graphics.setFont(FontManager.getRunescapeSmallFont());

        int startX = canvasPoint.getX();
        int startY = canvasPoint.getY();

        String[] words = text.split("\\s+");
        List<String> lines = new ArrayList<>();
        StringBuilder currentLine = new StringBuilder();

        for (int i = 0; i < words.length; i++) {
            currentLine.append(words[i]).append(" ");
            if ((i + 1) % 4 == 0 || i == words.length - 1) {
                lines.add(currentLine.toString().trim());
                currentLine.setLength(0);
            }
        }

        FontMetrics fm = graphics.getFontMetrics();
        int lineHeight = fm.getHeight();
        int totalHeight = lines.size() * lineHeight;

        int currentY = startY - (totalHeight / 2) + fm.getAscent();

        for (String line : lines) {
            int lineWidth = fm.stringWidth(line);
            int currentX = startX - (lineWidth / 2);

            // Draw a full 4-point black outline for maximum readability
            graphics.setColor(Color.BLACK);
            graphics.drawString(line, currentX - 1, currentY - 1); // Top Left
            graphics.drawString(line, currentX + 1, currentY - 1); // Top Right
            graphics.drawString(line, currentX - 1, currentY + 1); // Bottom Left
            graphics.drawString(line, currentX + 1, currentY + 1); // Bottom Right

            // Draw the actual colored text layer on top
            graphics.setColor(textColor);
            graphics.drawString(line, currentX, currentY);

            currentY += lineHeight;
        }
    }
}