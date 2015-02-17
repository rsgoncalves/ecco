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
    * `-i`		Ignore Abox axioms
    * `-j`		Maximum number of justifications computed per ineffectual change
    * `-v`		Verbose mode
    * `-h` `-help`	Print this help message

The standard output of *ecco* is an XML file representing the change set. With the **-t** flag, *ecco* will transform this XML file into HTML. 

By default, the output goes to a folder named **_ecco-output_**, which contains the required files for appropriate rendering on a Web browser. The output folder can be altered via the **-o** flag. For a more informative progress monitoring, use the **-v** flag.

In order to deploy *ecco* on a set of versions, the script `eccov.sh` can be used. Instructions for doing so are in the script itself.


deployment
--------------------
*ecco* is compatible with **Java 1.7 (or above)**, and requires [Apache Maven](http://maven.apache.org/) to be built from sources.

The tool relies directly on the following projects:

 * [OWL API](http://owlapi.sourceforge.net/) (v3.5.1)
 * [HermiT](http://www.hermit-reasoner.com/) reasoner (v1.3.8)
 * [JFact](http://jfact.sourceforge.net/) reasoner (v1.2.2)
 * [Guava](https://github.com/google/guava) (v14.0.1)
 * [Saxon-HE](http://saxon.sourceforge.net/) XSLT and XQuery processor (v9.6.0)
 * [apache-commons-io](http://commons.apache.org/proper/commons-io/) (v2.4)
 

future plans
--------------------
* An alternative, term-centric view of the differences
