package org.cyclops.flopper.proxy;

import org.cyclops.cyclopscore.init.ModBaseFabric;
import org.cyclops.cyclopscore.proxy.ClientProxyComponentFabric;
import org.cyclops.cyclopscore.proxy.CommonProxyFabric;
import org.cyclops.flopper.FlopperMainFabric;

/**
 * Proxy for the client side.
 *
 * @author rubensworks
 *
 */
public class ClientProxyFabric extends ClientProxyComponentFabric {

    public ClientProxyFabric() {
        super(new CommonProxyFabric());
    }

    @Override
    public ModBaseFabric<?> getMod() {
        return FlopperMainFabric._instance;
    }

}
