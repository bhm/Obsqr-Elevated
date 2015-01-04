package combustiblelemons.obsqr;

import android.provider.ContactsContract.CommonDataKinds;
import android.provider.ContactsContract.CommonDataKinds.StructuredPostal;
import android.util.Log;

public class VCardParser {
    /**
     * @param phonePrefs String[] of phone preferences taken raw from a *.vcf file.
     * @return int value representing phone type from CommondDataKinds.Phone .
     */
    private static int toPhoneType(String[] phonePrefs) {
        int result = CommonDataKinds.Phone.TYPE_MAIN;
        if (phonePrefs.length == 1) {
            return CommonDataKinds.Phone.TYPE_MOBILE;
        } else if (phonePrefs.length == 2) {
            return CommonDataKinds.Phone.TYPE_MAIN;
        } else {
            for (int i = 0; i < phonePrefs.length; i++) {
                if ("WORK".equalsIgnoreCase(phonePrefs[i])) {
                    for (int j = 0; j < phonePrefs.length; j++) {
                        if ("CELL".equalsIgnoreCase(phonePrefs[j])) {
                            result = CommonDataKinds.Phone.TYPE_WORK_MOBILE;
                        } else if ("PAGER".equalsIgnoreCase(phonePrefs[j])) {
                            result = CommonDataKinds.Phone.TYPE_WORK_PAGER;
                        } else if ("FAX".equalsIgnoreCase(phonePrefs[j])) {
                            result = CommonDataKinds.Phone.TYPE_FAX_WORK;
                        } else {
                            return CommonDataKinds.Phone.TYPE_WORK;
                        }
                    }
                } else if ("PREF".equalsIgnoreCase(phonePrefs[i])) {
                    for (int j = 0; j < phonePrefs.length; j++) {
                        if ("CELL".equalsIgnoreCase(phonePrefs[j])) {
                            result = CommonDataKinds.Phone.TYPE_MOBILE;
                        } else if ("COMPANY".equalsIgnoreCase(phonePrefs[j])) {
                            result = CommonDataKinds.Phone.TYPE_COMPANY_MAIN;
                        } else if ("ASSISTANT".equalsIgnoreCase(phonePrefs[j])) {
                            result = CommonDataKinds.Phone.TYPE_ASSISTANT;
                        }
                    }
                } else if ("HOME".equalsIgnoreCase(phonePrefs[i])) {
                    for (int j = 0; j < phonePrefs.length; j++) {
                        if ("FAX".equalsIgnoreCase(phonePrefs[j])) {
                            result = CommonDataKinds.Phone.TYPE_FAX_HOME;
                        } else {
                            result = CommonDataKinds.Phone.TYPE_HOME;
                        }
                    }
                } else if ("ASSISTANT".equalsIgnoreCase(phonePrefs[i])) {
                    result = CommonDataKinds.Phone.TYPE_ASSISTANT;
                } else if ("FAX".equalsIgnoreCase(phonePrefs[i])) {
                    result = CommonDataKinds.Phone.TYPE_FAX_WORK;
                } else if ("CAR".equalsIgnoreCase(phonePrefs[i])) {
                    result = CommonDataKinds.Phone.TYPE_CAR;
                }
            }
        }
        return result;
    }

    /**
     * @param phonePrefs String[] of email preferences taken raw from a *.vcf file.
     * @return int value representing phone type from CommondDataKinds.Email. Default TYPE_WORK
     */
    private static int toEmailType(String[] EmailPrefs) {
        int result = CommonDataKinds.Email.TYPE_WORK;
        if (EmailPrefs.length > 1) {
            for (String pref : EmailPrefs) {
                if ("HOME".equalsIgnoreCase(pref)) {
                    result = CommonDataKinds.Email.TYPE_HOME;
                } else if ("MOBILE".equalsIgnoreCase(pref) || "CELL".equalsIgnoreCase(pref)) {
                    result = CommonDataKinds.Email.TYPE_MOBILE;
                }
            }
        }
        return result;
    }

    /**
     * @param phonePrefs String[] of address preferences taken raw from a *.vcf file.
     * @return int value representing phone type from StructuredPostal. Default TYPE_HOME
     */
    private static int toAddressType(String[] AddressPrefs) {
        if (AddressPrefs.length > 1) {
            for (String address : AddressPrefs) {
                if ("WORK".equalsIgnoreCase(address)) {
                    return StructuredPostal.TYPE_WORK;
                } else {
                    return StructuredPostal.TYPE_OTHER;
                }
            }
        }
        return StructuredPostal.TYPE_HOME;
    }

    /**
     * @param RawContents Pass here raw text from a card
     */
    protected vCard parseContents(String RawContents) {
        vCard card = new vCard();
        String mContents = RawContents;
        String[] contact = mContents.split("\n");

        for (int i = 0; i < contact.length; i++) {
            if ("VERSION:".startsWith(contact[i])) {
                continue;
            }
            // N: NAME;SURNAME;;;
            if ("N:".startsWith(contact[i])) {
                String[] name_line = contact[i].split(":");
                card.Name = name_line[1].replaceAll(";", " ");
            } else if ("FN:".startsWith(contact[i])) {
                card.FName = contact[i].substring(3);
            } else if ("TEL".startsWith(contact[i])) {
                // TEL;PREF1;PREFN : TEL_NUMBER
                String[] tel_line = contact[i].split(":");
                card.mPhones.put(toPhoneType(tel_line[0].split(";")), tel_line[1]);
            } else if ("EMAIL".startsWith(contact[i])) {
                // EMAIL;PREF1;PREFN : EMAIL_ADDRESS
                String[] email_line = contact[i].split(":");
                card.mEmails.put(toEmailType(email_line[0].split(";")), email_line[1]);
                Log.v("Parser OBSQR", email_line[1]);
            } else if ("ADR".startsWith(contact[i])) {
                String[] address_line = contact[i].split(":");
                card.mAddresses.put(toAddressType(address_line[0].split(";")), address_line[1]);
            } else if ("TITLE".startsWith(contact[i])) {
                String[] title_line = contact[i].split(":");
                card.mTitles.add(title_line[1]);
            } else if ("ORG".startsWith(contact[i])) {
                String[] mOrganisation = contact[i].split(":");
                card.Organizations.add(mOrganisation[1].replace(";", " "));
            } else if ("URL".startsWith(contact[i])) {
                card.URLs.add(contact[i].substring(4));
            } else if ("BDAY".startsWith(contact[i])) {
                card.BirthDay = contact[i].substring(5);
            }
        }
        return card;
    }

    public static enum PhoneType {
        WORK, CELL, PAGER, FAX, ASSISTANT, CAR, HOME;
    }
}