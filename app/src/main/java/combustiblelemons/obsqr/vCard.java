package combustiblelemons.obsqr;

import android.text.TextUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class vCard {


    protected String               FName;               // FN					:		SINGLE_TEXT_VALUE
    protected String               Name;                // N					:		PREF1,PREFN
    protected Map<Integer, String> mPhones;              // TEL;CELL;WORK;HOME	:		SINGLE_TEXT_VALUE
    protected Map<Integer, String> mEmails;              // EMAIL;PREF1;PREFN 	:		EMAIL_ADDRESS
    protected Map<Integer, String> mAddresses;           // ADR;PREF1,PREFN		:		ADDRESS ;;Street;City;State;Zipcode;Country
    protected List<String>         mTitles;              // TITLE				:		SINGLE_TEXT_VALUE
    protected List<String>         mRoles;               // ROLE					:		SINGLE_TEXT_VALUE
    protected List<String>         URLs;                // URL					:		SINGLE_TEXT_VALUE
    protected List<String>         Organizations;       // ORG					:		PREF1;PREF2
    protected List<String>         Notes;               // NOTE					:		SINGLE_TEXT_VALUE
    protected String               BirthDay;            // BDAY					:		SINGLE_TEXT_VALUE

    public vCard() {
        mAddresses = new HashMap<Integer, String>();
        mEmails = new HashMap<Integer, String>();
        mPhones = new HashMap<Integer, String>();
        Organizations = new ArrayList<String>();
        mTitles = new ArrayList<String>();
        mRoles = new ArrayList<String>();
        Notes = new ArrayList<String>();
        URLs = new ArrayList<String>();
    }

    @SuppressWarnings("unchecked")
    protected String first(Map<Integer, String> from) {
        for (Entry<Integer, String> e : from.entrySet()) {
            if (!TextUtils.isEmpty(e.getValue())) {
                return e.getValue();
            }
        }
        return null;
    }

    public String first(Collection<String> from) {
        Iterator<String> _i = from.iterator();
        if (_i.hasNext()) {
            return _i.next();
        }
        return null;
    }

    protected int type(HashMap<Integer, String> from, String value) {
        int i = -1;
        for (Entry<Integer, String> e : from.entrySet()) {
            if (e.getValue().equals(value)) i = e.getKey();
        }
        return i;
    }
}
