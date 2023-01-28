package net.myitian.boat_maneuverability_enhancing;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class BoatManeuverabilityEnhancing {
    public static final String MODID = "boat-maneuverability-enhancing";
    public static final SimpleConfig CONFIG = SimpleConfig.of(MODID).provider(filename ->
            "# Boat maneuverability enhancing factor: 0 (won't stop yawing) ~ 1 (stop yawing immediately)\r\n# 船只操控性增强参数：0（不会阻止水平旋转） ~ 1（立即阻止水平旋转）\r\nfactor=0.75").request();
}
