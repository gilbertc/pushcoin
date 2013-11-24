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
