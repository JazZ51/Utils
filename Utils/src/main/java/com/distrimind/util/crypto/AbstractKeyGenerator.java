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


import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

/**
 * 
 * @author Jason Mahdjoub
 * @version 2.0
 * @since Utils 2.0
 */
public abstract class AbstractKeyGenerator {
	protected final SymmetricEncryptionType encryptionType;
	protected final SymmetricAuthenticatedSignatureType signatureType;

	AbstractKeyGenerator(SymmetricEncryptionType type) {
		this.encryptionType = type;
		this.signatureType = null;
	}
	AbstractKeyGenerator(SymmetricAuthenticatedSignatureType type) {
		this.encryptionType = null;
		this.signatureType = type;
	}

	/**
	 * Generate a key.
	 *
	 * @return The new key.
	 */
	public abstract SymmetricSecretKey generateKey();

	/**
	 * Return the name of this key generator.
	 *
	 * @return The algorithm name.
	 */
	public abstract String getAlgorithm();

	/**
	 * Return the provider of the underlying implementation.
	 *
	 * @return The provider.
	 */
	public abstract String getProvider();

	
	/**
	 * Initialize this key generator with a source of randomness. The
	 * implementation-specific default parameters (such as key size) will be used.
	 *
	 * @param random
	 *            The source of randomness.
	 */
	@SuppressWarnings("ConstantConditions")
	public void init(AbstractSecureRandom random) {
		
		if (encryptionType==null)
			init(signatureType.getDefaultKeySizeBits(), random);
		else
			init(encryptionType.getDefaultKeySizeBits(), random);
	}


	public void init(short keySize) throws IOException {
		try {
			init(keySize, SecureRandomType.FORTUNA_WITH_BC_FIPS_APPROVED_FOR_KEYS.getSingleton(SecureRandomType.nonce));
		} catch (NoSuchAlgorithmException | NoSuchProviderException e) {
			throw new IOException(e);
		}
	}

	/**
	 * Initialize this key generator with a key size (in bits) and a source of
	 * randomness.
	 *
	 * @param keySize
	 *            The target key size, in bits.
	 * @param random
	 *            The source of randomness.
	 */
	public abstract void init(short keySize, AbstractSecureRandom random);

}
