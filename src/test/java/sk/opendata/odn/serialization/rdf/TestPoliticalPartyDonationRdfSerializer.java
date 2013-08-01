package sk.opendata.odn.serialization.rdf;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.Calendar;
import java.util.Vector;

import org.junit.BeforeClass;
import org.junit.Test;

import sk.opendata.odn.model.PoliticalPartyDonationRecord;
import sk.opendata.odn.repository.sesame.SesameRepository;
import sk.opendata.odn.serialization.OdnSerializationException;
import sk.opendata.odn.utils.tests.PoliticalPartyDonationTestData;

public class TestPoliticalPartyDonationRdfSerializer {
	
	private final static PoliticalPartyDonationRecord record = new PoliticalPartyDonationRecord();
	private final static Vector<PoliticalPartyDonationRecord> records = new Vector<PoliticalPartyDonationRecord>();
	private static SesameRepository repository;
	private static PoliticalPartyDonationRdfSerializer rdfSerializer; 
	
	private final static String TEST_RESULT = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n"
			+ "<rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\" "
			+ "xmlns:dc=\"http://purl.org/dc/elements/1.1/\" "
			+ "xmlns:opendata=\"http://sk.eea.opendata/2011/02/opendicts#\" "
			+ "xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\">\n"
			+ "<skos:Concept rdf:about=\"http://opendata.sk/dataset/political_party_donations/donation_0\">\n"
			+ "<skos:prefLabel>Testname Testsurname Mr. Testing Company  - 1.25 - Nezavisla Iniciativa Excentrikov</skos:prefLabel>\n"
			+ "<dc:source rdf:resource=\"http://datanest.fair-play.sk/datasets/32/records/donation_0\"/>\n"
			+ "<opendata:xDonorName>Testname</opendata:xDonorName>\n"
			+ "<opendata:xDonorSurname>Testsurname</opendata:xDonorSurname>\n"
			+ "<opendata:xDonorTitle>Mr.</opendata:xDonorTitle>\n"
			+ "<opendata:xDonorCompanyName>Testing Company</opendata:xDonorCompanyName>\n"
			+ "<opendata:xGiftValue>1.25</opendata:xGiftValue>\n"
			+ "<opendata:xGiftCurrency>EUR</opendata:xGiftCurrency>\n"
			+ "<opendata:xRecipientParty>Nezavisla Iniciativa Excentrikov</opendata:xRecipientParty>\n"
			+ "<opendata:xAcceptDate>01.09.2013</opendata:xAcceptDate>\n"
			+ "</skos:Concept>\n"
			+ "</rdf:RDF>\n";

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		Calendar date = Calendar.getInstance();
		date.set(2013, 8, 1);
		record.setAcceptDate(date.getTime());
		record.setCurrency(PoliticalPartyDonationTestData.TEST_DONATION_CURRENCY);
		record.setDonationValue(PoliticalPartyDonationTestData.TEST_DONATION_VALUE);
		record.setDonorName(PoliticalPartyDonationTestData.TEST_DONOR_NAME);
		record.setDonorSurname(PoliticalPartyDonationTestData.TEST_DONOR_SURNAME);
		record.setDonorTitle(PoliticalPartyDonationTestData.TEST_DONOR_TITLE);
		record.setId(PoliticalPartyDonationTestData.TEST_ID);
		record.setName(PoliticalPartyDonationTestData.TEST_DONOR_COMPANY);
		record.setRecipientParty(PoliticalPartyDonationTestData.TEST_RECIPIENT_PARTY);
		
		records.add(record);
		
		repository = SesameRepository.getInstance();
		rdfSerializer = new PoliticalPartyDonationRdfSerializer(repository);
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
