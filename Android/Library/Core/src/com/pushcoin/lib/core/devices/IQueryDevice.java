package com.pushcoin.lib.core.devices;

import java.io.IOException;
import com.pushcoin.lib.core.query.QueryListener;

public interface IQueryDevice extends IDevice {
	public void enable(QueryListener receiver) throws IOException;

	public void disable();

	public boolean isEnabled();
}
