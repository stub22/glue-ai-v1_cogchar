
This resources directory contains RDF ontology models, using the OWL2 specification.
The model data is stored in turtle format, in files with extension ".ttl".
These models are viewable with a text editor, but are usually edited using
Protege v4.2 or higher, or a similar powerful ontology editor.

We also include the suffix "_owl2" in these model filenames to distinguish the vocabulary
they use.  "_rdfs" is another potential vocabulary we may use.

These ontology models are used throughout our development lifecycle to serve our software projects.
Here are some primary use cases:

----------------------------------------------------------------------------------------

1) At design time, 

these ontologies are used to define our semantic data models to properly
reflect our domain understanding. 

----------------------------------------------------------------------------------------

2) At project build time, 

a maven task executes jena schemagen, which generates one
*_owl2.java file for each of the _owl2.ttl files.  These .java files contain constants
for all of the well known entity-URIs found in the ontology, which are of 4 main kinds:

	Classes
	Individuals
	Object Properties
	Data Properties

These ontology-managed URIs form the entry points for all metadata feature and 
configuration of the glue.ai software.

----------------------------------------------------------------------------------------

3) At run time

The ontology files themselves are included as output resources in the org.cogchar.onto
jar file, and exported by the OSGi bundle:  org.cogchar.bundle.core.   Thus these models
may be loaded using any RDF-parsing feature, which may or may not be feeding an ontology-aware
pipeline, depending on the needs of the software implementation.   Most commonly a glue.ai
application accesses this ontology data using Jena APIs, although the Manchester OWL2 API
should also work (TODO:  Test it).  

The java constants from #2 above may be used as departure names for queries into the ontology
data, for example to retrieve 

	a) Type relationship information (i.e. the OWL2 schema)
	
	b) Object links between individuals, established using owl:ObjectProperty relationships, or other 
	statements with URIs in the object position of (s,p,o) triples.

	c) Primitive data (ints, strings, floats, dates) held in literals in an individual's owl:DataProperty
	relationships, or other statements with Literals in the in the object position of (s,p,o) triples.

----------------------------------------------------------------------------------------

4) For documentation

Comments in the ontologies (annotated using the rdfs:comment property) are preserved in the generated
*_owl2.java files output during the schemagen task above, #2.  Thus when Javadoc is produced from 
those *_owl2.java files, it produces orderly HTML output representing the ontology entities,
and containing the ontology constants.  Other Javadocs within the org.cogchar.lib.onto project 
may link into this Javadoc.

To see this Javadoc for the latest org.cogchar.lib.onto ontologies and code in Netbeans, right-click 
on the project and select "generate javadoc".

The Javadoc is also published as part of our maven full-release process.  Users of Cogchar may download
these released javadocs from Maven Central.

Generated ontology source 

What is the easiest way for us to cause our Maven-Central Javadocs to be auto-published on the interwebs?
Jarvana is gone.

GrepCode shows the generated Java source code - true?


