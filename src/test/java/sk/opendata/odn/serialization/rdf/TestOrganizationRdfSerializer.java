package sk.opendata.odn.serialization.rdf;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.Calendar;
import java.util.Vector;

import org.junit.BeforeClass;
import org.junit.Test;

import sk.opendata.odn.model.OrganizationRecord;
import sk.opendata.odn.repository.sesame.SesameRepository;
import sk.opendata.odn.serialization.OdnSerializationException;
import sk.opendata.odn.utils.tests.OrganizationTestData;

public class TestOrganizationRdfSerializer {
	
	private final static OrganizationRecord record = new OrganizationRecord();
	private final static Vector<OrganizationRecord> records = new Vector<OrganizationRecord>();
	private static SesameRepository repository;
	private static OrganizationRdfSerializer rdfSerializer; 
	
	private final static String TEST_RESULT = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n"
			+ "<rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\" "
			+ "xmlns:dc=\"http://purl.org/dc/elements/1.1/\" "
			+ "xmlns:locn=\"http://www.w3.org/ns/locn#\" "
			+ "xmlns:opendata=\"http://sk.eea.opendata/2011/02/opendicts#\" "
			+ "xmlns:rov=\"http://www.w3.org/TR/vocab-regorg/\" "
			+ "xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\">\n"
			+ "<rov:RegisteredOrganization rdf:about=\"http://data.gov.sk/id/interior/organization/17321204\">\n"
			+ "<rov:legalName>Test Name</rov:legalName>\n"
			+ "<dc:source rdf:resource=\"http://www.test.sk/test1\"/>\n"
			+ "<dc:type>Spoločnosť s ručením obmedzeným</dc:type>\n"
			+ "<opendata:dateFrom>01.09.2013</opendata:dateFrom>\n"
			+ "<opendata:dateTo>01.09.2013</opendata:dateTo>\n"
			+ "<rov:hasPrimarySite>\n"
			+ "<locn:address>\n"
			+ "<locn:fullAddress rdf:datatype=\"xsd:string\">Testovacia 1, 850 00 Bratislava</locn:fullAddress>\n"
			+ "</locn:address>\n"
			+ "</rov:hasPrimarySite>\n"
			+ "<opendata:ico>17321204</opendata:ico>\n"
			+ "</rov:RegisteredOrganization>\n"
			+ "</rdf:RDF>\n";

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		Calendar date = Calendar.getInstance();
		date.set(2013, 8, 1);
		record.setDateFrom(date.getTime());
		record.setDateTo(date.getTime());
		record.setIco(OrganizationTestData.TEST_ICO);
		record.setId(OrganizationTestData.TEST_ID);
		record.setLegalForm(OrganizationTestData.TEST_LEGAL_FORM);
		record.setName(OrganizationTestData.TEST_NAME);
		record.setSeat(OrganizationTestData.TEST_SEAT);
		record.setSource(OrganizationTestData.TEST_SOURCE);
		
		records.add(record);
		
		repository = SesameRepository.getInstance();
		rdfSerializer = new OrganizationRdfSerializer(repository);
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
