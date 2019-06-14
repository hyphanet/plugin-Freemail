/*
 * WoTConnection.java
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
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeoutException;

import freenet.pluginmanager.PluginNotFoundException;

public interface WoTConnection {
	/**
	 * Returns all the OwnIdentities fetched from WoT, or {@code null} if an error occurs. If there
	 * are no OwnIdentities the returned list will be empty.
	 * @return all the OwnIdentities fetched from WoT
	 * @throws PluginNotFoundException If the WoT plugin isn't loaded
	 */
	List<OwnIdentity> getAllOwnIdentities() throws PluginNotFoundException, IOException, TimeoutException, WoTException;

	List<Identity> getAllIdentities() throws PluginNotFoundException, IOException, TimeoutException, WoTException;

	/**
	 * Returns all the identities that are trusted by the given identity, or {@code null} if an
	 * error occurs. Trusted identities are those with a score of 0 or higher.
	 * @param trusterId the identity id of the truster
	 * @return all the identities that are trusted by the given identity
	 * @throws NullPointerException if trusterId is {@code null}
	 * @throws PluginNotFoundException If the WoT plugin isn't loaded
	 */
	Set<Identity> getAllTrustedIdentities(String trusterId) throws PluginNotFoundException, IOException, TimeoutException, WoTException;

	/**
	 * Returns all the identities that are not trusted by the given identity, or {@code null} if an
	 * error occurs.
	 * @param trusterId the identity id of the truster
	 * @return all the identities that are not trusted by the given identity
	 * @throws NullPointerException if trusterId is {@code null}
	 * @throws PluginNotFoundException If the WoT plugin isn't loaded
	 */
	Set<Identity> getAllUntrustedIdentities(String trusterId) throws PluginNotFoundException, IOException, TimeoutException, WoTException;

	/**
	 * Returns the Identity with the given identity string. The truster parameter is used to fetch
	 * the trust and score of the identity and must be a valid OwnIdentity.
	 * @param identityId the id of the Identity that should be fetched
	 * @param trusterId the id of the OwnIdentity to use for fetching the trust and score
	 * @return the Identity with the given identity string
	 * @throws NullPointerException if any of the parameters are {@code null}
	 * @throws PluginNotFoundException If the WoT plugin isn't loaded
	 */
	Identity getIdentity(String identityId, String trusterId) throws PluginNotFoundException, IOException, TimeoutException, WoTException;

	/**
	 * Sets the property {@code key} to {@code value} for the given identity, returning {@code true}
	 * if the property was successfully set and {@code false} otherwise.
	 * @param identityId the id of the identity whose parameter should be set
	 * @param key the name of the parameter
	 * @param value the new value of the parameter
	 * @return {@code true} if the parameter was successfully set
	 * @throws NullPointerException if any of the parameters are {@code null}
	 * @throws PluginNotFoundException If the WoT plugin isn't loaded
	 */
	boolean setProperty(String identityId, String key, String value) throws PluginNotFoundException, TimeoutException, IOException;

	/**
	 * Returns the value of the named parameter from the given identity. {@code null} is returned if
	 * the parameter doesn't exist, or if an error occurs.
	 * @param identityId the id of the identity whose parameter should be fetched
	 * @param key the name of the parameter to be fetched
	 * @return the value of the parameter, or {@code null}
	 * @throws NullPointerException if any of the parameters are {@code null}
	 * @throws PluginNotFoundException If the WoT plugin isn't loaded
	 */
	String getProperty(String identityId, String key) throws PluginNotFoundException, IOException, TimeoutException, WoTException;

	/**
	 * Sets the context {@code context} for the given identity, returning {@code true} if the
	 * context was successfully set and {@code false} otherwise.
	 * @param identityId the id of the identity whose context should be set
	 * @param context the context that should be set
	 */
	boolean setContext(String identityId, String context) throws PluginNotFoundException, IOException, TimeoutException;
}
