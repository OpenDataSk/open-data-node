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

package sk.opendata.odn.model;

import java.util.Vector;


public class ProcurementRecord {
	private String id;
	private String note;
	private String year;
	private String bulletinId;
	private String procurementId;
	private String procurementSubject;
	private float price;
	private Currency currency;
	private boolean isVatIncluded;
	private String customerIco;
	private String supplierIco;
	private Vector<String> scrapNotes = new Vector<String>();
	// TODO: rest of the items: customer company address, ... which was not
	// deemed useful now BUT might become handy later on (like in
	// crowdsourcing, having multiple items will help determine and correct
	// mistakes etc.)
	
	// TODO: for the purposes of "common use case" try adding the value of
	// 'pricateInEurWithoutVAT' calculated during harvesting from 'price',
	// 'currency' and 'isVatIncluded' so as to avoid having to complicate the
	// search queries with stuff like
	// 'if EUR then price > 10; if SKK then price > 300' 
	
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getNote() {
		return note;
	}
	public void setNote(String note) {
		this.note = note;
	}
	public String getYear() {
		return year;
	}
	public void setYear(String year) {
		this.year = year;
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
	
	public Vector<String> getScrapNotes() {
		return this.scrapNotes;
	}
	/**
	 * Append given scrap note to the list (Vector) of scrap notes.
	 * @param scrapNote scrap note to add
	 * @return what 'Vector.add()' returned
	 * @see java.util.Vector.add()
	 */
	public boolean addScrapNote(String scrapNote) {
		return this.scrapNotes.add(scrapNote);
	}
}