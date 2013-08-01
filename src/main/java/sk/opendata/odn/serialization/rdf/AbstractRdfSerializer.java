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

import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.List;

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

import sk.opendata.odn.model.AbstractRecord;
import sk.opendata.odn.repository.OdnRepositoryStoreInterface;
import sk.opendata.odn.repository.sesame.RdfData;
import sk.opendata.odn.serialization.AbstractSerializer;
import sk.opendata.odn.serialization.OdnSerializationException;

/**
 * Stuff common to all OpenData.sk RDF serializers.
 * 
 * @param <RecordType>
 *            type of individual record which will be converted to RDF
 */
public abstract class AbstractRdfSerializer<RecordType extends AbstractRecord> extends
		AbstractSerializer<RecordType, String, RdfData> {

	public final static String NS_RDF = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
	public final static String NS_SKOS = "http://www.w3.org/2004/02/skos/core#";
	public final static String NS_DC = "http://purl.org/dc/elements/1.1/";
	public final static String NS_ORG = "http://www.w3.org/ns/org#";
	public final static String NS_ROV = "http://www.w3.org/TR/vocab-regorg/";
	public final static String NS_LOCN = "http://www.w3.org/ns/locn#";
	public final static String NS_OPENDATA = "http://sk.eea.opendata/2011/02/opendicts#";
	
	public final static String TAG_NAME_SKOS_CONCEPT = "skos:Concept";
	
	public final static String OPENDATA_DATE_FORMAT = "dd.MM.yyyy";
	
	public final static String OPENDATA_COMBINED_REPO_NAME = "all";
	public final static String OPENDATA_COMBINED_BASE_URI = "http://opendata.sk/dataset/all/";

	public final static String ERR_CONVERSION = "unable to convert the data into RDF";
	
	protected final static SimpleDateFormat sdf = new SimpleDateFormat(
			OPENDATA_DATE_FORMAT);

	protected DocumentBuilder docBuilder;
	protected Transformer transformer;

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
	public AbstractRdfSerializer(OdnRepositoryStoreInterface<RdfData> repository)
			throws IllegalArgumentException, ParserConfigurationException,
			TransformerConfigurationException {

		super(repository);

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
	 * Serialize one given record into RDF and store the result in given
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
	public abstract void serializeRecord(Document doc, Element concept,
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
	
	/**
	 * @return tag name for a record
	 */
	public String getRecordTagName() {
		return TAG_NAME_SKOS_CONCEPT;
	}
	
	@Override
	public String serialize(List<RecordType> records)
			throws OdnSerializationException {
		
		Document doc = docBuilder.newDocument();
		
		Element rdfElement = doc.createElementNS(NS_RDF, "rdf:RDF");
		rdfElement.setAttribute("xmlns:rdf", NS_RDF);
		rdfElement.setAttribute("xmlns:skos", NS_SKOS);
		rdfElement.setAttribute("xmlns:dc", NS_DC);
		rdfElement.setAttribute("xmlns:org", NS_ORG);
		rdfElement.setAttribute("xmlns:rov", NS_ROV);
		rdfElement.setAttribute("xmlns:locn", NS_LOCN);
		rdfElement.setAttribute("xmlns:opendata", NS_OPENDATA);
		addCustomRdfNsElements(rdfElement);
		doc.appendChild(rdfElement);
		
		for (RecordType record : records) {
			Element concept = doc.createElement(getRecordTagName());
			concept.setAttribute("rdf:about", getConceptRdfAbout(record));
			serializeRecord(doc, concept, record);
			
			rdfElement.appendChild(concept);
		}
		
		StringWriter sw = new StringWriter();
        StreamResult result = new StreamResult(sw);
        DOMSource source = new DOMSource(doc);
        try {
			transformer.transform(source, result);
		} catch (TransformerException e) {
			throw new OdnSerializationException(e.getMessage(), e);
		}
        
		return sw.toString();
	}
}
