.PHONY: build, run
default: build


build:
	javac -Xlint:none -cp .:./pdfbox ./ir/*.java 

run: build 
	java -Xmx1024m -cp .:pdfbox ir.SearchGUI -d ./davisWiki 
	#java -Xmx1024m -cp .:pdfbox ir.SearchGUI -d ./testData

