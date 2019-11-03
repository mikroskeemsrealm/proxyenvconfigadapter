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

import net.md_5.bungee.api.config.ConfigurationAdapter;
import net.md_5.bungee.api.config.ListenerInfo;
import net.md_5.bungee.api.config.ServerInfo;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * @author Mark Vainomaa
 */
public final class InterceptingConfigurationAdapter implements ConfigurationAdapter {
    private final ConfigurationAdapter backing;
    private final Interceptor interceptor;

    public InterceptingConfigurationAdapter(ConfigurationAdapter backing, Interceptor interceptor) {
        this.backing = backing;
        this.interceptor = interceptor;
    }

    @Override
    public void load() {
        backing.load();
    }

    @Override
    public int getInt(String path, int def) {
        Object res = interceptor.intercept(path, Integer.class, def);
        return res == NOOP ? backing.getInt(path, def) : (Integer) res;
    }

    @Override
    public String getString(String path, String def) {
        Object res = interceptor.intercept(path, String.class, def);
        return res == NOOP ? backing.getString(path, def) : (String) res;
    }

    @Override
    public boolean getBoolean(String path, boolean def) {
        Object res = interceptor.intercept(path, Boolean.class, def);
        return res == NOOP ? backing.getBoolean(path, def) : (Boolean) res;
    }

    @Override
    public Collection<?> getList(String path, Collection<?> def) {
        // Too complex
        return backing.getList(path, def);
    }

    @Override
    public Map<String, ServerInfo> getServers() {
        // Too complex
        return backing.getServers();
    }

    @Override
    public Collection<ListenerInfo> getListeners() {
        Collection<ListenerInfo> listeners = backing.getListeners();
        List<ListenerInfo> newListeners = new ArrayList<>(listeners.size());
        int i = 0;
        for (ListenerInfo listener: listeners) {
            String host = getString("$$$listener.host:" + i, listener.getHost().getAddress()::getHostAddress);
            int port = getInt("$$$listener.port:" + i, listener.getHost()::getPort);
            int queryPort = getInt("$$$listener.query_port:" + i, listener::getQueryPort);
            boolean proxyProtocol = getBoolean("$$$listener.proxy_protocol:" + i, listener::isProxyProtocol);

            ListenerInfo newListener = new ListenerInfo(
                    new InetSocketAddress(host, port),
                    listener.getMotd(),
                    listener.getMaxPlayers(),
                    listener.getTabListSize(),
                    listener.getServerPriority(),
                    listener.isForceDefault(),
                    listener.getForcedHosts(),
                    listener.getTabListType(),
                    listener.isSetLocalAddress(),
                    listener.isPingPassthrough(),
                    queryPort,
                    listener.isQueryEnabled(),
                    proxyProtocol
            );
            newListeners.add(i, newListener);

            i++;
        }

        return newListeners;
    }

    @Override
    public Collection<String> getGroups(String player) {
        // Too complex
        return backing.getGroups(player);
    }

    @Override
    public Collection<String> getPermissions(String group) {
        // Too complex
        return backing.getPermissions(group);
    }

    @Override
    public int getInt(String path, Supplier<Integer> def) {
        Object res = interceptor.intercept(path, Integer.class, def);
        return res == NOOP ? backing.getInt(path, def) : (Integer) res;
    }

    @Override
    public String getString(String path, Supplier<String> def) {
        Object res = interceptor.intercept(path, String.class, def);
        return res == NOOP ? backing.getString(path, def) : (String) res;
    }

    @Override
    public boolean getBoolean(String path, Supplier<Boolean> def) {
        Object res = interceptor.intercept(path, Boolean.class, def);
        return res == NOOP ? backing.getBoolean(path, def) : (Boolean) res;
    }

    @Override
    public <T, C extends Collection<T>> C getList(String path, Supplier<C> def) {
        // Too complex
        return backing.getList(path, def);
    }

    public static abstract class Interceptor {
        @NonNull
        protected abstract Object intercept(@NonNull String path, @NonNull Class<?> type, @NonNull Object def);

        protected String getString(@NonNull String name, @NonNull Object def) {
            String envValue = System.getenv(name);
            return envValue != null ? envValue : (String) getDef(def);
        }

        protected Integer getInt(@NonNull String name, @NonNull Object def) {
            String envValue = System.getenv(name);
            return envValue != null ? Integer.parseInt(envValue) : (Integer) getDef(def);
        }

        protected Boolean getBoolean(@NonNull String name, @NonNull Object def) {
            String envValue = System.getenv(name);
            return envValue != null ? Boolean.parseBoolean(envValue) : (Boolean) getDef(def);
        }

        private Object getDef(Object def) {
            if (def instanceof Supplier) {
                return ((Supplier) def).get();
            }
            return def;
        }
    }

    public static final Object NOOP = new Object();
}
