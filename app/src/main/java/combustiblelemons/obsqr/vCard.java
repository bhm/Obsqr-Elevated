package combustiblelemons.obsqr;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

public class vCard {

	
	protected String FName;								// FN					:		SINGLE_TEXT_VALUE
	protected String Name;								// N					:		PREF1,PREFN	
	protected HashMap<Integer, String> Phones;			// TEL;CELL;WORK;HOME	:		SINGLE_TEXT_VALUE
	protected HashMap<Integer, String> Emails;			// EMAIL;PREF1;PREFN 	:		EMAIL_ADDRESS 
	protected HashMap<Integer, String> Addresses;		// ADR;PREF1,PREFN		:		ADDRESS ;;Street;City;State;Zipcode;Country
	protected ArrayList<String> Titles;					// TITLE				:		SINGLE_TEXT_VALUE
	protected ArrayList<String> Roles;					// ROLE					:		SINGLE_TEXT_VALUE
	protected ArrayList<String> URLs;					// URL					:		SINGLE_TEXT_VALUE
	protected ArrayList<String> Organizations;			// ORG					:		PREF1;PREF2
	protected ArrayList<String> Notes;					// NOTE					:		SINGLE_TEXT_VALUE
	protected String BirthDay;							// BDAY					:		SINGLE_TEXT_VALUE
	
	public vCard() {
		Addresses = new HashMap<Integer, String>();
		Emails = new HashMap<Integer, String>();
		Phones = new HashMap<Integer, String>();
		Organizations  = new ArrayList<String>();
		Titles = new ArrayList<String>();
		Roles = new ArrayList<String>();
		Notes = new ArrayList<String>();
		URLs = new ArrayList<String>();
	}
	
	@SuppressWarnings("unchecked")
	protected String first(Object from) {
		String v = null;
		if (from.getClass() == HashMap.class) {
			for (Entry<Integer, String> e : ((HashMap<Integer, String>) from).entrySet()) {
				v = e.getValue();
				break;
			}
		} else if (from.getClass() == ArrayList.class) {			
			Iterator<String> _i = ((ArrayList<String>) from).iterator();
			if (_i.hasNext()) { v = _i.next(); }
		}
		return v;
	}
	protected int type(HashMap<Integer, String> from, String value) {
		int i = -1;		
		for (Entry<Integer, String> e : from.entrySet()) {
			if (e.getValue().equals(value)) i = e.getKey();
		}
		return i;
	}	
}
