package sk.opendata.odn.serialization.rdf;

import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

import sk.opendata.odn.model.OrganizationRecord;
import sk.opendata.odn.repository.OdnRepositoryException;
import sk.opendata.odn.repository.OdnRepositoryInterface;
import sk.opendata.odn.repository.sesame.RdfData;

/**
 * This class is used by a Harvester to serialize organization records into RDF
 * and store them in Repository.
 */
public class OrganizationRdfSerializer {
	
	public final static String NS_RDF = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
	public final static String NS_SKOS = "http://www.w3.org/2004/02/skos/core#";
	public final static String NS_DC = "http://purl.org/dc/elements/1.1/";
	public final static String NS_OPENDATA = "http://sk.eea.opendata/2011/02/opendicts";
	
	public final static String OPENDATA_DATE_FORMAT = "dd.MM.yyyy";
	// TODO: do we need that configurable? if we want the that RDF data
	// accessible over the net via that URL/URI (which is encouraged) wit would
	// be either nice to "guess" is correctly from some other configuration or
	// have it in some per-ODN repository configuration
	public final static String OPENDATA_BASE_URI = "http://opendata.sk/dataset/organizations/";
	
	public final static String ERR_CONVERSION = "unable to convert the data into RDF";
	
	private final static SimpleDateFormat sdf = new SimpleDateFormat(
			OPENDATA_DATE_FORMAT);
	
	private OdnRepositoryInterface<RdfData> repository;
	private String repoName;
	
	private DocumentBuilder docBuilder;
	private Transformer transformer;
	
	/**
	 * Initialize serializer to use given repository.
	 * 
	 * @param repository
	 *            repository to use for storage of record
	 * @param name
	 *            name of the storage/back-end to store into
	 * 
	 * @throws ParserConfigurationException
	 *             when XML document builder fails to initialize
	 * @throws TransformerConfigurationException
	 *             when XML document transformer fails to initialize
	 */
	public OrganizationRdfSerializer(OdnRepositoryInterface<RdfData> repository,
			String name) throws ParserConfigurationException,
			TransformerConfigurationException {
		
		this.repository = repository;
		this.repoName = name;
		
		DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
		docBuilder = docBuilderFactory.newDocumentBuilder();
		
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
        transformer = transformerFactory.newTransformer();
        // TODO: nice for debugging, but might hurt performance in production
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
	}
	
	private Element appendTextNode(Document doc, String name, String value) {
		Element element = doc.createElement(name);
		Text textNode = doc.createTextNode(value);
		element.appendChild(textNode);
		return element;
	}
	
	private Element appendResourceNode(Document doc, String name, String attr,
			String value) {
		
		Element element = doc.createElement(name);
		element.setAttribute(attr, value);
		return element;
	}
	
	/**
	 * Serialize into RDF one given record and store the result in given
	 * 'concept' (which in turn is in given 'doc').
	 * 
	 * @param doc
	 *            XML document we are serializing into
	 * @param concept
	 *            XML document element used to store the serialization of given
	 *            record
	 * @param record
	 *            organization record to serialize into RDF
	 */
	private void orToRdf(Document doc, Element concept, OrganizationRecord record) {
	    concept.appendChild(appendTextNode(doc, "skos:prefLabel", record.getName()));
	    concept.appendChild(appendResourceNode(doc, "dc:source", "rdf:resource", record.getSource()));
	    // FIXME: That's NOT always the case with records from Datanest!!!
	    concept.appendChild(appendTextNode(doc, "dc:type", "nonprofit"));
	    if (record.getDateFrom() != null) {
	    	String dateFrom = sdf.format(record.getDateFrom());
	        concept.appendChild(appendTextNode(doc, "opendata:dateFrom", dateFrom));
	    }
	    if (record.getDateTo() != null) {
	    	String dateTo = sdf.format(record.getDateTo());
	        concept.appendChild(appendTextNode(doc, "opendata:dateTo", dateTo));
	    }
	    concept.appendChild(appendTextNode(doc, "opendata:seat", record.getSeat()));
	    concept.appendChild(appendTextNode(doc, "opendata:ico", record.getIco()));
	}
	
	public String toRdf(Vector<OrganizationRecord> orgRecords) throws TransformerException {
		Document doc = docBuilder.newDocument();
		
		Element rdfElement = doc.createElementNS(NS_RDF, "rdf:RDF");
		rdfElement.setAttribute("xmlns:rdf", NS_RDF);
		rdfElement.setAttribute("xmlns:skos", NS_SKOS);
		rdfElement.setAttribute("xmlns:dc", NS_DC);
		rdfElement.setAttribute("xmlns:opendata", NS_OPENDATA);
		doc.appendChild(rdfElement);
		
		for (OrganizationRecord record : orgRecords) {
			Element concept = doc.createElement("skos:Concept");
			concept.setAttribute("rdf:about",
					"http://www.eea.sk/opendata/dicts/rno-%s" + record.getIco());
			orToRdf(doc, concept, record);
			
			rdfElement.appendChild(concept);
		}
		
		StringWriter sw = new StringWriter();
        StreamResult result = new StreamResult(sw);
        DOMSource source = new DOMSource(doc);
        transformer.transform(source, result);
        
		return sw.toString();
	}
	
	/**
	 * Serialize and store given organization record.
	 * 
	 * @param orgRecords
	 *            list of organization records to serialize and store
	 * 
	 * @throws TransformerException
	 *             when conversion into RDF fails
	 * @throws OdnRepositoryException
	 *             when we fail to store given data into repository
	 * @throws IllegalArgumentException
	 *             if repository with given name does not exists
	 */
	public void store(Vector<OrganizationRecord> orgRecords)
			throws TransformerException, IllegalArgumentException, OdnRepositoryException {
		
		RdfData rdfData = new RdfData(
				toRdf(orgRecords),
				OPENDATA_BASE_URI);
		repository.store(repoName, rdfData);
	}

}
