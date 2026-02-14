package com.saygindogu.emulator;

import com.saygindogu.emulator.RegisterConstants.Width;

public class OperationWidth {
	private final Width width;

	public OperationWidth(Width width) {
		this.width = width;
	}

	public boolean isEightBit() {
		return width == Width.EIGHT_BIT;
	}

	public boolean isThirtyTwoBit() {
		return width == Width.THIRTY_TWO_BITS;
	}

	public int getIntWidth() {
		return width.getBits();
	}
}
