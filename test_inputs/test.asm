variable db 5
begin: mov AX, variable
add [11h], 12h
add AX, 12h
add BX, AX,
add BX, [11h]
add BX, [11h];BX = 2412h
add ES, [11h] ; ES = 1200h
shr ES, 8
sub BX, AX
mov AX, 0005h
mul variable
mov BH, AL;
mov CX, 10;
mov DX, 0;
label:
inc DX
loop label
add DL, 256
clc
jc label
jmp begin