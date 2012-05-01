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

package sk.opendata.odn.serialization;

import java.util.List;

import sk.opendata.odn.repository.OdnRepositoryException;
import sk.opendata.odn.repository.OdnRepositoryInterface;

/**
 * Stuff common to all OpenData.sk serializers.
 * 
 * Serializer is given harvested data, it converts them into form suitable to a
 * repository and then passes them to the repository.
 * 
 * @param <SerializationInputType>
 *            type of the input data, of the individual record, which will be
 *            serialized
 * @param <SerializationOutputType>
 *            type of the output data, the result of serialization
 * @param <RepositoryStoreType>
 *            type of the data pushed to the repository
 */
public abstract class AbstractSerializer<SerializationInputType, SerializationOutputType, RepositoryStoreType> {

	private OdnRepositoryInterface<RepositoryStoreType> repository;

	
	/**
	 * Initialize serializer to use given repository.
	 * 
	 * @param repository
	 *            repository to use for storage of record
	 * 
	 * @throws IllegalArgumentException
	 *             if repository is {@code null}
	 */
	public AbstractSerializer(
			OdnRepositoryInterface<RepositoryStoreType> repository)
			throws IllegalArgumentException {

		if (repository == null)
			throw new IllegalArgumentException("repository is null");
		this.repository = repository;
	}

	/**
	 * Serialize given harvested records into the form suitable for storage in
	 * repository.
	 * 
	 * @param records to be serialized
	 * @return records converted to a form suitable to be stored into repository
	 * 
	 * @throws OdnSerializationException when serialization fails
	 */
	public abstract SerializationOutputType serialize(List<SerializationInputType> records)
			throws OdnSerializationException;

	/**
	 * Serialize and store given records.
	 * 
	 * @param records
	 *            list of records to serialize and store
	 * 
	 * @throws IllegalArgumentException
	 *             if repository with given name does not exists
	 * @throws OdnSerializationException
	 *             when serialization fails
	 * @throws OdnRepositoryException
	 *             when we fail to store given data into repository
	 */
	public abstract void store(List<SerializationInputType> records)
			throws IllegalArgumentException, OdnSerializationException,
			OdnRepositoryException;

	
	public OdnRepositoryInterface<RepositoryStoreType> getRepository() {
		return repository;
	}

	public void setRepository(
			OdnRepositoryInterface<RepositoryStoreType> repository) {
		this.repository = repository;
	}

}
