package sk.opendata.odn.utils.tests;

import sk.opendata.odn.model.Currency;

/**
 * This class contains some common test data for Procurement used in several
 * tests.
 */
public class ProcurementTestData {

	public final static String TEST_ID = "procurement_0";
	public final static String TEST_PROCUREMENT_ID = "06281 - VUP";
	public final static String TEST_PROCUREMENT_SUBJECT = "Rekonštrukcia ZŠ v obci Horná Dolná";
	public final static String TEST_YEAR = "2012";
	public final static String TEST_CUSTOMER_ICO = "17321204";
	public final static String TEST_SUPPLIER_ICO = "40212371";
	public final static String TEST_PRICE_STRING = "1,25 ";
	public final static float TEST_PRICE = 1.25f;
	public final static String TEST_INVALID_PRICE = "L.25";
	public final static String TEST_PRICE_ISSUE_2 = "28 000 000,00 ";
	public final static Currency TEST_CURRENCY = Currency.EUR;
	public final static String TEST_EMPTY_STRING = "";
	
}
