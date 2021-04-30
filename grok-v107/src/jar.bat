clear
rm -f jgrok.jar
"c:/Program Files/Java/jdk1.6.0_10/bin/jar.exe" cv org/gnu/readline/*.class ca/uwaterloo/cs/jgrok/*.class ca/uwaterloo/cs/jgrok/env/*.class ca/uwaterloo/cs/jgrok/fb/*.class ca/uwaterloo/cs/jgrok/interp/*.class ca/uwaterloo/cs/jgrok/interp/op/*.class ca/uwaterloo/cs/jgrok/interp/select/*.class ca/uwaterloo/cs/jgrok/io/*.class ca/uwaterloo/cs/jgrok/io/ta/*.class ca/uwaterloo/cs/jgrok/lib/*.class ca/uwaterloo/cs/jgrok/lib/math/*.class ca/uwaterloo/cs/jgrok/lib/time/*.class ca/uwaterloo/cs/jgrok/test/*.class ca/uwaterloo/cs/jgrok/util/*.class > jgrok.jar
"c:/Program Files/Java/jdk1.6.0_10/bin/jar.exe" t < jgrok.jar
