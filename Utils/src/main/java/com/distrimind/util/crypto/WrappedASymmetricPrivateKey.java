package com.distrimind.util.crypto;

import com.distrimind.util.data_buffers.WrappedSecretData;
import com.distrimind.util.io.SecureExternalizable;
import com.distrimind.util.io.SecuredObjectInputStream;
import com.distrimind.util.io.SecuredObjectOutputStream;
import com.distrimind.util.io.SerializationTools;

import java.io.IOException;

/**
 * @author Jason Mahdjoub
 * @version 1.0
 * @since Utils 5.10.0
 */
public class WrappedASymmetricPrivateKey extends WrappedSecretData implements SecureExternalizable {
	static final int MAX_SIZE_IN_BYTES_OF_KEY=ASymmetricPrivateKey.MAX_SIZE_IN_BYTES_OF_PRIVATE_KEY;
	public static final int MAX_SIZE_IN_BYTES_OF_DATA=MAX_SIZE_IN_BYTES_OF_KEY+7;
	protected WrappedASymmetricPrivateKey() {
	}

	WrappedASymmetricPrivateKey(byte[] data) {
		super(data);
		if (data.length>MAX_SIZE_IN_BYTES_OF_KEY)
			throw new IllegalArgumentException();
	}




	public WrappedASymmetricPrivateKey(WrappedASymmetricPrivateKey secretData) {
		super(secretData);
	}

	@Override
	public final int getInternalSerializedSize() {
		return SerializationTools.getInternalSize(getBytes(), MAX_SIZE_IN_BYTES_OF_KEY);
	}

	@Override
	public final void writeExternal(SecuredObjectOutputStream out) throws IOException {
		out.writeBytesArray(getBytes(), false, MAX_SIZE_IN_BYTES_OF_KEY);
	}

	@Override
	public final void readExternal(SecuredObjectInputStream in) throws IOException {
		setData(in.readBytesArray(false, MAX_SIZE_IN_BYTES_OF_KEY));
	}
}
