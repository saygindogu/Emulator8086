# Emulator8086

An emulator for the Intel 8086 microprocessor architecture, featuring a Swing-based GUI for writing, loading, and executing 8086 assembly programs.

For background on the 8086 architecture, see the [GeeksforGeeks explanation](https://www.geeksforgeeks.org/architecture-of-8086/).

## Prerequisites

- **Java 25** or later
- **Gradle** (wrapper included — no separate installation required)

## Getting Started

### Run the application

```bash
./gradlew run
```

On Windows, use `gradlew.bat run` instead.

### Run tests

```bash
./gradlew test
```

### Build a distributable

```bash
./gradlew build
```

The build output will be placed in `build/`.

## Using the Emulator

Once launched, the GUI provides the following menus:

| Menu      | Actions                                                      |
|-----------|--------------------------------------------------------------|
| **File**  | New, Open, Save, Save As, Exit                               |
| **Run**   | Run All (execute the full program), Next (step one instruction), Reset |
| **Options** | Change representation mode (hex / binary / decimal for both values and addresses), Help |

### Writing Assembly

You can write assembly directly in the editor or load a `.asm` file via **File > Open**. Sample programs are included in the `test_inputs/` directory.

#### Syntax overview

```asm
; Comments start with a semicolon
variable db 5              ; Define a byte variable with initial value 5

begin: mov AX, variable    ; Labels end with a colon
add AX, 12h               ; Hexadecimal literals use the 'h' suffix
add BX, [11h]             ; Square brackets denote memory addressing
mov CX, 10
loop begin                 ; LOOP decrements CX and jumps if CX != 0
```

#### Variable declarations

| Directive | Width       | Example                    |
|-----------|-------------|----------------------------|
| `db`      | Byte (8-bit)  | `myVar db 5`             |
| `dw`      | Word (16-bit) | `myWord dw 1234h`        |
| `dd`      | Double word (32-bit) | `myDWord dd 0`     |

Variables support `?` for uninitialized values and `dup()` for repeated allocation.

#### Supported instructions

**Data movement:** `MOV`, `LEA`

**Arithmetic:** `ADD`, `ADC`, `SUB`, `SBB`, `MUL`, `IMUL`, `DIV`, `IDIV`, `INC`, `DEC`, `NEG`, `CMP`

**Logic:** `AND`, `OR`, `XOR`, `NOT`

**Shift and rotate:** `SHL`, `SHR`, `ROL`, `ROR`

**Control flow:** `JMP`, `JA`, `JAE`, `JB`, `JBE`, `JC`, `JG`, `JGE`, `JL`, `JLE`, `JNE`, `JNP`, `JP`, `JPO`, `LOOP`

**Flags:** `CLC`, `CLD`, `STC`, `STD`

**Other:** `NOP`, `HLT`

### Representation Modes

Use **Options > Representation Mode** to switch between hexadecimal, binary, and decimal display for both memory values and addresses. This applies to the register and memory panels in real time.

## Project Structure

```
src/main/java/com/saygindogu/emulator/
├── Assembler.java          # Parses and assembles .asm source files
├── Emulator.java           # Application entry point
├── Processor.java          # 8086 CPU emulation (instruction execution)
├── RegisterConstants.java  # Register index constants
├── RegisterType.java       # Register type definitions
└── gui/                    # Swing-based user interface panels

test_inputs/                # Sample assembly programs
```

## Documentation

- [How It Works](docs/how-it-works.md) — internal architecture, assembler pipeline, processor model, GUI layout, and [test program descriptions](docs/test-programs.md#test-programs).
- [HISTORY.md](HISTORY.md) — full project history.
