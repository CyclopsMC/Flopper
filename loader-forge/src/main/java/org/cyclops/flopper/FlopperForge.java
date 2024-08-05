package org.cyclops.flopper;

import net.minecraftforge.fml.common.Mod;
import org.cyclops.cyclopscore.init.ModBaseForge;

/**
 * The main mod class of Flopper.
 * @author rubensworks
 *
 */
@Mod(Reference.MOD_ID)
public class FlopperForge extends ModBaseForge<FlopperForge> {

    /**
     * The unique instance of this mod.
     */
    public static FlopperForge _instance;

    public FlopperForge() {
        super(Reference.MOD_ID, (instance) -> _instance = instance);
    }

}
