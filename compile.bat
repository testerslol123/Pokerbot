cd src
javac main/*.java
jar cmvf  META-INF/MANIFEST.MF ../Pokerbot.jar img/* main/*.class
cd main
del /f *.class