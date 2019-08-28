import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

/*
 * Un serveur de discussion qui fournit des messages et des fichiers publics et priv�s.
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
		{System.out.println("Aucun port sp�cifi� par l'utilisateur. \\nLe serveur est en cours d'ex�cution avec le num�ro de port par d�faut=" + numeroDuPort);} 
		else 
		{
			numeroDuPort = Integer.valueOf(args[0]).intValue();
			System.out.println("Le serveur est en cours d'ex�cution avec le num�ro de port sp�cifi�=" + numeroDuPort);
		}
		
		/*
		 * Ouvrir un socket de serveur sur numeroDuPort (1234 par d�faut).
		 */
		try {
			serverSocket = new ServerSocket(numeroDuPort);
		} catch (IOException e) {
			System.out.println("Le Socket Serveur ne peut pas �tre cr��");
			
		}
		
		/*
		 * Cr�ez un socket client pour chaque connexion et transmettez-le � un nouveau Thread client
		 */
		
		int numeroDuClient = 1;
		while (true) {
			try {

				clientSocket = serverSocket.accept();
				
				ThreadClient clientCourrant =  new ThreadClient(clientSocket, clients); //constructeur a ecrire
				
				clients.add(clientCourrant);
				
				clientCourrant.start();
				
				System.out.println("Client "  + numeroDuClient + " est connect�!");
				
				numeroDuClient++;

			} catch (IOException e) {

				System.out.println("Le client n'a pas pu �tre connect�");
			}


		}
		
		
	}
}

class ThreadClient extends Thread {
	
}
