package com.hueyhelper;

import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;

import javax.inject.Inject;
import java.awt.*;
import java.text.DecimalFormat;

public class HueyHelperOverlay extends OverlayPanel
{
    private final HueyHelperPlugin plugin;
    private final HueyHelperConfig config;

    private static final int MAX_BODY_HP = 1250;
    private static final int MAX_HEAD_TAIL_HP = 2800;
    private static final int TOTAL_HP = 4050;
    private final DecimalFormat df = new DecimalFormat("#.##");

    @Inject
    public HueyHelperOverlay(HueyHelperPlugin plugin, HueyHelperConfig config)
    {
        this.plugin = plugin;
        this.config = config;
        setPosition(OverlayPosition.TOP_LEFT);
    }

    @Override
    public Dimension render(Graphics2D graphics)
    {
        if (!config.showOverlay() || !plugin.isInArena()) return null;

        panelComponent.getChildren().clear();
        panelComponent.getChildren().add(TitleComponent.builder().text("Huey Helper").color(Color.ORANGE).build());

        int pCount = plugin.getParticipantCount();

        if (config.showParticipants())
        {
            panelComponent.getChildren().add(LineComponent.builder()
                    .left("Participants:")
                    .right(pCount + "/20")
                    .build());
        }

        int myBody = plugin.getBodyDamage();
        int myHeadTail = plugin.getHeadTailTotal();
        int myTotal = myBody + myHeadTail;

        boolean phase1Passed = myBody >= 25;
        boolean phase2Passed = myHeadTail >= 75;
        boolean isMass = pCount >= 6;

        boolean isEligible = isMass ? (phase1Passed || phase2Passed) : (phase1Passed && phase2Passed);

        if (config.showPhaseDamage())
        {
            double bodyContrib = Math.min((double) myBody / MAX_BODY_HP, 1.0);
            double htContrib = Math.min((double) myHeadTail / MAX_HEAD_TAIL_HP, 1.0);

            Color bodyColor = phase1Passed ? Color.GREEN : Color.RED;
            Color headTailColor;

            if (phase2Passed) {
                headTailColor = Color.GREEN;
            } else if (isMass && phase1Passed) {
                headTailColor = Color.GRAY;
            } else {
                headTailColor = Color.RED;
            }

            String bodyRight = config.showPercentages() ? myBody + " (" + df.format(bodyContrib * 100) + "%)" : String.valueOf(myBody);
            String htRight = config.showPercentages() ? myHeadTail + " (" + df.format(htContrib * 100) + "%)" : String.valueOf(myHeadTail);

            panelComponent.getChildren().add(LineComponent.builder().left("Phase 1:").right(bodyRight).rightColor(bodyColor).build());
            panelComponent.getChildren().add(LineComponent.builder().left("Phase 2:").right(htRight).rightColor(headTailColor).build());
        }

        if (config.showContribution() && myTotal > 0)
        {
            String contribText;
            double calc = (double) myTotal / TOTAL_HP;

            if (isMass) {
                contribText = (calc <= 0.05) ? "5% (Mass Protection)" : df.format(calc * 100) + "% (Assumed)";
            } else if (phase1Passed && phase2Passed) {
                contribText = (calc <= 0.05) ? "5% (Rounded up)" : df.format(calc * 100) + "% (Assumed)";
            } else {
                contribText = "5% (95% Penalty)";
            }

            Color contribColor = isEligible ? Color.GREEN : Color.RED;
            panelComponent.getChildren().add(LineComponent.builder().left("Reward %:").right(contribText).rightColor(contribColor).build());
        }

        if (config.showDropRates() && myTotal > 0)
        {
            if (!isEligible)
            {
                Color dropColor = Color.RED;
                panelComponent.getChildren().add(LineComponent.builder().left("Hide:").right("None").rightColor(dropColor).build());
                panelComponent.getChildren().add(LineComponent.builder().left("Tome:").right("None").rightColor(dropColor).build());
                panelComponent.getChildren().add(LineComponent.builder().left("Wand:").right("None").rightColor(dropColor).build());
                panelComponent.getChildren().add(LineComponent.builder().left("Pet:").right("None").rightColor(dropColor).build());
            }
            else
            {
                double dropMathContrib = Math.max(Math.min((double) myTotal / TOTAL_HP, 1.0), 0.05);

                double petPenalty = phase1Passed ? 1.0 : 20.0;
                Color petColor = phase1Passed ? Color.GREEN : Color.ORANGE;
                Color dropColor = Color.GREEN;

                panelComponent.getChildren().add(LineComponent.builder().left("Hide:").right("~1/" + df.format(28.64 / dropMathContrib)).rightColor(dropColor).build());
                panelComponent.getChildren().add(LineComponent.builder().left("Tome:").right("~1/" + df.format(90.0 / dropMathContrib)).rightColor(dropColor).build());
                panelComponent.getChildren().add(LineComponent.builder().left("Wand:").right("~1/" + df.format(105.0 / dropMathContrib)).rightColor(dropColor).build());
                panelComponent.getChildren().add(LineComponent.builder().left("Pet:").right("~1/" + df.format((400.0 / dropMathContrib) * petPenalty)).rightColor(petColor).build());
            }
        }

        return super.render(graphics);
    }
}