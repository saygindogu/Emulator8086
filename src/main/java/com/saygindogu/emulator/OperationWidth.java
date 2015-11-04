package com.saygindogu.emulator;

//
public class OperationWidth {
	private int width;
	
	public OperationWidth(int eightOrSixteenBit) {
		width = eightOrSixteenBit;
	}

	public boolean isEightBit(){
		return width == RegisterConstants.EIGHT_BIT;
	}
	public boolean isThirtyTwoBit(){
		return width == RegisterConstants.THIRTY_TWO_BITS;
	}

	public int getIntWidth() {
		switch( width){
		case RegisterConstants.EIGHT_BIT:
			return 8;
		case RegisterConstants.THIRTY_TWO_BITS:
			return 32;
		default:
			return 16;
		}
	}
	
}
