import java.net.ServerSocket;
import java.net.Socket;

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
		
	}
}
