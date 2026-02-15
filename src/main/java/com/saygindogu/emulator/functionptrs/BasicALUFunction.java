package com.saygindogu.emulator.functionptrs;

import com.saygindogu.emulator.OperationWidth;

@FunctionalInterface
public interface BasicALUFunction {
	int execute(int openardLeft, int openardRight, OperationWidth w);
}
