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

import sk.opendata.odn.model.ProcurementRecord;
import sk.opendata.odn.repository.OdnRepositoryException;
import sk.opendata.odn.repository.OdnRepositoryStoreInterface;
import sk.opendata.odn.repository.sesame.RdfData;
import sk.opendata.odn.serialization.OdnSerializationException;

/**
 * This class is used by a Harvester to serialize procurement records into RDF
 * and store them in Repository.
 */
public class ProcurementRdfSerializer extends AbstractRdfSerializer<ProcurementRecord> {
	
	public final static String NS_PROCUREMENT = "http://opendata.cz/vocabulary/procurement.rdf#";
	// TODO: do we need that configurable? if we want the that RDF data
	// accessible over the net via that URL/URI (which is encouraged) it would
	// be either nice to "guess" it correctly from some other configuration or
	// have it in some per-ODN repository configuration
	public final static String OPENDATA_PROCUREMENTS_BASE_URI = "http://opendata.sk/dataset/procurements/";
	public final static String OPENDATA_PROCUREMENTS_CONTEXTS_KEY = "procurements";
	
	private final static DecimalFormat priceFormat = new DecimalFormat("#.##");
	
	
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
	public ProcurementRdfSerializer(
			OdnRepositoryStoreInterface<RdfData> repository)
			throws IllegalArgumentException, ParserConfigurationException,
			TransformerConfigurationException {
	
		super(repository);
	}
	
	@Override
	public void addCustomRdfNsElements(Element rdfElement) {
		rdfElement.setAttribute("xmlns:pc", NS_PROCUREMENT);
	}
	
	@Override
	public void serializeRecord(Document doc, Element concept, ProcurementRecord record) {
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
		// sometimes the currency is not filled in the source (so far only for
		// cases where the price was 0)
		if (record.getCurrency() != null)
			concept.appendChild(appendTextNode(doc, "opendata:xCurrency",
					record.getCurrency().getCurrencyCode()));
		concept.appendChild(appendTextNode(doc, "opendata:xIsVatIncluded",
				Boolean.toString(record.isVatIncluded())));
		// TODO: use 'opendata:ico' child inside 'pc:buyerProfile' instead
		concept.appendChild(appendTextNode(doc, "opendata:xCustomerIco",
				record.getCustomerIco()));
		// TODO: use 'opendata:ico' child inside 'pc:Supplier' instead
		concept.appendChild(appendTextNode(doc, "opendata:xSupplierIco",
				record.getSupplierIco()));
		
		for (String scrapNote : record.getScrapNotes())
			concept.appendChild(appendTextNode(doc, "opendata:xScrapNote",
					scrapNote));
	}
	
	@Override
	public String getConceptRdfAbout(ProcurementRecord record) {
		return OPENDATA_PROCUREMENTS_BASE_URI + record.getId();
	}
	
	@Override
	public void store(List<ProcurementRecord> records)
			throws IllegalArgumentException, OdnSerializationException,
			OdnRepositoryException {
		
		RdfData rdfData = new RdfData(
				serialize(records),
				OPENDATA_PROCUREMENTS_BASE_URI,
				OPENDATA_PROCUREMENTS_CONTEXTS_KEY);
		getRepository().store(rdfData);
	}

}
