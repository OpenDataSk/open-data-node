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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.PropertyModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sk.opendata.odn.repository.solr.SolrBackend;

public class ResultPanel extends Panel {
	
	private static final long serialVersionUID = 6204753930519101266L;
	private static Logger logger = LoggerFactory.getLogger(ResultPanel.class);
	
	public final static int RESULTS_PER_PAGE = 20;
	
	
	private class ResultListView extends ListView<SolrItem> {

		private static final long serialVersionUID = -3631484307956485036L;

		public ResultListView(String id, List<? extends SolrItem> list) {
			super(id, list);
		}

		@Override
		protected void populateItem(ListItem<SolrItem> item) {
			final SolrItem solrResultItem = item.getModelObject();
			
			item.add(new Label("itemUrl", "TODO: itemUrl"));
			item.add(new Label("itemTitle", "TODO: itemTitle"));
		}
		
	}

	
	private String query;
	private List<SolrItem> resultList = new ArrayList<SolrItem>();
	private String resultCount = "TODO: number of results found";
	
	
	public ResultPanel(String id) {
		super(id);
		
		PropertyModel<String> resultCountModel = new PropertyModel<String>(this, "resultCount");
		add(new Label("resultCount", resultCountModel));
		
		ResultListView resultView = new ResultListView("resultlist", resultList);
		add(resultView);
	}

    
    /**
     * Update the search results display with items obtained using given query.
     * 
     * @param query SOLR dismax (i.e. user friendly) query
     * @throws IOException when creation of SOLR back-end fails
     * @throws SolrServerException when SOLR query fails
     */
	public void doSearch(final String query) throws IOException, SolrServerException {
		if (query.isEmpty())
			// nothing to do with empty search query
			return;
		
    	SolrBackend solrBackend = SolrBackend.getInstance();
    	
    	SolrQuery solrQuery = new SolrQuery();
    	solrQuery.setQuery(query);
    	
    	// prepare the SOLR query
    	// we're trying to identify some "basic" query types and perform tuning those special cases
    	if (query.matches("[0-9]{8}")) {
    		// ICO
    		solrQuery.set("defType", "dismax");
    		solrQuery.set("qf",
    				"ico_s^2 " +
    				"donor_ico_s " +
    				"customer_ico_s " +
    				"supplier_ico_s"
    				);
    	}
    	else {
    		// default
    		solrQuery.set("defType", "dismax");
    		solrQuery.set("qf",
    				"name_s^3 " +
    				"legal_form_s^0.5 " +
    				"seat_s ico_s^2 " +
    				"date_from_dt " +
    				"date_to_dt " +
    				"donor_name_s^2 " +
    				"donor_surname_s^2 " +
    				"donor_company_s^2 " +
    				"donor_ico_s " +
    				"donor_address_s " +
    				"donor_psc_s " +
    				"donor_city_s " +
    				"recipient_party_s^0.75 " +
    				"year_s " +
    				"accept_date_dt " +
    				"note_s^0.5 " +
    				"procurement_subject_s " +
    				"customer_ico_s " +
    				"supplier_ico_s"
    				);
    	}
		
		// TODO: paging - for now we just limit the search result list
    	int page = 0;
    	solrQuery.setStart(page * RESULTS_PER_PAGE);
    	solrQuery.setRows(RESULTS_PER_PAGE);
		
    	logger.info("XXX SOLR query: " + solrQuery.toString());
		
		
		// obtain results
		QueryResponse queryResponse = solrBackend.getSolrServer().query(solrQuery);
		List<SolrItem> responseItems = queryResponse.getBeans(SolrItem.class);
		
		// update the display
		resultList.addAll(responseItems);
    }

    
	public String getQuery() {
		return query;
	}

	public void setQuery(String query) {
		this.query = query;
	}

	public String getResultCount() {
		return resultCount;
	}

	public void setResultCount(String resultCount) {
		this.resultCount = resultCount;
	}

}
