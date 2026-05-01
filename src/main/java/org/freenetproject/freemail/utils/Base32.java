package org.freenetproject.freemail.utils;

import java.util.Arrays;
import java.util.stream.IntStream;

/**
 * RFC-compliant Base32 encoder and decoder.
 *
 * @see <a href="https://www.rfc-editor.org/rfc/rfc4648">RFC 4648</a>
 */
public class Base32 {

	/**
	 * Encodes the given byte array using the Base32 encoding and returns
	 * the result string.
	 *
	 * @param bytes The bytes to encode
	 * @return The Base32-encoded string
	 */
	public static String encode(byte[] bytes) {
		String encodedValue = encodeWithoutPadding(bytes);

		// now pad with as many ‘=’ as needed to make the length a multiple of 8
		return encodedValue + "======".substring(0, (8 - (encodedValue.length() % 8)) % 8);
	}

	/**
	 * Encodes the given byte array using the Base32 encoding and returns
	 * the result string without any padding.
	 *
	 * <p>
	 * Omitting the padding is a violation of the RFC, so use this with caution.
	 * </p>
	 *
	 * @param bytes The bytes to encode
	 * @return The string without any padding
	 */
	public static String encodeWithoutPadding(byte[] bytes) {
		StringBuilder result = new StringBuilder();
		int buffer = 0;
		int availableBits = 0;

		// shift each byte into a buffer and then use as many 5-bit units as possible
		for (byte b : bytes) {
			buffer = (buffer << 8) | (b & 0xff);
			availableBits += 8;
			while (availableBits >= 5) {
				result.append(base32Characters[(buffer >> (availableBits - 5)) & 0x1f]);
				availableBits -= 5;
			}
		}

		// one character might be left, append it
		if (availableBits > 0) {
			result.append(base32Characters[(buffer << (5 - availableBits)) & 0x1f]);
		}

		return result.toString();
	}

	/**
	 * Decodes the given Base32-encoded string and returns the resulting
	 * byte array. Non-alphabet characters are ignored.
	 *
	 * @param encodedValue The Base32-encoded string
	 * @return The decoded byte array
	 */
	public static byte[] decode(String encodedValue) {
		byte[] output = new byte[encodedValue.length() * 5 / 8];
		int buffer = 0;
		int availableBits = 0;
		int totalBytes = 0;

		for (char character : encodedValue.toCharArray()) {
			int decodedValue = (character < base32DecodedValues.length) ? base32DecodedValues[character] : -1;
			if (decodedValue == -1) {
				continue;
			}
			buffer = (buffer << 5) | decodedValue;
			availableBits += 5;
			if (availableBits >= 8) {
				output[totalBytes++] = (byte) (buffer >> (availableBits - 8));
				availableBits -= 8;
			}
		}

		return Arrays.copyOf(output, totalBytes);
	}

	private static final char[] base32Characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567".toCharArray();
	private static final int[] base32DecodedValues = generateBase32DecodedValues();

	private static int[] generateBase32DecodedValues() {
		int[] result = new int[128];
		Arrays.fill(result, (byte) -1);
		IntStream.range(0, base32Characters.length).forEach(index -> {
			result[base32Characters[index]] = index;
			// add the lower-case letters as well; for the numbers, ORing
			// them with 0x20 doesn’t do anything, as that bit is already set.
			result[base32Characters[index] | 0x20] = index;
		});
		return result;
	}

}
