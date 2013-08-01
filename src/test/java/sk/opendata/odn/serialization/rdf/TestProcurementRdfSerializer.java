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
			+ "xmlns:dc=\"http://purl.org/dc/elements/1.1/\" "
			+ "xmlns:locn=\"http://www.w3.org/ns/locn#\" "
			+ "xmlns:opendata=\"http://sk.eea.opendata/2011/02/opendicts#\" "
			+ "xmlns:org=\"http://www.w3.org/ns/org#\" "
			+ "xmlns:pc=\"http://opendata.cz/vocabulary/procurement.rdf#\" "
			+ "xmlns:rov=\"http://www.w3.org/TR/vocab-regorg/\" "
			+ "xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\">\n"
			+ "<skos:Concept rdf:about=\"http://opendata.sk/dataset/procurements/procurement_0\">\n"
			+ "<skos:prefLabel>06281 - VUP</skos:prefLabel>\n"
			+ "<dc:source rdf:resource=\"http://datanest.fair-play.sk/datasets/2/records/procurement_0\"/>\n"
			+ "<opendata:xProcurementSubject rdf:resource=\"Rekonštrukcia ZŠ v obci Horná Dolná\"/>\n"
			+ "<pc:price>1.25</pc:price>\n"
			+ "<opendata:xCurrency>EUR</opendata:xCurrency>\n"
			+ "<opendata:xIsVatIncluded>false</opendata:xIsVatIncluded>\n"
			+ "<opendata:xCustomerIco>17321204</opendata:xCustomerIco>\n"
			+ "<opendata:xSupplierIco>40212371</opendata:xSupplierIco>\n"
			+ "</skos:Concept>\n"
			+ "</rdf:RDF>\n";

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		Calendar date = Calendar.getInstance();
		date.set(2013, 8, 1);
		record.setId(ProcurementTestData.TEST_ID);
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
