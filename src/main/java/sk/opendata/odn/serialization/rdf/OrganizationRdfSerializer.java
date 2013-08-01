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

import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import sk.opendata.odn.model.OrganizationRecord;
import sk.opendata.odn.repository.OdnRepositoryException;
import sk.opendata.odn.repository.OdnRepositoryStoreInterface;
import sk.opendata.odn.repository.sesame.RdfData;
import sk.opendata.odn.serialization.OdnSerializationException;

/**
 * This class is used by a Harvester to serialize organization records into RDF
 * and store them in Repository.
 */
public class OrganizationRdfSerializer extends AbstractRdfSerializer<OrganizationRecord> {
	
	// One thing should have one URI. For Slovak companies it maybe will be this one even officially.
	// URI might be understood this way:
	// a) Slovak government has a dataset (http://data.gov.sk/...)
	// b) where we're distinguishing real things by ID (.../id/...)
	// c) which (the dataset) is being curated by Ministry of Interior (../interior/...)
	// d) and contains information about organizations (.../organization/...)
	// e) distinguished by ICO (the last thing appended to base URI: .../<ico>) 
	public final static String ORGANIZATIONS_BASE_URI = "http://data.gov.sk/id/interior/organization/";
	public final static String OPENDATA_ORGANIZATIONS_CONTEXTS_KEY = "organizations";
	
	public final static String TAG_NAME_ORG_REGORG = "org:RegisteredOrganization";
	
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
	public OrganizationRdfSerializer(
			OdnRepositoryStoreInterface<RdfData> repository)
			throws IllegalArgumentException, ParserConfigurationException,
			TransformerConfigurationException {
	
		super(repository);
	}
	
	@Override
	public String getRecordTagName() {
		return TAG_NAME_ORG_REGORG;
	}
	
	@Override
	public void serializeRecord(Document doc, Element concept, OrganizationRecord record) {
	    concept.appendChild(appendTextNode(doc, "org:legalName", record.getName()));
	    concept.appendChild(appendResourceNode(doc, "dc:source", "rdf:resource", record.getSource()));
	    concept.appendChild(appendTextNode(doc, "dc:type", record.getLegalForm()));
	    if (record.getDateFrom() != null) {
	    	String dateFrom = sdf.format(record.getDateFrom());
	        concept.appendChild(appendTextNode(doc, "opendata:dateFrom", dateFrom));
	    }
	    if (record.getDateTo() != null) {
	    	String dateTo = sdf.format(record.getDateTo());
	        concept.appendChild(appendTextNode(doc, "opendata:dateTo", dateTo));
	    }
	    //concept.appendChild(appendTextNode(doc, "opendata:seat", record.getSeat()));
	    Element fullAddress = appendTextNode(doc, "locn:fullAddress", record.getSeat());
	    fullAddress.setAttribute("rdf:datatype", "xsd:string");
	    Element address = doc.createElement("locn:Address");
	    // TODO: does the fullAddress have to contain also the organization name
	    // (i.e. is it as written on envelope)?
	    address.appendChild(fullAddress);
	    // TODO: parse out PSC from full address
	    //address.appendChild(appendTextNode(doc, "locn:postCode", record.getSeat()));
	    Element primarySite = doc.createElement("org:hasPrimarySite");
	    primarySite.appendChild(address);
	    concept.appendChild(primarySite);
	    concept.appendChild(appendTextNode(doc, "opendata:ico", record.getIco()));
	}
	
	@Override
	public String getConceptRdfAbout(OrganizationRecord record) {
		return ORGANIZATIONS_BASE_URI + record.getIco();
	}
	
	@Override
	public void store(List<OrganizationRecord> records)
			throws IllegalArgumentException, OdnSerializationException,
			OdnRepositoryException {
		
		RdfData rdfData = new RdfData(
				serialize(records),
				ORGANIZATIONS_BASE_URI,
				OPENDATA_ORGANIZATIONS_CONTEXTS_KEY);
		getRepository().store(rdfData);
	}

}
