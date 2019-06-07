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

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.spec.InvalidKeySpecException;

/**
 * 
 * @author Jason Mahdjoub
 * @version 3.0
 * @since Utils 1.4
 */
public class P2PASymmetricEncryptionAlgorithm extends AbstractEncryptionIOAlgorithm {
	private final ASymmetricKeyPair myKeyPair;

	private final ASymmetricPublicKey distantPublicKey;

	private final ASymmetricEncryptionType type;

	private final ASymmetricAuthenticatedSignatureType signatureType;

	private final int maxBlockSizeForEncoding, maxBlockSizeForDecoding;

	public P2PASymmetricEncryptionAlgorithm(ASymmetricKeyPair myKeyPair, ASymmetricPublicKey distantPublicKey)
			throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidKeySpecException,
			NoSuchProviderException, InvalidAlgorithmParameterException {
		this(myKeyPair.getEncryptionAlgorithmType().getDefaultSignatureAlgorithm(), myKeyPair, distantPublicKey);
	}

	public P2PASymmetricEncryptionAlgorithm(ASymmetricAuthenticatedSignatureType signatureType, ASymmetricKeyPair myKeyPair,
											ASymmetricPublicKey distantPublicKey) throws NoSuchAlgorithmException, NoSuchPaddingException,
			InvalidKeyException, InvalidKeySpecException, NoSuchProviderException, InvalidAlgorithmParameterException {
		super(myKeyPair.getEncryptionAlgorithmType().getCipherInstance(), 0);
		if (signatureType == null)
			throw new NullPointerException("signatureType");
		if (distantPublicKey == null)
			throw new NullPointerException("distantPublicKey");

		this.type = myKeyPair.getEncryptionAlgorithmType();
		this.myKeyPair = myKeyPair;
		this.distantPublicKey = distantPublicKey;
		this.signatureType = signatureType;
		// initCipherForEncrypt(this.cipher);
		this.maxBlockSizeForEncoding = myKeyPair.getMaxBlockSize();
		initCipherForEncrypt(this.cipher);
		this.maxBlockSizeForDecoding = cipher.getOutputSize(this.maxBlockSizeForEncoding);
		initBufferAllocatorArgs();
	}

	@Override
	protected AbstractCipher getCipherInstance() throws NoSuchAlgorithmException, NoSuchPaddingException, NoSuchProviderException {
		return type.getCipherInstance();
	}

	public ASymmetricPublicKey getDistantPublicKey() {
		return this.distantPublicKey;
	}

	@Override
	public int getMaxBlockSizeForDecoding() {
		return maxBlockSizeForDecoding;
	}

	@Override
	public int getMaxBlockSizeForEncoding() {
		return maxBlockSizeForEncoding;
	}

	public ASymmetricKeyPair getMyKeyPair() {
		return this.myKeyPair;
	}

	public ASymmetricAuthenticatedSignatureType getSignatureType() {
		return signatureType;
	}

	@Override
	protected boolean includeIV() {
		return false;
	}

	@Override
	public void initCipherForDecrypt(AbstractCipher _cipher, byte[] iv, byte[] externalCounter)
			throws InvalidKeyException, NoSuchAlgorithmException, InvalidKeySpecException, NoSuchProviderException {
		_cipher.init(Cipher.DECRYPT_MODE, myKeyPair.getASymmetricPrivateKey());
	}

	@Override
	public void initCipherForEncrypt(AbstractCipher _cipher, byte[] externalCounter)
			throws InvalidKeyException, NoSuchAlgorithmException, InvalidKeySpecException, NoSuchProviderException {
		initCipherForEncryptAndNotChangeIV(_cipher);
	}

	@Override
	public void initCipherForEncryptAndNotChangeIV(AbstractCipher _cipher)
			throws InvalidKeyException, NoSuchAlgorithmException, InvalidKeySpecException, NoSuchProviderException {
		_cipher.init(Cipher.ENCRYPT_MODE, distantPublicKey);

	}

	@Override
	public int getIVSizeBytesWithExternalCounter() {
		return 0;
	}

}
