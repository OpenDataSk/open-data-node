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
import static org.junit.Assert.fail;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Calendar;

import org.junit.Before;
import org.junit.Test;

import sk.opendata.odn.model.OrganizationRecord;
import sk.opendata.odn.utils.tests.OrganizationTestData;

public class TestOrganizationsDatanestHarvester {
	
	private OrganizationsDatanestHarvester harvester;
	
	private String[] fullRecord;
	private String[] recordWithoutDateTo;
	private String[] recordWithInvalidDateFrom;
	private String[] recordWithSpecialCharsInName;

	@Before
	public void setUp() throws Exception {
		harvester = new OrganizationsDatanestHarvester();
		
		fullRecord = new String[OrganizationsDatanestHarvester.ATTR_INDEX_SOURCE + 1];
		fullRecord[OrganizationsDatanestHarvester.ATTR_INDEX_NAME] = OrganizationTestData.TEST_NAME;
		fullRecord[OrganizationsDatanestHarvester.ATTR_INDEX_SEAT] = OrganizationTestData.TEST_SEAT;
		fullRecord[OrganizationsDatanestHarvester.ATTR_INDEX_ICO] = OrganizationTestData.TEST_ICO;
		fullRecord[OrganizationsDatanestHarvester.ATTR_INDEX_DATE_FROM] = OrganizationTestData.TEST_DATE_FROM;
		fullRecord[OrganizationsDatanestHarvester.ATTR_INDEX_DATE_TO] = OrganizationTestData.TEST_DATE_TO;
		fullRecord[OrganizationsDatanestHarvester.ATTR_INDEX_SOURCE] = OrganizationTestData.TEST_SOURCE;
		
		recordWithoutDateTo = Arrays.copyOf(fullRecord, fullRecord.length);
		recordWithoutDateTo[OrganizationsDatanestHarvester.ATTR_INDEX_DATE_TO] = OrganizationTestData.TEST_EMPTY_STRING;
		
		recordWithInvalidDateFrom = Arrays
				.copyOf(fullRecord, fullRecord.length);
		recordWithInvalidDateFrom[OrganizationsDatanestHarvester.ATTR_INDEX_DATE_FROM] = OrganizationTestData.TEST_INVALID_DATE_FROM;
		
		recordWithSpecialCharsInName = Arrays
				.copyOf(fullRecord, fullRecord.length);
		recordWithSpecialCharsInName[OrganizationsDatanestHarvester.ATTR_INDEX_NAME] = OrganizationTestData.TEST_NAME_WITH_SPECIAL_CHARS;
	}

	@Test
	public void testScrapOneRecordFull() {
		try {
			OrganizationRecord record = harvester.scrapOneRecord(fullRecord);
			
			assertEquals("name", OrganizationTestData.TEST_NAME, record.getName());
			assertEquals("seat", OrganizationTestData.TEST_SEAT, record.getSeat());
			assertEquals("ico", OrganizationTestData.TEST_ICO, record.getIco());
			
			Calendar dateFrom = Calendar.getInstance();
			dateFrom.setTime(record.getDateFrom());
			assertEquals("date from: year", 1991, dateFrom.get(Calendar.YEAR));
			assertEquals("date from: month", 7 - 1, dateFrom.get(Calendar.MONTH));
			assertEquals("date from: day", 17, dateFrom.get(Calendar.DAY_OF_MONTH));
			
			Calendar dateTo = Calendar.getInstance();
			dateTo.setTime(record.getDateTo());
			assertEquals("date to: year", 2011, dateTo.get(Calendar.YEAR));
			assertEquals("date to: month", 12 - 1, dateTo.get(Calendar.MONTH));
			assertEquals("date to: day", 6, dateTo.get(Calendar.DAY_OF_MONTH));
			
			assertEquals("source", OrganizationTestData.TEST_SOURCE, record.getSource());
		} catch (ParseException e) {
			fail("exception occured: " + e);
		}
	}

	@Test
	public void testScrapOneRecordWithoutDateTo() {
		try {
			OrganizationRecord record = harvester.scrapOneRecord(recordWithoutDateTo);
			
			assertEquals("date to", null, record.getDateTo());
		} catch (ParseException e) {
			fail("exception occured: " + e);
		}
	}

	@Test(expected=ParseException.class)
	public void testScrapOneRecordWithInvalidDateFrom() throws ParseException {
		harvester.scrapOneRecord(recordWithInvalidDateFrom);
	}

	@Test
	public void testScrapOneRecordWithSpecialCharsInName() {
		try {
			OrganizationRecord record = harvester.scrapOneRecord(recordWithSpecialCharsInName);
			
			assertEquals("name",
					"&lt;&quot;Test&quot; &amp; &apos;Name&apos;&gt;",
					record.getName());
		} catch (ParseException e) {
			fail("exception occured: " + e);
		}
	}

}
