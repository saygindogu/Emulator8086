package com.saygindogu.emulator.gui;


public class EmuAddress {
	
	private int addressValue;
	private int representationMode;
	
	public EmuAddress( int val){
		addressValue = val & 0xFFFFF; //20bits
		representationMode = GUIConstants.HEX_MODE;
	}
	

	@Override
	public String toString(){
		if( representationMode == GUIConstants.DECIMAL_MODE ){
			return "" + addressValue;
		}
		else if( representationMode == GUIConstants.HEX_MODE){
			return getHexString();
		}
		else if( representationMode == GUIConstants.BINARY_MODE ){
			return getBinaryString();
		}
		else{
			return "unknown (decimal value:" + addressValue +")";
		}
		
	}

	private String getBinaryString() {
		String binaryRepresentation = Integer.toBinaryString(addressValue);
		StringBuilder builder = new StringBuilder( binaryRepresentation);
		while( builder.length() < 20){
			builder.insert(0 ,'0');
		}
		return builder.toString();
	}

	private String getHexString() {
		String hexRepresentation = Integer.toHexString(addressValue);
		StringBuilder builder = new StringBuilder();
		builder.append( "0x");
		builder.append( hexRepresentation);
		while( builder.length() < 7){
			builder.insert( 2 ,'0');
		}
		return builder.toString();
	}


	public void setRepresentationMode(int addressRepMode) {
		representationMode = addressRepMode;
		
	}

}
