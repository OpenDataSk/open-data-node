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
import sk.opendata.odn.repository.OdnRepositoryStoreInterface;
import sk.opendata.odn.repository.solr.SolrItem;
import sk.opendata.odn.serialization.AbstractSerializer;
import sk.opendata.odn.serialization.OdnSerializationException;

/**
 * This class is used by a Harvester to serialize various records into beans
 * intended to be added to SOLR and store them in Repository.
 * 
 * Note in regards to "various records": We're storing entries from several
 * datasets (thus multiple types of entries) into just one SOLR index. Thus
 * {@code SolrItem} is used to convert each particular type into one SOLR Type
 * via {@code SolrItem.createSolrItem}.
 * 
 * @param <RecordType>
 *            type of individual record which will be converted to SOLR bean
 */
public class SolrSerializer<RecordType extends AbstractRecord> extends
		AbstractSerializer<RecordType, List<SolrItem>, List<SolrItem>> {
	
	/**
	 * Initialize serializer to use given repository.
	 * 
	 * @param repository
	 *            repository to use for storage of record
	 * 
	 * @throws IllegalArgumentException
	 *             if repository is {@code null}
	 */
	public SolrSerializer(OdnRepositoryStoreInterface<List<SolrItem>> repository)
			throws IllegalArgumentException {
	
		super(repository);
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
	@Override
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
	public void store(List<RecordType> records)
			throws OdnSerializationException, OdnRepositoryException {
		
		getRepository().store(serialize(records));
	}
}
