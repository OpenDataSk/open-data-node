package sk.opendata.odn.serialization.rdf;

import java.text.DecimalFormat;
import java.util.Vector;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import sk.opendata.odn.model.PoliticalPartyDonationRecord;
import sk.opendata.odn.repository.OdnRepositoryException;
import sk.opendata.odn.repository.OdnRepositoryInterface;
import sk.opendata.odn.repository.sesame.RdfData;

/**
 * This class is used by a Harvester to serialize political party donations
 * records into RDF and store them in Repository.
 */
public class PoliticalPartyDonationRdfSerializer extends AbstractRdfSerializer<PoliticalPartyDonationRecord> {
	
	// TODO: do we need that configurable? if we want the that RDF data
	// accessible over the net via that URL/URI (which is encouraged) it would
	// be either nice to "guess" it correctly from some other configuration or
	// have it in some per-ODN repository configuration
	public final static String OPENDATA_PPD_BASE_URI = "http://opendata.sk/dataset/political_party_donations/";
	
	private final static DecimalFormat donationValueFormat = new DecimalFormat("#.##");
	
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
	public PoliticalPartyDonationRdfSerializer(OdnRepositoryInterface<RdfData> repository,
			String name) throws ParserConfigurationException,
			TransformerConfigurationException {
	
		super(repository, name);
	}
	
	@Override
	public void recordToRdf(Document doc, Element concept, PoliticalPartyDonationRecord record) {
		// TODO: this is a) ugly and b) "suspect" (i.e. I feel like it's not
		// entirely "in the spirit" of RDF => rethink, re-research, ...
		StringBuffer label = new StringBuffer();
		if (record.getDonorName() != null)
			label.append(record.getDonorName()).append(" ");
		if (record.getDonorSurname() != null)
			label.append(record.getDonorSurname()).append(" ");
		if (record.getDonorTitle() != null)
			label.append(record.getDonorTitle()).append(" ");
		if (record.getDonorCompany() != null)
			label.append(record.getDonorCompany()).append(" ");
		label.append(" - ");
		label.append(donationValueFormat.format(record.getDonationValue()));
		label.append(" - ");
		label.append(record.getRecipientParty());
	    concept.appendChild(appendTextNode(doc, "skos:prefLabel",
	    		label.toString().trim()));
	    
		concept.appendChild(appendResourceNode(
				doc,
				"dc:source",
				"rdf:resource",
				"http://datanest.fair-play.sk/datasets/32/records/"
						+ record.getId()));
		// TODO: use FOAF for people and Good Relations for companies
		// and use only URIs or something ... as a link - we have or will have
		// organizations and people repository so the main point twill be the URI,
		// subsequent data will be useful for clean-up when proper link could not
		// be found automatically
		Element donorPerson = doc.createElement("opendata:xDonorPerson");
		Element donorCompany = doc.createElement("opendata:xDonorCompany");
		
		if (record.getDonorName() != null)
			donorPerson.appendChild(appendTextNode(doc, "opendata:xName",
					record.getDonorName()));
		if (record.getDonorSurname() != null)
			donorPerson.appendChild(appendTextNode(doc, "opendata:xSurname",
					record.getDonorSurname()));
		if (record.getDonorTitle() != null)
			donorPerson.appendChild(appendTextNode(doc, "opendata:xTitle",
					record.getDonorTitle()));
		if (record.getDonorCompany() != null)
			donorCompany.appendChild(appendTextNode(doc, "opendata:xName",
					record.getDonorCompany()));
		if (record.getDonorIco() != null)
			donorCompany.appendChild(appendTextNode(doc, "opendata:ico",
					record.getDonorIco()));
		// TODO: adresa, mesto a PSC darcu
		
		if (donorPerson.hasChildNodes())
			concept.appendChild(donorPerson);
		if (donorCompany.hasChildNodes())
			concept.appendChild(donorCompany);
		
		concept.appendChild(appendTextNode(doc, "opendata:xGiftValue",
				donationValueFormat.format(record.getDonationValue())));
		concept.appendChild(appendTextNode(doc, "opendata:xGiftCurrency",
				record.getDonationCurrency().getCurrencyCode()));
		concept.appendChild(appendTextNode(doc, "opendata:xRecipientParty",
				record.getRecipientParty()));
		if (record.getAcceptDate() != null) {
			String acceptDate = sdf.format(record.getAcceptDate());
			concept.appendChild(appendTextNode(doc, "opendata:xAcceptDate",
					acceptDate));
	    }
		if (record.getNote() != null)
			concept.appendChild(appendTextNode(doc, "opendata:xNote",
					record.getNote()));
	}
	
	@Override
	public String getConceptRdfAbout(PoliticalPartyDonationRecord record) {
		return OPENDATA_PPD_BASE_URI + record.getId();
	}
	
	@Override
	public void store(Vector<PoliticalPartyDonationRecord> records)
			throws TransformerException, IllegalArgumentException, OdnRepositoryException {
		
		RdfData rdfData = new RdfData(
				toRdf(records),
				OPENDATA_PPD_BASE_URI);
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
				OPENDATA_PPD_BASE_URI);
	}

}
