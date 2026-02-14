package com.saygindogu.emulator.functionptrs;

import com.saygindogu.emulator.OperationWidth;

@FunctionalInterface
public interface MultiplicationDivisionFunction {
	void execute(int value, OperationWidth width);
}
