/*
 * MadKitLanEdition (created by Jason MAHDJOUB (jason.mahdjoub@distri-mind.fr)) Copyright (c)
 * 2015 is a fork of MadKit and MadKitGroupExtension. 
 * 
 * Copyright or © or Copr. Jason Mahdjoub, Fabien Michel, Olivier Gutknecht, Jacques Ferber (1997)
 * 
 * jason.mahdjoub@distri-mind.fr
 * fmichel@lirmm.fr
 * olg@no-distance.net
 * ferber@lirmm.fr
 * 
 * This software is a computer program whose purpose is to
 * provide a lightweight Java library for designing and simulating Multi-Agent Systems (MAS).
 * This software is governed by the CeCILL-C license under French law and
 * abiding by the rules of distribution of free software.  You can  use,
 * modify and/ or redistribute the software under the terms of the CeCILL-C
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 * As a counterpart to the access to the source code and  rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty  and the software's author,  the holder of the
 * economic rights,  and the successive licensors  have only  limited
 * liability.
 * 
 * In this respect, the user's attention is drawn to the risks associated
 * with loading,  using,  modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean  that it is complicated to manipulate,  and  that  also
 * therefore means  that it is reserved for developers  and  experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or
 * data to be ensured and,  more generally, to use and operate it in the
 * same conditions as regards security.
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-C license and that you accept its terms.
 */
package com.distrimind.util.io;

import java.io.*;

/**
 * 
 * @author Jason Mahdjoub
 * @version 1.0
 * @since Utils 3.27.0
 */
public abstract class RandomInputStream extends InputStream implements AutoCloseable, DataInput {
	private long mark = -1;
	private int readLimit = -1;

	/*
	 * public int read(byte[] _bytes) throws IOException; public int read(byte[]
	 * _bytes, int offset, int length) throws InputStreamException;
	 */
	/**
	 * Returns the length of this stream source.
	 *
	 * @return the length of this stream, measured in bytes.
	 * @exception IOException
	 *                if an I/O error occurs.
	 */
	public abstract long length() throws IOException;

	/**
	 * Sets the stream source -pointer offset, measured from the beginning of this
	 * stream, at which the next read or write occurs. The offset may be set beyond
	 * the end of the stream source. Setting the offset or beyond over the end of
	 * the file does not change the file length.
	 *
	 * @param _pos
	 *            the offset position, measured in bytes from the beginning of the
	 *            stream, at which to set the stream source pointer.
	 * @exception IOException
	 *                if <code>pos</code> is less than <code>0</code> or if an I/O
	 *                error occurs.
	 */
	public abstract void seek(long _pos) throws IOException;

	// public void skipBytes(int _nb) throws InputStreamException;
	/**
	 * Returns the current position in this stream.
	 *
	 * @return the offset from the beginning of the stream, in bytes, at which the
	 *         next read or write occurs.
	 * @exception IOException
	 *                if an I/O error occurs.
	 */
	public abstract long currentPosition() throws IOException;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized void mark(int readlimit) {
		if (readlimit > 0) {
			try {
				mark = currentPosition();
				this.readLimit = readlimit;
			} catch (Exception e) {
				mark = -1;
				this.readLimit = -1;
			}
		} else {
			mark = -1;
			this.readLimit = -1;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean markSupported() {
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized void reset() throws IOException {
		if (mark == -1 || mark >= length())
			throw new IOException("Invalid mark : " + mark);
		if (currentPosition() > mark + readLimit)
			throw new IOException("Invalid mark (readLimit reached) : " + mark);
		seek(mark);
	}

	public abstract boolean isClosed();

	@Override
	public final void readFully(byte[] tab) throws IOException {
		readFully(tab, 0, tab==null?0:tab.length);
	}

	@Override
	public abstract void readFully(byte[] tab, int off, int len) throws IOException;


	@Override
	public final boolean readBoolean() throws IOException {
		int ch = read();
		if (ch < 0)
			throw new EOFException();
		return (ch != 0);
	}

	@Override
	public final byte readByte() throws IOException {
		int ch = read();
		if (ch < 0)
			throw new EOFException();
		return (byte)(ch);
	}

	@Override
	public final int readUnsignedByte() throws IOException {
		int ch = read();
		if (ch < 0)
			throw new EOFException();
		return ch;
	}


	@Override
	public final short readShort() throws IOException {
		int ch1 = read();
		int ch2 = read();
		if ((ch1 | ch2) < 0)
			throw new EOFException();
		return (short)((ch1 << 8) + (ch2));
	}

	@Override
	public final int readUnsignedShort() throws IOException {
		int ch1 = read();
		int ch2 = read();
		if ((ch1 | ch2) < 0)
			throw new EOFException();
		return (ch1 << 8) + (ch2);
	}

	public final int readUnsignedShortInt() throws IOException {
		int ch1 = read();
		int ch2 = read();
		int ch3 = read();
		if ((ch1 | ch2 | ch3) < 0)
			throw new EOFException();
		return (ch1 << 16) + (ch2 << 8) + (ch3);
	}

	@Override
	public final char readChar() throws IOException {
		int ch1 = read();
		int ch2 = read();
		if ((ch1 | ch2) < 0)
			throw new EOFException();
		return (char)((ch1 << 8) + (ch2));
	}

	@Override
	public final int readInt() throws IOException {
		int ch1 = read();
		int ch2 = read();
		int ch3 = read();
		int ch4 = read();
		if ((ch1 | ch2 | ch3 | ch4) < 0)
			throw new EOFException();
		return ((ch1 << 24) + (ch2 << 16) + (ch3 << 8) + (ch4));
	}

	private byte[] readBuffer = new byte[8];


	@Override
	public final long readLong() throws IOException {
		readFully(readBuffer, 0, 8);
		return (((long)readBuffer[0] << 56) +
				((long)(readBuffer[1] & 255) << 48) +
				((long)(readBuffer[2] & 255) << 40) +
				((long)(readBuffer[3] & 255) << 32) +
				((long)(readBuffer[4] & 255) << 24) +
				((readBuffer[5] & 255) << 16) +
				((readBuffer[6] & 255) <<  8) +
				((readBuffer[7] & 255)));
	}

	@Override
	public final float readFloat() throws IOException {
		return Float.intBitsToFloat(readInt());
	}

	@Override
	public final double readDouble() throws IOException {
		return Double.longBitsToDouble(readLong());
	}

	@Override
	public final String readUTF() throws IOException {
		return DataInputStream.readUTF(this);
	}




}