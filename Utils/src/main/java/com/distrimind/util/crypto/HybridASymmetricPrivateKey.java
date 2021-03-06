package com.distrimind.util.crypto;
/*
Copyright or © or Copr. Jason Mahdjoub (01/04/2013)

jason.mahdjoub@distri-mind.fr

This software (Object Oriented Database (OOD)) is a computer program 
whose purpose is to manage a local database with the object paradigm 
and the java language

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


import com.distrimind.util.data_buffers.WrappedSecretData;
import com.distrimind.util.data_buffers.WrappedSecretString;

import java.util.Objects;

/**
 * @author Jason Mahdjoub
 * @version 2.0
 * @since Utils 4.5.0
 */
public class HybridASymmetricPrivateKey extends AbstractKey implements IHybridKey, IASymmetricPrivateKey{

	public static final int MAX_SIZE_IN_BYTES_OF_HYBRID_PRIVATE_KEY_WITH_RSA_FOR_SIGNATURE=ASymmetricAuthenticatedSignatureType.MAX_SIZE_IN_BYTES_OF_HYBRID_PRIVATE_KEY_WITH_RSA_FOR_SIGNATURE;
	public static final int MAX_SIZE_IN_BYTES_OF_HYBRID_PRIVATE_KEY_WITHOUT_RSA_FOR_SIGNATURE=ASymmetricAuthenticatedSignatureType.MAX_SIZE_IN_BYTES_OF_HYBRID_PRIVATE_KEY_WITHOUT_RSA_FOR_SIGNATURE;
	public static final int MAX_SIZE_IN_BYTES_OF_HYBRID_PRIVATE_KEY_FOR_SIGNATURE=ASymmetricAuthenticatedSignatureType.MAX_SIZE_IN_BYTES_OF_HYBRID_PRIVATE_KEY_FOR_SIGNATURE;

	public static final int MAX_SIZE_IN_BYTES_OF_HYBRID_PRIVATE_KEY_WITH_RSA_FOR_ENCRYPTION=ASymmetricEncryptionType.MAX_SIZE_IN_BYTES_OF_HYBRID_PRIVATE_KEY_WITH_RSA_FOR_ENCRYPTION;
	public static final int MAX_SIZE_IN_BYTES_OF_HYBRID_PRIVATE_KEY_FOR_ENCRYPTION=ASymmetricEncryptionType.MAX_SIZE_IN_BYTES_OF_HYBRID_PRIVATE_KEY_FOR_ENCRYPTION;

	public static final int MAX_SIZE_IN_BYTES_OF_HYBRID_PRIVATE_KEY=MAX_SIZE_IN_BYTES_OF_HYBRID_PRIVATE_KEY_FOR_ENCRYPTION;
	private ASymmetricPrivateKey nonPQCPrivateKey, PQCPrivateKey;

	public HybridASymmetricPrivateKey(ASymmetricPrivateKey nonPQCPrivateKey, ASymmetricPrivateKey PQCPrivateKey) {
		if (nonPQCPrivateKey==null)
			throw new NullPointerException();
		if (PQCPrivateKey==null)
			throw new NullPointerException();
		if ((nonPQCPrivateKey.getEncryptionAlgorithmType()==null)!=(PQCPrivateKey.getEncryptionAlgorithmType()==null)
			|| (nonPQCPrivateKey.getAuthenticatedSignatureAlgorithmType()==null)!=(PQCPrivateKey.getAuthenticatedSignatureAlgorithmType()==null))
			throw new IllegalArgumentException("The given keys must be used both for encryption or both for signature");
		if ((nonPQCPrivateKey.getAuthenticatedSignatureAlgorithmType()!=null
				&& nonPQCPrivateKey.getAuthenticatedSignatureAlgorithmType().isPostQuantumAlgorithm())
			|| (nonPQCPrivateKey.getEncryptionAlgorithmType()!=null && nonPQCPrivateKey.getEncryptionAlgorithmType().isPostQuantumAlgorithm()))
			throw new IllegalArgumentException("nonPQCPrivateKey cannot be a post quantum algorithm");
		if ((PQCPrivateKey.getAuthenticatedSignatureAlgorithmType()!=null
				&& !PQCPrivateKey.getAuthenticatedSignatureAlgorithmType().isPostQuantumAlgorithm())
				|| (PQCPrivateKey.getEncryptionAlgorithmType()!=null && !PQCPrivateKey.getEncryptionAlgorithmType().isPostQuantumAlgorithm()))
			throw new IllegalArgumentException("PQCPrivateKey must be a post quantum algorithm");
		this.nonPQCPrivateKey = nonPQCPrivateKey;
		this.PQCPrivateKey = PQCPrivateKey;
	}

	@Override
	public boolean useEncryptionAlgorithm() {
		return getNonPQCPrivateKey().getEncryptionAlgorithmType()!=null;
	}

	@Override
	public boolean useAuthenticatedSignatureAlgorithm() {
		return getNonPQCPrivateKey().getAuthenticatedSignatureAlgorithmType()!=null;
	}

	@Override
	public ASymmetricPrivateKey getNonPQCPrivateKey() {
		return nonPQCPrivateKey;
	}

	public ASymmetricPrivateKey getPQCPrivateKey() {
		return PQCPrivateKey;
	}

	@Override
	public Object toGnuKey(){
		throw new IllegalAccessError();
	}

	@Override
	public java.security.Key toJavaNativeKey() {
		throw new IllegalAccessError();
	}

	@Override
	public com.distrimind.bcfips.crypto.Key toBouncyCastleKey() {
		throw new IllegalAccessError();
	}

	public WrappedSecretData encode() {
		return (WrappedSecretData)AbstractKey.encodeHybridKey(nonPQCPrivateKey, PQCPrivateKey, true);
	}

	@Override
	public WrappedSecretString encodeString() {
		return new WrappedSecretString(encode());
	}


	@Override
	public void zeroize() {
		nonPQCPrivateKey=null;
		PQCPrivateKey=null;
	}


	@Override
	public WrappedSecretData getKeyBytes() {
		return encode();
	}

	@Override
	public boolean isPostQuantumKey() {
		return true;
	}


	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		HybridASymmetricPrivateKey that = (HybridASymmetricPrivateKey) o;
		return nonPQCPrivateKey.equals(that.nonPQCPrivateKey) &&
				PQCPrivateKey.equals(that.PQCPrivateKey);
	}

	@Override
	public int hashCode() {
		return Objects.hash(nonPQCPrivateKey, PQCPrivateKey);
	}

}
