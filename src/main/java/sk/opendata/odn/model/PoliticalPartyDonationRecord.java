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

import java.util.Date;


/**
 * For now, this is a direct 1-1 mapping of DataNest's donation item
 * into a java bean with some items omitted.
 * 
 * TODO: To allow better matching of people, companies and political parties,
 * it would be nice to introduce "person" and "political party" records into the
 * Open Data Node. From here, we will then only "link" (via id) to those records.
 * 
 * That way, we can:
 * 
 * a) have only one data set for people with content coming from organization data sets
 *    (owners, ...), political parties data sets (party leaders, ...) etc. (with only
 *    "minor problem" at hand: properly detecting and merging information about same
 *    person from multiple sources into one record)
 * b) have better search: Say we have only one SOLR index for all data sets in
 *    Open Data Node. By putting less duplicates into the system, the index is will
 *    be smaller thus easier to search.
 * c) have better ability to detect relations: With data about people duplicated
 *    between data sets, each having its own copy, to find a relation means finding
 *    same name, same address, etc. Now if we store only references using IDs
 *    (and assuming we properly solved the "minor problem" mentioned above, i.e.
 *    merging of data about same entity) it would become easy ID lookup.
 */
public class PoliticalPartyDonationRecord extends AbstractRecord {
	private String datanestId;
	private String donorName;
	private String donorSurname;
	private String donorTitle;
	private String name;	// donor company
	private String ico;		// donor ICO
	private float donationValue;
	private Currency currency;
	private String donorAddress;
	private String donorPsc;
	private String donorCity;
	private String recipientParty;
	private String year;
	private Date acceptDate;
	private String note;

	// TODO: rest of the items: datum prijatia, ... which was not
	// deemed useful now BUT might become handy later on (like in
	// crowdsourcing, having multiple items will help determine and correct
	// mistakes etc.)
	
	// TODO: for the purposes of "common use case" try adding the value of
	// 'pricateInEur' calculated during harvesting from 'price' and
	// 'currency' so as to avoid having to complicate the
	// search queries with stuff like
	// 'if EUR then price > 10; if SKK then price > 300'
	
	public String getDatanestId() {
		return datanestId;
	}
	public void setDatanestId(String datanestId) {
		this.datanestId = datanestId;
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
	public String getDonorTitle() {
		return donorTitle;
	}
	public void setDonorTitle(String donorTitle) {
		this.donorTitle = donorTitle;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getIco() {
		return ico;
	}
	public void setIco(String ico) {
		this.ico = ico;
	}
	public float getDonationValue() {
		return donationValue;
	}
	public void setDonationValue(float donationValue) {
		this.donationValue = donationValue;
	}
	public Currency getCurrency() {
		return currency;
	}
	public void setCurrency(Currency currency) {
		this.currency = currency;
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
	
}