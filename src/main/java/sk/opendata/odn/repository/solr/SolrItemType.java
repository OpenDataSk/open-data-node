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

package sk.opendata.odn.repository.solr;

import java.util.HashMap;
import java.util.Map;

import sk.opendata.odn.model.OrganizationRecord;
import sk.opendata.odn.model.PoliticalPartyDonationRecord;
import sk.opendata.odn.model.ProcurementRecord;

/**
 * SOLR item type, used to distinguish between "data sets" stored
 * in one SOLR index.
 * 
 * Each type maps to one record type in 'sk.opendata.odn.model'.
 */
public enum SolrItemType {
	ORGANIZATION_RECORD,
	POLITICAL_PARTY_DONATION_RECORD,
	PROCUREMENT_RECORD;

	private final static Map<Class<?>, SolrItemType> lookup = new HashMap<Class<?>, SolrItemType>();
	
	
	static {
		lookup.put(OrganizationRecord.class, ORGANIZATION_RECORD);
		lookup.put(PoliticalPartyDonationRecord.class, POLITICAL_PARTY_DONATION_RECORD);
		lookup.put(ProcurementRecord.class, PROCUREMENT_RECORD);
	}
	
	
	/**
	 * Determine record type for given class.
	 * 
	 * @param recordClass
	 *            class of the record
	 * @return enum value corresponding to given record
	 * @throws IllegalArgumentException
	 *             when given record is not known
	 */
	public static SolrItemType getType(Class<?> recordClass) throws IllegalArgumentException {
		SolrItemType result = lookup.get(recordClass);
		
		if (result == null)
			throw new IllegalArgumentException(recordClass.getCanonicalName());
		
		return result;
	}
}
