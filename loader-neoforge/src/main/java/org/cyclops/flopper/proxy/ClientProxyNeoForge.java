package org.cyclops.flopper.proxy;

import org.cyclops.cyclopscore.init.ModBase;
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
    public ModBase getMod() {
        return FlopperNeoForge._instance;
    }

}
