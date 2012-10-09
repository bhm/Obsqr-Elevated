package trikita.obsqr;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.AlertDialog;
import android.content.ContentProviderOperation;
import android.content.Context;
import android.content.DialogInterface;
import android.content.OperationApplicationException;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds;
import android.provider.ContactsContract.CommonDataKinds.Organization;
import android.provider.ContactsContract.CommonDataKinds.StructuredPostal;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.RawContacts;
import android.util.Log;
import android.widget.Toast;

public class ContactManager {
	private final static String tag = "Obsqr : ContactManager";
	private Context context;	
	private vCard card;	
	
	private ContactManager(Context ctx){
		this.context = ctx;
	}
	
	public ContactManager(Context context, vCard card) {
		this(context);
		this.card = card;
	}
	
	public void setVCard(vCard card) {
		this.card = card;
	}
	
	public boolean showDialog() {
		AccountManager accountManager = AccountManager.get(context);
		Account[] accounts = accountManager.getAccountsByType(null);
		/*
		 * TODO Retrive proper account types for contacts!
		 */
		final String[] accountNames = new String[accounts.length];
		final String[] accountTypes = new String[accounts.length];
		int _i = 0;
		for (Account _a : accounts) {
			accountNames[_i] = _a.name;
			accountTypes[_i] = _a.type;
			Log.v(tag, "Accounts: " 
					+ _a.type + "\t\t\t" 
					+ _a.name + "\n");
			_i++;
		}
		final ArrayList<ContentProviderOperation> contact = new ArrayList<ContentProviderOperation>();
		AlertDialog.Builder dialog = new AlertDialog.Builder(context);
		dialog.setTitle(context.getResources().getString(R.string.choose_account));
		dialog.setItems(accountNames,  new DialogInterface.OnClickListener() {			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				try {
					addContact(card, contact, accountTypes[which], accountNames[which]);
					context.getContentResolver().applyBatch(ContactsContract.AUTHORITY, contact);
					Log.d(tag, "Added :" + contact.toString());
					Toast.makeText(context,  	card.Name 
												+ " "
												+ context.getResources().getString(R.string.toast_msg_added_to)
												+ " " 
												+ accountNames[which],
												Toast.LENGTH_LONG).show();
				} catch (RemoteException e) {
					Log.d(tag, "While adding " + card.Name.toString());
					e.printStackTrace();
				} catch (OperationApplicationException e) {
					Log.d(tag, "While adding " + card.Name.toString());
					e.printStackTrace();
				}					
			}
		});
		AlertDialog alert = dialog.create();
		alert.show();
		return false;
	}
	
	private boolean addContact(vCard card, final ArrayList<ContentProviderOperation> contact, String type, String name) {
		setupContact(contact, type, name);
		addName(contact, card.Name);
		addPhones(contact, card.Phones);
		addEmails(contact, card.Emails);
		addAdrresses(contact, card.Addresses);
		addOrganisations(contact, card.Organizations);
		addRoles(contact, card.Roles);
		addTitles(contact, card.Titles);
		addNotes(contact, card.Notes);
		return true;
	}
	
	private boolean setupContact(ArrayList<ContentProviderOperation> contact, String type, String name) {
		contact.add(ContentProviderOperation.newUpdate(RawContacts.CONTENT_URI)
				.withValue(RawContacts.ACCOUNT_TYPE, type)
				.withValue(RawContacts.ACCOUNT_NAME, name).build());
		return false;
	}
	
	private boolean addPhones(ArrayList<ContentProviderOperation> contact, HashMap<Integer, String> phones) {
		if (!phones.isEmpty()) {
			for (Map.Entry<Integer, String> phone : phones.entrySet()) {
				int type = (int) phone.getKey();
				contact.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
		                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
		                .withValue(ContactsContract.Data.MIMETYPE, CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
		                .withValue(CommonDataKinds.Phone.TYPE, type )
		                .withValue(CommonDataKinds.Phone.NUMBER, phone.getValue().toString())		               
		                .build());
			}				
		}
		return true;
	}
	
	private boolean addEmails(ArrayList<ContentProviderOperation> contact, HashMap<Integer, String> emails) {
		if (!emails.isEmpty()) {
			for (Map.Entry<Integer, String> email : emails.entrySet()) {
				int type = (int) email.getKey();
				contact.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
		                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
		                .withValue(ContactsContract.Data.MIMETYPE, CommonDataKinds.Email.CONTENT_ITEM_TYPE)
		                .withValue(CommonDataKinds.Email.TYPE, type )
		                .withValue(CommonDataKinds.Email.ADDRESS, email.getValue().toString())		               
		                .build());
				}
		}
		return true;
	}
	
	public boolean addName(ArrayList<ContentProviderOperation> contact, String value) {
		if (!value.equals(null)){			
			contact.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
	                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
	                .withValue(ContactsContract.Data.MIMETYPE,
	                        ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
	                .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, value)
	                .build());
			contact.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
	                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
	                .withValue(ContactsContract.Data.MIMETYPE,
	                        CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
	                .withValue(CommonDataKinds.StructuredName.GIVEN_NAME, (value.split(" "))[0])
	                .build());
			contact.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
	                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
	                .withValue(ContactsContract.Data.MIMETYPE,
	                        CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
	                .withValue(CommonDataKinds.StructuredName.FAMILY_NAME, (value.split(" "))[1])
	                .build());
		}
		return true;
	}
	
	private boolean addAdrresses(ArrayList<ContentProviderOperation> contact, HashMap<Integer, String> addresses) {
		if (!addresses.isEmpty()) {								
			String street = "";
			String city = "";
			String zipcode = "";
			String state = "";
			String country = "";
			for (Map.Entry<Integer, String> address : addresses.entrySet()) {
				String[] _address = address.getValue().split(";"); 
				for (int i=0; i<_address.length; i++) {						
					street = _address[2];
					city = _address[3];
					state = _address[4];
					zipcode = _address[5];
					country = _address[6];
				}						
				Log.d(tag, " Street: " + street + " City: " + city + " State: " + state + " Zipcode: " + zipcode + " Country: " + country);
				contact.add(ContentProviderOperation.newInsert(Data.CONTENT_URI)
						.withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
						.withValue(Data.MIMETYPE, StructuredPostal.CONTENT_ITEM_TYPE)
						.withValue(StructuredPostal.TYPE, address.getKey())
						.withValue(StructuredPostal.STREET, street)
						.withValue(Data.MIMETYPE, StructuredPostal.CONTENT_ITEM_TYPE)									
						.withValue(StructuredPostal.CITY, city)
						.withValue(Data.MIMETYPE, StructuredPostal.CONTENT_ITEM_TYPE)								
						.withValue(StructuredPostal.POSTCODE, zipcode)
						.withValue(Data.MIMETYPE, StructuredPostal.CONTENT_ITEM_TYPE)
						.withValue(StructuredPostal.REGION, state)
						.withValue(Data.MIMETYPE, StructuredPostal.CONTENT_ITEM_TYPE)
						.withValue(StructuredPostal.COUNTRY, country)
						.build());
			}
		}
		return true;
	}
	
	private boolean addOrganisations(ArrayList<ContentProviderOperation> contact, ArrayList<String> organisations) {
		if (!organisations.isEmpty()) {
			Iterator<String> iterator = organisations.iterator();
			while(iterator.hasNext()){
				String _company = iterator.next();
				contact.add(ContentProviderOperation.newInsert(Data.CONTENT_URI)
						.withValueBackReference(Data.RAW_CONTACT_ID, 0)
						.withValue(Data.MIMETYPE, ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE)
						.withValue(Organization.COMPANY, _company)
						.build());
			}
		}
		return true;
	}
	
	private boolean addRoles(ArrayList<ContentProviderOperation> contact, ArrayList<String> roles) {
		if (!roles.isEmpty()) {
			Iterator<String> iterator = roles.iterator();
			while(iterator.hasNext()){
				String _role = iterator.next();
				contact.add(ContentProviderOperation.newInsert(Data.CONTENT_URI)
						.withValueBackReference(Data.RAW_CONTACT_ID, 0)
						.withValue(Data.MIMETYPE, ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE)
						.withValue(Organization.JOB_DESCRIPTION, _role)
						.build());	
			}
		}
		return true;
	}
	
	private boolean addTitles(ArrayList<ContentProviderOperation> contact, ArrayList<String> titles) {
		if (!titles.isEmpty()) {
			Iterator<String> iterator = titles.iterator();
			while(iterator.hasNext()){
				String _title = iterator.next();
				contact.add(ContentProviderOperation.newInsert(Data.CONTENT_URI)
						.withValueBackReference(Data.RAW_CONTACT_ID, 0)
						.withValue(Data.MIMETYPE, ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE)
						.withValue(Organization.TITLE, _title)
						.build());	
			}		
		}
		return true;
	}
	
	private boolean addNotes(ArrayList<ContentProviderOperation> contact, ArrayList<String> notes) {
		if (!notes.isEmpty()) {
			Iterator<String> iterator = notes.iterator();
			while(iterator.hasNext()){
				String _note = iterator.next();
				contact.add(ContentProviderOperation.newInsert(Data.CONTENT_URI)
						.withValueBackReference(Data.RAW_CONTACT_ID, 0)
						.withValue(Data.MIMETYPE, ContactsContract.CommonDataKinds.Note.CONTENT_ITEM_TYPE)
						.withValue(ContactsContract.CommonDataKinds.Note.NOTE, _note)
						.build());
			}
		}
		return true;
	}
}
