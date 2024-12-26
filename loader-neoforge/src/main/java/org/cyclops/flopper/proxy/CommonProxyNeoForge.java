package org.cyclops.flopper.proxy;

import org.cyclops.cyclopscore.init.ModBaseNeoForge;
import org.cyclops.cyclopscore.proxy.CommonProxyComponent;
import org.cyclops.flopper.FlopperNeoForge;

/**
 * Proxy for server and client side.
 * @author rubensworks
 *
 */
public class CommonProxyNeoForge extends CommonProxyComponent {

    @Override
    public ModBaseNeoForge<FlopperNeoForge> getMod() {
        return FlopperNeoForge._instance;
    }

}
