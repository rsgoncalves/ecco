#!/bin/bash
# 
# ecco: a diff tool for OWL ontologies
# Copyright 2011-2013, The University of Manchester
#
# This script builds (if necessary) and runs ecco on a set of versions. The set of
# versions must be organised either as appropriately-sorted files within a folder, or
# a set of folders: each containing an ontology whose filename must be specified here.
# 
# For ecco's optional arguments (that can be passed onto it from here), do: sh ecco.sh -h
# 
# Last updated: 9-Jul-13
# 
# Compile sources and produce the jar and javadocs (if ecco.jar does not exist)
[ -f ecco.jar ] || (echo "Building ecco from sources..." && ant)
#
# Default argument values (which can be altered via options below)
# 
maxmem="4G"	# Maximum heap space allocated to the JVM. Default: 4GB
factlib=`pwd`"/lib"	# Library folder to load FaCT++'s native library
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
	echo "   -m --mem	Maximum heap space (memory) allocated to the JVM. Default: 4G (4 GB)"
	echo "   -f --fact	Directory where the FaCT++ native library resides. Default: ./lib/ "
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
        -f | --fact)
            factlib=$2
			echo "FaCT++ lib: $2"
            shift 2
            ;;
        --)
            shift
            break
            ;;
        -*)
            echo "Warning: Unknown option (ignored): $1" >&2
            shift
            ;;
        *)
            break
            ;;
    esac
done
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
	java -Xmx"$maxmem" -Djava.library.path="$factlib" -DentityExpansionLimit=100000000 -jar ecco.jar -ont1 "$ont1" -ont2 "$ont2"
done