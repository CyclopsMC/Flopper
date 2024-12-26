package org.cyclops.flopper.proxy;

import org.cyclops.cyclopscore.init.ModBaseNeoForge;
import org.cyclops.cyclopscore.proxy.ClientProxyComponent;
import org.cyclops.flopper.FlopperNeoForge;

/**
 * Proxy for the client side.
 *
 * @author rubensworks
 *
 */
public class ClientProxyNeoForge extends ClientProxyComponent {

    public ClientProxyNeoForge() {
        super(new CommonProxyNeoForge());
    }

    @Override
    public ModBaseNeoForge<FlopperNeoForge> getMod() {
        return FlopperNeoForge._instance;
    }

}
