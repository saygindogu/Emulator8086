# How Emulator8086 Works

This document describes the internal architecture and execution model of Emulator8086.

## Table of Contents

- [High-Level Architecture](#high-level-architecture)
- [Application Lifecycle](#application-lifecycle)
- [The Assembler](#the-assembler)
- [The Processor](#the-processor)
- [The GUI](#the-gui)
- [Supporting Types](#supporting-types)

---

## High-Level Architecture

The emulator is structured around three core components:

```
┌───────────────┐     ┌───────────────┐     ┌───────────────┐
│   Assembler   │────▶│   Processor   │◀────│     GUI       │
│  (parsing &   │     │  (execution & │     │  (display &   │
│   analysis)   │     │    state)     │     │   controls)   │
└───────────────┘     └───────────────┘     └───────────────┘
        ▲                     ▲                     ▲
        └─────────────────────┼─────────────────────┘
                              │
                       ┌──────┴──────┐
                       │  Emulator   │
                       │ (controller)│
                       └─────────────┘
```

- **Emulator** — the central controller that wires everything together.
- **Assembler** — reads `.asm` source files, tokenises instructions, resolves labels and variables.
- **Processor** — maintains CPU state (registers, memory, flags) and executes instructions.
- **GUI** — a Swing-based interface for editing code, stepping through execution, and inspecting state.

---

## Application Lifecycle

### Startup

1. `Emulator.main()` runs on the Swing Event Dispatch Thread via `SwingUtilities.invokeLater()`.
2. An `Emulator` instance is created, which initialises an `Assembler` with a 4096-byte minimum memory.
3. A default assembly file (`test_inputs/comprehensive_demo.asm`) is loaded.
4. `EmulatorView` (the main JFrame) is created and linked to the emulator.

### Execution Modes

| Mode | Trigger | Behaviour |
|------|---------|-----------|
| **Step** | Run > Next (or the Next button) | Executes one instruction, then updates the GUI. |
| **Run All** | Run > Run All | Repeatedly calls `oneStep()` until `HLT` is reached or an error occurs. |
| **Reset** | Run > Reset | Reinitialises the processor, reparses the source file, and reloads variables into memory. |

### The `oneStep()` Cycle

```
oneStep()
  ├─ processor.fetch()         ← get tokens for current instruction
  ├─ processor.execute()       ← run the instruction, update state
  └─ notifyViewForChanges()    ← repaint all GUI panels
```

If an `AssemblerException` or `EMURunTimeException` is thrown, the GUI highlights the offending line in red and displays an error dialog.

---

## The Assembler

The `Assembler` class (`Assembler.java`) is responsible for reading an `.asm` file and producing a structured representation that the processor can execute.

### Parsing Phases

#### Phase 1 — File Reading and Comment Stripping

The assembler reads the file line by line. Everything after a semicolon (`;`) is treated as a comment and discarded.

```asm
add AX, 12h   ; this part is removed
```

#### Phase 2 — Label Resolution

Lines containing a colon are checked for label declarations. A label must match `[A-Za-z][A-Za-z0-9]*` and appear before the colon:

```asm
myLabel: mov AX, 5
```

Labels are stored in two structures:
- `tagList` — a flat list of label names.
- `tagIndexTable` — a list of `Pair<labelName, instructionIndex>` for jump resolution.

Duplicate labels produce an `AssemblerException`.

#### Phase 3 — Variable Declarations

Variable declarations are extracted before the instruction stream. The syntax is:

```asm
name db value     ; byte (8-bit)
name dw value     ; word (16-bit)
name dd value     ; double word (32-bit)
```

Special forms:
- `name db ?` — uninitialized (defaults to 0).
- `name db 10 dup(5)` — allocates 10 bytes, each initialized to 5.
- `name db 'A'` — character literal.

Variables are stored as `AssemblyVariable` objects and later placed in memory starting at address 0 by `placeVariablesInMemory()`.

#### Phase 4 — Tokenisation

Each non-variable instruction line is split into tokens:

```
"add BX, [11h]"  →  mnemonic: "ADD", tokens: ["BX", "[11h]"]
```

Commas separate operands; spaces separate the mnemonic from the first operand. Hex literals (`12h`), binary literals (`1010b`), and decimal literals are all recognised.

### Addressing Modes

The assembler's `decodeAddress()` method handles a rich set of addressing modes:

| Syntax | Mode | Example |
|--------|------|---------|
| `[offset]` | Direct | `[11h]` |
| `[reg]` | Register indirect | `[BX]` |
| `[seg:offset]` | Segment override | `[ES:DX]` |
| `[reg + offset]` | Register + displacement | `[BX + 5]` |
| `[base + index]` | Base-index | `[BX + SI]` |
| `variableName` | Variable (resolved to address) | `myVar` |

All memory accesses are resolved to a 20-bit physical address using the formula:

```
physical = (segment << 4) + offset
```

The default segment register is DS unless an explicit segment override is specified.

---

## The Processor

The `Processor` class (`Processor.java`) emulates the 8086 CPU. It owns all mutable state: registers, memory, flags, and the instruction pointer.

### Register Model

The 8086 has several register groups, each stored as a separate array:

#### General-Purpose Registers

Stored as an 8-byte array with the following layout:

```
Index:  0    1    2    3    4    5    6    7
      ┌────┬────┬────┬────┬────┬────┬────┬────┐
      │ AH │ AL │ BH │ BL │ CH │ CL │ DH │ DL │
      └────┴────┴────┴────┴────┴────┴────┴────┘
        ╰───AX───╯ ╰───BX───╯ ╰───CX───╯ ╰───DX───╯
```

16-bit register access (e.g., `AX`) reads two consecutive bytes and combines them: `(high << 8) | (low & 0xFF)`. 8-bit register access (e.g., `AH`, `AL`) reads a single byte directly.

#### Segment Registers

Stored as a 4-element `short[]` array:

| Index | Register | Purpose |
|-------|----------|---------|
| 0 | CS | Code Segment |
| 1 | DS | Data Segment |
| 2 | SS | Stack Segment |
| 3 | ES | Extra Segment |

#### Pointer and Index Registers

Stored as a 4-element `short[]` array:

| Index | Register | Purpose |
|-------|----------|---------|
| 0 | SP | Stack Pointer |
| 1 | BP | Base Pointer |
| 2 | SI | Source Index |
| 3 | DI | Destination Index |

#### Flags Register

A 16-element `boolean[]` array. Key flag positions mirror the real 8086:

| Index | Flag | Meaning |
|-------|------|---------|
| 0 | CF | Carry Flag |
| 2 | PF | Parity Flag |
| 6 | ZF | Zero Flag |
| 7 | SF | Sign Flag |
| 10 | DF | Direction Flag |
| 11 | OF | Overflow Flag |

### Memory Model

Memory is a flat byte array (minimum 4096 bytes). Variables are placed starting at address 0. Byte and word reads/writes are handled through helper methods that combine or split bytes as needed.

### Instruction Execution

After `fetch()` retrieves the current instruction's tokens, `execute()` dispatches on the mnemonic via a `switch` expression:

```java
switch (mnemonic) {
    case "MOV" -> mov();
    case "ADD" -> add();
    case "JMP" -> jmp();
    // ... 30+ more cases
}
```

After execution, the instruction pointer (`IP`) is incremented by 2 and the instruction index advances by 1 — unless a jump instruction modifies these values directly.

### Instruction Categories

#### Data Movement: `MOV`, `LEA`

`MOV` transfers data between registers, memory, and immediates. `LEA` loads the effective address itself (not the value at that address) into a register.

#### Arithmetic: `ADD`, `ADC`, `SUB`, `SBB`, `INC`, `DEC`, `NEG`, `CMP`

These use the **strategy pattern** through functional interfaces. For example, `ADD` and `SUB` both call a shared `basicALU()` method, passing a different lambda:

```java
// ADD passes: (a, b, width) -> a + b
// SUB passes: (a, b, width) -> a - b
```

`ADC` and `SBB` add or subtract the carry flag. `CMP` performs subtraction but only updates flags, discarding the result. `INC` and `DEC` use `OneOpenardBasicFunction`.

#### Multiplication and Division: `MUL`, `IMUL`, `DIV`, `IDIV`

These use `MultiplicationDivisionFunction`. For 8-bit operands, the result goes into AX. For 16-bit operands, the result spans DX:AX. Signed variants (`IMUL`, `IDIV`) handle sign extension.

#### Logic: `AND`, `OR`, `XOR`, `NOT`

`AND`, `OR`, and `XOR` use the same `basicALU()` path as arithmetic. `NOT` inverts all bits via `OneOpenardBasicFunction`.

#### Shift and Rotate: `SHL`, `SHR`, `ROL`, `ROR`

These use `ShiftRotateFunction`. The operand is converted to a binary array for bit-level manipulation. Shifts discard bits (updating the carry flag), while rotates wrap bits around.

#### Control Flow: `JMP`, `Jcc`, `LOOP`

Unconditional `JMP` sets the instruction index to the label's target. Conditional jumps (`JA`, `JB`, `JG`, `JL`, `JE`, etc.) test flag combinations before jumping. `LOOP` decrements CX and jumps if CX is not zero.

#### Flag Control: `CLC`, `STC`, `CLD`, `STD`

Directly set or clear individual flags.

#### Halt: `HLT`, `NOP`

`HLT` sets a "waiting" flag that stops execution. `NOP` does nothing.

### Flag Computation

After most arithmetic and logic operations, `handleFlags()` is called:

1. **Parity Flag (PF):** Set if the number of 1-bits in the result is even.
2. **Sign Flag (SF):** Set to the most significant bit of the result.
3. **Carry Flag (CF):** Set if the result exceeds the operand width (e.g., bit 8 for byte operations, bit 16 for word operations).
4. **Zero Flag (ZF):** Set if the result is zero.

`handleOverflowFlag()` is called separately to detect signed overflow — checking whether the result of a signed operation falls outside the representable range.

---

## The GUI

The interface is built with Java Swing and organised into nested panels:

```
EmulatorView (JFrame, 800×600)
└─ MainPanel
   ├─ MenuPanel ─────────────────────────── top
   └─ JSplitPane (vertical)
      ├─ JSplitPane (horizontal)
      │  ├─ TextPanel ──────────── source code editor (left, 9/12 width)
      │  └─ MemoryPanel ────────── memory display (right, 3/12 width)
      └─ RegisterPanel ─────────── register display (bottom, 3/12 height)
         ├─ GeneralPurposeRegistersPanel
         ├─ SegmentRegisterPanel
         └─ PtrIndexRegistersPanel
            └─ ProgramStatusRegistersPanel
```

### TextPanel

A `JTextArea` that displays the assembly source code. The currently executing line is highlighted in blue. If an error occurs, the offending line turns red. The user can edit code directly; changes are picked up on the next reset.

### MemoryPanel

Two synchronised lists showing addresses (left column) and byte values (right column) for the entire memory space. The display format (hex, decimal, binary) follows the user's representation mode setting.

### RegisterPanel

Composed of sub-panels that read current values from the processor and display them:

- **General-Purpose:** Shows all 8-bit and 16-bit register values (AX, AH, AL, etc.).
- **Segment:** Shows CS, DS, SS, ES.
- **Pointer/Index:** Shows SP, BP, SI, DI.
- **Flags:** Shows all 16 flag bits as ON/OFF indicators, plus the current IP value.

### Representation Modes

The Options > Representation Mode dialog lets the user independently choose hex, decimal, or binary for both **values** and **addresses**. Three wrapper classes handle formatting:

| Class | Width | Usage |
|-------|-------|-------|
| `EmuByte` | 8-bit | Register bytes, memory values |
| `EmuWord` | 16-bit | 16-bit register values |
| `EmuAddress` | 20-bit | Memory addresses |

### View Update Cycle

After each instruction executes, `Emulator.notifyViewForChanges()` triggers `EmulatorView.updateView()`, which calls `invalidate()`, `validate()`, and `repaint()` on the main panel. Each sub-panel's `updateView()` method re-reads current values from the processor and refreshes its display.

---

## Supporting Types

### AssemblyVariable

Represents a declared variable with:
- `unitWidth` — byte/word/double-word size.
- `length` — array length (1 for scalars, N for `dup()` arrays).
- `value` — initial value.
- `name` — identifier used in source code.
- `address` — physical memory address, assigned at load time.

### Immediate

Wraps a numeric constant, masked to 16 bits. Automatically determines its width: values 0–255 are 8-bit, 256–65535 are 16-bit, larger values are 32-bit.

### OperationWidth

Wraps the `Width` enum (`EIGHT_BIT`, `SIXTEEN_BIT`, `THIRTY_TWO_BITS`) and provides convenience methods like `isEightBit()` and `getIntWidth()`.

### RegisterType

Encapsulates a register's identity: its class (general, segment, pointer/index, program status), its numeric index within that class, and its width. A static map resolves string names (e.g., `"AX"`) to `RegisterType` instances.

### Pair\<K, V\>

A generic record used internally for label-to-index mappings and instruction-to-text-line mappings.

### Functional Interfaces (Strategy Pattern)

The processor uses four `@FunctionalInterface` types to avoid duplicating instruction logic:

| Interface | Signature | Used By |
|-----------|-----------|---------|
| `BasicALUFunction` | `int execute(int left, int right, OperationWidth width)` | ADD, SUB, AND, OR, XOR, ADC, SBB |
| `OneOpenardBasicFunction` | `int execute(int value)` | INC, DEC, NEG, NOT |
| `MultiplicationDivisionFunction` | `void execute(int value, OperationWidth width)` | MUL, IMUL, DIV, IDIV |
| `ShiftRotateFunction` | `int execute(int left, int right, OperationWidth width)` | SHL, SHR, ROL, ROR |

Each instruction passes a lambda implementing the specific operation, while the surrounding infrastructure handles operand decoding, memory access, flag updates, and result storage.

### Exceptions

- **AssemblerException** — thrown during parsing for syntax errors, invalid labels, or malformed variable declarations.
- **EMURunTimeException** — thrown during execution for invalid memory accesses or unsupported operations. Carries the instruction index for error reporting.

Both are caught by the `Emulator` and surfaced to the user via dialog boxes, with the offending line highlighted in the editor.
