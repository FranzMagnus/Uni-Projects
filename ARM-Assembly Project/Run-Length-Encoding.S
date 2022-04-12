.globl run_length_encode
.type run_length_encode, %function


run_length_encode:

/*
Global genutzte Register:
    Input Parameter:
        X0 *data
        X1 *result
        X2 result_size
    Selbstdefienierte Variablen:
        X3 result_length = Anzahl der in den Outputbuffer geschriebenen Bytes
        X4 Pixel-Count = Anzahl der Pixel des Bildes

Temporäre Register:
    X5 Width = Breite des Bildes
    X6 Length = Länge des Bildes 
    X7 ColorCount = Anzahl der verschiedenen Farben des Bildes
*/

//Liest die wichtigsten Parameter des Headers
    mov X3, XZR
    ldrh W5, [X0], 2    //Lädt die Breite aus dem InputBuffer
    strh W5, [X1], 2    //Speichert die Breite in den OutputBuffer
    add X3, X3, 2
    ldrh W6, [X0], 2    //Lädt die Länge aus dem InputBuffer
    strh W6, [X1], 2    //Speichert die Länge in den OutputBuffer
    add X3, X3, 2
    ldrb W7, [X0], 1    //Lädt die Farbanzahl aus dem InputBuffer
    strb W7, [X1], 1    //Speichert die Farbanzahl in den OutputBuffer
    add X3, X3, 1

//For-Loop um die Farben auszulesen und zu Speichern
.LcolorLoop: 
    ldrb W4, [X0], 1    //Lädt den Rot Wert aus dem InputBuffer
    strb W4, [X1], 1    //Speichert den Rot Wert in den OutputBuffer
    add X3, X3, 1
    ldrb W4, [X0], 1    //Lädt den Grün Wert aus dem InputBuffer
    strb W4, [X1], 1    //Speichert den Grün Wert in den OutputBuffer
    add X3, X3, 1
    ldrb W4, [X0], 1    //Lädt den Blau Wert aus dem InputBuffer
    strb W4, [X1], 1    //Speichert den Blau Wert in den OutputBuffer
    add X3, X3, 1

    sub W7, W7, 1
    cmp W7, WZR         //ColorCount >= 0
    b.ge .LcolorLoop

    mul X4, X5, X6      //Berechnet die Anzahl der Pixel (länge x breite)
 
//Haupt Algorithmus

/* 
Temporäre Register:
    X5 PixelValue 1 = Wert des 1. Pixels zum Vergleichen
    X6 PixelValue 2 = Wert des 2. Pixels zum Vergleichen
    X7 Counter = Counter zum mitzählen der gleichen Pixel
*/
    ldrb W5, [X0], 1        //Lädt den 1. Pixelwert aus dem InputBuffer
    sub X4, X4, 1           // PixelCount -= 1
    mov X7, XZR
    mov W7, 1               // Counter = 1
.LmainAlgorithmLoop:
    ldrb W6, [X0], 1        //Lädt den 2. Pixelwert aus dem InputBuffer
    sub X4, X4, 1           // PixelCount -= 1   
    cmp W5, W6              //Pixelwert1 == Pixelwert2
    b.ne .LelseClause
    cmp W7, 255             // Counter == 255 da ff größter Wert der in einem Char gespeichert werden kann für das auslesen Wichtig
    b.eq .LelseClause
    add W7,W7, 1            // Counter += 1
    b .Lwhile

    .LelseClause:
        strb W7, [X1], 1    //Speichert den Counter in den OutputBuffer
        add X3, X3, 1
        strb W5, [X1], 1    //Speichert den Wert der gleichen Pixel in den OutputBuffer
        add X3, X3, 1
        mov W5, W6          // PixelValue 1 = PixelValue 2
        mov X7, XZR
        mov W7, 1           // Counter = 1

    .Lwhile:
    cmp X4, XZR             //PixelCount >= 0
    b.ge .LmainAlgorithmLoop 

    cmp X2, X3  //result_Size < result_Length
    b.lt .LFehler

    mov X0, X3  //Exit Success
    b .Lret

.LFehler:
    mov X0, -1  //Exit Failure

.Lret:
    ret