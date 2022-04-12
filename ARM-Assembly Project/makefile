CFLAGS= -O3 -g -Wall -Wextra

.PHONY: all
all: run-length-encode
run-length-encode: Run-Length-Encoding.c Run-Length-Encoding.S Run-Length-Decoding.S
	$(CC) $(CFLAGS) -o $@ $^