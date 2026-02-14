package com.saygindogu.emulator;

import com.saygindogu.emulator.RegisterConstants.RegisterClass;
import com.saygindogu.emulator.exception.AssemblerException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AssemblerTest {

	@Test
	void testIsNumberDecimal() {
		assertTrue(Assembler.isNumber("123"));
		assertTrue(Assembler.isNumber("0"));
		assertTrue(Assembler.isNumber("65535"));
	}

	@Test
	void testIsNumberHex() {
		assertTrue(Assembler.isNumber("FFh"));
		assertTrue(Assembler.isNumber("0Ah"));
		assertTrue(Assembler.isNumber("1234H"));
	}

	@Test
	void testIsNumberBinary() {
		assertTrue(Assembler.isNumber("1010b"));
		assertTrue(Assembler.isNumber("11111111b"));
	}

	@Test
	void testIsNotNumber() {
		assertFalse(Assembler.isNumber("AX"));
		assertFalse(Assembler.isNumber("hello"));
	}

	@Test
	void testIsRegister() {
		assertTrue(Assembler.isRegister("AX"));
		assertTrue(Assembler.isRegister("BX"));
		assertTrue(Assembler.isRegister("CX"));
		assertTrue(Assembler.isRegister("DX"));
		assertTrue(Assembler.isRegister("AH"));
		assertTrue(Assembler.isRegister("AL"));
		assertTrue(Assembler.isRegister("CS"));
		assertTrue(Assembler.isRegister("DS"));
		assertTrue(Assembler.isRegister("SP"));
		assertTrue(Assembler.isRegister("IP"));
	}

	@Test
	void testIsNotRegister() {
		assertFalse(Assembler.isRegister("XY"));
		assertFalse(Assembler.isRegister("MOV"));
		assertFalse(Assembler.isRegister("123"));
	}

	@Test
	void testGetImmediateValueDecimal() throws AssemblerException {
		var imm = Assembler.getImmediateValue("42");
		assertEquals(42, imm.getIntValue());
	}

	@Test
	void testGetImmediateValueHex() throws AssemblerException {
		var imm = Assembler.getImmediateValue("10h");
		assertEquals(16, imm.getIntValue());
	}

	@Test
	void testDetermineRegisterTypeAX() throws AssemblerException {
		var rt = Assembler.determineRegisterType("AX");
		assertEquals(RegisterClass.GENERAL_TYPE, rt.getRegisterClass());
		assertEquals(RegisterConstants.AX, rt.getRegisterIndex());
	}

	@Test
	void testDetermineRegisterTypeCS() throws AssemblerException {
		var rt = Assembler.determineRegisterType("CS");
		assertEquals(RegisterClass.SEGMENT_TYPE, rt.getRegisterClass());
		assertEquals(RegisterConstants.CS, rt.getRegisterIndex());
	}

	@Test
	void testDetermineRegisterTypeSP() throws AssemblerException {
		var rt = Assembler.determineRegisterType("SP");
		assertEquals(RegisterClass.PTR_INDEX_TYPE, rt.getRegisterClass());
		assertEquals(RegisterConstants.SP, rt.getRegisterIndex());
	}

	@Test
	void testDetermineRegisterTypeIP() throws AssemblerException {
		var rt = Assembler.determineRegisterType("IP");
		assertEquals(RegisterClass.PROGRAM_STATUS_TYPE, rt.getRegisterClass());
		assertEquals(RegisterConstants.IP, rt.getRegisterIndex());
	}

	@Test
	void testDetermineRegisterTypeInvalid() {
		assertThrows(AssemblerException.class, () -> Assembler.determineRegisterType("INVALID"));
	}

	@Test
	void testGetRidOfWhiteSpaceInTokens() {
		var tokens = new java.util.ArrayList<String>();
		tokens.add(" hello world ");
		tokens.add("  test  ");
		var result = Assembler.getRidOfWhiteSpaceInTokens(tokens);
		assertEquals("helloworld", result.get(0));
		assertEquals("test", result.get(1));
	}
}
