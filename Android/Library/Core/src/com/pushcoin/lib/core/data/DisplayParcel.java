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

import java.io.Serializable;

public class DisplayParcel implements Serializable {

	private static final long serialVersionUID = -6209031198559928044L;

	public enum Mode {
		EMPTY, ASCII, BINARY
	};

	public enum TextAlignment {
		LEFT, CENTER, RIGHT, JUSTIFIED
	};

	public static final char FILL = (char) 1;

	public static String Padding(int n, char c) {
		return n > 0 ? new String(new char[n]).replace('\0', c) : "";
	}

	public static String FormatString(String raw, TextAlignment align,
			int maxLength) {

		if (raw.length() == 0)
			return raw;

		int fillPos = raw.indexOf(FILL);
		if (fillPos >= 0)
			return raw.replaceFirst("" + FILL,
					Padding(maxLength - raw.length() + 1, ' ')).substring(0,
					maxLength);

		int length = Math.min(maxLength, raw.length() - (fillPos < 0 ? 0 : 1));
		int diff = maxLength - length;

		String s = raw.substring(0, length);
		switch (align) {
		case LEFT:
			return s;
		case CENTER:
			return Padding(diff / 2, ' ') + s;
		case RIGHT:
			return Padding(diff, ' ') + s;
		default:
			return raw;
		}

	}

	private Mode mode;
	private byte[] bytes;
	private String[] texts;
	private TextAlignment align;

	public Mode getMode() {
		return this.mode;
	}

	public byte[] getBytes() {
		if (this.mode == Mode.BINARY)
			return this.bytes;
		return null;
	}

	public String[] getTexts() {
		if (this.mode == Mode.ASCII)
			return this.texts;
		return null;
	}

	public TextAlignment getTextAlignment() {
		return this.align;
	}

	public DisplayParcel(String[] texts, TextAlignment align) {
		this.texts = texts;
		this.align = align;
		this.mode = Mode.ASCII;
	}

	public DisplayParcel(String[] texts) {
		this(texts, TextAlignment.LEFT);
	}

	public DisplayParcel(String text) {
		this(new String[] { text }, TextAlignment.LEFT);
	}

	public DisplayParcel(String text, TextAlignment align) {
		this(new String[] { text }, align);
	}

	public DisplayParcel(byte[] bytes) {
		this.bytes = bytes;
		this.mode = Mode.BINARY;
	}

	public DisplayParcel() {
		this.mode = Mode.EMPTY;
	}

}
