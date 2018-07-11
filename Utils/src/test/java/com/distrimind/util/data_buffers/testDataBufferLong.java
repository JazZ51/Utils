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

package com.distrimind.util.data_buffers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Random;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * 
 * @author Jason Mahdjoub
 * @version 1.0
 * @since Utils 3.4
 *
 */
public final class testDataBufferLong extends testDataBuffer {
	protected static int size = 50;
	protected static long tab[] = null;

	public static long[] getTab(int _size) {
		long res[] = new long[_size];
		Random r = new Random(System.currentTimeMillis());

		for (int i = _size - 1; i >= 0; i--) {
			res[i] = r.nextLong();
		}
		return res;
	}

	@BeforeClass
	public static void init() {
		tab = getTab(size);
	}

	@Override
	protected DataBuffer getNewDataBuffer(int _size) {
		return new DataBufferLong(_size);
	}

	@Override
	protected int getType() {
		return DataBuffer.TYPE_LONG;
	}

	@Override
	protected String getTypeString() {
		return "LONG";
	}

	@Override
	@Test
	public void testConstructors() {
		DataBufferLong d = new DataBufferLong(10);
		assertNotNull(d, "DataBufferBool allocation error");
		d = new DataBufferLong(tab);
		assertNotNull(d, "DataBufferBool allocation error");
	}

	@Override
	@Test
	public void testGetsSets() {
		DataBufferLong d = new DataBufferLong(tab.clone());
		for (int i = size - 1; i >= 0; i--) {
			assertEquals(tab[i], d.getLong(i));
		}

		d = new DataBufferLong(size);
		for (int i = size - 1; i >= 0; i--) {
			d.setLong(i, tab[i]);
		}
		for (int i = size - 1; i >= 0; i--) {
			assertEquals((byte) tab[i], d.getByte(i));
			assertEquals((char) tab[i], d.getChar(i));
			assertEquals((double) tab[i], d.getDouble(i), 0.0);
			assertEquals((float) tab[i], d.getFloat(i), 0.0);
			assertEquals((int) tab[i], d.getInt(i));
			assertEquals(tab[i], d.getLong(i));
			assertEquals((short) tab[i], d.getShort(i));
		}

		DataBufferLong dbool = new DataBufferLong(size);
		DataBufferLong db = new DataBufferLong(size);
		DataBufferLong dc = new DataBufferLong(size);
		DataBufferLong dd = new DataBufferLong(size);
		DataBufferLong df = new DataBufferLong(size);
		DataBufferLong di = new DataBufferLong(size);
		DataBufferLong ds = new DataBufferLong(size);

		for (int i = size - 1; i >= 0; i--) {

			dbool.setBoolean(i, tab[i] > 0);
			db.setByte(i, (byte) tab[i]);
			dc.setChar(i, (char) tab[i]);
			dd.setDouble(i, (double) tab[i]);
			df.setFloat(i, (float) tab[i]);
			di.setInt(i, (int) tab[i]);
			ds.setShort(i, (short) tab[i]);
		}

		for (int i = size - 1; i >= 0; i--) {
			assertEquals((tab[i] > 0 ? 1 : 0), dbool.getLong(i));
			assertEquals((byte) tab[i], db.getByte(i));
			assertEquals((char) tab[i], dc.getChar(i));
			assertEquals((double) tab[i], dd.getDouble(i), 0.0);
			assertEquals((float) tab[i], df.getFloat(i), 0.0);
			assertEquals((int) tab[i], di.getInt(i));
			assertEquals((short) tab[i], ds.getShort(i));
		}

		try {
			d.getBoolean(0);
			fail("getting a boolean on a DataBufferLong should be imposible");
		} catch (IllegalAccessError ignored) {
		}

	}

	@Override
	@Test
	public void testClone() {
		DataBufferLong d = new DataBufferLong(tab);
		DataBufferLong dd = d.clone();
		assertNotSame(d, dd, "A cloned object cannot have the same reference");
		for (int i = d.getSize() - 1; i >= 0; i--) {
			assertEquals(d.getLong(i), dd.getLong(i));
		}
	}

	@Override
	@Test
	public void getData() {
		DataBufferLong d = new DataBufferLong(tab);
		assertSame(d.getData(), tab);
	}

	@Override
	@Test
	public void setData() {
		DataBufferLong d = new DataBufferLong(0);
		d.setData(tab);
		DataBufferLong dd = new DataBufferLong(0);
		dd.setData(d.clone());
		for (int i = size - 1; i >= 0; i--) {
			assertEquals(tab[i], d.getLong(i));
			assertEquals(tab[i], dd.getLong(i));
		}

		boolean tbool[] = testDataBufferBool.getTab(size);
		d.setData(tbool);
		for (int i = size - 1; i >= 0; i--) {
			assertTrue(tbool[i] != (((int) d.getLong(i)) % 2 == 0));
		}

		byte tb[] = testDataBufferByte.getTab(size);
		d.setData(tb);
		for (int i = size - 1; i >= 0; i--) {
			assertEquals((long) tb[i], d.getLong(i));
		}

		char tc[] = testDataBufferChar.getTab(size);
		d.setData(tc);
		for (int i = size - 1; i >= 0; i--) {
			assertEquals((long) tc[i], d.getLong(i));
		}

		double td[] = testDataBufferDouble.getTab(size);
		d.setData(td);
		for (int i = size - 1; i >= 0; i--) {
			assertEquals((long) td[i], d.getLong(i));
		}

		float tf[] = testDataBufferFloat.getTab(size);
		d.setData(tf);
		for (int i = size - 1; i >= 0; i--) {
			assertEquals((long) tf[i], d.getLong(i));
		}

		int ti[] = testDataBufferInt.getTab(size);
		d.setData(ti);
		for (int i = size - 1; i >= 0; i--) {
			assertEquals((long) ti[i], d.getLong(i));
		}

		short ts[] = testDataBufferShort.getTab(size);
		d.setData(ts);
		for (int i = size - 1; i >= 0; i--) {
			assertEquals((long) ts[i], d.getLong(i));
		}
		d = new DataBufferLong(0);

		try {
			d.setData(0.0);
			fail("setting any object other than numeric buffer on a DataBufferLong should be imposible");
		} catch (IllegalArgumentException ignored) {
		}

	}

	@Override
	@Test
	public void insertData() {
		long tab2[] = getTab(size);

		DataBufferLong d = new DataBufferLong(tab);
		DataBufferLong dd = new DataBufferLong(tab2);
		d.insertData(d.getSize(), dd);
		assertEquals(d.getSize(), dd.getSize() * 2);
		for (int i = size - 1; i >= 0; i--) {
			assertEquals(tab[i], d.getLong(i));
		}
		for (int i = size * 2 - 1; i >= size; i--) {
			assertEquals(tab2[i - size], d.getLong(i));
		}
		d.insertData(0, dd);
		for (int i = size - 1; i >= 0; i--) {
			assertEquals(tab2[i], d.getLong(i));
		}

		dd.insertValues(dd.getSize(), 10);
		assertEquals(dd.getSize(), tab.length + 10);
		for (int i = size - 1; i >= 0; i--) {
			assertEquals(tab2[i], dd.getLong(i));
		}
		dd.insertValues(0, 10);
		assertEquals(dd.getSize(), tab.length + 20);
		for (int i = size - 1; i >= 0; i--) {
			assertEquals(tab2[i], dd.getLong(i + 10));
		}
	}

	@Override
	@Test
	public void removeValues() {
		DataBufferLong d = new DataBufferLong(tab);
		d.removeValues(0, 10);
		assertEquals(40, d.getSize());
		for (int i = 9; i >= 0; i--) {
			assertEquals(tab[i + 10], d.getLong(i));
		}
		d = new DataBufferLong(tab);
		d.removeValues(d.getSize() - 10, 10);
		assertEquals(40, d.getSize());
		for (int i = 9; i >= 0; i--) {
			assertEquals(tab[i], d.getLong(i));
		}

		d = new DataBufferLong(tab);
		try {
			d.removeValues(0, size + 10);
			fail();
		} catch (Exception e) {
			assertTrue(true);
		}

		d = new DataBufferLong(tab);
		try {
			d.removeValues(-1, size + 10);
			fail();
		} catch (Exception e) {
			assertTrue(true);
		}
	}

	@Override
	@Test
	public void serialize() {
		FileOutputStream fOut = null;
		ObjectOutputStream oOut = null;
		FileInputStream fIn = null;
		ObjectInputStream oIn = null;

		DataBufferLong d = new DataBufferLong(tab);
		boolean ok = true;
		try {
			fOut = new FileOutputStream(".test_databufferlong.dat");
			oOut = new ObjectOutputStream(fOut);
			oOut.writeObject(d);
		} catch (IOException e) {
			e.printStackTrace();
			ok = false;
		} finally {
			try {
				assert oOut != null;
				oOut.flush();
				oOut.close();
				fOut.close();
			} catch (IOException e1) {
				e1.printStackTrace();
				ok = false;
			}
		}
		try {
			fIn = new FileInputStream(".test_databufferlong.dat");
			oIn = new ObjectInputStream(fIn);
			DataBufferLong dd = (DataBufferLong) oIn.readObject();
			assertEquals(dd.getSize(), d.getSize());
			for (int i = d.getSize() - 1; i >= 0; i--) {
				assertEquals(d.getLong(i), dd.getLong(i));
			}
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
			ok = false;
		} finally {
			try {
				oIn.close();
				fIn.close();
				java.io.File f = new File(".test_databufferlong.dat");
				assertTrue(f.delete());
			} catch (IOException e1) {
				e1.printStackTrace();
				ok = false;
			}
		}
		assertTrue(ok);

	}

}
