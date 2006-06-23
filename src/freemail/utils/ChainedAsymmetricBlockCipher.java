package freemail.utils;

import java.io.IOException;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;

import org.bouncycastle.crypto.AsymmetricBlockCipher;
import org.bouncycastle.crypto.InvalidCipherTextException;

/*
 * A wrapper around AsymmetricBlockCipher to chain several blocks together.
 * This class just concatentates them, ie. without CBC or suchlike.
 *
 * Clearly this is intended for use with small amounts of data, where it's not worthwhile encrypting a symmetric key and using that
 */
public class ChainedAsymmetricBlockCipher {
	public static byte[] encrypt(AsymmetricBlockCipher cipher, byte[] in) throws InvalidCipherTextException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ByteArrayInputStream bis = new ByteArrayInputStream(in);
		
		int read;
		byte[] buf = new byte[cipher.getInputBlockSize()];
		
		while ( (read = bis.read(buf, 0, cipher.getInputBlockSize())) > 0) {
			byte[] obuf = cipher.processBlock(buf, 0, read);
			try {
				bos.write(obuf);
			} catch (IOException ioe) {
				throw new InvalidCipherTextException();
			}
		}
		
		return bos.toByteArray();
	}
}
