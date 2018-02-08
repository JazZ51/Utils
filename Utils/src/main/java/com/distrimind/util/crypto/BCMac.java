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
import java.nio.ByteBuffer;

import org.bouncycastle.crypto.UpdateOutputStream;
import org.bouncycastle.crypto.fips.FipsOutputMACCalculator;
import org.bouncycastle.crypto.fips.FipsSHS;
import org.bouncycastle.crypto.fips.FipsSHS.AuthParameters;

import gnu.vm.jgnu.security.InvalidKeyException;
import gnu.vm.jgnu.security.NoSuchAlgorithmException;
import gnu.vm.jgnu.security.spec.InvalidKeySpecException;

/**
 * 
 * @author Jason Mahdjoub
 * @version 1.0
 * @since Utils 3.10.0
 */
public final class BCMac extends AbstractMac {

	private final SymmetricAuthentifiedSignatureType type;
	private final int macLength;
	private org.bouncycastle.crypto.SymmetricSecretKey secretKey;
	private FipsOutputMACCalculator<AuthParameters> mac;
	private UpdateOutputStream macStream;
	
	BCMac(SymmetricAuthentifiedSignatureType type)
	{
		this.type=type;
		macLength=type.getSignatureSizeInBits();
	}
	
	@Override
	public int hashCode() {
		return mac.hashCode();
	}

	@Override
	public String getAlgorithm() {
		return type.getAlgorithmName();
	}

	@Override
	public boolean equals(Object _obj) {
		return mac.equals(_obj);
	}

	@Override
	public String toString() {
		return mac.toString();
	}

	@Override
	public int getMacLength() {
		
		return macLength;
	}

	@Override
	public void init(UtilKey _key) throws InvalidKeyException, NoSuchAlgorithmException, InvalidKeySpecException {
		init((org.bouncycastle.crypto.SymmetricSecretKey)_key.toBouncyCastleKey());
	}
	
	public void init(org.bouncycastle.crypto.SymmetricSecretKey _key) throws InvalidKeyException, NoSuchAlgorithmException, InvalidKeySpecException {
		FipsSHS.MACOperatorFactory fipsFacto=new FipsSHS.MACOperatorFactory();
		mac=fipsFacto.createOutputMACCalculator(secretKey=_key, type.getMessageDigestAuth());
		reset();
	}

	@Override
	public void update(byte _input) throws IllegalStateException {
		try
		{
			macStream.write(_input);
		}
		catch(IOException e)
		{
			throw new IllegalStateException(e);
		}
		
	}

	@Override
	public void update(byte[] _input) throws IllegalStateException {
		try
		{
			macStream.write(_input);
		}
		catch(IOException e)
		{
			throw new IllegalStateException(e);
		}
		
	}

	@Override
	public void update(byte[] _input, int _offset, int _len) throws IllegalStateException {
		try
		{
			macStream.write(_input, _offset, _len);
		}
		catch(IOException e)
		{
			throw new IllegalStateException(e);
		}
		
	}

	@Override
	public void update(ByteBuffer _input) {
		macStream.update(_input.array(), _input.position(), _input.remaining());
	}

	@Override
	public byte[] doFinal() throws IllegalStateException {
		try
		{
			macStream.close();
			byte res[]=mac.getMAC();
			reset();
			return res;
		}
		catch(IOException e)
		{
			throw new IllegalStateException(e);
		}
	}

	@Override
	public void doFinal(byte[] _output, int _outOffset) throws IllegalStateException {
		try
		{
			macStream.close();
			mac.getMAC(_output, _outOffset);
			reset();
		}
		catch(IOException e)
		{
			throw new IllegalStateException(e);
		}
	}

	@Override
	public byte[] doFinal(byte[] _input) throws IllegalStateException {
		update(_input);
		return doFinal();
	}

	@Override
	public void reset() {
		macStream=mac.getMACStream();
	}

	@Override
	public BCMac clone() throws CloneNotSupportedException {
		try {
			BCMac res=new BCMac(type);
			res.init(secretKey);
			return res;
		} catch (InvalidKeyException | NoSuchAlgorithmException | InvalidKeySpecException e) {
			throw new CloneNotSupportedException(e.getMessage());
		}
	}

}