package com.saygindogu.emulator.exception;

public class AssemblerException extends Exception {

	public AssemblerException(Exception e) {
		super("Assembler exception", e);
	}

	public AssemblerException() {
		super("Assembler exception");
	}

	public AssemblerException(String message, int lineNumber) {
		super("Assembler exception at line " + lineNumber + " - " + message);
	}
}
