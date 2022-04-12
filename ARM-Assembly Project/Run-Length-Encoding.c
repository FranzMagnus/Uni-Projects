
#define _POSIX_C_SOURCE 199309L
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <stdint.h>
#include <math.h>
#include <time.h>

//externe Funktionen
extern int run_length_encode(char *data, char *result, unsigned int result_size);
extern int run_length_decode(char *data, char *result, unsigned int result_size);

//Structs zur konvertierung zu .bmp für visuelle Ueberpruefung
#pragma pack(push,1)
typedef struct{
    short	        bitmap_type;
    int	            file_size;
    short	        reserved1;
    short	        reserved2;
    unsigned int 	offset_bits;
}bitmap_file_header;

typedef struct{
    unsigned int 	size_header;
    unsigned int 	width;
    unsigned int 	height;
    short int 	    planes;
    short int 	    bit_count;
    unsigned int 	compression;
    unsigned int 	image_size;
    unsigned int 	ppm_x;
    unsigned int 	ppm_y;
    unsigned int 	clr_used;
    unsigned int 	clr_important;
}bitmap_info_header;

typedef struct{
    bitmap_file_header bfh;
    bitmap_info_header bih;
}bitmap;

typedef struct{
    uint8_t		blue;
    uint8_t		green;
    uint8_t		red;
    uint8_t		reserved;
}rgb_quad;
#pragma pack(pop)

//--------------------------------------------
//----------	HELFER-FUNKTIONEN	----------
//--------------------------------------------

//	Ausgabe der "Hilfe/Anleitung" auf der Konsole
void printHelp(char* progName) {
	printf("\n\n================Help================\n");
	printf("\n-----  C-Funktionen:  -----\n");
    printf("%s -c_encode -InputFilePath <char*> -OutputFilePath <char*> : Enkodiert eine Datei von .gepasp zu .team104.\n", progName);
    printf("%s -c_decode -InputFilePath <char*> -OutputFilePath <char*> : Dekodiert eine Datei von .team104 zu .gepasp.\n", progName);
    printf("\n-----  Assembler-Funktionen:  -----\n");
    printf("%s -asm_encode -InputFilePath <char*> -OutputFilePath <char*> : Enkodiert eine Datei von .gepasp zu .team104.\n", progName);
    printf("%s -asm_decode -InputFilePath <char*> -OutputFilePath <char*> : Dekodiert eine Datei von .team104 zu .gepasp.\n", progName);
    printf("\n-----  Hilfs-Funktionen:  -----\n");
    printf("%s -convert -InputFilePath <char*> -OutputFilePath <char*> : Konvertiert eine Datei von .gepasp/.team zu .bmp\n", progName);
    printf("%s -help : Gibt diese Hilfe auf der Konsole aus.\n", progName);
	printf("====================================\n\n\n");
}

//--------------------------------------------
//--------     ASSEMBLER-AUFRUFE      --------
//--------------------------------------------

//	Rahmenprogramm Run-Length-Encoding.S
int asm_encode(char *inFilePath, char *outFilePath){
	printf("[Start] asm_encode\n");
	FILE *input = fopen(inFilePath, "rb");
	FILE *output = fopen(outFilePath, "wb");
	char *inputBuffer;
	char *outputBuffer;
	unsigned long inputfileSize;
	size_t readSize;
	int result;

	if(input == NULL){
		printf("[FAILURE]    opening file\n");
		return EXIT_FAILURE;
	}

	if(output == NULL){
		printf("[FAILURE]    opening file\n");
		return EXIT_FAILURE;
	}

	//berechnen der Input Größe
	fseek(input, 0, SEEK_END);
	inputfileSize = ftell(input);
	rewind(input);

	//Speicherallokation für den Input-Buffer
	inputBuffer = (char*) malloc(sizeof(char)*inputfileSize);
	if(inputBuffer == NULL){
		printf("[FAILURE]    allocating Memory for InputBuffer.\n");
		return EXIT_FAILURE;
	}

	//Einlesen der Datei in den Buffer
	readSize = fread(inputBuffer,sizeof(char),inputfileSize,input);
	if (readSize != inputfileSize) {
		printf("[FAILURE]    reading the Input File.\n");
		return EXIT_FAILURE;
	}

    //Speicherallokation für den Output-Buffer
	outputBuffer = (char*) malloc(sizeof(char)*inputfileSize);
	if(outputBuffer == NULL){
		printf("[FAILURE]   allocating Memory for OutputBuffer.\n");
		return EXIT_FAILURE;
	}

	//Aufruf AssemblyFunction
	result = run_length_encode(inputBuffer, outputBuffer, inputfileSize);
	if(result == -1){
		printf("[FAILURE]   encoding the File.\n");
		return EXIT_FAILURE;
	}

	//Schreiben des Ergebnisses in neue Datei
	fwrite(outputBuffer, sizeof(char), result, output);
	if(ferror(output)){
		printf("[FAILURE]    writing the Output File.\n");
		return EXIT_FAILURE;
	}

	fclose(input);
	fclose(output);
	free(inputBuffer);
	free(outputBuffer);
    printf("[Success] asm_encode\n");
	return result;
}

//	Rahmenprogramm Run-Length-Decoding.S
int asm_decode(char *inFilePath, char *outFilePath){
    printf("[Start] asm_decode\n");
	FILE *input = fopen(inFilePath, "rb");
	FILE *output = fopen(outFilePath, "wb");
	char *inputBuffer;
	char *outputBuffer;
	unsigned long inputfileSize;
	size_t readSize;
	int result;

	if(input == NULL){
		printf("[FAILURE]    opening InputFile\n");
		return EXIT_FAILURE;
	}

	if(output == NULL){
		printf("[FAILURE]    opening OutputFile\n");
		return EXIT_FAILURE;
	}

    //berechnen der Input Größe
	fseek(input, 0, SEEK_END);
	inputfileSize = ftell(input);
	rewind(input);

    //Speicherallokation für den Input-Buffer
	inputBuffer = (char*) malloc(sizeof(char)*inputfileSize);
	if(inputBuffer == NULL){
		printf("[FAILURE]    allocating Memory for InputBuffer.\n");
		return EXIT_FAILURE;
	}

    //Einlesen der Datei in den Buffer
	readSize = fread(inputBuffer,sizeof(char),inputfileSize,input);
	if (readSize != inputfileSize) {
		printf("[FAILURE]    reading the Input File.\n");
		return EXIT_FAILURE;
	}

    //Speicherallokation für den Output-Buffer
	outputBuffer = (char*) malloc(sizeof(char)*inputfileSize*10);
	if(outputBuffer == NULL){
		printf("[FAILURE]    allocating Memory for OutputBuffer.\n");
		return EXIT_FAILURE;
	}

    //Aufruf AssemblyFunction
	result = run_length_decode(inputBuffer, outputBuffer, inputfileSize*10);
	if(result == -1){
		printf("[FAILURE] decoding the File.\n");
		return EXIT_FAILURE;
	}

    //Schreiben des Ergebnisses in neue Datei
	fwrite(outputBuffer, sizeof(char), result, output);
	if(ferror(output)){
		printf("[FAILURE]    writing the Output File.\n");
		return EXIT_FAILURE;
	}

	fclose(input);
	fclose(output);
	free(inputBuffer);
	free(outputBuffer);
    printf("[Success] asm_decode\n");
	return result;
}



//--------------------------------------------
//----------	c_Implementierung	----------
//--------------------------------------------

//	C Implementierung zum enkodieren
int c_encode(char* inFilePath, char* outFilePath){
    printf("[Start] c_encode\n");
    //Einlesen des Files in einen Buffer
    char* input_buffer;
    char* header;
    char* pixeldata;

    //Oeffnen der Input-Datei
    FILE *input_file=fopen(inFilePath,"r");
    if(input_file == NULL){
        printf("[FAILURE]    opening Input File\n");
        return EXIT_FAILURE;
    }

    //berechnen der Groesse
    fseek(input_file, 0, SEEK_END);
    unsigned long inputfileSize = ftell(input_file);
    rewind(input_file);

    //Speicherallokation Buffer
    input_buffer = (char*) malloc(sizeof(char)*inputfileSize);
    if(input_buffer == NULL){
        printf("[FAILURE]    allocating Memory for InputBuffer.\n");
        return EXIT_FAILURE;
    }

    //Einlesen der Datei in den Buffer
    size_t readSize = fread(input_buffer,sizeof(char),inputfileSize,input_file);
    if (readSize != inputfileSize) {
        printf("[FAILURE]    reading the Input File.\n");
        return EXIT_FAILURE;
    }
    
    //Auslesen der Anzahl an Farben, um den Header zu Überspringen
    int numberOfColors = input_buffer[4] + 1;

    //kopieren des Headers in die Outputfile
    //pixelwerte starten nach RGB-werten (Anzahl Farben * 3) + 5 (2B für Höhe, 2B für Breite, 1B für Farbenanzahl)
    unsigned long header_size = 5 + 3 * numberOfColors;
    header = (char*)malloc(sizeof(char) * header_size);
    if(header == NULL){
        printf("[FAILURE]    allocating Memory for OutputBuffer.\n");
        return EXIT_FAILURE;
    }

    for(unsigned long i = 0; i < header_size; i++){
        header[i] = input_buffer[i];
    }
    //initialisieren des Pixeldaten Arrays
    int pixeldata_size = inputfileSize - header_size;
    pixeldata = (char*)malloc(sizeof(char) * pixeldata_size);
    if(pixeldata == NULL){
        printf("[FAILURE]    allocating Memory for OutputBuffer.\n");
        return EXIT_FAILURE;
    }

    char char_value = input_buffer[header_size];//erster Wert
    int char_counter = 1;		                //Anzahl gleichwertiger Chars
	int output_pointer = 0;	        //zeigt auf naechste freie Stelle zum schreiben
	
	//schleife über Pixeldaten des Inputs
	for(unsigned long i = header_size + 1; i < inputfileSize; i++){
		
		//naechster Wert gleich dem aktuellen
		if(char_value == input_buffer[i] && char_counter < 255){
			char_counter++;
		}

		//naechster Wert ungleich dem aktuellen
		else{
			//Testen der restlichen Groesse
			if(pixeldata_size < output_pointer + 2){
			    //output würde größer werden als input
				return -1;
			}

			//schreiben der gleichwertingen char in den output
			pixeldata[output_pointer] = (char) char_counter;//anzahl der chars
			char_counter = 1;                               //zuruecksetzen des Zählers
			pixeldata[output_pointer + 1] = char_value;	    //wert der chars
			char_value = input_buffer[i];				    //aktualisieren des Vergleichswerts
			output_pointer += 2;				            //erhöhen des Zeigers
		}
	}

	//schreiben der Outputfile
    //Oeffnen der Datei
    FILE *output = fopen(outFilePath, "wb");
    if(output == NULL){
		printf("[FAILURE]    opening OutputFile\n");
		return EXIT_FAILURE;
	}

	//schreiben des headers
    fwrite(header, 1, sizeof(char) * header_size, output);
     if(ferror(output)){
		printf("[FAILURE]    writing the Output File.\n");
		return EXIT_FAILURE;
	}

    //schreiben der Pixeldaten
    fwrite(pixeldata, 1, sizeof(char) * output_pointer, output);
     if(ferror(output)){
		printf("[FAILURE]    writing the Output File.\n");
		return EXIT_FAILURE;
	}

    //abschliessen der Funktion
    fclose(output);
    free(header);
    free(input_buffer);
    free(pixeldata);
    printf("[Success] c_encode\n");
    return (sizeof(char)*header_size+sizeof(char)+output_pointer);
}

//	c Implementatierung zum dekodieren der Pixelwerte (kein HEADER)
int c_decode(char* inFilePath, char* outFilePath){
    printf("[Start] c_decode\n");
    //Einlesen des Files in einen Buffer
    char* input_buffer;
    char* header;
    char* pixeldata;

    //Oeffnen der Input-Datei
    FILE *input_file=fopen(inFilePath,"r");
    if(input_file == NULL){
        printf("[FAILURE]    opening file\n");
        return EXIT_FAILURE;
    }

    //berechnen der Groesse
    fseek(input_file, 0, SEEK_END);
    unsigned long inputfileSize = ftell(input_file);
    rewind(input_file);

    //Speicherallokation Buffer
    input_buffer = (char*) malloc(sizeof(char)*inputfileSize);
    if(input_buffer == NULL){
        printf("[FAILURE]    allocating Memory for InputBuffer.\n");
        return EXIT_FAILURE;
    }

    //Einlesen der Datei in den Buffer
    size_t readSize = fread(input_buffer,sizeof(char),inputfileSize,input_file);
    if (readSize != inputfileSize) {
        printf("[FAILURE]    reading the Input File.\n");
        return EXIT_FAILURE;
    }

    //Auslesen der Anzahl an Farben, um den Header zu Überspringen
    int numberOfColors = input_buffer[4] + 1;

    //Auslesen der Hoehe und Breite um die Anzahl der Pixel zubestimmen
    //Berechnung der Hoehe
    int height = input_buffer[0];
    int tmp = input_buffer[1];
    if(tmp !=0){
        int x = tmp/16;
        int y = tmp%16;
        height += x*pow((double)16,(double)3);
        height += y*pow((double)16,(double)2);
    }

    //Berechnung der Breite
    int width = input_buffer[2];
    tmp = input_buffer[3];
    if(tmp !=0){
        int v = tmp/16;
        int w = tmp%16;
        width += v*pow((double)16,(double)3);
        width += w*pow((double)16,(double)2);
    }

    //initialisieren des Pixeldaten Arrays
    int pixeldata_size = width * height;
    pixeldata = (char*)malloc(sizeof(char) * pixeldata_size);
    if(pixeldata == NULL){
        printf("[FAILURE]    allocating Memory for OutputBuffer.\n");
        return EXIT_FAILURE;
    }

    //kopieren des Headers in die Outputfile
    //pixelwerte starten nach RGB-werten (Anzahl Farben * 3) + 5 (2B für Höhe, 2B für Breite, 1B für Farbenanzahl)
    unsigned long header_size = 5 + 3 * numberOfColors;

    header = (char*)malloc(sizeof(char) * header_size);
    if(header == NULL){
        printf("[FAILURE]    allocating Memory for OutputBuffer.\n");
        return EXIT_FAILURE;
    }

    for(unsigned long i = 0; i < header_size; i++){
        header[i] = input_buffer[i];
    }

	int input_pointer = header_size;		//naechster zu lesender char
	int output_pointer = 0;		//zeiger auf naechste stelle zum schreiben
	int char_counter = 0;		//anzahl der gleichen chars
	char char_value = 0;		//wert der folgenden chars

	//schleife ueber den restlichen input
	for(unsigned long x = input_pointer; x < inputfileSize; x+=2){
		
		//berechnen der anzahl der chars
		char_counter = (int) input_buffer[x];

		//auslesen des werts der chars
		char_value = input_buffer[x + 1];

		//testen der restlichen groesse
		if(pixeldata_size < output_pointer + char_counter){
			return -1;
		}

		//schreiben in den buffer
		for(int i = 0; i < char_counter; i++){
			pixeldata[output_pointer + i] = char_value;
		}

		//aktualisieren der Zeiger
		output_pointer += char_counter;
	}

    //schreiben der Outputfile
    //Oeffnen der Datei
    FILE *output = fopen(outFilePath, "wb");
    if(output == NULL){
		printf("[FAILURE]    opening OutputFile\n");
		return EXIT_FAILURE;
	}

    //schreiben des headers
    fwrite(header, 1, sizeof(char) * header_size, output);
    if(ferror(output)){
		printf("[FAILURE]    writing the Output File.\n");
		return EXIT_FAILURE;
	}

    //schreiben der Pixeldaten
    fwrite(pixeldata, 1, sizeof(char) * output_pointer, output);
    if(ferror(output)){
		printf("[FAILURE]    writing the Output File.\n");
		return EXIT_FAILURE;
	}

	free(input_buffer);
	free(pixeldata);
	free(header);
    printf("[Success] C_decode\n");
	return (sizeof(char)*header_size+sizeof(char)+output_pointer);
}



//--------------------------------------------
//----------	KONVERTIERUNG		----------
//--------------------------------------------

//	Methode zum konvertieren von .gepasp/.team104 zu .bmp
int convertToBMP(char *InputFilePath, char *OutputFilePath){

    printf("[Start] convert\n");
    //----------    ANMERKUNG   ----------
    /*Wir sind uns bewusst, dass es die Moeglichkeit gibt, BI_RLE8 (8bit lauflaengenkodierte) Bitmaps zu erstellen.
     * Dennoch wird die .team104 Datei vor dem schreiben in die .bmp Datei dekodiert und die Pixelwerte einzeln
     * in die Ausgabedatei geschrieben. Der Grund hierfür ist, dass bei BI_RLE8 Bitmaps die Pixelwerte 0 und 1 nicht
     * für Farben sondern das Ende einer Zeile bzw. das Ende der Bitmap stehen.
     * Da der Teil der Konvertierung zu einem mit einem Bildbetrachter ansehbaren Format nicht zu unserer Aufgabe
     * gehört, ahben wir uns für die folgende Umsetzung entschieden:*/

	//untersuchen des inputs auf kompression
	unsigned int compression_type = 0; //(0 = mit komprimiert, 1 = lauflangenkodiert)

    if(strstr(InputFilePath, ".gepasp") != NULL){
        compression_type = 0;
    } else if(strstr(InputFilePath, ".team104") != NULL){
        compression_type = 1;
    }

	//Dateien
	char *input_buffer;
	FILE *image;

	//structs
	bitmap_file_header bfh;
	bitmap_info_header bih;
	bitmap my_bitmap;

	//Daten
	int height;
	int width;
	int numberOfColors;
	int image_size;
	int file_size;

	//Oeffnen der Input-Datei
	FILE *input_file=fopen(InputFilePath,"r");
	if(input_file == NULL){
        printf("[FAILURE]    opening file");
		return EXIT_FAILURE;
    }

    //berechnen der Groesse
    fseek(input_file, 0, SEEK_END);
    unsigned long inputfileSize = ftell(input_file);
    rewind(input_file);

    //Speicherallokation Buffer
    input_buffer = (char*) malloc(sizeof(char)*inputfileSize);
    if(input_buffer == NULL){
        printf("[FAILURE]    allocating Memory for InputBuffer.\n");
        return EXIT_FAILURE;
    }

	//Einlesen der Datei in den Buffer
    size_t readSize = fread(input_buffer,sizeof(char),inputfileSize,input_file);
    if (readSize != inputfileSize) {
        printf("[FAILURE]    reading the Input File.\n");
		return EXIT_FAILURE;
    }

	//Auslesen der Daten aus dem Header (bei .gepasp und .team104 gleich)
	//Berechnung der Hoehe
    height = input_buffer[0];
    int tmp = input_buffer[1];
    if(tmp !=0){
        int x = tmp/16;
        int y = tmp%16;
        height += x*pow((double)16,(double)3);
        height += y*pow((double)16,(double)2);
    }

    //Berechnung der Breite
    width = input_buffer[2];
    tmp = input_buffer[3];
    if(tmp !=0){
        int v = tmp/16;
        int w = tmp%16;
        width += v*pow((double)16,(double)3);
        width += w*pow((double)16,(double)2);
    }

    //Auslesen der Anzahl von Farben (+1)
	numberOfColors = input_buffer[4] + 1;

    //Berechnen der Bildgroesse
    image_size = width * height;

    //Berechnen der Datei-Groesse
    file_size = image_size + sizeof(bitmap) + sizeof(rgb_quad) * numberOfColors;

    //Befüllen der Structs
	//BITMAP FILE HEADER:
    bfh.bitmap_type = 19778;
	bfh.file_size = file_size;
	bfh.reserved1 = 0;
	bfh.reserved2 = 0;
	bfh.offset_bits = sizeof(bitmap) + sizeof(rgb_quad) * numberOfColors;

	//BITMAP INFO HEADER:
	bih.size_header = sizeof(bitmap_info_header);
	bih.width = width;
	bih.height = height;
	bih.planes = 1;
	bih.bit_count = 8;
	bih.compression = 0;
	bih.image_size = image_size;
	bih.ppm_x = 0x0B13;
	bih.ppm_y = 0x0B13;
	bih.clr_used = numberOfColors;
	bih.clr_important = 0;

    //Erstellen der Farb-Tabelle
	rgb_quad color_table[numberOfColors];

	//Einfuellen der Farbwerte aus dem Buffer (RGB --> BGR0)
	unsigned long input_pointer = 5; //(2 Byte Hoehe, 2 Breite, 1 Byte Anzahl der Farben)
	for(int i = 0; i < numberOfColors; i++){
		color_table[i].blue = input_buffer[input_pointer];
		color_table[i].green = input_buffer[input_pointer + 1];
		color_table[i].red = input_buffer[input_pointer + 2];
		color_table[i].reserved = 0;
		input_pointer += 3;
	}

	//Oeffnen der Datei
	image = fopen(OutputFilePath, "wb");
    if(image == NULL){
        printf("[FAILURE]    opening file");
		return EXIT_FAILURE;
    }

    //Schreiben der Structs in die Bitmap
	my_bitmap.bfh = bfh;
    my_bitmap.bih = bih;
    bitmap *bitmap_pointer = &my_bitmap;
    fwrite(bitmap_pointer, 1, sizeof(bitmap), image);
    if(ferror(image)){
		printf("[FAILURE]    writing the Output File.\n");
		return EXIT_FAILURE;
	}

	//Schreiben der Farb-Tabelle in die Datei
	fwrite(color_table, 1, sizeof(rgb_quad)*numberOfColors, image);
    if(ferror(image)){
		printf("[FAILURE]    writing the Output File.\n");
		return EXIT_FAILURE;
	}

    //Erstellen eines Pixel-Buffers
    int pixels_size = file_size - input_pointer;
	char* pixels = (char*) malloc(sizeof(char) * pixels_size);
    if(pixels == NULL){
        printf("[FAILURE]    allocating Memory for OutputBuffer.\n");
        return EXIT_FAILURE;
    }

    //Auswertung des Buffers je nach Format (komprimiert oder unkomprimiert)
    int arraypointer = 0;
    if(compression_type == 1){
        //pixelwerte sind komprimiert/kodiert
        int number_pixels = 0;
        int pixel_value = 0;
        //schleife ueber den Rest des Input-Buffers
        for(unsigned long x = input_pointer; x < inputfileSize; x += 2){
            //Auslesen der Pixel-Anzahl
            number_pixels= input_buffer[x];
            pixel_value = input_buffer[x + 1];

            //Schreiben der Pixel in den Buffer
            for(int i = 0; i < number_pixels; i++){
                pixels[arraypointer + i] = pixel_value;
            }

            //aktualisieren des Pointers
            arraypointer += number_pixels;
        }
    }else{
        //Pixelwerte unkomprimiert --> Werte einfach kopieren
        for (unsigned long i = input_pointer; i < inputfileSize; i++){
            pixels[arraypointer] = input_buffer[i];
            arraypointer++;
        }
    }

	//Schreiben des Buffers in die Datei
    fwrite(pixels, 1, sizeof(char) * pixels_size, image);
    if(ferror(image)){
		printf("[FAILURE]    writing the Output File.\n");
		return EXIT_FAILURE;
	}

    //Beenden der Funktion
	fclose(image);
	free(input_buffer);
	free(pixels);
    printf("[Success] convert\n");
	return 0;
}


//--------------------------------------------
//----------	I/O-Operationen		----------
//--------------------------------------------
int main(int argc, char* argv[]){
    if(argc != 4){
		if(argc == 2){
			if(strncmp((char *)argv[1], "-help", 5)==0 || strncmp((char *)argv[1], "-h", 1)==0){
				printHelp(argv[0]);
				return EXIT_SUCCESS;
			}
		}else{
			printHelp(argv[0]);
			return EXIT_FAILURE;
		}
	}
    if(strncmp((char *)argv[1], "-asm_encode", 7)==0){
        if(strstr(argv[2], ".gepasp") != NULL && strstr(argv[3], ".team104") != NULL){
           if(1 < asm_encode(argv[2], argv[3])){
			   return EXIT_SUCCESS;
		   } else {
			   return EXIT_FAILURE;
		   }
        }
    }else if(strncmp((char *)argv[1], "-c_encode", 9)==0){
        if(strstr(argv[2], ".gepasp") != NULL && strstr(argv[3], ".team104") != NULL){
            if(1 < c_encode(argv[2], argv[3])){
                return EXIT_SUCCESS;
            } else {
                return EXIT_FAILURE;
            }
        }
    } else if(strncmp((char *)argv[1], "-asm_decode", 7)==0){
         if(strstr(argv[2], ".team104") != NULL && strstr(argv[3], ".gepasp") != NULL){
            if(1 < asm_decode(argv[2], argv[3])){
			   return EXIT_SUCCESS;
		   } else {
			   return EXIT_FAILURE;
		   }
        }
	}else if(strncmp((char *)argv[1], "-c_decode", 9)==0){
        if(strstr(argv[2], ".team104") != NULL && strstr(argv[3], ".gepasp") != NULL){
            if(1 < c_decode(argv[2], argv[3])){
                return EXIT_SUCCESS;
            } else {
                return EXIT_FAILURE;
            }
        }
    } else if(strncmp((char *)argv[1], "-convert", 8)==0){
         if((strstr(argv[2], ".team104") != NULL || (strstr(argv[2], ".gepasp") != NULL))&& strstr(argv[3], ".bmp") != NULL) {
             if (EXIT_SUCCESS == convertToBMP(argv[2], argv[3])) {
                 return EXIT_SUCCESS;
             } else {
                 return EXIT_FAILURE;
             }
         }
    } else {
       printHelp(argv[0]);
       return EXIT_FAILURE;
    }
}
