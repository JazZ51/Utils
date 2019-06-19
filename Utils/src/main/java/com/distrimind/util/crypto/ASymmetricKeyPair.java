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

import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;

import com.distrimind.util.DecentralizedValue;
import org.apache.commons.codec.binary.Base64;

import com.distrimind.util.Bits;

/**
 * 
 * @author Jason Mahdjoub
 * @version 3.3
 * @since Utils 1.7.1
 */
public class ASymmetricKeyPair extends DecentralizedValue {
	/**
	 * 
	 */
	private static final long serialVersionUID = -8249147431069134363L;



	public static ASymmetricKeyPair valueOf(String key) throws IllegalArgumentException {
		return decode(Base64.decodeBase64(key));
	}

	private ASymmetricPrivateKey privateKey;

	private ASymmetricPublicKey publicKey;

	private final short keySizeBits;

	private final ASymmetricEncryptionType encryptionType;
	private final ASymmetricAuthenticatedSignatureType signatureType;

	private final int hashCode;

	private transient volatile KeyPair nativeKeyPair;

	private transient volatile Object gnuKeyPair;

	public void zeroize()
	{
		privateKey=null;
		publicKey=null;
		if (nativeKeyPair!=null)
		{
			Arrays.fill(nativeKeyPair.getPublic().getEncoded(), (byte)0);
			Arrays.fill(nativeKeyPair.getPrivate().getEncoded(), (byte)0);
			nativeKeyPair=null;
		}
		if (gnuKeyPair!=null)
		{
			Arrays.fill(GnuFunctions.keyGetEncoded(GnuFunctions.getPublicKey(gnuKeyPair)), (byte)0);
			Arrays.fill(GnuFunctions.keyGetEncoded(GnuFunctions.getPrivateKey(gnuKeyPair)), (byte)0);
			gnuKeyPair=null;
		}
	}
	
	@SuppressWarnings("deprecation")
	@Override public void finalize()
	{
		zeroize();
	}
	
	ASymmetricKeyPair(ASymmetricEncryptionType type, ASymmetricPrivateKey privateKey, ASymmetricPublicKey publicKey,
			short keySize) {
		if (type == null)
			throw new NullPointerException("type");
		if (privateKey == null)
			throw new NullPointerException("privateKey");
		if (publicKey == null)
			throw new NullPointerException("publicKey");
		if (keySize < 256)
			throw new IllegalArgumentException("keySize");
		this.privateKey = privateKey;
		this.publicKey = publicKey;
		this.keySizeBits = keySize;
		this.encryptionType = type;
		this.signatureType=null;

		hashCode = privateKey.hashCode() + publicKey.hashCode();
	}

	public ASymmetricKeyPair(ASymmetricPrivateKey privateKey, ASymmetricPublicKey publicKey) {
		if (privateKey == null)
			throw new NullPointerException("privateKey");
		if (publicKey == null)
			throw new NullPointerException("publicKey");
		if (privateKey.getAuthentifiedSignatureAlgorithmType()!=publicKey.getAuthentifiedSignatureAlgorithmType())
			throw new IllegalArgumentException();
		if (privateKey.getEncryptionAlgorithmType()!=publicKey.getEncryptionAlgorithmType())
			throw new IllegalArgumentException();
		this.privateKey = privateKey;
		this.publicKey = publicKey;
		this.keySizeBits = publicKey.getKeySizeBits();
		this.encryptionType = publicKey.getEncryptionAlgorithmType();
		this.signatureType=privateKey.getAuthentifiedSignatureAlgorithmType();

		hashCode = privateKey.hashCode() + publicKey.hashCode();
	}

	public ASymmetricKeyPair getKeyPairWithNewExpirationTime(long timeExpirationUTC)
	{
		return new ASymmetricKeyPair(this.privateKey.getNewClonedPrivateKey(), this.publicKey.getPublicKeyWithNewExpirationTime(timeExpirationUTC));
	}

	ASymmetricKeyPair(ASymmetricEncryptionType type, Object keyPair, short keySize,
			long expirationUTC) {
		if (type == null)
			throw new NullPointerException("type");
		if (keyPair == null)
			throw new NullPointerException("keyPair");
		if (keySize < 256)
			throw new IllegalArgumentException("keySize");
		privateKey = new ASymmetricPrivateKey(type, GnuFunctions.getPrivateKey(keyPair), keySize);
		publicKey = new ASymmetricPublicKey(type, GnuFunctions.getPublicKey(keyPair), keySize, expirationUTC);
		this.keySizeBits = keySize;
		this.encryptionType = type;
		this.signatureType=null;

		hashCode = privateKey.hashCode() + publicKey.hashCode();
		this.gnuKeyPair=keyPair;
	}

	ASymmetricKeyPair(ASymmetricEncryptionType type, KeyPair keyPair, short keySize, long expirationUTC) {
		if (type == null)
			throw new NullPointerException("type");
		if (keyPair == null)
			throw new NullPointerException("keyPair");
		if (keySize < 256)
			throw new IllegalArgumentException("keySize");
		privateKey = new ASymmetricPrivateKey(type, keyPair.getPrivate(), keySize);
		publicKey = new ASymmetricPublicKey(type, keyPair.getPublic(), keySize, expirationUTC);
		this.keySizeBits = keySize;
		this.encryptionType = type;
		this.signatureType=null;

		hashCode = privateKey.hashCode() + publicKey.hashCode();
		this.nativeKeyPair=keyPair;
	}

	ASymmetricKeyPair(ASymmetricAuthenticatedSignatureType type, ASymmetricPrivateKey privateKey, ASymmetricPublicKey publicKey,
					  short keySize) {
		if (type == null)
			throw new NullPointerException("type");
		if (privateKey == null)
			throw new NullPointerException("privateKey");
		if (publicKey == null)
			throw new NullPointerException("publicKey");
		if (keySize < 256)
			throw new IllegalArgumentException("keySize");
		this.privateKey = privateKey;
		this.publicKey = publicKey;
		this.keySizeBits = keySize;
		this.encryptionType = null;
		this.signatureType=type;

		hashCode = privateKey.hashCode() + publicKey.hashCode();
	}

	ASymmetricKeyPair(ASymmetricAuthenticatedSignatureType type, Object keyPair, short keySize,
					  long expirationUTC) {
		if (type == null)
			throw new NullPointerException("type");
		if (keyPair == null)
			throw new NullPointerException("keyPair");
		if (keySize < 256)
			throw new IllegalArgumentException("keySize");
		privateKey = new ASymmetricPrivateKey(type, GnuFunctions.getPrivateKey(keyPair), keySize);
		publicKey = new ASymmetricPublicKey(type, GnuFunctions.getPublicKey(keyPair), keySize, expirationUTC);
		this.keySizeBits = keySize;
		this.encryptionType = null;
		this.signatureType=type;

		hashCode = privateKey.hashCode() + publicKey.hashCode();
		this.gnuKeyPair=keyPair;
	}

	ASymmetricKeyPair(ASymmetricAuthenticatedSignatureType type, KeyPair keyPair, short keySize, long expirationUTC, boolean xdhKey) {
		if (type == null)
			throw new NullPointerException("type");
		if (keyPair == null)
			throw new NullPointerException("keyPair");
		if (keySize < 256)
			throw new IllegalArgumentException("keySize");
		privateKey = new ASymmetricPrivateKey(type, keyPair.getPrivate(), keySize, xdhKey);
		publicKey = new ASymmetricPublicKey(type, keyPair.getPublic(), keySize, expirationUTC, xdhKey);
		this.keySizeBits = keySize;
		this.encryptionType = null;
		this.signatureType=type;

		hashCode = privateKey.hashCode() + publicKey.hashCode();
		this.nativeKeyPair=keyPair;
	}
	
	
	public byte[] encode(boolean includeTimeExpiration) {
		int codedTypeSize=ASymmetricPrivateKey.getEncodedTypeSize();
		byte[] kp=Bits.concateEncodingWithShortSizedTabs(privateKey.getBytesPrivateKey(), publicKey.getBytesPublicKey());
		byte[] tab = new byte[3+codedTypeSize+kp.length+(includeTimeExpiration?8:0)];
		tab[0]=encryptionType==null?(byte)9:(byte)8;
		if (includeTimeExpiration)
			tab[0]|=Key.INCLUDE_KEY_EXPIRATION_CODE;
		if (privateKey.xdhKey)
			tab[0]|=Key.IS_XDH_KEY;
		Bits.putShort(tab, 1, keySizeBits);
		Bits.putPositiveInteger(tab, 3, encryptionType==null?signatureType.ordinal():encryptionType.ordinal(), codedTypeSize);
		int pos=3+codedTypeSize;
		if (includeTimeExpiration) {
			Bits.putLong(tab, 3 + codedTypeSize, publicKey.getTimeExpirationUTC());
			pos += 8;
		}
		System.arraycopy(kp, 0, tab, pos, kp.length);
		return tab;
	}
	public static boolean isValidType(byte[] b, int off)
	{
		byte type=b[off];
		type&=~Key.INCLUDE_KEY_EXPIRATION_CODE;
		type&=~Key.IS_XDH_KEY;
		return type>=8 && type<=9;
	}
	public static ASymmetricKeyPair decode(byte[] b) throws IllegalArgumentException {
		return decode(b, true);
	}
	public static ASymmetricKeyPair decode(byte[] b, int off, int len) throws IllegalArgumentException {
		return decode(b, off, len,true);
	}
	public static ASymmetricKeyPair decode(byte[] b, boolean fillArrayWithZerosWhenDecoded) throws IllegalArgumentException {
		return decode(b, 0, b.length, fillArrayWithZerosWhenDecoded);
	}

	@Override
	public byte[] encodeWithDefaultParameters() {
		return encode(true);
	}

	public static ASymmetricKeyPair decode(byte[] b, int off, int len, boolean fillArrayWithZerosWhenDecoded) throws IllegalArgumentException {
		if (off<0 || len<0 || len+off>b.length)
			throw new IllegalArgumentException();

		try {
			int codedTypeSize = SymmetricSecretKey.getEncodedTypeSize();
			short keySize = Bits.getShort(b, 1+off);
			int posKey=codedTypeSize+3+off;
			long expirationUTC;
			boolean includeKeyExpiration=(b[off] & Key.INCLUDE_KEY_EXPIRATION_CODE) == Key.INCLUDE_KEY_EXPIRATION_CODE;
			boolean kdhKey=(b[off] & Key.IS_XDH_KEY) == Key.IS_XDH_KEY;
			if (includeKeyExpiration)
				b[off]-=Key.INCLUDE_KEY_EXPIRATION_CODE;
			if (kdhKey)
				b[off]-=Key.IS_XDH_KEY;
			if (includeKeyExpiration) {

				expirationUTC=Bits.getLong(b, posKey);
				posKey += 8;
			}
			else
				expirationUTC=Long.MAX_VALUE;

			byte[] kp = new byte[len - 3 - codedTypeSize-(includeKeyExpiration?8:0)];
			System.arraycopy(b, posKey, kp, 0, kp.length);
			byte[][] keys = Bits.separateEncodingsWithShortSizedTabs(kp);

			if (b[off] == 9) {
				ASymmetricAuthenticatedSignatureType type = ASymmetricAuthenticatedSignatureType.valueOf((int) Bits.getPositiveInteger(b, 3+off, codedTypeSize));

				ASymmetricKeyPair res=new ASymmetricKeyPair(type, new ASymmetricPrivateKey(type, keys[0], keySize),
						new ASymmetricPublicKey(type, keys[1], keySize, expirationUTC), keySize);
				res.getASymmetricPublicKey().xdhKey=kdhKey;
				res.getASymmetricPrivateKey().xdhKey=kdhKey;
				return res;
			} else if (b[off] == 8) {
				ASymmetricEncryptionType type = ASymmetricEncryptionType.valueOf((int) Bits.getPositiveInteger(b, 3+off, codedTypeSize));

				ASymmetricKeyPair res=new ASymmetricKeyPair(type, new ASymmetricPrivateKey(type, keys[0], keySize),
						new ASymmetricPublicKey(type, keys[1], keySize, expirationUTC), keySize);
				res.getASymmetricPublicKey().xdhKey=kdhKey;
				res.getASymmetricPrivateKey().xdhKey=kdhKey;
				return res;
			} else
				throw new IllegalArgumentException();
		}
		finally {
			if (fillArrayWithZerosWhenDecoded)
				Arrays.fill(b, off, len, (byte)0);
		}



    }
	@Override
	public boolean equals(Object o) {
		if (o == null)
			return false;
		if (o == this)
			return true;
		if (o instanceof ASymmetricKeyPair) {
			ASymmetricKeyPair other = ((ASymmetricKeyPair) o);
			return privateKey.equals(other.privateKey) && publicKey.equals(other.publicKey) && keySizeBits == other.keySizeBits
					&& encryptionType == other.encryptionType && signatureType == other.signatureType;
		}
		return false;
	}

	public long getTimeExpirationUTC() {
		return publicKey.getTimeExpirationUTC();
	}

	public ASymmetricEncryptionType getEncryptionAlgorithmType() {
		return encryptionType;
	}
	public ASymmetricAuthenticatedSignatureType getAuthentifiedSignatureAlgorithmType() {
		return signatureType;
	}

	public ASymmetricPrivateKey getASymmetricPrivateKey() {
		return privateKey;
	}

	public ASymmetricPublicKey getASymmetricPublicKey() {
		return publicKey;
	}

	public short getKeySizeBits() {
		return keySizeBits;
	}

	public int getMaxBlockSize() {
		if (encryptionType==null)
			throw new IllegalAccessError("This key should be used for signature");
		else
			return encryptionType.getMaxBlockSize(keySizeBits);
	}

	@Override
	public int hashCode() {
		return hashCode;
	}

	public Object toGnuKeyPair()throws NoSuchAlgorithmException, InvalidKeySpecException
	{
		if (gnuKeyPair == null)
			gnuKeyPair = GnuFunctions.getKeyPairInstance(publicKey.toGnuKey(), privateKey.toGnuKey());

		return gnuKeyPair;
	}

	/*
	 * public static ASymmetricKeyPair generate(SecureRandom random) throws
	 * NoSuchAlgorithmException { return generate(random,
	 * ASymmetricEncryptionType.DEFAULT,
	 * ASymmetricEncryptionType.DEFAULT.getDefaultKeySize()); }
	 * 
	 * public static ASymmetricKeyPair generate(SecureRandom random,
	 * ASymmetricEncryptionType type) throws NoSuchAlgorithmException { return
	 * generate(random, type, type.getDefaultKeySize()); }
	 * 
	 * public static ASymmetricKeyPair generate(SecureRandom random,
	 * ASymmetricEncryptionType type, short keySize) throws NoSuchAlgorithmException
	 * { return new ASymmetricKeyPair(type, type.getKeyPairGenerator(random,
	 * keySize).generateKeyPair(), keySize); }
	 */

	public KeyPair toJavaNativeKeyPair()
			throws NoSuchAlgorithmException, InvalidKeySpecException {
		if (nativeKeyPair == null)
			nativeKeyPair = new KeyPair(publicKey.toJavaNativeKey(), privateKey.toJavaNativeKey());

		return nativeKeyPair;
	}

	@Override
	public String toString() {
		try {
			return Base64.encodeBase64URLSafeString(encode(true));
		} catch (Exception e) {
			return e.toString();
		}
	}

}
