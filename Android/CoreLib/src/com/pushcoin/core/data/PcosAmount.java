package com.pushcoin.core.data;

import java.math.BigDecimal;
import java.text.DecimalFormat;

import com.pushcoin.pcos.InputBlock;
import com.pushcoin.pcos.OutputBlock;
import com.pushcoin.pcos.PcosError;

public class PcosAmount {

	public static boolean OPTIONAL = true;

	private static DecimalFormat FMT_WITH_SIGN = new DecimalFormat("$###0.00");
	private static DecimalFormat FMT_NO_SIGN = new DecimalFormat("###0.00");

	private boolean optional = false;
	private long value;
	private int scale;

	public PcosAmount(long value, int scale) {
		this(value, scale, !OPTIONAL);
	}

	public PcosAmount(long value, int scale, boolean optional) {
		this.optional = optional;
		this.value = value;
		this.scale = scale;
	}

	public PcosAmount(BigDecimal amount, boolean opt) {
		optional = opt;
		String strAmt = FMT_NO_SIGN.format(amount.doubleValue()).replace(".",
				"");
		value = Long.parseLong(strAmt);
		scale = -2;
	}

	public PcosAmount(BigDecimal amount) {
		this(amount, !OPTIONAL);
	}

	public PcosAmount(InputBlock ib) throws PcosError {
		value = ib.readLong();
		scale = ib.readInt();
	}

	public void write(OutputBlock writer) throws PcosError {
		if (optional)
			writer.writeBool(true);
		writer.writeLong(value);
		writer.writeInt(scale);
	}

	@Override
	public String toString() {
		BigDecimal Amount = new BigDecimal(value * Math.pow(10, scale));
		return FMT_WITH_SIGN.format(Amount.doubleValue());
	}

}