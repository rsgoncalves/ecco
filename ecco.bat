:: This script is untested! Please let me know if any problems occur
:: 
:: ecco: a diff tool for OWL ontologies
:: Copyright 2011-2013, The University of Manchester
::
:: This script builds (if necessary) and runs ecco. This requires 
:: Java 1.7 installed, and the default JRE. Additionally, if building 
:: from sources, ant must be installed.
:: 
:: Last updated: 7-May-13
:: 
:: Compile sources and produce the jar and javadocs (if ecco.jar does not exist)
if not exist ecco.jar echo Building ecco from sources... && ant
::
:: Run ecco with the specified arguments
:: Maximum heap space is set to: 4GB, and Java library path to: .\lib
java -Xmx4G -Djava.library.path=".\lib" -DentityExpansionLimit=100000000 -jar ecco.jar %*