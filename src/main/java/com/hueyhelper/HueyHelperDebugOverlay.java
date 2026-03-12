package com.hueyhelper;

import net.runelite.api.Client;
import net.runelite.api.Perspective;
import net.runelite.api.Point;
import net.runelite.api.WorldView;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;

import javax.inject.Inject;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.util.Collection;

public class HueyHelperDebugOverlay extends OverlayPanel
{
    private final Client client;
    private final HueyHelperPlugin plugin;
    private final HueyHelperConfig config;

    @Inject
    public HueyHelperDebugOverlay(Client client, HueyHelperPlugin plugin, HueyHelperConfig config)
    {
        this.client = client;
        this.plugin = plugin;
        this.config = config;
        setPosition(OverlayPosition.TOP_RIGHT);
    }

    @Override
    public Dimension render(Graphics2D graphics)
    {
        boolean showBox = config.debugStateBox();
        boolean showTiles = config.debugHighlightedArena();

        if (!showBox && !showTiles) return null;

        // Grab the modern WorldView to handle coordinates (Fixes the deprecation warnings!)
        WorldView wv = client.getTopLevelWorldView();
        if (wv == null) return null;

        // 1. Draw your exact custom tiles, translated for the instance!
        if (showTiles) {
            int z = wv.getPlane();
            for (int packed : plugin.getArenaTiles()) {
                int wx = packed >> 16;
                int wy = packed & 0xFFFF;
                drawTile(graphics, wv, wx, wy, z);
            }
        }

        // 2. Render the Debug Box
        if (showBox) {
            panelComponent.getChildren().clear();
            panelComponent.getChildren().add(TitleComponent.builder().text("Huey Debugger").color(Color.YELLOW).build());
            panelComponent.getChildren().add(LineComponent.builder()
                    .left("In Arena Zone:")
                    .right(plugin.isInArena() ? "YES" : "NO")
                    .rightColor(plugin.isInArena() ? Color.GREEN : Color.RED)
                    .build());
            panelComponent.getChildren().add(LineComponent.builder()
                    .left("State Machine:")
                    .right("Phase " + plugin.getCurrentFightPhase())
                    .rightColor(Color.CYAN)
                    .build());

            return super.render(graphics);
        }

        return null;
    }

    private void drawTile(Graphics2D graphics, WorldView wv, int wx, int wy, int plane) {
        WorldPoint templateWp = new WorldPoint(wx, wy, plane);

        // Uses the new modern WorldView coordinate system
        Collection<WorldPoint> activePoints = WorldPoint.toLocalInstance(wv, templateWp);

        for (WorldPoint activeWp : activePoints) {
            LocalPoint lp = LocalPoint.fromWorld(wv, activeWp);

            if (lp != null) {
                // Draw a perfectly glued-to-the-ground green tile
                Polygon poly = Perspective.getCanvasTilePoly(client, lp);
                if (poly != null) {
                    graphics.setColor(new Color(0, 255, 0, 40));
                    graphics.drawPolygon(poly);
                    graphics.setColor(new Color(0, 255, 0, 15));
                    graphics.fillPolygon(poly);
                }

                // Draw it on the minimap
                Point minimapPoint = Perspective.localToMinimap(client, lp);
                if (minimapPoint != null) {
                    graphics.setColor(new Color(0, 255, 0, 60));
                    graphics.fillRect(minimapPoint.getX(), minimapPoint.getY(), 1, 1);
                }
            }
        }
    }
}