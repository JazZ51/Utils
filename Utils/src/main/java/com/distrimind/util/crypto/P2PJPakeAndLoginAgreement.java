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

import org.bouncycastle.crypto.CryptoException;

import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.spec.InvalidKeySpecException;

/**
 * 
 * @author Jason Mahdjoub
 * @version 3.0
 * @since Utils 3.15.0
 */
public class P2PJPakeAndLoginAgreement extends P2PLoginAgreement {
	
	private final P2PJPAKESecretMessageExchanger jpake;
	private final P2PLoginWithSymmetricSignature login;
	/*P2PJPakeAndLoginAgreement(AbstractSecureRandom random, Serializable participantID, char[] message, SymmetricSecretKey secretKeyForSignature) throws NoSuchAlgorithmException, InvalidKeySpecException, NoSuchProviderException, IOException {
	
		this(random, participantID, message, null, 0, 0, secretKeyForSignature);
	}*/
	P2PJPakeAndLoginAgreement(AbstractSecureRandom random, byte[] participantID, char[] message, byte[] salt,
			int offset_salt, int len_salt, SymmetricSecretKey secretKeyForSignature) throws NoSuchAlgorithmException, InvalidKeySpecException, NoSuchProviderException {
		super(secretKeyForSignature==null?3:5, secretKeyForSignature==null?3:5);
		jpake=new P2PJPAKESecretMessageExchanger(random, participantID, message, salt, offset_salt, len_salt);
		if (secretKeyForSignature==null)
			login=null;
		else
			login=new P2PLoginWithSymmetricSignature(secretKeyForSignature, random);
	}
	/*P2PJPakeAndLoginAgreement(AbstractSecureRandom random, Serializable participantID, byte[] message, boolean messageIsKey, SymmetricSecretKey secretKeyForSignature) throws NoSuchAlgorithmException, InvalidKeySpecException, NoSuchProviderException, IOException {
		
		this(random, participantID, message, 0, message.length,null, 0, 0, messageIsKey, secretKeyForSignature);
	}*/
	P2PJPakeAndLoginAgreement(AbstractSecureRandom random, byte[] participantID, byte[] message, int offset, int len, byte[] salt,
							  int offset_salt, int len_salt, boolean messageIsKey, SymmetricSecretKey secretKeyForSignature) throws NoSuchAlgorithmException, InvalidKeySpecException, NoSuchProviderException {
		super(secretKeyForSignature==null?3:5, secretKeyForSignature==null?3:5);
		jpake=new P2PJPAKESecretMessageExchanger(random, participantID, message, offset, len, salt, offset_salt, len_salt, messageIsKey);
		if (secretKeyForSignature==null)
			login=null;
		else
			login=new P2PLoginWithSymmetricSignature(secretKeyForSignature, random);
	}
	@Override
	protected boolean isAgreementProcessValidImpl() {
		
		return jpake.isAgreementProcessValidImpl() && (login==null || login.isAgreementProcessValidImpl());
	}
	@Override
	protected byte[] getDataToSend(int stepNumber) throws Exception {
		if (login!=null && stepNumber<2)
			return login.getDataToSend();
		else
			return jpake.getDataToSend();
	}
	@Override
	protected void receiveData(int stepNumber, byte[] data) throws CryptoException {
		if (login!=null && stepNumber<2)
			login.receiveData(data);
		else
			jpake.receiveData(data);
		
	}
}
