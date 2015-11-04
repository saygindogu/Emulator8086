package com.saygindogu.emulator.functionptrs;

import com.saygindogu.emulator.OperationWidth;

//Runtime srasnda oluabilecek hatalarda bu exception gnderilir.
public interface ShiftRotateFunction {

	int execute(int leftValue, int rightValue, OperationWidth width);
}
