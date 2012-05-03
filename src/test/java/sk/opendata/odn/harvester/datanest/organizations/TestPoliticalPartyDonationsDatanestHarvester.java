/* Copyright (C) 2011 Peter Hanecak <hanecak@opendata.sk>
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

package sk.opendata.odn.harvester.datanest.organizations;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Calendar;

import org.junit.Before;
import org.junit.Test;

import sk.opendata.odn.model.Currency;
import sk.opendata.odn.model.PoliticalPartyDonationRecord;

public class TestPoliticalPartyDonationsDatanestHarvester {
	
	public final String TEST_DONOR_NAME = "Testname";
	public final String TEST_DONOR_SURNAME = "Testsurname";
	public final String TEST_DONOR_TITLE = "Mr.";
	public final String TEST_DONOR_COMPANY = "Testing Company";
	public final String TEST_DONOR_ICO = "17321204";
	public final String TEST_DONOR_CITY = "Bratislava";
	public final String TEST_DONOR_PSC_COMMON_FORM = "831 05";
	public final String TEST_DONOR_PSC_ODN_STORAGE_FORM = "83105";
	public final Float TEST_DONATION_VALUE = 1.25f;
	public final String TEST_INVALID_DONATION_VALUE = "L.25";
	public final Currency TEST_DONATION_CURRENCY = Currency.EUR;
	public final String TEST_ACCEPT_DATE = "1991-07-17";
	public final String TEST_INVALID_ACCEPT_DATE = "L991/07/1T";
	public final String TEST_EMPTY_STRING = "";
	
	private PoliticalPartyDonationsDatanestHarvester harvester;
	
	private String[] fullRecord;
	private String[] recordWithEmptyCurrency;
	private String[] recordWithInvalidAcceptDate;

	@Before
	public void setUp() throws Exception {
		harvester = new PoliticalPartyDonationsDatanestHarvester();

		fullRecord = new String[PoliticalPartyDonationsDatanestHarvester.ATTR_INDEX_NOTE + 1];
		fullRecord[PoliticalPartyDonationsDatanestHarvester.ATTR_INDEX_DONOR_NAME] = TEST_DONOR_NAME;
		fullRecord[PoliticalPartyDonationsDatanestHarvester.ATTR_INDEX_DONOR_SURNAME] = TEST_DONOR_SURNAME;
		fullRecord[PoliticalPartyDonationsDatanestHarvester.ATTR_INDEX_DONOR_TITLE] = TEST_DONOR_TITLE;
		fullRecord[PoliticalPartyDonationsDatanestHarvester.ATTR_INDEX_DONOR_COMPANY] = TEST_DONOR_COMPANY;
		fullRecord[PoliticalPartyDonationsDatanestHarvester.ATTR_INDEX_DONOR_ICO] = TEST_DONOR_ICO;
		fullRecord[PoliticalPartyDonationsDatanestHarvester.ATTR_INDEX_DONOR_CITY] = TEST_DONOR_CITY;
		fullRecord[PoliticalPartyDonationsDatanestHarvester.ATTR_INDEX_DONOR_PSC] = TEST_DONOR_PSC_COMMON_FORM;
		fullRecord[PoliticalPartyDonationsDatanestHarvester.ATTR_INDEX_DONATION_VALUE] = TEST_DONATION_VALUE.toString();
		fullRecord[PoliticalPartyDonationsDatanestHarvester.ATTR_INDEX_DONATION_CURRENCY] = TEST_DONATION_CURRENCY.toString();
		fullRecord[PoliticalPartyDonationsDatanestHarvester.ATTR_INDEX_ACCEPT_DATE] = TEST_ACCEPT_DATE;
		fullRecord[PoliticalPartyDonationsDatanestHarvester.ATTR_INDEX_NOTE] = TEST_EMPTY_STRING;

		recordWithEmptyCurrency = Arrays.copyOf(fullRecord, fullRecord.length);
		recordWithEmptyCurrency[PoliticalPartyDonationsDatanestHarvester.ATTR_INDEX_DONATION_CURRENCY] = TEST_EMPTY_STRING;

		recordWithInvalidAcceptDate = Arrays.copyOf(fullRecord,
				fullRecord.length);
		recordWithInvalidAcceptDate[PoliticalPartyDonationsDatanestHarvester.ATTR_INDEX_ACCEPT_DATE] = TEST_INVALID_ACCEPT_DATE;
	}

	@Test
	public void testScrapOneRecordFull() {
		try {
			PoliticalPartyDonationRecord record = harvester.scrapOneRecord(fullRecord);
			
			assertEquals("donor name", TEST_DONOR_NAME, record.getDonorName());
			assertEquals("donor surname", TEST_DONOR_SURNAME, record.getDonorSurname());
			assertEquals("donor title", TEST_DONOR_TITLE, record.getDonorTitle());
			assertEquals("donor company", TEST_DONOR_COMPANY, record.getName());
			assertEquals("donor ico", TEST_DONOR_ICO, record.getIco());
			assertEquals("donor ico", TEST_DONOR_CITY, record.getDonorCity());
			assertEquals("donor ico", TEST_DONOR_PSC_ODN_STORAGE_FORM, record.getDonorPsc());
			
			assertEquals("donation value", TEST_DONATION_VALUE.floatValue(), record.getDonationValue(), 0.001f);
			assertEquals("donation currency", TEST_DONATION_CURRENCY, record.getCurrency());
			
			Calendar acceptDate = Calendar.getInstance();
			acceptDate.setTime(record.getAcceptDate());
			assertEquals("accept date: year", 1991, acceptDate.get(Calendar.YEAR));
			assertEquals("accept date: month", 7 - 1, acceptDate.get(Calendar.MONTH));
			assertEquals("accept date: day", 17, acceptDate.get(Calendar.DAY_OF_MONTH));
			
			assertEquals("note", null, record.getNote());
		} catch (ParseException e) {
			fail("exception occured: " + e);
		}
	}

	@Test
	public void testScrapOneRecordWithoutDateTo() {
		try {
			PoliticalPartyDonationRecord record = harvester.scrapOneRecord(recordWithEmptyCurrency);
			
			assertEquals("date to", Currency.UNDEFINED, record.getCurrency());
		} catch (ParseException e) {
			fail("exception occured: " + e);
		}
	}

	@Test(expected=ParseException.class)
	public void testScrapOneRecordWithInvalidAcceptDate() throws ParseException {
		harvester.scrapOneRecord(recordWithInvalidAcceptDate);
	}

}
