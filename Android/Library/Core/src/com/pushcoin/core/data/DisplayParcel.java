package com.pushcoin.core.data;

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
