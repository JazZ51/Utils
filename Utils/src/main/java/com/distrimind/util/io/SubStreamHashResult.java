package com.distrimind.util.io;
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
import com.distrimind.util.crypto.MessageDigestType;

import java.io.IOException;
import java.util.Arrays;

/**
 * @author Jason Mahdjoub
 * @version 1.0
 * @since Utils 4.16.0
 */
public class SubStreamHashResult implements SecureExternalizable {
	private static final int MAX_HASH_SIZE= MessageDigestType.getMaxDigestLengthInBytes();
	private static final int MAX_IV_LENGTH=64;
	private static final int MAX_IV_NUMBERS=Short.MAX_VALUE;
	private byte[] hash;
	private byte[][] ivs;

	public SubStreamHashResult(byte[] hash, byte[][] ivs) {
		if (hash==null)
			throw new NullPointerException();
		if (hash.length>MAX_HASH_SIZE)
			throw new IllegalArgumentException();
		if (ivs!=null) {
			if (ivs.length>MAX_IV_NUMBERS)
				throw new IllegalArgumentException();
			for (byte[] iv : ivs)
				if (iv != null && iv.length > MAX_IV_LENGTH)
					throw new IllegalArgumentException();
		}
		this.hash = hash;
		this.ivs = ivs;
	}

	public byte[] getHash() {
		return hash;
	}

	public byte[][] getIvs() {
		return ivs;
	}

	@Override
	public int getInternalSerializedSize() {
		return SerializationTools.getInternalSize(hash, MAX_HASH_SIZE)+SerializationTools.getInternalSize(ivs, MAX_IV_NUMBERS);
	}

	@Override
	public void writeExternal(SecuredObjectOutputStream out) throws IOException {
		out.writeBytesArray(hash, false, MAX_HASH_SIZE);
		out.write2DBytesArray(ivs, true, false, MAX_IV_NUMBERS, MAX_IV_LENGTH);
	}

	@Override
	public void readExternal(SecuredObjectInputStream in) throws IOException {
		hash=in.readBytesArray(false, MAX_HASH_SIZE);
		ivs=in.read2DBytesArray(true, false, MAX_IV_NUMBERS, MAX_IV_LENGTH);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		SubStreamHashResult that = (SubStreamHashResult) o;
		return Arrays.equals(hash, that.hash);
	}

	@Override
	public int hashCode() {
		return Arrays.hashCode(hash);
	}
}
