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

package com.pushcoin.lib.core.utils;

public class Stopwatch {

	private static Stopwatch defaultStopwatch = null;

	public static Stopwatch getDefaultStopwatch() {
		if (defaultStopwatch == null)
			return defaultStopwatch = new Stopwatch();
		return defaultStopwatch;
	}

	public static final long NS_IN_MS = 1000000;

	public long start;
	public long end;

	public void start() {
		start = System.nanoTime();
	}

	public long stop() {
		return (end = System.nanoTime()) - start;
	}

	public long duration_ms() {
		return duration_ns() / NS_IN_MS;
	}

	public long duration_ns() {
		return end - start;
	}

	public long split_ms() {
		return split_ns() / NS_IN_MS;
	}

	public long split_ns() {
		return System.nanoTime() - start;
	}
}
