package postaelettronica;

import common.Message;
import javafx.event.ActionEvent;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.regex.Pattern;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class FXMLVisualizeEmailController implements Initializable {

    @FXML
    private Label lbl_accountName;
    @FXML
    private TableView table_message;
    @FXML
    private TextArea text_messaggio;

    private CasellaPostale casella;
    private int selected = -1;
    private UpdateWork update;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        casella = new CasellaPostale();
    }

    @FXML
    private void table_message_onMouseClicked(MouseEvent event) {
        selected = table_message.getSelectionModel().getSelectedIndex();
        Email email = casella.get(selected);
        if (!(email == null)) {
            text_messaggio.setText(email.getMessaggio());
        }
    }

    @FXML
    private void deleteEmail(ActionEvent event) {
        if (selected == -1) {
            Alert alert = new Alert(AlertType.WARNING);
            alert.setTitle("Attenzione");
            alert.setContentText("Per cancellare una email selezionala dall'elenco");
            alert.showAndWait();
        } else {
            int id = casella.getId(selected);
            if (id != -1) {
                //Invia al server la richiesta di cancellazione
                Socket socket = new Socket();
                try {
                    socket.connect(new InetSocketAddress("localhost", 9999));
                } catch (Exception ex) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Errore");
                    alert.setContentText("Impossibile connettersi al server, prova a cancellare più tardi");
                    alert.showAndWait();
                    try {
                        socket.close();
                    } catch (IOException e) {
                    }
                    return;
                }
                //Prende conoscenza dei canali di comunicazione
                ObjectInputStream is = null;
                ObjectOutputStream os = null;
                try {
                    is = new ObjectInputStream(socket.getInputStream());
                    os = new ObjectOutputStream(socket.getOutputStream());
                } catch (IOException ex) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Errore");
                    alert.setContentText("Errore di comunicazione con il server, prova a cancellare più tardi");
                    alert.showAndWait();
                    try {
                        socket.close();
                    } catch (IOException e) {
                    }
                    return;
                }
                //Invia richiesta
                try {
                    Message message = new Message(1, UserConnected.getUser(), id);
                    os.writeObject(message);
                    os.flush();
                } catch (IOException ex) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Errore");
                    alert.setContentText("Errore di comunicazione con il server, prova a cancellare più tardi");
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
                    alert.setContentText("Errore di comunicazione con il server, la mail potrebbe essere stata comunque cancellata, se entro 60 secondi non è scomparsa riprova");
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
                    alert.setContentText("Errore di comunicazione con il server, la mail potrebbe essere stata comunque cancellata, se entro 60 secondi non è scomparsa riprova");
                    alert.showAndWait();
                    try {
                        socket.close();
                    } catch (IOException e) {
                    }
                    return;
                }
                if (response.equals("OK")) {
                    Alert alert = new Alert(AlertType.INFORMATION);
                    alert.setTitle("Email cancellata");
                    alert.setContentText("Email cancellata correttamente");
                    alert.showAndWait();
                    casella.remove(selected);
                } else {
                    Alert alert = new Alert(AlertType.ERROR);
                    alert.setTitle("Errore");
                    alert.setContentText("Errore durante la cancellazione, riprova");
                    alert.showAndWait();
                }
            }
        }
    }

    @FXML
    private void forward(ActionEvent event) {
        if (selected == -1) {
            Alert alert = new Alert(AlertType.WARNING);
            alert.setTitle("Attenzione");
            alert.setContentText("Per inoltrare una email selezionala dall'elenco");
            alert.showAndWait();
        } else {
            generaFormNuovoMessaggio(casella.get(selected), FXMLNewMessageController.FWD);
        }
    }

    @FXML
    private void answer(ActionEvent event) {
        if (selected == -1) {
            Alert alert = new Alert(AlertType.WARNING);
            alert.setTitle("Attenzione");
            alert.setContentText("Per rispondere a una email selezionala dall'elenco");
            alert.showAndWait();
        } else {
            generaFormNuovoMessaggio(casella.get(selected), FXMLNewMessageController.ANS);
        }
    }

    @FXML
    private void answerAll(ActionEvent event) {
        if (selected == -1) {
            Alert alert = new Alert(AlertType.WARNING);
            alert.setTitle("Attenzione");
            alert.setContentText("Per rispondere a una email selezionala dall'elenco");
            alert.showAndWait();
        } else {
            generaFormNuovoMessaggio(casella.get(selected), FXMLNewMessageController.ANA);
        }
    }

    @FXML
    private void newMessage(ActionEvent event) {
        generaFormNuovoMessaggio(null, FXMLNewMessageController.NEW);
    }

    private void generaFormNuovoMessaggio(Email email, int mod) {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("FXMLNewMessage.fxml"));
        Stage stage = new Stage();
        stage.setTitle("Nuovo Messaggio");
        try {
            stage.setScene(new Scene(loader.load()));
        } catch (IOException ex) {
            ex.toString();
        }
        FXMLNewMessageController controller = loader.<FXMLNewMessageController>getController();
        controller.initEmail(email, mod);
        stage.show();
    }

    public void init(String user, Stage stage) {
        stage.setOnHiding((WindowEvent event) -> {
            update.finish();
        });
        lbl_accountName.setText(lbl_accountName.getText() + " " + user);
        Socket socket = new Socket();
        try {
            socket.connect(new InetSocketAddress("localhost", 9999));
        } catch (Exception ex) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Errore");
            alert.setContentText("Impossibile connettersi al server, il programma verrà chiuso");
            alert.showAndWait();
            try {
                socket.close();
            } catch (IOException e) {
            }
            System.exit(0);
        }
        //Prende conoscenza dei canali di comunicazione
        ObjectInputStream is = null;
        ObjectOutputStream os = null;
        try {
            is = new ObjectInputStream(socket.getInputStream());
            os = new ObjectOutputStream(socket.getOutputStream());
        } catch (IOException ex) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Errore");
            alert.setContentText("Errore di connessione, il programma verrà chiuso");
            alert.showAndWait();
            try {
                socket.close();
            } catch (IOException e) {
            }
            System.exit(0);
        }
        //Invia richiesta
        try {
            Message message = new Message(3, UserConnected.getUser());
            os.writeObject(message);
            os.flush();
        } catch (IOException ex) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Errore");
            alert.setContentText("Errore caricamento messaggi, il programma verrà chiuso");
            alert.showAndWait();
            try {
                socket.close();
            } catch (IOException e) {
            }
            System.exit(0);
        }
        //Lettura della risposta del server
        ArrayList<String> response = null;
        try {
            response = (ArrayList<String>) is.readObject();
        } catch (Exception ex) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Errore");
            alert.setContentText("Errore caricamento messaggi, il programma verrà chiuso");
            alert.showAndWait();
            try {
                socket.close();
            } catch (IOException e) {
            }
            System.exit(0);
        }
        if (response == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Errore");
            alert.setContentText("Errore caricamento messaggi, il programma verrà chiuso");
            alert.showAndWait();
            try {
                socket.close();
            } catch (IOException e) {
            }
            System.exit(0);
        }
        //Update casella di posta
        ArrayList<Email> emails = new ArrayList();
        int i;
        for (i = 0; i < response.size() - 1; i++) {
            String s = response.get(i);
            String[] field = s.split(Pattern.quote("**"));
            Email email = new Email(Integer.valueOf(field[0]), field[1], field[2], field[3], field[4], field[5], field[6]);
            emails.add(email);
        }
        casella.addAll(emails);
        table_message.setItems(casella.getEmails());
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Info");
        alert.setContentText("Hai " + response.get(i) + " nuovi messaggi rispetto all'ultima volta che hai aperto la tua casella");
        alert.showAndWait();
        update = new UpdateWork();
        update.start();
    }

    private class UpdateWork extends Thread {

        private volatile boolean finishWork = false;

        public void finish() {
            finishWork = true;
        }

        @Override
        public void run() {
            Socket socket = new Socket();
            try {
                socket.connect(new InetSocketAddress("localhost", 9999));
            } catch (Exception ex) {
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Errore");
                    alert.setContentText("Impossibile connettersi al server, il programma verrà chiuso");
                    alert.showAndWait();
                    System.exit(0);
                }
                );
                try {
                    socket.close();
                } catch (IOException e) {
                }
            }
            //Prende conoscenza dei canali di comunicazione
            ObjectInputStream is = null;
            ObjectOutputStream os = null;
            try {
                is = new ObjectInputStream(socket.getInputStream());
                os = new ObjectOutputStream(socket.getOutputStream());
            } catch (IOException ex) {
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Errore");
                    alert.setContentText("Errore di update, il programma verrà chiuso");
                    alert.showAndWait();
                    System.exit(0);
                }
                );
                try {
                    socket.close();
                } catch (IOException e) {
                }
            }
            //Invia richiesta
            try {
                Message message = new Message(4, UserConnected.getUser());
                os.writeObject(message);
                os.flush();
            } catch (IOException ex) {
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Errore");
                    alert.setContentText("Errore di update, il programma verrà chiuso");
                    alert.showAndWait();
                    System.exit(0);
                }
                );
                try {
                    socket.close();
                } catch (IOException e) {
                }
            }
            //Lettura della risposta del server
            ArrayList<String> response = null;
            try {
                response = (ArrayList<String>) is.readObject();
            } catch (Exception ex) {
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Errore");
                    alert.setContentText("Errore di update, il programma verrà chiuso");
                    alert.showAndWait();
                    System.exit(0);
                }
                );
                try {
                    socket.close();
                } catch (IOException e) {
                }
            }
            if (response == null) {
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Errore");
                    alert.setContentText("Errore di update, il programma verrà chiuso");
                    alert.showAndWait();
                    System.exit(0);
                }
                );
                try {
                    socket.close();
                } catch (IOException e) {
                }
            }
            //Update casella di posta
            ArrayList<Email> emails = new ArrayList();
            for (String s : response) {
                String[] field = s.split(Pattern.quote("**"));
                Email email = new Email(Integer.valueOf(field[0]), field[1], field[2], field[3], field[4], field[5], field[6]);
                emails.add(email);
            }
            int newMessage = emails.size();
            if (newMessage > 0) {
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Info");
                    alert.setContentText("Hai " + newMessage + " nuovi messaggi");
                    alert.showAndWait();
                }
                );
            }
            casella.addAll(emails);
            try {
                sleep(1000*30);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Errore");
                    alert.setContentText("Errore di update, il programma verrà chiuso");
                    alert.showAndWait();
                    System.exit(0);
                }
                );
            }
            if (!finishWork) {
                update = new UpdateWork();
                update.start();
            }
        }
    }
}
