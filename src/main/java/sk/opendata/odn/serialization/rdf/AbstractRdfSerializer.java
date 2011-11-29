package sk.opendata.odn.serialization.rdf;

import java.io.StringWriter;
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

import sk.opendata.odn.repository.OdnRepositoryException;
import sk.opendata.odn.repository.OdnRepositoryInterface;
import sk.opendata.odn.repository.sesame.RdfData;

/**
 * Stuff common to all OpenData.sk RDF serializers.
 * 
 * @param <RecordType>
 *            type of individual record which will be converted to RDF
 */
public abstract class AbstractRdfSerializer<RecordType> {

	public final static String NS_RDF = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
	public final static String NS_SKOS = "http://www.w3.org/2004/02/skos/core#";
	public final static String NS_DC = "http://purl.org/dc/elements/1.1/";
	public final static String NS_OPENDATA = "http://sk.eea.opendata/2011/02/opendicts";

	public final static String ERR_CONVERSION = "unable to convert the data into RDF";

	protected OdnRepositoryInterface<RdfData> repository;
	protected String repoName;

	protected DocumentBuilder docBuilder;
	protected Transformer transformer;

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
	public AbstractRdfSerializer(OdnRepositoryInterface<RdfData> repository,
			String name) throws ParserConfigurationException,
			TransformerConfigurationException {

		this.repository = repository;
		this.repoName = name;

		DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory
				.newInstance();
		docBuilder = docBuilderFactory.newDocumentBuilder();

		TransformerFactory transformerFactory = TransformerFactory
				.newInstance();
		transformer = transformerFactory.newTransformer();
		// TODO: nice for debugging, but might hurt performance in production
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
	}

	protected Element appendTextNode(Document doc, String name, String value) {
		Element element = doc.createElement(name);
		Text textNode = doc.createTextNode(value);
		element.appendChild(textNode);
		return element;
	}

	protected Element appendResourceNode(Document doc, String name,
			String attr, String value) {

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
	 *            record to serialize into RDF
	 */
	public abstract void recordToRdf(Document doc, Element concept,
			RecordType record);

	/**
	 * Override this method if you need to add custom RDF NS elements to the XML
	 * document.
	 * 
	 * @param rdfElement
	 *            RDF element of the XML document
	 */
	public void addCustomRdfNsElements(Element rdfElement) {
		// nothing to do if there are no custom elements needed
	}
	
	/**
	 * Derive an OpenData.sk's URI for the given record.
	 * 
	 * @param record
	 *            record for which we need to derive OpenData.sk's ID
	 * @return OpenData.sk's URI of the given record
	 */
	abstract public String getConceptRdfAbout(RecordType record);
	
	public String toRdf(Vector<RecordType> records)
			throws TransformerException {
		
		Document doc = docBuilder.newDocument();
		
		Element rdfElement = doc.createElementNS(NS_RDF, "rdf:RDF");
		rdfElement.setAttribute("xmlns:rdf", NS_RDF);
		rdfElement.setAttribute("xmlns:skos", NS_SKOS);
		rdfElement.setAttribute("xmlns:dc", NS_DC);
		rdfElement.setAttribute("xmlns:opendata", NS_OPENDATA);
		addCustomRdfNsElements(rdfElement);
		doc.appendChild(rdfElement);
		
		for (RecordType record : records) {
			Element concept = doc.createElement("skos:Concept");
			concept.setAttribute("rdf:about", getConceptRdfAbout(record));
			recordToRdf(doc, concept, record);
			
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
	 * @param records
	 *            list of organization records to serialize and store
	 * 
	 * @throws TransformerException
	 *             when conversion into RDF fails
	 * @throws OdnRepositoryException
	 *             when we fail to store given data into repository
	 * @throws IllegalArgumentException
	 *             if repository with given name does not exists
	 */
	public abstract void store(Vector<RecordType> records)
			throws TransformerException, IllegalArgumentException,
			OdnRepositoryException;
}
