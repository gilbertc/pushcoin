package com.pushcoin.core.utils;

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
