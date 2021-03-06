package com.distrimind.util.io;
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

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.Random;

/**
 * @author Jason Mahdjoub
 * @version 1.0
 * @since Utils 1.30.0
 */
public class TestStreams {

	@Test(invocationCount = 1000, threadPoolSize = 16, dataProvider = "provideInputStreams")
	public void testInputStreams(RandomInputStream inputStream, RandomInputStream referenceInputStream) throws IOException {
		testInputStreams(inputStream, 9000, referenceInputStream);
	}

	@DataProvider(name = "provideInputStreams")
	Object[][] provideInputStreams() throws IOException {
		Object[][] res=new Object[6][2];
		Random rand=new Random(System.currentTimeMillis());
		byte[] tab=new byte[1000000];
		rand.nextBytes(tab);
		RandomByteArrayInputStream ris=new RandomByteArrayInputStream(tab);
		res[0][0]=new BufferedRandomInputStream(new RandomByteArrayInputStream(tab));
		res[0][1]=ris;
		tab=new byte[1000];
		rand.nextBytes(tab);
		int s, l;
		res[1][0]=new LimitedRandomInputStream(new RandomByteArrayInputStream(tab), s=rand.nextInt(100), l=tab.length-s-rand.nextInt(100));
		res[1][1]=new RandomByteArrayInputStream(Arrays.copyOfRange(tab, s, l+s));
		tab=new byte[1000];
		rand.nextBytes(tab);
		byte[] tab2=new byte[500];
		rand.nextBytes(tab2);
		res[2][0]=new AggregatedRandomInputStreams(new RandomByteArrayInputStream(tab), new RandomByteArrayInputStream(tab2));
		byte[] tab3=new byte[tab.length+tab2.length];
		System.arraycopy(tab, 0, tab3, 0, tab.length);
		System.arraycopy(tab2, 0, tab3, tab.length, tab2.length);
		res[2][1]=new RandomByteArrayInputStream(tab3);

		FragmentedStreamParameters parameters=new FragmentedStreamParameters((byte)2, (byte)0);
		tab=new byte[1000+(rand.nextBoolean()?1:0)];
		rand.nextBytes(tab);
		tab2=new byte[tab.length/2+tab.length%2];
		tab3=new byte[tab.length/2];
		for (int i=0;i<tab2.length;i++)
			tab2[i]=tab[i*2];
		for (int i=0;i<tab3.length;i++)
			tab3[i]=tab[i*2+1];
		res[3][0]=new FragmentedRandomInputStream(parameters, new RandomByteArrayInputStream(tab2), new RandomByteArrayInputStream(tab3));
		res[3][1]=new RandomByteArrayInputStream(tab);
		res[4][0]=new FragmentedRandomInputStreamPerChannel(parameters, new RandomByteArrayInputStream(tab));
		res[4][1]=new RandomByteArrayInputStream(tab2);
		res[5][0]=new FragmentedRandomInputStreamPerChannel(new FragmentedStreamParameters((byte)2, (byte)1), new RandomByteArrayInputStream(tab));
		res[5][1]=new RandomByteArrayInputStream(tab3);
		return res;
	}



	@SuppressWarnings("ResultOfMethodCallIgnored")
	private void testInputStreams(RandomInputStream inputStream, int maxCycles, RandomInputStream referenceInputStream) throws IOException {
		Random rand=new Random(System.currentTimeMillis());
		Assert.assertEquals(inputStream.length(), referenceInputStream.length());
		for (int i=0;i<maxCycles;i++)
		{
			/*if (i%(maxCycles/100)==0)
				System.out.println(((i*100)/maxCycles));*/
			Assert.assertEquals(inputStream.available(), referenceInputStream.available());
			if (rand.nextDouble()<0.1) {
				if (rand.nextDouble()<0.5) {
					long pos = (long) (Math.random() * inputStream.length());
					inputStream.seek(pos);
					referenceInputStream.seek(pos);
				}
				else
				{
					long skip=(long)(Math.random()*Math.min(16000, inputStream.available()));
					if (rand.nextDouble()<0.5) {
						inputStream.skip(skip);
						referenceInputStream.skip(skip);
					}
					else
					{
						inputStream.skipBytes((int)skip);
						referenceInputStream.skipBytes((int)skip);
					}
				}
			}
			Assert.assertEquals(inputStream.currentPosition(), referenceInputStream.currentPosition());
			if (inputStream.available()>0) {
				if (rand.nextDouble() < 0.5) {
					Assert.assertEquals(inputStream.read(), referenceInputStream.read());
				}
				else
				{
					byte[] bytes;
					byte[] bytes2;
					if (rand.nextDouble()<0.5)
					{
						bytes=new byte[(int)(Math.random()*inputStream.available())];
						bytes2=new byte[bytes.length];
						if (rand.nextDouble() < 0.5) {
							inputStream.readNBytes(bytes, 0, bytes.length);
							referenceInputStream.readNBytes(bytes2, 0, bytes2.length);
						} else {
							inputStream.readFully(bytes);
							referenceInputStream.readFully(bytes2);

						}
					}
					else
					{
						bytes=inputStream.readNBytes((int)(Math.random()*inputStream.available()));
						bytes2=referenceInputStream.readNBytes(bytes.length);
					}


					Assert.assertEquals(bytes, bytes2, "length="+bytes.length);

				}
			}

		}
	}

	@DataProvider(name = "provideOutputStreams")
	public Object[][] provideOutputStreams() throws IOException {
		Object[][] res=new Object[7][2];
		RandomOutputStream dest=new RandomByteArrayOutputStream();
		RandomOutputStream outputStream=new BufferedRandomOutputStream(dest);
		res[0][0]=dest;
		res[0][1]=outputStream;

		RandomCacheFileCenter.getSingleton().setMaxMemoryUsedToStoreDataIntoMemoryInsteadOfFiles(1024*1024);
		dest=RandomCacheFileCenter.getSingleton().getNewRandomCacheFileOutputStream(true);
		outputStream=dest;
		res[1][0]=dest;
		res[1][1]=outputStream;

		dest=RandomCacheFileCenter.getSingleton().getNewBufferedRandomCacheFileOutputStream(true);
		outputStream=dest;
		res[2][0]=dest;
		res[2][1]=outputStream;

		dest=new AggregatedRandomOutputStreams(new RandomOutputStream[]{
				new RandomByteArrayOutputStream(),
				new RandomByteArrayOutputStream()
		}, new long[]{
				10,
				10000000
		});
		outputStream=dest;

		res[3][0]=dest;
		res[3][1]=outputStream;

		dest=new FragmentedRandomOutputStream((byte)2,new RandomByteArrayOutputStream(), new RandomByteArrayOutputStream());
		outputStream=dest;
		res[4][0]=dest;
		res[4][1]=outputStream;

		dest=new FragmentedRandomOutputStreamPerChannel(new FragmentedStreamParameters((byte)2, (byte)0),new RandomByteArrayOutputStream());
		outputStream=dest;
		res[5][0]=dest;
		res[5][1]=outputStream;

		dest=new FragmentedRandomOutputStreamPerChannel(new FragmentedStreamParameters((byte)2, (byte)1),new RandomByteArrayOutputStream());
		outputStream=dest;
		res[6][0]=dest;
		res[6][1]=outputStream;

		return res;

	}

	@Test(dependsOnMethods = "testInputStreams", invocationCount = 1000, threadPoolSize = 16, dataProvider = "provideOutputStreams")
	public void testOutputStreams(RandomOutputStream dest, RandomOutputStream outputStream) throws IOException {
		int maxCycles=10000;
		Random rand=new Random(System.currentTimeMillis());
		RandomByteArrayOutputStream dest2=new RandomByteArrayOutputStream();
		for (int i=0;i<maxCycles;i++)
		{
			/*if (i%(maxCycles/100)==0)
				System.out.println(((i*100)/maxCycles));*/
			if (outputStream.length()>100 && rand.nextDouble()<0.1)
			{
				if (rand.nextDouble()<0.2)
				{
					if (rand.nextDouble()<0.5) {
						outputStream.setLength(outputStream.length()*4/5);
						dest2.setLength(dest2.length()*4/5);
					}
					else
					{
						outputStream.ensureLength(outputStream.length()*4/5);
						dest2.ensureLength(dest2.length()*4/5);
					}
				}
				else
				{
					long pos=(long)(Math.random()*outputStream.length());
					outputStream.seek(pos);
					dest2.seek(pos);
				}
			}
			Assert.assertEquals(outputStream.currentPosition(), dest2.currentPosition());
			if (rand.nextDouble()>0.5)
			{
				int v=rand.nextInt();
				outputStream.write(v);
				dest2.write(v);
			}
			else
			{
				byte[] bytes=new byte[(int)(20+Math.random()*20000)];
				rand.nextBytes(bytes);
				outputStream.write(bytes);
				dest2.write(bytes);
			}
			if (rand.nextDouble()<0.05) {

				StringBuilder s= new StringBuilder("test");
				while(Math.random()<0.3)
					s.append("0");
				outputStream.writeString(s.toString(), false, Integer.MAX_VALUE);
				dest2.writeString(s.toString(), false, Integer.MAX_VALUE);
			}
			if (rand.nextDouble()<0.05)
				outputStream.flush();
			Assert.assertEquals(outputStream.length(),dest2.length() );
		}

		outputStream.flush();
		dest2.flush();
		Assert.assertEquals(outputStream.length(),dest2.length() );
		Assert.assertEquals(dest.length(),dest2.length() );
		RandomInputStream inputStream=outputStream.getRandomInputStream();
		RandomInputStream ris=dest2.getRandomInputStream();

		Assert.assertEquals(inputStream.length(), outputStream.length());
		testInputStreams(inputStream, maxCycles, ris);
		outputStream.setLength(0);
		dest.setLength(0);
	}

}
