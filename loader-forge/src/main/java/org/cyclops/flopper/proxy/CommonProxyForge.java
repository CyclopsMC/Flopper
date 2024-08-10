package org.cyclops.flopper.proxy;

import org.cyclops.cyclopscore.init.ModBaseForge;
import org.cyclops.cyclopscore.proxy.CommonProxyComponentForge;
import org.cyclops.flopper.FlopperForge;

/**
 * Proxy for server and client side.
 * @author rubensworks
 *
 */
public class CommonProxyForge extends CommonProxyComponentForge {

    @Override
    public ModBaseForge<?> getMod() {
        return FlopperForge._instance;
    }

}
