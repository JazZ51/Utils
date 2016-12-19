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

import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;

/**
 * 
 * @author Jason Mahdjoub
 * @version 1.0
 * @since Utils 2.0
 */
public enum SecureRandomType
{
    DEFAULT(null, null, false), 
    GNU_DEFAULT(null, null, true), 
    SHA1PRNG("SHA1PRNG", "SUN", false), 
    GNU_SHA1PRNG("SHA1PRNG", "GNU-Crypto", true), 
    GNU_SHA256PRNG("SHA-256PRNG", "GNU-Crypto", true), 
    GNU_SHA384PRNG("SHA-384PRNG", "GNU-Crypto", true), 
    GNU_SHA512PRNG("SHA-512PRNG", "GNU-Crypto", true), 
    GNU_WIRLPOOLPRNG("WHIRLPOOLPRNG","GNU-Crypto",true), 
    SPEEDIEST(SHA1PRNG);

    private final String algorithmeName;

    private final String provider;

    private final boolean gnuVersion;

    private SecureRandomType(SecureRandomType type)
    {
	this(type.algorithmeName, type.provider, type.gnuVersion);
    }

    private SecureRandomType(String algorithmName, String provider, boolean gnuVersion)
    {
	this.algorithmeName = algorithmName;
	this.provider = provider;
	this.gnuVersion = gnuVersion;
    }

    public AbstractSecureRandom getInstance() throws gnu.vm.java.security.NoSuchAlgorithmException, gnu.vm.java.security.NoSuchProviderException
    {
	if (gnuVersion)
	{
	    if (algorithmeName == null)
		return new GnuSecureRandom(this,
			new gnu.vm.java.security.SecureRandom());
	    else
		return new GnuSecureRandom(this,
			gnu.vm.java.security.SecureRandom
				.getInstance(algorithmeName));
	}
	else
	{
	    try
	    {
		if (algorithmeName == null)
		    return new JavaNativeSecureRandom(this, new SecureRandom());
		else
		    return new JavaNativeSecureRandom(this,
			    SecureRandom.getInstance(algorithmeName, provider));
	    }
	    catch (NoSuchAlgorithmException e)
	    {
		throw new gnu.vm.java.security.NoSuchAlgorithmException(e);
	    }
	    catch (NoSuchProviderException e)
	    {
		throw new gnu.vm.java.security.NoSuchProviderException(
			e.getMessage());
	    }
	}

    }

    public AbstractSecureRandom getInstance(byte[] seed) throws gnu.vm.java.security.NoSuchAlgorithmException, gnu.vm.java.security.NoSuchProviderException
    {
	AbstractSecureRandom sr = getInstance();
	sr.setSeed(seed);
	return sr;
    }

    public AbstractSecureRandom getInstance(long seed) throws gnu.vm.java.security.NoSuchAlgorithmException, gnu.vm.java.security.NoSuchProviderException
    {
	AbstractSecureRandom sr = getInstance();
	sr.setSeed(seed);
	return sr;
    }

    public boolean isGNUVersion()
    {
	return gnuVersion;
    }

}