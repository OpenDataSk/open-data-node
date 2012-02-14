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

package sk.opendata.odn.model;

/**
 * Attributes and methods common to all records harvested by Open Data Node.
 */
public abstract class AbstractRecord {
	
	private String id;
	
	/**
	 * @return ID of the record unique for whole Open Data Node (i.e. even
	 *         between multiple data sets)
	 */
	public String getId() {
		return this.id;
	}

	/**
	 * Sets the record ID. ID have to be unique for whole Open Data Node, i.e.
	 * also between data sets.
	 * 
	 * Implementation note: Say we're harvesting two data sets. Both data sets
	 * already do have an ID fields, both use sequence from 0. The easiest way
	 * to generate non-colliding ID for this setter is prepending data set
	 * name/id to the field ID, for example:
	 * 
	 * record.setId("dataset1_" + harvestedRecord.getId())
	 * 
	 * @param id
	 *            record ID
	 */
	public void setId(String id) {
		this.id = id;
	}
	
}
