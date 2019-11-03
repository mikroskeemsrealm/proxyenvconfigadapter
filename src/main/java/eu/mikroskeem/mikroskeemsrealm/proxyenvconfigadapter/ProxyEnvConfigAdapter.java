/*
 * This file is part of project ProxyEnvConfigAdapter, licensed under the MIT License (MIT).
 *
 * Copyright (c) 2019 Mark Vainomaa <mikroskeem@mikroskeem.eu>
 * Copyright (c) Contributors
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
 */

package eu.mikroskeem.mikroskeemsrealm.proxyenvconfigadapter;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ConfigurationAdapter;
import net.md_5.bungee.api.plugin.Plugin;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

/**
 * @author Mark Vainomaa
 */
public final class ProxyEnvConfigAdapter extends Plugin {
    private final Map<ProxyServer.ConfigurationType, ConfigurationAdapter> oldConfigurationAdapters = new HashMap<>();

    @Override
    public void onLoad() {
        try {
            Class.forName("net.md_5.bungee.api.ProxyServer$ConfigurationType");
        } catch (ClassNotFoundException e) {
            getLogger().log(Level.SEVERE, "This plugin runs only on MikroCord, sorry");
            return;
        }

        replaceAdapter(ProxyServer.ConfigurationType.BUNGEECORD, new InterceptingConfigurationAdapter.Interceptor() {
            @NonNull
            @Override
            protected Object intercept(@NonNull String path, @NonNull Class<?> type, @NonNull Object def) {
                if (path.startsWith("$$$listener.host:")) {
                    // Get the index and construct env variable name
                    int index = Integer.parseInt(path.split(":", 2)[1]);
                    return getString("LISTENER_" + index + "_HOST", def);
                }

                if (path.startsWith("$$$listener.port:")) {
                    int index = Integer.parseInt(path.split(":", 2)[1]);
                    return getInt("LISTENER_" + index + "_PORT", def);
                }

                if (path.startsWith("$$$listener.query_port:")) {
                    int index = Integer.parseInt(path.split(":", 2)[1]);
                    return getInt("LISTENER_" + index + "_QUERY_PORT", def);
                }

                if (path.startsWith("$$$listener.proxy_protocol:")) {
                    int index = Integer.parseInt(path.split(":", 2)[1]);
                    return getBoolean("LISTENER_" + index + "_HAPROXY", def);
                }

                return InterceptingConfigurationAdapter.NOOP;
            }
        });

        replaceAdapter(ProxyServer.ConfigurationType.WATERFALL, new InterceptingConfigurationAdapter.Interceptor() {
            @NonNull
            @Override
            protected Object intercept(@NonNull String path, @NonNull Class<?> type, @NonNull Object def) {
                return InterceptingConfigurationAdapter.NOOP;
            }
        });

        replaceAdapter(ProxyServer.ConfigurationType.MIKROCORD, new InterceptingConfigurationAdapter.Interceptor() {
            @NonNull
            @Override
            protected Object intercept(@NonNull String path, @NonNull Class<?> type, @NonNull Object def) {
                if ("velocity_modern_forwarding.secret".equals(path)) {
                    return getString("VELOCITY_FORWARDING_SECRET", def);
                }

                if ("prometheus.enabled".equals(path)) {
                    return getBoolean("PROMETHEUS_ENABLED", def);
                }

                if ("prometheus.listen_host".equals(path)) {
                    return getString("PROMETHEUS_HOST", def);
                }

                if ("prometheus.listen_port".equals(path)) {
                    return getInt("PROMETHEUS_PORT", def);
                }

                return InterceptingConfigurationAdapter.NOOP;
            }
        });
    }

    private void replaceAdapter(ProxyServer.ConfigurationType type, InterceptingConfigurationAdapter.Interceptor interceptor) {
        ConfigurationAdapter oldAdapter = ProxyServer.getInstance().getConfigurationAdapter(type);
        InterceptingConfigurationAdapter newAdapter = new InterceptingConfigurationAdapter(oldAdapter, interceptor);
        oldConfigurationAdapters.put(type, oldAdapter);
        ProxyServer.getInstance().setConfigurationAdapter(type, newAdapter);
    }
}
