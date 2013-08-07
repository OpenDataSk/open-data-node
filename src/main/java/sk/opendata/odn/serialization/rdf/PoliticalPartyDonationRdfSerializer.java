/* Copyright (C) 2011 Peter Hanecak <hanecak@opendata.sk>
 * Rastislav Senderak <rastislav.senderak@eea.sk>
 *
 * This file is part of Open Data Node.
 *
 * Open Data Node is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Open Data Node is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Open Data Node.  If not, see <http://www.gnu.org/licenses/>.
 */

package sk.opendata.odn.serialization.rdf;

import java.text.DecimalFormat;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import sk.opendata.odn.model.PoliticalPartyDonationRecord;
import sk.opendata.odn.repository.OdnRepositoryException;
import sk.opendata.odn.repository.OdnRepositoryStoreInterface;
import sk.opendata.odn.repository.sesame.RdfData;
import sk.opendata.odn.serialization.OdnSerializationException;

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
	public final static String OPENDATA_PPD_CONTEXTS_KEY = "political_party_donations";
	
	private final static DecimalFormat donationValueFormat = new DecimalFormat("#.##");
	
	/**
	 * Initialize serializer to use given repository.
	 * 
	 * @param repository
	 *            repository to use for storage of record
	 * 
	 * @throws IllegalArgumentException
	 *             if repository is {@code null}
	 * @throws ParserConfigurationException
	 *             when XML document builder fails to initialize
	 * @throws TransformerConfigurationException
	 *             when XML document transformer fails to initialize
	 */
	public PoliticalPartyDonationRdfSerializer(
			OdnRepositoryStoreInterface<RdfData> repository)
			throws IllegalArgumentException, ParserConfigurationException,
			TransformerConfigurationException {
	
		super(repository);
	}
	
	@Override
	public void serializeRecord(Document doc, Element rdfElement, PoliticalPartyDonationRecord record) {
		Element concept = doc.createElement(TAG_NAME_SKOS_CONCEPT);
		concept.setAttribute("rdf:about", OPENDATA_PPD_BASE_URI + record.getId());

		// TODO: this is a) ugly and b) "suspect" (i.e. I feel like it's not
		// entirely "in the spirit" of RDF => re-think, re-research, ...
		StringBuffer label = new StringBuffer();
		if (record.getDonorName() != null)
			label.append(record.getDonorName()).append(" ");
		if (record.getDonorSurname() != null)
			label.append(record.getDonorSurname()).append(" ");
		if (record.getDonorTitle() != null)
			label.append(record.getDonorTitle()).append(" ");
		if (record.getName() != null)
			label.append(record.getName()).append(" ");
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
						+ record.getDatanestId()));
		// TODO: use FOAF for people and Good Relations for companies
		// and use only URIs or something ... as a link - we have or will have
		// organizations and people repository so the main point twill be the URI,
		// subsequent data will be useful for clean-up when proper link could not
		// be found automatically
		if (record.getDonorName() != null)
			concept.appendChild(appendTextNode(doc, "opendata:donorName",
					record.getDonorName()));
		if (record.getDonorSurname() != null)
			concept.appendChild(appendTextNode(doc, "opendata:donorSurname",
					record.getDonorSurname()));
		if (record.getDonorTitle() != null)
			concept.appendChild(appendTextNode(doc, "opendata:donorTitle",
					record.getDonorTitle()));
		if (record.getName() != null)
			concept.appendChild(appendTextNode(doc, "opendata:donorCompanyName",
					record.getName()));
		if (record.getIco() != null) {
			concept.appendChild(appendResourceNode(
					doc,
					"opendata:donorCompany",
					"rdf:resource",
					OrganizationRdfSerializer.ORGANIZATIONS_BASE_URI + record.getIco()));
			
			concept.appendChild(appendTextNode(doc, "opendata:donorIco",
					record.getIco()));
		}
		// TODO: adresa, mesto a PSC darcu
		
		concept.appendChild(appendTextNode(doc, "opendata:giftValue",
				donationValueFormat.format(record.getDonationValue())));
		concept.appendChild(appendTextNode(doc, "opendata:giftCurrency",
				record.getCurrency().getCurrencyCode()));
		concept.appendChild(appendTextNode(doc, "opendata:recipientParty",
				record.getRecipientParty()));
		if (record.getAcceptDate() != null) {
			String acceptDate = sdf.format(record.getAcceptDate());
			concept.appendChild(appendTextNode(doc, "opendata:acceptDate",
					acceptDate));
	    }
		if (record.getNote() != null)
			concept.appendChild(appendTextNode(doc, "opendata:xNote",
					record.getNote()));
		
		rdfElement.appendChild(concept);
	}
	
	@Override
	public void store(List<PoliticalPartyDonationRecord> records)
			throws IllegalArgumentException, OdnSerializationException,
			OdnRepositoryException {
		
		RdfData rdfData = new RdfData(
				serialize(records),
				OPENDATA_PPD_BASE_URI,
				OPENDATA_PPD_CONTEXTS_KEY);
		getRepository().store(rdfData);
	}

}
