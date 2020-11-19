package com.distrimind.util.data_buffers;

import com.distrimind.util.Bits;
import com.distrimind.util.crypto.Zeroizable;

import java.io.IOException;
import java.util.Arrays;
import java.util.Base64;

/**
 * @author Jason Mahdjoub
 * @version 1.0
 * @since Utils 5.10.0
 */
public class WrappedSecretData extends WrappedData implements Zeroizable {

	private transient boolean toZeroize;


	protected WrappedSecretData()
	{
		super();
		toZeroize=false;
	}
	public WrappedSecretData(WrappedString secretData) throws IOException {
		super();

		String s=new String(secretData.getChars());
		byte[] d=Base64.getUrlDecoder().decode(s);
		setData(Bits.checkByteArrayAndReturnsItWithoutCheckSum(d));
		WrappedSecretString.zeroizeString(s);
		Arrays.fill(d, (byte)0);
	}


	public WrappedSecretData(byte[] secretData) {
		super(secretData);
		toZeroize=true;

	}

	public WrappedSecretData(WrappedData wrappedSecretData) {
		super(wrappedSecretData.getBytes().clone());
	}

	protected void setData(byte[] data)
	{
		zeroize();
		toZeroize=true;
		super.setData(data);
	}

	@Override
	public void zeroize()
	{
		if (toZeroize) {
			Arrays.fill(getBytes(), (byte) 0);
			toZeroize=false;
		}
	}
	@SuppressWarnings("deprecation")
	@Override
	public void finalize()
	{
		zeroize();
	}

	@Override
	public WrappedSecretData transformToSecretData() {
		return this;
	}
}
