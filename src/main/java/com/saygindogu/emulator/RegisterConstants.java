package com.saygindogu.emulator;

public interface RegisterConstants {
	public static final int NONE = -1;
	public static final int INVALID_VALUE = 2000000000;
	
	//index values
	public static final int AX = 0;
	public static final int AH = 0;
	public static final int AL = 1;
	
	
	public static final int BX = 2;
	public static final int BH = 2;
	public static final int BL = 3;
	public static final int CX = 4;
	public static final int CH = 4;
	public static final int CL = 5;
	public static final int DX = 6;
	public static final int DH = 6;
	public static final int DL = 7;
	public static final int CS = 0;
	public static final int DS = 1;
	public static final int SS = 2;
	public static final int ES = 3;
	public static final int SP = 0;
	public static final int BP = 1;
	public static final int SI = 2;
	public static final int DI = 3;
	
	//Type values( valid for program status type registers)
	public static final int FLAG = 85;
	public static final int IP = 90;
	
	//Register Class Constants
	public static final int SEGMENT_TYPE = 307;
	public static final int GENERAL_TYPE = 308;
	public static final int GENERAL_TYPE_8 = 888;
	public static final int PTR_INDEX_TYPE = 309;
	public static final int PROGRAM_STATUS_TYPE = 400;
	
	//flag indexes
	public static final int OF = 11; //overflow flag
	public static final int ZF = 6; //zero flag
	public static final int SF = 7; //sign flag
	public static final int DF = 10; //direction flag
	public static final int PF = 2; //pairity flag
	public static final int CF = 0; //carry flag
	
	//width types
	public static final int EIGHT_BIT = -80000;
	public static final int SIXTEEN_BIT = -160000;
	public static final int THIRTY_TWO_BITS = -32000;
	
	

	// register name indexes in register names
	public static final int NAME_INDEX_AX = 2;
	public static final int NAME_INDEX_AH = 0;
	public static final int NAME_INDEX_AL = 1;
	public static final int NAME_INDEX_BX = 5;
	public static final int NAME_INDEX_BH = 3;
	public static final int NAME_INDEX_BL = 4;
	public static final int NAME_INDEX_CX = 8;
	public static final int NAME_INDEX_CH = 6;
	public static final int NAME_INDEX_CL = 7;
	public static final int NAME_INDEX_DX = 11;
	public static final int NAME_INDEX_DH = 9;
	public static final int NAME_INDEX_DL = 10;
	public static final int NAME_INDEX_CS = 12;
	public static final int NAME_INDEX_DS = 13;
	public static final int NAME_INDEX_SS = 14;
	public static final int NAME_INDEX_ES = 15;
	public static final int NAME_INDEX_SP = 16;
	public static final int NAME_INDEX_BP = 17;
	public static final int NAME_INDEX_SI = 18;
	public static final int NAME_INDEX_DI = 19;
	public static final int NAME_INDEX_IP = 20;
	
	//register names
	public static final String[] REGISTER_NAMES = { "AH", "AL","AX", "BH", "BL", "BX", "CH", "CL", "CX",
		"DH", "DL", "DX", "CS", "DS", "SS", "ES", "SP", "BP", "SI", "DI", "IP"};
	
	
	
}
