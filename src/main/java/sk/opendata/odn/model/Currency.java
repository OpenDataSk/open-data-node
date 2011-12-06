package sk.opendata.odn.model;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

/**
 * Enumeration used to work with currencies intended for following use cases:
 * 
 * <ul>
 * <li>parsing various representations if a currency name(s) into enum value</li>
 * <li>formatting enum value into String</li>
 * <li>the usual Java enum use cases</li>
 * </ul>
 */
public enum Currency {
	CZK("CZK", "Kč"),
	EUR("EUR", "€"),
	SKK("SKK", "Sk"),
	USD("USD", "$");

	private String currencyCode;	// ISO 4217 - see http://en.wikipedia.org/wiki/ISO_4217
	private String currencySign;	// see http://en.wikipedia.org/wiki/Currency_sign

	private final static Map<String, Currency> lookup = new HashMap<String, Currency>();
	
	
	static {
		for (Currency currency : EnumSet.allOf(Currency.class)) {
			lookup.put(currency.getCurrencyCode(), currency);
			lookup.put(currency.getCurrencySign(), currency);
		}
		// plus some "non-standard" strings to cover what we are finding in data
		// we are harvesting
		lookup.put("Eur", EUR);
	}
	
	private Currency(String currencyCode, String currencySign) {
		this.currencyCode = currencyCode;
		this.currencySign = currencySign;
	}
	
	public String getCurrencyCode() {
		return currencyCode;
	}
	
	public String getCurrencySign() {
		return currencySign;
	}
	
	/**
	 * Parse given string as currency (code, sign, ...) and return the enum
	 * value which corresponds to it.
	 * 
	 * @param currency
	 *            currency code, sign or other form of it's identification
	 * @return enum value corresponding to given currency string
	 * @throws IllegalArgumentException
	 *             when given currency string is not known
	 */
	public static Currency parse(String currency) throws IllegalArgumentException {
		Currency result = lookup.get(currency);
		
		if (result == null)
			throw new IllegalArgumentException(currency);
		
		return result;
	}
}
