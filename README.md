*ecco*
====

#### a diff tool for OWL ontologies ####

Built using the [OWL API](http://owlapi.sourceforge.net/). For demo purposes there is a Web-based front-end [here](http://owl.cs.manchester.ac.uk/diff) (alternatively [here](http://rpc440.cs.man.ac.uk:8080/diff)).


intro
--------------------

*ecco* identifies changes according to OWL's notion of [structural equivalence](http://www.w3.org/TR/owl2-syntax/#Structural_Specification),
and then verifies whether these changes produce any *effect on entailments*, i.e. whether they are logically effectual or ineffectual. 
Subsequently these differences are aligned according to their impact (or lack thereof).

Additionally, *ecco* now has an option (`-c`) to detect which concepts had their meaning affected. This is done by checking which atomic concepts have different sub- or super-concepts, where the latter can be:

* `-c at`		Atomic concepts
* `-c sub`		Subconcepts asserted in either ontology
* `-c gr`		Concepts of the form `r some C`, `r only C`, `not C`, `C or C'`, `C and C'`, where `C, C'` are subconcepts asserted in either ontology, and `r` is an atomic role
* `-c cvs`		Concepts of the form `r some A`, `r only A`, `not A`, where `A` is an atomic concept, and `r` is an atomic role

Note that these are all sound but incomplete approximations of the *minimal change set*, as defined [here](http://www.cs.man.ac.uk/~goncalvj/files/2012_iswc_diff.pdf). The last option: `-c cvs` is based on the entailment grammar employed in [ContentCVS](http://www.cs.ox.ac.uk/isg/tools/ContentCVS).

After detecting the set of affected concepts, *ecco* distinguishes between concepts that are directly or indirectly affected, that is, whether some concept `A` changed due to a change in some concept `B`, where both ontologies entail that `A => B`.

<!-- For more details and literature pointers, check [this webpage](http://owl.cs.manchester.ac.uk/research/topics/diff/). It contains links to published papers with the relevant definitions. -->


usage
--------------------

`[SCRIPT]` **-ont1** `[ONTOLOGY]` **-ont2** `[ONTOLOGY]` `[OPTIONS]`

* `[SCRIPT]` in **Windows**: use *ecco.bat*, in **UNIX**-based systems: use *ecco.sh*

* `[ONTOLOGY]` an input ontology file path or URL

* `[OPTIONS]`
    * `-o`    Output directory for generated files
    * `-t`    Transform XML diff report into HTML
    * `-c`    Compute one of: [ at | sub | gr | cvs ] concept diff
    * `-r`    Analyse root ontologies only, i.e. ignore imports
    * `-n`    Normalize entity URIs, i.e. if two ontologies have the same entity names in a different namespace, this trigger establishes a common namespace
    * `-x`		File path of XSLT file
    * `-i`		Ignore Abox axioms
    * `-j`		Maximum number of justifications computed per ineffectual change
    * `-v`		Verbose mode
    * `-h` `-help`	Print this help message

The standard output of *ecco* is an XML file representing the change set. With the **-t** flag, *ecco* will transform this XML file into HTML using the supplied XSLT file (if the location of this file changes, specify the new location via the **-x** flag). By default, the output goes to the **_/out_** (or **_\out_** in Windows) folder, which contains the required files for appropriate rendering on a Web browser. This can be altered via the **-o** flag, though make sure the contents of the default output folder are shifted over to the new output location. For a more informative progress monitoring, use the **-v** flag.


before running
--------------------
Make sure that **Java 1.7** is installed and the default Java runtime environment. In order to execute properly, *ecco* needs the appropriate native library of FaCT++ for your operating system. 
This library, denoted *FaCT++ version 1.6.2; precompiled [OS] binaries* should be obtained from [here](https://code.google.com/p/factplusplus/downloads/list).
Afterwards, the *single* appropriate file (in Windows a **.dll**, in Linux a **.so**, or in Mac OS X a **.jnlib** file) should
be moved into the **_lib_** folder.


future plans
--------------------

* An alternative, term-centric view of the differences


dependencies
--------------------

*ecco* relies on the following projects:

 * [OWL API](http://owlapi.sourceforge.net/) (v3.4.5)
 * [HermiT](http://www.hermit-reasoner.com/) reasoner (v1.3.8)
 * [FaCT++](https://code.google.com/p/factplusplus/) reasoner (v1.6.2)
 
 
license
--------------------
*ecco* is distributed under the terms of the GNU Lesser General Public License (LGPL), Version 3.0. For licenses of projects *ecco* relies on, check the relevant websites.