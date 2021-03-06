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

import com.distrimind.util.Bits;
import com.distrimind.util.sizeof.ObjectSizer;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.Arrays;


/**
 * 
 * @author Jason Mahdjoub
 * @version 3.0
 * @since Utils 1.8
 *
 */
public class PasswordHash {
	final static byte DEFAULT_SALT_SIZE = 24;

	final static byte DEFAULT_COST = 16;

	public static byte[] generateSalt(SecureRandom random, int saltSize) {
		byte[] res = new byte[saltSize];
		random.nextBytes(res);
		return res;
	}

	private final PasswordHashType type;

	private final SecureRandom random;

	private byte saltSize;

	private byte cost;
	

	public PasswordHash() {
		this(PasswordHashType.DEFAULT);
	}

	public PasswordHash(PasswordHashType type) {
		this(type, new SecureRandom());
	}
	

	public PasswordHash(PasswordHashType type, SecureRandom random) {
		this(type, random, DEFAULT_COST, DEFAULT_SALT_SIZE);
	}
	public PasswordHash(PasswordHashType type, SecureRandom random, byte cost) {
		this(type, random, cost, DEFAULT_SALT_SIZE);
	}
	public PasswordHash(PasswordHashType type, SecureRandom random, byte cost, byte saltSize) {
		if (cost<4 || cost>31)
			throw new IllegalArgumentException("cost must be greater or equals than 4 and lower or equals than 31");

		this.type = type;
		this.random = random;
		this.cost = cost;
		this.saltSize = saltSize;
	}


	public static boolean checkValidHashedPassword(WrappedPassword password, WrappedHashedPassword goodHash) {
		return checkValidHashedPassword(password, goodHash, null);
	}

	public static boolean checkValidHashedPassword(WrappedPassword password, WrappedHashedPassword goodHash, byte[] staticAdditionalSalt) {
		PasswordHashType type=PasswordHashType.valueOf(goodHash);
		byte cost=PasswordHashType.getCost(goodHash);
		try {
			
			
			//byte []composedHash=getHashFromIdentifiedHash(goodHash);
			byte[][] separated = Bits.separateEncodingsWithShortSizedTabs(goodHash.getBytes(), 2, goodHash.getBytes().length-2);
			byte[] generatedSalt = separated[1];
			byte[] salt = mixSaltWithStaticSalt(generatedSalt, staticAdditionalSalt);
			byte[] hash = separated[0];

			assert type != null;
			char[] charArray=password.getChars();
			boolean res=Arrays.equals(type.hash(charArray, salt, cost, (byte)hash.length), hash);
			Arrays.fill(generatedSalt, (byte)0);
			Arrays.fill(salt, (byte)0);
			Arrays.fill(hash, (byte)0);
			Arrays.fill(charArray, '0');
			return res;
		} catch (Exception e) {
			return false;
		}
	}


	public byte getCost() {
		return cost;
	}

	public byte getSaltSizeBytes() {
		return saltSize;
	}

	public WrappedHashedPassword hash(WrappedPassword password)
			throws IOException {
		return hash(password, null, type.getDefaultHashLengthBytes());
	}
	public WrappedHashedPassword hash(WrappedPassword password, byte defaultHashLengthBytes)
			throws IOException {
		return hash(password, null, defaultHashLengthBytes);
	}
	public WrappedHashedPassword hash(WrappedPassword password, byte[] staticAdditionalSalt) throws IOException
	{
		return hash(password, staticAdditionalSalt, type.getDefaultHashLengthBytes());
	}
	public WrappedHashedPassword hash(WrappedPassword password, byte[] staticAdditionalSalt, byte hashLengthBytes)
			throws IOException {
		if (password == null)
			throw new NullPointerException("password");

		byte[] generatedSalt = generateSalt(random, saltSize);
		byte[] salt = mixSaltWithStaticSalt(generatedSalt, staticAdditionalSalt);
		byte[] hash=type.hash(password.getChars(), salt, cost, hashLengthBytes);
		byte[] concatenated=Bits.concatenateEncodingWithShortSizedTabs(hash, generatedSalt);
		WrappedHashedPassword res=getIdentifiedHash(concatenated);
		Arrays.fill(generatedSalt, (byte)0);
		Arrays.fill(salt, (byte)0);
		Arrays.fill(hash, (byte)0);
		Arrays.fill(concatenated, (byte)0);
		return res;

		
	}
	
	private WrappedHashedPassword getIdentifiedHash(byte[] hash)
	{
		byte[] res = new byte[hash.length + 2];
		res[0]=type.getID();
		res[1]=cost;
		System.arraycopy(hash, 0, res, ObjectSizer.SHORT_FIELD_SIZE, hash.length);
		return new WrappedHashedPassword(res);
	}

	private static byte[] mixSaltWithStaticSalt(byte[] salt, byte[] staticAdditionalSalt) {
		if (staticAdditionalSalt != null) {
			byte[] res = new byte[salt.length + staticAdditionalSalt.length];
			System.arraycopy(salt, 0, res, 0, salt.length);
			System.arraycopy(staticAdditionalSalt, 0, res, salt.length, staticAdditionalSalt.length);
			return res;
		}
		return salt;
	}

	public void setCost(byte cost) {
		if (cost<4 || cost>31)
			throw new IllegalArgumentException("cost must be greater or equals than 4 and lower or equals than 31");

		this.cost = cost;
	}

	public void setSaltSize(byte _saltSize) {
		saltSize = _saltSize;
	}

}
