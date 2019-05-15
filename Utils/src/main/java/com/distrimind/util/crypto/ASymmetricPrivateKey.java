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

import java.security.PrivateKey;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.RSAPrivateKey;
import java.util.Arrays;

import org.apache.commons.codec.binary.Base64;
import org.bouncycastle.crypto.Algorithm;
import org.bouncycastle.crypto.AsymmetricKey;
import org.bouncycastle.crypto.asymmetric.AsymmetricECPrivateKey;
import org.bouncycastle.crypto.asymmetric.AsymmetricRSAPrivateKey;

import com.distrimind.util.Bits;

import gnu.vm.jgnu.security.NoSuchAlgorithmException;
import gnu.vm.jgnu.security.spec.InvalidKeySpecException;


/**
 * 
 * @author Jason Mahdjoub
 * @version 2.3
 * @since Utils 1.7.1
 */
public class ASymmetricPrivateKey extends Key {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1279365581082525690L;


	// private final PrivateKey privateKey;
	private byte[] privateKey;

	private final short keySizeBits;

	private ASymmetricEncryptionType encryptionType;
	private ASymmetricAuthentifiedSignatureType signatureType;

	private final int hashCode;

	private volatile transient PrivateKey nativePrivateKey;

	private volatile transient gnu.vm.jgnu.security.PrivateKey gnuPrivateKey;
	boolean xdhKey=false;

	
	@Override
	public void zeroize()
	{
		if (privateKey!=null)
		{
			Arrays.fill(privateKey, (byte)0);
			privateKey=null;
		}
		if (nativePrivateKey!=null)
		{
			Arrays.fill(nativePrivateKey.getEncoded(), (byte)0);
			nativePrivateKey=null;
		}
		if (gnuPrivateKey!=null)
		{
			Arrays.fill(gnuPrivateKey.getEncoded(), (byte)0);
			gnuPrivateKey=null;
		}
	}



    @Override
    byte[] getKeyBytes() {
        return privateKey;
    }

    ASymmetricPrivateKey(ASymmetricEncryptionType type, byte[] privateKey, short keySize) {
		this(privateKey, keySize);
		if (type == null)
			throw new NullPointerException("type");
		this.encryptionType = type;
		this.signatureType=null;
	}
	ASymmetricPrivateKey(ASymmetricAuthentifiedSignatureType type, byte[] privateKey, short keySize) {
		this(privateKey, keySize);
		if (type == null)
			throw new NullPointerException("type");
		this.encryptionType = null;
		this.signatureType=type;
	}

	ASymmetricPrivateKey(ASymmetricEncryptionType type, gnu.vm.jgnu.security.PrivateKey privateKey, short keySize) {
		this(privateKey, keySize);
		if (type == null)
			throw new NullPointerException("type");
		this.encryptionType = type;
		this.signatureType=null;
		
	}
	ASymmetricPrivateKey(ASymmetricAuthentifiedSignatureType type, gnu.vm.jgnu.security.PrivateKey privateKey, short keySize) {
		this(privateKey, keySize);
		if (type == null)
			throw new NullPointerException("type");
		this.encryptionType = null;
		this.signatureType=type;
	}

	ASymmetricPrivateKey(ASymmetricEncryptionType type, PrivateKey privateKey, short keySize) {
		this(ASymmetricEncryptionType.encodePrivateKey(privateKey, type), keySize);
		if (type == null)
			throw new NullPointerException("type");
		if (type.getCodeProviderForEncryption() == CodeProvider.GNU_CRYPTO)
			throw new IllegalAccessError();
		this.encryptionType = type;
		this.signatureType=null;
	}
	ASymmetricPrivateKey(ASymmetricAuthentifiedSignatureType type, PrivateKey privateKey, short keySize, boolean xdhKey) {
		this(ASymmetricEncryptionType.encodePrivateKey(privateKey, type, xdhKey), keySize);
		if (type == null)
			throw new NullPointerException("type");
		if (type.getCodeProviderForSignature() == CodeProvider.GNU_CRYPTO)
			throw new IllegalAccessError();
		this.encryptionType = null;
		this.signatureType=type;
		this.xdhKey=xdhKey;
	}

	ASymmetricPrivateKey getNewClonedPrivateKey()
	{
		if (signatureType==null)
			return new ASymmetricPrivateKey(encryptionType, privateKey.clone(), keySizeBits);
		else
			return new ASymmetricPrivateKey(signatureType, privateKey.clone(), keySizeBits);
	}

	private ASymmetricPrivateKey(byte[] privateKey, short keySize) {
		if (privateKey == null)
			throw new NullPointerException("privateKey");
		if (keySize < 256)
			throw new IllegalArgumentException("keySize");
		this.privateKey = privateKey;
		this.keySizeBits = keySize;
		hashCode = Arrays.hashCode(privateKey);
	}

	private ASymmetricPrivateKey(gnu.vm.jgnu.security.PrivateKey privateKey, short keySize) {
		if (privateKey == null)
			throw new NullPointerException("privateKey");
		if (keySize < 256)
			throw new IllegalArgumentException("keySize");
		this.privateKey = ASymmetricEncryptionType.encodePrivateKey(privateKey);
		this.keySizeBits = keySize;
		hashCode = Arrays.hashCode(this.privateKey);
		this.gnuPrivateKey=null;
	}



	static int getEncodedTypeSize()
	{
		int max=Math.max(ASymmetricEncryptionType.values().length, ASymmetricAuthentifiedSignatureType.values().length);
		if (max<=0xFF)
			return 1;
		else if (max<=0xFFFF)
			return 2;
		else if (max<=0xFFFFFF)
			return 3;
		else
			return 4;
	}
	@Override
	public byte[] encode(boolean includeTimeExpiration) {
		return encode();
	}

	public byte[] encode() {
		int codedTypeSize=getEncodedTypeSize();
		byte[] tab = new byte[3+codedTypeSize+privateKey.length];

		tab[0]=encryptionType==null?(byte)((xdhKey?Key.IS_XDH_KEY:0)|2):(byte)3;
		Bits.putShort(tab, 1, keySizeBits);
		Bits.putPositiveInteger(tab, 3, encryptionType==null?signatureType.ordinal():encryptionType.ordinal(), codedTypeSize);
        System.arraycopy(privateKey, 0, tab, codedTypeSize+3, privateKey.length);
        return tab;
	}

	@Override
	public boolean equals(Object o) {
		if (o == null)
			return false;
		if (o == this)
			return true;
		if (o instanceof ASymmetricPrivateKey) {
			ASymmetricPrivateKey other = (ASymmetricPrivateKey) o;
			return keySizeBits == other.keySizeBits && encryptionType == other.encryptionType && signatureType == other.signatureType && Arrays.equals(privateKey, other.privateKey);
		}
		return false;
	}

	public ASymmetricEncryptionType getEncryptionAlgorithmType() {
		return encryptionType;
	}
	public ASymmetricAuthentifiedSignatureType getAuthentifiedSignatureAlgorithmType() {
		return signatureType;
	}

	byte[] getBytesPrivateKey() {
		return privateKey;
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

	@Override
	public gnu.vm.jgnu.security.PrivateKey toGnuKey()
			throws gnu.vm.jgnu.security.NoSuchAlgorithmException, gnu.vm.jgnu.security.spec.InvalidKeySpecException {
		if (gnuPrivateKey == null)
			gnuPrivateKey = ASymmetricEncryptionType.decodeGnuPrivateKey(privateKey, encryptionType==null?signatureType.getKeyGeneratorAlgorithmName():encryptionType.getAlgorithmName());

		return gnuPrivateKey;
	}

	@Override
	public PrivateKey toJavaNativeKey()
			throws gnu.vm.jgnu.security.NoSuchAlgorithmException, gnu.vm.jgnu.security.spec.InvalidKeySpecException {
		if (nativePrivateKey == null)
			nativePrivateKey = ASymmetricEncryptionType.decodeNativePrivateKey(privateKey, encryptionType==null?signatureType.getKeyGeneratorAlgorithmName():encryptionType.getAlgorithmName(),
					encryptionType==null?signatureType.name():encryptionType.name(), xdhKey);

		return nativePrivateKey;
	}

	@Override
	public String toString() {
		return Base64.encodeBase64URLSafeString(encode());
	}
	
	Algorithm getBouncyCastleAlgorithm()
	{
		if (encryptionType==null)
			return signatureType.getBouncyCastleAlgorithm();
		else
			return encryptionType.getBouncyCastleAlgorithm();
					
	}

	@Override
	public AsymmetricKey toBouncyCastleKey() throws NoSuchAlgorithmException, InvalidKeySpecException {
		PrivateKey pk=toJavaNativeKey();
		if (pk instanceof RSAPrivateKey)
		{
			RSAPrivateKey javaNativePrivateKey=(RSAPrivateKey)pk;
			return new AsymmetricRSAPrivateKey(getBouncyCastleAlgorithm(),
				javaNativePrivateKey.getModulus(), javaNativePrivateKey.getPrivateExponent());

		}
		else if (pk instanceof ECPrivateKey)
		{
			ECPrivateKey javaNativePrivateKey=(ECPrivateKey)pk;
			return new AsymmetricECPrivateKey(getBouncyCastleAlgorithm(), javaNativePrivateKey.getEncoded());
		}
		else
			throw new IllegalAccessError();
		
	}
}
