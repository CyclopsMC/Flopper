package org.cyclops.flopper;

import net.fabricmc.api.ModInitializer;
import org.cyclops.cyclopscore.Reference;
import org.cyclops.cyclopscore.init.ModBaseFabric;

/**
 * The main mod class of Flopper.
 * @author rubensworks
 */
public class FlopperMainFabric extends ModBaseFabric<FlopperMainFabric> implements ModInitializer {

    /**
     * The unique instance of this mod.
     */
    public static FlopperMainFabric _instance;

    public FlopperMainFabric() {
        super(Reference.MOD_ID, (instance) -> _instance = instance);
    }
}
