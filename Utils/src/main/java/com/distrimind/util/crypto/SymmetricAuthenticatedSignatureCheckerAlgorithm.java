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
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

/**
 * 
 * @author Jason Mahdjoub
 * @version 3.0
 * @since Utils 2.10.0
 */
public class SymmetricAuthenticatedSignatureCheckerAlgorithm extends AbstractAuthenticatedCheckerAlgorithm {

	private final SymmetricAuthenticatedSignerAlgorithm signer;
	private byte[] signature=null;
	private SymmetricAuthenticatedSignatureCheckerAlgorithm(SymmetricAuthenticatedSignerAlgorithm signer) {
		if (signer == null)
			throw new NullPointerException();
		this.signer = signer;
	}

	@Override
	public boolean isPostQuantumChecker() {
		return signer.isPostQuantumSigner();
	}

	@Override
	public int getMacLengthBytes() {
		return signer.getMacLengthBytes();
	}

	public SymmetricAuthenticatedSignatureCheckerAlgorithm(SymmetricSecretKey secretKey) throws NoSuchAlgorithmException, NoSuchProviderException, IOException {
		this(new SymmetricAuthenticatedSignerAlgorithm(secretKey));

	}

	public SymmetricSecretKey getSecretKey() {
		return signer.getSecretKey();
	}



	@Override
	public void init(byte[] signature, int off, int len) throws IOException {
		this.signature=new byte[len];
		
		System.arraycopy(signature, off, this.signature, 0, this.signature.length);

		signer.init();
	}

	@Override
	public void update(byte[] message, int offm, int lenm) throws IOException {
		signer.update(message, offm, lenm);
		
	}

	@Override
	public boolean verify()
			throws IllegalStateException {
		try
		{
			if (signature == null)
				return false;
			byte[] mySignature = signer.getSignature();
			if (mySignature.length != signature.length)
				return false;
			for (int i = 0; i < mySignature.length; i++)
				if (mySignature[i] != signature[i])
					return false;
			return true;
		} catch (IOException e) {
			return false;
		} finally
		{
			signature=null;
		}
	}



}
