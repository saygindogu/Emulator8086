package com.saygindogu.emulator.gui;

public class EmuWord {
	private int wordValue;
	private int representationMode;
	
	public EmuWord( short val){
		wordValue = val & 0xFFFF;
		representationMode = GUIConstants.HEX_MODE;
	}

	public int getWordValue() {
		return wordValue;
	}


	public void setWordValue(int wordValue) {
		this.wordValue = wordValue;
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
			return "" + wordValue;
		}
		else if( representationMode == GUIConstants.HEX_MODE){
			return getHexString();
		}
		else if( representationMode == GUIConstants.BINARY_MODE ){
			return getBinaryString();
		}
		else{
			return "unknown (decimal value:" + wordValue +")";
		}
		
	}

	private String getBinaryString() {
		String binaryRepresentation = Integer.toBinaryString(wordValue);
		StringBuilder builder = new StringBuilder( binaryRepresentation);
		while( builder.length() < 16){
			builder.insert(0 ,'0');
		}
		return builder.toString();
	}

	private String getHexString() {
		String hexRepresentation = Integer.toHexString(wordValue);
		StringBuilder builder = new StringBuilder( "0x" + hexRepresentation);
		while( builder.length() < 6){
			builder.insert( 2 ,'0');
		}
		return builder.toString();
	}

}
