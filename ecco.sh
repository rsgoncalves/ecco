#!/bin/bash
# 
# ecco: a diff tool for OWL 2 ontologies
# Copyright 2011-2014, The University of Manchester
#
# This script builds (if necessary) and runs ecco. The requirements
# to build and run ecco are Java 1.7 (or above) and Apache Maven.
# 
# Last updated: 17-Feb-15
# 
# Build project if ecco.jar does not exist
if [ ! -f target/ecco.jar ];
then
	if [ -d target ]
	then
		echo "cleaning up..."$'\n'
		mvn clean
	fi
	echo $'\n'"building ecco..."$'\n'
	mvn install
	echo "done"$'\n'
fi
# 
# Set the maximum memory to be used, by default, 8GB
maxmem="8G"
#
# deprecated: Pointer to lib folder
# lib=`pwd`"/target/lib"  -Djava.library.path="$lib" 
#
# Run ecco with the specified arguments
echo "starting ecco..."
java -Xmx"$maxmem" -DentityExpansionLimit=100000000 -jar target/ecco.jar $*