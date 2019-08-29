import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

/*
 * Un serveur de discussion qui fournit des messages et des fichiers publics et privés.
 */
public class Serveur {
	
	//Socket Serveur
	private static ServerSocket serverSocket = null;
	//Socket Client
	private static Socket clientSocket = null;
	
	public static ArrayList<ThreadClient> clients = new ArrayList<ThreadClient>();
	
	public static void main(String args[]) {
		
		int numeroDuPort = 1234;
			
		if (args.length < 1) 
		{System.out.println("Aucun port spécifié par l'utilisateur. \\nLe serveur est en cours d'exécution avec le numéro de port par défaut=" + numeroDuPort);} 
		else 
		{
			numeroDuPort = Integer.valueOf(args[0]).intValue();
			System.out.println("Le serveur est en cours d'exécution avec le numéro de port spécifié=" + numeroDuPort);
		}
		
		/*
		 * Ouvrir un socket de serveur sur numeroDuPort (1234 par défaut).
		 */
		try {
			serverSocket = new ServerSocket(numeroDuPort);
		} catch (IOException e) {
			System.out.println("Le Socket Serveur ne peut pas être créé");		
		}
		
		/*
		 * Créez un socket client pour chaque connexion et transmettez-le à un nouveau Thread client
		 */
		
		int numeroDuClient = 1;
		while (true) {
			try {

				clientSocket = serverSocket.accept();
				
				ThreadClient clientCourrant =  new ThreadClient(clientSocket, clients); //constructeur a ecrire
				
				clients.add(clientCourrant);
				
				clientCourrant.start();
				
				System.out.println("Client "  + numeroDuClient + " est connecté!");
				
				numeroDuClient++;

			} catch (IOException e) {

				System.out.println("Le client n'a pas pu être connecté");
			}
		}	
	}
}

/*
 * Cette classe de thread client gère des clients individuels dans leurs threads respectifs en ouvrant des flux d'entrée et de sortie distincts.
 */
class ThreadClient extends Thread {
	private String clientName = null;
	private ObjectInputStream is = null;
	private ObjectOutputStream os = null;
	private Socket clientSocket = null;
	private final ArrayList<ThreadClient> clients;
	
	//constructeur
	public ThreadClient(Socket clientSocket, ArrayList<ThreadClient> clients) {

		this.clientSocket = clientSocket;
		this.clients = clients;

	}
	
	public void run() {
		ArrayList<ThreadClient> clients = this.clients;
		try {
			/*
			 * Creation de input et output streams pour le client
			 */
			is = new ObjectInputStream(clientSocket.getInputStream());
			os = new ObjectOutputStream(clientSocket.getOutputStream());
			String nomDuClient;
			while (true) {
				synchronized (this) {
					this.os.writeObject("Saisir votre nom :");
					this.os.flush();
					nomDuClient = ((String) this.is.readObject()).trim();
					if ((nomDuClient.indexOf('@') == -1) || (nomDuClient.indexOf('!') == -1)) {
						break;
					} else {
						this.os.writeObject("Votre nom ne doit pas contenir  '@' ou '!'");
						this.os.flush();
					}
				}
			}
			
			/* Bienvenue */
			System.out.println("Le nom du client est " + nomDuClient); 

			this.os.writeObject("*** Bienvenue " + nomDuClient + " à CNAM chat room ***\nSaisir _quit pour quiter le Serveur");
			this.os.flush();

			this.os.writeObject("Répertoire créé pour recevoir des fichiers"); // a implementer
			this.os.flush();
			
			synchronized(this)
			{

			for (ThreadClient clientCourrant : clients)  
			{
				if (clientCourrant != null && clientCourrant == this) {
					clientName = "@" + nomDuClient;
					break;
				}
			}

			for (ThreadClient clientCourrant : clients) {
				if (clientCourrant != null && clientCourrant != this) {
					clientCourrant.os.writeObject(nomDuClient + " a rejoint");
					clientCourrant.os.flush();

					}

				}
			}
			
			/* Demarrer la conversation. */

			while (true) {

				this.os.writeObject("Saisir votre commande ( _quit pour quitter @ pour envoyer un message privé:");
				this.os.flush();

				String ligne = (String) is.readObject();


				if (ligne.startsWith("_quit")) {

					break;
				}

				/* message privé. */

				if (ligne.startsWith("@")) {

					unicast(ligne,nomDuClient);        	

				}

				/* Si le message est bloqué pour un client */

				else if(ligne.startsWith("!"))
				{
					blockcast(ligne,nomDuClient);
				}

				else 
				{

					broadcast(ligne,nomDuClient);

				}

			}
			
			/* cas ou le client saisit _quit */

			this.os.writeObject("*** Au revoir " + nomDuClient + " ***");
			this.os.flush();
			System.out.println(nomDuClient + " s'est déconnecté.");
			clients.remove(this);
			
			synchronized(this) {

				if (!clients.isEmpty()) {
					for (ThreadClient clientCourrant : clients) {

						if (clientCourrant != null && clientCourrant != this && clientCourrant.clientName != null) {
							clientCourrant.os.writeObject("*** L'utilisateur " + nomDuClient + " s'est déconnecté. ***");
							clientCourrant.os.flush();
						}
					}
				}
			}

			this.is.close();
			this.os.close();
			clientSocket.close();
			
		} catch (Exception e) {
			System.out.println("session terminée");
		} catch (ClassNotFoundException e) {
			System.out.println("Class Not Found");
		}		
		
	}
	
	void blockcast(String line, String nomDuClient) throws IOException, ClassNotFoundException {} //a ecrire
	
	void unicast(String line, String name) throws IOException, ClassNotFoundException {

		String[] words = line.split(":", 2); 

		/* transfere fichier a un client particulier */

		if (words[1].split(" ")[0].toLowerCase().equals("sendfile"))
		{
			byte[] file_data = (byte[]) is.readObject();

			for (ThreadClient clientCourrant : clients) {
				if (clientCourrant != null && clientCourrant != this && clientCourrant.clientName != null
						&& clientCourrant.clientName.equals(words[0]))
				{
					clientCourrant.os.writeObject("Envoyer le fichier...:"+words[1].split(" ",2)[1].substring(words[1].split("\\s",2)[1].lastIndexOf(File.separator)+1));
					clientCourrant.os.writeObject(file_data);
					clientCourrant.os.flush();
					System.out.println(this.clientName.substring(1) + " a enovye un fichier prive au client "+ clientCourrant.clientName.substring(1));

					/* Echo this message to let the sender know the private message was sent.*/

					this.os.writeObject("Fichier prive envoye a  " + clientCourrant.clientName.substring(1));
					this.os.flush();
					break;

				}
			}
		}

		/* transfere message a un client particulier*/

		else
		{

			if (words.length > 1 && words[1] != null) {

				words[1] = words[1].trim();


				if (!words[1].isEmpty()) {

					for (ThreadClient clientCourrant : clients) {
						if (clientCourrant != null && clientCourrant != this && clientCourrant.clientName != null
								&& clientCourrant.clientName.equals(words[0])) {
							clientCourrant.os.writeObject("<" + name + "> " + words[1]);
							clientCourrant.os.flush();

							System.out.println(this.clientName.substring(1) + " a envoye un message prive au client "+ clientCourrant.clientName.substring(1));

							/* Echo this message to let the sender know the private message was sent.*/

							this.os.writeObject("Message prive envoye a " + clientCourrant.clientName.substring(1));
							this.os.flush();
							break;
						}
					}
				}
			}
		}
	}
	
	/* *** cette fonction transfer un message ou un ficiher a tous les clients connects au serveru */

	void broadcast(String ligne, String name) throws IOException, ClassNotFoundException {

		/* transferer un fichier a tous les clients */

		if (ligne.split("\\s")[0].toLowerCase().equals("sendfile"))
		{

			byte[] file_data = (byte[]) is.readObject();
			synchronized(this){
				for (ThreadClient clientCourrant : clients) {
					if (clientCourrant != null && clientCourrant.clientName != null && clientCourrant.clientName!=this.clientName) 
					{
						clientCourrant.os.writeObject("Sending_File:"+ligne.split("\\s",2)[1].substring(ligne.split("\\s",2)[1].lastIndexOf(File.separator)+1));
						clientCourrant.os.writeObject(file_data);
						clientCourrant.os.flush();

					}
				}

				this.os.writeObject("Succes! fichier envoye a tous les clients");
				this.os.flush();
				System.out.println("Fichier envoye a tous les clients par " + this.clientName.substring(1));
			}
		}

		else
		{
			/* transferrer un message a tous les clients */

			synchronized(this){

				for (ThreadClient clientCourrant : clients) {

					if (clientCourrant != null && clientCourrant.clientName != null && clientCourrant.clientName!=this.clientName) 
					{

						clientCourrant.os.writeObject("<" + name + "> " + ligne);
						clientCourrant.os.flush();

					}
				}

				this.os.writeObject("succes! Message envoye a tous les clients.");
				this.os.flush();
				System.out.println("Message envoye a tous par " + this.clientName.substring(1));
			}

		}

	}
}
