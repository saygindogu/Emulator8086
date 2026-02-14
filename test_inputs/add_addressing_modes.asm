; add_addressing_modes.asm
; Demonstrates the ADD instruction with various addressing modes.

add [11h], 12h               ; Memory + Immediate: add 12h to the byte at address 11h
add AX, 12h                  ; Register + Immediate: add 12h to the AX register
add BX, AX,                  ; Register + Register: add AX into BX
add BX, [11h]                ; Register + Memory: add the value at address 11h into BX
add ES, [11h]                ; Segment Register + Memory: add value at 11h into ES
