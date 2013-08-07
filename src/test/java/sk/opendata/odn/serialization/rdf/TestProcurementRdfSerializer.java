package sk.opendata.odn.serialization.rdf;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.Calendar;
import java.util.Vector;

import org.junit.BeforeClass;
import org.junit.Test;

import sk.opendata.odn.model.ProcurementRecord;
import sk.opendata.odn.repository.sesame.SesameRepository;
import sk.opendata.odn.serialization.OdnSerializationException;
import sk.opendata.odn.utils.tests.ProcurementTestData;

public class TestProcurementRdfSerializer {
	
	private final static ProcurementRecord record = new ProcurementRecord();
	private final static Vector<ProcurementRecord> records = new Vector<ProcurementRecord>();
	private static SesameRepository repository;
	private static ProcurementRdfSerializer rdfSerializer; 
	
	private final static String TEST_RESULT = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n"
			+ "<rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\" "
			+ "xmlns:adms=\"http://www.w3.org/ns/adms#\" "
			+ "xmlns:dc=\"http://purl.org/dc/elements/1.1/\" "
			+ "xmlns:dcterms=\"http://purl.org/dc/terms/\" "
			+ "xmlns:locn=\"http://www.w3.org/ns/locn#\" "
			+ "xmlns:opendata=\"http://sk.eea.opendata/2011/02/opendicts#\" "
			+ "xmlns:org=\"http://www.w3.org/ns/org#\" "
			+ "xmlns:pc=\"http://opendata.cz/vocabulary/procurement.rdf#\" "
			+ "xmlns:rov=\"http://www.w3.org/TR/vocab-regorg/\" "
			+ "xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\">\n"
			+ "<skos:Concept rdf:about=\"http://opendata.sk/dataset/procurements/procurement_0\">\n"
			+ "<skos:prefLabel>06281 - VUP</skos:prefLabel>\n"
			+ "<dc:source rdf:resource=\"http://datanest.fair-play.sk/datasets/2/records/0\"/>\n"
			+ "<opendata:procurementSubject>Rekonštrukcia ZŠ v obci Horná Dolná</opendata:procurementSubject>\n"
			+ "<pc:price>1.25</pc:price>\n"
			+ "<opendata:currency>EUR</opendata:currency>\n"
			+ "<opendata:xIsVatIncluded>false</opendata:xIsVatIncluded>\n"
			+ "<opendata:customerIco>17321204</opendata:customerIco>\n"
			+ "<opendata:customer rdf:resource=\"http://data.gov.sk/id/interior/organization/17321204\"/>\n"
			+ "<opendata:supplierIco>40212371</opendata:supplierIco>\n"
			+ "<opendata:supplier rdf:resource=\"http://data.gov.sk/id/interior/organization/40212371\"/>\n"
			+ "</skos:Concept>\n"
			+ "</rdf:RDF>\n";

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		Calendar date = Calendar.getInstance();
		date.set(2013, 8, 1);
		record.setId(ProcurementTestData.TEST_ID);
		record.setDatanestId(ProcurementTestData.TEST_DATANEST_ID);
		record.setCurrency(ProcurementTestData.TEST_CURRENCY);
		record.setCustomerIco(ProcurementTestData.TEST_CUSTOMER_ICO);
		record.setPrice(ProcurementTestData.TEST_PRICE);
		record.setProcurementId(ProcurementTestData.TEST_PROCUREMENT_ID);
		record.setProcurementSubject(ProcurementTestData.TEST_PROCUREMENT_SUBJECT);
		record.setSupplierIco(ProcurementTestData.TEST_SUPPLIER_ICO);
		
		records.add(record);
		
		repository = SesameRepository.getInstance();
		rdfSerializer = new ProcurementRdfSerializer(repository);
	}

	@Test
	public void test() {
		try {
			String result = rdfSerializer.serialize(records);
			
			assertEquals("result", TEST_RESULT, result);
		} catch (OdnSerializationException e) {
			fail("exception occured: " + e);
		}
	}

}
