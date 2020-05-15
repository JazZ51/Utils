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

import com.distrimind.util.FileTools;
import com.distrimind.util.io.*;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;

/**
 * 
 * @author Jason Mahdjoub
 * @version 5.0
 * @since Utils 1.5
 */
public abstract class AbstractEncryptionOutputAlgorithm {
	final static int BUFFER_SIZE = FileTools.BUFFER_SIZE;

	protected final AbstractCipher cipher;

	final byte[] nullIV;

	protected final byte[] buffer;
	protected byte[] bufferOut;
	private int maxPlainTextSizeForEncoding;
	private long maxEncryptedPartLength;
	private final byte[] one=new byte[1];
	
	public byte getBlockModeCounterBytes() {
		return (byte)0;
	}
	
	public boolean useExternalCounter()
	{
		return false;
	}

	protected AbstractEncryptionOutputAlgorithm()
	{
		super();
		cipher=null;
		nullIV=null;
		buffer=null;
		bufferOut=null;
	}

	protected AbstractEncryptionOutputAlgorithm(AbstractCipher cipher, int ivSizeBytes) throws IOException {
		if (cipher == null)
			throw new NullPointerException("cipher");
		this.cipher = cipher;
		if (includeIV()) {
			nullIV = new byte[ivSizeBytes];
			Arrays.fill(nullIV, (byte) 0);
		}
		else
			nullIV = null;

		buffer=new byte[BUFFER_SIZE];
		setMaxPlainTextSizeForEncoding(getMaxPlainTextSizeForEncoding());
	}

	protected void initBufferAllocatorArgs()
	{
		bufferOut=new byte[(int)getOutputSizeForEncryption(BUFFER_SIZE)];
	}
	

	
	public byte[] encode(byte[] bytes) throws IOException{
		return encode(bytes, 0, bytes.length);
	}
	public byte[] encode(byte[] bytes, byte[] associatedData) throws IOException{
		return encode(bytes, 0, bytes.length, associatedData, 0, associatedData==null?0:associatedData.length, (byte[])null);
	}
	public byte[] encode(byte[] bytes, byte[] associatedData, byte[] externalCounter) throws IOException{
		return encode(bytes, 0, bytes.length, associatedData, 0, associatedData==null?0:associatedData.length, externalCounter);
	}

	public byte[] encode(byte[] bytes, int off, int len) throws IOException{
		return encode(bytes, off, len, null, 0, 0);
	}
	
	public byte[] encode(byte[] bytes, int off, int len, byte[] associatedData, int offAD, int lenAD) throws IOException{
		return encode(bytes, off, len, associatedData, offAD, lenAD, (byte[])null);
	}
	public byte[] encode(byte[] bytes, int off, int len, byte[] associatedData, int offAD, int lenAD, byte[] externalCounter) throws IOException{
		try (RandomByteArrayOutputStream baos = new RandomByteArrayOutputStream((int)getOutputSizeForEncryption(len))) {
			encode(bytes, off, len, associatedData, offAD, lenAD, baos, externalCounter);
			return baos.getBytes();
		}
	}
	public void encode(byte[] bytes, int off, int len, RandomOutputStream os) throws IOException{
		encode(bytes, off, len, null,0, 0, os);
	}
	public void encode(byte[] bytes, int off, int len, byte[] associatedData, int offAD, int lenAD, RandomOutputStream os) throws IOException
	{
		encode(bytes, off, len, associatedData, offAD, lenAD, os, null);
	}

	protected abstract void initCipherForEncryptionWithIvAndCounter(AbstractCipher cipher, byte[] iv, int counter);

	public void encode(byte[] bytes, int off, int len, byte[] associatedData, int offAD, int lenAD, RandomOutputStream os, byte[] externalCounter) throws IOException{
		RandomInputStream ris=new RandomByteArrayInputStream(bytes);
		if (len!=bytes.length)
			ris=new LimitedRandomInputStream(ris, off, len);
		encode(ris, associatedData, offAD, lenAD, os, externalCounter);
	}
	public void encode(RandomInputStream is, RandomOutputStream os) throws IOException
	{
		encode(is, null, 0, 0, os);
	}
	public void encode(RandomInputStream is, byte[] associatedData, RandomOutputStream os) throws IOException
	{
		encode(is, associatedData, 0, associatedData==null?0:associatedData.length, os);
	}
	public void encode(RandomInputStream is, byte[] associatedData, int offAD, int lenAD, RandomOutputStream os) throws IOException{
		encode(is, associatedData, offAD, lenAD, os, null);
	}

	public void encode(RandomInputStream is, byte[] associatedData, int offAD, int lenAD, RandomOutputStream os, byte[] externalCounter) throws IOException {

		try(RandomOutputStream cos = getCipherOutputStream(os, associatedData, offAD, lenAD, externalCounter))
		{
			is.transferTo(cos);
		}
	}

	protected abstract AbstractCipher getCipherInstance() throws NoSuchAlgorithmException,
			NoSuchPaddingException, NoSuchProviderException;
	protected static void checkLimits(byte[] b, int off, int len)
	{
		if (b==null)
			throw new NullPointerException();
		if ((off | len) < 0 || len > b.length - off)
			throw new IndexOutOfBoundsException();
	}
	public RandomOutputStream getCipherOutputStream(final RandomOutputStream os) throws IOException
	{
		return getCipherOutputStream(os, null, 0,0, null);
	}
	public RandomOutputStream getCipherOutputStream(final RandomOutputStream os, final byte[] associatedData, final int offAD, final int lenAD) throws IOException
	{
		return getCipherOutputStream(os, associatedData, offAD, lenAD, null);
	}
	public RandomOutputStream getCipherOutputStream(final RandomOutputStream os, byte[] externalCounter) throws IOException
	{
		return getCipherOutputStream(os, null, 0,0, externalCounter);
	}

	protected abstract int getCounterStepInBytes();

	public RandomOutputStream getCipherOutputStream(final RandomOutputStream os, final byte[] associatedData, final int offAD, final int lenAD, final byte[] externalCounter) throws
			IOException{

		final long initialOutPos=os.currentPosition();



		return new RandomOutputStream() {
			long length=0;
			long currentPos=0;
			boolean closed=false;

			private long checkInit() throws IOException {
				if (currentPos % maxPlainTextSizeForEncoding == 0) {
					long round=currentPos/ maxPlainTextSizeForEncoding;
					if (round>0){
						try {
							byte[] f=cipher.doFinal();
							if (f!=null && f.length>0)
								os.write(f);

						} catch (IllegalBlockSizeException | BadPaddingException e) {
							throw new IOException(e);
						}
					}
					if (includeIV()) {
						try {
							byte[] iv=initCipherForEncrypt(cipher, externalCounter);
							os.write(iv, 0, getIVSizeBytesWithoutExternalCounter());
						} catch (InvalidKeyException | InvalidAlgorithmParameterException | NoSuchAlgorithmException | InvalidKeySpecException | NoSuchProviderException e) {
							throw new IOException(e);
						}
					}
					else {
						try {
							initCipherForEncryptWithNullIV(cipher);
						} catch (InvalidKeyException | InvalidAlgorithmParameterException | NoSuchAlgorithmException | InvalidKeySpecException | NoSuchProviderException e) {
							throw new IOException(e);
						}
					}

					if (associatedData != null && lenAD > 0)
						cipher.updateAAD(associatedData, offAD, lenAD);
					return maxPlainTextSizeForEncoding;
				}
				return (int) (currentPos % maxPlainTextSizeForEncoding);
			}
			@Override
			public long length() {
				return length;
			}

			@Override
			public void write(byte[] b, int off, int len) throws IOException {
				if (closed)
					throw new IOException("Stream closed");
				checkLimits(b, off, len);
				if (len==0)
					return;
				long l=checkInit();
				while (len>0) {
					int s=(int)Math.min(len, l);
					os.write(cipher.update(b, off, s));
					len-=s;
					currentPos+=s;
					if (len>0) {
						off+=s;
						checkInit();
					}
				}
				length=Math.max(length, currentPos);
			}
			@Override
			public void write(int b) throws IOException {
				if (closed)
					throw new IOException("Stream closed");
				checkInit();
				one[0]=(byte)b;
				os.write(cipher.update(one));
			}

			@Override
			public void setLength(long newLength) throws IOException {
				if (closed)
					throw new IOException("Stream closed");
				if (newLength<0)
					throw new IllegalArgumentException();
				if (newLength==0) {
					os.setLength(initialOutPos);
					currentPos=0;
					length=0;
				}
				else {
					long round=newLength/ maxPlainTextSizeForEncoding;
					newLength=initialOutPos + round * maxEncryptedPartLength+(newLength % maxPlainTextSizeForEncoding);
					os.setLength(newLength);
					length=newLength;
					seek(Math.min(newLength, currentPos));
				}
			}

			@Override
			public void seek(long _pos) throws IOException {
				if (closed)
					throw new IOException("Stream closed");
				if (_pos<0 || _pos>length)
					throw new IllegalArgumentException();
				long round = _pos / maxPlainTextSizeForEncoding;
				if (includeIV()) {
					long p = initialOutPos + round * maxEncryptedPartLength;
					RandomInputStream ris = os.getRandomInputStream();
					os.getRandomInputStream().seek(p);
					byte[] iv = new byte[getIVSizeBytesWithExternalCounter()];
					ris.readFully(iv);
					long mod=_pos % maxPlainTextSizeForEncoding;
					int counter=(int)(mod/getCounterStepInBytes());
					if (mod>0) {
						mod = cipher.getOutputSize((int)mod)+getIVSizeBytesWithoutExternalCounter();
					}
					p += mod;
					os.seek(p);
					System.arraycopy(externalCounter, 0, iv, getIVSizeBytesWithoutExternalCounter(), externalCounter.length);
					initCipherForEncryptionWithIvAndCounter(cipher, iv, counter);
				}
				else
				{
					long add=cipher.getOutputSize((int)(_pos % maxPlainTextSizeForEncoding));
					if (add>0)
						add+=getIVSizeBytesWithoutExternalCounter();
					os.seek(initialOutPos + round * maxEncryptedPartLength+add);
					try {
						initCipherForEncryptWithNullIV(cipher);
					} catch (InvalidKeyException | InvalidAlgorithmParameterException | NoSuchAlgorithmException | InvalidKeySpecException | NoSuchProviderException e) {
						throw new IOException(e);
					}
				}
			}

			@Override
			public long currentPosition() {
				return currentPos;
			}

			@Override
			public boolean isClosed() {
				return closed;
			}

			@Override
			protected RandomInputStream getRandomInputStreamImpl() throws IOException {
				throw new IOException(new IllegalAccessException());
			}

			@Override
			public void flush() throws IOException {
				os.flush();
			}

			@Override
			public void close() throws IOException {
				if (closed)
					return;
				if (length>0) {

					try {
						byte[] f=cipher.doFinal();
						if (f!=null && f.length>0)
							os.write(f);
					} catch (IllegalBlockSizeException | BadPaddingException e) {
						throw new IOException(e);
					}
				}
				flush();
				closed=true;
			}


		};

	}

	public abstract int getMaxPlainTextSizeForEncoding();

	void setMaxPlainTextSizeForEncoding(int maxPlainTextSizeForEncoding) throws IOException {
		try {
			initCipherForEncryptWithNullIV(cipher);
			this.maxPlainTextSizeForEncoding=maxPlainTextSizeForEncoding;
			int maxCipherTextLength = cipher.getOutputSize(maxPlainTextSizeForEncoding);
			this.maxEncryptedPartLength =((long) maxCipherTextLength)+((long)getIVSizeBytesWithoutExternalCounter());
		} catch (InvalidKeyException | InvalidAlgorithmParameterException | NoSuchAlgorithmException | InvalidKeySpecException | NoSuchProviderException e) {
			throw new IOException(e);
		}

	}

	
	public abstract int getIVSizeBytesWithExternalCounter();
	public final int getIVSizeBytesWithoutExternalCounter()
	{
		return getIVSizeBytesWithExternalCounter()-(useExternalCounter()?getBlockModeCounterBytes():0);
	}

	public long getOutputSizeForEncryption(long inputLen)
	{
		if (inputLen<0)
			throw new IllegalArgumentException();
		if (inputLen==0)
			return 0;
		long add=cipher.getOutputSize((int)(inputLen % maxPlainTextSizeForEncoding));
		if (add>0)
			add+=getIVSizeBytesWithoutExternalCounter();
		return inputLen / maxPlainTextSizeForEncoding * maxEncryptedPartLength+add;
	}


	protected abstract boolean includeIV();
	public void initCipherForEncrypt(AbstractCipher cipher) throws InvalidKeyException,
	InvalidAlgorithmParameterException, NoSuchAlgorithmException, InvalidKeySpecException, NoSuchProviderException {
		initCipherForEncrypt(cipher, null);
	}
	public abstract byte[] initCipherForEncrypt(AbstractCipher cipher, byte[] externalCounter)
			throws InvalidKeyException, InvalidAlgorithmParameterException,
			NoSuchAlgorithmException, InvalidKeySpecException, NoSuchProviderException;

	public abstract void initCipherForEncryptWithNullIV(AbstractCipher cipher)
			throws InvalidKeyException, InvalidAlgorithmParameterException,
			NoSuchAlgorithmException, InvalidKeySpecException, NoSuchProviderException;

	public abstract boolean isPostQuantumEncryption();

}
