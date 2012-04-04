package trikita.obsqr;

import java.util.ArrayList;
import java.util.HashMap;

import android.provider.ContactsContract.CommonDataKinds;
import android.provider.ContactsContract.CommonDataKinds.StructuredPostal;

public class VCardParser {
	private String FName;								// FN					:		SINGLE_TEXT_VALUE
	private String Name;								// N					:		PREF1,PREFN	
	private HashMap<Integer, String> Phones;			// TEL;CELL;WORK;HOME	:		SINGLE_TEXT_VALUE
	private HashMap<Integer, String> Emails;			// EMAIL;PREF1;PREFN 	:		EMAIL_ADDRESS 
	private HashMap<Integer, String[]> Addresses;		// ADR;PREF1,PREFN		:		ADDRESS ;;Street;City;State;Zipcode;Country
	private ArrayList<String> Titles;					// TITLE				:		SINGLE_TEXT_VALUE
	private ArrayList<String> Roles;					// ROLE					:		SINGLE_TEXT_VALUE
	private ArrayList<String> URLs;						// URL					:		SINGLE_TEXT_VALUE
	private ArrayList<String> Organizations;			// ORG					:		PREF1;PREF2
	private ArrayList<String> Notes;					// NOTE					:		SINGLE_TEXT_VALUE
	private String BirthDay;							// BDAY					:		SINGLE_TEXT_VALUE
	// -------------------------------------Less used -------------------------------------//
//	private String Rev;									// REV Combination of the calendar date and time of day of the last update to the vCard object	
//	private String Photo;								// BASE64 Encoding, 'fattens' the vCard code considerably.
	
	public HashMap<Integer, String> getPhones()			{ return this.Phones;}
	public HashMap<Integer, String> getEmails()			{ return this.Emails;}
	public HashMap<Integer, String[]> getAddresses()	{ return this.Addresses;}
	
	public String getFName()							{ return this.FName;}
	public String getName()								{ return this.Name;}	
	public ArrayList<String> getTitles()				{ return this.Titles;}
	public ArrayList<String> getRoles()					{ return this.Roles;}
	public ArrayList<String> getURLs()					{ return this.URLs;}
	public ArrayList<String> getNotes()					{ return this.Notes;}
	
	public ArrayList< String> getOrg() 					{ return this.Organizations;}
	public String getBirthDay()							{ return this.BirthDay;}
	
	/**	
	 * @param RawContents Pass here raw text from a vCard
	 */
	private void parseContents(String RawContents){
		this.Addresses = new HashMap<Integer, String[]>();
		this.Emails = new HashMap<Integer, String>();
		this.Phones = new HashMap<Integer, String>();
		this.Organizations  = new ArrayList<String>();
		this.Titles = new ArrayList<String>();
		this.Roles = new ArrayList<String>();
		this.Notes = new ArrayList<String>();
		String mContents = RawContents;
		String[] contact = mContents.split("\n");
		
		for (int i=0; i < contact.length; i++){
			if(contact[i].startsWith("VERSION:")){ ;}
			// N: NAME;SURNAME;;;
			else if (contact[i].startsWith("N:")){				
				String[] name_line = contact[i].split(":");
				this.Name = name_line[1].replaceAll(";", " ");				
			} else if (contact[i].startsWith("FN:")){ FName = contact[i].substring(3);}
			else if (contact[i].startsWith("TEL")){
				// TEL;PREF1;PREFN : TEL_NUMBER
				String[] tel_line = contact[i].split(":");		
				this.Phones.put((Integer)toPhoneType(tel_line[0].split(";")), tel_line[1]);
			} else if (contact[i].startsWith("EMAIL")){
				// EMAIL;PREF1;PREFN : EMAIL_ADDRESS
				String[] email_line = contact[i].split(":");
				this.Emails.put((Integer)toEmailType(email_line[0].split(";")), email_line[1]);				
			} else if (contact[i].startsWith("ADR")){				
				String[] address_line = contact[i].split(":");
				String[] address = new String[7];
				String[] address_split = address_line[1].split(";");
				int boundary = 7;
				if (address_split.length < 7)
					boundary = address_split.length;				
				for (int j=0; j<boundary; j++){
					address[j] = address_split[j];
				}
				this.Addresses.put(toAddressType(address_line[0].split(";")), address);
			} else if (contact[i].startsWith("TITLE")){	
				String[] title_line = contact[i].split(":");
				Titles.add(title_line[1]);
			} else if (contact[i].startsWith("ORG")) {
				String[] mOrganisation = contact[i].split(":");				
				this.Organizations.add(mOrganisation[1].replace(";", " "));
			} else if (contact[i].startsWith("URL")) {
				//String[] url_line = contact[i].split(":");
				/*
				 * Will fall flat on http://
				 */
				this.URLs.add(contact[i].substring(4));
			} else if (contact[i].startsWith("BDAY")) { this.BirthDay = contact[i].substring(5);}				
		}	
	}		
	/**
	 * @param phonePrefs String[] of phone preferences taken raw from a *.vcf file.
	 * @return int value representing phone type from CommondDataKinds.Phone .
	 */
	private int toPhoneType(String[] phonePrefs){
		int result = CommonDataKinds.Phone.TYPE_MAIN;
		if (phonePrefs.length == 1) { result = CommonDataKinds.Phone.TYPE_MOBILE;}
		if (phonePrefs.length == 2) { result = CommonDataKinds.Phone.TYPE_MAIN;}
		for (int i=0; i<phonePrefs.length; i++){			
			if (phonePrefs[i].equalsIgnoreCase("WORK")){
				for (int j=0; j<phonePrefs.length; j++){
					if (phonePrefs[j].equalsIgnoreCase("CELL")){ result = CommonDataKinds.Phone.TYPE_WORK_MOBILE;}
					else if (phonePrefs[j].equalsIgnoreCase("PAGER")) { result = CommonDataKinds.Phone.TYPE_WORK_PAGER;}
					else if (phonePrefs[j].equalsIgnoreCase("FAX")) { result = CommonDataKinds.Phone.TYPE_FAX_WORK;}
					else { return CommonDataKinds.Phone.TYPE_WORK;}
				}
			} else if (phonePrefs[i].equalsIgnoreCase("PREF")){
				for (int j=0; j<phonePrefs.length; j++){
					if (phonePrefs[j].equalsIgnoreCase("CELL")){ result = CommonDataKinds.Phone.TYPE_MOBILE;}
					else if (phonePrefs[j].equalsIgnoreCase("COMPANY")) { result = CommonDataKinds.Phone.TYPE_COMPANY_MAIN;}
					else if (phonePrefs[j].equalsIgnoreCase("ASSISTANT")) { result = CommonDataKinds.Phone.TYPE_ASSISTANT;}
				}
			} else if (phonePrefs[i].equalsIgnoreCase("HOME")){
				for (int j=0; j<phonePrefs.length; j++){
					if (phonePrefs[j].equalsIgnoreCase("FAX")){ result = CommonDataKinds.Phone.TYPE_FAX_HOME;}
					else { result = CommonDataKinds.Phone.TYPE_HOME;}
				}				
			} else if (phonePrefs[i].equalsIgnoreCase("ASSISTANT")) { result = CommonDataKinds.Phone.TYPE_ASSISTANT;
			} else if (phonePrefs[i].equalsIgnoreCase("FAX")) { result = CommonDataKinds.Phone.TYPE_FAX_WORK;
			} else if (phonePrefs[i].equalsIgnoreCase("CAR")) { result = CommonDataKinds.Phone.TYPE_CAR;}
		}
		
		return result;
	}

	/**
	 * @param phonePrefs String[] of email preferences taken raw from a *.vcf file.
	 * @return int value representing phone type from CommondDataKinds.Email. Default TYPE_WORK
	 */
	private int toEmailType(String[] EmailPrefs){
		int result = CommonDataKinds.Email.TYPE_WORK;
		if (EmailPrefs.length>1){
			for (String pref : EmailPrefs){
				if(pref.equalsIgnoreCase("HOME")){ result = CommonDataKinds.Email.TYPE_HOME;}
				else if (pref.equalsIgnoreCase("MOBILE") || pref.equalsIgnoreCase("CELL")) { result = CommonDataKinds.Email.TYPE_MOBILE;}
			}
		}
		return result;
	}

	/**
	 * @param phonePrefs String[] of address preferences taken raw from a *.vcf file.
	 * @return int value representing phone type from StructuredPostal. Default TYPE_HOME
	 */
	private int toAddressType(String[] AddressPrefs){
		int result = CommonDataKinds.StructuredPostal.TYPE_HOME;
		if (AddressPrefs.length > 1){
			for (String address : AddressPrefs){
				if (address.equalsIgnoreCase("WORK")){ result = StructuredPostal.TYPE_WORK;}
				else { result = StructuredPostal.TYPE_OTHER;}
			}
		} else { return result = StructuredPostal.TYPE_HOME;}
		return result;
	}
	public VCardParser(String RawContents) throws NullPointerException{
		parseContents(RawContents);
	}
}