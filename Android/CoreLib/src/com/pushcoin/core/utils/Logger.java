package com.pushcoin.core.utils;
import android.util.Log;

public class Logger {
	public static Logger getLogger(Class<?> c) {
		return new Logger(c);
	}
	
	public Logger(Class<?> c) {
		this.tagName = c.getSimpleName();
	}

	public void d(String message) {
		Log.d(tagName, message);
	}
	
	public void d(String message, Throwable t) {
		Log.d(tagName, message, t);
	}

	public void e(String message) {
		Log.e(tagName, message);
	}

	public void e(String message, Throwable t) {
		Log.e(tagName, message, t);
	}

	public void i(String message) {
		Log.i(tagName, message);
	}
	
	public void i(String message, Throwable t) {
		Log.i(tagName, message, t);
	}
	
	public void v(String message) {
		Log.v(tagName, message);
	}
	
	public void v(String message, Throwable t) {
		Log.v(tagName, message, t);
	}

	public void w(String message) {
		Log.w(tagName, message);
	}
	
	public void w(String message, Throwable t) {
		Log.w(tagName, message, t);
	}

	private String tagName;
}
