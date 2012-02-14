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

package sk.opendata.odn.repository;


/**
 * This is a Open Data Node Repository interface defining "internal API"
 * between "serialization" and "repository" classes.
 *
 * @param <RecordType> type of records which are going to be stored in repository
 */
public interface OdnRepositoryInterface<RecordType> {
	
	/**
	 * Store given record into the back-end with given name.
	 * 
	 * Essentially, a repository can use multiple storages/back-ends so the
	 * {@code name} defines which one to store into.
	 * 
	 * @param name
	 *            name of the store/back-end to store into
	 * @param records
	 *            record to store
	 * @param contexts
	 *            the context for RDF statements used for the statements in the
	 *            repository (TODO: should be moved to 'records' so as to make the
	 *            Repository API "clean" and "back-end neutral")
	 * 
	 * @throws IllegalArgumentException
	 *             when some of the given arguments is not valid
	 * @throws OdnRepositoryException
	 *             when storage operation fails
	 */
	public void store(String name, RecordType records, String... contexts)
			throws IllegalArgumentException, OdnRepositoryException;

}
