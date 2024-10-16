package org.cyclops.flopper.proxy;

import org.cyclops.cyclopscore.init.ModBase;
import org.cyclops.cyclopscore.proxy.CommonProxyComponent;
import org.cyclops.flopper.FlopperNeoForge;

/**
 * Proxy for server and client side.
 * @author rubensworks
 *
 */
public class CommonProxyNeoForge extends CommonProxyComponent {

    @Override
    public ModBase getMod() {
        return FlopperNeoForge._instance;
    }

}
