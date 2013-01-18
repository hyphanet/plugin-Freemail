/*
 * NullMessageHandler.java
 * This file is part of Freemail
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

package fakes;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.freenetproject.freemail.Freemail;
import org.freenetproject.freemail.FreemailAccount;
import org.freenetproject.freemail.fcp.HighLevelFCPClientFactory;
import org.freenetproject.freemail.transport.MessageHandler;
import org.freenetproject.freemail.utils.PropsFile;
import org.freenetproject.freemail.wot.Identity;

import freenet.support.api.Bucket;

public abstract class NullMessageHandler extends MessageHandler {
	public NullMessageHandler(File outbox, Freemail freemail, File channelDir,
	                          FreemailAccount freemailAccount, HighLevelFCPClientFactory hlFcpClientFactory) {
		super(outbox, freemail, channelDir, freemailAccount, hlFcpClientFactory);
	}

	@Override
	public void start() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean sendMessage(List<Identity> recipients, Bucket message) throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void createChannelFromRTS(PropsFile rtsProps) {
		throw new UnsupportedOperationException();
	}

	@Override
	public List<OutboxMessage> listOutboxMessages() throws IOException {
		throw new UnsupportedOperationException();
	}
}
