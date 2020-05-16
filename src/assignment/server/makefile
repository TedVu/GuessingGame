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
default: DriverServer.class Server.class StartGameThread.class UDPClientHandler.class ClientGameHandler.class ClientRegistrationHandler.class RepromptForRegistrationHandler.class CommunicationCode.class 

# building file

DriverServer.class: DriverServer.java
	$(JCC) $(JFLAGS) DriverServer.java

Server.class: Server.java
	$(JCC) $(JFLAGS) Server.java

StartGameThread.class: StartGameThread.java
	$(JCC) $(JFLAGS) StartGameThread.java

UDPClientHandler.class: UDPClientHandler.java
	$(JCC) $(JFLAGS) UDPClientHandler.java

ClientGameHandler.class: ClientGameHandler.java
	$(JCC) $(JFLAGS) ClientGameHandler.java

ClientRegistrationHandler.class: ClientRegistrationHandler.java
	$(JCC) $(JFLAGS) ClientRegistrationHandler.java

RepromptForRegistrationHandler.class: RepromptForRegistrationHandler.java
	$(JCC) $(JFLAGS) RepromptForRegistrationHandler.java

CommunicationCode.class: CommunicationCode.java
	$(JCC) $(JFLAGS) CommunicationCode.java


# To start over from scratch, type 'make clean'.  
# Removes all .class files, so that the next make rebuilds them
#
clean: 
	$(RM) *.class
