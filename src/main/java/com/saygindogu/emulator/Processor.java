package com.saygindogu.emulator;

import com.saygindogu.emulator.exception.AssemblerException;
import com.saygindogu.emulator.exception.EMURunTimeException;
import com.saygindogu.emulator.functionptrs.BasicALUFunction;
import com.saygindogu.emulator.functionptrs.MultiplicationDivisionFunction;
import com.saygindogu.emulator.functionptrs.OneOpenardBasicFunction;
import com.saygindogu.emulator.functionptrs.ShiftRotateFunction;
import javafx.util.Pair;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 
 * btn ilemleri gerekletiren, mantksal ve aritmetik ilemleri yapan, ilemci'yi temsil eden snf.
 *
 */
public class Processor {
	private static final int GENERAL_REGISTER_ARRAY_SIZE = 8;

	public int getInstructionIndex() {
		return instructionIndex;
	}

	private static final int SEGMENT_REGISTERS_ARRAY_SIZE = 4;
	private static final int FLAG_ARRAY_SIZE = 16;
	private static final int PTR_INDEX_REGS_ARRAY_SIZE = 4;
	public static final String[] SUPPORTED_MNEMONICS_LIST = { "ADD", "MOV", "STC", "STD", "JPO", "JNP", "JNE", "JMP", "JLE", "JL", "JG", "JGE", "JC", "JB", "JBE", "JAE", "JA", "CMP", "CLD", 
		"ADC", "CLC", "ROL", "ROR", "NOP", "AND", "OR", "XOR", "SUB", "NEG", "NOT", "DEC", "INC", "LEA", "HLT", "DIV", "MUL", "IDIV", "IMUL", "SBB", "SHL", "SHR" };

	ArrayList<String> currentInstructionLineTokens;

	// memory is here for easy use
	private Memory memory;
	private int memorySize;
	private boolean finished;
	private boolean waiting;

	// REGISTERS
	private byte[] generalPurposeRegisters;
	/*
	 * 0,1-> AX -> AH,AL 2,3-> BX -> BH,BL 4,5-> CX -> CH,CL 6,7-> DX -> DH,DL
	 */
	private short[] segmentRegisters;
	/*
	 * 0 -> CS 1 -> DS 2 -> SS 3 -> ES
	 */
	private short[] ptrIndexRegisters;
	/*
	 * 0 -> SP 1 -> BP 2 -> SI 3 -> DI
	 */
	private short instructionPointerRegister;
	private boolean[] flagRegister;
	private Assembler assembler;
	private int instructionIndex;

	public Processor(Assembler assembler, int memorySize) {
		this.assembler = assembler;
		init( memorySize);
		currentInstructionLineTokens = null;
	}

	/**
	 * gerekli initilization burada yaplr.
	 * @param memorySize
	 */
	private void init( int memorySize) {
		// init memory
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

	public Memory getMemory() {
		return memory;
	}

	public int getMemorySize() {
		return memorySize;
	}

	public byte[] getGeneralPurposeRegisters() {
		return generalPurposeRegisters;
	}

	public short[] getSegmentRegisters() {
		return segmentRegisters;
	}

	public short[] getPtrIndexRegisters() {
		return ptrIndexRegisters;
	}

	public short getInstructionPointer() {
		return instructionPointerRegister;
	}
	
	/**
	 * DS ve CS deerleri burada verilir, bir nevi OS altrlyormu gibi davranr.
	 * @throws AssemblerException
	 */
	public void startOS() throws AssemblerException{
		setRegisterValue( new RegisterType("CS"), 0x10 );
		setRegisterValue( new RegisterType("DS"), 0x20 );
		assembler.placeVariablesInMemory( memory, decodeAddress( "[0]", "DS"), true);
	}

	/**
	 * Sradaki instruction'u okur
	 */
	public void fetch() {
		currentInstructionLineTokens = assembler
				.getLineTokensWithoutLabel(instructionIndex);
	}

	/**
	 * verilen addressToken'in adres deeri hesaplanr.
	 * @param addressToken
	 * @param defaultSegmentName
	 * @return
	 * @throws AssemblerException
	 */
	private int decodeAddress(String addressToken, String defaultSegmentName)
			throws AssemblerException {
		if ( assembler.isVariable( addressToken) ){
			return assembler.getVariable( addressToken).getAddress();
		}
		if (!addressToken.contains("+")) {
			// direct or register addressing
			String inside = addressToken.substring(
					addressToken.indexOf("[") + 1, addressToken.indexOf("]"));
			if (Assembler.isNumber(inside)) { // direct addressing
				Immediate immediateValue = Assembler.getImmediateValue(inside);
				// implement syntax MOV AX,[1234h]
				return immediateValue.getIntValue()
						+ (getRegisterValue(new RegisterType(defaultSegmentName)) << 4);
			} else if (inside.contains(":")) {
				String[] registers = inside.split(":");
				RegisterType segment = Assembler
						.determineRegisterType(registers[0]);
				RegisterType offset = Assembler
						.determineRegisterType(registers[1]);
				// implement syntax MOV AX,[CS:AX]
				return ((getRegisterValue(segment) << 4) + getRegisterValue(offset));
			} else {
				RegisterType offset = Assembler.determineRegisterType(inside);
				// implement syntax MOV AX,[BX]
				return getRegisterValue(offset)
						+ getRegisterValue(new RegisterType(defaultSegmentName));
			}
		} else if (addressToken.indexOf("[") == addressToken.lastIndexOf("[")) {
			String inside = addressToken.substring(
					addressToken.indexOf("[") + 1, addressToken.indexOf("]"));
			if (inside.contains("+")) {
				ArrayList<RegisterType> registers = new ArrayList<RegisterType>();
				Immediate numberImmediate = null;
				String[] innerTokens = inside.split("+");
				for (String innerToken : innerTokens) {
					if (Assembler.isRegister(innerToken)) {
						registers.add(Assembler
								.determineRegisterType(innerToken));
					} else {
						numberImmediate = Assembler
								.getImmediateValue(innerToken);
					}
				}
				// implement syntax MOV AX,[BP+SI+29]
				int result = 0;
				for (RegisterType register : registers) {
					result += getRegisterValue(register);
				}
				result += getRegisterValue(new RegisterType(defaultSegmentName)) << 4;
				result += numberImmediate.getIntValue();
				return result;
			} else {
				String[] tokenStrings = addressToken.split("+");
				Immediate numImmediate = Assembler
						.getImmediateValue(tokenStrings[1]);
				RegisterType reg = Assembler.determineRegisterType(inside);
				// implement syntax MOV AX,[SI]+1234h
				return getRegisterValue(reg)
						+ numImmediate.getIntValue()
						+ (getRegisterValue(new RegisterType(defaultSegmentName)) << 4);
			}
		} else { // syntax : MOV AX,[SI][BP]+29
			String inside1 = addressToken.substring(
					addressToken.indexOf("[") + 1, addressToken.indexOf("]"));
			String inside2 = addressToken.substring(
					addressToken.lastIndexOf("[") + 1,
					addressToken.lastIndexOf("]"));
			RegisterType register1 = Assembler.determineRegisterType(inside1);
			RegisterType register2 = Assembler.determineRegisterType(inside2);
			Immediate numberImmediate = Assembler
					.getImmediateValue(addressToken.split("+")[1]);
			// implement the syntax
			int result = 0;
			result += numberImmediate.getIntValue();
			result += getRegisterValue(register2);
			result += getRegisterValue(register1);
			result += getRegisterValue(new RegisterType(defaultSegmentName)) << 4;
			return result;
		}
	}

	/**
	 * Sradaki instruction altrlr.
	 * 
	 * @throws EMURunTimeException
	 * @throws AssemblerException
	 */
	public void execute() throws EMURunTimeException, AssemblerException {
		System.out.println("Instruction:" + instructionIndex);
		System.out.println(assembler);
		// TODO ekledikce ekle
		if (currentInstructionLineTokens != null) {
			if (currentInstructionLineTokens.get(0).equalsIgnoreCase("MOV")) {
				mov();
			} else if (currentInstructionLineTokens.get(0).equalsIgnoreCase(
					"SUB")) {
				sub();
			} else if (currentInstructionLineTokens.get(0).equalsIgnoreCase(
					"ADD")) {
				add();
			} else if (currentInstructionLineTokens.get(0).equalsIgnoreCase(
					"OR")) {
				or();
				flagRegister[ RegisterConstants.CF] = false;
				flagRegister[ RegisterConstants.OF] = false;
			} else if (currentInstructionLineTokens.get(0).equalsIgnoreCase(
					"XOR")) {
				xor();
				flagRegister[ RegisterConstants.CF] = false;
				flagRegister[ RegisterConstants.OF] = false;
			} else if (currentInstructionLineTokens.get(0).equalsIgnoreCase(
					"AND")) {
				and();
				flagRegister[ RegisterConstants.CF] = false;
				flagRegister[ RegisterConstants.OF] = false;
			} else if (currentInstructionLineTokens.get(0).equalsIgnoreCase(
					"NOP")) {
				nop();
			} else if (currentInstructionLineTokens.get(0).equalsIgnoreCase(
					"CLC")) {
				clc();
			} else if (currentInstructionLineTokens.get(0).equalsIgnoreCase(
					"CLC")) {
				cld();
			} else if (currentInstructionLineTokens.get(0).equalsIgnoreCase(
					"ADC")) {
				adc();
			}
			else if (currentInstructionLineTokens.get(0).equalsIgnoreCase(
					"CMP")) {
				cmp();
			}
			else if (currentInstructionLineTokens.get(0).equalsIgnoreCase(
					"JA")) {
				ja();
			}
			else if (currentInstructionLineTokens.get(0).equalsIgnoreCase(
					"JAE")) {
				jae();
			}
			else if (currentInstructionLineTokens.get(0).equalsIgnoreCase(
					"JBE")) {
				jbe();
			}
			else if (currentInstructionLineTokens.get(0).equalsIgnoreCase(
					"JB")) {
				jb();
			}
			else if (currentInstructionLineTokens.get(0).equalsIgnoreCase(
					"JC")) {
				jc();
			}
			else if (currentInstructionLineTokens.get(0).equalsIgnoreCase(
					"JG")) {
				jg();
			}
			else if (currentInstructionLineTokens.get(0).equalsIgnoreCase(
					"JGE")) {
				jge();
			}
			else if (currentInstructionLineTokens.get(0).equalsIgnoreCase(
					"JL")) {
				jl();
			}
			else if (currentInstructionLineTokens.get(0).equalsIgnoreCase(
					"JLE")) {
				jle();
			}
			else if (currentInstructionLineTokens.get(0).equalsIgnoreCase(
					"JMP")) {
				jmp();
			}
			else if (currentInstructionLineTokens.get(0).equalsIgnoreCase(
					"JNE")) {
				jne();
			}
			else if (currentInstructionLineTokens.get(0).equalsIgnoreCase(
					"JNP")) {
				jnp();
			}
			else if (currentInstructionLineTokens.get(0).equalsIgnoreCase(
					"JP")) {
				jp();
			}
			else if (currentInstructionLineTokens.get(0).equalsIgnoreCase(
					"JPO")) {
				jpo();
			}
			else if (currentInstructionLineTokens.get(0).equalsIgnoreCase(
					"STD")) {
				std();
			}
			else if (currentInstructionLineTokens.get(0).equalsIgnoreCase(
					"STC")) {
				stc();
			}
			else if (currentInstructionLineTokens.get(0).equalsIgnoreCase(
					"ROL")) {
				rol();
			}
			else if (currentInstructionLineTokens.get(0).equalsIgnoreCase(
					"ROR")) {
				ror();
			}
			else if (currentInstructionLineTokens.get(0).equalsIgnoreCase(
					"SHL")) {
				shl();
			}
			else if (currentInstructionLineTokens.get(0).equalsIgnoreCase(
					"SHR")) {
				shr();
			}
			else if (currentInstructionLineTokens.get(0).equalsIgnoreCase(
					"SBB")) {
				sbb();
			}
			else if (currentInstructionLineTokens.get(0).equalsIgnoreCase(
					"LOOP")) {
				loop();
			}
			else if (currentInstructionLineTokens.get(0).equalsIgnoreCase(
					"MUL")) {
				mul();
			}
			else if (currentInstructionLineTokens.get(0).equalsIgnoreCase(
					"DIV")) {
				div();
			}
			else if (currentInstructionLineTokens.get(0).equalsIgnoreCase(
					"IMUL")) {
				imul();
			}
			else if (currentInstructionLineTokens.get(0).equalsIgnoreCase(
					"IDIV")) {
				idiv();
			}
			else if (currentInstructionLineTokens.get(0).equalsIgnoreCase(
					"HLT")) {
				hlt();
			}
			else if (currentInstructionLineTokens.get(0).equalsIgnoreCase(
					"HLT")) {
				lea();
			}
			else if (currentInstructionLineTokens.get(0).equalsIgnoreCase(
					"INC")) {
				inc();
			}
			else if (currentInstructionLineTokens.get(0).equalsIgnoreCase(
					"DEC")) {
				dec();
			}
			else if (currentInstructionLineTokens.get(0).equalsIgnoreCase(
					"NOT")) {
				not();
			}
			else if (currentInstructionLineTokens.get(0).equalsIgnoreCase(
					"NEG")) {
				neg();
			}
			
			
			else{
				JOptionPane.showMessageDialog( null , "Mnemonic not recognised" );
				throw new AssemblerException( "Mnemonic not recognised");
			}

			instructionIndex++;
			instructionPointerRegister += 2;
		} else {
			System.out.println("Program did not return..");
			finished = true;
			JOptionPane.showMessageDialog( null , "Program did not return but instructions has finished!" );
		}
	}
	
	////////////////////////////////////////////////////////////////////////////////////////
	//instruction trleri
	private void neg() throws AssemblerException {
		oneOpenardBasicOperation( new OneOpenardBasicFunction() {
			
			public int execute(int value) {
				return -value;
			}
		});
		
	}

	private void not() throws AssemblerException {
		oneOpenardBasicOperation( new OneOpenardBasicFunction() {
			
			public int execute(int value) {
				return ~value;
			}
		});
		
	}

	private void dec() throws AssemblerException{
		oneOpenardBasicOperation( new OneOpenardBasicFunction() {
			
			public int execute(int value) {
				return --value;
			}
		});
		
	}

	private void inc() throws AssemblerException{
		oneOpenardBasicOperation( new OneOpenardBasicFunction() {
			
			public int execute(int value) {
				return ++value;
			}
		});
		
	}

	private void oneOpenardBasicOperation( OneOpenardBasicFunction function ) throws AssemblerException{
		// we have 1 openards
		verifyTokenCount( 2);
		String lhs = currentInstructionLineTokens.get(1);

		// debug code
		System.out.println("oneOpenard:" + currentInstructionLineTokens.get(0)
				+ "lhs:" + lhs );

		if (Assembler.isRegister(lhs)) {
			RegisterType leftRegister = Assembler.determineRegisterType(lhs);
			int result = function.execute( getRegisterValue(leftRegister));
			setRegisterValue( leftRegister, result);
			
		} else if (assembler.isAddress(lhs)) {
			int memAddressOfLeft = decodeAddress(lhs, "DS");
			OperationWidth width = null;
			if( assembler.isVariable(lhs)){
				assembler.getVariable( lhs);
			}
			else{
				width = new OperationWidth( RegisterConstants.EIGHT_BIT );
			}
			int result = function.execute( memory.read(memAddressOfLeft, width));
			memory.write(memAddressOfLeft, result, width);
		}else
			throw new AssemblerException("invalid operation at "
					+ getTextLineIndex());
		
	}
	
	private void lea() throws AssemblerException, EMURunTimeException {
		// we have 2 openards
		verifyTokenCount( 3);
		String lhs = currentInstructionLineTokens.get(1);
		String rhs = currentInstructionLineTokens.get(2);
		

		System.out.println("lea:\nlhs:" + lhs + "\nrhs:" + rhs);

		if (Assembler.isRegister(lhs)) {
			RegisterType leftRegister = Assembler.determineRegisterType(lhs);
			if (leftRegister.isIP() || leftRegister.isCS()) {
				throw new AssemblerException("invalid operation"
						+ instructionIndex);
			}
			if ( assembler.isAddress(rhs)) {
				int memAddressOfRight = decodeAddress(rhs, "DS");
				if (memAddressOfRight >= memorySize)
					throw new EMURunTimeException("invalid address ",
							instructionIndex);
				OperationWidth width = leftRegister.getWidth();
				if( assembler.isVariable(rhs)){
					width = assembler.getVariable(rhs).getUnitWidth();
				}
				setRegisterValue(leftRegister, memory.read(memAddressOfRight, width));

			} else
				throw new AssemblerException("invalid operation"
						+ instructionIndex);
		} 
		else throw new AssemblerException("invalid operation" + instructionIndex);
		
	}

	private void hlt() {
		waiting = true;
		
	}

	private void div() throws AssemblerException {
		multDiv( new MultiplicationDivisionFunction() {
			
			public void execute(int openard, OperationWidth width) {
				if( width.isEightBit() ){
					int ax = getRegisterValue( new RegisterType("AX"));
					int result = ax /  openard;
					int remainder = ax % openard;
					setRegisterValue( new RegisterType("AL"), result);
					setRegisterValue( new RegisterType("AH"), remainder);
				}
				else{
					int msb = getRegisterValue( new RegisterType("DX"));
					int lsb = getRegisterValue( new RegisterType("AX"));
					long value = lsb + ((long)msb << 32);
					long result = value / openard;
					long remainder = value % openard;
					setRegisterValue( new RegisterType("AX"), (int)result);
					setRegisterValue( new RegisterType("DX"), (int)remainder);
				}
				
			}
		});
	}
	
	private void idiv() throws AssemblerException {
		//TODO make this signed?
		multDiv( new MultiplicationDivisionFunction() {
			
			public void execute(int openard, OperationWidth width) {
				if( width.isEightBit() ){
					int ax = getRegisterValue( new RegisterType("AX"));
					int result = ax /  openard;
					int remainder = ax % openard;
					setRegisterValue( new RegisterType("AL"), result);
					setRegisterValue( new RegisterType("AH"), remainder);
				}
				else{
					int msb = getRegisterValue( new RegisterType("DX"));
					int lsb = getRegisterValue( new RegisterType("AX"));
					long value = lsb + ((long)msb << 32);
					long result = value / openard;
					long remainder = value % openard;
					setRegisterValue( new RegisterType("AX"), (int)result);
					setRegisterValue( new RegisterType("DX"), (int)remainder);
				}
				
			}
		});
	}
	
	private void mul() throws AssemblerException {
		multDiv( new MultiplicationDivisionFunction() {
			
			public void execute(int openard, OperationWidth width) {
				if( width.isEightBit() ){
					int result = getRegisterValue( new RegisterType("AL")) *  openard;
					setRegisterValue( new RegisterType("AX"), result);
				}
				else{
					long result = getRegisterValue( new RegisterType("AX")) *  openard;
					int msb,lsb;
					lsb = (int)(result & 0x00000000FFFFFFFFl);
					msb = (int)((result & 0xFFFFFFFF00000000l) >>> 32 );
					setRegisterValue( new RegisterType("AX"), lsb);
					setRegisterValue( new RegisterType("DX"), msb);
				}
				
			}
		});
	}
	
	private void imul() throws AssemblerException {
		//TODO make this signed..
		multDiv( new MultiplicationDivisionFunction() {
			
			public void execute(int openard, OperationWidth width) {
				if( width.isEightBit() ){
					int result = getRegisterValue( new RegisterType("AL")) *  openard;
					setRegisterValue( new RegisterType("AX"), result);
				}
				else{
					long result = getRegisterValue( new RegisterType("AX")) *  openard;
					int msb,lsb;
					lsb = (int)(result & 0x00000000FFFFFFFFl);
					msb = (int)((result & 0xFFFFFFFF00000000l) >>> 32 );
					setRegisterValue( new RegisterType("AX"), lsb);
					setRegisterValue( new RegisterType("DX"), msb);
				}
				
			}
		});
	}

	private void multDiv( MultiplicationDivisionFunction function) throws AssemblerException{
		// we have 1 openard
		verifyTokenCount( 2);
		String lhs = currentInstructionLineTokens.get(1);

		// debug code
		System.out.println("mult:" + currentInstructionLineTokens.get(0)
				+ "lhs:" + lhs );

		if (Assembler.isRegister(lhs)) {
			RegisterType leftRegister = Assembler.determineRegisterType(lhs);
			function.execute( getRegisterValue(leftRegister), leftRegister.getWidth() );
			
		} else if (assembler.isAddress(lhs)) {
			int memAddressOfLeft = decodeAddress(lhs, "DS");
			OperationWidth width = null;
			if( assembler.isVariable(lhs) ){
				width = assembler.getVariable(lhs).getUnitWidth();
			}
			else{
				width = new OperationWidth( RegisterConstants.EIGHT_BIT );
			}
			function.execute( memory.read(memAddressOfLeft, width), width );
			
		} else throw new AssemblerException("invalid operation at "
					+ getTextLineIndex());
	}
	
	private void stc() throws AssemblerException{
		verifyTokenCount( 1);
		flagRegister[RegisterConstants.CF] = true;
	}
	
	private void std() throws AssemblerException{
		verifyTokenCount( 1);
		flagRegister[RegisterConstants.DF] = true;
	}
	
	private void loop() throws AssemblerException{
		verifyTokenCount( 2);
		String label = currentInstructionLineTokens.get(1);
		int cx = getRegisterValue(new RegisterType("CX"));
		setRegisterValue( new RegisterType("CX"), --cx);
		if( cx != 0 ){
			jumpTo( label);
		}	
	}
	
	private void jpo() throws AssemblerException{
		verifyTokenCount( 2);
		String label = currentInstructionLineTokens.get(1);
		if( !flagRegister[RegisterConstants.PF] ){
			jumpTo( label);
		}	
	}
	
	private void jp() throws AssemblerException{
		verifyTokenCount( 2);
		String label = currentInstructionLineTokens.get(1);
		if( flagRegister[RegisterConstants.PF] ){
			jumpTo( label);
		}	
	}
	
	private void jnp() throws AssemblerException{
		verifyTokenCount( 2);
		String label = currentInstructionLineTokens.get(1);
		if( !flagRegister[RegisterConstants.PF] ){
			jumpTo( label);
		}	
	}
	
	private void jne() throws AssemblerException{
		verifyTokenCount( 2);
		String label = currentInstructionLineTokens.get(1);
		if( !flagRegister[RegisterConstants.ZF] ){
			jumpTo( label);
		}	
	}
	
	private void jmp() throws AssemblerException{
		verifyTokenCount( 2);
		String label = currentInstructionLineTokens.get(1);
		jumpTo( label);
	}
	
	private void jle() throws AssemblerException{
		verifyTokenCount( 2);
		String label = currentInstructionLineTokens.get(1);
		if( (flagRegister[RegisterConstants.SF] != flagRegister[RegisterConstants.OF]) ||  flagRegister[RegisterConstants.ZF]){
			jumpTo( label);
		}	
	}
	
	private void jl() throws AssemblerException{
		verifyTokenCount( 2);
		String label = currentInstructionLineTokens.get(1);
		if( flagRegister[RegisterConstants.SF] != flagRegister[RegisterConstants.OF] ){
			jumpTo( label);
		}	
	}
	
	private void jg() throws AssemblerException {
		verifyTokenCount( 2);
		String label = currentInstructionLineTokens.get(1);
		if( !flagRegister[RegisterConstants.ZF] &&  (flagRegister[RegisterConstants.SF] == flagRegister[RegisterConstants.OF])){
			jumpTo( label);
		}	
	}
	
	private void jge() throws AssemblerException {
		verifyTokenCount( 2);
		String label = currentInstructionLineTokens.get(1);
		if( flagRegister[RegisterConstants.SF] == flagRegister[RegisterConstants.OF] ){
			jumpTo( label);
		}	
	}
	
	private void jc() throws AssemblerException {
		verifyTokenCount( 2);
		String label = currentInstructionLineTokens.get(1);
		if( flagRegister[RegisterConstants.CF] ){
			jumpTo( label);
		}	
	}
	
	private void jb() throws AssemblerException {
		verifyTokenCount( 2);
		String label = currentInstructionLineTokens.get(1);
		if( flagRegister[RegisterConstants.CF] ){
			jumpTo( label);
		}	
	}
	
	private void jbe() throws AssemblerException {
		verifyTokenCount( 2);
		String label = currentInstructionLineTokens.get(1);
		if( flagRegister[RegisterConstants.CF] || flagRegister[RegisterConstants.ZF]){
			jumpTo( label);
		}	
	}
	
	private void jae() throws AssemblerException {
		verifyTokenCount( 2);
		String label = currentInstructionLineTokens.get(1);
		if( !flagRegister[RegisterConstants.CF] ){
			jumpTo( label);
		}	
	}
	
	private void ja() throws AssemblerException {
		verifyTokenCount( 2);
		String label = currentInstructionLineTokens.get(1);
		if( !flagRegister[RegisterConstants.CF] && !flagRegister[RegisterConstants.ZF]){
			jumpTo( label);
		}	
	}

	private void jumpTo(String label) {
		List<Pair<String,Integer>> table = assembler.getTagIndexTable();
		List<String> tags = assembler.getTagList();
		int labelInstrIndex = table.get( tags.indexOf(label)).getValue();
		instructionIndex = labelInstrIndex - 1;
		setRegisterValue( new RegisterType("IP"), (labelInstrIndex - 1)*2 ); //TODO dikkatli ol
	}

	private void cmp() throws AssemblerException, EMURunTimeException{
		int result = 0;
		int width = 0;
		// we have 2 openards
		verifyTokenCount( 3);
		String lhs = currentInstructionLineTokens.get(1);
		String rhs = currentInstructionLineTokens.get(2);

		// debug code
		System.out.println("alu:" + currentInstructionLineTokens.get(0)
				+ "lhs:" + lhs + "\nrhs" + rhs);

		if (Assembler.isRegister(lhs)) {
			RegisterType leftRegister = Assembler.determineRegisterType(lhs);
			width = leftRegister.getWidth().getIntWidth();
			if (Assembler.isRegister(rhs)) {
				RegisterType rightRegister = Assembler
						.determineRegisterType(rhs);
				result = getRegisterValue(leftRegister) -
						getRegisterValue(rightRegister);
			} else if (assembler.isAddress(rhs)) {
				int memAddressOfRight = decodeAddress(rhs, "DS");
				if (memAddressOfRight >= memorySize) {
					throw new EMURunTimeException("invalid address",
							instructionIndex);
				}
				result = getRegisterValue(leftRegister) -
								memory.read(memAddressOfRight,leftRegister.getWidth());
			} else if (Assembler.isNumber(rhs)) {
				Immediate rightImmediate = Assembler.getImmediateValue(rhs);
				result = getRegisterValue(leftRegister) - rightImmediate.getIntValue();
			} else
				throw new AssemblerException("invalid operation at "
						+ instructionIndex);
		} else if (assembler.isAddress(lhs)) {
			int memAddressOfLeft = decodeAddress(lhs, "DS");
			if (memAddressOfLeft >= memorySize) {
				throw new EMURunTimeException("invalid address",
						instructionIndex);
			}
			if (Assembler.isRegister(rhs)) {
				RegisterType rightRegister = Assembler
						.determineRegisterType(rhs);
				result = memory.read(memAddressOfLeft,rightRegister.getWidth()) - getRegisterValue(rightRegister);
				width = rightRegister.getWidth().getIntWidth();

			} else if (assembler.isAddress(rhs)) {
				throw new AssemblerException("invalid operation at "
						+ instructionIndex);
			} else if (Assembler.isNumber(rhs)) {
				Immediate rightImmediate = Assembler.getImmediateValue(rhs);
				result = memory.read(memAddressOfLeft,
						rightImmediate.getWidth()) - rightImmediate
						.getIntValue();
				width = rightImmediate.getWidth().getIntWidth();
			} else
				throw new AssemblerException("invalid operation at "
						+ instructionIndex);
		} else
			throw new AssemblerException("invalid operation at "
					+ instructionIndex);
		
		handleFlags( result, width);
	}

	private void cld() throws AssemblerException {
		verifyTokenCount( 1);
		flagRegister[RegisterConstants.DF] = false;
	}

	private void sbb() throws AssemblerException, EMURunTimeException {
		basicALUInstruction(new BasicALUFunction() {

			public int execute(int openardLeft, int openardRight, OperationWidth w) {
				handleOverFlowFlag( openardLeft, openardRight, w);
				int sum = openardLeft - openardRight;
				if (flagRegister[RegisterConstants.CF]) {
					sum--;
				}
				return sum;
			}
		});
	}
	
	private void adc() throws AssemblerException, EMURunTimeException {
		basicALUInstruction(new BasicALUFunction() {

			public int execute(int openardLeft, int openardRight, OperationWidth w) {
				handleOverFlowFlag( openardLeft, openardRight, w);
				int sum = openardLeft + openardRight;
				if (flagRegister[RegisterConstants.CF]) {
					sum++;
				}
				return sum;
			}
		});
	}

	private void clc() throws AssemblerException {
		verifyTokenCount( 1);
		flagRegister[RegisterConstants.CF] = false;

	}

	private void dummy() throws AssemblerException {
		// we have 2 openards
		verifyTokenCount( 3);
		String lhs = currentInstructionLineTokens.get(1);
		String rhs = currentInstructionLineTokens.get(2);

		// debug code
		System.out.println("add:" + currentInstructionLineTokens.get(0)
				+ "lhs:" + lhs + "\nrhs" + rhs);

		if (Assembler.isRegister(lhs)) {
			RegisterType leftRegister = Assembler.determineRegisterType(lhs);
			if (Assembler.isRegister(rhs)) {
				RegisterType rightRegister = Assembler
						.determineRegisterType(rhs);
			} else if (assembler.isAddress(rhs)) {
				int memAddressOfRight = decodeAddress(rhs, "DS");
			} else if (Assembler.isNumber(rhs)) {
				Immediate rightImmediate = Assembler.getImmediateValue(rhs);
			} else
				throw new AssemblerException("invalid operation at "
						+ getTextLineIndex() );
		} else if (assembler.isAddress(lhs)) {
			int memAddressOfLeft = decodeAddress(lhs, "DS");
			if (Assembler.isRegister(rhs)) {
				RegisterType rightRegister = Assembler
						.determineRegisterType(rhs);
			} else if (assembler.isAddress(rhs)) {
				throw new AssemblerException("invalid operation at "
						+ getTextLineIndex());
			} else if (Assembler.isNumber(rhs)) {
				Immediate rightImmediate = Assembler.getImmediateValue(rhs);
			} else
				throw new AssemblerException("invalid operation at "
						+ getTextLineIndex());
		} else if (Assembler.isNumber(lhs)) {
			Immediate leftImmediate = Assembler.getImmediateValue(lhs);
			if (Assembler.isRegister(rhs)) {
				RegisterType rightRegister = Assembler
						.determineRegisterType(rhs);
			} else if (assembler.isAddress(rhs)) {
				int memAddressOfRight = decodeAddress(rhs, "DS");
			} else if (Assembler.isNumber(rhs)) {
				throw new AssemblerException("Two immediate oppenards: "
						+ getTextLineIndex());
			} else
				throw new AssemblerException("invalid operation at "
						+ getTextLineIndex());
		} else
			throw new AssemblerException("invalid operation at "
					+ getTextLineIndex());
	}
	
	private void shiftRotate( ShiftRotateFunction function ) throws AssemblerException {
		// we have 2 openards
		verifyTokenCount( 3);
		String lhs = currentInstructionLineTokens.get(1);
		String rhs = currentInstructionLineTokens.get(2);

		// debug code
		System.out.println("rol: " + currentInstructionLineTokens.get(0)
				+ "\nlhs:" + lhs + "\nrhs" + rhs);
		
		int firstValue;
		int lastValue;
		OperationWidth width;

		if (Assembler.isRegister(lhs)) {
			RegisterType leftRegister = Assembler.determineRegisterType(lhs);
			firstValue = getRegisterValue(leftRegister);
			width = leftRegister.getWidth();
			if (Assembler.isRegister(rhs)) {
				RegisterType rightRegister = Assembler.determineRegisterType(rhs);
				lastValue = function.execute( getRegisterValue(leftRegister), getRegisterValue(rightRegister), width );
				setRegisterValue(leftRegister, lastValue);
			} else if (assembler.isAddress(rhs)) {
				throw new AssemblerException("invalid operation at "
						+ getTextLineIndex() );
			} else if (Assembler.isNumber(rhs)) {
				Immediate rightImmediate = Assembler.getImmediateValue(rhs);
				lastValue = function.execute( getRegisterValue(leftRegister), rightImmediate.getIntValue(), width);
				setRegisterValue(leftRegister, lastValue);
			} else
				throw new AssemblerException("invalid operation at "
						+ getTextLineIndex() );
		} else if (assembler.isAddress(lhs)) {
			int memAddressOfLeft = decodeAddress(lhs, "DS");
			if( assembler.isVariable(lhs) ){
				width = assembler.getVariable(lhs).getUnitWidth();
			}
			else{
				width = null;
			}
			if (Assembler.isRegister(rhs)) {
				RegisterType rightRegister = Assembler.determineRegisterType(rhs);
				if(width == null) width = rightRegister.getWidth();
				firstValue = memory.read(memAddressOfLeft, width);
				lastValue = function.execute( firstValue, getRegisterValue(rightRegister), width);
				memory.write(memAddressOfLeft, lastValue, width);
			} else if (assembler.isAddress(rhs)) {
				throw new AssemblerException("invalid operation at "
						+ instructionIndex);
			} else if (Assembler.isNumber(rhs)) {
				Immediate rightImmediate = Assembler.getImmediateValue(rhs);
				if(width == null) width = rightImmediate.getWidth();
				firstValue = memory.read(memAddressOfLeft, width);
				lastValue = function.execute( firstValue, rightImmediate.getIntValue(), width);
				memory.write(memAddressOfLeft, lastValue, width);
			} else
				throw new AssemblerException("invalid operation at "
						+ getTextLineIndex()  );
		} else
			throw new AssemblerException("invalid operation at " +
					getTextLineIndex() );
	
		//overflow flag
		flagRegister[RegisterConstants.OF] = getSignedValue(firstValue, width) * getSignedValue( lastValue, width) < 0;
	}
	
	private void nop() throws AssemblerException {
		verifyTokenCount( 1);
	}

	private void mov() throws AssemblerException, EMURunTimeException {
		// we have 2 openards
		verifyTokenCount( 3);
		String lhs = currentInstructionLineTokens.get(1);
		String rhs = currentInstructionLineTokens.get(2);
		
	
		System.out.println("MOV:\nlhs:" + lhs + "\nrhs:" + rhs);
	
		if (Assembler.isRegister(lhs)) {
			RegisterType leftRegister = Assembler.determineRegisterType(lhs);
			if (leftRegister.isIP() || leftRegister.isCS()) {
				throw new AssemblerException("invalid operation"
						+ instructionIndex);
			}
			if (Assembler.isRegister(rhs)) {
				RegisterType rightRegister = Assembler
						.determineRegisterType(rhs);
				if (rightRegister.isSegmentRegister()
						&& leftRegister.isSegmentRegister()) {
					throw new AssemblerException("invalid operation"
							+ instructionIndex);
				}
				setRegisterValue(leftRegister, getRegisterValue(rightRegister));
			} else if (assembler.isAddress(rhs)) {
				int memAddressOfRight = decodeAddress(rhs, "DS");
				if (memAddressOfRight >= memorySize)
					throw new EMURunTimeException("invalid address ",
							instructionIndex);
				OperationWidth width = leftRegister.getWidth();
				if( assembler.isVariable(rhs)){
					width = assembler.getVariable(rhs).getUnitWidth();
				}
				setRegisterValue(leftRegister, memory.read(memAddressOfRight, width));
	
			} else if (Assembler.isNumber(rhs)) {
				if (leftRegister.isSegmentRegister()) {
					throw new AssemblerException("invalid operation"
							+ instructionIndex);
				}
				Immediate rightImmediate = Assembler.getImmediateValue(rhs);
				setRegisterValue(leftRegister, rightImmediate.getIntValue());
			} else
				throw new AssemblerException("invalid operation"
						+ instructionIndex);
		} else if (assembler.isAddress(lhs)) {
			int memAddressOfLeft = decodeAddress(lhs, "DS");
			OperationWidth width = null;
			if( assembler.isVariable(lhs)){
				width = assembler.getVariable(lhs).getUnitWidth();
			}
			if (memAddressOfLeft >= memorySize)
				throw new EMURunTimeException("invalid address ",
						instructionIndex);
			if (Assembler.isRegister(rhs)) {
				RegisterType rightRegister = Assembler
						.determineRegisterType(rhs);
				if( width == null){
					width = rightRegister.getWidth();
				}
				memory.write(memAddressOfLeft, getRegisterValue(rightRegister),
						width);
			} else if (assembler.isAddress(rhs)) {
				throw new AssemblerException("invalid operation at "
						+ instructionIndex);
			} else if (Assembler.isNumber(rhs)) {
				Immediate rightImmediate = Assembler.getImmediateValue(rhs);
				if( width == null){
					width = rightImmediate.getWidth();
				}
				memory.write(memAddressOfLeft, rightImmediate.getIntValue(), width);
			} else
				throw new AssemblerException("invalid operation"
						+ instructionIndex);
		} else if (Assembler.isNumber(lhs)) {
			throw new AssemblerException("Illegal operation" + instructionIndex);
		} else
			throw new AssemblerException("invalid operation" + instructionIndex);
	}

	private void add() throws AssemblerException, EMURunTimeException {
		basicALUInstruction(new BasicALUFunction() {
	
			public int execute(int openardLeft, int openardRight, OperationWidth w) {
				handleOverFlowFlag( openardLeft, openardRight, w);
				return openardLeft + openardRight;
			}
		});
	}

	private void and() throws AssemblerException, EMURunTimeException {
		basicALUInstruction(new BasicALUFunction() {
	
			public int execute(int openardLeft, int openardRight, OperationWidth w) {
				return openardLeft & openardRight;
			}
		});
	}

	private void or() throws AssemblerException, EMURunTimeException {
		basicALUInstruction(new BasicALUFunction() {
	
			public int execute(int openardLeft, int openardRight, OperationWidth w) {
				return openardLeft | openardRight;
			}
		});
	}

	private void xor() throws AssemblerException, EMURunTimeException {
		basicALUInstruction(new BasicALUFunction() {
	
			public int execute(int openardLeft, int openardRight, OperationWidth w) {
				return openardLeft ^ openardRight;
			}
		});
	}

	private void sub() throws AssemblerException, EMURunTimeException {
		basicALUInstruction(new BasicALUFunction() {
	
			public int execute(int openardLeft, int openardRight, OperationWidth w) {
				handleOverFlowFlag( openardLeft, -1 * openardRight, w);
				return openardLeft - openardRight;
			}			
		});
	}

	private void basicALUInstruction(BasicALUFunction function)
			throws AssemblerException, EMURunTimeException {
		verifyTokenCount( 3);
		int result = 0;
		int width = 0;
		OperationWidth rWidth = null;
		// we have 2 openards
		String lhs = currentInstructionLineTokens.get(1);
		String rhs = currentInstructionLineTokens.get(2);
	
		// debug code
		System.out.println("alu:" + currentInstructionLineTokens.get(0)
				+ "\nlhs:" + lhs + "\nrhs" + rhs);
	
		if (Assembler.isRegister(lhs)) {
			RegisterType leftRegister = Assembler.determineRegisterType(lhs);
			width = leftRegister.getWidth().getIntWidth();
			if (Assembler.isRegister(rhs)) {
				RegisterType rightRegister = Assembler
						.determineRegisterType(rhs);
				result = function.execute(getRegisterValue(leftRegister),
						getRegisterValue(rightRegister), leftRegister.getWidth());
				setRegisterValue(leftRegister, result);
			} else if (assembler.isAddress(rhs)) {
				if( assembler.isVariable(rhs)){
					rWidth = assembler.getVariable(rhs).getUnitWidth();
					width = rWidth.getIntWidth();
				}
				int memAddressOfRight = decodeAddress(rhs, "DS");
				if (memAddressOfRight >= memorySize) {
					throw new EMURunTimeException("invalid address",
							instructionIndex);
				}
				if( assembler.isVariable(rhs)){
					rWidth = assembler.getVariable(rhs).getUnitWidth();
				}
				else{
					rWidth = leftRegister.getWidth();
				}
				result = function
						.execute( getRegisterValue(leftRegister),memory.read(memAddressOfRight,rWidth),rWidth);
				setRegisterValue(leftRegister, result);
			} else if (Assembler.isNumber(rhs)) {
				Immediate rightImmediate = Assembler.getImmediateValue(rhs);
				result = function.execute(getRegisterValue(leftRegister),
						rightImmediate.getIntValue(), leftRegister.getWidth());
				setRegisterValue(leftRegister, result);
			} else
				throw new AssemblerException("invalid operation at "
						+ instructionIndex);
		} else if (assembler.isAddress(lhs)) {
			if( assembler.isVariable(lhs)){
				rWidth = assembler.getVariable(lhs).getUnitWidth();
			}
			int memAddressOfLeft = decodeAddress(lhs, "DS");
			if (memAddressOfLeft >= memorySize) {
				throw new EMURunTimeException("invalid address",
						instructionIndex);
			}
			if (Assembler.isRegister(rhs)) {
				
				RegisterType rightRegister = Assembler
						.determineRegisterType(rhs);
				if( rWidth == null){
					rWidth = rightRegister.getWidth();
				}
				result = function
						.execute(
								memory.read(memAddressOfLeft,
										rWidth),
								getRegisterValue(rightRegister), rightRegister.getWidth());
				width = rWidth.getIntWidth();
				memory.write(memAddressOfLeft, result, rWidth);
	
			} else if (assembler.isAddress(rhs)) {
				throw new AssemblerException("invalid operation at "
						+ instructionIndex);
			} else if (Assembler.isNumber(rhs)) {
				Immediate rightImmediate = Assembler.getImmediateValue(rhs);
				if( rWidth == null){
					rWidth = rightImmediate.getWidth();
				}
				result = function.execute(memory.read(memAddressOfLeft,
						rightImmediate.getWidth()), rightImmediate
						.getIntValue(), rWidth);
				width = rWidth.getIntWidth();
				memory.write(memAddressOfLeft, result,
						rWidth);
			} else
				throw new AssemblerException("invalid operation at "
						+ instructionIndex);
		} else
			throw new AssemblerException("invalid operation at "
					+ instructionIndex);
		
		handleFlags( result, width);
	}

	private void rol() throws AssemblerException{
		shiftRotate( new ShiftRotateFunction() {
			
			public int execute(int leftValue, int rightValue, OperationWidth width) {
				return rotate( leftValue, rightValue, width, "left");
			}
		});
	}
	
	private void ror() throws AssemblerException{
		shiftRotate( new ShiftRotateFunction() {
			
			public int execute(int leftValue, int rightValue, OperationWidth width) {
				return rotate( leftValue, rightValue, width, "right");
			}
		});
	}
	
	private void shl() throws AssemblerException{
		shiftRotate( new ShiftRotateFunction() {
			
			public int execute(int leftValue, int rightValue, OperationWidth width) {
				return shift( leftValue, rightValue, width, "left");
			}
		});
	}
	
	private void shr() throws AssemblerException{
		shiftRotate( new ShiftRotateFunction() {
			
			public int execute(int leftValue, int rightValue, OperationWidth width) {
				return shift( leftValue, rightValue, width, "right");
			}
		});
	}
	
	private int shift(int openard1, int openard2,
			OperationWidth width, String direction) {
		System.out.println( openard2);
		int[] binary = convertToBinary( openard1, width);
		int temp;
		int msbIndex = width.isEightBit() ? 7 : 15;
		if( direction.equalsIgnoreCase("left") ){
			while( openard2 > 0){
				flagRegister[RegisterConstants.CF] = binary[msbIndex] == 1;
				for( int i = binary.length - 2; i >= 0; i-- ){
					binary[i+1] = binary[i];
				}
				openard2--;
			}
		}
		else if( direction.equalsIgnoreCase( "right")){
			while( openard2 > 0){
				flagRegister[RegisterConstants.CF] = binary[0] == 1;
				for( int i = 1; i < binary.length; i++ ){
					binary[i-1] = binary[i];
				}
				openard2--;
			}
		}
		else{
			System.out.println( "Wrong direction in rotate: " + direction);
			return -1;
		}
		return convertToDecimal( binary);
	}

	private int rotate(int openard1, int openard2, OperationWidth width,
			String direction) {
		System.out.println( openard2);
		int[] binary = convertToBinary( openard1, width);
		int temp;
		int msbIndex = width.isEightBit() ? 7 : 15;
		if( direction.equalsIgnoreCase("left") ){
			while( openard2 > 0){
				flagRegister[RegisterConstants.CF] = binary[msbIndex] == 1;
				temp = binary[ msbIndex ];
				for( int i = binary.length - 2; i >= 0; i-- ){
					binary[i+1] = binary[i];
				}
				binary[0] = temp;
				openard2--;
			}
		}
		else if( direction.equalsIgnoreCase( "right")){
			while( openard2 > 0){
				flagRegister[RegisterConstants.CF] = binary[0] == 1;
				temp = binary[ 0 ];
				for( int i = 1; i < binary.length; i++ ){
					binary[i-1] = binary[i];
				}
				binary[msbIndex] = temp;
				openard2--;
			}
		}
		else{
			System.out.println( "Wrong direction in rotate: " + direction);
			return -1;
		}
		return convertToDecimal( binary);
	}
	
	//////////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * instruction'un parametre saysnn uygun olup olmad kontrol edilir.
	 * @param count
	 * @throws AssemblerException
	 */
	private void verifyTokenCount(int count) throws AssemblerException {
		if( count != currentInstructionLineTokens.size() ){
			throw new AssemblerException( "Wrong parameter count ");
		}
		
	}

	/**
	 * 
	 * @param binary halde gelen deer decimal hale dndrlr
	 * @return edilir.
	 */ 
	private int convertToDecimal(int[] binary) {
		int decimal = 0;
		for( int i = 0; i < binary.length; i++){
			decimal += binary[i] << i;
		}
		return decimal;
	}

	/**
	 * decimal halde gelen say binary hale dndrr ve return eder.
	 * @param decimal 
	 * @param width
	 * @return
	 */
	private int[] convertToBinary(int decimal, OperationWidth width) {
		int[] binary;
		if( width.isEightBit()){
			binary = new int[8];
		}
		else{
			binary = new int[16];
		}
		for( int i = 0; i < binary.length; i++){
			binary[i] = (decimal >> i) & 1;
		}
		return binary;
	}

	/**
	 * Overflow flag deeri gelen inputlara gre deerlendirilerek set edilir.
	 * @param left
	 * @param right
	 * @param w
	 */
	private void handleOverFlowFlag(int left, int right, OperationWidth w) {
		left = getSignedValue( left, w);
		right = getSignedValue( right, w);
		int sum = left + right;
		if( w.isEightBit() ){
			flagRegister[ RegisterConstants.OF] = sum < -128 || sum > 127;
		}
		else{
			flagRegister[ RegisterConstants.OF] = sum < -32768 || sum > 32767;
		}
	}
	
	/**
	 * @param unsignedValue'nun signed hali
	 * @param gelen genilik deerine gre hesaplanr ve
	 * @return edilir.
	 */
	private int getSignedValue(int unsignedValue, OperationWidth w) {
		int sign;
		if( w.isEightBit() ){
			sign = 0x01 & (unsignedValue >>> 7);
			if( sign == 1){
				return unsignedValue | 0xFFFFFF00;
			}
		}
		else{
			sign = 0x01 & (unsignedValue >>> 15);
			if( sign == 1){
				return unsignedValue | 0xFFFF0000;
			}
		}
		return unsignedValue;
	}

	/**
	 * returns the old value of the register
	 */
	private int setRegisterValue(RegisterType register, int value) {
		int index = register.getRegisterIndex();
		int retValue = RegisterConstants.INVALID_VALUE;
		switch (register.getRegisterClass()) {
		case RegisterConstants.GENERAL_TYPE:
			retValue = getRegisterValue(register);
			generalPurposeRegisters[index] = (byte) ((value & 0xFF00) >>> 8);// MSB
			generalPurposeRegisters[index + 1] = (byte) (value & 0xFF);// LSB
			return retValue;
		case RegisterConstants.GENERAL_TYPE_8:
			retValue = getRegisterValue(register);
			generalPurposeRegisters[index] = (byte) (value & 0xFF);
			return retValue;
		case RegisterConstants.PROGRAM_STATUS_TYPE:
			retValue = getRegisterValue(register);
			if (index == RegisterConstants.IP) {
				int msb = ((value & 0xFF00) >>> 8);// MSB
				int lsb = (value & 0xFF);// LSB
				instructionPointerRegister = (short)(lsb + (msb << 8));
			} else if (index == RegisterConstants.FLAG) {
				String binary = Integer.toBinaryString(value);
				for (int i = 0; i < flagRegister.length; i++) {
					flagRegister[i] = binary.charAt(i) == '1';
				}
			}
			return retValue;
		case RegisterConstants.SEGMENT_TYPE:
			retValue = getRegisterValue(register);
			int msb = ((value & 0xFF00) >>> 8);// MSB
			int lsb = (value & 0xFF);// LSB
			segmentRegisters[index] = (short)(lsb + (msb << 8));
			return retValue;
		case RegisterConstants.PTR_INDEX_TYPE:
			retValue = getRegisterValue(register);
			ptrIndexRegisters[index] = (short) (value & 0xFFFF);
			return retValue;
		default:
			return retValue;
		}
	}

	private int unsignedValue(short word) {
		return word & 0xffff;
	}

	/**
	 * Bir register'in deeri okunur.
	 * @param register
	 * @return
	 */
	public int getRegisterValue(RegisterType register) {
		int index = register.getRegisterIndex();
		switch (register.getRegisterClass()) {
		case RegisterConstants.GENERAL_TYPE_8:
			return unsignedValue(generalPurposeRegisters[index]);
		case RegisterConstants.GENERAL_TYPE:
			int MSB = unsignedValue(generalPurposeRegisters[index]);
			int LSB = unsignedValue(generalPurposeRegisters[index + 1]);
			return ((MSB << 8) | LSB);
		case RegisterConstants.PTR_INDEX_TYPE:
			return unsignedValue(ptrIndexRegisters[index]);
		case RegisterConstants.SEGMENT_TYPE:
			return unsignedValue(segmentRegisters[index]);
		case RegisterConstants.PROGRAM_STATUS_TYPE:
			if (index == RegisterConstants.IP) {
				return unsignedValue(instructionPointerRegister);
			} else {
				return getFlagRegValueAsInteger();
			}
		default:
			return RegisterConstants.INVALID_VALUE;
		}
	}

	/**
	 * @return flag register'i 16 bitlik bir say olarak return edilir ( int tipinde)
	 */
	private int getFlagRegValueAsInteger() {
		int value = 0;
		for (int i = 0; i < flagRegister.length; i++) {
			value += 1 << i;
		}
		return value;
	}

	/**
	 * register'lara ilk deer olan 0 deeri yazlr.
	 */
	private void initRegisters() {
		flagRegister = new boolean[FLAG_ARRAY_SIZE];
		ptrIndexRegisters = new short[PTR_INDEX_REGS_ARRAY_SIZE];
		generalPurposeRegisters = new byte[GENERAL_REGISTER_ARRAY_SIZE];
		segmentRegisters = new short[SEGMENT_REGISTERS_ARRAY_SIZE];
		instructionPointerRegister = 0;
	}

	/**
	 * @param result deikenine gre flag deerleri set edilir.
	 * @param width
	 */
	private void handleFlags(int result, int width) {
		int pairitySum = 0;
		for( int i = 0; i < width; i++){
			int particularBit = (result >>> i) & 0x1;
			pairitySum += particularBit;
		}
		int signBit = result >>> width - 1;
		int carryBit = result >>> width;
		carryBit &= 0x1;
		signBit &= 0x1;
		
		flagRegister[ RegisterConstants.SF] = (signBit == 1);
		flagRegister[ RegisterConstants.CF] = (carryBit == 1);
		flagRegister[ RegisterConstants.PF] = (pairitySum % 2 == 0);
		//OF is dureing operation
		//DF is during operation
		flagRegister[ RegisterConstants.ZF] = (result == 0);	
	}

	/**
	 * Balangta test etmek iin yazlm bir method.
	 * @throws EMURunTimeException
	 * @throws AssemblerException
	 */
	public void test() throws EMURunTimeException, AssemblerException {
		RegisterType ax = new RegisterType(RegisterConstants.GENERAL_TYPE,
				RegisterConstants.AX);
		RegisterType bx = new RegisterType(RegisterConstants.GENERAL_TYPE,
				RegisterConstants.BX);
		setRegisterValue(ax, 5);
		System.out.println("ax:" + getRegisterValue(ax));
		System.out.println("bx:" + getRegisterValue(bx));
		fetch();
		execute();
		System.out.println("ax:" + getRegisterValue(ax));
		System.out.println("bx:" + getRegisterValue(bx));
		fetch();
		execute();
		System.out.println("ax:" + getRegisterValue(ax));
		System.out.println("bx:" + getRegisterValue(bx));
		fetch();
		execute();
		System.out.println("ax:" + getRegisterValue(ax));
		System.out.println("bx:" + getRegisterValue(bx));
	}

	public boolean[] getFlagRegister() {
		return flagRegister;
	}
	
	public boolean isFinished(){
		return finished;
	}

	/**
	 * lemci btn her eyi ilk bataki haline dndrr.
	 * @throws AssemblerException
	 */
	public void reset() throws AssemblerException {
		finished = false;
		instructionIndex = 0;
		assembler.reset();
		initRegisters();
		memory.reset( memorySize);
		startOS();
	}

	public int getTextLineIndex() {
		if( instructionIndex >= assembler.getInstructionToTextMapping().size()) return -1;
		return assembler.getInstructionToTextMapping().get(instructionIndex).getValue();
	}

	public boolean isWaiting() {
		return waiting;
	}

	public void setWaiting(boolean b) {
		waiting = b;
	}

}
