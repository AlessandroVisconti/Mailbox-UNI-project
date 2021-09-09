package postaelettronica;

import common.Message;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class FXMLNewMessageController implements Initializable {

    @FXML
    private TextArea text_messaggio;
    @FXML
    private TextField text_destinatario;
    @FXML
    private TextField text_cc;
    @FXML
    private TextField text_argomento;

    public static final int NEW = 0;
    public static final int FWD = 1;
    public static final int ANS = 2;
    public static final int ANA = 3;

    private Email originalMessage;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
    }

    @FXML
    private void send(ActionEvent event) {
        //Controllo dei campi
        boolean isCorrect = true;
        if (!text_destinatario.getText().matches(".*@email.it") || text_destinatario.getText().isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Errore");
            alert.setContentText("Inserisci un'email destinatario e ricorda il dominio @email.it");
            alert.showAndWait();
            isCorrect = false;
        }
        if (!text_cc.getText().isEmpty()) {
            String[] ccs = text_cc.getText().split(";");
            for (String s : ccs) {
                if (!s.matches(".*@email.it")) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Errore");
                    alert.setContentText("Il dominio delle email in cc deve essere @email.it\nSepara le email con ; senza spazi");
                    alert.showAndWait();
                    isCorrect = false;
                }
            }
        }
        if (text_argomento.getText().isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Errore");
            alert.setContentText("Il campo oggetto non può essere vuoto");
            alert.showAndWait();
            isCorrect = false;
        }
        if (text_messaggio.getText().isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Errore");
            alert.setContentText("Il campo messaggio non può essere vuoto");
            alert.showAndWait();
            isCorrect = false;
        }
        if (!isCorrect) {
            return;
        }
        //Preparazione della email prendendo i vari campi
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        Date date = new Date();
        String data = dateFormat.format(date).substring(0, 10);
        Email email = new Email(-1, UserConnected.getUser(), text_destinatario.getText(), text_cc.getText(), text_argomento.getText(), text_messaggio.getText(), data);
        //Apertura del socket di comunicazione
        Socket socket = new Socket();
        try {
            socket.connect(new InetSocketAddress("localhost", 9999));
        } catch (Exception ex) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Errore");
            alert.setContentText("Impossibile connettersi al server, riprova più tardi");
            alert.showAndWait();
            try {
                socket.close();
            } catch (IOException e) {
            }
            return;
        }
        //Prende conoscenza dei canali di comunicazione
        ObjectInputStream is;
        ObjectOutputStream os;
        try {
            is = new ObjectInputStream(socket.getInputStream());
            os = new ObjectOutputStream(socket.getOutputStream());
        } catch (IOException ex) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Errore");
            alert.setContentText("E' occorso un errore con la connessione al server, riprova più tardi");
            alert.showAndWait();
            try {
                socket.close();
            } catch (IOException e) {
            }
            return;
        }
        //Invia la mail al server
        try {
            String emailS = email.toString();
            Message message = new Message(0, UserConnected.getUser(), emailS);
            os.writeObject(message);
            os.flush();
        } catch (IOException ex) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Errore");
            alert.setContentText("E' occorso un errore inviando la mail al server, potrebbe non essere stata inviata");
            alert.showAndWait();
            try {
                socket.close();
            } catch (IOException e) {
            }
            return;
        }
        //Lettura della risposta del server
        String response;
        try {
            response = (String) is.readObject();
        } catch (Exception ex) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Errore");
            alert.setContentText("E' occorso un errore sulla risposta del server, la mail potrebbe non essere stata inviata");
            alert.showAndWait();
            try {
                socket.close();
            } catch (IOException e) {
            }
            return;
        }
        if (response == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Errore");
            alert.setContentText("Errore di comunicazione con il server, la mail potrebbe non essere stata inviata");
            alert.showAndWait();
            try {
                socket.close();
            } catch (IOException e) {
            }
            return;
        }
        boolean isSend = false;
        if (response.equals("OK")) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Info");
            alert.setContentText("La mail è stata inviata con successo");
            alert.showAndWait();
            isSend = true;
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Info");
            alert.setContentText("La tua mail è stata rigettata dal server per il seguente motivo: " + response);
            alert.showAndWait();
        }
        //Chiude connessione al server
        try {
            socket.close();
        } catch (IOException ex) {
        }
        //Se la mail è stata inviata correttamente chiude la finestra
        if (isSend) {
            Button b = (Button) (event.getSource());
            Stage old = (Stage) b.getScene().getWindow();
            old.close();
        }
    }

    public void initEmail(Email email, int mod) {
        originalMessage = email;
        switch (mod) {
            case NEW:
                break;
            case FWD:
                text_messaggio.setText("FWD\n---------------------------------------------------\nMittente: "
                        + originalMessage.getMittente()
                        + "\nDestinatario: "
                        + originalMessage.getDestinatario()
                        + "\nCC: "
                        + originalMessage.getCC()
                        + "\nMessaggio:\n"
                        + originalMessage.getMessaggio()
                        + "\n---------------------------------------------------\n");
                text_argomento.setText("FWD: " + originalMessage.getArgomento());
                break;
            case ANS:
                text_messaggio.setText("<<<\nMessaggio originale:\n"
                        + originalMessage.getMessaggio()
                        + "\n>>>\n");
                text_argomento.setText("RE: " + originalMessage.getArgomento());
                text_destinatario.setText(originalMessage.getMittente());
                break;
            case ANA:
                text_messaggio.setText("<<<\nMessaggio originale:\n"
                        + originalMessage.getMessaggio()
                        + "\n>>>\n");
                text_argomento.setText("RE: " + originalMessage.getArgomento());
                text_destinatario.setText(originalMessage.getMittente());
                text_cc.setText(originalMessage.getCC());
                break;
        }
    }
}
