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
import java.util.LinkedList;
import java.util.List;

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
	private final List<Channel> channels = new LinkedList<Channel>();
	private final Freemail freemail;

	private int nextChannelNum = 0;
	
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
			if(!f.isDirectory()) {
				Logger.debug(this, "Spurious file in channel directory: " + f);
				continue;
			}

			try {
				int num = Integer.parseInt(f.getName());
				if(num >= nextChannelNum) {
					nextChannelNum = num + 1;
				}
			} catch(NumberFormatException e) {
				Logger.debug(this, "Found directory with malformed name: " + f);
				continue;
			}

			Channel channel = new Channel(f, FreemailPlugin.getExecutor(), new HighLevelFCPClient(), freemail, this);
			channels.add(channel);
		}
	}
	
	public void startTasks() {
		synchronized(channels) {
			for(Channel c : channels) {
				c.startTasks();
			}
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
		synchronized(channels) {
			for(Channel c : channels) {
				if(remoteIdentity.equals(c.getRemoteIdentity())) {
					return c;
				}
			}

			//The channel didn't exist, so create a new one
			File channelsDir = new File(accdir, "channels");
			File newChannelDir = new File(channelsDir, "" + nextChannelNum++);
			if(!newChannelDir.mkdir()) {
				Logger.error(this, "Couldn't create the channel directory");
				return null;
			}

			Channel channel = new Channel(newChannelDir, FreemailPlugin.getExecutor(), new HighLevelFCPClient(), freemail, this);
			channel.setRemoteIdentity(remoteIdentity);
			channel.startTasks();
			channels.add(channel);

			return channel;
		}
	}

	public Channel createChannelFromRTS(PropsFile rtsProps) {
		//First try to find a channel with the same key
		String rtsPrivateKey = rtsProps.get("channel");

		synchronized(channels) {
			for(Channel c : channels) {
				if(rtsPrivateKey.equals(c.getPrivateKey())) {
					c.processRTS(rtsProps);
					return c;
				}
			}

			//Create a new channel from the RTS values
			Logger.debug(this, "Creating new channel from RTS");
			File channelsDir = new File(accdir, "channels");
			File newChannelDir = new File(channelsDir, "" + nextChannelNum++);
			if(!newChannelDir.mkdir()) {
				Logger.error(this, "Couldn't create the channel directory");
				return null;
			}

			String remoteIdentity = rtsProps.get("mailsite");
			remoteIdentity = remoteIdentity.substring(remoteIdentity.indexOf("@") + 1); //Strip USK@
			remoteIdentity = remoteIdentity.substring(0, remoteIdentity.indexOf(","));

			Channel channel = new Channel(newChannelDir, FreemailPlugin.getExecutor(), new HighLevelFCPClient(), freemail, this);
			channel.setRemoteIdentity(remoteIdentity);
			channel.processRTS(rtsProps);
			channel.startTasks();
			channels.add(channel);

			return channel;
		}
	}
}
