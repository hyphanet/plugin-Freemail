/*
 * WoTConnections.java
 * This file is part of Freemail
 * Copyright (C) 2011 Martin Nyhus
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package org.freenetproject.freemail.wot;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.*;
import java.util.concurrent.*;

import freenet.clients.fcp.FCPPluginConnection;
import freenet.clients.fcp.FCPPluginMessage;
import freenet.keys.FreenetURI;
import freenet.pluginmanager.FredPluginFCPMessageHandler;
import freenet.pluginmanager.PluginNotFoundException;
import freenet.pluginmanager.PluginRespirator;
import freenet.support.Base64;
import org.freenetproject.freemail.utils.Logger;
import org.freenetproject.freemail.utils.SimpleFieldSetFactory;

public class WoTConnections implements WoTConnection {

    private final PluginRespirator pluginRespirator;

    private final Queue<WoTConnectionImpl> wotConnectionsPool = new ConcurrentLinkedQueue<>();

    public WoTConnections(PluginRespirator pluginRespirator) {
        this.pluginRespirator = pluginRespirator;
    }

    @Override
    public List<OwnIdentity> getAllOwnIdentities()
            throws PluginNotFoundException, IOException, TimeoutException, WoTException, InterruptedException {
        WoTConnectionImpl wotConnection = borrowWoTConnection();
        List<OwnIdentity> ownIdentities = wotConnection.getAllOwnIdentities();
        returnWoTConnection(wotConnection);
        return ownIdentities;
    }

    @Override
    public List<Identity> getAllIdentities()
            throws PluginNotFoundException, IOException, TimeoutException, WoTException, InterruptedException {
        WoTConnectionImpl wotConnection = borrowWoTConnection();
        List<Identity> identities = wotConnection.getAllIdentities();
        returnWoTConnection(wotConnection);
        return identities;
    }

    @Override
    public Set<Identity> getAllTrustedIdentities(String trusterId)
            throws PluginNotFoundException, IOException, TimeoutException, WoTException, InterruptedException {
        WoTConnectionImpl wotConnection = borrowWoTConnection();
        Set<Identity> trustedIdentities = wotConnection.getAllTrustedIdentities(trusterId);
        returnWoTConnection(wotConnection);
        return trustedIdentities;
    }

    @Override
    public Set<Identity> getAllUntrustedIdentities(String trusterId)
            throws PluginNotFoundException, IOException, TimeoutException, WoTException, InterruptedException {
        WoTConnectionImpl wotConnection = borrowWoTConnection();
        Set<Identity> untrustedIdentities = wotConnection.getAllUntrustedIdentities(trusterId);
        returnWoTConnection(wotConnection);
        return untrustedIdentities;
    }

    @Override
    public Identity getIdentity(String identityId, String trusterId)
            throws PluginNotFoundException, IOException, TimeoutException, WoTException, InterruptedException {
        WoTConnectionImpl wotConnection = borrowWoTConnection();
        Identity wotIdentity = wotConnection.getIdentity(identityId, trusterId);
        returnWoTConnection(wotConnection);
        return wotIdentity;
    }

    @Override
    public boolean setProperty(String identity, String key, String value)
            throws PluginNotFoundException, TimeoutException, IOException, InterruptedException {
        WoTConnectionImpl wotConnection;
        try {
            wotConnection = borrowWoTConnection();
        } catch (WoTException e) {
            Logger.warning(this, "setProperty " + key + " = " + value + " failed", e);
            return false;
        }
        boolean isSuccessfullySet = wotConnection.setProperty(identity, key, value);
        returnWoTConnection(wotConnection);
        return isSuccessfullySet;
    }

    @Override
    public String getProperty(String identity, String key)
            throws PluginNotFoundException, IOException, TimeoutException, WoTException, InterruptedException {
        WoTConnectionImpl wotConnection = borrowWoTConnection();
        String parameterValue = wotConnection.getProperty(identity, key);
        returnWoTConnection(wotConnection);
        return parameterValue;
    }

    @Override
    public boolean setContext(String identity, String context)
            throws PluginNotFoundException, IOException, TimeoutException, InterruptedException {
        WoTConnectionImpl wotConnection;
        try {
            wotConnection = borrowWoTConnection();
        } catch (WoTException e) {
            Logger.warning(this, "setContext failed: " + identity + " - " + context, e);
            return false;
        }
        boolean isSuccessfullySet = wotConnection.setContext(identity, context);
        returnWoTConnection(wotConnection);
        return isSuccessfullySet;
    }

    private WoTConnectionImpl borrowWoTConnection()
            throws PluginNotFoundException, TimeoutException, IOException, WoTException, InterruptedException {
        WoTConnectionImpl wotConnection;
        if ((wotConnection = wotConnectionsPool.poll()) == null) {
            wotConnection = new WoTConnectionImpl(pluginRespirator);
        } else {
            try {
                wotConnection.ping();
            } catch (IOException | TimeoutException | WoTException ignored) {
                if (wotConnectionsPool.size() > 0) {
                    borrowWoTConnection();
                } else {
                    wotConnection = new WoTConnectionImpl(pluginRespirator);
                    wotConnection.ping();
                }
            }
        }

        return wotConnection;
    }

    private void returnWoTConnection(WoTConnectionImpl wotConnection) {
        if (wotConnection == null) {
            return;
        }

        wotConnectionsPool.offer(wotConnection);
    }

    private class WoTConnectionImpl implements WoTConnection {

        private final FCPPluginConnection fcpPluginConnection;

        private WoTConnectionImpl(PluginRespirator pr) throws PluginNotFoundException {
            fcpPluginConnection = pr.connectToOtherPlugin(WoTProperties.WOT_FCP_NAME, new FredPluginFCPMessageHandler.ClientSideFCPMessageHandler() {
                @Override
                public FCPPluginMessage handlePluginFCPMessage(FCPPluginConnection connection, FCPPluginMessage message) {
                    /* NOTICE: It is possible that the reply message <b>is</b> passed to the message handler
                     * upon certain error conditions, for example if the timeout you specify when calling
                     * sendSynchronous expires before the reply arrives. This is not guaranteed though.
                     * - {@link FCPPluginConnection#sendSynchronous(FCPPluginMessage, long)} Javadoc */
                    Logger.warning(this, "Receive unexpected FCPMessage from WoT connection: " + message);
                    return null;
                }
            });
        }

        private void ping() throws IOException, TimeoutException, WoTException, InterruptedException {
            FCPPluginMessage message = FCPPluginMessage.construct();
            message.params.putAllOverwrite(new SimpleFieldSetFactory().put("Message", "Ping").create());
            FCPPluginMessage response = sendSynchronous(message, "Pong");
            checkErrorMessage("ping", response);
        }

        @Override
        public List<OwnIdentity> getAllOwnIdentities()
                throws IOException, TimeoutException, WoTException, InterruptedException {
            FCPPluginMessage message = FCPPluginMessage.construct();
            message.params.putAllOverwrite(new SimpleFieldSetFactory().put("Message", "GetOwnIdentities").create());
            FCPPluginMessage response = sendSynchronous(message, "OwnIdentities");
            checkErrorMessage("getAllOwnIdentities", response);

            int identitiesAmount = Integer.parseInt(response.params.get("Amount"));
            final List<OwnIdentity> ownIdentities = new ArrayList<>(identitiesAmount);
            for (int i = 0; i < identitiesAmount; i++) {
                String identityID = response.params.get("Identity" + i);
                assert identityID != null;

                String requestURI = response.params.get("RequestURI" + i);
                assert requestURI != null;

                String insertURI = response.params.get("InsertURI" + i);
                assert insertURI != null;

                String nickname = response.params.get("Nickname" + i);

                ownIdentities.add(new OwnIdentity(identityID, requestURI, insertURI, nickname));
            }

            return ownIdentities;
        }

        @Override
        public List<Identity> getAllIdentities() throws IOException, TimeoutException, WoTException, InterruptedException {
            FCPPluginMessage message = FCPPluginMessage.construct();
            message.params.putAllOverwrite(new SimpleFieldSetFactory().put("Message", "GetIdentities").create());
            FCPPluginMessage response = sendSynchronous(message, "Identities");
            checkErrorMessage("getAllIdentities", response);

            int identitiesAmount = Integer.parseInt(response.params.get("Identities.Amount"));
            final List<Identity> identities = new ArrayList<>(identitiesAmount);
            for (int i = 0; i < identitiesAmount; i++) {
                String requestURI = response.params.get("Identities." + i + ".RequestURI");
                assert requestURI != null;

                String nickname = response.params.get("Identities." + i + ".Nickname");

                if ("OwnIdentity".equals(response.params.get("Identities." + i + ".Type"))) {
                    String insertURI = response.params.get("Identities." + i + ".InsertURI");
                    assert insertURI != null;

                    identities.add(new OwnIdentity(toIdentityId(requestURI), requestURI, insertURI, nickname));
                } else {
                    identities.add(new Identity(toIdentityId(requestURI), requestURI, nickname));
                }
            }

            return identities;
        }

        @Override
        public Set<Identity> getAllTrustedIdentities(String trusterId)
                throws IOException, TimeoutException, WoTException, InterruptedException {
            List<Trustee> trustees = getTrustees(
                    Objects.requireNonNull(trusterId, "Parameter trusterId must not be null"));
            Set<Identity> allTrusted = new HashSet<>();
            for (Trustee trustee : trustees) {
                if (trustee.getTrustValue() >= 0) {
                    allTrusted.add(trustee);
                }
            }

            return allTrusted;
        }

        @Override
        public Set<Identity> getAllUntrustedIdentities(String trusterId)
                throws IOException, TimeoutException, WoTException, InterruptedException {
            List<Trustee> trustees = getTrustees(
                    Objects.requireNonNull(trusterId, "Parameter trusterId must not be null"));
            Set<Identity> allTrusted = new HashSet<>();
            for (Trustee trustee : trustees) {
                if (trustee.getTrustValue() < 0) {
                    allTrusted.add(trustee);
                }
            }

            return allTrusted;
        }

        private List<Trustee> getTrustees(String trusterId)
                throws IOException, TimeoutException, WoTException, InterruptedException {
            FCPPluginMessage message = FCPPluginMessage.construct();
            message.params.putAllOverwrite(new SimpleFieldSetFactory()
                    .put("Message", "GetTrustees")
                    .put("Context", WoTProperties.CONTEXT)
                    .put("Identity", Objects.requireNonNull(trusterId, "Parameter trusterId must not be null"))
                    .create());
            FCPPluginMessage response = sendSynchronous(message, "Identities");
            checkErrorMessage("getTrustees for " + trusterId, response);

            int identitiesAmount = Integer.parseInt(response.params.get("Amount"));
            final List<Trustee> identities = new ArrayList<>(identitiesAmount);
            for (int i = 0; i < identitiesAmount; i++) {
                String requestURI = response.params.get("RequestURI" + i);
                assert requestURI != null;

                String nickname = response.params.get("Nickname" + i);

                Byte trustValue = Byte.parseByte(response.params.get("Value" + i));
                assert trustValue >= -100 && trustValue <= 100;

                String comment = response.params.get("Comment" + i);

                identities.add(new Trustee(toIdentityId(requestURI), requestURI, nickname, trustValue, comment));
            }

            return identities;
        }

        @Override
        public Identity getIdentity(String identityId, String trusterId)
                throws IOException, TimeoutException, WoTException, InterruptedException {
            FCPPluginMessage message = FCPPluginMessage.construct();
            message.params.putAllOverwrite(new SimpleFieldSetFactory()
                    .put("Message", "GetIdentity")
                    .put("Identity", Objects.requireNonNull(identityId, "Parameter identityId must not be null"))
                    .put("Truster", Objects.requireNonNull(trusterId, "Parameter trusterId must not be null"))
                    .create());
            FCPPluginMessage response = sendSynchronous(message, "Identity");

            try {
                checkErrorMessage("getIdentity(" + identityId + ", " + trusterId + ")", response);
            } catch (WoTException e) {
                String description = response.params.get("Description");
                if (description != null && description.contains("UnknownIdentityException")) {
                    throw new UnknownIdentityException(description);
                }

                throw e;
            }

            String requestURI = response.params.get("Identities.0.RequestURI");
            assert (requestURI != null);

            String nickname = response.params.get("Identities.0.Nickname");

            return new Identity(identityId, requestURI, nickname);
        }

        @Override
        public boolean setProperty(String identityId, String key, String value)
                throws TimeoutException, IOException, InterruptedException {
            FCPPluginMessage message = FCPPluginMessage.construct();
            message.params.putAllOverwrite(new SimpleFieldSetFactory()
                    .put("Message", "SetProperty")
                    .put("Identity", Objects.requireNonNull(identityId, "Parameter identityId must not be null"))
                    .put("Property", Objects.requireNonNull(key, "Parameter key must not be null"))
                    .put("Value", Objects.requireNonNull(value, "Parameter value must not be null"))
                    .create());
            FCPPluginMessage response = sendSynchronous(message, "PropertyAdded");

            try {
                checkErrorMessage("setProperty(" + identityId + ", " + key + " = " + value + ")", response);
            } catch (WoTException ignored) {
                return false;
            }

            return true;
        }

        @Override
        public String getProperty(String identityId, String key)
                throws IOException, TimeoutException, WoTException, InterruptedException {
            FCPPluginMessage message = FCPPluginMessage.construct();
            message.params.putAllOverwrite(new SimpleFieldSetFactory()
                    .put("Message", "GetProperty")
                    .put("Identity", Objects.requireNonNull(identityId, "Parameter identityId must not be null"))
                    .put("Property", Objects.requireNonNull(key, "Parameter key must not be null"))
                    .create());
            FCPPluginMessage response = sendSynchronous(message, "PropertyValue");
            checkErrorMessage("getProperty(" + identityId + ", " + key + ")", response);

            return response.params.get("Property");
        }

        @Override
        public boolean setContext(String identityId, String context)
                throws IOException, TimeoutException, InterruptedException {
            FCPPluginMessage message = FCPPluginMessage.construct();
            message.params.putAllOverwrite(new SimpleFieldSetFactory()
                    .put("Message", "AddContext")
                    .put("Identity", Objects.requireNonNull(identityId, "Parameter identityId must not be null"))
                    .put("Context", Objects.requireNonNull(context, "Parameter context must not be null"))
                    .create());
            FCPPluginMessage response = sendSynchronous(message, "ContextAdded");

            try {
                checkErrorMessage("setContext(" + identityId + ", " + context + ")", response);
            } catch (WoTException ignored) {
                return false;
            }

            return true;
        }

        private FCPPluginMessage sendSynchronous(final FCPPluginMessage message, final String expectedResponseMessageType)
                throws IOException, TimeoutException, InterruptedException {
            assert message != null;

            try {
                FCPPluginMessage response = fcpPluginConnection.sendSynchronous(message, TimeUnit.MINUTES.toNanos(1));

                final String messageType = response.params.get("Message");
                if (!expectedResponseMessageType.equals(messageType) && !"Error".equals(messageType)) {
                    throw new IOException("Received unexpected message type: " + messageType +
                            "; expected: " + expectedResponseMessageType);
                }

                return response;
            } catch (IOException e) {
                if (e.getMessage().startsWith("timed out waiting for reply")) {
                    throw new TimeoutException(e.getMessage());
                }

                throw e;
            }
        }

        private String toIdentityId(String requestURI) throws MalformedURLException {
            return Base64.encode(new FreenetURI(requestURI).getRoutingKey());
        }

        private void checkErrorMessage(String method, FCPPluginMessage response) throws WoTException {
            if ("Error".equals(response.params.get("Message"))) {
                String errorMessage = response.params.toString();
                Logger.warning(this, method + " failed: " + errorMessage);
                throw new WoTException("WoT Error: " + errorMessage);
            }
        }
    }
}
