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

import java.lang.reflect.InvocationTargetException;
import java.util.Date;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.solr.client.solrj.beans.Field;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * FIXME:
 * This is almost exact copy of {@code sk.opendata.odn.repository.solr.SolrItem} with
 * ONLY following differences:
 * 
 * 1) type of {@code type} is {@code String} instead of
 * {@sk.opendata.odn.repository.solr.SolrItemType} so as to work around the issue with
 * obtaining results from SOLR using beans:
 * 
 * Caused by: java.lang.RuntimeException: Exception while setting value : ORGANIZATION_RECORD on private sk.opendata.odn.repository.solr.SolrItemType sk.opendata.odn.repository.solr.SolrItem.type
 * 	at org.apache.solr.client.solrj.beans.DocumentObjectBinder$DocField.set(DocumentObjectBinder.java:380)
 * 	at org.apache.solr.client.solrj.beans.DocumentObjectBinder$DocField.inject(DocumentObjectBinder.java:362)
 * 	at org.apache.solr.client.solrj.beans.DocumentObjectBinder.getBean(DocumentObjectBinder.java:67)
 * 	at org.apache.solr.client.solrj.beans.DocumentObjectBinder.getBeans(DocumentObjectBinder.java:47)
 * 	at org.apache.solr.client.solrj.response.QueryResponse.getBeans(QueryResponse.java:452)
 * 	at sk.opendata.odn.ui.panel.ResultPanel.doSearch(ResultPanel.java:147)
 * 
 * 2) type of {@code currency} is {@code String} instead of
 * {@sk.opendata.odn.model.Currency} for reasons similar to those stated in item #1 (above)
 * 
 * 3) 'createSolrItem()' removed
 * 
 * Until that issue is resolved in a better way, we need to maintain this class.
 * 
 * Hint: Maybe {@code @FieldObject} annotation in upcoming SOLR 3.6.0 will help.
 */
public class SolrItem {
	
	private static Logger logger = LoggerFactory.getLogger(SolrItem.class);
	
	// common fields:
	@Field
	private String id;
	@Field
	private String type;
	// "organization record" fields: should match what is available in
	// 'OrganizationRecord' and we need only those fields which are going to be
	// used in SOLR index
	// note: for now, to avoid changing schema, we're taking advantage of
	// dynamic field definitions.
	@Field
	private String name;
	@Field("legal_form")
	private String legalForm;
	@Field
	private String seat;
	@Field
	private String ico;
	@Field("date_from")
	private Date dateFrom;
	@Field("date_to")
	private Date dateTo;
	// "political party donation record" fields: should match what is available
	// in 'PoliticalPartyDonationRecord' and we need only those fields which are
	// going to be used in SOLR index
	// note: for now, to avoid changing schema, we're taking advantage of
	// dynamic field definitions.
	@Field("donor_name")
	private String donorName;
	@Field("donor_surname")
	private String donorSurname;
	//private String donorCompany;	- we will map it to 'name' from OrganizationRecord as it is organization name too
	//private String donorIco;	- we will map it to 'ico' from OrganizationRecord as it is ICO of organization too
	@Field("donation_value")
	private float donationValue;
	@Field
	private String currency;
	@Field("donor_address")
	private String donorAddress;
	@Field("donor_psc")
	private String donorPsc;	// TODO: we use "string" in SOLR schema => make sure we use same form by filtering out spaces (i.e. to prevent cases like "058 01" and "05801" being considered different PSC)
	@Field("donor_city")
	private String donorCity;
	@Field("recipient_party")
	private String recipientParty;
	@Field("year")
	private String year;
	@Field("accept_date")
	private Date acceptDate;
	@Field("note")
	private String note;
	// "procurement record" fields: should match what is available in
	// 'ProcurementRecord' and we need only those fields which are going to be
	// used in SOLR index
	// note: for now, to avoid changing schema, we're taking advantage of
	// dynamic field definitions.
	//private String note;	- same name as field in PoliticalPartyDonationRecord
	//private String year;	- same name as field in PoliticalPartyDonationRecord
	@Field("bulletin_id")
	private String bulletinId;
	@Field("procurement_id")
	private String procurementId;
	@Field("procurement_subject")
	private String procurementSubject;
	@Field("price")
	private float price;
	//private Currency currency;	- same name as field in PoliticalPartyDonationRecord
	@Field("vat_included")
	private boolean isVatIncluded;
	@Field("customer_ico")
	private String customerIco;
	@Field("supplier_ico")
	private String supplierIco;
	

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

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
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

	public float getDonationValue() {
		return donationValue;
	}

	public void setDonationValue(float donationValue) {
		this.donationValue = donationValue;
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

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
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
