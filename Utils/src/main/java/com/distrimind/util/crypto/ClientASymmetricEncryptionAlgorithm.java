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

import gnu.vm.jgnu.security.InvalidKeyException;
import gnu.vm.jgnu.security.NoSuchAlgorithmException;
import gnu.vm.jgnu.security.NoSuchProviderException;
import gnu.vm.jgnu.security.spec.InvalidKeySpecException;
import gnu.vm.jgnux.crypto.NoSuchPaddingException;

/**
 * 
 * @author Jason Mahdjoub
 * @version 3.0
 * @since Utils 1.7
 */
public class ClientASymmetricEncryptionAlgorithm extends AbstractEncryptionOutputAlgorithm {
	private final ASymmetricPublicKey distantPublicKey;

	private final ASymmetricAuthentifiedSignatureCheckerAlgorithm signatureChecker;

	private final ASymmetricEncryptionType type;

	private final ASymmetricAuthentifiedSignatureType signatureType;

	private final int maxBlockSize;

	public ClientASymmetricEncryptionAlgorithm(ASymmetricPublicKey distantPublicKey) throws NoSuchAlgorithmException, NoSuchPaddingException,
			InvalidKeyException, InvalidKeySpecException, NoSuchProviderException {
		super(distantPublicKey.getEncryptionAlgorithmType().getCipherInstance());
		this.type = distantPublicKey.getEncryptionAlgorithmType();
		this.distantPublicKey = distantPublicKey;
		this.signatureType = distantPublicKey.getAuthentifiedSignatureAlgorithmType();
		this.signatureChecker = new ASymmetricAuthentifiedSignatureCheckerAlgorithm(distantPublicKey);
		initCipherForEncrypt(this.cipher);
		this.maxBlockSize = distantPublicKey.getMaxBlockSize();
	}

	@Override
	protected AbstractCipher getCipherInstance() throws NoSuchAlgorithmException, NoSuchPaddingException, NoSuchProviderException {
		return type.getCipherInstance();
	}

	public ASymmetricPublicKey getDistantPublicKey() {
		return this.distantPublicKey;
	}

	@Override
	public int getMaxBlockSizeForEncoding() {
		return maxBlockSize;
	}

	public ASymmetricAuthentifiedSignatureCheckerAlgorithm getSignatureCheckerAlgorithm() {
		return signatureChecker;
	}

	public ASymmetricAuthentifiedSignatureType getSignatureType() {
		return signatureType;
	}

	@Override
	protected boolean includeIV() {
		return false;
	}

	@Override
	public void initCipherForEncrypt(AbstractCipher _cipher)
			throws InvalidKeyException, NoSuchAlgorithmException, InvalidKeySpecException, NoSuchProviderException {
		initCipherForEncryptAndNotChangeIV(_cipher);
	}

	@Override
	public void initCipherForEncryptAndNotChangeIV(AbstractCipher _cipher)
			throws InvalidKeyException, NoSuchAlgorithmException, InvalidKeySpecException, NoSuchProviderException {
		_cipher.init(Cipher.ENCRYPT_MODE, distantPublicKey);

	}

}