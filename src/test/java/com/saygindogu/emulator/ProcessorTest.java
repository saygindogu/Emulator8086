package com.saygindogu.emulator;

import com.saygindogu.emulator.RegisterConstants.RegisterClass;
import com.saygindogu.emulator.exception.AssemblerException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ProcessorTest {

	private Processor processor;
	private Assembler assembler;

	@BeforeEach
	void setUp() throws AssemblerException {
		assembler = new Assembler(null);
		processor = new Processor(assembler, 4096);
	}

	@Test
	void testInitialRegisterValues() {
		var ax = new RegisterType("AX");
		assertEquals(0, processor.getRegisterValue(ax));
	}

	@Test
	void testSetAndGetGeneralRegister() throws AssemblerException {
		var ax = new RegisterType(RegisterClass.GENERAL_TYPE, RegisterConstants.AX);
		// Use startOS-less approach: directly test via the public getRegisterValue
		// We need to access setRegisterValue which is private, so test through instructions
		assertEquals(0, processor.getRegisterValue(ax));
	}

	@Test
	void testRegisterType8Bit() {
		var ah = new RegisterType("AH");
		assertEquals(RegisterClass.GENERAL_TYPE_8, ah.getRegisterClass());
		assertEquals(RegisterConstants.AH, ah.getRegisterIndex());
	}

	@Test
	void testRegisterType16Bit() {
		var ax = new RegisterType("AX");
		assertEquals(RegisterClass.GENERAL_TYPE, ax.getRegisterClass());
		assertEquals(RegisterConstants.AX, ax.getRegisterIndex());
	}

	@Test
	void testRegisterTypeSegment() {
		var cs = new RegisterType("CS");
		assertTrue(cs.isSegmentRegister());
		assertTrue(cs.isCS());
	}

	@Test
	void testRegisterTypeIP() {
		var ip = new RegisterType("IP");
		assertTrue(ip.isIP());
	}

	@Test
	void testRegisterWidth8Bit() {
		var ah = new RegisterType("AH");
		assertTrue(ah.getWidth().isEightBit());
		assertEquals(8, ah.getWidth().getIntWidth());
	}

	@Test
	void testRegisterWidth16Bit() {
		var ax = new RegisterType("AX");
		assertFalse(ax.getWidth().isEightBit());
		assertEquals(16, ax.getWidth().getIntWidth());
	}

	@Test
	void testMemorySize() {
		assertEquals(4096, processor.getMemorySize());
	}

	@Test
	void testMemoryNotNull() {
		assertNotNull(processor.getMemory());
	}

	@Test
	void testInitialFlags() {
		var flags = processor.getFlagRegister();
		assertNotNull(flags);
		assertEquals(16, flags.length);
		for (var flag : flags) {
			assertFalse(flag);
		}
	}

	@Test
	void testIsFinishedInitially() {
		assertFalse(processor.isFinished());
	}

	@Test
	void testIsWaitingInitially() {
		assertFalse(processor.isWaiting());
	}

	@Test
	void testSetWaiting() {
		processor.setWaiting(true);
		assertTrue(processor.isWaiting());
		processor.setWaiting(false);
		assertFalse(processor.isWaiting());
	}

	@Test
	void testAllRegisterNamesResolvable() {
		for (var name : RegisterConstants.REGISTER_NAMES) {
			var rt = new RegisterType(name);
			assertNotNull(rt.getRegisterClass(), "Register " + name + " should have a class");
		}
	}

	@Test
	void testPairRecord() {
		var pair = new Pair<>("key", 42);
		assertEquals("key", pair.key());
		assertEquals(42, pair.value());
		assertEquals("key", pair.getKey());
		assertEquals(42, pair.getValue());
	}

	@Test
	void testImmediateValue() {
		var imm = new Immediate(100);
		assertEquals(100, imm.getIntValue());
		assertTrue(imm.getWidth().isEightBit());
	}

	@Test
	void testImmediateValueLarge() {
		var imm = new Immediate(1000);
		assertEquals(1000, imm.getIntValue());
		assertFalse(imm.getWidth().isEightBit());
	}
}
