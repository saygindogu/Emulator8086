; segment_override.asm
; Demonstrates segment-override addressing using the ES segment register.
; The ES:DX notation accesses memory at the address computed from
; the ES segment and DX offset (physical address = ES * 16 + DX).

MOV [ES:DX], 5              ; Store immediate value 5 at the address ES:DX
MOV AX, [ES:DX]             ; Load the value at ES:DX into AX
