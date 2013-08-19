package com.pushcoin.core.data;

import java.math.BigDecimal;
import java.text.DecimalFormat;

import com.pushcoin.pcos.InputBlock;
import com.pushcoin.pcos.OutputBlock;
import com.pushcoin.pcos.PcosError;

public class PcosAmount {

	static public boolean Optional = true;
	static private DecimalFormat moneyFormatwithSign = new DecimalFormat("$###0.00");
	static private DecimalFormat moneyFormatwithSign2 = new DecimalFormat("###0.00");

	private boolean optional = false;
	private long Value;
	private int Scale;
	
	public PcosAmount(BigDecimal amount, boolean opt)
	{
		optional = opt;
		String strAmt = moneyFormatwithSign2.format(amount.doubleValue()).replace(".","");
		Value = Long.parseLong(strAmt);
		Scale = -2;
	}
	public PcosAmount(BigDecimal amount) { this(amount, false); }
	
	public PcosAmount(InputBlock ib) throws PcosError
	{		
		Value = ib.readLong();
		Scale = ib.readInt();	}
	
	public void Write(OutputBlock writer) throws PcosError, Exception
	{
		if(optional)
			writer.writeBool(true);
		writer.writeLong(Value);
		writer.writeInt(Scale);
	}
	
	@Override
	public String toString()
	{
		BigDecimal Amount = new BigDecimal(Value * Math.pow(10, Scale));
		return moneyFormatwithSign.format(Amount.doubleValue());
	}
	
}