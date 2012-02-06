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

package sk.opendata.odn.repository.sesame;

/**
 * This class holds everything what is necessary to push some RDF data into
 * Sesame repository.
 */
public class RdfData {
	String rdfData;
	String rdfBaseURI;
	
	public RdfData(String rdfData, String rdfBaseURI) {
		super();
		this.rdfData = rdfData;
		this.rdfBaseURI = rdfBaseURI;
	}

	public String getRdfData() {
		return rdfData;
	}

	public void setRdfData(String rdfData) {
		this.rdfData = rdfData;
	}

	public String getRdfBaseURI() {
		return rdfBaseURI;
	}

	public void setRdfBaseURI(String rdfBaseURI) {
		this.rdfBaseURI = rdfBaseURI;
	}
}
