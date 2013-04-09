package combustiblelemons.obsqr;

import android.provider.ContactsContract.CommonDataKinds;
import android.provider.ContactsContract.CommonDataKinds.StructuredPostal;
import android.util.Log;

public class VCardParser {
	/**	
	 * @param RawContents Pass here raw text from a card
	 */
	protected vCard parseContents(String RawContents){
		vCard card = new vCard();
		String mContents = RawContents;
		String[] contact = mContents.split("\n");
		
		for (int i=0; i < contact.length; i++){
			if(contact[i].startsWith("VERSION:")) { ;}
			// N: NAME;SURNAME;;;
			else if (contact[i].startsWith("N:")){				
				String[] name_line = contact[i].split(":");
				card.Name = name_line[1].replaceAll(";", " ");				
			} else if (contact[i].startsWith("FN:")) { 
				card.FName = contact[i].substring(3);
			} else if (contact[i].startsWith("TEL")){
				// TEL;PREF1;PREFN : TEL_NUMBER
				String[] tel_line = contact[i].split(":");		
				card.Phones.put((Integer)toPhoneType(tel_line[0].split(";")), 
										tel_line[1]);
			} else if (contact[i].startsWith("EMAIL")) {
				// EMAIL;PREF1;PREFN : EMAIL_ADDRESS
				String[] email_line = contact[i].split(":");
				card.Emails.put((Integer)toEmailType(email_line[0].split(";")),
										email_line[1]);		
				Log.v("Parser OBSQR", email_line[1]);
			} else if (contact[i].startsWith("ADR")) {				
				String[] address_line = contact[i].split(":");				
				card.Addresses.put(toAddressType(address_line[0].split(";")), address_line[1]);
			} else if (contact[i].startsWith("TITLE")){	
				String[] title_line = contact[i].split(":");
				card.Titles.add(title_line[1]);
			} else if (contact[i].startsWith("ORG")) {
				String[] mOrganisation = contact[i].split(":");				
				card.Organizations.add(mOrganisation[1].replace(";", " "));
			} else if (contact[i].startsWith("URL")) {				
				card.URLs.add(contact[i].substring(4));
			} else if (contact[i].startsWith("BDAY")) { card.BirthDay = contact[i].substring(5);}				
		}	
		return card;
	}		
	/**
	 * @param phonePrefs String[] of phone preferences taken raw from a *.vcf file.
	 * @return int value representing phone type from CommondDataKinds.Phone .
	 */
	private static int toPhoneType(String[] phonePrefs){
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
	private static int toEmailType(String[] EmailPrefs){
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
	private static int toAddressType(String[] AddressPrefs){
		int result = CommonDataKinds.StructuredPostal.TYPE_HOME;
		if (AddressPrefs.length > 1){
			for (String address : AddressPrefs){
				if (address.equalsIgnoreCase("WORK")){ result = StructuredPostal.TYPE_WORK;}
				else { result = StructuredPostal.TYPE_OTHER;}
			}
		} else { return result = StructuredPostal.TYPE_HOME;}
		return result;
	}	
}