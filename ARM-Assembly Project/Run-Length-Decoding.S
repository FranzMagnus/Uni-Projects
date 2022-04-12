.globl run_length_decode
.type run_length_decode, %function


run_length_decode:

/*
Global genutzte Register:
    Input Parameter:
        X0 *data
        X1 *result
        X2 result_size
    Selbstdefienierte Variablen:
        X3 result_length = Anzahl der in den Outputbuffer geschriebenen Bytes
        X4 Pixel-Count = Anzahl der Pixel des Bildes
        X8 endGröße = Anzahl der Bytes die das Bild am Ende haben muss

Temporäre Register:
    X5 Width = Breite des Bildes
    X6 Length = Länge des Bildes 
    X7 ColorCount = Anzahl der verschiedenen Farben des Bildes  
    x9 farbBytes = Anzahl der zum speichern der Farben benötigten Bytes
*/

//reads Header
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

//Berechnet ob die Funktion einen Fehler wirft
    mul X4, X5, X6      //Berechnet die Anzahl der Pixel (länge x breite)
    add X8, X3, X4      //endGröße = result_length + PixelCount
    mov X9, 3
    mul X9, X7, X9      //farbBytes = ColorCount*3
    add X9, X9, 3       //farbBytes += 3  Da die Farben auf 0 gezählt werden
    add X8, X8, X9      //endGröße += farbBytes

    cmp X2, X8  //result_Size < result_Length   Bricht die Funktion ab wenn der zur Verfügung gestellte BUffer nicht groß genug ist erfüllt ist
    b.lt .LFehler

//For-Loop for reading the Colors;

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
    cmp W7, WZR         //ColorCount > 0
    b.ge .LcolorLoop

//Haupt Algorithmus

/* 
Temporäre Register:
    X5 CounterValue = Anzahl der Pixel
    X6 PixelValue = Wert der Pixel
*/

.LmainAlgorithmLoop:
    ldrb W5, [X0], 1    //Lädt den Counter Wert aus dem InputBuffer
    ldrb W6, [X0], 1    //Lädt den Pixel Wert aus dem InputBuffer

    .LForLoop:
        strb W6, [X1], 1    //Speichert den Pixel Wert in den OutputBuffer
        add X3, X3, 1
        sub X5, X5, 1       // Countervalue--
        cmp X5, XZR         // Countervalue > 0
        b.gt .LForLoop
    
    cmp X8, X3              //endGröße > result_length
    b.gt .LmainAlgorithmLoop

    mov X0, X3  //Exit Success
    b .Lret

.LFehler:
    mov X0, -1  //Exit Failure

.Lret:
    ret