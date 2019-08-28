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
	void broadcast(String line, String nomDuClient) throws IOException, ClassNotFoundException {} // a ecrire
	void unicast(String line, String nomDuClient) throws IOException, ClassNotFoundException {} // a ecrire
}
