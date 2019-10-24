default out = "stdout";
default file = FLUX_DIR + "Test_DNB_Mono.xml";
default id = "1025374754";
default sector = "sec_002";
default media_type = "mediatype_003";

file|
open-file|
//"1025374754"|
//id|
//template("https://portal.dnb.de/opac.htm?method=requestMarcXml&idn=${o}")| 
//open-http|
decode-xml|
handle-marcxml|
morph(FLUX_DIR + "MARC21-EDM.xml", *)|
add-oreaggregation|
rdf-macros|
encode-xml(roottag="rdf:RDF", recordtag="", namespacefile= FLUX_DIR+"edm-namespaces.properties")|
write(out);
