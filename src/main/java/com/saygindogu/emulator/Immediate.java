package com.saygindogu.emulator;

import com.saygindogu.emulator.RegisterConstants.Width;

public class Immediate {

	private final int value;

	public Immediate(int val) {
		value = val & 0xFFFF;
	}

	public OperationWidth getWidth() {
		if (value < 255) {
			return new OperationWidth(Width.EIGHT_BIT);
		} else if (value < 65535) {
			return new OperationWidth(Width.SIXTEEN_BIT);
		}
		return new OperationWidth(Width.THIRTY_TWO_BITS);
	}

	public int getIntValue() {
		return value;
	}
}
