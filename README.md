*ecco*
====

#### a Java-based diff tool for OWL ontologies ####

Built using the [OWL API](http://owlapi.sourceforge.net/). For demo purposes there is a Web-based front-end [here](http://ecco-rsgtools.rhcloud.com/).


summary
--------------------
*ecco* identifies changes according to OWL's notion of [structural equivalence](http://www.w3.org/TR/owl2-syntax/#Structural_Specification),
and then verifies whether these changes produce any *effect on entailments*, i.e. whether they are logically effectual or ineffectual. 
Subsequently these differences are aligned according to their impact (or lack thereof).

Additionally, *ecco* now has an option (`-c`) to detect which concepts had their meaning affected by axiom changes. This is done by checking which (atomic) concepts have different sub- or super-concepts between ontologies, where the latter can be:

* `-c at`		Atomic concepts
* `-c sub`		Subconcepts asserted in either ontology
* `-c gr`		Concepts of the form `r some C`, `r only C`, `not C`, `C or C'`, `C and C'`, where `C, C'` are subconcepts asserted in either ontology, and `r` is an atomic role
* `-c cvs`		Concepts of the form `r some A`, `r only A`, `not A`, where `A` is an atomic concept, and `r` is an atomic role

Note that these are all sound but incomplete approximations of the *minimal [concept] change set*, as defined [here](http://www.cs.man.ac.uk/~goncalvj/files/2012_iswc_diff.pdf). The last option: `-c cvs` is based on the entailment grammar employed in [ContentCVS](http://www.cs.ox.ac.uk/isg/tools/ContentCVS).

After detecting the set of affected concepts, *ecco* distinguishes between concepts that are directly or indirectly affected, that is, whether some concept `A` changed due to a change in some concept `B`, where both ontologies entail that `A => B`. 

Finally, axiom changes are aligned with the concepts that they affect; these are shown on the right hand side columns. When hovering over affected concepts, the tool will show the entailment differences for each concept change.

For full details, check [my thesis](https://www.escholar.manchester.ac.uk/uk-ac-man-scw:220347), particularly Chapter 7 where a tool walkthrough is carried out.


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


deployment
--------------------
*ecco* is compatible with **Java 1.7 (or above)**. It was tested with Java 1.8, and relies mainly on the following projects:

 * [OWL API](http://owlapi.sourceforge.net/) (v4.0.1)
 * [HermiT](http://www.hermit-reasoner.com/) reasoner (v1.3.8)
 * [JFact](http://jfact.sourceforge.net/) reasoner (v4.0.0)


future plans
--------------------
* An alternative, term-centric view of the differences
