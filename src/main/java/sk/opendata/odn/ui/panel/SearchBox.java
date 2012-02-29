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

package sk.opendata.odn.ui.panel;

import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.form.StatelessForm;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.PropertyModel;

public class SearchBox extends Panel {
	
	private static final long serialVersionUID = -7569041415066592320L;
	
	private String query;

	
	private class SearchForm extends StatelessForm<Void> {
		private static final long serialVersionUID = 6173832378336743503L;
		
		public SearchForm(String id) {
			super(id);
		}

		@Override
		protected void onSubmit() {
			PageParameters params = new PageParameters();
			
			if (query != null && !query.isEmpty())
				params.add("q", query);
			
			setResponsePage(getApplication().getHomePage(), params);
		}
	}

	
	public SearchBox(String id) {
		super(id);
		
		SearchForm searchForm = new SearchForm("searchform");
		add(searchForm);
		
		PropertyModel<String> queryModel = new PropertyModel<String>(this, "query");
		searchForm.add(new TextField<String>("query", queryModel));
	}


	public String getQuery() {
		return query;
	}

	public void setQuery(String query) {
		this.query = query;
	}

}
