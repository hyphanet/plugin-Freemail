/*
 * MesssageBankTools.java
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

package freemail.support;

import freemail.FreemailAccount;
import freemail.MailMessage;
import freemail.MessageBank;
import freemail.utils.Logger;

public class MessageBankTools {
	/**
	 * Returns the message bank indicated by {@code folderName}, or null if it
	 * could not be found. The name of the subfolder uses '.' as a hierarchy delimiter.
	 * @param account the owner of the subfolder
	 * @param folderName the name of the folder that should be returned
	 * @return the message bank indicated by {@code folderName}
	 * @throws NullPointerException if any of the parameters are {@code null}
	 */
	public static MessageBank getMessageBank(FreemailAccount account, String folderName) {
		if(account == null) throw new NullPointerException("Parameter account was null");
		if(folderName == null) throw new NullPointerException("Parameter folderName was null");

		MessageBank topLevel = account.getMessageBank();
		if(!folderName.startsWith(topLevel.getName())) {
			throw new IllegalArgumentException("Parameter folderName has wrong value: " + folderName);
		}

		if(folderName.equals(topLevel.getName())) {
			return topLevel;
		}
		return getMessageBank(topLevel, folderName.substring(topLevel.getName().length()));
	}

	/**
	 * Returns the subfolder of {@code messageBank} indicated by {@code folderName}, or null if it
	 * could not be found. The name of the subfolder uses '.' as a hierarchy delimiter.
	 * @param messageBank the message bank that should be searched
	 * @param folderName the name of the folder that should be returned
	 * @return the subfolder of {@code messageBank} indicated by {@code folderName}
	 * @throws NullPointerException if any of the parameters are {@code null}
	 */
	public static MessageBank getMessageBank(MessageBank messageBank, String folderName) {
		if(messageBank == null) throw new NullPointerException("Parameter messageBank was null");
		if(folderName == null) throw new NullPointerException("Parameter folderName was null");

		MessageBank folder = messageBank;
		for(String name : folderName.split("\\.")) {
			folder = folder.getSubFolder(name);
			if(folder == null) {
				Logger.debug(MessageBankTools.class, "Returning null for folder " + folderName);
				return null;
			}
		}
		return folder;
	}

	/**
	 * Returns a {@code MailMessage} with the specified uid from {@code messageBank}, or
	 * {@code null} if no such message exists.
	 * @param messageBank the {@code MessageBank} that should be searched
	 * @param messageUid the uid of the message that should be returned
	 * @return a {@code MailMessage} with the specified uid from {@code messageBank}
	 * @throws NullPointerException if {@code messageBank} is {@code null}
	 */
	public static MailMessage getMessage(MessageBank messageBank, int messageUid) {
		if(messageBank == null) throw new NullPointerException("Parameter messageBank was null");
		if(messageUid < 0) throw new IllegalArgumentException("Parameter messageUid was < 0: " + messageUid);

		return messageBank.listMessages().get(Integer.valueOf(messageUid));
	}
}
