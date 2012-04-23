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
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.PropertyModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sk.opendata.odn.repository.solr.SolrRepository;
import sk.opendata.odn.repository.solr.SolrItem;
import sk.opendata.odn.utils.PscUtil;

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
			
			// TODO: rework into BookmarkablePageLink to a page which will
			// display the given item based on ID
			item.add(new ExternalLink("itemUrl",
					"http://www.opendata.sk/item/" + solrResultItem.getId(),
					solrResultItem.getId() + ": " + solrResultItem.getName()));
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
		
    	SolrRepository solrRepository = SolrRepository.getInstance();
    	
    	SolrQuery solrQuery = new SolrQuery();
    	solrQuery.setQuery(query);
    	
    	// prepare the SOLR query
    	// we're trying to identify some "basic" query types and perform tuning those special cases
    	if (query.matches("[0-9]{8}")) {
    		// ICO
    		solrQuery.set("defType", "dismax");
    		solrQuery.set("qf",
    				"ico^2 " +
    				"donor_ico " +
    				"customer_ico " +
    				"supplier_ico"
    				);
    	} else if (PscUtil.isPsc(query)) {
    		// PCS
    		solrQuery.set("defType", "dismax");
    		solrQuery.set("qf",
    				"donor_psc^2 " +
    				"seat " +		// TODO: this is "text_general' in SOLR schema and here PSC might still be in a form "058 01" thus searching for "05801" wont match it
    				"donor_address"	// TODO: -"-
    				);
    		// re-set query to normalized PSC
    		solrQuery.setQuery(PscUtil.normalize(query));
    	} else {
    		// default
    		solrQuery.set("defType", "dismax");
    		solrQuery.set("qf",
    				"name^3 " +
    				"legal_form^0.5 " +
    				"seat " +
    				"ico^2 " +
    				"date_from " +
    				"date_to " +
    				"donor_name^2 " +
    				"donor_surname^2 " +
    				"donor_company^2 " +
    				"donor_ico " +
    				"currency^0.5 " +
    				"donor_address " +
    				"donor_psc " +
    				"donor_city " +
    				"recipient_party^0.75 " +
    				"year " +
    				"accept_date " +
    				"note^0.5 " +
    				"procurement_subject " +
    				"customer_ico " +
    				"supplier_ico"
    				);
    	}
		
		// TODO: paging - for now we just limit the search result list
    	int page = 0;
    	solrQuery.setStart(page * RESULTS_PER_PAGE);
    	solrQuery.setRows(RESULTS_PER_PAGE);
		
    	logger.info("XXX SOLR query: " + solrQuery.toString());
		
		
		// obtain results
		QueryResponse queryResponse = solrRepository.getSolrServer().query(solrQuery);
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
