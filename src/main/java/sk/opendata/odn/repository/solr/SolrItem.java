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

import sk.opendata.odn.model.OrganizationRecord;
import sk.opendata.odn.model.PoliticalPartyDonationRecord;
import sk.opendata.odn.model.ProcurementRecord;

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
	 * Create SOLR item from given record, with given type and ID.
	 * 
	 * TODO: Make this more general (i.e., auto-determine the type from given
	 * class plus make some order with the IDs for various records), get rid of
	 * other public versions and make this public instead. Try not making a
	 * woodoo out of it.
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
	 * @throws IllegalAccessException
	 *             if {@code PropertyUtils.copyProperties()} fails
	 * @throws InvocationTargetException
	 *             if {@code PropertyUtils.copyProperties()} fails
	 * @throws NoSuchMethodException
	 *             if {@code PropertyUtils.copyProperties()} fails
	 */
	private static SolrItem createSolrItem(Object source, SolrItemType type,
			String id) throws IllegalAccessException,
			InvocationTargetException, NoSuchMethodException {

		SolrItem solrItem = new SolrItem();
		PropertyUtils.copyProperties(solrItem, source);
		solrItem.type = type;
		solrItem.id = id;
		return solrItem;
	}
	
	/**
	 * Create SOLR item from given organization record.
	 * 
	 * @param or
	 *            organization record
	 * @return SOLR item
	 * @throws IllegalAccessException
	 *             if {@code PropertyUtils.copyProperties()} fails
	 * @throws InvocationTargetException
	 *             if {@code PropertyUtils.copyProperties()} fails
	 * @throws NoSuchMethodException 
	 *             if {@code PropertyUtils.copyProperties()} fails
	 */
	public static SolrItem createSolrItem(OrganizationRecord or)
			throws IllegalAccessException, InvocationTargetException,
			NoSuchMethodException {

		return createSolrItem(or,
				SolrItemType.ORGANIZATION_RECORD,
				"XXX_orgRec_" + or.getIco());
	}

	/**
	 * Create SOLR item from given political party donation record.
	 * 
	 * @param ppdr political party donation record
	 * @return SOLR item
	 * @throws IllegalAccessException
	 *             if {@code PropertyUtils.copyProperties()} fails
	 * @throws InvocationTargetException
	 *             if {@code PropertyUtils.copyProperties()} fails
	 * @throws NoSuchMethodException 
	 *             if {@code PropertyUtils.copyProperties()} fails
	 */
	public static SolrItem createSolrItem(PoliticalPartyDonationRecord ppdr)
			throws IllegalAccessException, InvocationTargetException,
			NoSuchMethodException {

		return createSolrItem(ppdr,
				SolrItemType.POLITICAL_PARTY_DONATION_RECORD,
				"XXX_ppdRec_" + ppdr.getId());
	}

	/**
	 * Create SOLR item from given procurement record.
	 * 
	 * @param pr procurement record
	 * @return SOLR item
	 * @throws IllegalAccessException
	 *             if {@code PropertyUtils.copyProperties()} fails
	 * @throws InvocationTargetException
	 *             if {@code PropertyUtils.copyProperties()} fails
	 * @throws NoSuchMethodException 
	 *             if {@code PropertyUtils.copyProperties()} fails
	 */
	public static SolrItem createSolrItem(ProcurementRecord pr)
			throws IllegalAccessException, InvocationTargetException,
			NoSuchMethodException {

		return createSolrItem(pr,
				SolrItemType.PROCUREMENT_RECORD,
				"XXX_procurementRec_" + pr.getId());
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
