package com.distrimind.util.crypto;
/*
Copyright or © or Copr. Jason Mahdjoub (01/04/2013)

jason.mahdjoub@distri-mind.fr

This software (Object Oriented Database (OOD)) is a computer program 
whose purpose is to manage a local database with the object paradigm 
and the java langage 

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

import org.bouncycastle.crypto.Algorithm;
import org.bouncycastle.pqc.jcajce.provider.sphincs.Sphincs256KeyPairGeneratorSpi;

import java.security.*;
import java.util.Objects;

import static com.distrimind.util.crypto.ASymmetricAuthenticatedSignatureType.BCPQC_SPHINCS256_SHA2_512_256;
import static com.distrimind.util.crypto.ASymmetricAuthenticatedSignatureType.BCPQC_SPHINCS256_SHA3_512;

/**
 * @author Jason Mahdjoub
 * @version 1.0
 * @since Utils 4.5.0
 */
public final class HybridASymmetricAuthenticatedSignatureType {
	private final ASymmetricAuthenticatedSignatureType nonPQCASymmetricAuthenticatedSignatureType, PQCASymmetricAuthenticatedSignatureType;

	public HybridASymmetricAuthenticatedSignatureType(ASymmetricAuthenticatedSignatureType nonPQCASymmetricAuthenticatedSignatureType, ASymmetricAuthenticatedSignatureType PQCASymmetricAuthenticatedSignatureType) {
		if (nonPQCASymmetricAuthenticatedSignatureType==null)
			throw new NullPointerException();
		if (PQCASymmetricAuthenticatedSignatureType==null)
			throw new NullPointerException();
		if (nonPQCASymmetricAuthenticatedSignatureType.isPostQuantumAlgorithm())
			throw new IllegalArgumentException();
		if (!PQCASymmetricAuthenticatedSignatureType.isPostQuantumAlgorithm())
			throw new IllegalArgumentException();
		this.nonPQCASymmetricAuthenticatedSignatureType = nonPQCASymmetricAuthenticatedSignatureType;
		this.PQCASymmetricAuthenticatedSignatureType = PQCASymmetricAuthenticatedSignatureType;
	}

	public ASymmetricAuthenticatedSignatureType getNonPQCASymmetricAuthenticatedSignatureType() {
		return nonPQCASymmetricAuthenticatedSignatureType;
	}

	public ASymmetricAuthenticatedSignatureType getPQCASymmetricAuthenticatedSignatureType() {
		return PQCASymmetricAuthenticatedSignatureType;
	}
	public HybridASymmetricKeyPair generateKeyPair(AbstractSecureRandom random) throws InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchProviderException {
		return generateKeyPair(random, -1);
	}
	public HybridASymmetricKeyPair generateKeyPair(AbstractSecureRandom random, int keySizeBits) throws InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchProviderException {
		return generateKeyPair(random, keySizeBits, Long.MIN_VALUE);
	}
	public HybridASymmetricKeyPair generateKeyPair(AbstractSecureRandom random, int keySizeBits,
												   long expirationTimeUTC) throws InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchProviderException {
		ASymmetricKeyPair kp=nonPQCASymmetricAuthenticatedSignatureType.getKeyPairGenerator(random, keySizeBits, expirationTimeUTC ).generateKeyPair();
		ASymmetricKeyPair pqcKP=PQCASymmetricAuthenticatedSignatureType.getKeyPairGenerator(random, keySizeBits, expirationTimeUTC ).generateKeyPair();
		return new HybridASymmetricKeyPair(kp, pqcKP);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		HybridASymmetricAuthenticatedSignatureType that = (HybridASymmetricAuthenticatedSignatureType) o;
		return nonPQCASymmetricAuthenticatedSignatureType == that.nonPQCASymmetricAuthenticatedSignatureType &&
				PQCASymmetricAuthenticatedSignatureType == that.PQCASymmetricAuthenticatedSignatureType;
	}

	@Override
	public int hashCode() {
		return Objects.hash(nonPQCASymmetricAuthenticatedSignatureType, PQCASymmetricAuthenticatedSignatureType);
	}

	@Override
	public String toString() {
		return "HybridASymmetricAuthenticatedSignatureType{" +
				"nonPQCASymmetricAuthenticatedSignatureType=" + nonPQCASymmetricAuthenticatedSignatureType +
				", PQCASymmetricAuthenticatedSignatureType=" + PQCASymmetricAuthenticatedSignatureType +
				'}';
	}
}