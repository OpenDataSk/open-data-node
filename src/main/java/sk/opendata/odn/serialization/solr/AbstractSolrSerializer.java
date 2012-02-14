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

import sk.opendata.odn.repository.OdnRepositoryException;
import sk.opendata.odn.repository.OdnRepositoryInterface;
import sk.opendata.odn.repository.solr.SolrItem;

/**
 * Stuff common to all OpenData.sk SOLR serializers.
 * 
 * @param <RecordType>
 *            type of individual record which will be converted to SOLR bean
 */
public abstract class AbstractSolrSerializer<RecordType> {

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
	 * @throws IllegalAccessException if serialization into SOLR bean fails
	 * @throws InvocationTargetException if serialization into SOLR bean fails
	 * @throws NoSuchMethodException if serialization into SOLR bean fails
	 */
	public abstract List<SolrItem> serialize(List<RecordType> records)
			throws IllegalAccessException, InvocationTargetException,
			NoSuchMethodException;

	/**
	 * Serialize and store given organization record.
	 * 
	 * @param records
	 *            list of organization records to serialize and store
	 * 
	 * TODO: redo the exception list as appropriate
	 * @throws OdnRepositoryException
	 *             when we fail to store given data into repository
	 * @throws IllegalArgumentException
	 *             if repository with given name does not exists
	 * @throws IllegalAccessException
	 *             if {@code BeanUtils.copyProperties()} fails
	 * @throws InvocationTargetException
	 *             if {@code BeanUtils.copyProperties()} fails
	 * @throws NoSuchMethodException 
	 *             if {@code PropertyUtils.copyProperties()} fails
	 */
	public abstract void store(Vector<RecordType> records)
			throws IllegalArgumentException, OdnRepositoryException,
			IllegalAccessException, InvocationTargetException, NoSuchMethodException;
}
