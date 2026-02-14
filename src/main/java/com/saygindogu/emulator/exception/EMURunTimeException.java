package com.saygindogu.emulator.exception;

public class EMURunTimeException extends Exception {

	private final int instructionIndex;

	public EMURunTimeException(int instructionIndex) {
		super("Run Time Exception in line " + instructionIndex);
		this.instructionIndex = instructionIndex;
	}

	public EMURunTimeException(String message, int instructionIndex) {
		super("Run Time Exception: " + message + " in line " + instructionIndex);
		this.instructionIndex = instructionIndex;
	}

	public int getInstructionIndex() {
		return instructionIndex;
	}
}
