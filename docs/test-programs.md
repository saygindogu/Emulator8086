## Test Programs

The `test_inputs/` directory contains sample assembly programs that exercise different emulator features. Each is described below.

### `comprehensive_demo.asm`

A full walkthrough of the emulator's core capabilities. It is loaded by default on startup.

**What it demonstrates:**

1. **Variable declaration** — declares a byte variable (`variable db 5`) and reads it back with `MOV AX, variable`, showing how variables are placed in memory and accessed by name.
2. **Addressing modes for arithmetic** — uses `ADD` with immediate-to-memory (`add [11h], 12h`), immediate-to-register (`add AX, 12h`), register-to-register (`add BX, AX`), memory-to-register (`add BX, [11h]`), and memory-to-segment-register (`add ES, [11h]`).
3. **Shift operations** — performs `SHR ES, 8` to logical-shift-right the ES register by 8 bits, demonstrating bitwise manipulation.
4. **Subtraction** — `SUB BX, AX` shows register-to-register subtraction and the resulting flag updates.
5. **Multiplication** — loads 5 into AX and multiplies by the variable (also 5) using `MUL variable`. The result (25) lands in AX, and its low byte is copied to BH with `MOV BH, AL`.
6. **Looping** — sets CX to 10, clears DX, then uses `INC DX` / `LOOP label` to increment DX ten times. This demonstrates the LOOP instruction's CX-decrement-and-branch behavior.
7. **Flag manipulation and conditional jumps** — adds 256 to DL (an 8-bit overflow that sets the carry flag), clears carry with `CLC`, then tests `JC` which correctly does *not* jump because carry was just cleared.
8. **Program termination** — ends with `HLT` to cleanly stop execution.

### `add_addressing_modes.asm`

A focused test of the `ADD` instruction across all supported operand combinations.

**What it demonstrates:**

| Instruction | Addressing Mode | What Happens |
|---|---|---|
| `add [11h], 12h` | Memory + Immediate | Adds the value `12h` directly to the byte stored at memory address `11h`. |
| `add AX, 12h` | Register + Immediate | Adds the value `12h` to the AX register. |
| `add BX, AX` | Register + Register | Adds the current value of AX into BX. |
| `add BX, [11h]` | Register + Memory | Reads the byte at address `11h` and adds it to BX. |
| `add ES, [11h]` | Segment Register + Memory | Reads the byte at address `11h` and adds it to the ES segment register. |

This program is useful for verifying that operand decoding works correctly for each combination of register, memory, and immediate operands.

### `segment_override.asm`

A minimal test of segment-override addressing using the `ES:DX` notation.

**What it demonstrates:**

1. `MOV [ES:DX], 5` — stores the value 5 at the physical address computed as `ES * 16 + DX`. This shows how the emulator resolves segment:offset pairs to a 20-bit physical address, bypassing the default DS segment.
2. `MOV AX, [ES:DX]` — reads the value back from the same segment-overridden address into AX, confirming the write succeeded.

This program verifies that the `[segment:offset]` addressing syntax works and that explicit segment overrides are applied correctly instead of defaulting to DS.