package org.cyclops.flopper.proxy;

import org.cyclops.cyclopscore.init.ModBaseFabric;
import org.cyclops.cyclopscore.proxy.CommonProxyComponentFabric;
import org.cyclops.flopper.FlopperFabric;

/**
 * Proxy for server and client side.
 * @author rubensworks
 *
 */
public class CommonProxyFabric extends CommonProxyComponentFabric {

    @Override
    public ModBaseFabric<?> getMod() {
        return FlopperFabric._instance;
    }

}
