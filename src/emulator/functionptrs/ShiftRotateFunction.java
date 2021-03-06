package emulator.functionptrs;

import emulator.OperationWidth;

//Runtime sırasında oluşabilecek hatalarda bu exception gönderilir.
public interface ShiftRotateFunction {

	int execute(int leftValue, int rightValue, OperationWidth width);
}
