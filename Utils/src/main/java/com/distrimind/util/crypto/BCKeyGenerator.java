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

import org.bouncycastle.crypto.SymmetricKeyGenerator;
import org.bouncycastle.crypto.fips.FipsAES;
import org.bouncycastle.crypto.general.Serpent;
import org.bouncycastle.crypto.general.Twofish;

/**
 * 
 * @author Jason Mahdjoub
 * @version 1.0
 * @since Utils 3.10.0
 */
public final class BCKeyGenerator extends AbstractKeyGenerator {

	private SymmetricKeyGenerator<org.bouncycastle.crypto.SymmetricSecretKey> keyGenerator;
	private short keySizeBits;
	
	BCKeyGenerator(SymmetricAuthentifiedSignatureType type) {
		super(type);
	}
	
	BCKeyGenerator(SymmetricEncryptionType type) {
		super(type);
	}

	@Override
	public SymmetricSecretKey generateKey() {
		if (encryptionType==null)
			return new SymmetricSecretKey(signatureType, keyGenerator.generateKey(), keySizeBits);
		else
			return new SymmetricSecretKey(encryptionType, keyGenerator.generateKey(), keySizeBits);
	}

	@Override
	public String getAlgorithm() {
		
		if (encryptionType==null)
			return signatureType.getAlgorithmName();
		else
			return encryptionType.getAlgorithmName();
	}

	@Override
	public String getProvider() {
		if (encryptionType==null)
			return signatureType.getCodeProviderForKeyGenerator().name();
		else
			return encryptionType.getCodeProviderForKeyGenerator().name();
	}

	

	@SuppressWarnings("unchecked")
	@Override
	public void init(short keySize, AbstractSecureRandom random) {
		if (encryptionType==null)
		{
			if (signatureType.getCodeProviderForKeyGenerator().equals(CodeProvider.BC) || signatureType.getCodeProviderForKeyGenerator().equals(CodeProvider.BCFIPS))
			{
				keyGenerator=new FipsAES.KeyGenerator(FipsAES.CBCwithPKCS7, keySize, random);
				this.keySizeBits=keySize;
			}
			else
				throw new IllegalAccessError();
			
		}
		else
		{
			if (encryptionType.getAlgorithmName().equals(SymmetricEncryptionType.BC_FIPS_AES.getAlgorithmName()))
			{
				keyGenerator=new FipsAES.KeyGenerator(FipsAES.CBCwithPKCS7, keySize, random);
				this.keySizeBits=keySize;
			}
			else if (encryptionType.getAlgorithmName().equals(SymmetricEncryptionType.BC_SERPENT.getAlgorithmName()))
			{
				keyGenerator=new Serpent.KeyGenerator(Serpent.CBCwithPKCS7, keySize, random);
				this.keySizeBits=keySize;
			}
			else if (encryptionType.getAlgorithmName().equals(SymmetricEncryptionType.BC_TWOFISH.getAlgorithmName()))
			{
				keyGenerator=new Twofish.KeyGenerator(Twofish.CBCwithPKCS7, keySize, random);
				this.keySizeBits=keySize;
			}
			else
				throw new IllegalAccessError();

		}
		
	}

}