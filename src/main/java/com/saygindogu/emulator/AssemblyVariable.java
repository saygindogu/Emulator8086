package com.saygindogu.emulator;

public class AssemblyVariable {

	private final OperationWidth unitWidth;
	private final int lenght;
	private final String name;
	private final int value;
	private int address;

	public AssemblyVariable(OperationWidth unitWidth, int lenght, int value, String name) {
		this.unitWidth = unitWidth;
		this.lenght = lenght;
		this.name = name;
		this.value = value;
	}

	public OperationWidth getUnitWidth() { return unitWidth; }
	public int getLenght() { return lenght; }
	public String getName() { return name; }
	public int getValue() { return value; }
	public int getAddress() { return address; }
	public void setAddress(int address) { this.address = address; }
}
