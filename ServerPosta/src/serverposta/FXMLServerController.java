 package serverposta;

import common.Message;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.ResourceBundle;
import java.util.Scanner;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextArea;

public class FXMLServerController implements Initializable {

    @FXML
    private TextArea text_log;
    private ServerWork work;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        work = new ServerWork();
        work.start();
    }

    public void finish() {
        work.close();
    }

    private class ServerWork extends Thread {

        private ServerSocket server;

        @Override
        public void run() {
            //Apertura server
            try {
                server = new ServerSocket(9999);
            } catch (IOException ex) {
                Platform.runLater(() -> text_log.appendText("Impossibile aprire il server\n"));
                return;
            }
            try {
                while (true) {
                    //Accettazione client
                    Socket client = server.accept();
                    ServerService service = new ServerService(client);
                    service.start();
                }
            } catch (IOException ex) {
            }
        }

        public void close() {
            try {
                server.close();
            } catch (IOException ex) {
            }
        }
    }

    private class ServerService extends Thread {

        private final String MAILBOXADDRESS = "MAILBOX/";
        private final Socket client;

        public ServerService(Socket client) {
            this.client = client;
        }

        @Override
        public void run() {
            Platform.runLater(() -> text_log.appendText("Connesso al client: "));
            ObjectOutputStream os;
            ObjectInputStream is;
            //Prende conoscenza dei canali di comunicazione
            try {
                os = new ObjectOutputStream(client.getOutputStream());
                is = new ObjectInputStream(client.getInputStream());
            } catch (IOException ex) {
                Platform.runLater(() -> text_log.appendText("? -- PROBLEMA DI CONNESSIONE\n"));
                try {
                    client.close();
                } catch (IOException e) {
                }
                return;
            }

            //Ricezione messaggio
            Message message;
            try {
                message = (Message) is.readObject();
            } catch (Exception ex) {
                Platform.runLater(() -> text_log.appendText("? -- PROBLEMA DI RICEZIONE MESSAGGIO\n"));
                try {
                    os.writeObject("messaggio non ricevuto");
                } catch (IOException e) {
                    Platform.runLater(() -> text_log.appendText("> ?" + ": PROBLEMA INVIO RISPOSTA AL CLIENT\n"));
                }
                try {
                    client.close();
                } catch (IOException e) {
                }
                return;
            }
            if (message == null) {
                Platform.runLater(() -> text_log.appendText("? -- MESSAGGIO VUOTO\n"));
                try {
                    os.writeObject("messaggio vuoto");
                } catch (IOException ex) {
                    Platform.runLater(() -> text_log.appendText("> ?" + ": PROBLEMA INVIO RISPOSTA AL CLIENT\n"));
                }
                try {
                    client.close();
                } catch (IOException ex) {
                }
                return;
            }

            //Controllo parametri obbligatori
            Integer operation = message.getOperation();
            String user = message.getClientName();
            if (operation == null) {
                Platform.runLater(() -> text_log.appendText("? -- PROBLEMA PARAMETRO OPERATION -- VUOTO\n"));
                try {
                    os.writeObject("operazione vuota");
                } catch (IOException ex) {
                    Platform.runLater(() -> text_log.appendText("> " + user + ": PROBLEMA INVIO RISPOSTA AL CLIENT\n"));
                }
                try {
                    client.close();
                } catch (IOException e) {
                }
                return;
            }
            if (user == null) {
                Platform.runLater(() -> text_log.appendText("? -- PROBLEMA PARAMETRO USER -- VUOTO\n"));
                try {
                    os.writeObject("user mancante");
                } catch (IOException ex) {
                    Platform.runLater(() -> text_log.appendText("> " + user + ": PROBLEMA INVIO RISPOSTA AL CLIENT\n"));
                }
                try {
                    client.close();
                } catch (IOException e) {
                }
                return;
            } else {
                Platform.runLater(() -> text_log.appendText(user + "\n"));
            }

            switch (operation) {
                case 0:
                    //Invio di email
                    String email = message.getEmail();
                    if (email == null) {
                        Platform.runLater(() -> text_log.appendText("> " + user + ": PROBLEMA PARAMETRO EMAIL -- VUOTO\n"));
                        try {
                            os.writeObject("email vuota");
                        } catch (IOException ex) {
                            Platform.runLater(() -> text_log.appendText("> " + user + ": PROBLEMA INVIO RISPOSTA AL CLIENT\n"));
                        }
                        try {
                            client.close();
                        } catch (IOException e) {
                        }
                        return;
                    } else {
                        //Controllo dei dati passati dal client
                        String[] emailField = email.split("//");
                        String mittente = emailField[0];
                        String destinatario = emailField[1];
                        String ccs = emailField[2];
                        String argomento = emailField[3];
                        String messaggio = emailField[4];
                        String data = emailField[5];
                        //Controllo che mittente e destinatario siano diversi
                        if (mittente.equals(destinatario)) {
                            Platform.runLater(() -> text_log.appendText("> " + user + ": Email autoinviata non corretto\n"));
                            try {
                                os.writeObject("mittente = destinatario");
                            } catch (IOException ex) {
                                Platform.runLater(() -> text_log.appendText("> " + user + ": PROBLEMA INVIO RISPOSTA AL CLIENT\n"));
                            }
                            try {
                                client.close();
                            } catch (IOException ex) {
                            }
                            return;
                        }
                        //Controllo che ogni account in cc sia diverso da mittente e destinatario
                        String[] ccList = new String[0];
                        if (!ccs.isEmpty()) {
                            boolean error = false;
                            ccList = ccs.split(";");
                            for (String cc : ccList) {
                                if (cc.equals(destinatario) || cc.equals(mittente)) {
                                    error = true;
                                }
                            }
                            if (error) {
                                Platform.runLater(() -> text_log.appendText("> " + user + ": CC non corretto\n"));
                                try {
                                    os.writeObject("cc = destinatario oppure cc = mittente");
                                } catch (IOException ex) {
                                    Platform.runLater(() -> text_log.appendText("> " + user + ": PROBLEMA INVIO RISPOSTA AL CLIENT\n"));
                                }
                                try {
                                    client.close();
                                } catch (IOException ex) {
                                }
                                return;
                            }
                        }
                        //Controllo che gli account inseriti siano presenti
                        FileReader fr = null;
                        try {
                            fr = new FileReader("account.txt");
                        } catch (IOException ex) {
                            Platform.runLater(() -> text_log.appendText("> " + user + ": Mancato controllo account\n"));
                            try {
                                os.writeObject("errore controllo account");
                            } catch (IOException e) {
                                Platform.runLater(() -> text_log.appendText("> " + user + ": PROBLEMA INVIO RISPOSTA AL CLIENT\n"));
                            }
                            try {
                                client.close();
                            } catch (IOException e) {
                            }
                            return;
                        }
                        BufferedReader br = new BufferedReader(fr);
                        Scanner scan = new Scanner(br);
                        boolean mittOK = false, destOK = false, ccOK = true;
                        boolean[] ccF = new boolean[ccList.length];
                        for (int i = 0; i < ccF.length; i++) {
                            ccF[i] = false;
                        }
                        while (scan.hasNext()) {
                            String line = scan.nextLine();
                            if (line.equals(destinatario)) {
                                destOK = true;
                            } else if (line.equals(mittente)) {
                                mittOK = true;
                            } else {
                                for (int i = 0; i < ccList.length; i++) {
                                    if (line.equals(ccList[i])) {
                                        ccF[i] = true;
                                    }
                                }
                            }
                        }
                        try {
                            scan.close();
                            br.close();
                            fr.close();
                        } catch (IOException ex) {
                        }
                        for (int i = 0; i < ccF.length; i++) {
                            if (!ccF[i]) {
                                ccOK = false;
                            }
                        }
                        if (ccOK == false || mittOK == false || destOK == false) {
                            Platform.runLater(() -> text_log.appendText("> " + user + ": Qualche account non verificato\n"));
                            try {
                                os.writeObject("qualche account tra destinatario e cc non è presente");
                            } catch (IOException ex) {
                                Platform.runLater(() -> text_log.appendText("> " + user + ": PROBLEMA INVIO RISPOSTA AL CLIENT\n"));
                            }
                            try {
                                client.close();
                            } catch (IOException ex) {
                            }
                            return;
                        }
                        //Scrive la mail temporanea da inserire nella mailbox dei destinatari
                        try {
                            PrintWriter writer = new PrintWriter("provvisorio" + mittente + ".txt", "UTF-8");
                            writer.println(mittente + "**");
                            writer.println(destinatario + "**");
                            writer.println(ccs + "**");
                            writer.println(argomento + "**");
                            writer.println(messaggio + "**");
                            writer.println(data);
                            writer.close();
                        } catch (IOException ex) {
                            Platform.runLater(() -> text_log.appendText("> " + user + ": ERRORE SERVER\n"));
                            try {
                                os.writeObject("errore imprevisto del server");
                            } catch (IOException e) {
                                Platform.runLater(() -> text_log.appendText("> " + user + ": PROBLEMA INVIO RISPOSTA AL CLIENT\n"));
                            }
                            try {
                                client.close();
                            } catch (IOException e) {
                            }
                            return;
                        }
                        //Copia la mail temporanea nelle caselle dei destinatari con il giusto numero id
                        File folder = new File(MAILBOXADDRESS + destinatario);
                        Path provvisorio = Paths.get("provvisorio" + mittente + ".txt");
                        File[] listOfFiles = folder.listFiles();
                        int next;
                        if (listOfFiles.length == 1) {
                            next = 0;
                        } else {
                            next = Integer.valueOf(listOfFiles[listOfFiles.length - 2].getName()
                                    .substring(0, listOfFiles[listOfFiles.length - 2].getName().indexOf("."))) + 1;
                        }
                        Path dest = Paths.get(MAILBOXADDRESS + destinatario + "/" + next + ".txt");
                        try {
                            Files.copy(provvisorio, dest, StandardCopyOption.REPLACE_EXISTING);
                        } catch (IOException ex) {
                            Platform.runLater(() -> text_log.appendText("> " + user + ": ERRORE SERVER\n"));
                            try {
                                os.writeObject("errore imprevisto del server");
                            } catch (IOException e) {
                                Platform.runLater(() -> text_log.appendText("> " + user + ": PROBLEMA INVIO RISPOSTA AL CLIENT\n"));
                            }
                            try {
                                client.close();
                            } catch (IOException e) {
                            }
                            return;
                        }
                        int[] nexts = new int[ccList.length];
                        int i = 0;
                        Arrays.sort(ccList);
                        for (String cc : ccList) {
                            folder = new File(MAILBOXADDRESS + cc);
                            listOfFiles = folder.listFiles();
                            if (listOfFiles.length == 1) {
                                nexts[i] = 0;
                            } else {
                                nexts[i] = Integer.valueOf(listOfFiles[listOfFiles.length - 2].getName()
                                        .substring(0, listOfFiles[listOfFiles.length - 2].getName().indexOf("."))) + 1;
                            }
                            dest = Paths.get(MAILBOXADDRESS + cc + "/" + nexts[i] + ".txt");
                            try {
                                Files.copy(provvisorio, dest, StandardCopyOption.REPLACE_EXISTING);
                            } catch (IOException ex) {
                                Platform.runLater(() -> text_log.appendText("> " + user + ": ERRORE SERVER\n"));
                                try {
                                    os.writeObject("errore imprevisto del server");
                                } catch (IOException e) {
                                    Platform.runLater(() -> text_log.appendText("> " + user + ": PROBLEMA INVIO RISPOSTA AL CLIENT\n"));
                                }
                                try {
                                    client.close();
                                } catch (IOException e) {
                                }
                                return;
                            }
                            i++;
                        }
                        //Elimina la mail temporanea
                        File temp = new File("provvisorio" + mittente + ".txt");
                        temp.delete();
                        Platform.runLater(() -> text_log.appendText("> " + user + ": Email inviata\n"));
                        try {
                            Files.write(Paths.get(MAILBOXADDRESS + destinatario + "/new.txt"), ("" + next + ".txt,").getBytes(), StandardOpenOption.APPEND);
                        } catch (IOException ex) {
                        }
                        i = 0;
                        for (String cc : ccList) {
                            try {
                                Files.write(Paths.get(MAILBOXADDRESS + cc + "/new.txt"), ("" + nexts[i] + ".txt,").getBytes(), StandardOpenOption.APPEND);
                            } catch (IOException ex) {
                            }
                            i++;
                        }
                        try {
                            os.writeObject("OK");
                        } catch (IOException ex) {
                            Platform.runLater(() -> text_log.appendText("> " + user + ": PROBLEMA INVIO RISPOSTA AL CLIENT\n"));
                            try {
                                client.close();
                            } catch (IOException e) {
                            }
                            return;
                        }
                    }
                    break;
                case 1:
                    //Cancellazione di email
                    Integer id = message.getId();
                    if (id == null) {
                        Platform.runLater(() -> text_log.appendText("> " + user + ": PROBLEMA PARAMETRO ID -- VUOTO\n"));
                        try {
                            os.writeObject("id vuoto");
                        } catch (IOException ex) {
                            Platform.runLater(() -> text_log.appendText("> " + user + ": PROBLEMA INVIO RISPOSTA AL CLIENT\n"));
                        }
                        try {
                            client.close();
                        } catch (IOException e) {
                        }
                        return;
                    } else {
                        boolean isInList = false;
                        //Apre la cartella dell'utente per cercare l'email
                        File folder = new File(MAILBOXADDRESS + user);
                        File[] listOfFile = folder.listFiles();
                        for (File f : listOfFile) {
                            if (f.getName().equals(id + ".txt")) {
                                f.delete();
                                isInList = true;
                            }
                        }
                        if (isInList) {
                            //Email presente
                            Platform.runLater(() -> text_log.appendText("Email " + id + " cancellata\n"));
                            try {
                                os.writeObject("OK");
                            } catch (IOException ex) {
                                Platform.runLater(() -> text_log.appendText("PROBLEMA INVIO RISPOSTA AL CLIENT\n"));
                                try {
                                    client.close();
                                } catch (IOException e) {
                                }
                                return;
                            }
                        } else {
                            //Email non presente
                            Platform.runLater(() -> text_log.appendText("Email " + id + " non esistente\n"));
                            try {
                                os.writeObject("id non corretto");
                            } catch (IOException ex) {
                                Platform.runLater(() -> text_log.appendText("PROBLEMA INVIO RISPOSTA AL CLIENT\n"));
                            }
                            try {
                                client.close();
                            } catch (IOException e) {
                            }
                            return;
                        }
                    }
                    break;
                case 2:
                    //Login
                    boolean isInList = false;
                    FileReader fr = null;
                    try {
                        fr = new FileReader("account.txt");
                    } catch (IOException ex) {
                        Platform.runLater(() -> text_log.appendText("> " + user + ": ERRORE SERVER"));
                        try {
                            os.writeObject("errore imprevisto server");
                        } catch (IOException e) {
                            Platform.runLater(() -> text_log.appendText("> " + user + ": PROBLEMA INVIO RISPOSTA AL CLIENT\n"));
                        }
                        try {
                            client.close();
                        } catch (IOException e) {
                        }
                        return;
                    }
                    BufferedReader br = new BufferedReader(fr);
                    Scanner scan = new Scanner(br);
                    while (scan.hasNext() && !isInList) {
                        String line = scan.nextLine();
                        if (line.equals(user)) {
                            isInList = true;
                        }
                    }
                    try {
                        scan.close();
                        br.close();
                        fr.close();
                    } catch (IOException ex) {
                    }
                    if (isInList) {
                        Platform.runLater(() -> text_log.appendText("> " + user + ": Utente connesso\n"));
                        try {
                            os.writeObject("OK");
                        } catch (IOException ex) {
                            Platform.runLater(() -> text_log.appendText("> " + user + ": PROBLEMA INVIO RISPOSTA AL CLIENT\n"));
                            try {
                                client.close();
                            } catch (IOException e) {
                            }
                            return;
                        }
                    } else {
                        Platform.runLater(() -> text_log.appendText("> " + user + ": Utente non presente\n"));
                        try {
                            os.writeObject("NO");
                        } catch (IOException ex) {
                            Platform.runLater(() -> text_log.appendText("> " + user + ": PROBLEMA INVIO RISPOSTA AL CLIENT\n"));
                            try {
                                client.close();
                            } catch (IOException e) {
                            }
                            return;
                        }
                    }
                    break;
                case 3:
                    //Caricamento totale casella postale
                    //Apro la cartella contenente le email
                    File folder = new File(MAILBOXADDRESS + user);
                    File[] listOfFiles = folder.listFiles();
                    ArrayList<String> emails = new ArrayList<>();
                    //Per ogni email la inserisco nella lista
                    for (File f : listOfFiles) {
                        FileReader fileReader = null;
                        try {
                            fileReader = new FileReader(f);
                        } catch (IOException ex) {
                            Platform.runLater(() -> text_log.appendText("> " + user + "ERRORE SERVER"));
                            try {
                                os.writeObject(null);
                            } catch (IOException e) {
                                Platform.runLater(() -> text_log.appendText("> " + user + ": PROBLEMA INVIO RISPOSTA AL CLIENT\n"));
                            }
                            try {
                                client.close();
                            } catch (IOException e) {
                            }
                            return;
                        }
                        BufferedReader bufferedReader = new BufferedReader(fileReader);
                        Scanner scanner = new Scanner(bufferedReader);
                        if (!f.getName().equals("new.txt")) {
                            String singleEmail = "" + f.getName().substring(0, f.getName().indexOf(".")) + "**";
                            while (scanner.hasNext()) {
                                singleEmail += scanner.nextLine();
                            }
                            emails.add(singleEmail);
                        }
                        try {
                            scanner.close();
                            bufferedReader.close();
                            fileReader.close();
                        } catch (IOException ex) {
                        }
                    }
                    FileReader news;
                    try {
                        news = new FileReader(MAILBOXADDRESS + user + "/new.txt");
                    } catch (IOException ex) {
                        Platform.runLater(() -> text_log.appendText("> " + user + "ERRORE SERVER"));
                        try {
                            os.writeObject(null);
                        } catch (IOException e) {
                            Platform.runLater(() -> text_log.appendText("> " + user + ": PROBLEMA INVIO RISPOSTA AL CLIENT\n"));
                        }
                        try {
                            client.close();
                        } catch (IOException e) {
                        }
                        return;
                    }
                    BufferedReader reader = new BufferedReader(news);
                    Scanner scanner = new Scanner(reader);
                    String line = null;
                    int newNumber = -1;
                    try {
                        line = scanner.nextLine();
                    } catch (NoSuchElementException ex) {
                        newNumber = 0;
                    }
                    if (newNumber == -1 && line != null) {
                        newNumber = line.substring(0, line.length() - 1).split(",").length;
                    } else {
                        newNumber = 0;
                    }
                    emails.add(String.valueOf(newNumber));
                    try {
                        Files.write(Paths.get(MAILBOXADDRESS + user + "/new.txt"), ("").getBytes(), StandardOpenOption.TRUNCATE_EXISTING);
                    } catch (IOException ex) {
                    }
                    //Invio al client 
                    Platform.runLater(() -> text_log.appendText("> " + user + ": Casella caricata\n"));
                    try {
                        os.writeObject(emails);
                    } catch (IOException ex) {
                        Platform.runLater(() -> text_log.appendText("> " + user + ": PROBLEMA INVIO RISPOSTA AL CLIENT\n"));
                        try {
                            client.close();
                        } catch (IOException e) {
                        }
                        return;
                    }
                    break;
                case 4:
                    //Apro la cartella contenente le email
                    File mailbox = new File(MAILBOXADDRESS + user);
                    File[] fileList = mailbox.listFiles();
                    ArrayList<String> emailsList = new ArrayList<>();
                    FileReader newFile;
                    try {
                        newFile = new FileReader(MAILBOXADDRESS + user + "/new.txt");
                    } catch (IOException ex) {
                        Platform.runLater(() -> text_log.appendText("> " + user + "ERRORE SERVER"));
                        try {
                            os.writeObject(null);
                        } catch (IOException e) {
                            Platform.runLater(() -> text_log.appendText("> " + user + ": PROBLEMA INVIO RISPOSTA AL CLIENT\n"));
                        }
                        try {
                            client.close();
                        } catch (IOException e) {
                        }
                        return;
                    }
                    BufferedReader bufferedReader = new BufferedReader(newFile);
                    Scanner scanner2 = new Scanner(bufferedReader);
                    String line2;
                    try {
                        line2 = scanner2.nextLine();
                    } catch (NoSuchElementException ex) {
                        try {
                            os.writeObject(emailsList);
                        } catch (IOException e) {
                            Platform.runLater(() -> text_log.appendText("> " + user + ": PROBLEMA INVIO RISPOSTA AL CLIENT\n"));
                            try {
                                client.close();
                            } catch (IOException ee) {
                            }
                            return;
                        }
                        break;
                    }
                    line2 = line2.substring(0, line2.length() - 1);
                    ArrayList<String> newFiles = new ArrayList<>(Arrays.asList(line2.split(";")));
                    //Per ogni email nuova la inserisco nella lista
                    for (File f : fileList) {
                        FileReader fileReader = null;
                        try {
                            fileReader = new FileReader(f);
                        } catch (IOException ex) {
                            Platform.runLater(() -> text_log.appendText("> " + user + "ERRORE SERVER"));
                            try {
                                os.writeObject(null);
                            } catch (IOException e) {
                                Platform.runLater(() -> text_log.appendText("> " + user + ": PROBLEMA INVIO RISPOSTA AL CLIENT\n"));
                            }
                            try {
                                client.close();
                            } catch (IOException e) {
                            }
                            return;
                        }
                        bufferedReader = new BufferedReader(fileReader);
                        scanner = new Scanner(bufferedReader);
                        if (!f.getName().equals("new.txt") && newFiles.contains(f.getName())) {
                            String singleEmail = "" + f.getName().substring(0, f.getName().indexOf(".")) + "**";
                            while (scanner.hasNext()) {
                                singleEmail += scanner.nextLine();
                            }
                            emailsList.add(singleEmail);
                        }
                        try {
                            scanner.close();
                            bufferedReader.close();
                            fileReader.close();
                        } catch (IOException ex) {
                        }
                    }
                    try {
                        Files.write(Paths.get(MAILBOXADDRESS + user + "/new.txt"), ("").getBytes(), StandardOpenOption.TRUNCATE_EXISTING);
                    } catch (IOException ex) {
                    }
                    //Invio al client 
                    Platform.runLater(() -> text_log.appendText("> " + user + ": Update inviato\n"));
                    try {
                        os.writeObject(emailsList);
                    } catch (IOException ex) {
                        Platform.runLater(() -> text_log.appendText("> " + user + ": PROBLEMA INVIO RISPOSTA AL CLIENT\n"));
                        try {
                            client.close();
                        } catch (IOException e) {
                        }
                        return;
                    }
                    break;
                default:
                    Platform.runLater(() -> text_log.appendText("> " + user + ": Operazione non riconosciuta\n"));
                    try {
                        os.writeObject("operazione non riconosciuta");
                    } catch (IOException ex) {
                        Platform.runLater(() -> text_log.appendText("> " + user + ": PROBLEMA INVIO RISPOSTA AL CLIENT\n"));
                    }
                    try {
                        client.close();
                    } catch (IOException ex) {
                    }
                    return;
            }
            //Chiude connessione con successo
            Platform.runLater(() -> text_log.appendText("> Client " + user + " servito correttamente\n"));
            try {
                client.close();
            } catch (IOException ex) {
            }
        }
    }
}
