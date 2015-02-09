:: This script is untested! Please let me know if any problems occur
:: 
:: ecco: a diff tool for OWL ontologies
:: Copyright 2011-2014, The University of Manchester
::
:: This script builds (if necessary) and runs ecco. This requires 
:: Java 1.7+ installed, and the default JRE. Additionally, if building 
:: from sources, ant must be installed.
:: 
:: Last updated: 8-Feb-15
:: 
:: Compile sources and produce the jar and javadocs
@echo off
set mem=8G
set lib=".\lib"
echo.
echo Building ecco from sources...
echo.
call ant
::
:: Run ecco with the specified arguments
:: Maximum heap space is set to: 8GB, and Java library path to: .\lib
echo.
echo Starting ecco...
echo.
java -Xmx%mem% -Djava.library.path=%lib% -DentityExpansionLimit=100000000 -jar ecco.jar %*