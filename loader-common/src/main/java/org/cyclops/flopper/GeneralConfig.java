package org.cyclops.flopper;

import org.cyclops.cyclopscore.config.ConfigurablePropertyCommon;
import org.cyclops.cyclopscore.config.extendedconfig.DummyConfigCommon;
import org.cyclops.cyclopscore.helper.ModBaseCommon;

/**
 * A config with general options for this mod.
 * @author rubensworks
 *
 */
public class GeneralConfig extends DummyConfigCommon<ModBaseCommon<?>> {

    @ConfigurablePropertyCommon(category = "core", comment = "If an anonymous mod startup analytics request may be sent to our analytics service.")
    public static boolean analytics = true;

    @ConfigurablePropertyCommon(category = "core", comment = "If the version checker should be enabled.")
    public static boolean versionChecker = true;

    public GeneralConfig(ModBaseCommon<?> mod) {
        super(mod, "general");
    }

}
