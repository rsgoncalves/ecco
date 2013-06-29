*ecco*
====

#### a diff tool for OWL ontologies ####

Built using the [OWL API](http://owlapi.sourceforge.net/). For demo purposes there is a Web-based front-end [here](http://owl.cs.manchester.ac.uk/diff) (alternatively [here](http://rpc440.cs.man.ac.uk:8080/diff)).


intro
--------------------

*ecco* identifies differences according to OWL's notion of [structural equivalence](http://www.w3.org/TR/owl2-syntax/#Structural_Specification),
and then verifies whether these differences produce any *effect on entailments*. Subsequently these differences are aligned according to their impact (or lack thereof). For more details and literature pointers, check [this webpage](http://owl.cs.manchester.ac.uk/research/topics/diff/). It contains links to published papers with the relevant definitions.


usage
--------------------

`[SCRIPT]` **-ont1** `[ONTOLOGY]` **-ont2** `[ONTOLOGY]` `[OPTIONS]`

* `[SCRIPT]` in **Windows**: use *ecco.bat*, in **UNIX**-based systems: use *ecco.sh*

* `[ONTOLOGY]` an input ontology file path or URL

* `[OPTIONS]`
    * `-o`    Output directory for generated files
    * `-t`    Transform XML diff report into HTML
    * `-s`    Compute structural diff only
    * `-l`	Compute logical diff only
    * `-r`    Analyse root ontologies only, i.e. ignore imports
    * `-n`    Normalize entity URIs, i.e. if two ontologies have the same entity names in a different namespace, this trigger establishes a common namespace
    * `-x`		File path of XSLT file
    * `-i`		Ignore Abox axioms
    * `-j`		Maximum number of justifications computed per ineffectual change
    * `-v`		Verbose mode
    * `-h` `-help`	Print this help message

The standard output of *ecco* is an XML file representing the change set. With the **-t** flag, *ecco* will transform this XML file into HTML using the supplied XSLT file (if the location of this file changes, specify the new location via **-x**). If an output directory is not specified (via **-o**), the HTML file is saved in the **_/out_** (or **_\out_** in Windows) folder, which contains the required files for appropriate rendering on a Web browser. For a more informative progress monitoring, use the **-v** flag.


before running
--------------------
Make sure that **Java 1.7** is installed and the default Java runtime environment. In order to execute properly, *ecco* needs the appropriate native library of FaCT++ for your operating system. 
This library, denoted *FaCT++ version 1.6.2; precompiled [OS] binaries* should be obtained from [here](https://code.google.com/p/factplusplus/downloads/list).
Afterwards, the *single* appropriate file (in Windows a **.dll**, in Linux a **.so**, or in Mac OS X a **.jnlib** file) should
be moved into the **_lib_** folder.


future plans
--------------------

* Identification of **affected concepts** according to different entailment grammars
* Alignment of concepts and axioms


dependencies
--------------------

*ecco* relies on the following projects:

 * [OWL API](http://owlapi.sourceforge.net/) (v3.4.5)
 * [HermiT](http://www.hermit-reasoner.com/) reasoner (v1.3.8)
 * [FaCT++](https://code.google.com/p/factplusplus/) reasoner (v1.6.2)
 
 
license
--------------------
*ecco* is distributed under the terms of the GNU Lesser General Public License (LGPL), Version 3.0. For licenses of projects *ecco* relies on, check the relevant websites.