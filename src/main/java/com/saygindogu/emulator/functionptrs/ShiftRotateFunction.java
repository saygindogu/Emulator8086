package com.saygindogu.emulator.functionptrs;

import com.saygindogu.emulator.OperationWidth;

@FunctionalInterface
public interface ShiftRotateFunction {
	int execute(int leftValue, int rightValue, OperationWidth width);
}
