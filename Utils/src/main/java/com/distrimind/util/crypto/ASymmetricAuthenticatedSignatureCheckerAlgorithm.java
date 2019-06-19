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
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.InvalidParameterSpecException;
import java.security.spec.PSSParameterSpec;

import com.distrimind.util.Bits;


/**
 * 
 * @author Jason Mahdjoub
 * @version 3.0
 * @since Utils 1.7
 */
public class ASymmetricAuthenticatedSignatureCheckerAlgorithm extends AbstractAuthenticatedCheckerAlgorithm {
	private final ASymmetricPublicKey distantPublicKey;

	private final AbstractSignature signer;
	private final ASymmetricAuthenticatedSignatureType type;

	private byte[] signature=null;
	public ASymmetricAuthenticatedSignatureCheckerAlgorithm(ASymmetricPublicKey distantPublicKey)
			throws NoSuchAlgorithmException, NoSuchProviderException {
		if (distantPublicKey == null)
			throw new NullPointerException("distantPublicKey");
		type=distantPublicKey.getAuthentifiedSignatureAlgorithmType();
		if (type==null)
			throw new IllegalArgumentException("The given key is not destinated to a signature process");
		this.distantPublicKey = distantPublicKey;
		this.signer = type.getSignatureInstance();
	}

	public ASymmetricPublicKey getDistantPublicKey() {
		return distantPublicKey;
	}

	

	@Override
	public void init(byte[] signature, int offs, int lens)
			throws InvalidKeyException, NoSuchAlgorithmException, InvalidKeySpecException, NoSuchProviderException, InvalidAlgorithmParameterException, InvalidParameterSpecException, IOException {
		if (type== ASymmetricAuthenticatedSignatureType.BC_FIPS_SHA256withRSAandMGF1 || type== ASymmetricAuthenticatedSignatureType.BC_FIPS_SHA384withRSAandMGF1 || type== ASymmetricAuthenticatedSignatureType.BC_FIPS_SHA512withRSAandMGF1)
		{
			byte[][] tmp=Bits.separateEncodingsWithIntSizedTabs(signature, offs, lens);
			this.signature=tmp[0];
			byte[] encParameters=tmp[1];
			AlgorithmParameters pssParameters;
			pssParameters = AlgorithmParameters.getInstance("PSS","BCFIPS");
			pssParameters.init(encParameters);

			PSSParameterSpec pssParameterSpec = pssParameters.getParameterSpec(PSSParameterSpec.class);
			((JavaNativeSignature)signer).getSignature().setParameter(pssParameterSpec);
		}
		else
		{
			this.signature=new byte[lens];
			
			System.arraycopy(signature, offs, this.signature, 0, this.signature.length);
		}
		signer.initVerify(distantPublicKey);

		
	}

	@Override
	public void update(byte[] message, int offm, int lenm) throws SignatureException {
		signer.update(message, offm, lenm);
		
		
	}

	@Override
	public boolean verify() throws SignatureException, IllegalStateException {
		try
		{
			return signer.verify(signature);
		}
		finally
		{
			signature=null;
		}
	}

}