@echo off
color 2
echo Generating Parser with Coco...
cd ..\..\..\..\com\oracle\truffle\cmm\parser
del Parser.java
del Scanner.java
java -jar Coco.jar CMM.atg
pause >nul