; comprehensive_demo.asm
; A comprehensive demonstration of the emulator's capabilities.
; Exercises variables, arithmetic, memory addressing, shifts,
; multiplication, loops, flag manipulation, and conditional jumps.
; Note: This program loops infinitely via "jmp begin" at the end.

; --- Variable Declaration ---
variable db 5                ; Define a byte variable initialized to 5

; --- Arithmetic and Memory Operations ---
begin: mov AX, variable      ; Load the value of 'variable' (5) into AX
add [11h], 12h               ; Add immediate 12h to memory at address 11h
add AX, 12h                  ; Add immediate 12h to AX
add BX, AX,                  ; Add AX into BX
add BX, [11h]                ; Add value at memory address 11h into BX
add BX, [11h]                ; Add again; BX should now be 2412h
add ES, [11h]                ; Add memory value into segment register ES (ES = 1200h)

; --- Shift Operation ---
shr ES, 8                    ; Logical shift right ES by 8 bits

; --- Subtraction ---
sub BX, AX                   ; Subtract AX from BX

; --- Multiplication ---
mov AX, 0005h                ; Load 5 into AX
mul variable                 ; Unsigned multiply AX by 'variable' (5); result in DX:AX
mov BH, AL                   ; Store low byte of result into BH

; --- Loop: Increment DX ten times ---
mov CX, 10                   ; Set loop counter to 10
mov DX, 0                    ; Clear DX
label:
inc DX                       ; Increment DX by 1
loop label                   ; Decrement CX; jump to 'label' if CX != 0

; --- Flag Manipulation and Conditional Jump ---
add DL, 256                  ; Add 256 to DL (overflows, sets carry flag)
clc                          ; Clear carry flag
jc label                     ; Jump if carry — will NOT jump since carry was just cleared

; --- Restart ---
jmp begin                    ; Jump back to beginning (infinite loop)
