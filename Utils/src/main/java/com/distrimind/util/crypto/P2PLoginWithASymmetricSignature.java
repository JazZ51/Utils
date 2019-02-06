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
 * @version 1.1
 * @since MaDKitLanEdition 3.23.0
 */
public class P2PLoginWithASymmetricSignature extends P2PLoginAgreement{
    private final ASymmetricPrivateKey privateKey;
    private byte[] myMessage, otherMessage=null;
    static final int messageSize=32;
    private boolean valid=true;

    P2PLoginWithASymmetricSignature(ASymmetricKeyPair keyPair, AbstractSecureRandom random) {
        super(2, 2);
        if (keyPair==null)
            throw new NullPointerException();
        if (keyPair.getAuthentifiedSignatureAlgorithmType()==null)
            throw new IllegalArgumentException("The given key pair is not usable for signature");
        this.privateKey=keyPair.getASymmetricPrivateKey();
        myMessage=new byte[messageSize];
        random.nextBytes(myMessage);

    }

    @Override
    protected boolean isAgreementProcessValidImpl() {
        return valid;
    }

    @Override
    protected byte[] getDataToSend(int stepNumber) throws Exception {
        if (!valid)
            throw new CryptoException();

        try {
            switch (stepNumber) {
                case 0:
                    return myMessage;
                case 1: {
                    if (otherMessage == null) {
                        valid = false;
                        throw new IllegalAccessError();
                    }
                    ASymmetricAuthentifiedSignerAlgorithm signer = new ASymmetricAuthentifiedSignerAlgorithm(privateKey);
                    signer.init();
                    signer.update(myMessage);
                    signer.update(otherMessage);
                    return signer.getSignature();

                }
                default:
                    valid = false;
                    throw new IllegalAccessError();
            }
        }
        catch(Exception e)
        {
            valid=false;
            throw e;
        }

    }

    @Override
    protected void receiveData(int stepNumber, byte[] data) throws CryptoException {
        if (!valid)
            throw new CryptoException();

        switch(stepNumber)
        {
            case 0:
            {
                if (otherMessage!=null)
                {
                    valid=false;
                    throw new CryptoException();
                }
                if (data.length!=messageSize)
                {
                    valid=false;
                    throw new CryptoException();
                }
                otherMessage=data;
            }
            break;
            case 1:
            {
                if (data.length!=0)
                {
                    valid=false;
                    throw new CryptoException();
                }
            }
            break;
            default:
                valid=false;
                throw new CryptoException(""+stepNumber);
        }
    }
}