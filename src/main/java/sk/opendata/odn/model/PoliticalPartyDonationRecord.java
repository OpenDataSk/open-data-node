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


public class PoliticalPartyDonationRecord {
	private String id;
	private String donorName;
	private String donorSurname;
	private String donorTitle;
	private String donorCompany;
	private String donorIco;
	private float donationValue;
	private Currency donationCurrency;
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
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
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
	
}