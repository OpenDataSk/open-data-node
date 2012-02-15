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

import java.util.List;
import java.util.Vector;

import sk.opendata.odn.model.AbstractRecord;
import sk.opendata.odn.repository.OdnRepositoryException;
import sk.opendata.odn.repository.OdnRepositoryInterface;
import sk.opendata.odn.repository.solr.SolrItem;
import sk.opendata.odn.serialization.OdnSerializationException;

/**
 * Stuff common to all OpenData.sk SOLR serializers.
 * 
 * @param <RecordType>
 *            type of individual record which will be converted to SOLR bean
 */
public abstract class AbstractSolrSerializer<RecordType extends AbstractRecord> {

	protected OdnRepositoryInterface<List<SolrItem>> repository;

	
	/**
	 * Initialize serializer to use given repository.
	 * 
	 * @param repository
	 *            repository to use for storage of record
	 */
	public AbstractSolrSerializer(OdnRepositoryInterface<List<SolrItem>> repository) {

		this.repository = repository;
	}

	/**
	 * Serialize all given records into beans for SOLR.
	 * 
	 * @param records
	 *            list of records as harvested
	 *
	 * @return list of records suitable to be pushed into SOLR
	 *
	 * @throws OdnSerializationException
	 *             when conversion into SOLR beans fails
	 */
	public List<SolrItem> serialize(List<RecordType> records)
			throws OdnSerializationException {
		
		Vector<SolrItem> solrItems = new Vector<SolrItem>(records.size());
		for (RecordType record : records) {
			SolrItem solrItem = SolrItem.createSolrItem(record);
			solrItems.add(solrItem);
		}
		
		return solrItems;
	}

	/**
	 * Serialize and store given organization record.
	 * 
	 * @param records
	 *            list of organization records to serialize and store
	 * 
	 * @throws OdnRepositoryException
	 *             when we fail to store given data into repository
	 * @throws OdnSerializationException
	 *             when conversion into SOLR beans fails
	 */
	public void store(Vector<RecordType> records)
			throws OdnSerializationException, OdnRepositoryException {
		
		repository.store(serialize(records));
	}
}
