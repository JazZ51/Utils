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

import gnu.vm.java.security.spec.InvalidKeySpecException;

/**
 * 
 * @author Jason Mahdjoub
 * @version 2.0
 * @since Utils 1.7
 */
public class SignerAlgorithm
{
    private final ASymmetricPrivateKey localPrivateKey;

    private final AbstractSignature signature;

    public SignerAlgorithm(AbstractSignature signature, ASymmetricPrivateKey localPrivateKey)
    {
	if (signature == null)
	    throw new NullPointerException("signature");
	if (localPrivateKey == null)
	    throw new NullPointerException("localPrivateKey");
	this.localPrivateKey = localPrivateKey;
	this.signature = signature;
    }

    public SignerAlgorithm(ASymmetricPrivateKey localPrivateKey) throws gnu.vm.java.security.NoSuchAlgorithmException
    {
	this(localPrivateKey.getAlgorithmType().getDefaultSignatureAlgorithm()
		.getSignatureInstance(), localPrivateKey);
    }

    public SignerAlgorithm(SignatureType type, ASymmetricPrivateKey localPrivateKey) throws gnu.vm.java.security.NoSuchAlgorithmException
    {
	this(type.getSignatureInstance(), localPrivateKey);
    }

    public ASymmetricPrivateKey getLocalPrivateKey()
    {
	return localPrivateKey;
    }

    public AbstractSignature getSignature()
    {
	return signature;
    }

    public byte[] sign(byte bytes[]) throws gnu.vm.java.security.InvalidKeyException, gnu.vm.java.security.SignatureException, gnu.vm.java.security.NoSuchAlgorithmException, InvalidKeySpecException
    {
	return this.sign(bytes, 0, bytes.length);
    }

    public byte[] sign(byte bytes[], int off, int len) throws gnu.vm.java.security.InvalidKeyException, gnu.vm.java.security.SignatureException, gnu.vm.java.security.NoSuchAlgorithmException, InvalidKeySpecException
    {
	signature.initSign(localPrivateKey);
	signature.update(bytes, off, len);
	return signature.sign();
    }

    public void sign(byte message[], int offm, int lenm, byte signature[], int off_sig, int len_sig) throws gnu.vm.java.security.InvalidKeyException, gnu.vm.java.security.SignatureException, gnu.vm.java.security.NoSuchAlgorithmException, InvalidKeySpecException
    {
	this.signature.initSign(localPrivateKey);
	this.signature.update(message, offm, lenm);
	this.signature.sign(signature, off_sig, len_sig);
    }

}