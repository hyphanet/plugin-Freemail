/*
 * FreemailAccount.java
 * This file is part of Freemail, copyright (C) 2008 Dave Baker
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

package freemail;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.archive.util.Base32;

import freemail.transport.Channel;
import freemail.utils.Logger;
import freemail.utils.PropsFile;
import freenet.support.Base64;
import freenet.support.IllegalBase64Exception;

public class FreemailAccount {
	private final String identity;
	private final String nickname;
	private final File accdir;
	private final PropsFile accprops;
	private final MessageBank mb;
	private final Map<String, Channel> channels = new HashMap<String, Channel>();
	
	FreemailAccount(String identity, String nickname, File _accdir, PropsFile _accprops) {
		this.identity = identity;
		this.nickname = nickname;
		accdir = _accdir;
		accprops = _accprops;
		mb = new MessageBank(this);

		//Create and start all the channels
		File channelDir = new File(accdir, "channels");
		if(!channelDir.exists()) {
			if(!channelDir.mkdir()) {
				Logger.error(this, "Couldn't create channel directory: " + channelDir);
			}
		}

		for(File f : channelDir.listFiles()) {
			Channel channel = new Channel(f, FreemailPlugin.getExecutor());
			channel.startTasks();
			channels.put(f.getName(), channel);
		}
	}
	
	public String getUsername() {
		return identity;
	}
	
	public File getAccountDir() {
		return accdir;
	}
	
	public PropsFile getProps() {
		return accprops;
	}
	
	public MessageBank getMessageBank() {
		return mb;
	}

	public String getNickname() {
		return nickname;
	}

	public String getAddressDomain() {
		try {
			return Base32.encode(Base64.decode(identity)) + ".freemail";
		} catch(IllegalBase64Exception e) {
			//This would mean that WoT has changed the encoding of the identity string
			throw new AssertionError("Got IllegalBase64Exception when decoding " + identity);
		}
	}

	public Channel getChannel(String remoteIdentity) {
		Channel channel = channels.get(remoteIdentity);
		if(channel == null) {
			File channelsDir = new File(accdir, "channels");
			File newChannelDir = new File(channelsDir, remoteIdentity);
			if(!newChannelDir.mkdir()) {
				Logger.error(this, "Couldn't create the channel directory");
				return null;
			}

			channel = new Channel(newChannelDir, FreemailPlugin.getExecutor());
			channel.startTasks();
			channels.put(remoteIdentity, channel);
		}

		return channel;
	}
}
