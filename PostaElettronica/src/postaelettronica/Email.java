package postaelettronica;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Email {

    //ID
    private SimpleIntegerProperty id;

    public final int getID() {
        return idProperty().get();
    }

    public final void setID(int id) {
        idProperty().set(id);
    }

    public IntegerProperty idProperty() {
        if (id == null) {
            id = new SimpleIntegerProperty(this, "id");
        }
        return id;
    }
    //MITTENTE
    private SimpleStringProperty mittente;

    public final String getMittente() {
        return mittenteProperty().get();
    }

    public final void setMittente(String mittente) {
        mittenteProperty().set(mittente);
    }

    public StringProperty mittenteProperty() {
        if (mittente == null) {
            mittente = new SimpleStringProperty(this, "mittente");
        }
        return mittente;
    }
    //DESTINATARIO
    private SimpleStringProperty destinatario;

    public final String getDestinatario() {
        return destinatarioProperty().get();
    }

    public final void setDestinatario(String destinatario) {
        destinatarioProperty().set(destinatario);
    }

    public StringProperty destinatarioProperty() {
        if (destinatario == null) {
            destinatario = new SimpleStringProperty(this, "destinatario");
        }
        return destinatario;
    }
    //CC
    private SimpleStringProperty cc;

    public final String getCC() {
        return ccProperty().get();
    }

    public final void setCC(String cc) {
        ccProperty().set(cc);
    }

    public StringProperty ccProperty() {
        if (cc == null) {
            cc = new SimpleStringProperty(this, "cc");
        }
        return cc;
    }
    //ARGOMENTO
    private SimpleStringProperty argomento;

    public final String getArgomento() {
        return argomentoProperty().get();
    }

    public final void setArgomento(String argomento) {
        argomentoProperty().set(argomento);
    }

    public StringProperty argomentoProperty() {
        if (argomento == null) {
            argomento = new SimpleStringProperty(this, "argomento");
        }
        return argomento;
    }
    //MESSAGGIO
    private SimpleStringProperty messaggio;

    public final String getMessaggio() {
        return messaggioProperty().get();
    }

    public final void setMessaggio(String messaggio) {
        messaggioProperty().set(messaggio);
    }

    public StringProperty messaggioProperty() {
        if (messaggio == null) {
            messaggio = new SimpleStringProperty(this, "messaggio");
        }
        return messaggio;
    }
    //DATA
    private SimpleStringProperty dataInvio;

    public final String getDataInvio() {
        return dataInvioProperty().get();
    }

    public final void setDataInvio(String dataInvio) {
        dataInvioProperty().set(dataInvio);
    }

    public StringProperty dataInvioProperty() {
        if (dataInvio == null) {
            dataInvio = new SimpleStringProperty(this, "dataInvio");
        }
        return dataInvio;
    }

    public Email(int id, String mittente, String destinatario, String cc, String argomento, String messaggio, String data) {
        setID(id);
        setMittente(mittente);
        setDestinatario(destinatario);
        setCC(cc);
        setArgomento(argomento);
        setMessaggio(messaggio);
        setDataInvio(data);
    }

    @Override
    public String toString() {
        return mittente.get() + "//" + destinatario.get() + "//" + cc.get()
                + "//" + argomento.get() + "//" + messaggio.get() + "//" + dataInvio.get();
    }
}
