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

import com.distrimind.util.data_buffers.WrappedData;
import com.distrimind.util.io.Integrity;
import com.distrimind.util.io.MessageExternalizationException;
import com.distrimind.bcfips.crypto.SymmetricSecretKey;
import com.distrimind.bcfips.crypto.*;
import com.distrimind.bcfips.crypto.fips.FipsAES;
import com.distrimind.bcfips.crypto.general.AES;
import com.distrimind.bcfips.crypto.general.ChaCha20;
import com.distrimind.bcfips.crypto.general.Serpent;
import com.distrimind.bcfips.crypto.general.Twofish;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.ShortBufferException;
import java.io.*;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;



/**
 * 
 * @author Jason Mahdjoub
 * @version 3.0
 * @since Utils 3.10.0
 */
@SuppressWarnings("ConstantConditions")
public class BCCipher extends AbstractCipher {

	final SymmetricEncryptionType type;
	private OutputCipher<?> cipher=null;
	private UpdateOutputStream processingStream=null;
	private ByteArrayOutputStream resultStream;
	private final SymmetricKeyWrapperType keyWrapperType;
	private byte[] iv;
	private KeyWrapper<?> wrapper;
	private KeyUnwrapper<?> unWrapper;
	private UpdateOutputStream aadStream=null;
	private int mode=-1;
	@Override
	public int getMode() {
		return mode;
	}
	
	BCCipher(SymmetricEncryptionType type)
	{
		this.type=type;
		this.keyWrapperType=null;
	}
	
	BCCipher(SymmetricKeyWrapperType keyWrapperType)
	{
		this.type=null;
		this.keyWrapperType=keyWrapperType;
	}
	
	@Override
	public byte[] doFinal() throws IOException {
		return doFinal(null, 0, 0);
	}



	@Override
	public byte[] doFinal(byte[] input, int inputOffset, int inputLength) throws IOException
			 {
		try
        {
            if (input != null && inputLength != 0)
            {
                processingStream.update(input, inputOffset, inputLength);
                
            }
            
            processingStream.flush();
            processingStream.close();
        }
        catch (IOException e)
        {
            if (cipher.getParameters() instanceof AuthenticationParametersWithIV)
            {
                throw new IllegalStateException(e.getMessage());
            }
			Exception bpe=new BadPaddingException(e.getMessage());
            if (mode==Cipher.ENCRYPT_MODE)
            	throw new IOException(bpe);
            else
            	throw new MessageExternalizationException(Integrity.FAIL, bpe);
        }

        byte[] result = resultStream.toByteArray();

        clearAndResetByteArrayOutputStream(resultStream);


        return result;
	}
	@Override
	public int doFinal(byte[] output, int outputOffset) throws IOException
			 {
		return doFinal(null, 0, 0, output, outputOffset);
	}

	static void clearAndResetByteArrayOutputStream(ByteArrayOutputStream bOut)
    {
        int size = bOut.size();

        bOut.reset();

        for (int i = 0; i != size; i++)
        {
            bOut.write(0);
        }

        bOut.reset();
    }

	@Override
	public int doFinal(byte[] input, int inputOffset, int inputLength, byte[] output, int outputOffset) throws IOException
			 {
		if (outputOffset + getOutputSize(inputLength) > output.length)
        {
            throw new IOException(new ShortBufferException("Output buffer too short for input."));
        }

        byte[] result = doFinal(input, inputOffset, inputLength);

        System.arraycopy(result, 0, output, outputOffset, result.length);

        Arrays.fill(result, (byte)0);

        return result.length;
	}



	@Override
	public String getAlgorithm() {
		
		return type.getAlgorithmName();
	}

	@Override
	public int getBlockSize() {
		return type.getBlockSizeBits()/8;
	}

	@Override
	public InputStream getCipherInputStream(InputStream in) {
		return new BCCipherInputStream(in);
	}

	@Override
	public OutputStream getCipherOutputStream(OutputStream out) {
		return new BCCipherOutputStream(out);
	}

	@Override
	public byte[] getIV() {
		return iv;
	}

	@Override
	public int getOutputSize(int inputLength) {
		/*if (type.getBlockMode().toUpperCase().equals("GCM"))
		{
			
			if (this.encryptor!=null)
			{
				return inputLength+16+12;
			}
			else
				return inputLength-16-12;
		}
		else*/
			return cipher.getMaxOutputSize(inputLength);
	}

	@Override
	public void init(int opMode, AbstractKey key) throws IOException
	{
		init(opMode, key, (byte[])null);
	}

	@Override
	public void init(int opMode, AbstractKey key, AbstractSecureRandom random) throws IOException
	{
		init(opMode, key, (byte[])null);
	}
	@Override
	public void init(int opMode, AbstractKey key, byte[] iv) throws IOException  {
		try {
			mode = opMode;
			this.iv = iv;
			OutputDecryptor<?> descriptor;
			OutputEncryptor<?> encryptor;
			this.wrapper = null;
			this.unWrapper = null;
			OutputAEADEncryptor<?> aeadEncryptor = null;
			OutputAEADDecryptor<?> readDescriptor = null;
			this.aadStream = null;
			resultStream = new ByteArrayOutputStream();
			if (opMode == Cipher.ENCRYPT_MODE) {

				if (type.getAlgorithmName().equals(SymmetricEncryptionType.BC_FIPS_AES_CBC_PKCS7Padding.getAlgorithmName())) {
					if (type.getBlockMode().equalsIgnoreCase("CBC") && type.getPadding().equalsIgnoreCase("PKCS7PADDING")) {
						FipsAES.OperatorFactory fipsSymmetricFactory = new FipsAES.OperatorFactory();
						FipsAES.Parameters param = FipsAES.CBCwithPKCS7;
						if (iv != null)
							param = param.withIV(iv);
						encryptor = fipsSymmetricFactory.createOutputEncryptor((SymmetricSecretKey) key.toBouncyCastleKey(), param);
					} else if (type.getBlockMode().equalsIgnoreCase("GCM") && type.getPadding().equalsIgnoreCase("NOPADDING")) {
						FipsAES.AEADOperatorFactory fipsSymmetricFactory = new FipsAES.AEADOperatorFactory();
						FipsAES.AuthParameters param = FipsAES.GCM;

						if (iv != null)
							param = param.withIV(iv).withMACSize(128);
						else
							param = param.withMACSize(128);

						encryptor = aeadEncryptor = fipsSymmetricFactory.createOutputAEADEncryptor((SymmetricSecretKey) key.toBouncyCastleKey(), param);
					} else if (type.getBlockMode().equalsIgnoreCase("EAX") && type.getPadding().equalsIgnoreCase("NOPADDING")) {
						AES.AEADOperatorFactory fipsSymmetricFactory = new AES.AEADOperatorFactory();
						AES.AuthParameters param = AES.EAX;

						if (iv != null)
							param = param.withIV(iv).withMACSize(128);
						else
							param = param.withMACSize(128);

						encryptor = aeadEncryptor = fipsSymmetricFactory.createOutputAEADEncryptor((SymmetricSecretKey) key.toBouncyCastleKey(), param);
					} else if (type.getBlockMode().equalsIgnoreCase("CTR") && type.getPadding().equalsIgnoreCase("NOPADDING")) {
						FipsAES.OperatorFactory fipsSymmetricFactory = new FipsAES.OperatorFactory();
						FipsAES.Parameters param = FipsAES.CTR;
						if (iv != null)
							param = param.withIV(iv);
						encryptor = fipsSymmetricFactory.createOutputEncryptor((SymmetricSecretKey) key.toBouncyCastleKey(), param);

					} else {
						throw new IllegalAccessError();
					}
				} else if (type.getAlgorithmName().equals(SymmetricEncryptionType.BC_SERPENT_CBC_PKCS7Padding.getAlgorithmName())) {
					if (type.getBlockMode().equalsIgnoreCase("CBC") && type.getPadding().equalsIgnoreCase("PKCS7PADDING")) {
						Serpent.OperatorFactory fipsSymmetricFactory = new Serpent.OperatorFactory();
						Serpent.Parameters param = Serpent.CBCwithPKCS7;
						if (iv != null)
							param = param.withIV(iv);
						encryptor = fipsSymmetricFactory.createOutputEncryptor((SymmetricSecretKey) key.toBouncyCastleKey(), param);
					} else if (type.getBlockMode().equalsIgnoreCase("CTR") && type.getPadding().equalsIgnoreCase("NOPADDING")) {
						Serpent.OperatorFactory fipsSymmetricFactory = new Serpent.OperatorFactory();
						Serpent.Parameters param = Serpent.CTR;
						if (iv != null)
							param = param.withIV(iv);
						encryptor = fipsSymmetricFactory.createOutputEncryptor((SymmetricSecretKey) key.toBouncyCastleKey(), param);
					} else if (type.getBlockMode().equalsIgnoreCase("GCM") && type.getPadding().equalsIgnoreCase("NOPADDING")) {
						Serpent.AEADOperatorFactory fipsSymmetricFactory = new Serpent.AEADOperatorFactory();
						Serpent.AuthParameters param = Serpent.GCM;

						if (iv != null)
							param = param.withIV(iv).withMACSize(128);
						else
							param = param.withMACSize(128);

						encryptor = aeadEncryptor = fipsSymmetricFactory.createOutputAEADEncryptor((SymmetricSecretKey) key.toBouncyCastleKey(), param);
					} else if (type.getBlockMode().equalsIgnoreCase("EAX") && type.getPadding().equalsIgnoreCase("NOPADDING")) {
						Serpent.AEADOperatorFactory fipsSymmetricFactory = new Serpent.AEADOperatorFactory();
						Serpent.AuthParameters param = Serpent.EAX;

						if (iv != null)
							param = param.withIV(iv).withMACSize(128);
						else
							param = param.withMACSize(128);

						encryptor = aeadEncryptor = fipsSymmetricFactory.createOutputAEADEncryptor((SymmetricSecretKey) key.toBouncyCastleKey(), param);
					} else {
						throw new IllegalAccessError();
					}
				} else if (type.getAlgorithmName().equals(SymmetricEncryptionType.BC_TWOFISH_CBC_PKCS7Padding.getAlgorithmName())) {
					if (type.getBlockMode().equalsIgnoreCase("CBC") && type.getPadding().equalsIgnoreCase("PKCS7PADDING")) {
						Twofish.OperatorFactory fipsSymmetricFactory = new Twofish.OperatorFactory();
						Twofish.Parameters param = Twofish.CBCwithPKCS7;
						if (iv != null)
							param = param.withIV(iv);
						encryptor = fipsSymmetricFactory.createOutputEncryptor((SymmetricSecretKey) key.toBouncyCastleKey(), param);
					} else if (type.getBlockMode().equalsIgnoreCase("CTR") && type.getPadding().equalsIgnoreCase("NOPADDING")) {
						Twofish.OperatorFactory fipsSymmetricFactory = new Twofish.OperatorFactory();
						Twofish.Parameters param = Twofish.CTR;
						if (iv != null)
							param = param.withIV(iv);
						encryptor = fipsSymmetricFactory.createOutputEncryptor((SymmetricSecretKey) key.toBouncyCastleKey(), param);
					} else if (type.getBlockMode().equalsIgnoreCase("GCM") && type.getPadding().equalsIgnoreCase("NOPADDING")) {
						Twofish.AEADOperatorFactory fipsSymmetricFactory = new Twofish.AEADOperatorFactory();
						Twofish.AuthParameters param = Twofish.GCM;

						if (iv != null)
							param = param.withIV(iv).withMACSize(128);
						else
							param = param.withMACSize(128);

						encryptor = aeadEncryptor = fipsSymmetricFactory.createOutputAEADEncryptor((SymmetricSecretKey) key.toBouncyCastleKey(), param);
					} else if (type.getBlockMode().equalsIgnoreCase("EAX") && type.getPadding().equalsIgnoreCase("NOPADDING")) {
						Twofish.AEADOperatorFactory fipsSymmetricFactory = new Twofish.AEADOperatorFactory();
						Twofish.AuthParameters param = Twofish.EAX;

						if (iv != null)
							param = param.withIV(iv).withMACSize(128);
						else
							param = param.withMACSize(128);

						encryptor = aeadEncryptor = fipsSymmetricFactory.createOutputAEADEncryptor((SymmetricSecretKey) key.toBouncyCastleKey(), param);
					} else {
						throw new IllegalAccessError();
					}
				} else if (type.getAlgorithmName().equals(SymmetricEncryptionType.BC_CHACHA20_NO_RANDOM_ACCESS.getAlgorithmName())) {
					ChaCha20.OperatorFactory factory = new ChaCha20.OperatorFactory();
					ChaCha20.Parameters param = ChaCha20.STREAM;
					if (iv != null)
						param = param.withIV(iv);
					encryptor = factory.createOutputEncryptor((SymmetricSecretKey) key.toBouncyCastleKey(), param);
				} else
					throw new IllegalAccessError();
				cipher = encryptor;

				processingStream = encryptor.getEncryptingStream(resultStream);
				if (aeadEncryptor != null) {
					aadStream = aeadEncryptor.getAADStream();
				}

			} else if (opMode == Cipher.DECRYPT_MODE) {
				if (type.getAlgorithmName().equals(SymmetricEncryptionType.BC_FIPS_AES_CBC_PKCS7Padding.getAlgorithmName())) {
					if (type.getBlockMode().equalsIgnoreCase("CBC") && type.getPadding().equalsIgnoreCase("PKCS7PADDING")) {
						FipsAES.OperatorFactory fipsSymmetricFactory = new FipsAES.OperatorFactory();
						FipsAES.Parameters param = FipsAES.CBCwithPKCS7;
						if (iv != null)
							param = param.withIV(iv);
						descriptor = fipsSymmetricFactory.createOutputDecryptor((SymmetricSecretKey) key.toBouncyCastleKey(), param);
					} else if (type.getBlockMode().equalsIgnoreCase("CTR") && type.getPadding().equalsIgnoreCase("NOPADDING")) {
						FipsAES.OperatorFactory fipsSymmetricFactory = new FipsAES.OperatorFactory();
						FipsAES.Parameters param = FipsAES.CTR;
						if (iv != null)
							param = param.withIV(iv);
						descriptor = fipsSymmetricFactory.createOutputDecryptor((SymmetricSecretKey) key.toBouncyCastleKey(), param);
					} else if (type.getBlockMode().equalsIgnoreCase("GCM") && type.getPadding().equalsIgnoreCase("NOPADDING")) {
						FipsAES.AEADOperatorFactory fipsSymmetricFactory = new FipsAES.AEADOperatorFactory();
						FipsAES.AuthParameters param = FipsAES.GCM;

						if (iv != null)
							param = param.withIV(iv).withMACSize(128);
						else
							param = param.withMACSize(128);

						descriptor = readDescriptor = fipsSymmetricFactory.createOutputAEADDecryptor((SymmetricSecretKey) key.toBouncyCastleKey(), param);
					} else if (type.getBlockMode().equalsIgnoreCase("EAX") && type.getPadding().equalsIgnoreCase("NOPADDING")) {
						AES.AEADOperatorFactory fipsSymmetricFactory = new AES.AEADOperatorFactory();
						AES.AuthParameters param = AES.EAX;

						if (iv != null)
							param = param.withIV(iv).withMACSize(128);
						else
							param = param.withMACSize(128);

						descriptor = readDescriptor = fipsSymmetricFactory.createOutputAEADDecryptor((SymmetricSecretKey) key.toBouncyCastleKey(), param);
					} else {
						throw new IllegalAccessError();
					}
				} else if (type.getAlgorithmName().equals(SymmetricEncryptionType.BC_SERPENT_CBC_PKCS7Padding.getAlgorithmName())) {
					if (type.getBlockMode().equalsIgnoreCase("CBC") && type.getPadding().equalsIgnoreCase("PKCS7PADDING")) {
						Serpent.OperatorFactory fipsSymmetricFactory = new Serpent.OperatorFactory();
						Serpent.Parameters param = Serpent.CBCwithPKCS7;
						if (iv != null)
							param = param.withIV(iv);
						descriptor = fipsSymmetricFactory.createOutputDecryptor((SymmetricSecretKey) key.toBouncyCastleKey(), param);
					} else if (type.getBlockMode().equalsIgnoreCase("CTR") && type.getPadding().equalsIgnoreCase("NOPADDING")) {
						Serpent.OperatorFactory fipsSymmetricFactory = new Serpent.OperatorFactory();
						Serpent.Parameters param = Serpent.CTR;
						if (iv != null)
							param = param.withIV(iv);
						descriptor = fipsSymmetricFactory.createOutputDecryptor((SymmetricSecretKey) key.toBouncyCastleKey(), param);
					} else if (type.getBlockMode().equalsIgnoreCase("GCM") && type.getPadding().equalsIgnoreCase("NOPADDING")) {
						Serpent.AEADOperatorFactory fipsSymmetricFactory = new Serpent.AEADOperatorFactory();
						Serpent.AuthParameters param = Serpent.GCM;

						if (iv != null)
							param = param.withIV(iv).withMACSize(128);
						else
							param = param.withMACSize(128);

						descriptor = readDescriptor = fipsSymmetricFactory.createOutputAEADDecryptor((SymmetricSecretKey) key.toBouncyCastleKey(), param);
					} else if (type.getBlockMode().equalsIgnoreCase("EAX") && type.getPadding().equalsIgnoreCase("NOPADDING")) {
						Serpent.AEADOperatorFactory fipsSymmetricFactory = new Serpent.AEADOperatorFactory();
						Serpent.AuthParameters param = Serpent.EAX;

						if (iv != null)
							param = param.withIV(iv).withMACSize(128);
						else
							param = param.withMACSize(128);

						descriptor = readDescriptor = fipsSymmetricFactory.createOutputAEADDecryptor((SymmetricSecretKey) key.toBouncyCastleKey(), param);
					} else {
						throw new IllegalAccessError();
					}
				} else if (type.getAlgorithmName().equals(SymmetricEncryptionType.BC_TWOFISH_CBC_PKCS7Padding.getAlgorithmName())) {
					if (type.getBlockMode().equalsIgnoreCase("CBC") && type.getPadding().equalsIgnoreCase("PKCS7PADDING")) {
						Twofish.OperatorFactory fipsSymmetricFactory = new Twofish.OperatorFactory();
						Twofish.Parameters param = Twofish.CBCwithPKCS7;
						if (iv != null)
							param = param.withIV(iv);
						descriptor = fipsSymmetricFactory.createOutputDecryptor((SymmetricSecretKey) key.toBouncyCastleKey(), param);
					} else if (type.getBlockMode().equalsIgnoreCase("CTR") && type.getPadding().equalsIgnoreCase("NOPADDING")) {
						Twofish.OperatorFactory fipsSymmetricFactory = new Twofish.OperatorFactory();
						Twofish.Parameters param = Twofish.CTR;
						if (iv != null)
							param = param.withIV(iv);
						descriptor = fipsSymmetricFactory.createOutputDecryptor((SymmetricSecretKey) key.toBouncyCastleKey(), param);
					} else if (type.getBlockMode().equalsIgnoreCase("GCM") && type.getPadding().equalsIgnoreCase("NOPADDING")) {
						Twofish.AEADOperatorFactory fipsSymmetricFactory = new Twofish.AEADOperatorFactory();
						Twofish.AuthParameters param = Twofish.GCM;

						if (iv != null)
							param = param.withIV(iv).withMACSize(128);
						else
							param = param.withMACSize(128);

						descriptor = readDescriptor = fipsSymmetricFactory.createOutputAEADDecryptor((SymmetricSecretKey) key.toBouncyCastleKey(), param);
					} else if (type.getBlockMode().equalsIgnoreCase("EAX") && type.getPadding().equalsIgnoreCase("NOPADDING")) {
						Twofish.AEADOperatorFactory fipsSymmetricFactory = new Twofish.AEADOperatorFactory();
						Twofish.AuthParameters param = Twofish.EAX;

						if (iv != null)
							param = param.withIV(iv).withMACSize(128);
						else
							param = param.withMACSize(128);

						descriptor = readDescriptor = fipsSymmetricFactory.createOutputAEADDecryptor((SymmetricSecretKey) key.toBouncyCastleKey(), param);
					} else {
						throw new IllegalAccessError();
					}
				} else if (type.getAlgorithmName().equals(SymmetricEncryptionType.BC_CHACHA20_NO_RANDOM_ACCESS.getAlgorithmName())) {
					ChaCha20.OperatorFactory factory = new ChaCha20.OperatorFactory();
					ChaCha20.Parameters param = ChaCha20.STREAM;
					if (iv != null)
						param = param.withIV(iv);
					descriptor = factory.createOutputDecryptor((SymmetricSecretKey) key.toBouncyCastleKey(), param);
				} else
					throw new IllegalAccessError();
				cipher = descriptor;
				processingStream = descriptor.getDecryptingStream(resultStream);
				if (readDescriptor != null)
					this.aadStream = readDescriptor.getAADStream();
			} else if (opMode == Cipher.WRAP_MODE) {

				if (keyWrapperType.equals(SymmetricKeyWrapperType.BC_FIPS_AES)) {
					FipsAES.KeyWrapOperatorFactory factory = new FipsAES.KeyWrapOperatorFactory();
					wrapper = factory.createKeyWrapper((SymmetricSecretKey) key.toBouncyCastleKey(), FipsAES.KW);

				} else if (keyWrapperType.equals(SymmetricKeyWrapperType.BC_FIPS_AES_WITH_PADDING)) {
					FipsAES.KeyWrapOperatorFactory factory = new FipsAES.KeyWrapOperatorFactory();
					wrapper = factory.createKeyWrapper((SymmetricSecretKey) key.toBouncyCastleKey(), FipsAES.KWP);
				} else
					throw new IllegalAccessError();

			} else if (opMode == Cipher.UNWRAP_MODE) {
				if (keyWrapperType.equals(SymmetricKeyWrapperType.BC_FIPS_AES)) {
					FipsAES.KeyWrapOperatorFactory factory = new FipsAES.KeyWrapOperatorFactory();
					unWrapper = factory.createKeyUnwrapper((SymmetricSecretKey) key.toBouncyCastleKey(), FipsAES.KW);

				} else if (keyWrapperType.equals(SymmetricKeyWrapperType.BC_FIPS_AES_WITH_PADDING)) {
					FipsAES.KeyWrapOperatorFactory factory = new FipsAES.KeyWrapOperatorFactory();
					unWrapper = factory.createKeyUnwrapper((SymmetricSecretKey) key.toBouncyCastleKey(), FipsAES.KWP);
				} else
					throw new IllegalAccessError();

			} else
				throw new IllegalAccessError();
		} catch (NoSuchAlgorithmException e) {
			throw new MessageExternalizationException(Integrity.FAIL, e);
		} catch (InvalidKeySpecException e) {
			throw new MessageExternalizationException(Integrity.FAIL_AND_CANDIDATE_TO_BAN, e);
		}

	}




	@Override
	public byte[] update(byte[] input, int inputOffset, int inputLength)  {
		
		processingStream.update(input, inputOffset, inputLength);
		if (resultStream.size() > 0)
        {
            byte[] result = resultStream.toByteArray();

            resultStream.reset();

            return result;
        }

        return null;
	}

	public byte[] wrap(AbstractKey key) throws PlainInputProcessingException {
		WrappedData b = key.getKeyBytes();
		return wrapper.wrap(b.getBytes(), 0, b.getBytes().length);
	}
	
	public final byte[] unwrap(byte[] wrappedKey)
			throws InvalidWrappingException {
		return unWrapper.unwrap(wrappedKey, 0, wrappedKey.length);
	}

	@Override
	public int update(byte[] input, int inputOffset, int inputLength, byte[] output, int outputOffset) throws IOException
			 {
		if (outputOffset + cipher.getUpdateOutputSize(inputLength) > output.length)
        {
            throw new IOException(new ShortBufferException("Output buffer too short for input."));
        }

        byte[] result = update(input, inputOffset, inputLength);

        if (result != null)
        {
            System.arraycopy(result, 0, output, outputOffset, result.length);

            return result.length;
        }

        return 0;
	}

	/**
	 * A filtered output stream that transforms data written to it with a
	 * {@link Cipher} before sending it to the underlying output stream.
	 *
	 * @author Casey Marshall (csm@gnu.org)
	 */
	@SuppressWarnings("NullableProblems")
	private class BCCipherOutputStream extends FilterOutputStream {



		/**
		 * Create a new cipher output stream. The cipher argument must have already been
		 * initialized.
		 *
		 * @param out
		 *            The sink for transformed data.
		 */
		private BCCipherOutputStream(OutputStream out) {
			super(out);
		}

		/**
		 * Close this output stream, and the sink output stream.
		 * <p>
		 * This method will first invoke the {@link Cipher#doFinal()} method of the
		 * underlying {@link Cipher}, and writes the output of that method to the sink
		 * output stream.
		 *
		 * @throws IOException
		 *             If an I/O error occurs, or if an error is caused by finalizing
		 *             the transformation.
		 */
		@Override
		public void close() throws IOException {
			try {
				out.write(doFinal());
				out.flush();
				out.close();
			} catch (Exception cause) {
				throw new IOException(String.valueOf(cause));
			}
		}

		/**
		 * Flush any pending output.
		 *
		 * @throws IOException
		 *             If an I/O error occurs.
		 */
		@Override
		public void flush() throws IOException {
			out.flush();
		}

		/**
		 * Write a byte array to the output stream.
		 *
		 * @param buf
		 *            The next bytes.
		 * @throws IOException
		 *             If an I/O error occurs, or if the underlying cipher is not in the
		 *             correct state to transform data.
		 */
		@Override
		public void write(byte[] buf) throws IOException {
			write(buf, 0, buf.length);
		}

		/**
		 * Write a portion of a byte array to the output stream.
		 *
		 * @param buf
		 *            The next bytes.
		 * @param off
		 *            The offset in the byte array to start.
		 * @param len
		 *            The number of bytes to write.
		 * @throws IOException
		 *             If an I/O error occurs, or if the underlying cipher is not in the
		 *             correct state to transform data.
		 */
		@Override
		public void write(byte[] buf, int off, int len) throws IOException {
			byte[] b = update(buf, off, len);
			if (b != null)
				out.write(b);
		}

		/**
		 * Write a single byte to the output stream.
		 *
		 * @param b
		 *            The next byte.
		 * @throws IOException
		 *             If an I/O error occurs, or if the underlying cipher is not in the
		 *             correct state to transform data.
		 */
		@Override
		public void write(int b) throws IOException {
			write(new byte[] { (byte) b }, 0, 1);
		}
	}
	/**
	 * This is an {@link java.io.InputStream} that filters its data through a
	 * {@link Cipher} before returning it. The <code>Cipher</code> argument must
	 * have been initialized before it is passed to the constructor.
	 *
	 * @author Casey Marshall (csm@gnu.org)
	 */
	@SuppressWarnings("NullableProblems")
	public class BCCipherInputStream extends FilterInputStream {

		// Constants and variables.
		// ------------------------------------------------------------------------



		/**
		 * Data that has been transformed but not read.
		 */
		private byte[] outBuffer;

		/**
		 * The offset into {@link #outBuffer} where valid data starts.
		 */
		private int outOffset;

		/**
		 * We set this when the cipher block size is 1, meaning that we can transform
		 * any amount of data.
		 */
		private final boolean isStream;

		/**
		 * Whether or not we've reached the end of the stream.
		 */
		private boolean eof;

		// Constructors.
		// ------------------------------------------------------------------------


		/**
		 * Creates a new input stream with a source input stream and cipher.
		 *
		 * @param in
		 *            The underlying input stream.

		 */
		public BCCipherInputStream(InputStream in) {
			super(in);
			isStream = getBlockSize() == 1;
			eof = false;
		}

		// Instance methods overriding java.io.FilterInputStream.
		// ------------------------------------------------------------------------

		/**
		 * Returns the number of bytes available without blocking. The value returned is
		 * the number of bytes that have been processed by the cipher, and which are
		 * currently buffered by this class.
		 *
		 * @return The number of bytes immediately available.
		 * @throws java.io.IOException
		 *             If an I/O exception occurs.
		 */
		@Override
		public int available() throws IOException {
			if (isStream)
				return super.available();
			if (outBuffer == null || outOffset >= outBuffer.length)
				nextBlock();
			return outBuffer.length - outOffset;
		}



		/**
		 * Set the mark. This method is unsupported and is empty.
		 *
		 * @param mark
		 *            Is ignored.
		 */
		@Override
		public void mark(int mark) {
		}

		/**
		 * Returns whether or not this input stream supports the long and
		 * {@link #reset()} methods; this input stream does not, however, and invariably
		 * returns <code>false</code>.
		 *
		 * @return <code>false</code>
		 */
		@Override
		public boolean markSupported() {
			return false;
		}

		private void nextBlock() throws IOException {
			byte[] buf = new byte[getBlockSize()];

			outBuffer = null;
			outOffset = 0;
			while (outBuffer == null) {
				int l = in.read(buf);
				if (l == -1) {
					outBuffer = doFinal();
					eof = true;
					return;
				}

				outOffset = 0;
				outBuffer = update(buf, 0, l);
			}
		}

		/**
		 * Read a single byte from this input stream; returns -1 on the end-of-file.
		 *
		 * @return The byte read, or -1 if there are no more bytes.
		 * @throws IOException
		 *             If an I/O exception occurs.
		 */
		@Override
		public synchronized int read() throws IOException {
			if (isStream) {
				byte[] buf = new byte[1];
				int in = super.read();
				if (in == -1)
					return -1;
				buf[0] = (byte) in;
				update(buf, 0, 1, buf, 0);
				return buf[0] & 0xFF;
			}

			if (outBuffer == null || outOffset == outBuffer.length) {
				if (eof)
					return -1;
				nextBlock();
			}
			return outBuffer[outOffset++] & 0xFF;
		}

		/**
		 * Read bytes into an array, returning the number of bytes read or -1 on the
		 * end-of-file.
		 *
		 * @param buf
		 *            The byte arry to read into.
		 * @return The number of bytes read, or -1 on the end-of-file.
		 * @throws java.io.IOException
		 *             If an I/O exception occurs.
		 */
		@Override
		public int read(byte[] buf) throws IOException {
			return read(buf, 0, buf.length);
		}

		/**
		 * Read bytes into an array, returning the number of bytes read or -1 on the
		 * end-of-file.
		 *
		 * @param buf
		 *            The byte array to read into.
		 * @param off
		 *            The offset in <code>buf</code> to start.
		 * @param len
		 *            The maximum number of bytes to read.
		 * @return The number of bytes read, or -1 on the end-of-file.
		 * @throws java.io.IOException
		 *             If an I/O exception occurs.
		 */
		@Override
		public synchronized int read(byte[] buf, int off, int len) throws IOException {
			// CipherInputStream has this wierd implementation where if
			// the buffer is null, this call is the same as `skip'.
			if (buf == null)
				return (int) skip(len);

			if (isStream) {
				len = super.read(buf, off, len);
				if (len > 0) {
					update(buf, off, len, buf, off);
				}
				return len;
			}

			int count = 0;
			while (count < len) {
				if (outBuffer == null || outOffset >= outBuffer.length) {
					if (eof) {
						if (count == 0)
							count = -1;
						break;
					}
					nextBlock();
				}
				int l = Math.min(outBuffer.length - outOffset, len - count);
				System.arraycopy(outBuffer, outOffset, buf, count + off, l);
				count += l;
				outOffset += l;
			}
			return count;
		}

		/**
		 * Reset to the mark. This method is unsupported and is empty.
		 */
		@Override
		public void reset() throws IOException {
			throw new IOException("reset not supported");
		}

		// Own methods.
		// -------------------------------------------------------------------------

		// FIXME: I don't fully understand how this class is supposed to work.

		/**
		 * Skip a number of bytes. This class only supports skipping as many bytes as
		 * are returned by {@link #available()}, which is the number of transformed
		 * bytes currently in this class's internal buffer.
		 *
		 * @param bytes
		 *            The number of bytes to skip.
		 * @return The number of bytes skipped.
		 */
		@Override
		public long skip(long bytes) throws IOException {
			if (isStream) {
				return super.skip(bytes);
			}
			long ret = 0;
			if (bytes > 0 && outBuffer != null && outOffset >= outBuffer.length) {
				ret = outBuffer.length - outOffset;
				outOffset = outBuffer.length;
			}
			return ret;
		}
	}
	@Override
	public void updateAAD(byte[] ad, int offset, int size) {
		if (aadStream!=null)
			aadStream.update(ad, offset, size);
		else
			throw new IllegalStateException();
	}

}
