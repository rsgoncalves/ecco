:: This script is untested! Please let me know if any problems occur
:: 
:: ecco: a diff tool for OWL ontologies
:: Copyright 2011-2014, The University of Manchester
::
:: This script builds (if necessary) and runs ecco. The requirements
:: to build and run ecco are Java 1.7 (or above) and Apache Maven.
:: 
:: Last updated: 17-Feb-15
:: 
:: Build project if ecco.jar does not exist
@echo off
set mem=8G
set lib=".\lib"
if not exist target\ecco.jar (
	echo.
	echo building ecco...
	echo.
	call mvn install
	echo done
)
::
:: Run ecco with the specified arguments
:: Maximum heap space is set to: 8GB, and Java library path to: .\lib
echo.
echo starting ecco...
echo.
java -Xmx%mem% -Djava.library.path=%lib% -DentityExpansionLimit=100000000 -jar target\ecco.jar %*