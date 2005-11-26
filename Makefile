#@+leo
#@+node:0::@file Makefile
#@+body
# Makefile for SSLCrypto python extension

# if the make screws up, you might need to edit the 2 variables
# at the top of the file 'setup.py'.

SSLCrypto: SSLCrypto.so

SSLCrypto.so: src/SSLCrypto.c src/die.c
	python setup.py build_ext --inplace

clean:
	rm -rf SSLCrypto.so src/SSLCrypto.o src/die.o build

doco:
	epydoc -n "FreeMail API" -o doc/classes freemail.py

#@-body
#@-node:0::@file Makefile
#@-leo
