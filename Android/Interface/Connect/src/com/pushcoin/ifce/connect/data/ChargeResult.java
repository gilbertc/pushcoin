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

package com.pushcoin.ifce.connect.data;

import android.os.Bundle;

public class ChargeResult extends CallbackResult {
	public static final String KEY_REFDATA = "REFDATA";
	public static final String KEY_TRXID = "TRXID";
	public static final String KEY_ISAMOUNTEXACT = "ISAMOUNTEXACT";
	public static final String KEY_BALANCE = "BALANCE";
	public static final String KEY_UTC = "UTC";

	public ChargeResult() {
		this(new Bundle());
	}

	public ChargeResult(Bundle bundle) {
		super(bundle);
	}

	public byte[] getRefData() {
		return bundle.getByteArray(KEY_REFDATA);
	}

	public void setRefData(byte[] value) {
		bundle.putByteArray(KEY_REFDATA, value);
	}

	public String getTrxId() {
		return bundle.getString(KEY_TRXID);
	}

	public void setTrxId(String value) {
		bundle.putString(KEY_TRXID, value);
	}

	public Amount getBalance() {
		return bundle.getParcelable(KEY_BALANCE);
	}

	public void setBalance(Amount value) {
		bundle.putParcelable(KEY_BALANCE, value);
	}

	public boolean getIsAmountExact() {
		return bundle.getBoolean(KEY_ISAMOUNTEXACT);
	}

	public void setIsAmountExact(boolean value) {
		bundle.putBoolean(KEY_ISAMOUNTEXACT, value);
	}

	public long getUtc() {
		return bundle.getLong(KEY_UTC);
	}

	public void setUtc(long value) {
		bundle.putLong(KEY_UTC, value);
	}

}
