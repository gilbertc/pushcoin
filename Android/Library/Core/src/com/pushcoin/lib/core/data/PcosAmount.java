/*
  Copyright (c) 2013 PushCoin Inc

  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.
  
  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.
  
  You should have received a copy of the GNU General Public License
  along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package com.pushcoin.lib.core.data;

import java.math.BigDecimal;
import java.text.DecimalFormat;

import com.pushcoin.lib.pcos.InputBlock;
import com.pushcoin.lib.pcos.OutputBlock;
import com.pushcoin.lib.pcos.PcosError;

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

	public long getValue() {
		return value;
	}

	public int getScale() {
		return scale;
	}

}
