package com.saygindogu.emulator.functionptrs;

import com.saygindogu.emulator.OperationWidth;

// C'deki function pointer ilevini gren bir interface. MUL, DIV iin kullanlr
public interface MultiplicationDivisionFunction {

	void execute(int value, OperationWidth width);

}
