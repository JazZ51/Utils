/*
Copyright or © or Copr. Jason Mahdjoub (04/02/2016)

jason.mahdjoub@distri-mind.fr

This software (Utils) is a computer program whose purpose is to give several kind of tools for developers 
(ciphers, XML readers, decentralized id generators, etc.).

This software is governed by the CeCILL-C license under French law and
abiding by the rules of distribution of free software.  You can  use, 
modify and/ or redistribute the software under the terms of the CeCILL-C
license as circulated by CEA, CNRS and INRIA at the following URL
"http://www.cecill.info". 

As a counterpart to the access to the source code and  rights to copy,
modify and redistribute granted by the license, users are provided only
with a limited warranty  and the software's author,  the holder of the
economic rights,  and the successive licensors  have only  limited
liability. 

In this respect, the user's attention is drawn to the risks associated
with loading,  using,  modifying and/or developing or reproducing the
software by the user in light of its specific status of free software,
that may mean  that it is complicated to manipulate,  and  that  also
therefore means  that it is reserved for developers  and  experienced
professionals having in-depth computer knowledge. Users are therefore
encouraged to load and test the software's suitability as regards their
requirements in conditions enabling the security of their systems and/or 
data to be ensured and,  more generally, to use and operate it in the 
same conditions as regards security. 

The fact that you are presently reading this means that you have had
knowledge of the CeCILL-C license and that you accept its terms.
 */
package com.distrimind.util.crypto;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Random;

import javax.crypto.Cipher;

import gnu.vm.jgnu.security.InvalidAlgorithmParameterException;
import gnu.vm.jgnu.security.InvalidKeyException;
import gnu.vm.jgnu.security.NoSuchAlgorithmException;
import gnu.vm.jgnu.security.NoSuchProviderException;
import gnu.vm.jgnu.security.SecureRandom;
import gnu.vm.jgnu.security.spec.InvalidKeySpecException;
import gnu.vm.jgnux.crypto.BadPaddingException;
import gnu.vm.jgnux.crypto.IllegalBlockSizeException;
import gnu.vm.jgnux.crypto.NoSuchPaddingException;

/**
 * 
 * @author Jason Mahdjoub
 * @version 2.1
 * @since Utils 1.4.1
 */
public class P2PASymmetricSecretMessageExchanger {
	static class FakeSecureRandom extends SecureRandom {

		/**
		 * 
		 */
		private static final long serialVersionUID = -3862260428441022619L;

		private Random random = null;

		protected FakeSecureRandom() {
			random = new Random();
		}

		
		@Override
		public byte[] generateSeed(int numBytes) {
			return null;
		}

		@Override
		public String getAlgorithm() {
			return "Fake Secure Random";
		}

		@Override
		public boolean nextBoolean() {
			return random.nextBoolean();
		}

		@Override
		synchronized public void nextBytes(byte[] bytes) {
			random.nextBytes(bytes);
		}

		@Override
		public double nextDouble() {
			return random.nextDouble();
		}

		@Override
		public float nextFloat() {
			return random.nextFloat();
		}

		@Override
		public double nextGaussian() {
			return random.nextGaussian();
		}

		@Override
		public int nextInt() {
			return random.nextInt();
		}

		@Override
		public int nextInt(int bound) {
			return random.nextInt(bound);
		}

		@Override
		public long nextLong() {
			return random.nextLong();
		}

		@Override
		public void setSeed(byte[] seed) {
			BigInteger num = new BigInteger(seed);
			random.setSeed(num.mod(maxLongValue).longValue());
		}

		@Override
		public void setSeed(long seed) {
			if (random != null)
				random.setSeed(seed);
		}
	}

	protected static BigInteger maxLongValue = BigInteger.valueOf(1).shiftLeft(63);

	private static AbstractCipher getCipherInstancePriv(ASymmetricEncryptionType type)
			throws NoSuchAlgorithmException, NoSuchPaddingException {
		return type.getCipherInstance();
	}

	private final ASymmetricPublicKey myPublicKey;

	private final ASymmetricEncryptionType type;

	private P2PASymmetricSecretMessageExchanger distantMessageEncoder;

	private final GnuSecureRandom random;

	private final AbstractCipher cipher;

	private final AbstractMessageDigest messageDigest, messageDigest256;

	private final MessageDigestType messageDigestType;

	private final PasswordHashType passwordHashType;

	private int hashIterationsNumber = PasswordHash.DEFAULT_NB_ITERATIONS;

	public P2PASymmetricSecretMessageExchanger(ASymmetricPublicKey myPublicKey)
			throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException,
			InvalidAlgorithmParameterException, InvalidKeySpecException, NoSuchProviderException {
		this(MessageDigestType.SHA_512, PasswordHashType.DEFAULT, myPublicKey);
	}

	public P2PASymmetricSecretMessageExchanger(MessageDigestType messageDigestType, PasswordHashType passwordHashType,
			ASymmetricPublicKey myPublicKey) throws NoSuchAlgorithmException, NoSuchPaddingException,
			InvalidKeyException, InvalidAlgorithmParameterException, InvalidKeySpecException, NoSuchProviderException {
		this(messageDigestType, passwordHashType, myPublicKey, null);
	}

	public P2PASymmetricSecretMessageExchanger(MessageDigestType messageDigestType, PasswordHashType passwordHashType,
			ASymmetricPublicKey myPublicKey, byte[] distantPublicKey)
			throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidKeySpecException,
			InvalidAlgorithmParameterException, NoSuchProviderException {
		if (messageDigestType == null)
			throw new NullPointerException("messageDigestType");
		if (myPublicKey == null)
			throw new NullPointerException("myPublicKey");
		if (passwordHashType == null)
			throw new NullPointerException("passwordHashType");
		this.type = myPublicKey.getAlgorithmType();
		this.myPublicKey = myPublicKey;

		if (distantPublicKey != null)
			setDistantPublicKey(distantPublicKey);
		random = new GnuSecureRandom(new FakeSecureRandom());
		cipher = getCipherInstancePriv(type);
		this.messageDigestType = messageDigestType;
		this.messageDigest = messageDigestType.getMessageDigestInstance();
		this.messageDigest.reset();
		this.passwordHashType = passwordHashType;
		this.messageDigest256 = MessageDigestType.BOUNCY_CASTLE_SHA3_256.getMessageDigestInstance();
	}

	public byte[] encode(byte[] message, byte[] salt, boolean messageIsKey) throws IOException, InvalidKeyException,
			IllegalBlockSizeException, BadPaddingException, NoSuchAlgorithmException, InvalidKeySpecException,
			NoSuchPaddingException, InvalidAlgorithmParameterException, NoSuchProviderException {
		if (salt == null)
			salt = new byte[0];
		return encode(message, 0, message.length, salt, 0, salt.length, messageIsKey);
	}

	public byte[] encode(byte[] message, int offset, int len, byte[] salt, int offset_salt, int len_salt,
			boolean messageIsKey) throws IOException, InvalidKeyException, IllegalBlockSizeException,
			BadPaddingException, NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException,
			InvalidAlgorithmParameterException, NoSuchProviderException {
		if (message == null)
			throw new NullPointerException("message");
		if (message.length - offset < len)
			throw new IllegalArgumentException("message");
		if (salt != null && salt.length - offset_salt < len_salt)
			throw new IllegalArgumentException("salt");

		message = hashMessage(messageDigest, message, offset, len, salt, offset_salt, len_salt, messageIsKey);
		byte[] encodedLevel2 = encodeLevel2(message);
		return encodeLevel1(encodedLevel2, message, salt, offset_salt, len_salt);
	}

	private byte[] encodeLevel2(byte hashedMessage[])
			throws InvalidKeyException, NoSuchAlgorithmException, InvalidKeySpecException, IOException,
			IllegalStateException, IllegalBlockSizeException, BadPaddingException {
		initCipherForEncrypt(hashedMessage);

		int maxBlockSize = myPublicKey.getMaxBlockSize();

		boolean finish = false;
		int offset = 0;
		try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
			while (!finish) {

				int size = Math.min(maxBlockSize, hashedMessage.length - offset);
				if (size > 0) {
					baos.write(cipher.doFinal(hashedMessage, offset, size));
					offset += size;
				}
				if (size <= 0)
					finish = true;
			}
			baos.flush();
			return baos.toByteArray();
		}
	}

	private byte[] encodeLevel1(byte encodedLevel2[], byte[] hashedMessage, byte salt[], int offset_salt, int len_salt)
			throws NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException, NoSuchPaddingException,
			InvalidAlgorithmParameterException, NoSuchProviderException, BadPaddingException, IllegalStateException,
			IllegalBlockSizeException, IOException {
		hashedMessage = hashMessage(messageDigest256, hashedMessage, 0, hashedMessage.length, salt, offset_salt,
				len_salt, false);

		byte randomSeed[] = new byte[16];
		random.nextBytes(randomSeed);
		SymmetricEncryptionAlgorithm sea = new SymmetricEncryptionAlgorithm(
				new SymmetricSecretKey(SymmetricEncryptionType.GNU_TWOFISH,
						new gnu.vm.jgnux.crypto.spec.SecretKeySpec(hashedMessage,
								SymmetricEncryptionType.GNU_TWOFISH.getAlgorithmName()),
						(short) 256),
				SecureRandomType.DEFAULT, randomSeed);
		return sea.encode(OutputDataPackagerWithRandomValues.encode(encodedLevel2, encodedLevel2.length));
	}

	private byte[] decodeLevel2(byte encodedLevel1[], int off_encodedlevel1, int len_encodedlevel1,
			byte[] hashedMessage, byte salt[], int offset_salt, int len_salt)
			throws NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException, NoSuchPaddingException,
			InvalidAlgorithmParameterException, NoSuchProviderException, BadPaddingException, IllegalStateException,
			IllegalBlockSizeException, IOException {

		hashedMessage = hashMessage(messageDigest256, hashedMessage, 0, hashedMessage.length, salt, offset_salt,
				len_salt, false);

		byte randomSeed[] = new byte[16];
		random.nextBytes(randomSeed);
		SymmetricEncryptionAlgorithm sea = new SymmetricEncryptionAlgorithm(
				new SymmetricSecretKey(SymmetricEncryptionType.GNU_TWOFISH,
						new gnu.vm.jgnux.crypto.spec.SecretKeySpec(hashedMessage,
								SymmetricEncryptionType.GNU_TWOFISH.getAlgorithmName()),
						(short) 256),
				SecureRandomType.DEFAULT, randomSeed);
		byte[] v = sea.decode(encodedLevel1, off_encodedlevel1, len_encodedlevel1);
		try {
			return InputDataPackagedWithRandomValues.decode(v);
		} catch (Throwable e) {
			return null;
		}
	}

	public byte[] encode(char[] message, byte[] salt) throws IOException, InvalidKeyException,
			IllegalBlockSizeException, BadPaddingException, NoSuchAlgorithmException, InvalidKeySpecException,
			NoSuchPaddingException, InvalidAlgorithmParameterException, NoSuchProviderException, IllegalStateException {
		if (salt == null)
			salt = new byte[0];
		return encode(message, salt, 0, salt.length);
	}

	public byte[] encode(char[] message, byte[] salt, int offset_salt, int len_salt)
			throws IOException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException,
			NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException,
			InvalidAlgorithmParameterException, NoSuchProviderException, IllegalStateException {
		if (message == null)
			throw new NullPointerException("message");
		if (salt == null) {
			salt = new byte[0];
			offset_salt = 0;
			len_salt = 0;
		}
		if (salt.length - offset_salt < len_salt)
			throw new IllegalArgumentException("salt");

		byte hashedMessage[] = hashMessage(messageDigest, message, salt, offset_salt, len_salt);
		byte encodedLevel2[] = encodeLevel2(hashedMessage);
		return encodeLevel1(encodedLevel2, hashedMessage, salt, offset_salt, len_salt);
	}

	public byte[] encode(String message, byte[] salt) throws IOException, InvalidKeyException,
			IllegalBlockSizeException, BadPaddingException, NoSuchAlgorithmException, InvalidKeySpecException,
			NoSuchPaddingException, InvalidAlgorithmParameterException, NoSuchProviderException, IllegalStateException {
		return encode(message.toCharArray(), salt);
	}

	public byte[] encode(String message, byte[] salt, int offset_salt, int len_salt)
			throws IOException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException,
			NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException,
			InvalidAlgorithmParameterException, NoSuchProviderException, IllegalStateException {
		return encode(message.toCharArray(), salt, offset_salt, len_salt);
	}

	public byte[] encodeMyPublicKey() {
		return myPublicKey.encode();
	}

	public ASymmetricPublicKey getDistantPublicKey() {
		if (distantMessageEncoder == null)
			return null;
		return this.distantMessageEncoder.getMyPublicKey();
	}

	public int getHashIterationsNumber() {
		return hashIterationsNumber;
	}

	public ASymmetricPublicKey getMyPublicKey() {
		return myPublicKey;
	}

	private byte[] hashMessage(AbstractMessageDigest messageDigest, byte data[], int off, int len, byte[] salt,
			int offset_salt, int len_salt, boolean messageIsKey)
			throws NoSuchAlgorithmException, InvalidKeySpecException {
		if (!messageIsKey && salt != null && len_salt > 0) {
			byte s[] = new byte[len_salt];
			System.arraycopy(salt, offset_salt, s, 0, len_salt);
			data = passwordHashType.hash(data, off, len, s, hashIterationsNumber);
			off = 0;
			len = data.length;
		}
		messageDigest.update(data, off, len);
		if (messageIsKey && salt != null && len_salt > 0)
			messageDigest.update(salt, offset_salt, len_salt);
		return messageDigest.digest();
	}

	private byte[] hashMessage(AbstractMessageDigest messageDigest, char password[], byte[] salt, int offset_salt,
			int len_salt) throws NoSuchAlgorithmException, InvalidKeySpecException {
		if (salt == null)
			throw new NullPointerException(
					"salt can't be null when the message is a password (and not a secret key) !");
		if (salt.length == 0)
			throw new NullPointerException(
					"salt can't be empty when the message is a password (and not a secret key) !");

		if (salt != null && len_salt > 0) {
			byte s[] = new byte[len_salt];
			System.arraycopy(salt, offset_salt, s, 0, len_salt);
			byte[] res = passwordHashType.hash(password, s, hashIterationsNumber);

			return hashMessage(messageDigest, res, 0, res.length, null, -1, -1, true);
		} else {
			byte[] res = new byte[password.length * 2];
			for (int i = 0; i < password.length; i++) {
				res[i * 2] = (byte) (password[i] & 0xFF);
				res[i * 2 + 1] = (byte) ((password[i] >> 8) & 0xFF);
			}
			return hashMessage(messageDigest, res, 0, res.length, null, -1, -1, true);
		}

	}

	private void initCipherForEncrypt(byte hashedMessage[])
			throws InvalidKeyException, NoSuchAlgorithmException, InvalidKeySpecException {
		random.setSeed(hashedMessage);
		cipher.init(Cipher.ENCRYPT_MODE, myPublicKey, random);
		messageDigest.reset();
	}

	public void setDistantPublicKey(byte[] distantPublicKeyAndIV)
			throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeySpecException, InvalidKeyException,
			InvalidAlgorithmParameterException, NoSuchProviderException {
		distantMessageEncoder = new P2PASymmetricSecretMessageExchanger(messageDigestType, passwordHashType,
				ASymmetricPublicKey.decode(distantPublicKeyAndIV));
		if (myPublicKey.equals(distantMessageEncoder.myPublicKey))
			throw new IllegalArgumentException("Local public key equals distant public key");
		distantMessageEncoder.setHashIterationsNumber(getHashIterationsNumber());
	}

	public void setHashIterationsNumber(int _hashIterationsNumber) {
		hashIterationsNumber = _hashIterationsNumber;
		if (distantMessageEncoder != null)
			distantMessageEncoder.setHashIterationsNumber(_hashIterationsNumber);
	}

	public boolean verifyDistantMessage(byte[] originalMessage, byte[] salt, byte[] distantMessage,
			boolean messageIsKey) throws InvalidKeyException, IOException, IllegalAccessException,
			IllegalBlockSizeException, BadPaddingException, NoSuchAlgorithmException, InvalidKeySpecException {
		if (salt == null)
			salt = new byte[0];
		return this.verifyDistantMessage(originalMessage, 0, originalMessage.length, salt, 0, salt.length,
				distantMessage, 0, distantMessage.length, messageIsKey);
	}

	public boolean verifyDistantMessage(byte[] originalMessage, int offo, int leno, byte[] salt, int offset_salt,
			int len_salt, byte[] distantMessage, int offd, int lend, boolean messageIsKey)
			throws InvalidKeyException, IOException, IllegalAccessException, IllegalBlockSizeException,
			BadPaddingException, NoSuchAlgorithmException, InvalidKeySpecException {
		if (originalMessage == null)
			throw new NullPointerException("message");
		if (originalMessage.length - offo < leno)
			throw new IllegalArgumentException("message");
		if (distantMessage == null)
			throw new NullPointerException("distantMessage");
		if (distantMessage.length - offd < lend)
			throw new IllegalArgumentException("distantMessage");
		if (salt == null) {
			salt = new byte[0];
			offset_salt = 0;
			len_salt = 0;
		}
		if (salt.length - offset_salt < len_salt)
			throw new IllegalArgumentException("salt");

		if (distantMessageEncoder == null)
			throw new IllegalAccessException("You must set the distant public key before calling this function ! ");

		byte hashedMessage[] = distantMessageEncoder.hashMessage(messageDigest, originalMessage, offo, leno, salt,
				offset_salt, len_salt, messageIsKey);
		byte encodedLevel2[] = distantMessageEncoder.encodeLevel2(hashedMessage);
		try {
			byte distantLevel2[] = distantMessageEncoder.decodeLevel2(distantMessage, offd, lend, hashedMessage, salt,
					offset_salt, len_salt);

			return compareEncodedLevel2(encodedLevel2, distantLevel2);
		} catch (Throwable e) {
			return false;
		}
	}

	private boolean compareEncodedLevel2(byte encodedLevel2[], byte distantLevel2[]) {
		if (distantLevel2 == null)
			return false;
		if (encodedLevel2.length != distantLevel2.length)
			return false;
		for (int i = 0; i < encodedLevel2.length; i++) {
			if (encodedLevel2[i] != distantLevel2[i])
				return false;
		}
		return true;
	}

	public boolean verifyDistantMessage(char[] originalMessage, byte[] salt, byte[] distantMessage)
			throws InvalidKeyException, IOException, IllegalAccessException, IllegalStateException,
			NoSuchAlgorithmException, InvalidKeySpecException, IllegalBlockSizeException, BadPaddingException {
		if (salt == null)
			salt = new byte[0];
		return this.verifyDistantMessage(originalMessage, salt, 0, salt.length, distantMessage, 0,
				distantMessage.length);
	}

	public boolean verifyDistantMessage(char[] originalMessage, byte[] salt, int offset_salt, int len_salt,
			byte[] distantMessage, int offd, int lend)
			throws IllegalAccessException, IllegalStateException, NoSuchAlgorithmException, InvalidKeySpecException,
			InvalidKeyException, IllegalBlockSizeException, BadPaddingException, IOException {
		if (originalMessage == null)
			throw new NullPointerException("message");
		if (distantMessage == null)
			throw new NullPointerException("distantMessage");
		if (distantMessage.length - offd < lend)
			throw new IllegalArgumentException("distantMessage");
		if (salt == null) {
			salt = new byte[0];
			offset_salt = 0;
			len_salt = 0;
		}
		if (salt.length - offset_salt < len_salt)
			throw new IllegalArgumentException("salt");

		if (distantMessageEncoder == null)
			throw new IllegalAccessException("You must set the distant public key before calling this function ! ");

		byte hashedMessage[] = distantMessageEncoder.hashMessage(messageDigest, originalMessage, salt, offset_salt,
				len_salt);
		byte encodedLevel2[] = distantMessageEncoder.encodeLevel2(hashedMessage);
		try {
			byte distantLevel2[] = distantMessageEncoder.decodeLevel2(distantMessage, offd, lend, hashedMessage, salt,
					offset_salt, len_salt);

			return compareEncodedLevel2(encodedLevel2, distantLevel2);
		} catch (Throwable e) {
			return false;
		}
	}

	public boolean verifyDistantMessage(String originalMessage, byte[] salt, byte[] distantMessage)
			throws InvalidKeyException, IOException, IllegalAccessException, IllegalBlockSizeException,
			BadPaddingException, NoSuchAlgorithmException, InvalidKeySpecException, IllegalStateException {
		return this.verifyDistantMessage(originalMessage.toCharArray(), salt, distantMessage);
	}

}