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

import freemail.fcp.HighLevelFCPClient;
import freemail.transport.Channel;
import freemail.utils.Logger;
import freemail.utils.PropsFile;
import freenet.support.Base64;
import freenet.support.IllegalBase64Exception;

public class FreemailAccount {
	private final String identity;
	private String nickname = null;
	private final File accdir;
	private final PropsFile accprops;
	private final MessageBank mb;
	private final Map<String, Channel> channels = new HashMap<String, Channel>();
	private final Freemail freemail;
	
	FreemailAccount(String identity, File _accdir, PropsFile _accprops, Freemail freemail) {
		this.identity = identity;
		accdir = _accdir;
		accprops = _accprops;
		mb = new MessageBank(this);
		this.freemail = freemail;

		//Create and start all the channels
		File channelDir = new File(accdir, "channels");
		if(!channelDir.exists()) {
			if(!channelDir.mkdir()) {
				Logger.error(this, "Couldn't create channel directory: " + channelDir);
			}
		}

		for(File f : channelDir.listFiles()) {
			Channel channel = new Channel(f, FreemailPlugin.getExecutor(), new HighLevelFCPClient(), freemail, this);
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

	public synchronized String getNickname() {
		return nickname;
	}

	public synchronized void setNickname(String nickname) {
		this.nickname = nickname;
		synchronized(accprops) {
			accprops.put("nickname", nickname);
		}
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

			channel = new Channel(newChannelDir, FreemailPlugin.getExecutor(), new HighLevelFCPClient(), freemail, this);
			channel.startTasks();
			channels.put(remoteIdentity, channel);
		}

		return channel;
	}

	public Channel createChannelFromRTS(PropsFile rtsProps) {
		String remoteIdentity = rtsProps.get("mailsite");
		remoteIdentity = remoteIdentity.substring(remoteIdentity.indexOf("@") + 1); //Strip USK@
		remoteIdentity = remoteIdentity.substring(0, remoteIdentity.indexOf(","));

		Channel channel = channels.get(remoteIdentity);
		if(channel != null) {
			Logger.debug(this, "Got RTS for existing channel");
			channel.processRTS(rtsProps);
		} else {
			File channelsDir = new File(accdir, "channels");
			File newChannelDir = new File(channelsDir, remoteIdentity);
			if(!newChannelDir.mkdir()) {
				Logger.error(this, "Couldn't create the channel directory");
				return null;
			}

			channel = new Channel(newChannelDir, FreemailPlugin.getExecutor(), new HighLevelFCPClient(), freemail, this);
			channel.processRTS(rtsProps);
			channel.startTasks();
			channels.put(remoteIdentity, channel);
		}

		return channel;
	}
}
