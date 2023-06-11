/*
 * GenerateMessage.java
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

public class GenerateMessage {
	public static void main(String[] args) throws IOException {
		Map<String, String> headers = new HashMap<String, String>();

		for(int i = 0; i < args.length; i += 2) {
			String key = args[i];
			String value = args[i + 1];
			headers.put(key, value);
		}

		PrintWriter output = new PrintWriter(System.out);
		for(Map.Entry<String, String> header : headers.entrySet()) {
			output.print(header.getKey() + ": " + header.getValue() + "\r\n");
		}

		output.print("\r\n"); //End of headers

		BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
		for(String line = input.readLine(); line != null; line = input.readLine()) {
			output.print(line + "\r\n");
		}

		output.close();
	}
}
