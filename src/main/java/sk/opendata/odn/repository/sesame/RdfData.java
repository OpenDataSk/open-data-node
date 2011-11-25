package sk.opendata.odn.repository.sesame;

/**
 * This class holds everything what is necessary to push some RDF data into
 * Sesame repository.
 */
public class RdfData {
	String rdfData;
	String rdfBaseURI;
	
	public RdfData(String rdfData, String rdfBaseURI) {
		super();
		this.rdfData = rdfData;
		this.rdfBaseURI = rdfBaseURI;
	}

	public String getRdfData() {
		return rdfData;
	}

	public void setRdfData(String rdfData) {
		this.rdfData = rdfData;
	}

	public String getRdfBaseURI() {
		return rdfBaseURI;
	}

	public void setRdfBaseURI(String rdfBaseURI) {
		this.rdfBaseURI = rdfBaseURI;
	}
}
