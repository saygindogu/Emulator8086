package com.saygindogu.emulator;

//Assembly kodu iinde geen immediate Value'lar temsil eden obje.
public class Immediate {
	
	private int value;
	
	public Immediate( int val){
		value = val & 0xFFFF;
	}
	
	public OperationWidth getWidth(){
		if( value < 255){
			return new OperationWidth( RegisterConstants.EIGHT_BIT);
		}
		else if( value < 65535){
			return new OperationWidth( RegisterConstants.SIXTEEN_BIT);
		}
		else return new OperationWidth(RegisterConstants.THIRTY_TWO_BITS);
	}

	public int getIntValue() {
		return value;
	}

}
