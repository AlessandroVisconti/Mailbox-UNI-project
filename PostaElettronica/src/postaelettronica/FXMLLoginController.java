package postaelettronica;

import common.Message;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class FXMLLoginController implements Initializable {

    @FXML
    private TextField txt_user;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
    }

    @FXML
    private void login(ActionEvent event) {
        String user = txt_user.getText();
        if (user.isEmpty()) {
            //Se non è inserito il nome utente
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Errore");
            alert.setContentText("Inserisci il nome utente per accedere");
            alert.showAndWait();
        } else {
            //Socket per effettuare il login
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
            //Invia il messaggio al server
            try {
                Message message = new Message(2, user);
                os.writeObject(message);
                os.flush();
            } catch (IOException ex) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Errore");
                alert.setContentText("E' occorso un errore inviando la richiesta al server");
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
                alert.setContentText("E' occorso un errore sulla risposta del server");
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
                alert.setContentText("Errore di comunicazione con il server, riprova");
                alert.showAndWait();
                try {
                    socket.close();
                } catch (IOException e) {
                }
                return;
            }
            boolean isLogged = false;
            if (response.equals("OK")) {
                isLogged = true;
            } else if (response.equals("NO")) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Info");
                alert.setContentText("Nome utente non riconosciuto");
                alert.showAndWait();
            } else {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Info");
                alert.setContentText("Errore imprevisto");
                alert.showAndWait();
            }
            //Chiude connessione al server
            try {
                socket.close();
            } catch (IOException ex) {
            }
            if (isLogged) {
                UserConnected.setUser(user);
                FXMLLoader loader = new FXMLLoader(getClass().getResource("FXMLVisualizeEmail.fxml"));
                Stage stage = new Stage();
                try {
                    stage.setScene(new Scene(loader.load()));
                } catch (IOException ex) {
                    ex.toString();
                }
                FXMLVisualizeEmailController controller = loader.<FXMLVisualizeEmailController>getController();
                controller.init(user, stage);
                stage.show();
                //Chiude la finestra di login
                Button b = (Button) (event.getSource());
                Stage old = (Stage) b.getScene().getWindow();
                old.close();
            }
        }
    }
}
