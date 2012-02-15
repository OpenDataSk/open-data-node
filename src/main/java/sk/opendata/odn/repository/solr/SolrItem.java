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
import sk.opendata.odn.model.Currency;
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
	// "political party donation record" fields: should match what is available
	// in 'PoliticalPartyDonationRecord' and we need only those fields which are
	// going to be used in SOLR index
	// note: for now, to avoid changing schema, we're taking advantage of
	// dynamic field definitions.
	@Field("donor_name_s")
	private String donorName;
	@Field("donor_surname_s")
	private String donorSurname;
	@Field("donor_company_s")
	private String donorCompany;
	@Field("donor_ico_s")
	private String donorIco;
	@Field("donation_value_f")
	private float donationValue;
	@Field("donation_currency_s")
	private Currency donationCurrency;
	@Field("donor_address_s")
	private String donorAddress;
	@Field("donor_psc_s")
	private String donorPsc;
	@Field("donor_city_s")
	private String donorCity;
	@Field("recipient_party_s")
	private String recipientParty;
	@Field("year_s")
	private String year;
	@Field("accept_date_dt")
	private Date acceptDate;
	@Field("note_s")
	private String note;
	// "procurement record" fields: should match what is available in
	// 'ProcurementRecord' and we need only those fields which are going to be
	// used in SOLR index
	// note: for now, to avoid changing schema, we're taking advantage of
	// dynamic field definitions.
	//private String note;	- same name as field in PoliticalPartyDonationRecord
	//private String year;	- same name as field in PoliticalPartyDonationRecord
	@Field("bulletin_id_s")
	private String bulletinId;
	@Field("procurement_id_s")
	private String procurementId;
	@Field("procurement_subject_s")
	private String procurementSubject;
	@Field("price_f")
	private float price;
	@Field("currency_s")
	private Currency currency;
	@Field("vat_included_b")
	private boolean isVatIncluded;
	@Field("customer_ico_s")
	private String customerIco;
	@Field("supplier_ico_s")
	private String supplierIco;
	

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

	public String getDonorName() {
		return donorName;
	}

	public void setDonorName(String donorName) {
		this.donorName = donorName;
	}

	public String getDonorSurname() {
		return donorSurname;
	}

	public void setDonorSurname(String donorSurname) {
		this.donorSurname = donorSurname;
	}

	public String getDonorCompany() {
		return donorCompany;
	}

	public void setDonorCompany(String donorCompany) {
		this.donorCompany = donorCompany;
	}

	public String getDonorIco() {
		return donorIco;
	}

	public void setDonorIco(String donorIco) {
		this.donorIco = donorIco;
	}


	public float getDonationValue() {
		return donationValue;
	}

	public void setDonationValue(float donationValue) {
		this.donationValue = donationValue;
	}

	public Currency getDonationCurrency() {
		return donationCurrency;
	}

	public void setDonationCurrency(Currency donationCurrency) {
		this.donationCurrency = donationCurrency;
	}

	public String getDonorAddress() {
		return donorAddress;
	}

	public void setDonorAddress(String donorAddress) {
		this.donorAddress = donorAddress;
	}

	public String getDonorPsc() {
		return donorPsc;
	}

	public void setDonorPsc(String donorPsc) {
		this.donorPsc = donorPsc;
	}

	public String getDonorCity() {
		return donorCity;
	}

	public void setDonorCity(String donorCity) {
		this.donorCity = donorCity;
	}

	public String getRecipientParty() {
		return recipientParty;
	}

	public void setRecipientParty(String recipientParty) {
		this.recipientParty = recipientParty;
	}

	public String getYear() {
		return year;
	}

	public void setYear(String year) {
		this.year = year;
	}

	public Date getAcceptDate() {
		return acceptDate;
	}

	public void setAcceptDate(Date acceptDate) {
		this.acceptDate = acceptDate;
	}

	public String getNote() {
		return note;
	}

	public void setNote(String note) {
		this.note = note;
	}

	public String getBulletinId() {
		return bulletinId;
	}

	public void setBulletinId(String bulletinId) {
		this.bulletinId = bulletinId;
	}

	public String getProcurementId() {
		return procurementId;
	}

	public void setProcurementId(String procurementId) {
		this.procurementId = procurementId;
	}

	public String getProcurementSubject() {
		return procurementSubject;
	}

	public void setProcurementSubject(String procurementSubject) {
		this.procurementSubject = procurementSubject;
	}

	public float getPrice() {
		return price;
	}

	public void setPrice(float price) {
		this.price = price;
	}

	public Currency getCurrency() {
		return currency;
	}

	public void setCurrency(Currency currency) {
		this.currency = currency;
	}

	public boolean isVatIncluded() {
		return isVatIncluded;
	}

	public void setVatIncluded(boolean isVatIncluded) {
		this.isVatIncluded = isVatIncluded;
	}

	public String getCustomerIco() {
		return customerIco;
	}

	public void setCustomerIco(String customerIco) {
		this.customerIco = customerIco;
	}

	public String getSupplierIco() {
		return supplierIco;
	}

	public void setSupplierIco(String supplierIco) {
		this.supplierIco = supplierIco;
	}

}
