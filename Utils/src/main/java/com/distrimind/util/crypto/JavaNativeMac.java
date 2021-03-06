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

import com.distrimind.util.io.Integrity;
import com.distrimind.util.io.MessageExternalizationException;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import javax.crypto.Mac;
import javax.crypto.ShortBufferException;
import javax.crypto.spec.SecretKeySpec;


/**
 * 
 * @author Jason Mahdjoub
 * @version 2.0
 * @since Utils 2.10.0
 */
public final class JavaNativeMac extends AbstractMac {
	private final Mac mac;

	JavaNativeMac(Mac mac) {
		if (mac == null)
			throw new NullPointerException();
		this.mac = mac;
	}

	@Override
	public int hashCode() {
		return mac.hashCode();
	}

	@Override
	public final String getAlgorithm() {
		return mac.getAlgorithm();
	}

	@Override
	public boolean equals(Object _obj) {
		if (_obj instanceof JavaNativeMac)
			return mac.equals(((JavaNativeMac) _obj).mac);
		else if (_obj instanceof Mac)
			return mac.equals(_obj);
		else
			return false;
	}

	@Override
	public String toString() {
		return mac.toString();
	}

	@Override
	public final int getMacLengthBytes() {
		return mac.getMacLength();
	}

	@Override
	public final void init(AbstractKey _key) throws IOException {
		try {
			mac.init(new SecretKeySpec(_key.toJavaNativeKey().getEncoded(), mac.getAlgorithm()));
		} catch (InvalidKeyException | NoSuchAlgorithmException | InvalidKeySpecException e) {
			throw new IOException(e);
		}
	}

	@Override
	public final void update(byte _input) throws IOException {
		try {
			mac.update(_input);
		}
		catch (IllegalStateException e)
		{
			throw new MessageExternalizationException(Integrity.FAIL_AND_CANDIDATE_TO_BAN, e);
		}
	}

	@Override
	public final void update(byte[] _input) throws IOException {
		try {
			mac.update(_input);
		}
		catch (IllegalStateException e)
		{
			throw new MessageExternalizationException(Integrity.FAIL_AND_CANDIDATE_TO_BAN, e);
		}
	}

	@Override
	public final void update(byte[] _input, int _offset, int _len) throws IOException {
		try {
			mac.update(_input, _offset, _len);
		}
		catch (IllegalStateException e)
		{
			throw new MessageExternalizationException(Integrity.FAIL_AND_CANDIDATE_TO_BAN, e);
		}
	}

	@Override
	public final void update(ByteBuffer _input) {
		mac.update(_input);
	}

	@Override
	public final byte[] doFinal() throws IOException {
		try {
			return mac.doFinal();
		}
		catch (IllegalStateException e)
		{
			throw new MessageExternalizationException(Integrity.FAIL_AND_CANDIDATE_TO_BAN, e);
		}

	}

	@Override
	public final void doFinal(byte[] _output, int _outOffset) throws IOException {
		try {
			mac.doFinal(_output, _outOffset);
		}
		catch (IllegalStateException e)
		{
			throw new MessageExternalizationException(Integrity.FAIL_AND_CANDIDATE_TO_BAN, e);
		} catch (ShortBufferException e) {
			throw new MessageExternalizationException(Integrity.FAIL, e);
		}
	}

	@Override
	public final byte[] doFinal(byte[] _input) throws IOException {
		try {
			return mac.doFinal(_input);
		}
		catch (IllegalStateException e)
		{
			throw new MessageExternalizationException(Integrity.FAIL_AND_CANDIDATE_TO_BAN, e);
		}
	}

	@Override
	public final void reset() {
		mac.reset();
	}

	@Override
	public final JavaNativeMac clone() throws CloneNotSupportedException {
		return new JavaNativeMac((Mac) mac.clone());
	}

}
