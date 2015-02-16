#!/bin/bash
# 
# ecco: a diff tool for OWL 2 ontologies
# Copyright 2011-2014, The University of Manchester
#
# This script builds (if necessary) and runs ecco on a set of versions. The requirements
# to build and run ecco are Java 1.7 (or above) and Apache Maven. The set of versions
# must be organised either as appropriately-sorted files within a folder, or a set of
# folders: each containing an ontology whose filename must be specified here.
# 
# For ecco's optional arguments (that can be passed onto it from here), do: sh ecco.sh -h
# 
# Last updated: 17-Feb-15
# 
# Build project if ecco.jar does not exist
if [ -f target/ecco.jar ] 
	then
		echo "Building ecco (this requires an internet connection to fetch dependencies)"$'\n'
		mvn install
		echo "done"$'\n'
fi
#
# Default argument values (which can be altered via options below)
# 
maxmem="8G"		# Maximum heap space allocated to the JVM. Default: 8GB
lib=`pwd`"/lib"	# Java library folder
# 
# Iterate arguments
#
ontbase=""
ontname=""
usage(){
	echo ""
	echo "Usage: sh ecco.sh -b DIRECTORY [-o FILENAME] [-m MEMORY] [-f DIRECTORY] [ECCO OPTIONS]"
	echo ""
	echo "   -b --base	Base folder where the the ontology files or folders are contained"
	echo "   -o --ont	If the 'base' folder contains folders, then -o must specify a single, universal ontology name"
	echo "   -m --mem	Maximum heap space (memory) allocated to the JVM. Default: 8G (8 GB)"
	echo "   -l --lib	Java library path. Default: ./lib/"
	echo ""
}
while :
do
    case $1 in
            -h | --help | -\?)
            usage
            exit 0
            ;;
        -b | --base)
            ontbase=$2
            echo "Ontology base folder: $2"
            shift 2
            ;;
        -o | --ont)
            ontname=$2
			echo "Ontology name: $2"
            shift 2
            ;;
        -m | --mem)
            maxmem=$2
			echo "JVM heap space: $2"
            shift 2
            ;;
        -l | --lib)
            lib=$2
			echo "Java lib path: $2"
            shift 2
            ;;
        --)
            shift
            break
            ;;
        -*)
            echo "Warning: Unknown option (ignored): $1" >&2
            break
            ;;
        *)
            break
            ;;
    esac
done
echo "ecco arguments: $@"
# 
# Ensure required argument is specified
# 
if [ ! "$ontbase" ]; then
    echo "Error: Required option '--base or -b DIRECTORY' not given. See --help" >&2
    exit 1
fi
#
# The input parameters beyond this script will go on to ecco. Note that -ont1 and -ont2
# are established here. So pass onto ecco only optional commands (like -v for verbose)
# 
versions=( $ontbase/* )
for (( i = 0 ; i < ${#versions[@]}-1 ; i++ ))
do
	if [ ! "$ontname" ]; then
		ont1=${versions[$i]}
		ont2=${versions[$i+1]}
	else
		ont1=${versions[$i]}/"$ontname"
		ont2=${versions[$i+1]}/"$ontname"
	fi
	# Run ecco with the specified arguments
	java -Xmx"$maxmem" -Djava.library.path="$lib" -DentityExpansionLimit=100000000 -jar target/ecco.jar -ont1 "$ont1" -ont2 "$ont2" "$@"
done