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

package sk.opendata.odn.repository.solr;

import java.lang.reflect.InvocationTargetException;
import java.util.Date;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.solr.client.solrj.beans.Field;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sk.opendata.odn.model.AbstractRecord;
import sk.opendata.odn.serialization.OdnSerializationException;

/**
 * We're storing multiple harvested data sets in one SOLR index (see
 * http://wiki.apache.org/solr/MultipleIndexes for basic rationale why).
 * 
 * To distinguish between data sets (and to allow searching only in
 * specific data set), field "type" is used.
 * 
 * Theory of operation:
 * <ol>
 * <li>data are harvested, parsed, ... and stored in a relevant bean
 * {@code sk.opendata.odn.model.<Some>Record}</li>
 * <li>{@code SolrItem} is created from the "record bean" using
 * appropriate {@code SolrItem} constructor</li>
 * <li>"SOLR items" are collected in list and pushed to SOLR</li>
 * </ol>
 */
public class SolrItem {
	
	private static Logger logger = LoggerFactory.getLogger(SolrItem.class);
	
	// common fields:
	@Field
	private String id;
	@Field("type_s")
	private SolrItemType type;
	// "organization record" fields: should match what is available in
	// 'OrganizationRecord' and we need only those fields which are going to be
	// used in SOLR index
	// note: for now, to avoid changing schema, we're taking advantage of
	// dynamic field definitions.
	@Field("source_s")
	private String source;
	@Field("name_s")
	private String name;
	@Field("legal_form_s")
	private String legalForm;
	@Field("seat_s")
	private String seat;
	@Field("ico_s")
	private String ico;
	@Field("date_from_dt")
	private Date dateFrom;
	@Field("date_to_dt")
	private Date dateTo;
	

	/**
	 * Create SOLR item from given record.
	 * 
	 * @param source
	 *            record - source of data
	 * @param type
	 *            type of the record
	 * @param id
	 *            ID of the record
	 * 
	 * @return SOLR item created from given data
	 * 
	 * @throws OdnSerializationException
	 *             when conversion into SOLR beans fails
	 */
	public static SolrItem createSolrItem(AbstractRecord source)
			throws OdnSerializationException {

		SolrItem solrItem = null;
		
		try {
			solrItem = new SolrItem();
			PropertyUtils.copyProperties(solrItem, source);
			solrItem.type = SolrItemType.getType(source.getClass());
			solrItem.id = source.getId();
		} catch (IllegalAccessException e) {
			logger.error("illegal access exception", e);
			throw new OdnSerializationException(e.getMessage(), e);
		} catch (InvocationTargetException e) {
			logger.error("invocation target exception", e);
			throw new OdnSerializationException(e.getMessage(), e);
		} catch (NoSuchMethodException e) {
			logger.error("no such method exception", e);
			throw new OdnSerializationException(e.getMessage(), e);
		}
		
		return solrItem;
	}
	
	
	public String toString() {
		try {
			return BeanUtils.describe(this).toString();
		} catch (IllegalAccessException e) {
			logger.error("illegal access exception", e);
		} catch (InvocationTargetException e) {
			logger.error("invocation target exception", e);
		} catch (NoSuchMethodException e) {
			logger.error("no such method exception", e);
		}
		
		return super.toString();
	}


	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public SolrItemType getType() {
		return type;
	}

	public void setType(SolrItemType type) {
		this.type = type;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getLegalForm() {
		return legalForm;
	}

	public void setLegalForm(String legalForm) {
		this.legalForm = legalForm;
	}

	public String getSeat() {
		return seat;
	}

	public void setSeat(String seat) {
		this.seat = seat;
	}

	public String getIco() {
		return ico;
	}

	public void setIco(String ico) {
		this.ico = ico;
	}

	public Date getDateFrom() {
		return dateFrom;
	}

	public void setDateFrom(Date dateFrom) {
		this.dateFrom = dateFrom;
	}

	public Date getDateTo() {
		return dateTo;
	}

	public void setDateTo(Date dateTo) {
		this.dateTo = dateTo;
	}

}
