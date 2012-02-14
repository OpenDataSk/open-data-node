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

package sk.opendata.odn.serialization.solr;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Vector;

import sk.opendata.odn.model.OrganizationRecord;
import sk.opendata.odn.repository.OdnRepositoryException;
import sk.opendata.odn.repository.OdnRepositoryInterface;
import sk.opendata.odn.repository.solr.SolrItem;

/**
 * This class is used by a Harvester to serialize organization records into beans
 * intended to be added to SOLR and store them in Repository.
 */
public class OrganizationSolrSerializer extends AbstractSolrSerializer<OrganizationRecord> {
	
	/**
	 * Initialize serializer to use given repository.
	 * 
	 * @param repository
	 *            repository to use for storage of record
	 * @param name
	 *            name of the storage/back-end to store into
	 */
	public OrganizationSolrSerializer(OdnRepositoryInterface<List<SolrItem>> repository,
			String name) {
	
		super(repository, name);
	}
	
	@Override
	public List<SolrItem> serialize(List<OrganizationRecord> records)
			throws IllegalAccessException, InvocationTargetException,
			NoSuchMethodException {

		Vector<SolrItem> solrItems = new Vector<SolrItem>(records.size());
		for (OrganizationRecord or : records) {
			SolrItem solrItem = SolrItem.createSolrItem(or);
			solrItems.add(solrItem);
		}
		
		return solrItems;
	}
	
	@Override
	public void store(Vector<OrganizationRecord> records)
			throws IllegalArgumentException, OdnRepositoryException,
			IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		
		repository.store("XXX", serialize(records));
	}

}
