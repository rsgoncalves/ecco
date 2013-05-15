#!/bin/bash
# 
# ecco: a diff tool for OWL ontologies
# Copyright 2011-2013, The University of Manchester
#
# This script builds (if necessary) and runs ecco. This requires 
# Java 1.7 installed, and the default JRE. Additionally, if building 
# from sources, ant must be installed.
# 
# Last updated: 7-May-13
# 
# Compile sources and produce the JAR and javadocs
[ -f ecco.jar ] || (echo "Building ecco from sources..." && ant)
# 
# Set the maximum memory to be used, by default, 4GB
maxmem="4G"
#
# Library folder to load FaCT++'s native library
lib=`pwd`"/lib" 
#
# Run ecco with the specified arguments
java -Xmx"$maxmem" -Djava.library.path="$lib" -jar ecco.jar $*