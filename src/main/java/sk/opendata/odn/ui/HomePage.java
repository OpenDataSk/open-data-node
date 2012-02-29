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

package sk.opendata.odn.ui;

import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.WebPage;

import sk.opendata.odn.ui.panel.SearchBox;

/**
 * Open Data Node "management console" homepage.
 */
public class HomePage extends WebPage {

	private static final long serialVersionUID = 1L;
	//private static Logger logger = LoggerFactory.getLogger(HomePage.class);
	

    /**
	 * Constructor that is invoked when page is invoked without a session.
	 * 
	 * @param parameters
	 *            Page parameters
	 */
    public HomePage(final PageParameters parameters) {
    	String query = parameters.getString("q", "");
    	
    	SearchBox searchBox = new SearchBox("searchbox");
    	searchBox.setQuery(query);
    	add(searchBox);
    	
    	doSearch(parameters);
    }
    
    private void doSearch(final PageParameters parameters) {
    	// TODO
    }
}
