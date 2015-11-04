package com.saygindogu.emulator.functionptrs;

import com.saygindogu.emulator.OperationWidth;

// add, sub, mov gibi instruction'lar bunu implement eder. ( c++'daki function pointer gibi kullanlr.)
public interface BasicALUFunction {
	
	public int execute( int openardLeft, int openardRight, OperationWidth w);
}
