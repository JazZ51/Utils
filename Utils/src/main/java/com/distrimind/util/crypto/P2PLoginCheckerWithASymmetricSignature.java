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

/**
 * @author Jason Mahdjoub
 * @version 1.0
 * @since MaDKitLanEdition 3.23.0
 */
public class P2PLoginCheckerWithASymmetricSignature extends P2PLoginAgreement{
    private final ASymmetricPublicKey publicKey;
    private byte[] myMessage, otherMessage=null;

    private boolean valid=true;

    P2PLoginCheckerWithASymmetricSignature(ASymmetricPublicKey publicKey, AbstractSecureRandom random) {
        super(2, 2);
        if (publicKey==null)
            throw new NullPointerException();
        if (publicKey.getAuthentifiedSignatureAlgorithmType()==null)
            throw new IllegalArgumentException("The given public key is not usable for signature");
        this.publicKey=publicKey;
        myMessage=new byte[P2PLoginWithASymmetricSignature.messageSize];
        random.nextBytes(myMessage);

    }

    @Override
    protected boolean isAgreementProcessValidImpl() {
        return valid;
    }

    private final static byte[] emptyTab=new byte[0];
    @Override
    protected byte[] getDataToSend(int stepNumber) throws CryptoException {
        if (!valid)
            throw new CryptoException();

        switch(stepNumber)
        {
            case 0:
                return myMessage;
            case 1:
                return emptyTab;

            default:
                valid=false;
                throw new IllegalAccessError();
        }

    }

    @Override
    protected void receiveData(int stepNumber, byte[] data) throws CryptoException {
        if (!valid)
            throw new CryptoException();

        try {
            switch (stepNumber) {
                case 0: {
                    if (data.length != P2PLoginWithASymmetricSignature.messageSize) {
                        valid = false;
                        throw new CryptoException();
                    }
                    otherMessage = data;
                }
                break;
                case 1: {
                    if (otherMessage == null) {
                        valid = false;
                        throw new CryptoException();
                    }
                    ASymmetricAuthentifiedSignatureCheckerAlgorithm checker = new ASymmetricAuthentifiedSignatureCheckerAlgorithm(publicKey);
                    checker.init(data);
                    checker.update(otherMessage);
                    checker.update(myMessage);

                    valid = checker.verify();
                }
                break;
                default:
                    valid = false;
                    throw new CryptoException("" + stepNumber);
            }
        }
        catch (Exception e)
        {
            valid = false;
            if (e instanceof CryptoException)
                throw (CryptoException)e;
            else
                throw new CryptoException("", e);
        }
    }
}