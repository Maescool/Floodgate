/*
 * Copyright (c) 2019-2021 GeyserMC. http://geysermc.org
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 *
 * @author GeyserMC
 * @link https://github.com/GeyserMC/Floodgate
 */

package org.geysermc.floodgate.addon.data;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import io.netty.channel.Channel;
import io.netty.util.AttributeKey;
import org.geysermc.floodgate.api.ProxyFloodgateApi;
import org.geysermc.floodgate.api.inject.InjectorAddon;
import org.geysermc.floodgate.api.logger.FloodgateLogger;
import org.geysermc.floodgate.api.player.FloodgatePlayer;
import org.geysermc.floodgate.config.ProxyFloodgateConfig;
import org.geysermc.floodgate.player.HandshakeHandler;
import org.geysermc.floodgate.util.Utils;

public final class VelocityDataAddon implements InjectorAddon {
    @Inject private HandshakeHandler handshakeHandler;
    @Inject private ProxyFloodgateConfig config;
    @Inject private ProxyFloodgateApi api;
    @Inject private FloodgateLogger logger;

    @Inject
    @Named("packetHandler")
    private String packetHandler;

    @Inject
    @Named("packetEncoder")
    private String packetEncoder;

    @Inject
    @Named("playerAttribute")
    private AttributeKey<FloodgatePlayer> playerAttribute;

    @Inject
    @Named("kickMessageAttribute")
    private AttributeKey<String> kickMessageAttribute;

    @Override
    public void onInject(Channel channel, boolean toServer) {
        if (toServer) {
            channel.pipeline().addAfter(
                    packetEncoder, "floodgate_data_handler",
                    new VelocityServerDataHandler(config, api)
            );
            return;
        }
        // The handler is already added so we should add our handler before it
        channel.pipeline().addBefore(
                packetHandler, "floodgate_data_handler",
                new VelocityProxyDataHandler(
                        config, api, handshakeHandler, playerAttribute,
                        kickMessageAttribute, logger
                )
        );
    }

    @Override
    public void onLoginDone(Channel channel) {
        onRemoveInject(channel);
    }

    @Override
    public void onRemoveInject(Channel channel) {
        Utils.removeHandler(channel.pipeline(), "floodgate_data_handler");
    }

    @Override
    public boolean shouldInject() {
        return true;
    }
}
