package com.saygindogu.emulator;

import com.saygindogu.emulator.RegisterConstants.Width;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MemoryTest {

	private Memory memory;

	@BeforeEach
	void setUp() {
		memory = new Memory(256);
	}

	@Test
	void testDefaultMemorySize() {
		var defaultMemory = new Memory();
		assertEquals(Memory.DEFAULT_MEMORY_SIZE, defaultMemory.getByteArray().length);
	}

	@Test
	void testCustomMemorySize() {
		assertEquals(256, memory.getByteArray().length);
	}

	@Test
	void testWriteAndReadByte() {
		var width = new OperationWidth(Width.EIGHT_BIT);
		memory.write(0, 0x42, width);
		assertEquals(0x42, memory.read(0, width));
	}

	@Test
	void testWriteAndReadWord() {
		var width = new OperationWidth(Width.SIXTEEN_BIT);
		memory.write(0, 0x1234, width);
		assertEquals(0x1234, memory.read(0, width));
	}

	@Test
	void testWriteByteAtOffset() {
		var width = new OperationWidth(Width.EIGHT_BIT);
		memory.write(10, 0xAB, width);
		assertEquals((byte) 0xAB, memory.getByteArray()[10]);
	}

	@Test
	void testClear() {
		var width = new OperationWidth(Width.EIGHT_BIT);
		memory.write(0, 0xFF, width);
		memory.clear();
		assertEquals(0, memory.read(0, width));
	}

	@Test
	void testReset() {
		var width = new OperationWidth(Width.EIGHT_BIT);
		memory.write(0, 0xFF, width);
		memory.reset(128);
		assertEquals(128, memory.getByteArray().length);
		assertEquals(0, memory.read(0, width));
	}
}
