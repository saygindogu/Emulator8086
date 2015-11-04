package com.saygindogu.emulator;

//Register'lar tanmlamak iin yazlm tanmlayc data snf.
public class RegisterType {
	private int registerClass;
	private int registerIndex;
	
	public RegisterType( int cls, int index){
		registerClass = cls;
		registerIndex = index;
	}

	public RegisterType(String string) {
		if( string.equalsIgnoreCase( RegisterConstants.REGISTER_NAMES[ RegisterConstants.NAME_INDEX_AX]) ){
			registerClass = RegisterConstants.GENERAL_TYPE;
			registerIndex = RegisterConstants.AX;
		}
		else if( string.equalsIgnoreCase( RegisterConstants.REGISTER_NAMES[ RegisterConstants.NAME_INDEX_AH])){
			registerClass = RegisterConstants.GENERAL_TYPE_8;
			registerIndex = RegisterConstants.AH;
		}
		else if( string.equalsIgnoreCase( RegisterConstants.REGISTER_NAMES[ RegisterConstants.NAME_INDEX_AL])){
			registerClass = RegisterConstants.GENERAL_TYPE_8;
			registerIndex = RegisterConstants.AL;
		}
		else if( string.equalsIgnoreCase( RegisterConstants.REGISTER_NAMES[ RegisterConstants.NAME_INDEX_BX]) ){
			registerClass = RegisterConstants.GENERAL_TYPE;
			registerIndex = RegisterConstants.BX;
		}
		else if( string.equalsIgnoreCase( RegisterConstants.REGISTER_NAMES[ RegisterConstants.NAME_INDEX_BH])){
			registerClass = RegisterConstants.GENERAL_TYPE_8;
			registerIndex = RegisterConstants.BH;
		}
		else if( string.equalsIgnoreCase( RegisterConstants.REGISTER_NAMES[ RegisterConstants.NAME_INDEX_BL])){
			registerClass = RegisterConstants.GENERAL_TYPE_8;
			registerIndex = RegisterConstants.BL;
		}
		else if( string.equalsIgnoreCase( RegisterConstants.REGISTER_NAMES[ RegisterConstants.NAME_INDEX_CX]) ){
			registerClass = RegisterConstants.GENERAL_TYPE;
			registerIndex = RegisterConstants.CX;
		}
		else if( string.equalsIgnoreCase( RegisterConstants.REGISTER_NAMES[ RegisterConstants.NAME_INDEX_CH])){
			registerClass = RegisterConstants.GENERAL_TYPE_8;
			registerIndex = RegisterConstants.CH;
		}
		else if( string.equalsIgnoreCase( RegisterConstants.REGISTER_NAMES[ RegisterConstants.NAME_INDEX_CL])){
			registerClass = RegisterConstants.GENERAL_TYPE_8;
			registerIndex = RegisterConstants.CL;
		}
		else if( string.equalsIgnoreCase( RegisterConstants.REGISTER_NAMES[ RegisterConstants.NAME_INDEX_DX]) ){
			registerClass = RegisterConstants.GENERAL_TYPE;
			registerIndex = RegisterConstants.DX;
		}
		else if( string.equalsIgnoreCase( RegisterConstants.REGISTER_NAMES[ RegisterConstants.NAME_INDEX_DH])){
			registerClass = RegisterConstants.GENERAL_TYPE_8;
			registerIndex = RegisterConstants.DH;
		}
		else if( string.equalsIgnoreCase( RegisterConstants.REGISTER_NAMES[ RegisterConstants.NAME_INDEX_DL])){
			registerClass = RegisterConstants.GENERAL_TYPE_8;
			registerIndex = RegisterConstants.DL;
		}
		else if( string.equalsIgnoreCase( RegisterConstants.REGISTER_NAMES[ RegisterConstants.NAME_INDEX_CS])){
			registerClass = RegisterConstants.SEGMENT_TYPE;
			registerIndex = RegisterConstants.CS;
		}
		else if( string.equalsIgnoreCase( RegisterConstants.REGISTER_NAMES[ RegisterConstants.NAME_INDEX_DS])){
			registerClass = RegisterConstants.SEGMENT_TYPE;
			registerIndex = RegisterConstants.DS;
		}
		else if( string.equalsIgnoreCase( RegisterConstants.REGISTER_NAMES[ RegisterConstants.NAME_INDEX_ES])){
			registerClass = RegisterConstants.SEGMENT_TYPE;
			registerIndex = RegisterConstants.ES;
		}
		else if( string.equalsIgnoreCase( RegisterConstants.REGISTER_NAMES[ RegisterConstants.NAME_INDEX_SS])){
			registerClass = RegisterConstants.SEGMENT_TYPE;
			registerIndex = RegisterConstants.SS;
		}
		else if( string.equalsIgnoreCase( RegisterConstants.REGISTER_NAMES[ RegisterConstants.NAME_INDEX_SP])){
			registerClass = RegisterConstants.PTR_INDEX_TYPE;
			registerIndex = RegisterConstants.SP;
		}
		else if( string.equalsIgnoreCase( RegisterConstants.REGISTER_NAMES[ RegisterConstants.NAME_INDEX_BP])){
			registerClass = RegisterConstants.PTR_INDEX_TYPE;
			registerIndex = RegisterConstants.BP;
		}
		else if( string.equalsIgnoreCase( RegisterConstants.REGISTER_NAMES[ RegisterConstants.NAME_INDEX_SI])){
			registerClass = RegisterConstants.PTR_INDEX_TYPE;
			registerIndex = RegisterConstants.SI;
		}
		else if( string.equalsIgnoreCase( RegisterConstants.REGISTER_NAMES[ RegisterConstants.NAME_INDEX_DI])){
			registerClass = RegisterConstants.PTR_INDEX_TYPE;
			registerIndex = RegisterConstants.DI;
		}
		else if( string.equalsIgnoreCase( RegisterConstants.REGISTER_NAMES[ RegisterConstants.NAME_INDEX_IP])){
			registerClass = RegisterConstants.PROGRAM_STATUS_TYPE;
			registerIndex = RegisterConstants.IP;
		}
		else if( string.equalsIgnoreCase( "FLAG" )){ // doru mu bu?
			registerClass = RegisterConstants.PTR_INDEX_TYPE;
			registerIndex = RegisterConstants.FLAG;
		}
		else{
			registerClass = RegisterConstants.NONE;
			registerIndex = RegisterConstants.NONE;
		}
	}

	public int getRegisterClass() {
		return registerClass;
	}

	public int getRegisterIndex() {
		return registerIndex;
	}

	public boolean isIP() {
		return registerClass == RegisterConstants.PROGRAM_STATUS_TYPE && 
				registerIndex == RegisterConstants.IP;
	}
	
	public boolean isCS(){
		return registerClass == RegisterConstants.SEGMENT_TYPE && 
				registerIndex == RegisterConstants.CS;
	}
	
	public boolean isSegmentRegister(){
		return registerClass == RegisterConstants.SEGMENT_TYPE;
	}

	public OperationWidth getWidth() {
		if( registerClass == RegisterConstants.GENERAL_TYPE_8 ){
			return new OperationWidth( RegisterConstants.EIGHT_BIT);
		}
		else
			return new OperationWidth( RegisterConstants.SIXTEEN_BIT);
	}
}
