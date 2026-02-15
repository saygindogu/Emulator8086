package com.saygindogu.emulator;

import com.saygindogu.emulator.RegisterConstants.RegisterClass;
import com.saygindogu.emulator.RegisterConstants.Width;
import com.saygindogu.emulator.exception.AssemblerException;
import com.saygindogu.emulator.exception.EMURunTimeException;
import com.saygindogu.emulator.functionptrs.BasicALUFunction;
import com.saygindogu.emulator.functionptrs.MultiplicationDivisionFunction;
import com.saygindogu.emulator.functionptrs.OneOpenardBasicFunction;
import com.saygindogu.emulator.functionptrs.ShiftRotateFunction;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class Processor {
	private static final Logger LOGGER = Logger.getLogger(Processor.class.getName());
	private static final int GENERAL_REGISTER_ARRAY_SIZE = 8;
	private static final int SEGMENT_REGISTERS_ARRAY_SIZE = 4;
	private static final int FLAG_ARRAY_SIZE = 16;
	private static final int PTR_INDEX_REGS_ARRAY_SIZE = 4;

	public static final String[] SUPPORTED_MNEMONICS_LIST = {
		"ADD", "MOV", "STC", "STD", "JPO", "JNP", "JNE", "JMP", "JLE", "JL",
		"JG", "JGE", "JC", "JB", "JBE", "JAE", "JA", "CMP", "CLD",
		"ADC", "CLC", "ROL", "ROR", "NOP", "AND", "OR", "XOR", "SUB",
		"NEG", "NOT", "DEC", "INC", "LEA", "HLT", "DIV", "MUL", "IDIV", "IMUL",
		"SBB", "SHL", "SHR"
	};

	List<String> currentInstructionLineTokens;

	private Memory memory;
	private int memorySize;
	private boolean finished;
	private boolean waiting;

	private byte[] generalPurposeRegisters;
	private short[] segmentRegisters;
	private short[] ptrIndexRegisters;
	private short instructionPointerRegister;
	private boolean[] flagRegister;
	private Assembler assembler;
	private int instructionIndex;

	public Processor(Assembler assembler, int memorySize) {
		this.assembler = assembler;
		init(memorySize);
		currentInstructionLineTokens = null;
	}

	private void init(int memorySize) {
		if (memorySize < Emulator.MIN_MEMORY_SIZE) {
			memory = new Memory();
			this.memorySize = Memory.DEFAULT_MEMORY_SIZE;
		} else {
			memory = new Memory(memorySize);
			this.memorySize = memorySize;
		}
		initRegisters();
		finished = false;
		waiting = false;
	}

	public Memory getMemory() { return memory; }
	public int getMemorySize() { return memorySize; }
	public boolean[] getFlagRegister() { return flagRegister; }
	public boolean isFinished() { return finished; }
	public boolean isWaiting() { return waiting; }
	public void setWaiting(boolean b) { waiting = b; }

	public void startOS() throws AssemblerException {
		setRegisterValue(new RegisterType("CS"), 0x10);
		setRegisterValue(new RegisterType("DS"), 0x20);
		assembler.placeVariablesInMemory(memory, decodeAddress("[0]", "DS"), true);
	}

	public void fetch() {
		currentInstructionLineTokens = assembler.getLineTokensWithoutLabel(instructionIndex);
	}

	private int decodeAddress(String addressToken, String defaultSegmentName) throws AssemblerException {
		if (assembler.isVariable(addressToken)) {
			return assembler.getVariable(addressToken).getAddress();
		}
		if (!addressToken.contains("+")) {
			var inside = addressToken.substring(addressToken.indexOf("[") + 1, addressToken.indexOf("]"));
			if (Assembler.isNumber(inside)) {
				var immediateValue = Assembler.getImmediateValue(inside);
				return immediateValue.getIntValue() + (getRegisterValue(new RegisterType(defaultSegmentName)) << 4);
			} else if (inside.contains(":")) {
				var registers = inside.split(":");
				var segment = Assembler.determineRegisterType(registers[0]);
				var offset = Assembler.determineRegisterType(registers[1]);
				return ((getRegisterValue(segment) << 4) + getRegisterValue(offset));
			} else {
				var offset = Assembler.determineRegisterType(inside);
				return getRegisterValue(offset) + getRegisterValue(new RegisterType(defaultSegmentName));
			}
		} else if (addressToken.indexOf("[") == addressToken.lastIndexOf("[")) {
			var inside = addressToken.substring(addressToken.indexOf("[") + 1, addressToken.indexOf("]"));
			if (inside.contains("+")) {
				var registers = new ArrayList<RegisterType>();
				Immediate numberImmediate = null;
				var innerTokens = inside.split("\\+");
				for (var innerToken : innerTokens) {
					if (Assembler.isRegister(innerToken)) {
						registers.add(Assembler.determineRegisterType(innerToken));
					} else {
						numberImmediate = Assembler.getImmediateValue(innerToken);
					}
				}
				var result = 0;
				for (var register : registers) {
					result += getRegisterValue(register);
				}
				result += getRegisterValue(new RegisterType(defaultSegmentName)) << 4;
				result += numberImmediate.getIntValue();
				return result;
			} else {
				var tokenStrings = addressToken.split("\\+");
				var numImmediate = Assembler.getImmediateValue(tokenStrings[1]);
				var reg = Assembler.determineRegisterType(inside);
				return getRegisterValue(reg) + numImmediate.getIntValue()
						+ (getRegisterValue(new RegisterType(defaultSegmentName)) << 4);
			}
		} else {
			var inside1 = addressToken.substring(addressToken.indexOf("[") + 1, addressToken.indexOf("]"));
			var inside2 = addressToken.substring(addressToken.lastIndexOf("[") + 1, addressToken.lastIndexOf("]"));
			var register1 = Assembler.determineRegisterType(inside1);
			var register2 = Assembler.determineRegisterType(inside2);
			var numberImmediate = Assembler.getImmediateValue(addressToken.split("\\+")[1]);
			var result = 0;
			result += numberImmediate.getIntValue();
			result += getRegisterValue(register2);
			result += getRegisterValue(register1);
			result += getRegisterValue(new RegisterType(defaultSegmentName)) << 4;
			return result;
		}
	}

	public void execute() throws EMURunTimeException, AssemblerException {
		LOGGER.fine("Instruction:" + instructionIndex);
		LOGGER.fine(() -> String.valueOf(assembler));
		if (currentInstructionLineTokens == null) {
			LOGGER.fine("No more instructions to execute, program finished.");
			finished = true;
			return;
		}
		switch (currentInstructionLineTokens.get(0).toUpperCase()) {
			case "MOV" -> mov();
			case "SUB" -> sub();
			case "ADD" -> add();
			case "OR" -> {
				or();
				flagRegister[RegisterConstants.CF] = false;
				flagRegister[RegisterConstants.OF] = false;
			}
			case "XOR" -> {
				xor();
				flagRegister[RegisterConstants.CF] = false;
				flagRegister[RegisterConstants.OF] = false;
			}
			case "AND" -> {
				and();
				flagRegister[RegisterConstants.CF] = false;
				flagRegister[RegisterConstants.OF] = false;
			}
			case "NOP" -> nop();
			case "CLC" -> clc();
			case "CLD" -> cld();
			case "ADC" -> adc();
			case "CMP" -> cmp();
			case "JA" -> ja();
			case "JAE" -> jae();
			case "JBE" -> jbe();
			case "JB" -> jb();
			case "JC" -> jc();
			case "JG" -> jg();
			case "JGE" -> jge();
			case "JL" -> jl();
			case "JLE" -> jle();
			case "JMP" -> jmp();
			case "JNE" -> jne();
			case "JNP" -> jnp();
			case "JP" -> jp();
			case "JPO" -> jpo();
			case "STD" -> std();
			case "STC" -> stc();
			case "ROL" -> rol();
			case "ROR" -> ror();
			case "SHL" -> shl();
			case "SHR" -> shr();
			case "SBB" -> sbb();
			case "LOOP" -> loop();
			case "MUL" -> mul();
			case "DIV" -> div();
			case "IMUL" -> imul();
			case "IDIV" -> idiv();
			case "HLT" -> hlt();
			case "LEA" -> lea();
			case "INC" -> inc();
			case "DEC" -> dec();
			case "NOT" -> not();
			case "NEG" -> neg();
			default -> {
				JOptionPane.showMessageDialog(null, "Mnemonic not recognised");
				throw new AssemblerException("Mnemonic not recognised", getTextLineIndex());
			}
		}
		instructionIndex++;
		instructionPointerRegister += 2;
	}

	// Instruction implementations

	private void neg() throws AssemblerException {
		oneOpenardBasicOperation(value -> -value);
	}

	private void not() throws AssemblerException {
		oneOpenardBasicOperation(value -> ~value);
	}

	private void dec() throws AssemblerException {
		oneOpenardBasicOperation(value -> value - 1);
	}

	private void inc() throws AssemblerException {
		oneOpenardBasicOperation(value -> value + 1);
	}

	private void oneOpenardBasicOperation(OneOpenardBasicFunction function) throws AssemblerException {
		verifyTokenCount(2);
		var lhs = currentInstructionLineTokens.get(1);

		LOGGER.fine("oneOpenard:" + currentInstructionLineTokens.get(0) + "lhs:" + lhs);

		if (Assembler.isRegister(lhs)) {
			var leftRegister = Assembler.determineRegisterType(lhs);
			var result = function.execute(getRegisterValue(leftRegister));
			setRegisterValue(leftRegister, result);
		} else if (assembler.isAddress(lhs)) {
			var memAddressOfLeft = decodeAddress(lhs, "DS");
			OperationWidth width = null;
			if (assembler.isVariable(lhs)) {
				assembler.getVariable(lhs);
			} else {
				width = new OperationWidth(Width.EIGHT_BIT);
			}
			var result = function.execute(memory.read(memAddressOfLeft, width));
			memory.write(memAddressOfLeft, result, width);
		} else {
			throw new AssemblerException("invalid operation", getTextLineIndex());
		}
	}

	private void lea() throws AssemblerException, EMURunTimeException {
		verifyTokenCount(3);
		var lhs = currentInstructionLineTokens.get(1);
		var rhs = currentInstructionLineTokens.get(2);

		LOGGER.fine("lea:\nlhs:" + lhs + "\nrhs:" + rhs);

		if (Assembler.isRegister(lhs)) {
			var leftRegister = Assembler.determineRegisterType(lhs);
			if (leftRegister.isIP() || leftRegister.isCS()) {
				throw new AssemblerException("invalid operation", getTextLineIndex());
			}
			if (assembler.isAddress(rhs)) {
				var memAddressOfRight = decodeAddress(rhs, "DS");
				if (memAddressOfRight >= memorySize)
					throw new EMURunTimeException("invalid address ", instructionIndex);
				var width = leftRegister.getWidth();
				if (assembler.isVariable(rhs)) {
					width = assembler.getVariable(rhs).getUnitWidth();
				}
				setRegisterValue(leftRegister, memory.read(memAddressOfRight, width));
			} else {
				throw new AssemblerException("invalid operation", getTextLineIndex());
			}
		} else {
			throw new AssemblerException("invalid operation", getTextLineIndex());
		}
	}

	private void hlt() {
		waiting = true;
	}

	private void div() throws AssemblerException {
		multDiv((openard, width) -> {
			if (width.isEightBit()) {
				var ax = getRegisterValue(new RegisterType("AX"));
				var result = ax / openard;
				var remainder = ax % openard;
				setRegisterValue(new RegisterType("AL"), result);
				setRegisterValue(new RegisterType("AH"), remainder);
			} else {
				var msb = getRegisterValue(new RegisterType("DX"));
				var lsb = getRegisterValue(new RegisterType("AX"));
				var value = lsb + ((long) msb << 32);
				var result = value / openard;
				var remainder = value % openard;
				setRegisterValue(new RegisterType("AX"), (int) result);
				setRegisterValue(new RegisterType("DX"), (int) remainder);
			}
		});
	}

	private void idiv() throws AssemblerException {
		multDiv((openard, width) -> {
			if (width.isEightBit()) {
				var ax = getRegisterValue(new RegisterType("AX"));
				var result = ax / openard;
				var remainder = ax % openard;
				setRegisterValue(new RegisterType("AL"), result);
				setRegisterValue(new RegisterType("AH"), remainder);
			} else {
				var msb = getRegisterValue(new RegisterType("DX"));
				var lsb = getRegisterValue(new RegisterType("AX"));
				var value = lsb + ((long) msb << 32);
				var result = value / openard;
				var remainder = value % openard;
				setRegisterValue(new RegisterType("AX"), (int) result);
				setRegisterValue(new RegisterType("DX"), (int) remainder);
			}
		});
	}

	private void mul() throws AssemblerException {
		multDiv((openard, width) -> {
			if (width.isEightBit()) {
				var result = getRegisterValue(new RegisterType("AL")) * openard;
				setRegisterValue(new RegisterType("AX"), result);
			} else {
				var result = (long) getRegisterValue(new RegisterType("AX")) * openard;
				var lsb = (int) (result & 0x00000000FFFFFFFFl);
				var msb = (int) ((result & 0xFFFFFFFF00000000l) >>> 32);
				setRegisterValue(new RegisterType("AX"), lsb);
				setRegisterValue(new RegisterType("DX"), msb);
			}
		});
	}

	private void imul() throws AssemblerException {
		multDiv((openard, width) -> {
			if (width.isEightBit()) {
				var result = getRegisterValue(new RegisterType("AL")) * openard;
				setRegisterValue(new RegisterType("AX"), result);
			} else {
				var result = (long) getRegisterValue(new RegisterType("AX")) * openard;
				var lsb = (int) (result & 0x00000000FFFFFFFFl);
				var msb = (int) ((result & 0xFFFFFFFF00000000l) >>> 32);
				setRegisterValue(new RegisterType("AX"), lsb);
				setRegisterValue(new RegisterType("DX"), msb);
			}
		});
	}

	private void multDiv(MultiplicationDivisionFunction function) throws AssemblerException {
		verifyTokenCount(2);
		var lhs = currentInstructionLineTokens.get(1);

		LOGGER.fine("mult:" + currentInstructionLineTokens.get(0) + "lhs:" + lhs);

		if (Assembler.isRegister(lhs)) {
			var leftRegister = Assembler.determineRegisterType(lhs);
			function.execute(getRegisterValue(leftRegister), leftRegister.getWidth());
		} else if (assembler.isAddress(lhs)) {
			var memAddressOfLeft = decodeAddress(lhs, "DS");
			OperationWidth width;
			if (assembler.isVariable(lhs)) {
				width = assembler.getVariable(lhs).getUnitWidth();
			} else {
				width = new OperationWidth(Width.EIGHT_BIT);
			}
			function.execute(memory.read(memAddressOfLeft, width), width);
		} else {
			throw new AssemblerException("invalid operation", getTextLineIndex());
		}
	}

	private void stc() throws AssemblerException {
		verifyTokenCount(1);
		flagRegister[RegisterConstants.CF] = true;
	}

	private void std() throws AssemblerException {
		verifyTokenCount(1);
		flagRegister[RegisterConstants.DF] = true;
	}

	private void loop() throws AssemblerException {
		verifyTokenCount(2);
		var label = currentInstructionLineTokens.get(1);
		var cx = getRegisterValue(new RegisterType("CX"));
		setRegisterValue(new RegisterType("CX"), --cx);
		if (cx != 0) {
			jumpTo(label);
		}
	}

	private void jpo() throws AssemblerException {
		verifyTokenCount(2);
		var label = currentInstructionLineTokens.get(1);
		if (!flagRegister[RegisterConstants.PF]) {
			jumpTo(label);
		}
	}

	private void jp() throws AssemblerException {
		verifyTokenCount(2);
		var label = currentInstructionLineTokens.get(1);
		if (flagRegister[RegisterConstants.PF]) {
			jumpTo(label);
		}
	}

	private void jnp() throws AssemblerException {
		verifyTokenCount(2);
		var label = currentInstructionLineTokens.get(1);
		if (!flagRegister[RegisterConstants.PF]) {
			jumpTo(label);
		}
	}

	private void jne() throws AssemblerException {
		verifyTokenCount(2);
		var label = currentInstructionLineTokens.get(1);
		if (!flagRegister[RegisterConstants.ZF]) {
			jumpTo(label);
		}
	}

	private void jmp() throws AssemblerException {
		verifyTokenCount(2);
		var label = currentInstructionLineTokens.get(1);
		jumpTo(label);
	}

	private void jle() throws AssemblerException {
		verifyTokenCount(2);
		var label = currentInstructionLineTokens.get(1);
		if ((flagRegister[RegisterConstants.SF] != flagRegister[RegisterConstants.OF]) || flagRegister[RegisterConstants.ZF]) {
			jumpTo(label);
		}
	}

	private void jl() throws AssemblerException {
		verifyTokenCount(2);
		var label = currentInstructionLineTokens.get(1);
		if (flagRegister[RegisterConstants.SF] != flagRegister[RegisterConstants.OF]) {
			jumpTo(label);
		}
	}

	private void jg() throws AssemblerException {
		verifyTokenCount(2);
		var label = currentInstructionLineTokens.get(1);
		if (!flagRegister[RegisterConstants.ZF] && (flagRegister[RegisterConstants.SF] == flagRegister[RegisterConstants.OF])) {
			jumpTo(label);
		}
	}

	private void jge() throws AssemblerException {
		verifyTokenCount(2);
		var label = currentInstructionLineTokens.get(1);
		if (flagRegister[RegisterConstants.SF] == flagRegister[RegisterConstants.OF]) {
			jumpTo(label);
		}
	}

	private void jc() throws AssemblerException {
		verifyTokenCount(2);
		var label = currentInstructionLineTokens.get(1);
		if (flagRegister[RegisterConstants.CF]) {
			jumpTo(label);
		}
	}

	private void jb() throws AssemblerException {
		verifyTokenCount(2);
		var label = currentInstructionLineTokens.get(1);
		if (flagRegister[RegisterConstants.CF]) {
			jumpTo(label);
		}
	}

	private void jbe() throws AssemblerException {
		verifyTokenCount(2);
		var label = currentInstructionLineTokens.get(1);
		if (flagRegister[RegisterConstants.CF] || flagRegister[RegisterConstants.ZF]) {
			jumpTo(label);
		}
	}

	private void jae() throws AssemblerException {
		verifyTokenCount(2);
		var label = currentInstructionLineTokens.get(1);
		if (!flagRegister[RegisterConstants.CF]) {
			jumpTo(label);
		}
	}

	private void ja() throws AssemblerException {
		verifyTokenCount(2);
		var label = currentInstructionLineTokens.get(1);
		if (!flagRegister[RegisterConstants.CF] && !flagRegister[RegisterConstants.ZF]) {
			jumpTo(label);
		}
	}

	private void jumpTo(String label) {
		var table = assembler.getTagIndexTable();
		var tags = assembler.getTagList();
		var labelInstrIndex = table.get(tags.indexOf(label)).getValue();
		instructionIndex = labelInstrIndex - 1;
		setRegisterValue(new RegisterType("IP"), (labelInstrIndex - 1) * 2);
	}

	private void cmp() throws AssemblerException, EMURunTimeException {
		var result = 0;
		var width = 0;
		verifyTokenCount(3);
		var lhs = currentInstructionLineTokens.get(1);
		var rhs = currentInstructionLineTokens.get(2);

		LOGGER.fine("alu:" + currentInstructionLineTokens.get(0) + "lhs:" + lhs + "\nrhs" + rhs);

		if (Assembler.isRegister(lhs)) {
			var leftRegister = Assembler.determineRegisterType(lhs);
			width = leftRegister.getWidth().getIntWidth();
			if (Assembler.isRegister(rhs)) {
				var rightRegister = Assembler.determineRegisterType(rhs);
				result = getRegisterValue(leftRegister) - getRegisterValue(rightRegister);
			} else if (assembler.isAddress(rhs)) {
				var memAddressOfRight = decodeAddress(rhs, "DS");
				if (memAddressOfRight >= memorySize) {
					throw new EMURunTimeException("invalid address", instructionIndex);
				}
				result = getRegisterValue(leftRegister) - memory.read(memAddressOfRight, leftRegister.getWidth());
			} else if (Assembler.isNumber(rhs)) {
				var rightImmediate = Assembler.getImmediateValue(rhs);
				result = getRegisterValue(leftRegister) - rightImmediate.getIntValue();
			} else {
				throw new AssemblerException("invalid operation", getTextLineIndex());
			}
		} else if (assembler.isAddress(lhs)) {
			var memAddressOfLeft = decodeAddress(lhs, "DS");
			if (memAddressOfLeft >= memorySize) {
				throw new EMURunTimeException("invalid address", instructionIndex);
			}
			if (Assembler.isRegister(rhs)) {
				var rightRegister = Assembler.determineRegisterType(rhs);
				result = memory.read(memAddressOfLeft, rightRegister.getWidth()) - getRegisterValue(rightRegister);
				width = rightRegister.getWidth().getIntWidth();
			} else if (assembler.isAddress(rhs)) {
				throw new AssemblerException("invalid operation", getTextLineIndex());
			} else if (Assembler.isNumber(rhs)) {
				var rightImmediate = Assembler.getImmediateValue(rhs);
				result = memory.read(memAddressOfLeft, rightImmediate.getWidth()) - rightImmediate.getIntValue();
				width = rightImmediate.getWidth().getIntWidth();
			} else {
				throw new AssemblerException("invalid operation", getTextLineIndex());
			}
		} else {
			throw new AssemblerException("invalid operation", getTextLineIndex());
		}

		handleFlags(result, width);
	}

	private void cld() throws AssemblerException {
		verifyTokenCount(1);
		flagRegister[RegisterConstants.DF] = false;
	}

	private void sbb() throws AssemblerException, EMURunTimeException {
		basicALUInstruction((openardLeft, openardRight, w) -> {
			handleOverFlowFlag(openardLeft, openardRight, w);
			var sum = openardLeft - openardRight;
			if (flagRegister[RegisterConstants.CF]) {
				sum--;
			}
			return sum;
		});
	}

	private void adc() throws AssemblerException, EMURunTimeException {
		basicALUInstruction((openardLeft, openardRight, w) -> {
			handleOverFlowFlag(openardLeft, openardRight, w);
			var sum = openardLeft + openardRight;
			if (flagRegister[RegisterConstants.CF]) {
				sum++;
			}
			return sum;
		});
	}

	private void clc() throws AssemblerException {
		verifyTokenCount(1);
		flagRegister[RegisterConstants.CF] = false;
	}

	private void shiftRotate(ShiftRotateFunction function) throws AssemblerException {
		verifyTokenCount(3);
		var lhs = currentInstructionLineTokens.get(1);
		var rhs = currentInstructionLineTokens.get(2);

		LOGGER.fine("rol: " + currentInstructionLineTokens.get(0) + "\nlhs:" + lhs + "\nrhs" + rhs);

		int firstValue;
		int lastValue;
		OperationWidth width;

		if (Assembler.isRegister(lhs)) {
			var leftRegister = Assembler.determineRegisterType(lhs);
			firstValue = getRegisterValue(leftRegister);
			width = leftRegister.getWidth();
			if (Assembler.isRegister(rhs)) {
				var rightRegister = Assembler.determineRegisterType(rhs);
				lastValue = function.execute(getRegisterValue(leftRegister), getRegisterValue(rightRegister), width);
				setRegisterValue(leftRegister, lastValue);
			} else if (assembler.isAddress(rhs)) {
				throw new AssemblerException("invalid operation", getTextLineIndex());
			} else if (Assembler.isNumber(rhs)) {
				var rightImmediate = Assembler.getImmediateValue(rhs);
				lastValue = function.execute(getRegisterValue(leftRegister), rightImmediate.getIntValue(), width);
				setRegisterValue(leftRegister, lastValue);
			} else {
				throw new AssemblerException("invalid operation", getTextLineIndex());
			}
		} else if (assembler.isAddress(lhs)) {
			var memAddressOfLeft = decodeAddress(lhs, "DS");
			if (assembler.isVariable(lhs)) {
				width = assembler.getVariable(lhs).getUnitWidth();
			} else {
				width = null;
			}
			if (Assembler.isRegister(rhs)) {
				var rightRegister = Assembler.determineRegisterType(rhs);
				if (width == null) width = rightRegister.getWidth();
				firstValue = memory.read(memAddressOfLeft, width);
				lastValue = function.execute(firstValue, getRegisterValue(rightRegister), width);
				memory.write(memAddressOfLeft, lastValue, width);
			} else if (assembler.isAddress(rhs)) {
				throw new AssemblerException("invalid operation", getTextLineIndex());
			} else if (Assembler.isNumber(rhs)) {
				var rightImmediate = Assembler.getImmediateValue(rhs);
				if (width == null) width = rightImmediate.getWidth();
				firstValue = memory.read(memAddressOfLeft, width);
				lastValue = function.execute(firstValue, rightImmediate.getIntValue(), width);
				memory.write(memAddressOfLeft, lastValue, width);
			} else {
				throw new AssemblerException("invalid operation", getTextLineIndex());
			}
		} else {
			throw new AssemblerException("invalid operation", getTextLineIndex());
		}
	}

	private void nop() throws AssemblerException {
		verifyTokenCount(1);
	}

	private void mov() throws AssemblerException, EMURunTimeException {
		verifyTokenCount(3);
		var lhs = currentInstructionLineTokens.get(1);
		var rhs = currentInstructionLineTokens.get(2);

		LOGGER.fine("MOV:\nlhs:" + lhs + "\nrhs:" + rhs);

		if (Assembler.isRegister(lhs)) {
			var leftRegister = Assembler.determineRegisterType(lhs);
			if (leftRegister.isIP() || leftRegister.isCS()) {
				throw new AssemblerException("invalid operation", getTextLineIndex());
			}
			if (Assembler.isRegister(rhs)) {
				var rightRegister = Assembler.determineRegisterType(rhs);
				if (rightRegister.isSegmentRegister() && leftRegister.isSegmentRegister()) {
					throw new AssemblerException("invalid operation", getTextLineIndex());
				}
				setRegisterValue(leftRegister, getRegisterValue(rightRegister));
			} else if (assembler.isAddress(rhs)) {
				var memAddressOfRight = decodeAddress(rhs, "DS");
				if (memAddressOfRight >= memorySize)
					throw new EMURunTimeException("invalid address ", instructionIndex);
				var width = leftRegister.getWidth();
				if (assembler.isVariable(rhs)) {
					width = assembler.getVariable(rhs).getUnitWidth();
				}
				setRegisterValue(leftRegister, memory.read(memAddressOfRight, width));
			} else if (Assembler.isNumber(rhs)) {
				if (leftRegister.isSegmentRegister()) {
					throw new AssemblerException("invalid operation", getTextLineIndex());
				}
				var rightImmediate = Assembler.getImmediateValue(rhs);
				setRegisterValue(leftRegister, rightImmediate.getIntValue());
			} else {
				throw new AssemblerException("invalid operation", getTextLineIndex());
			}
		} else if (assembler.isAddress(lhs)) {
			var memAddressOfLeft = decodeAddress(lhs, "DS");
			OperationWidth width = null;
			if (assembler.isVariable(lhs)) {
				width = assembler.getVariable(lhs).getUnitWidth();
			}
			if (memAddressOfLeft >= memorySize)
				throw new EMURunTimeException("invalid address ", instructionIndex);
			if (Assembler.isRegister(rhs)) {
				var rightRegister = Assembler.determineRegisterType(rhs);
				if (width == null) {
					width = rightRegister.getWidth();
				}
				memory.write(memAddressOfLeft, getRegisterValue(rightRegister), width);
			} else if (assembler.isAddress(rhs)) {
				throw new AssemblerException("invalid operation", getTextLineIndex());
			} else if (Assembler.isNumber(rhs)) {
				var rightImmediate = Assembler.getImmediateValue(rhs);
				if (width == null) {
					width = rightImmediate.getWidth();
				}
				memory.write(memAddressOfLeft, rightImmediate.getIntValue(), width);
			} else {
				throw new AssemblerException("invalid operation", getTextLineIndex());
			}
		} else if (Assembler.isNumber(lhs)) {
			throw new AssemblerException("Illegal operation", getTextLineIndex());
		} else {
			throw new AssemblerException("invalid operation", getTextLineIndex());
		}
	}

	private void add() throws AssemblerException, EMURunTimeException {
		basicALUInstruction((openardLeft, openardRight, w) -> {
			handleOverFlowFlag(openardLeft, openardRight, w);
			return openardLeft + openardRight;
		});
	}

	private void and() throws AssemblerException, EMURunTimeException {
		basicALUInstruction((openardLeft, openardRight, w) -> openardLeft & openardRight);
	}

	private void or() throws AssemblerException, EMURunTimeException {
		basicALUInstruction((openardLeft, openardRight, w) -> openardLeft | openardRight);
	}

	private void xor() throws AssemblerException, EMURunTimeException {
		basicALUInstruction((openardLeft, openardRight, w) -> openardLeft ^ openardRight);
	}

	private void sub() throws AssemblerException, EMURunTimeException {
		basicALUInstruction((openardLeft, openardRight, w) -> {
			handleOverFlowFlag(openardLeft, -1 * openardRight, w);
			return openardLeft - openardRight;
		});
	}

	private void basicALUInstruction(BasicALUFunction function) throws AssemblerException, EMURunTimeException {
		verifyTokenCount(3);
		var result = 0;
		var width = 0;
		OperationWidth rWidth = null;
		var lhs = currentInstructionLineTokens.get(1);
		var rhs = currentInstructionLineTokens.get(2);

		LOGGER.fine("alu:" + currentInstructionLineTokens.get(0) + "\nlhs:" + lhs + "\nrhs" + rhs);

		if (Assembler.isRegister(lhs)) {
			var leftRegister = Assembler.determineRegisterType(lhs);
			width = leftRegister.getWidth().getIntWidth();
			if (Assembler.isRegister(rhs)) {
				var rightRegister = Assembler.determineRegisterType(rhs);
				result = function.execute(getRegisterValue(leftRegister), getRegisterValue(rightRegister), leftRegister.getWidth());
				setRegisterValue(leftRegister, result);
			} else if (assembler.isAddress(rhs)) {
				if (assembler.isVariable(rhs)) {
					rWidth = assembler.getVariable(rhs).getUnitWidth();
					width = rWidth.getIntWidth();
				}
				var memAddressOfRight = decodeAddress(rhs, "DS");
				if (memAddressOfRight >= memorySize) {
					throw new EMURunTimeException("invalid address", instructionIndex);
				}
				if (assembler.isVariable(rhs)) {
					rWidth = assembler.getVariable(rhs).getUnitWidth();
				} else {
					rWidth = leftRegister.getWidth();
				}
				result = function.execute(getRegisterValue(leftRegister), memory.read(memAddressOfRight, rWidth), rWidth);
				setRegisterValue(leftRegister, result);
			} else if (Assembler.isNumber(rhs)) {
				var rightImmediate = Assembler.getImmediateValue(rhs);
				result = function.execute(getRegisterValue(leftRegister), rightImmediate.getIntValue(), leftRegister.getWidth());
				setRegisterValue(leftRegister, result);
			} else {
				throw new AssemblerException("invalid operation", getTextLineIndex());
			}
		} else if (assembler.isAddress(lhs)) {
			if (assembler.isVariable(lhs)) {
				rWidth = assembler.getVariable(lhs).getUnitWidth();
			}
			var memAddressOfLeft = decodeAddress(lhs, "DS");
			if (memAddressOfLeft >= memorySize) {
				throw new EMURunTimeException("invalid address", instructionIndex);
			}
			if (Assembler.isRegister(rhs)) {
				var rightRegister = Assembler.determineRegisterType(rhs);
				if (rWidth == null) {
					rWidth = rightRegister.getWidth();
				}
				result = function.execute(memory.read(memAddressOfLeft, rWidth), getRegisterValue(rightRegister), rightRegister.getWidth());
				width = rWidth.getIntWidth();
				memory.write(memAddressOfLeft, result, rWidth);
			} else if (assembler.isAddress(rhs)) {
				throw new AssemblerException("invalid operation", getTextLineIndex());
			} else if (Assembler.isNumber(rhs)) {
				var rightImmediate = Assembler.getImmediateValue(rhs);
				if (rWidth == null) {
					rWidth = rightImmediate.getWidth();
				}
				result = function.execute(memory.read(memAddressOfLeft, rightImmediate.getWidth()), rightImmediate.getIntValue(), rWidth);
				width = rWidth.getIntWidth();
				memory.write(memAddressOfLeft, result, rWidth);
			} else {
				throw new AssemblerException("invalid operation", getTextLineIndex());
			}
		} else {
			throw new AssemblerException("invalid operation", getTextLineIndex());
		}

		handleFlags(result, width);
	}

	private void rol() throws AssemblerException {
		shiftRotate((leftValue, rightValue, width) -> rotate(leftValue, rightValue, width, "left"));
	}

	private void ror() throws AssemblerException {
		shiftRotate((leftValue, rightValue, width) -> rotate(leftValue, rightValue, width, "right"));
	}

	private void shl() throws AssemblerException {
		shiftRotate((leftValue, rightValue, width) -> shift(leftValue, rightValue, width, "left"));
	}

	private void shr() throws AssemblerException {
		shiftRotate((leftValue, rightValue, width) -> shift(leftValue, rightValue, width, "right"));
	}

	private int shift(int openard1, int openard2, OperationWidth width, String direction) {
		LOGGER.fine(String.valueOf(openard2));
		var binary = convertToBinary(openard1, width);
		var msbIndex = width.isEightBit() ? 7 : 15;
		if (direction.equalsIgnoreCase("left")) {
			while (openard2 > 0) {
				flagRegister[RegisterConstants.CF] = binary[msbIndex] == 1;
				for (var i = binary.length - 2; i >= 0; i--) {
					binary[i + 1] = binary[i];
				}
				openard2--;
			}
		} else if (direction.equalsIgnoreCase("right")) {
			while (openard2 > 0) {
				flagRegister[RegisterConstants.CF] = binary[0] == 1;
				for (var i = 1; i < binary.length; i++) {
					binary[i - 1] = binary[i];
				}
				openard2--;
			}
		} else {
			LOGGER.warning("Wrong direction in rotate: " + direction);
			return -1;
		}
		return convertToDecimal(binary);
	}

	private int rotate(int openard1, int openard2, OperationWidth width, String direction) {
		LOGGER.fine(String.valueOf(openard2));
		var binary = convertToBinary(openard1, width);
		var msbIndex = width.isEightBit() ? 7 : 15;
		if (direction.equalsIgnoreCase("left")) {
			while (openard2 > 0) {
				flagRegister[RegisterConstants.CF] = binary[msbIndex] == 1;
				var temp = binary[msbIndex];
				for (var i = binary.length - 2; i >= 0; i--) {
					binary[i + 1] = binary[i];
				}
				binary[0] = temp;
				openard2--;
			}
		} else if (direction.equalsIgnoreCase("right")) {
			while (openard2 > 0) {
				flagRegister[RegisterConstants.CF] = binary[0] == 1;
				var temp = binary[0];
				for (var i = 1; i < binary.length; i++) {
					binary[i - 1] = binary[i];
				}
				binary[msbIndex] = temp;
				openard2--;
			}
		} else {
			LOGGER.warning("Wrong direction in rotate: " + direction);
			return -1;
		}
		return convertToDecimal(binary);
	}

	private void verifyTokenCount(int count) throws AssemblerException {
		if (count != currentInstructionLineTokens.size()) {
			throw new AssemblerException("Wrong parameter count", getTextLineIndex());
		}
	}

	private int convertToDecimal(int[] binary) {
		var decimal = 0;
		for (var i = 0; i < binary.length; i++) {
			decimal += binary[i] << i;
		}
		return decimal;
	}

	private int[] convertToBinary(int decimal, OperationWidth width) {
		var binary = new int[width.isEightBit() ? 8 : 16];
		for (var i = 0; i < binary.length; i++) {
			binary[i] = (decimal >> i) & 1;
		}
		return binary;
	}

	private void handleOverFlowFlag(int left, int right, OperationWidth w) {
		left = getSignedValue(left, w);
		right = getSignedValue(right, w);
		var sum = left + right;
		if (w.isEightBit()) {
			flagRegister[RegisterConstants.OF] = sum < -128 || sum > 127;
		} else {
			flagRegister[RegisterConstants.OF] = sum < -32768 || sum > 32767;
		}
	}

	private int getSignedValue(int unsignedValue, OperationWidth w) {
		if (w.isEightBit()) {
			var sign = 0x01 & (unsignedValue >>> 7);
			if (sign == 1) {
				return unsignedValue | 0xFFFFFF00;
			}
		} else {
			var sign = 0x01 & (unsignedValue >>> 15);
			if (sign == 1) {
				return unsignedValue | 0xFFFF0000;
			}
		}
		return unsignedValue;
	}

	private int setRegisterValue(RegisterType register, int value) {
		var index = register.getRegisterIndex();
		var retValue = RegisterConstants.INVALID_VALUE;
		switch (register.getRegisterClass()) {
			case GENERAL_TYPE -> {
				retValue = getRegisterValue(register);
				generalPurposeRegisters[index] = (byte) ((value & 0xFF00) >>> 8);
				generalPurposeRegisters[index + 1] = (byte) (value & 0xFF);
				return retValue;
			}
			case GENERAL_TYPE_8 -> {
				retValue = getRegisterValue(register);
				generalPurposeRegisters[index] = (byte) (value & 0xFF);
				return retValue;
			}
			case PROGRAM_STATUS_TYPE -> {
				retValue = getRegisterValue(register);
				if (index == RegisterConstants.IP) {
					var msb = ((value & 0xFF00) >>> 8);
					var lsb = (value & 0xFF);
					instructionPointerRegister = (short) (lsb + (msb << 8));
				} else if (index == RegisterConstants.FLAG) {
					var binary = Integer.toBinaryString(value);
					for (var i = 0; i < flagRegister.length; i++) {
						flagRegister[i] = binary.charAt(i) == '1';
					}
				}
				return retValue;
			}
			case SEGMENT_TYPE -> {
				retValue = getRegisterValue(register);
				var msb = ((value & 0xFF00) >>> 8);
				var lsb = (value & 0xFF);
				segmentRegisters[index] = (short) (lsb + (msb << 8));
				return retValue;
			}
			case PTR_INDEX_TYPE -> {
				retValue = getRegisterValue(register);
				ptrIndexRegisters[index] = (short) (value & 0xFFFF);
				return retValue;
			}
			default -> {
				return retValue;
			}
		}
	}

	private int unsignedValue(short word) {
		return word & 0xffff;
	}

	public int getRegisterValue(RegisterType register) {
		var index = register.getRegisterIndex();
		return switch (register.getRegisterClass()) {
			case GENERAL_TYPE_8 -> unsignedValue(generalPurposeRegisters[index]);
			case GENERAL_TYPE -> {
				var MSB = unsignedValue(generalPurposeRegisters[index]);
				var LSB = unsignedValue(generalPurposeRegisters[index + 1]);
				yield (MSB << 8) | LSB;
			}
			case PTR_INDEX_TYPE -> unsignedValue(ptrIndexRegisters[index]);
			case SEGMENT_TYPE -> unsignedValue(segmentRegisters[index]);
			case PROGRAM_STATUS_TYPE -> {
				if (index == RegisterConstants.IP) {
					yield unsignedValue(instructionPointerRegister);
				} else {
					yield getFlagRegValueAsInteger();
				}
			}
		};
	}

	private int getFlagRegValueAsInteger() {
		var value = 0;
		for (var i = 0; i < flagRegister.length; i++) {
			value += 1 << i;
		}
		return value;
	}

	private void initRegisters() {
		flagRegister = new boolean[FLAG_ARRAY_SIZE];
		ptrIndexRegisters = new short[PTR_INDEX_REGS_ARRAY_SIZE];
		generalPurposeRegisters = new byte[GENERAL_REGISTER_ARRAY_SIZE];
		segmentRegisters = new short[SEGMENT_REGISTERS_ARRAY_SIZE];
		instructionPointerRegister = 0;
	}

	private void handleFlags(int result, int width) {
		var pairitySum = 0;
		for (var i = 0; i < width; i++) {
			var particularBit = (result >>> i) & 0x1;
			pairitySum += particularBit;
		}
		var signBit = result >>> width - 1;
		var carryBit = result >>> width;
		carryBit &= 0x1;
		signBit &= 0x1;

		flagRegister[RegisterConstants.SF] = (signBit == 1);
		flagRegister[RegisterConstants.CF] = (carryBit == 1);
		flagRegister[RegisterConstants.PF] = (pairitySum % 2 == 0);
		flagRegister[RegisterConstants.ZF] = (result == 0);
	}

	public void test() throws EMURunTimeException, AssemblerException {
		var ax = new RegisterType(RegisterClass.GENERAL_TYPE, RegisterConstants.AX);
		var bx = new RegisterType(RegisterClass.GENERAL_TYPE, RegisterConstants.BX);
		setRegisterValue(ax, 5);
		LOGGER.fine("ax:" + getRegisterValue(ax));
		LOGGER.fine("bx:" + getRegisterValue(bx));
		fetch();
		execute();
		LOGGER.fine("ax:" + getRegisterValue(ax));
		LOGGER.fine("bx:" + getRegisterValue(bx));
		fetch();
		execute();
		LOGGER.fine("ax:" + getRegisterValue(ax));
		LOGGER.fine("bx:" + getRegisterValue(bx));
		fetch();
		execute();
		LOGGER.fine("ax:" + getRegisterValue(ax));
		LOGGER.fine("bx:" + getRegisterValue(bx));
	}

	public void reset() throws AssemblerException {
		finished = false;
		instructionIndex = 0;
		assembler.reset();
		initRegisters();
		memory.reset(memorySize);
		startOS();
	}

	public int getTextLineIndex() {
		if (instructionIndex >= assembler.getInstructionToTextMapping().size()) return -1;
		return assembler.getInstructionToTextMapping().get(instructionIndex).getValue();
	}
}
