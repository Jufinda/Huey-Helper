package com.hueyhelper;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.util.List; // We need this to read the list!
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayUtil;

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

    @Override
    public Dimension render(Graphics2D graphics) {
        if (!config.highlightBody() || !plugin.isInArena()) {
            return null;
        }

        // Get the entire list of body parts
        List<NPC> bodyParts = plugin.getHueycoatlBodyParts();

        // Loop through the list and draw a Cyan outline on every single one
        for (NPC bossPart : bodyParts) {
            Shape poly = bossPart.getConvexHull();
            if (poly != null) {
                OverlayUtil.renderPolygon(graphics, poly, Color.CYAN);
            }
        }

        return null;
    }
}