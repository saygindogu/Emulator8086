package com.saygindogu.emulator.gui;

public class EmuByte {
	
	private int byteValue; //20bit address
	private int representationMode;
	
	public EmuByte( byte val){
		byteValue = val & 0xFF;
		representationMode = GUIConstants.HEX_MODE;
	}
	
	public int getByteValue() {
		return byteValue;
	}

	public void setByteValue(int byteValue) {
		this.byteValue = byteValue;
	}

	public int getRepresentationMode() {
		return representationMode;
	}

	public void setRepresentationMode(int representationMode) {
		this.representationMode = representationMode;
	}

	@Override
	public String toString(){
		if( representationMode == GUIConstants.DECIMAL_MODE ){
			return "" + byteValue;
		}
		else if( representationMode == GUIConstants.HEX_MODE){
			return getHexString();
		}
		else if( representationMode == GUIConstants.BINARY_MODE ){
			return getBinaryString();
		}
		else{
			return "unknown (decimal value:" + byteValue +")";
		}
		
	}

	private String getBinaryString() {
		String binaryRepresentation = Integer.toBinaryString(byteValue);
		StringBuilder builder = new StringBuilder( binaryRepresentation);
		while( builder.length() < 8){
			builder.insert(0 ,'0');
		}
		return builder.toString();
	}

	private String getHexString() {
		String hexRepresentation = Integer.toHexString(byteValue);
		StringBuilder builder = new StringBuilder( "0x" + hexRepresentation);
		while( builder.length() < 4){
			builder.insert( 2 ,'0');
		}
		return builder.toString();
	}
}
