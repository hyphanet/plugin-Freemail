package org.freenetproject.freemail.utils;

import org.junit.Test;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.freenetproject.freemail.utils.Base32.decode;
import static org.freenetproject.freemail.utils.Base32.encode;
import static org.freenetproject.freemail.utils.Base32.encodeWithoutPadding;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class Base32Test {

	@Test
	public void decodingAnEmptyStringResultsInAnEmptyArray() {
		assertThat(decode(""), equalTo(new byte[0]));
	}

	@Test
	public void encodingAnEmptyArrayResultsInAnEmptyString() {
		assertThat(encode(new byte[0]), equalTo(""));
	}

	@Test
	public void encodingAnEmptyArrayWithoutPaddingResultsInAnEmptyString() {
		assertThat(encode(new byte[0]), equalTo(""));
	}

	@Test
	public void encodingASingleByteResultsInAnEightCharacterString() {
		assertThat(encode(bytes(0xff)), equalTo("74======"));
	}

	@Test
	public void encodingASingleByteWithoutPaddingResultsInATwoCharacterString() {
		assertThat(encodeWithoutPadding(bytes(0xff)), equalTo("74"));
	}

	@Test
	public void twoBytesAreEncodedCorrectly() {
		assertThat(encode(bytes(0x55, 0xaa)), equalTo("KWVA===="));
	}

	@Test
	public void twoBytesAreEncodedWithoutPaddingCorrectly() {
		assertThat(encodeWithoutPadding(bytes(0x55, 0xaa)), equalTo("KWVA"));
	}

	@Test
	public void threeBytesAreEncodedCorrectly() {
		assertThat(encode(bytes(0xaa, 0x55, 0xff)), equalTo("VJK76==="));
	}

	@Test
	public void threeBytesAreEncodedWithoutPaddingCorrectly() {
		assertThat(encodeWithoutPadding(bytes(0xaa, 0x55, 0xff)), equalTo("VJK76"));
	}

	@Test
	public void fourBytesAreEncodedCorrectly() {
		assertThat(encode(bytes(0x12, 0x34, 0x56, 0x78)), equalTo("CI2FM6A="));
	}

	@Test
	public void fourBytesAreEncodedWithoutPaddingCorrectly() {
		assertThat(encodeWithoutPadding(bytes(0x12, 0x34, 0x56, 0x78)), equalTo("CI2FM6A"));
	}

	@Test
	public void fiveBytesAreEncodedCorrectly() {
		assertThat(encode(bytes(0xff, 0xfe, 0xfd, 0xfc, 0xfb)), equalTo("777P37H3"));
	}

	@Test
	public void fiveBytesAreEncodedWithoutPaddingCorrectly() {
		assertThat(encodeWithoutPadding(bytes(0xff, 0xfe, 0xfd, 0xfc, 0xfb)), equalTo("777P37H3"));
	}

	@Test
	public void decodingAnEightCharacterStringReturnsTheCorrectByte() {
		assertThat(decode("74======"), equalTo(bytes(0xff)));
	}

	// https://datatracker.ietf.org/doc/html/draft-ietf-sasl-gssapi-00, § 2.1
	@Test
	public void exampleDataFromSaslGssapiDraftIsEncodedCorrectly() {
		assertThat(encode(bytes(0x57, 0xee, 0x81, 0x82, 0x4e, 0xac, 0x4d, 0xb0, 0xe6, 0x50)), equalTo("K7XIDASOVRG3BZSQ"));
	}

	// https://datatracker.ietf.org/doc/html/draft-ietf-sasl-gssapi-00, § 2.1
	@Test
	public void exampleDataFromSaslGssapiDraftIsDecodedCorrectly() {
		assertThat(decode("K7XIDASOVRG3BZSQ"), equalTo(bytes(0x57, 0xee, 0x81, 0x82, 0x4e, 0xac, 0x4d, 0xb0, 0xe6, 0x50)));
	}

	@Test
	public void testVectorsFromRfc4648AreEncodedCorrectly() {
		assertThat(encode("f".getBytes(UTF_8)), equalTo("MY======"));
		assertThat(encode("fo".getBytes(UTF_8)), equalTo("MZXQ===="));
		assertThat(encode("foo".getBytes(UTF_8)), equalTo("MZXW6==="));
		assertThat(encode("foob".getBytes(UTF_8)), equalTo("MZXW6YQ="));
		assertThat(encode("fooba".getBytes(UTF_8)), equalTo("MZXW6YTB"));
		assertThat(encode("foobar".getBytes(UTF_8)), equalTo("MZXW6YTBOI======"));
	}

	@Test
	public void testVectorsFromRfc4648AreDecodedCorrectly() {
		assertThat(decode("MY======"), equalTo(bytes('f')));
		assertThat(decode("MZXQ===="), equalTo(bytes('f', 'o')));
		assertThat(decode("MZXW6==="), equalTo(bytes('f', 'o', 'o')));
		assertThat(decode("MZXW6YQ="), equalTo(bytes('f', 'o', 'o', 'b')));
		assertThat(decode("MZXW6YTB"), equalTo(bytes('f', 'o', 'o', 'b', 'a')));
		assertThat(decode("MZXW6YTBOI======"), equalTo(bytes('f', 'o', 'o', 'b', 'a', 'r')));
	}

	@Test
	public void lowercaseAlphabetCharactersAreDecodedCorrectly() {
		assertThat(decode("hntq3blvijjhyg4t4cdq===="), equalTo(bytes(0x3b, 0x67, 0x0d, 0x85, 0x75, 0x42, 0x52, 0x7c, 0x1b, 0x93, 0xe0, 0x87)));
	}

	@Test
	public void missingPaddingIsIgnored() {
		assertThat(decode("HNTQ3BLVIJJHYG4T4CDQ="), equalTo(bytes(0x3b, 0x67, 0x0d, 0x85, 0x75, 0x42, 0x52, 0x7c, 0x1b, 0x93, 0xe0, 0x87)));
	}

	@Test
	public void nonAlphabetCharactersAreIgnored() {
		assertThat(decode("ßK87-X%I$D#A/S!O'V\"RäG930B(Z)S}Q["), equalTo(bytes(0x57, 0xee, 0x81, 0x82, 0x4e, 0xac, 0x4d, 0xb0, 0xe6, 0x50)));
	}

	private static byte[] bytes(int... values) {
		byte[] result = new byte[values.length];
		for (int index = 0; index < values.length; index++) {
			result[index] = (byte) values[index];
		}
		return result;
	}

}
