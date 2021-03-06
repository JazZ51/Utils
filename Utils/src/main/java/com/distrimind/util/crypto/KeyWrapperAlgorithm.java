package com.distrimind.util.crypto;

import com.distrimind.util.Bits;
import com.distrimind.util.DecentralizedValue;
import com.distrimind.util.data_buffers.WrappedData;
import com.distrimind.util.data_buffers.WrappedSecretData;
import com.distrimind.util.io.*;
import com.distrimind.util.properties.MultiFormatProperties;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.Arrays;

/**
 * @author Jason Mahdjoub
 * @version 2.1
 * @since Utils 5.10.0
 */
public class KeyWrapperAlgorithm extends MultiFormatProperties implements SecureExternalizable, Zeroizable{
	public static final int MAX_SIZE_OF_WRAPPED_SYMMETRIC_SECRET_KEY_WITH_SYMMETRIC_ALGORITHM=SymmetricSecretKey.MAX_SIZE_IN_BYTES_OF_SYMMETRIC_KEY_FOR_SIGNATURE +16;
	public static final int MAX_SIZE_OF_WRAPPED_ASYMMETRIC_PRIVATE_KEY_WITH_SYMMETRIC_ALGORITHM=IASymmetricPrivateKey.MAX_SIZE_IN_BYTES_OF_PRIVATE_KEY+16;

	private SymmetricKeyWrapperType symmetricKeyWrapperType;
	private ASymmetricKeyWrapperType aSymmetricKeyWrapperType;
	private IASymmetricPrivateKey privateKeyForEncryption, privateKeyForSignature;
	private IASymmetricPublicKey publicKeyForEncryption, publicKeyForSignature;
	private SymmetricSecretKey secretKeyForSignature;
	private SymmetricSecretKey secretKeyForEncryption;

	private final static byte ENCRYPTION_WITH_SYMMETRIC_SECRET_KEY=2;
	private final static byte ENCRYPTION_WITH_ASYMMETRIC_KEY_PAIR=4;
	private final static byte SIGNATURE_WITH_ASYMMETRIC_KEY_PAIR=8;
	private final static byte SIGNATURE_WITH_SYMMETRIC_SECRET_KEY=16;
	private byte mode;

	@Override
	public void zeroize() {
		if (publicKeyForEncryption!=null)
		{
			publicKeyForEncryption.zeroize();
			publicKeyForEncryption=null;
		}
		if (privateKeyForEncryption!=null)
		{
			privateKeyForEncryption.zeroize();
			privateKeyForEncryption=null;
		}
		if (publicKeyForSignature!=null)
		{
			publicKeyForSignature.zeroize();
			publicKeyForSignature=null;
		}
		if (privateKeyForSignature!=null)
		{
			privateKeyForSignature.zeroize();
			privateKeyForSignature=null;
		}
		if (secretKeyForEncryption !=null)
		{
			secretKeyForEncryption.zeroize();
			secretKeyForEncryption =null;
		}
		if (secretKeyForSignature!=null)
		{
			secretKeyForSignature.zeroize();
			secretKeyForSignature=null;
		}
	}

	@SuppressWarnings("unused")
	private KeyWrapperAlgorithm()
	{
		super(null);
		symmetricKeyWrapperType=null;
		aSymmetricKeyWrapperType=null;
		secretKeyForEncryption =null;
		privateKeyForEncryption=null;
		privateKeyForSignature =null;
		publicKeyForEncryption=null;
		publicKeyForSignature=null;
		mode=0;
	}
	public KeyWrapperAlgorithm(SymmetricKeyWrapperType symmetricKeyWrapperType, SymmetricSecretKey secretKeyForEncryption) {
		this(symmetricKeyWrapperType, secretKeyForEncryption, null, null, null, false, false);
	}
	public KeyWrapperAlgorithm(SymmetricKeyWrapperType symmetricKeyWrapperType, SymmetricSecretKey secretKeyForEncryption, SymmetricSecretKey secretKeyForSignature) {
		this(symmetricKeyWrapperType, secretKeyForEncryption, secretKeyForSignature, null, null, false, true);
	}
	public KeyWrapperAlgorithm(SymmetricKeyWrapperType symmetricKeyWrapperType, SymmetricSecretKey secretKeyForEncryption, IASymmetricPublicKey publicKeyForSignature) {
		this(symmetricKeyWrapperType, secretKeyForEncryption, null, publicKeyForSignature, null, true, false);
	}
	public KeyWrapperAlgorithm(SymmetricKeyWrapperType symmetricKeyWrapperType, SymmetricSecretKey secretKeyForEncryption, AbstractKeyPair<?, ?> keyPairForSignature) {
		this(symmetricKeyWrapperType, secretKeyForEncryption, null, keyPairForSignature.getASymmetricPublicKey(), keyPairForSignature.getASymmetricPrivateKey(), true, false);
	}
	public KeyWrapperAlgorithm(SymmetricKeyWrapperType symmetricKeyWrapperType, SymmetricSecretKey secretKeyForEncryption, SymmetricSecretKey secretKeyForSignature, IASymmetricPublicKey publicKeyForSignature) {
		this(symmetricKeyWrapperType, secretKeyForEncryption, secretKeyForSignature, publicKeyForSignature, null, true, true);
	}

	public KeyWrapperAlgorithm(SymmetricKeyWrapperType symmetricKeyWrapperType, SymmetricSecretKey secretKeyForEncryption, SymmetricSecretKey secretKeyForSignature, AbstractKeyPair<?, ?> keyPairForSignature) {
		this(symmetricKeyWrapperType, secretKeyForEncryption, secretKeyForSignature, keyPairForSignature.getASymmetricPublicKey(), keyPairForSignature.getASymmetricPrivateKey(), true, true);
	}
	private KeyWrapperAlgorithm(SymmetricKeyWrapperType symmetricKeyWrapperType, SymmetricSecretKey secretKeyForEncryption, SymmetricSecretKey secretKeyForSignature, IASymmetricPublicKey publicKeyForSignature, IASymmetricPrivateKey privateKeyForSignature, boolean includeASymmetricSignature, boolean includeSecretKeyForSignature) {
		this();
		if (symmetricKeyWrapperType==null)
			throw new NullPointerException();
		if (secretKeyForEncryption ==null)
			throw new NullPointerException();
		if (secretKeyForEncryption.getEncryptionAlgorithmType()!=symmetricKeyWrapperType.getSymmetricEncryptionType())
			throw new IllegalArgumentException();
		if (publicKeyForSignature ==null && privateKeyForSignature==null && includeASymmetricSignature)
			throw new NullPointerException();
		if (includeSecretKeyForSignature && secretKeyForSignature==null)
			throw new NullPointerException();

		this.symmetricKeyWrapperType = symmetricKeyWrapperType;
		this.secretKeyForEncryption = secretKeyForEncryption;
		this.secretKeyForSignature=secretKeyForSignature;
		this.mode=ENCRYPTION_WITH_SYMMETRIC_SECRET_KEY;
		this.privateKeyForSignature = privateKeyForSignature;
		this.publicKeyForSignature = publicKeyForSignature;
		if (includeASymmetricSignature)
			this.mode+=SIGNATURE_WITH_ASYMMETRIC_KEY_PAIR;
		if (includeSecretKeyForSignature)
			this.mode+=SIGNATURE_WITH_SYMMETRIC_SECRET_KEY;
		if (symmetricKeyWrapperType.getAlgorithmName()==null && (secretKeyForEncryption.getEncryptionAlgorithmType()==null ||
				(!secretKeyForEncryption.getEncryptionAlgorithmType().isAuthenticatedAlgorithm()
						&& !useSignature())))
			throw new IllegalArgumentException("This key wrapping type and this secret key for encryption must be used with a signature algorithm");
	}
	public KeyWrapperAlgorithm(ASymmetricKeyWrapperType aSymmetricKeyWrapperType, IASymmetricPublicKey publicKeyForEncryption) {
		this(aSymmetricKeyWrapperType, publicKeyForEncryption, null, null, null, null, false, false);
	}
	public KeyWrapperAlgorithm(ASymmetricKeyWrapperType aSymmetricKeyWrapperType, IASymmetricPublicKey publicKeyForEncryption, SymmetricSecretKey secretKeyForSignature) {
		this(aSymmetricKeyWrapperType, publicKeyForEncryption, null, null, null, secretKeyForSignature, false, true);
	}
	public KeyWrapperAlgorithm(ASymmetricKeyWrapperType aSymmetricKeyWrapperType, IASymmetricPrivateKey privateKeyForDecryption) {
		this(aSymmetricKeyWrapperType, null, privateKeyForDecryption, null, null, null, false, false);
	}
	public KeyWrapperAlgorithm(ASymmetricKeyWrapperType aSymmetricKeyWrapperType, IASymmetricPrivateKey privateKeyForDecryption, SymmetricSecretKey secretKeyForSignature) {
		this(aSymmetricKeyWrapperType, null, privateKeyForDecryption, null, null, secretKeyForSignature, false, true);
	}
	public KeyWrapperAlgorithm(ASymmetricKeyWrapperType aSymmetricKeyWrapperType, AbstractKeyPair<?, ?> keyPairForEncryption) {
		this(aSymmetricKeyWrapperType, keyPairForEncryption, null, null, false, false);
	}
	public KeyWrapperAlgorithm(ASymmetricKeyWrapperType aSymmetricKeyWrapperType, AbstractKeyPair<?, ?> keyPairForEncryption, SymmetricSecretKey secretKeyForSignature) {
		this(aSymmetricKeyWrapperType, keyPairForEncryption, null, secretKeyForSignature, false, true);
	}
	public KeyWrapperAlgorithm(ASymmetricKeyWrapperType aSymmetricKeyWrapperType, IASymmetricPublicKey publicKeyForEncryption, IASymmetricPrivateKey privateKeyForSignature) {
		this(aSymmetricKeyWrapperType, publicKeyForEncryption, null, null, privateKeyForSignature,null, true, false);
	}
	public KeyWrapperAlgorithm(ASymmetricKeyWrapperType aSymmetricKeyWrapperType, IASymmetricPublicKey publicKeyForEncryption, IASymmetricPrivateKey privateKeyForSignature, SymmetricSecretKey secretKeyForSignature) {
		this(aSymmetricKeyWrapperType, publicKeyForEncryption, null, null, privateKeyForSignature,secretKeyForSignature, true, true);
	}
	public KeyWrapperAlgorithm(ASymmetricKeyWrapperType aSymmetricKeyWrapperType, IASymmetricPrivateKey privateKeyForEncryption, IASymmetricPublicKey publicKeyForSignature) {
		this(aSymmetricKeyWrapperType, null, privateKeyForEncryption, publicKeyForSignature, null, null, true, false);
	}
	public KeyWrapperAlgorithm(ASymmetricKeyWrapperType aSymmetricKeyWrapperType, IASymmetricPrivateKey privateKeyForEncryption, IASymmetricPublicKey publicKeyForSignature, SymmetricSecretKey secretKeyForSignature) {
		this(aSymmetricKeyWrapperType, null, privateKeyForEncryption, publicKeyForSignature, null, secretKeyForSignature, true, true);
	}
	public KeyWrapperAlgorithm(ASymmetricKeyWrapperType aSymmetricKeyWrapperType, AbstractKeyPair<?, ?> keyPairForEncryption, AbstractKeyPair<?, ?> keyPairForSignature) {
		this(aSymmetricKeyWrapperType, keyPairForEncryption, keyPairForSignature, null, true, false);
	}
	public KeyWrapperAlgorithm(ASymmetricKeyWrapperType aSymmetricKeyWrapperType, AbstractKeyPair<?, ?> keyPairForEncryption, AbstractKeyPair<?, ?> keyPairForSignature, SymmetricSecretKey secretKeyForSignature) {
		this(aSymmetricKeyWrapperType, keyPairForEncryption, keyPairForSignature, secretKeyForSignature, true, true);
	}
	private KeyWrapperAlgorithm(ASymmetricKeyWrapperType aSymmetricKeyWrapperType, AbstractKeyPair<?, ?> keyPairForEncryption, AbstractKeyPair<?, ?> keyPairForSignature, SymmetricSecretKey secretKeyForSignature, boolean includeASymmetricSignature, boolean includeSecretKeyForSignature) {
		this(aSymmetricKeyWrapperType, keyPairForEncryption.getASymmetricPublicKey(), keyPairForEncryption.getASymmetricPrivateKey(), keyPairForSignature==null?null:keyPairForSignature.getASymmetricPublicKey(), keyPairForSignature==null?null:keyPairForSignature.getASymmetricPrivateKey(), secretKeyForSignature, includeASymmetricSignature, includeSecretKeyForSignature);
	}

	private KeyWrapperAlgorithm(ASymmetricKeyWrapperType aSymmetricKeyWrapperType, IASymmetricPublicKey publicKeyForEncryption, IASymmetricPrivateKey privateKeyForEncryption, IASymmetricPublicKey publicKeyForSignature, IASymmetricPrivateKey privateKeyForSignature, SymmetricSecretKey secretKeyForSignature, boolean includeASymmetricSignature, boolean includeSecretKeyForSignature) {
		super(null);
		if (aSymmetricKeyWrapperType==null)
			throw new NullPointerException();
		if (privateKeyForEncryption ==null && publicKeyForEncryption==null)
			throw new NullPointerException();
		if (publicKeyForSignature ==null && privateKeyForSignature==null && includeASymmetricSignature)
			throw new NullPointerException();
		if (includeASymmetricSignature && ((privateKeyForEncryption==null)!=(publicKeyForSignature==null) || (publicKeyForEncryption==null)!=(privateKeyForSignature==null)))
			throw new NullPointerException();
		if (includeSecretKeyForSignature && secretKeyForSignature==null)
			throw new NullPointerException();

		assert includeSecretKeyForSignature || secretKeyForSignature==null;
		assert includeASymmetricSignature || (privateKeyForSignature==null && publicKeyForSignature==null);
		this.aSymmetricKeyWrapperType = aSymmetricKeyWrapperType;
		this.privateKeyForEncryption=privateKeyForEncryption;
		this.publicKeyForEncryption=publicKeyForEncryption;
		this.privateKeyForSignature = privateKeyForSignature;
		this.publicKeyForSignature = publicKeyForSignature;
		this.symmetricKeyWrapperType = null;
		this.secretKeyForEncryption = null;
		this.secretKeyForSignature=secretKeyForSignature;
		this.mode=ENCRYPTION_WITH_ASYMMETRIC_KEY_PAIR;
		if (includeASymmetricSignature)
			this.mode+=SIGNATURE_WITH_ASYMMETRIC_KEY_PAIR;
		if (includeSecretKeyForSignature)
			this.mode+=SIGNATURE_WITH_SYMMETRIC_SECRET_KEY;
		if (!aSymmetricKeyWrapperType.wrappingIncludeSignature()
				&& !useSignature())
			throw new IllegalArgumentException("This key wrapping type and this public key for encryption must be used with a signature algorithm");
	}

	public KeyWrapperAlgorithm(SymmetricKeyWrapperType symmetricKeyWrapperType, PasswordHashType passwordHashType, WrappedPassword password) throws NoSuchAlgorithmException, NoSuchProviderException, IOException {
		this(symmetricKeyWrapperType, SymmetricKeyWrapperType.hashPasswordForSecretKeyEncryption(symmetricKeyWrapperType.getSymmetricEncryptionType(), passwordHashType, password));
	}
	public WrappedEncryptedSymmetricSecretKeyString wrapString(AbstractSecureRandom random, SymmetricSecretKey secretKeyToWrap) throws IOException {
		return new WrappedEncryptedSymmetricSecretKeyString(wrap(random, secretKeyToWrap));
	}
	private WrappedEncryptedSymmetricSecretKey signSymmetricSecretKey(byte[] encoded) throws IOException {
		return new WrappedEncryptedSymmetricSecretKey(sign(encoded));
	}
	private WrappedEncryptedSymmetricSecretKey signSymmetricSecretKey(WrappedData encoded) throws IOException {
		byte[] res=sign(encoded.getBytes());
		if (res==encoded.getBytes() && encoded instanceof WrappedSecretData)
			return new WrappedEncryptedSymmetricSecretKey(res.clone());
		else
			return new WrappedEncryptedSymmetricSecretKey(res);
	}
	private WrappedEncryptedASymmetricPrivateKey signASymmetricPrivateKey(byte[] encoded) throws IOException {
		return new WrappedEncryptedASymmetricPrivateKey(sign(encoded));
	}

	private byte[] sign(byte[] encoded) throws IOException {
		if (((this.mode & SIGNATURE_WITH_ASYMMETRIC_KEY_PAIR)==SIGNATURE_WITH_ASYMMETRIC_KEY_PAIR) && privateKeyForSignature==null)
			throw new IOException("Private key for signature has not be given");
		if (((this.mode & SIGNATURE_WITH_SYMMETRIC_SECRET_KEY)==SIGNATURE_WITH_SYMMETRIC_SECRET_KEY) && secretKeyForSignature==null)
			throw new IOException("Secret key for signature has not be given");
		try {
			SymmetricAuthenticatedSignerAlgorithm symSigner = null;
			if (secretKeyForSignature != null) {
				symSigner = new SymmetricAuthenticatedSignerAlgorithm(secretKeyForSignature);
				symSigner.init();
			}
			int symSignSize = symSigner == null ? 0 : symSigner.getMacLengthBytes();
			if (privateKeyForSignature != null) {

				ASymmetricAuthenticatedSignerAlgorithm signer = new ASymmetricAuthenticatedSignerAlgorithm(privateKeyForSignature);
				signer.init();
				byte[] signature = signer.sign(encoded);
				byte[] res = new byte[2 + encoded.length + signature.length+symSignSize];
				Bits.putUnsignedInt16Bits(res, symSignSize, signature.length);
				System.arraycopy(signature, 0, res, symSignSize+2, signature.length);
				System.arraycopy(encoded, 0, res, symSignSize+2 + signature.length, encoded.length);
				Arrays.fill(encoded, (byte) 0);
				encoded=res;
			}
			if (symSigner!=null)
			{
				int s;
				if (privateKeyForSignature==null) {
					s=encoded.length;
					byte[] res=new byte[s+symSignSize];
					System.arraycopy(encoded, 0, res, symSignSize, s);
					Arrays.fill(encoded, (byte)0);
					encoded=res;
				}
				else
					s = encoded.length - symSignSize;
				symSigner.sign(encoded, symSignSize, s, encoded, 0, symSignSize);
			}
			return encoded;
		} catch (NoSuchProviderException | NoSuchAlgorithmException e) {
			throw new IOException(e);
		}
	}
	private int checkSignature(byte[] encoded) throws IOException {
		if (((this.mode & SIGNATURE_WITH_ASYMMETRIC_KEY_PAIR)==SIGNATURE_WITH_ASYMMETRIC_KEY_PAIR) && publicKeyForSignature==null)
			throw new IOException("Public key used to check signature is lacking");
		if (((this.mode & SIGNATURE_WITH_SYMMETRIC_SECRET_KEY)==SIGNATURE_WITH_SYMMETRIC_SECRET_KEY) && secretKeyForSignature==null)
			throw new IOException("Secret key for signature has not be given");
		try {
			SymmetricAuthenticatedSignatureCheckerAlgorithm symChecker = null;
			if (secretKeyForSignature != null) {
				symChecker = new SymmetricAuthenticatedSignatureCheckerAlgorithm(secretKeyForSignature);
			}
			int symSignSize = symChecker == null ? 0 : symChecker.getMacLengthBytes();
			int offM=0;
			if (symChecker!=null)
			{
				if (!symChecker.verify(encoded, symSignSize, encoded.length-symSignSize, encoded, 0, symSignSize))
					throw new MessageExternalizationException(Integrity.FAIL_AND_CANDIDATE_TO_BAN);
				offM+=symSignSize;
			}
			if (publicKeyForSignature != null) {

				ASymmetricAuthenticatedSignatureCheckerAlgorithm checker = new ASymmetricAuthenticatedSignatureCheckerAlgorithm(publicKeyForSignature);
				int size = Bits.getUnsignedInt16Bits(encoded, offM);
				int aSigOff=offM+2;
				offM += size + 2;
				if (offM >= encoded.length)
					throw new MessageExternalizationException(Integrity.FAIL);
				if (!checker.verify(encoded, offM, encoded.length - offM, encoded, aSigOff, size))
					throw new MessageExternalizationException(Integrity.FAIL_AND_CANDIDATE_TO_BAN);
			}
			return offM;
		} catch (NoSuchProviderException | NoSuchAlgorithmException | MessageExternalizationException e) {
			throw new IOException(e);
		}
	}
	@SuppressWarnings("BooleanMethodIsAlwaysInverted")
	private boolean useSignature()
	{
		return ((this.mode & SIGNATURE_WITH_SYMMETRIC_SECRET_KEY)==SIGNATURE_WITH_SYMMETRIC_SECRET_KEY)
				|| ((this.mode & SIGNATURE_WITH_ASYMMETRIC_KEY_PAIR)==SIGNATURE_WITH_ASYMMETRIC_KEY_PAIR);
	}

	public WrappedEncryptedSymmetricSecretKey wrap(AbstractSecureRandom random, SymmetricSecretKey secretKeyToWrap) throws IOException {
		if (((this.mode & ENCRYPTION_WITH_ASYMMETRIC_KEY_PAIR)==ENCRYPTION_WITH_ASYMMETRIC_KEY_PAIR) && publicKeyForEncryption==null)
			throw new IOException("Public key used for encryption is not available");


		if (symmetricKeyWrapperType!=null)
		{
			if (symmetricKeyWrapperType.getAlgorithmName()==null) {
				SymmetricEncryptionAlgorithm cipher = new SymmetricEncryptionAlgorithm(random, secretKeyForEncryption);
				WrappedSecretData wsd=secretKeyToWrap.encode();
				return signSymmetricSecretKey(cipher.encode(wsd.getBytes()));
			}
			else {
				WrappedEncryptedSymmetricSecretKey w=symmetricKeyWrapperType.wrapKey(secretKeyForEncryption, secretKeyToWrap, random);
				return signSymmetricSecretKey(w);
			}
		}
		else
		{
			WrappedEncryptedSymmetricSecretKey w=aSymmetricKeyWrapperType.wrapKey(random, publicKeyForEncryption, secretKeyToWrap);
			return signSymmetricSecretKey(w);
		}
	}
	public SymmetricSecretKey unwrap(WrappedEncryptedSymmetricSecretKeyString encryptedSecretKey) throws IOException {
		return unwrap(new WrappedEncryptedSymmetricSecretKey(encryptedSecretKey));
	}
	public SymmetricSecretKey unwrap(WrappedEncryptedSymmetricSecretKey encryptedSecretKey) throws IOException {
		if (((this.mode & ENCRYPTION_WITH_ASYMMETRIC_KEY_PAIR)==ENCRYPTION_WITH_ASYMMETRIC_KEY_PAIR) && privateKeyForEncryption==null)
			throw new IOException("Private key used for encryption is not available");
		if (publicKeyForEncryption!=null && privateKeyForEncryption==null)
			throw new IOException("Private key used for decryption is lacking");
		if (symmetricKeyWrapperType!=null)
		{
			if (symmetricKeyWrapperType.getAlgorithmName()==null)
			{
				try {
					SymmetricEncryptionAlgorithm cipher = new SymmetricEncryptionAlgorithm(SecureRandomType.DEFAULT.getInstance(null), secretKeyForEncryption);
					int off=checkSignature(encryptedSecretKey.getBytes());
					AbstractKey ak=SymmetricSecretKey.decode(cipher.decode(encryptedSecretKey.getBytes(), off, encryptedSecretKey.getBytes().length-off));
					encryptedSecretKey.getBytes();//gc delayed
					if (ak instanceof SymmetricSecretKey)
						return (SymmetricSecretKey)ak;
					else
						throw new MessageExternalizationException(Integrity.FAIL_AND_CANDIDATE_TO_BAN);
				} catch (NoSuchAlgorithmException | NoSuchProviderException e) {
					throw new IOException(e);
				}
			}
			else {
				int off=checkSignature(encryptedSecretKey.getBytes());
				if (off>0)
					encryptedSecretKey=new WrappedEncryptedSymmetricSecretKey(Arrays.copyOfRange(encryptedSecretKey.getBytes(), off, encryptedSecretKey.getBytes().length));
				encryptedSecretKey.getBytes();//gc delayed
				return symmetricKeyWrapperType.unwrapKey(secretKeyForEncryption, encryptedSecretKey);
			}
		}
		else
		{
			int off=checkSignature(encryptedSecretKey.getBytes());
			if (off>0)
				encryptedSecretKey=new WrappedEncryptedSymmetricSecretKey(Arrays.copyOfRange(encryptedSecretKey.getBytes(), off, encryptedSecretKey.getBytes().length));
			return aSymmetricKeyWrapperType.unwrapKey(privateKeyForEncryption, encryptedSecretKey);
		}
	}
	public WrappedEncryptedASymmetricPrivateKeyString wrapString(AbstractSecureRandom random, IASymmetricPrivateKey privateKeyToWrap) throws IOException {
		return new WrappedEncryptedASymmetricPrivateKeyString(wrap(random, privateKeyToWrap));
	}
	public WrappedEncryptedASymmetricPrivateKey wrap(AbstractSecureRandom random, IASymmetricPrivateKey privateKeyToWrap) throws IOException {
		if (mode==ENCRYPTION_WITH_ASYMMETRIC_KEY_PAIR && publicKeyForEncryption==null)
			throw new IOException("Public key used for encryption is not available");
		WrappedSecretData wsd=privateKeyToWrap.encode();
		try {
			AbstractEncryptionOutputAlgorithm cipher;
			if (symmetricKeyWrapperType != null) {
				cipher=new SymmetricEncryptionAlgorithm(random, secretKeyForEncryption);
			} else {
				cipher = new ClientASymmetricEncryptionAlgorithm(random, publicKeyForEncryption);
			}
			return signASymmetricPrivateKey(cipher.encode(wsd.getBytes()));
		}
		finally {
			wsd.zeroize();
		}
	}
	public IASymmetricPrivateKey unwrap(WrappedEncryptedASymmetricPrivateKeyString privateKeyToUnwrap) throws IOException {
		return unwrap(new WrappedEncryptedASymmetricPrivateKey(privateKeyToUnwrap));
	}
	public IASymmetricPrivateKey unwrap(WrappedEncryptedASymmetricPrivateKey privateKeyToUnwrap) throws IOException {
		if (mode==ENCRYPTION_WITH_ASYMMETRIC_KEY_PAIR && privateKeyForEncryption==null)
			throw new IOException("Private key used for encryption is not available");
		if (publicKeyForEncryption!=null && privateKeyForEncryption==null)
			throw new IOException("Private key used for decryption is lacking");
		try {
			IEncryptionInputAlgorithm cipher;
			if (symmetricKeyWrapperType != null) {
				cipher=new SymmetricEncryptionAlgorithm(SecureRandomType.DEFAULT.getInstance(null), secretKeyForEncryption);
			} else {
				cipher = new ServerASymmetricEncryptionAlgorithm(privateKeyForEncryption);
			}
			int offM=checkSignature(privateKeyToUnwrap.getBytes());
			DecentralizedValue dv = DecentralizedValue.decode(cipher.decode(privateKeyToUnwrap.getBytes(), offM, privateKeyToUnwrap.getBytes().length - offM), true);
			privateKeyToUnwrap.getBytes();//gc delayed
			if (!(dv instanceof IASymmetricPrivateKey))
				throw new MessageExternalizationException(Integrity.FAIL_AND_CANDIDATE_TO_BAN);
			return (IASymmetricPrivateKey) dv;

		} catch (NoSuchAlgorithmException | NoSuchProviderException e) {
			throw new MessageExternalizationException(Integrity.FAIL);
		}

	}

	@Override
	public int getInternalSerializedSize() {
		if ((mode & ENCRYPTION_WITH_SYMMETRIC_SECRET_KEY)==ENCRYPTION_WITH_SYMMETRIC_SECRET_KEY)
		{
			return SerializationTools.getInternalSize(SymmetricKeyWrapperType.DEFAULT)
					+SerializationTools.getInternalSize(secretKeyForEncryption)
					+SerializationTools.getInternalSize(secretKeyForSignature);
		}
		else if ((mode & ENCRYPTION_WITH_ASYMMETRIC_KEY_PAIR)==ENCRYPTION_WITH_ASYMMETRIC_KEY_PAIR)
		{
			return SerializationTools.getInternalSize(ASymmetricKeyWrapperType.DEFAULT)
					+SerializationTools.getInternalSize(publicKeyForEncryption)
					+SerializationTools.getInternalSize(privateKeyForEncryption)
					+SerializationTools.getInternalSize(publicKeyForSignature)
					+SerializationTools.getInternalSize(privateKeyForSignature)
					+SerializationTools.getInternalSize(secretKeyForSignature);
		}
		throw new IllegalAccessError();
	}

	@Override
	public void writeExternal(SecuredObjectOutputStream out) throws IOException {
		out.writeByte(mode);
		if ((mode & SIGNATURE_WITH_SYMMETRIC_SECRET_KEY)==SIGNATURE_WITH_SYMMETRIC_SECRET_KEY) {
			out.writeObject(secretKeyForSignature, false);
		}
		if ((mode & SIGNATURE_WITH_ASYMMETRIC_KEY_PAIR)==SIGNATURE_WITH_ASYMMETRIC_KEY_PAIR) {
			out.writeObject(privateKeyForSignature, true);
			out.writeObject(publicKeyForSignature, privateKeyForSignature!=null);
		}

		if ((mode & ENCRYPTION_WITH_SYMMETRIC_SECRET_KEY)==ENCRYPTION_WITH_SYMMETRIC_SECRET_KEY) {
			out.writeEnum(symmetricKeyWrapperType, false);
			out.writeObject(secretKeyForEncryption, false);

		}
		else if ((mode & ENCRYPTION_WITH_ASYMMETRIC_KEY_PAIR)==ENCRYPTION_WITH_ASYMMETRIC_KEY_PAIR)
		{
			out.writeEnum(aSymmetricKeyWrapperType, false);
			out.writeObject(publicKeyForEncryption, false);
			out.writeObject(privateKeyForEncryption, false);
		}
		else throw new IOException();
	}

	@Override
	public void readExternal(SecuredObjectInputStream in) throws IOException, ClassNotFoundException {
		mode=in.readByte();
		if ((mode & SIGNATURE_WITH_SYMMETRIC_SECRET_KEY)==SIGNATURE_WITH_SYMMETRIC_SECRET_KEY) {
			secretKeyForSignature=in.readObject(false);
		}
		else
			secretKeyForSignature=null;
		if ((mode & SIGNATURE_WITH_SYMMETRIC_SECRET_KEY)==SIGNATURE_WITH_SYMMETRIC_SECRET_KEY) {
			privateKeyForSignature = in.readObject(true);
			publicKeyForSignature = in.readObject(privateKeyForSignature!=null);

		}
		else
		{
			publicKeyForSignature = null;
			privateKeyForSignature = null;
		}

		if ((mode & ENCRYPTION_WITH_SYMMETRIC_SECRET_KEY)==ENCRYPTION_WITH_SYMMETRIC_SECRET_KEY) {
			symmetricKeyWrapperType = in.readEnum(false, SymmetricKeyWrapperType.class);
			secretKeyForEncryption = in.readObject(false);
			aSymmetricKeyWrapperType = null;
			publicKeyForEncryption = null;
			privateKeyForEncryption = null;
		}
		else if ((mode & ENCRYPTION_WITH_ASYMMETRIC_KEY_PAIR)==ENCRYPTION_WITH_ASYMMETRIC_KEY_PAIR) {
			aSymmetricKeyWrapperType = in.readEnum(false, ASymmetricKeyWrapperType.class);
			publicKeyForEncryption = in.readObject(false);
			privateKeyForEncryption = in.readObject(false);
			if ((privateKeyForSignature != null || publicKeyForSignature != null) && ((privateKeyForEncryption == null) != (publicKeyForSignature == null) || (publicKeyForEncryption == null) != (privateKeyForSignature == null)))
				throw new MessageExternalizationException(Integrity.FAIL_AND_CANDIDATE_TO_BAN);
			secretKeyForEncryption = null;
			symmetricKeyWrapperType = null;
		}
		else
			throw new MessageExternalizationException(Integrity.FAIL);

	}
	public boolean isPostQuantumAlgorithm()
	{
		if (symmetricKeyWrapperType==null)
			return aSymmetricKeyWrapperType.isPostQuantumKeyAlgorithm()
					&& (publicKeyForSignature==null || publicKeyForSignature.isPostQuantumKey())
					&& (privateKeyForSignature==null || privateKeyForSignature.isPostQuantumKey())
					&& (publicKeyForEncryption==null || publicKeyForEncryption.isPostQuantumKey())
					&& (privateKeyForEncryption==null || privateKeyForEncryption.isPostQuantumKey())
					&& (secretKeyForSignature==null || secretKeyForSignature.isPostQuantumKey())
					;
		else {
			return symmetricKeyWrapperType.isPostQuantumAlgorithm(secretKeyForEncryption.getKeySizeBits()) && (secretKeyForSignature==null || secretKeyForSignature.isPostQuantumKey());
		}
	}
}
