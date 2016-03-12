CLASSPATH = gson-2.6.2.jar
SOURCEPATH = .
DOCPATH = ../docs

all: compile

setup:
	export CLASSPATH=$(CLASSPATH)	

compile:
	javac -cp $(CLASSPATH) -sourcepath $(SOURCEPATH) *.java

javadoc:
	javadoc -cp $(CLASSPATH) -d $(DOCPATH) *.java

clean:
	rm -f *.class
