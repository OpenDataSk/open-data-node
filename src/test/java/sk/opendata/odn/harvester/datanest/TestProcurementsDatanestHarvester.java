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

package sk.opendata.odn.harvester.datanest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.text.ParseException;
import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

import sk.opendata.odn.model.Currency;
import sk.opendata.odn.model.ProcurementRecord;
import sk.opendata.odn.utils.tests.ProcurementTestData;

public class TestProcurementsDatanestHarvester {
	
	private ProcurementsDatanestHarvester harvester;
	
	private String[] fullRecord;
	private String[] recordWithPriceIssue2;
	private String[] recordWithInvalidPrice;
	private String[] recordWithEmptyPrice;
	private String[] recordWithEmptyCurrency;
	private String[] recordWithEmptyCurrencyAndPrice;

	@Before
	public void setUp() throws Exception {
		harvester = new ProcurementsDatanestHarvester();
		
		fullRecord = new String[ProcurementsDatanestHarvester.ATTR_INDEX_SUPPLIER_ICO + 1];
		fullRecord[ProcurementsDatanestHarvester.ATTR_INDEX_YEAR] = ProcurementTestData.TEST_YEAR;
		fullRecord[ProcurementsDatanestHarvester.ATTR_INDEX_CUSTOMER_ICO] = ProcurementTestData.TEST_CUSTOMER_ICO;
		fullRecord[ProcurementsDatanestHarvester.ATTR_INDEX_SUPPLIER_ICO] = ProcurementTestData.TEST_CUSTOMER_ICO;
		fullRecord[ProcurementsDatanestHarvester.ATTR_INDEX_PRICE] = ProcurementTestData.TEST_PRICE_STRING;
		fullRecord[ProcurementsDatanestHarvester.ATTR_INDEX_CURRENCY] = ProcurementTestData.TEST_CURRENCY.toString();
		
		recordWithPriceIssue2 = Arrays
				.copyOf(fullRecord, fullRecord.length);
		recordWithPriceIssue2[ProcurementsDatanestHarvester.ATTR_INDEX_PRICE] = ProcurementTestData.TEST_PRICE_ISSUE_2;
		
		recordWithInvalidPrice = Arrays
				.copyOf(fullRecord, fullRecord.length);
		recordWithInvalidPrice[ProcurementsDatanestHarvester.ATTR_INDEX_PRICE] = ProcurementTestData.TEST_INVALID_PRICE;
		
		recordWithEmptyPrice = Arrays
				.copyOf(fullRecord, fullRecord.length);
		recordWithEmptyPrice[ProcurementsDatanestHarvester.ATTR_INDEX_PRICE] = ProcurementTestData.TEST_EMPTY_STRING;
		
		recordWithEmptyCurrency = Arrays
				.copyOf(fullRecord, fullRecord.length);
		recordWithEmptyCurrency[ProcurementsDatanestHarvester.ATTR_INDEX_CURRENCY] = ProcurementTestData.TEST_EMPTY_STRING;
		
		recordWithEmptyCurrencyAndPrice = Arrays
				.copyOf(fullRecord, fullRecord.length);
		recordWithEmptyCurrencyAndPrice[ProcurementsDatanestHarvester.ATTR_INDEX_PRICE] = ProcurementTestData.TEST_EMPTY_STRING;
		recordWithEmptyCurrencyAndPrice[ProcurementsDatanestHarvester.ATTR_INDEX_CURRENCY] = ProcurementTestData.TEST_EMPTY_STRING;
	}

	@Test
	public void testScrapOneRecordFull() {
		try {
			ProcurementRecord record = harvester.scrapOneRecord(fullRecord);
			
			assertEquals("name", ProcurementTestData.TEST_YEAR, record.getYear());
			assertEquals("customer ICO", ProcurementTestData.TEST_CUSTOMER_ICO, record.getCustomerIco());
			assertEquals("supplier ICO", ProcurementTestData.TEST_CUSTOMER_ICO, record.getSupplierIco());
			
			assertEquals("price", ProcurementTestData.TEST_PRICE, record.getPrice(), 0.001f);
			assertEquals("currency", ProcurementTestData.TEST_CURRENCY, record.getCurrency());

			assertEquals("number of scrap notes", 0, record.getScrapNotes().size());
		} catch (ParseException e) {
			fail("exception occured: " + e);
		}
	}

	/**
	 * This test replicates issue #2 reported on GitHub:
	 * https://github.com/OpenDataSk/open-data-node/issues/2
	 */
	@Test
	public void testScrapOneRecordIssue2() {
		try {
			ProcurementRecord record = harvester.scrapOneRecord(recordWithPriceIssue2);
			
			assertEquals("price", 28000000f, record.getPrice(), 0.001f);
		} catch (ParseException e) {
			fail("exception occured: " + e);
		}
	}

	@Test(expected=ParseException.class)
	public void testScrapOneRecordWithInvalidPrice() throws ParseException {
		harvester.scrapOneRecord(recordWithInvalidPrice);
	}

	@Test
	public void testScrapOneRecordWithEmptyPrice() {
		try {
			ProcurementRecord record = harvester.scrapOneRecord(recordWithEmptyPrice);
			
			assertEquals("price", 0f, record.getPrice(), 0.001f);
			assertEquals("number of scrap notes", 1, record.getScrapNotes().size());
			assertEquals("scrap note", ProcurementsDatanestHarvester.SC_MISSING_PRICE, record.getScrapNotes().get(0));
		} catch (ParseException e) {
			fail("exception occured: " + e);
		}
	}

	@Test
	public void testScrapOneRecordWithEmptyCurrency() {
		try {
			ProcurementRecord record = harvester.scrapOneRecord(recordWithEmptyCurrency);
			
			assertEquals("currency", Currency.UNDEFINED, record.getCurrency());
			assertEquals("number of scrap notes", 1, record.getScrapNotes().size());
			assertEquals("scrap note", ProcurementsDatanestHarvester.SC_MISSING_CURRENCY_FOR_NON_ZERO_PRICE, record.getScrapNotes().get(0));
		} catch (ParseException e) {
			fail("exception occured: " + e);
		}
	}

	@Test
	public void testScrapOneRecordWithEmptyCurrencyAndPrice() {
		try {
			ProcurementRecord record = harvester.scrapOneRecord(recordWithEmptyCurrencyAndPrice);
			
			assertEquals("currency", Currency.UNDEFINED, record.getCurrency());
			assertEquals("number of scrap notes", 2, record.getScrapNotes().size());
			assertTrue("scrap note about currency", record.getScrapNotes().contains(ProcurementsDatanestHarvester.SC_MISSING_CURRENCY));
			assertTrue("scrap note about price", record.getScrapNotes().contains(ProcurementsDatanestHarvester.SC_MISSING_PRICE));
		} catch (ParseException e) {
			fail("exception occured: " + e);
		}
	}

}
