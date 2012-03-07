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

package sk.opendata.odn.utils;

/**
 * Various functions related to PSC (postove smerove cislo) - slovak equivalent of ZIP code. 
 */
public class PscUtil {
	
	/**
	 * Determine, if given string looks like PCS.
	 * 
	 * @param s string to be tested
	 * @return {@code true} if given string looks like PSC, otherwise {@code false}
	 */
	public static boolean isPsc(String s) {
		return s.trim().matches("[0-9]{3}\\s*[0-9]{2}");
	}
	
	/**
	 * Normalize given string containing PCS, i.e. remove all white spaces from it.
	 * 
	 * Example:	"058 01" -> "05801"
	 * 
	 * Purpose: We're processing PSC from various sources. To make sure we do not store
	 * duplicated and we're searching for proper PCS, we normalize PCS before storage and
	 * also before we include it in the search query.
	 * 
	 * @param pcs to normalize
	 * @return normalized PCS
	 */
	public static String normalize(String pcs) {
		return pcs.trim().replaceAll("\\s+", "");
	}

}
