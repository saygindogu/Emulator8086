package com.saygindogu.emulator;

import com.saygindogu.emulator.RegisterConstants.RegisterClass;
import com.saygindogu.emulator.RegisterConstants.Width;

import java.util.Map;

import static com.saygindogu.emulator.RegisterConstants.*;
import static com.saygindogu.emulator.RegisterConstants.RegisterClass.*;

public class RegisterType {
	private static final Map<String, RegisterType> REGISTER_MAP = Map.ofEntries(
		Map.entry("AX", new RegisterType(GENERAL_TYPE, AX)),
		Map.entry("AH", new RegisterType(GENERAL_TYPE_8, AH)),
		Map.entry("AL", new RegisterType(GENERAL_TYPE_8, AL)),
		Map.entry("BX", new RegisterType(GENERAL_TYPE, BX)),
		Map.entry("BH", new RegisterType(GENERAL_TYPE_8, BH)),
		Map.entry("BL", new RegisterType(GENERAL_TYPE_8, BL)),
		Map.entry("CX", new RegisterType(GENERAL_TYPE, CX)),
		Map.entry("CH", new RegisterType(GENERAL_TYPE_8, CH)),
		Map.entry("CL", new RegisterType(GENERAL_TYPE_8, CL)),
		Map.entry("DX", new RegisterType(GENERAL_TYPE, DX)),
		Map.entry("DH", new RegisterType(GENERAL_TYPE_8, DH)),
		Map.entry("DL", new RegisterType(GENERAL_TYPE_8, DL)),
		Map.entry("CS", new RegisterType(SEGMENT_TYPE, CS)),
		Map.entry("DS", new RegisterType(SEGMENT_TYPE, DS)),
		Map.entry("SS", new RegisterType(SEGMENT_TYPE, SS)),
		Map.entry("ES", new RegisterType(SEGMENT_TYPE, ES)),
		Map.entry("SP", new RegisterType(PTR_INDEX_TYPE, SP)),
		Map.entry("BP", new RegisterType(PTR_INDEX_TYPE, BP)),
		Map.entry("SI", new RegisterType(PTR_INDEX_TYPE, SI)),
		Map.entry("DI", new RegisterType(PTR_INDEX_TYPE, DI)),
		Map.entry("IP", new RegisterType(PROGRAM_STATUS_TYPE, IP)),
		Map.entry("FLAG", new RegisterType(PTR_INDEX_TYPE, FLAG))
	);

	private final RegisterClass registerClass;
	private final int registerIndex;

	public RegisterType(RegisterClass cls, int index) {
		registerClass = cls;
		registerIndex = index;
	}

	public RegisterType(String name) {
		var found = REGISTER_MAP.get(name.toUpperCase());
		if (found != null) {
			registerClass = found.registerClass;
			registerIndex = found.registerIndex;
		} else {
			registerClass = null;
			registerIndex = NONE;
		}
	}

	public RegisterClass getRegisterClass() {
		return registerClass;
	}

	public int getRegisterIndex() {
		return registerIndex;
	}

	public boolean isIP() {
		return registerClass == PROGRAM_STATUS_TYPE && registerIndex == IP;
	}

	public boolean isCS() {
		return registerClass == SEGMENT_TYPE && registerIndex == CS;
	}

	public boolean isSegmentRegister() {
		return registerClass == SEGMENT_TYPE;
	}

	public OperationWidth getWidth() {
		if (registerClass == GENERAL_TYPE_8) {
			return new OperationWidth(Width.EIGHT_BIT);
		}
		return new OperationWidth(Width.SIXTEEN_BIT);
	}

	public static RegisterType fromName(String name) {
		return REGISTER_MAP.get(name.toUpperCase());
	}
}
