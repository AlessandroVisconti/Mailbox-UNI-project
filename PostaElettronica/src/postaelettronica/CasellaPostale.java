package postaelettronica;

import java.util.ArrayList;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class CasellaPostale {

    private ObservableList<Email> emails = FXCollections.observableArrayList();

    public CasellaPostale() {
    }

    public boolean add(Email email) {
        return emails.add(email);

    }

    public Email get(int index) {
        try {
            return emails.get(index);
        } catch (IndexOutOfBoundsException ex) {
            return null;
        }

    }

    public void clear() {
        emails.clear();
    }

    public boolean addAll(ArrayList<Email> email) {
        boolean res = true;
        for (Email e : email) {
            res = res && add(e);
        }
        return res;
    }

    public boolean remove(int index) {
        try {
            emails.remove(index);
            return true;
        } catch (IndexOutOfBoundsException ex) {
            return false;
        }
    }

    public Integer getId(int index) {
        try {
            Email em = emails.get(index);
            return em.getID();
        } catch (IndexOutOfBoundsException ex) {
            return -1;
        }
    }

    public ObservableList<Email> getEmails() {
        return emails;
    }
}
