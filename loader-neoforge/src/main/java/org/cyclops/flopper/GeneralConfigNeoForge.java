package org.cyclops.flopper;

import org.cyclops.cyclopscore.init.ModBase;
import org.cyclops.cyclopscore.tracking.Analytics;
import org.cyclops.cyclopscore.tracking.Versions;

/**
 * @author rubensworks
 */
public class GeneralConfigNeoForge extends GeneralConfig {
    public GeneralConfigNeoForge() {
        super(FlopperNeoForge._instance);
    }

    @Override
    public void onRegistered() {
        if(analytics) {
            Analytics.registerMod((ModBase) getMod(), Reference.GA_TRACKING_ID);
        }
        if(versionChecker) {
            Versions.registerMod((ModBase) getMod(), FlopperNeoForge._instance, "https://raw.githubusercontent.com/CyclopsMC/Versions/master/" + getMod().getModHelpers().getMinecraftHelpers().getMinecraftVersionMajorMinor() + "/Flopper.txt");
        }
    }
}
