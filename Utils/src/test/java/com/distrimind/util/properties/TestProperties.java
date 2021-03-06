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
package com.distrimind.util.properties;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.Properties;

/**
 * 
 * @author Jason Mahdjoub
 * @version 1.0
 * @since Utils 1.6.1
 */
public class TestProperties {
	@DataProvider(name = "getPropertiesExample")
	Object[][] getPropertiesExample() throws IOException,
			NoSuchAlgorithmException, NoSuchProviderException {
		PropertiesExample[][] res = new PropertiesExample[100][];
		res[0] = new PropertiesExample[] { new PropertiesExample(null) };
		for (int i = 1; i < res.length; i++) {
			PropertiesExample pe = new PropertiesExample(null);
			pe.generateValues();
			res[i] = new PropertiesExample[] { pe };
		}

		return res;

	}

	@Test
	void testFreeProperties() {
		Properties p = new Properties();
		p.put("freeProp", "valueFreeProp");
		PropertiesExample pe = new PropertiesExample(null);
		pe.loadFromProperties(p);
		Assert.assertTrue(pe.getFreeStringProperties().containsKey("freeProp"));
		Assert.assertEquals("valueFreeProp", pe.getFreeStringProperties().get("freeProp"));
	}

	@SuppressWarnings("ResultOfMethodCallIgnored")
	@Test(dataProvider = "getPropertiesExample")
	public void testPropertiesExport(PropertiesExample pe) throws PropertiesParseException, CloneNotSupportedException {
		Properties p = pe.convertToStringProperties();
		PropertiesExample pe2 = new PropertiesExample(null);
		pe2.loadFromProperties(p);
		pe.equals(pe2);
		Assert.assertEquals(pe2, pe);
        p = pe.convertToStringProperties(pe.clone());
        pe2 = new PropertiesExample(null);
        pe2.booleanValue=!pe.booleanValue;
        pe2.loadFromProperties(p);
        pe.equals(pe2);
        Assert.assertNotEquals(pe2, pe);
        pe2 = pe.clone();
        pe2.loadFromProperties(p);
        pe.equals(pe2);
        Assert.assertEquals(pe2, pe);
	}

	@SuppressWarnings("ResultOfMethodCallIgnored")
	@Test(dataProvider = "getPropertiesExample")
	public void testPropertiesXMLSave(PropertiesExample pe) throws PropertiesParseException, IOException, CloneNotSupportedException {
		File f = new File("propertiesExemple.xml");
		pe.saveXML(f);
		PropertiesExample pe2 = new PropertiesExample(null);
		pe2.loadXML(f);
		pe.equals(pe2);
		Assert.assertEquals(pe2, pe.clone());
		f.delete();
		pe.saveXML(f, pe);
		pe2 = new PropertiesExample(null);
        pe2.booleanValue=!pe.booleanValue;
		pe2.loadXML(f);
		pe.equals(pe2);
        Assert.assertNotEquals(pe2, pe);
        pe2 = pe.clone();
        pe2.loadXML(f);
        pe.equals(pe2);
        Assert.assertEquals(pe2, pe);
		f.delete();
	}

	@SuppressWarnings("ResultOfMethodCallIgnored")
	@Test(dataProvider = "getPropertiesExample")
	public void testPropertiesYAMLSave(PropertiesExample pe) throws IOException, CloneNotSupportedException {
		try
		{
			File f = new File("propertiesExemple.yaml");
			pe.saveYAML(f);
			PropertiesExample pe2 = new PropertiesExample(null);
			pe2.loadYAML(f);
			pe.equals(pe2);
			Assert.assertEquals(pe2, pe);
			f.delete();
            pe.saveYAML(f, pe.clone());
            pe2 = new PropertiesExample(null);
            pe2.booleanValue=!pe.booleanValue;
            pe2.loadYAML(f);
            pe.equals(pe2);
            Assert.assertNotEquals(pe2, pe);
            pe2 = pe.clone();
            pe2.loadYAML(f);
            pe.equals(pe2);
            Assert.assertEquals(pe2, pe);
            f.delete();
		}
		catch(Exception e)
		{
			e.printStackTrace();
			throw e;
		}
		
	}
}
