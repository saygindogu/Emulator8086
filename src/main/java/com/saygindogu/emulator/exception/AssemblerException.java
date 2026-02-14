package com.saygindogu.emulator.exception;

public class AssemblerException extends Exception {

	public AssemblerException(Exception e) {
		super("Assembler exception", e);
	}

	public AssemblerException() {
		super("Assembler exception");
	}

	public AssemblerException(String message) {
		super("Assembler exception - " + message);
	}
}
