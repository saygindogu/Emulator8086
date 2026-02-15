package com.saygindogu.emulator;

public interface RegisterConstants {
	int NONE = -1;
	int INVALID_VALUE = 2000000000;

	// Register class enum
	enum RegisterClass {
		GENERAL_TYPE, GENERAL_TYPE_8, SEGMENT_TYPE, PTR_INDEX_TYPE, PROGRAM_STATUS_TYPE
	}

	// Index values
	int AX = 0;
	int AH = 0;
	int AL = 1;
	int BX = 2;
	int BH = 2;
	int BL = 3;
	int CX = 4;
	int CH = 4;
	int CL = 5;
	int DX = 6;
	int DH = 6;
	int DL = 7;
	int CS = 0;
	int DS = 1;
	int SS = 2;
	int ES = 3;
	int SP = 0;
	int BP = 1;
	int SI = 2;
	int DI = 3;

	// Type values (valid for program status type registers)
	int FLAG = 85;
	int IP = 90;

	// Flag indexes
	int OF = 11;
	int ZF = 6;
	int SF = 7;
	int DF = 10;
	int PF = 2;
	int CF = 0;

	// Width enum
	enum Width {
		EIGHT_BIT(8), SIXTEEN_BIT(16), THIRTY_TWO_BITS(32);

		private final int bits;
		Width(int bits) { this.bits = bits; }
		public int getBits() { return bits; }
	}

	int NAME_INDEX_AH = 0;
	int NAME_INDEX_AL = 1;
	int NAME_INDEX_AX = 2;
	int NAME_INDEX_BH = 3;
	int NAME_INDEX_BL = 4;
	int NAME_INDEX_BX = 5;
	int NAME_INDEX_CH = 6;
	int NAME_INDEX_CL = 7;
	int NAME_INDEX_CX = 8;
	int NAME_INDEX_DH = 9;
	int NAME_INDEX_DL = 10;
	int NAME_INDEX_DX = 11;
	int NAME_INDEX_CS = 12;
	int NAME_INDEX_DS = 13;
	int NAME_INDEX_SS = 14;
	int NAME_INDEX_ES = 15;
	int NAME_INDEX_SP = 16;
	int NAME_INDEX_BP = 17;
	int NAME_INDEX_SI = 18;
	int NAME_INDEX_DI = 19;
	int NAME_INDEX_IP = 20;

	// Register names
	String[] REGISTER_NAMES = {
		"AH", "AL", "AX", "BH", "BL", "BX", "CH", "CL", "CX",
		"DH", "DL", "DX", "CS", "DS", "SS", "ES", "SP", "BP", "SI", "DI", "IP"
	};
}
