package com.pushcoin.core.utils;
import android.util.Log;

public class Logger {
	public static Logger getLogger(Class<?> c) {
		return new Logger(c);
	}
	
	public Logger(Class<?> c) {
		this.className = c.getSimpleName();
		this.tagName = "[pushcoin] " + this.className; // Use to grep in logcat
	}
	
	private String getMessage(String message) {
		return message;
	}

	public void d(String message) {
		Log.d(tagName, getMessage(message));
	}
	
	public void d(String message, Throwable t) {
		Log.d(tagName, getMessage(message), t);
	}

	public void e(String message) {
		Log.d(tagName, getMessage(message));
	}

	public void e(String message, Throwable t) {
		Log.e(tagName, getMessage(message), t);
	}

	public void i(String message) {
		Log.i(tagName, message);
	}
	
	public void i(String message, Throwable t) {
		Log.i(tagName, getMessage(message), t);
	}
	
	public void v(String message) {
		Log.v(tagName, message);
	}
	
	public void v(String message, Throwable t) {
		Log.v(tagName, getMessage(message), t);
	}

	public void w(String message) {
		Log.w(tagName, message);
	}
	
	public void w(String message, Throwable t) {
		Log.w(tagName, getMessage(message), t);
	}

	private String tagName;
	private String className;
}
