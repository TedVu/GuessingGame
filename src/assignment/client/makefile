#
# A simple makefile for compiling three java classes
#

# define a makefile variable for the java compiler
#
JCC = javac

# define a makefile variable for compilation flags
# the -g flag compiles with debugging information
#
JFLAGS = -g

# typing 'make' will invoke the first target entry in the makefile 
# (the default one in this case)
#
default: CommunicationCode.class UDPClient.class Client.class DriverClient.class

# building file
CommunicationCode.class: CommunicationCode.java
	$(JCC) $(JFLAGS) CommunicationCode.java

UDPClient.class: UDPClient.java
	$(JCC) $(JFLAGS) UDPClient.java

Client.class: Client.java
	$(JCC) $(JFLAGS) Client.java

DriverClient.class: DriverClient.java
	$(JCC) $(JFLAGS) DriverClient.java
# To start over from scratch, type 'make clean'.  
# Removes all .class files, so that the next make rebuilds them
#
clean: 
	$(RM) *.class
