package sk.opendata.odn.serialization.rdf;

import java.text.SimpleDateFormat;
import java.util.Vector;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import sk.opendata.odn.model.OrganizationRecord;
import sk.opendata.odn.repository.OdnRepositoryException;
import sk.opendata.odn.repository.OdnRepositoryInterface;
import sk.opendata.odn.repository.sesame.RdfData;

/**
 * This class is used by a Harvester to serialize organization records into RDF
 * and store them in Repository.
 */
public class OrganizationRdfSerializer extends AbstractRdfSerializer<OrganizationRecord> {
	
	public final static String OPENDATA_DATE_FORMAT = "dd.MM.yyyy";
	// TODO: do we need that configurable? if we want the that RDF data
	// accessible over the net via that URL/URI (which is encouraged) wit would
	// be either nice to "guess" is correctly from some other configuration or
	// have it in some per-ODN repository configuration
	public final static String OPENDATA_BASE_URI = "http://opendata.sk/dataset/organizations/";
	
	private final static SimpleDateFormat sdf = new SimpleDateFormat(
			OPENDATA_DATE_FORMAT);
	
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
	
		super(repository, name);
	}
	
	@Override
	public void recordToRdf(Document doc, Element concept, OrganizationRecord record) {
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
	
	@Override
	public String getConceptRdfAbout(OrganizationRecord record) {
		return record.getIco();
	}
	
	@Override
	public void store(Vector<OrganizationRecord> records)
			throws TransformerException, IllegalArgumentException, OdnRepositoryException {
		
		RdfData rdfData = new RdfData(
				toRdf(records),
				OPENDATA_BASE_URI);
		repository.store(repoName, rdfData);
	}

}
