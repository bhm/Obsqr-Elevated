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
import android.provider.ContactsContract.CommonDataKinds.Organization;
import android.provider.ContactsContract.CommonDataKinds.StructuredPostal;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.RawContacts;
import android.util.Log;
import android.widget.Toast;

public class ContactManager {
	private final static String tag = "Obsqr : ContactManager";
	private Context Context;
	
	private String Name  = null;
	private HashMap<Integer, String> Phones  = null;
	private HashMap<Integer, String> Emails = null;
	private HashMap<Integer, String[]> Addresses  = null;
	private ArrayList<String> Organisations = null;
	private ArrayList<String> Roles = null;
	private ArrayList<String> Titles  = null;
	private ArrayList<String> Notes  = null;

	/**
	 * @param name Set name for a contact
	 */
	public void setName(String name) {
		this.Name = name;
	}

	/**
	 * @param phones Set phones for a contact where Integer belongs to CommonDataKinds.Phone
	 */
	public void setPhones(HashMap<Integer, String> phones) {
		this.Phones = phones;
	}

	/**
	 * @param addresses the addresses to set
	 */
	public void setAddresses(HashMap<Integer, String[]> addresses) {
		this.Addresses = addresses;
	}

	/**
	 * @param emails Set email addresses for a contact where Integer in HashMap<Integer, String> belongs to CommonDataKinds.Phone and describes and String to email address
	 */
	public void setEmails(HashMap<Integer, String> emails) {
		this.Emails = emails;
	}

	/**
	 * @param organisations Set organizations for a contact.
	 */
	public void setOrganisations(ArrayList<String> organisations) {
		this.Organisations = organisations;
	}

	/**
	 * @param role the role to set
	 */
	public void setRoles(ArrayList<String> roles) {
		this.Roles = roles;
	}

	/**
	 * @param title the title to set
	 */
	public void setTitles(ArrayList<String> titles) {
		this.Titles = titles;
	}
	
	/**
	 * @param title the title to set
	 */
	public void setNotes(ArrayList<String> notes) {
		this.Notes = notes;
	}
	
	
	public ContactManager(Context ctx){
		this.Context = ctx;
	}
	
	/**
	 * 
	 */

	public void addContact(){
		AccountManager am = AccountManager.get(Context);			
		Account[] accounts =  am.getAccountsByType(null);
		final String[] names = new String[accounts.length];
		final String[] types = new String[accounts.length];
		int iterator=0;
		for (Account a : accounts){				
			names[iterator] = a.name;
			types[iterator] = a.type;
			iterator++;
			Log.d(tag, " ACCOUNTS: " + a.type + "\t\t\t" + a.name);
		}					
		AlertDialog.Builder buildDialog = new AlertDialog.Builder(Context);
		buildDialog.setTitle("Choose an account");
		buildDialog.setItems(names, new DialogInterface.OnClickListener() {				
			@Override
			public void onClick(DialogInterface dialog, int which){	
				ArrayList<ContentProviderOperation> contact = new ArrayList<ContentProviderOperation>();
				//int rawInsertIndex = contact.size();
				if (!Name.equals(null)){
					contact.add(ContentProviderOperation.newInsert(RawContacts.CONTENT_URI)
							.withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, types[which])
							.withValue(ContactsContract.RawContacts.ACCOUNT_NAME, names[which])
							.build());
					contact.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
			                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
			                .withValue(ContactsContract.Data.MIMETYPE,
			                        ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
			                .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, Name)
			                .build());
					contact.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
			                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
			                .withValue(ContactsContract.Data.MIMETYPE,
			                        ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
			                .withValue(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME, (Name.split(" "))[0])
			                .build());
					contact.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
			                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
			                .withValue(ContactsContract.Data.MIMETYPE,
			                        ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
			                .withValue(ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME, (Name.split(" "))[1])
			                .build());
				}
				if (Phones.size() > 0 || Phones != null){
					for (Map.Entry<Integer, String> phone : Phones.entrySet()){
						int type = (int) phone.getKey();
						contact.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
				                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
				                .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
				                .withValue(ContactsContract.CommonDataKinds.Phone.TYPE, type )
				                .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, phone.getValue().toString())		               
				                .build());
					}						
				}					
				if (Emails.size() > 0 || Emails != null){
					for (Map.Entry<Integer, String> email : Emails.entrySet()){
						int type = (int) email.getKey();
						contact.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
				                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
				                .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE)
				                .withValue(ContactsContract.CommonDataKinds.Email.TYPE, type )
				                .withValue(ContactsContract.CommonDataKinds.Email.ADDRESS, email.getValue().toString())		               
				                .build());
						}
				}
				if (Addresses.size() > 0 || Addresses != null){								
					String street = "";
					String city = "";
					String zipcode = "";
					String state = "";
					String country = "";
					for (Map.Entry<Integer, String[]> address : Addresses.entrySet()){
						String[] _address = address.getValue(); 
						for (int i=0; i<_address.length; i++){
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
				if(Organisations.size() > 0 || Organisations != null){
					Iterator<String> iterator = Organisations.iterator();
					while(iterator.hasNext()){
						String _company = iterator.next();
						contact.add(ContentProviderOperation.newInsert(Data.CONTENT_URI)
								.withValueBackReference(Data.RAW_CONTACT_ID, 0)
								.withValue(Data.MIMETYPE, ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE)
								.withValue(Organization.COMPANY, _company)
								.build());
					}
				}
				if(Roles.size() > 0 || Roles != null){
					Iterator<String> iterator = Roles.iterator();
					while(iterator.hasNext()){
						String _role = iterator.next();
						contact.add(ContentProviderOperation.newInsert(Data.CONTENT_URI)
								.withValueBackReference(Data.RAW_CONTACT_ID, 0)
								.withValue(Data.MIMETYPE, ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE)
								.withValue(Organization.JOB_DESCRIPTION, _role)
								.build());	
					}
					
				}		
				if(Titles.size() > 0 || Titles != null){
					Iterator<String> iterator = Titles.iterator();
					while(iterator.hasNext()){
						String _title = iterator.next();
						contact.add(ContentProviderOperation.newInsert(Data.CONTENT_URI)
								.withValueBackReference(Data.RAW_CONTACT_ID, 0)
								.withValue(Data.MIMETYPE, ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE)
								.withValue(Organization.TITLE, _title)
								.build());	
					}					
				}
				if (Notes.size() > 0 || Notes != null){
					Iterator<String> iterator = Notes.iterator();
					while(iterator.hasNext()){
						String _note = iterator.next();
						contact.add(ContentProviderOperation.newInsert(Data.CONTENT_URI)
								.withValueBackReference(Data.RAW_CONTACT_ID, 0)
								.withValue(Data.MIMETYPE, ContactsContract.CommonDataKinds.Note.CONTENT_ITEM_TYPE)
								.withValue(ContactsContract.CommonDataKinds.Note.NOTE, _note)
								.build());
					}
				}
				
				try {
					Context.getContentResolver().applyBatch(ContactsContract.AUTHORITY, contact);
					Log.d(tag, "Added :" + contact.toString());
					Toast.makeText(Context,  	Name 
												+ " "
												+ Context.getResources().getString(R.string.toast_msg_added_to)
												+ " " 
												+ names[which],
												Toast.LENGTH_LONG).show();
				} catch (RemoteException e) {
					Log.d(tag, "While adding " + contact.toString());
					e.printStackTrace();
				} catch (OperationApplicationException e) {
					Log.d(tag, "While adding " + contact.toString());
					e.printStackTrace();
				}					
			}
		});
		AlertDialog alert = buildDialog.create();
		alert.show();
	}
}
