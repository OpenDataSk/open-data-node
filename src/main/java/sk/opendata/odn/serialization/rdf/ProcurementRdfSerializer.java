package sk.opendata.odn.serialization.rdf;

import java.text.DecimalFormat;
import java.util.Vector;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import sk.opendata.odn.model.ProcurementRecord;
import sk.opendata.odn.repository.OdnRepositoryException;
import sk.opendata.odn.repository.OdnRepositoryInterface;
import sk.opendata.odn.repository.sesame.RdfData;

/**
 * This class is used by a Harvester to serialize organization records into RDF
 * and store them in Repository.
 */
public class ProcurementRdfSerializer extends AbstractRdfSerializer<ProcurementRecord> {
	
	public final static String NS_PROCUREMENT = "http://opendata.cz/vocabulary/procurement.rdf#";
	// TODO: do we need that configurable? if we want the that RDF data
	// accessible over the net via that URL/URI (which is encouraged) wit would
	// be either nice to "guess" is correctly from some other configuration or
	// have it in some per-ODN repository configuration
	public final static String OPENDATA_PROCUREMENTS_BASE_URI = "http://opendata.sk/dataset/procurements/";
	
	private final static DecimalFormat priceFormat = new DecimalFormat("#.##");
	
	
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
	public ProcurementRdfSerializer(OdnRepositoryInterface<RdfData> repository,
			String name) throws ParserConfigurationException,
			TransformerConfigurationException {
	
		super(repository, name);
	}
	
	@Override
	public void addCustomRdfNsElements(Element rdfElement) {
		rdfElement.setAttribute("xmlns:pc", NS_PROCUREMENT);
	}
	
	@Override
	public void recordToRdf(Document doc, Element concept, ProcurementRecord record) {
		// TODO: verify, that it is indeed a form of unique name identifying
		// single procurement
	    concept.appendChild(appendTextNode(doc, "skos:prefLabel", record.getProcurementId()));
	    // TODO: hardcoded strings are not nice ... meaning the URL mainly but ...
	    concept.appendChild(appendResourceNode(doc, "dc:source", "rdf:resource",
	    		"http://datanest.fair-play.sk/datasets/2/records/" + record.getId()));
		concept.appendChild(appendResourceNode(doc,
				"opendata:xProcurementSubject", "rdf:resource",
				record.getProcurementSubject()));
		concept.appendChild(appendTextNode(doc, "pc:price",
				priceFormat.format(record.getPrice())));
		concept.appendChild(appendTextNode(doc, "opendata:xCurrency",
				record.getCurrency()));
		concept.appendChild(appendTextNode(doc, "opendata:xIsVatIncluded",
				Boolean.toString(record.isVatIncluded())));
		// TODO: use 'opendata:ico' child inside 'pc:buyerProfile' instead
		concept.appendChild(appendResourceNode(doc,
				"opendata:xCustomerIco", "rdf:resource",
				record.getCustomerIco()));
		// TODO: use 'opendata:ico' child inside 'pc:Supplier' instead
		concept.appendChild(appendResourceNode(doc,
				"opendata:xSupplierIco", "rdf:resource",
				record.getSupplierIco()));
	}
	
	@Override
	public String getConceptRdfAbout(ProcurementRecord record) {
		return OPENDATA_PROCUREMENTS_BASE_URI + record.getId();
	}
	
	@Override
	public void store(Vector<ProcurementRecord> records)
			throws TransformerException, IllegalArgumentException, OdnRepositoryException {
		
		RdfData rdfData = new RdfData(
				toRdf(records),
				OPENDATA_PROCUREMENTS_BASE_URI);
		repository.store(repoName, rdfData);
		
		// "combined mirror" of the RDF statements: for the purpose of doing
		// combined queries on top of all RDF data sets we have one special
		// repository where we push all our RDF statements with same special
		// base URI but differenciated by contexts (and we reuse the "original"
		// base URI as context
		rdfData = new RdfData(
				toRdf(records),
				OPENDATA_COMBINED_BASE_URI);
		repository.store(OPENDATA_COMBINED_REPO_NAME, rdfData,
				OPENDATA_PROCUREMENTS_BASE_URI);
	}

}
