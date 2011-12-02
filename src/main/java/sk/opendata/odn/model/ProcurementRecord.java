package sk.opendata.odn.model;


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
}