import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

public class Client implements Runnable {

	private static Socket clientSocket = null;
	private static ObjectOutputStream os = null;
	private static ObjectInputStream is = null;
	private static BufferedReader inputLine = null;
	private static BufferedInputStream bis = null;
	private static boolean closed = false;

	public static void main(String[] args) {

		// port par defaut
		int portNumber = 1234;
		// machine par defaut
		String host = "localhost";

		if (args.length < 2) {
			System.out.println("Serveur par defaut: " + host + ", Port par defaut: " + portNumber);
		} else {
			host = args[0];
			portNumber = Integer.valueOf(args[1]).intValue();
			System.out.println("Server: " + host + ", Port: " + portNumber);
		}

		/*
		 * Ouvrir un socket pour la machine et le port
		 * Ouvrir les input et output streams
		 */
		try {
			clientSocket = new Socket(host, portNumber);
			inputLine = new BufferedReader(new InputStreamReader(System.in));
			os = new ObjectOutputStream(clientSocket.getOutputStream());
			is = new ObjectInputStream(clientSocket.getInputStream());
		} catch (UnknownHostException e) {
			System.err.println("Inconnu " + host);
		} catch (IOException e) {
			System.err.println("Le Serveur est introuvable. Assurez vous que le serveur est demarré.");
		}

		/* 
		 * envoyer des donnees au socket
		 */
		if (clientSocket != null && os != null && is != null) {
			try {

				/* Creation d'un thread pour lire les messages du serveur. */
				new Thread(new Client()).start();
				while (!closed) {

					/* lire les commandes du client */

					String msg = (String) inputLine.readLine().trim();

					/* traitement des commandes*/

					if ((msg.split(":").length > 1))
					{
						if (msg.split(":")[1].toLowerCase().startsWith("sendfile"))
						{
							File sfile = new File((msg.split(":")[1]).split(" ",2)[1]);
							
							if (!sfile.exists())
							{
								System.out.println("Fichier introuvable!!");
								continue;
							}
							
							byte [] mybytearray  = new byte [(int)sfile.length()];
							FileInputStream fis = new FileInputStream(sfile);
							bis = new BufferedInputStream(fis);
							while (bis.read(mybytearray,0,mybytearray.length)>=0)
							{
								bis.read(mybytearray,0,mybytearray.length);
							}
							os.writeObject(msg);
							os.writeObject(mybytearray);
							os.flush();

						}
						else
						{
							os.writeObject(msg);
							os.flush();
						}

					}

					/* Cas broadcast file */

					else if (msg.toLowerCase().startsWith("sendfile"))
					{

						File sfile = new File(msg.split(" ",2)[1]);
						
						if (!sfile.exists())
						{
							System.out.println("Fichier introuvable!!");
							continue;
						}
						
						byte [] mybytearray  = new byte [(int)sfile.length()];
						FileInputStream fis = new FileInputStream(sfile);
						bis = new BufferedInputStream(fis);
						while (bis.read(mybytearray,0,mybytearray.length)>=0)
						{
							bis.read(mybytearray,0,mybytearray.length);
						}
						os.writeObject(msg);
						os.writeObject(mybytearray);
						os.flush();

					}

					/* messages a tous */

					else 
					{
						os.writeObject(msg);
						os.flush();
					}


				}

				/*
				 * fermer les input et output stream et les sockets
				 */
				os.close();
				is.close();
				clientSocket.close();
			} catch (IOException e) 
			{
				System.err.println("IOException:  " + e);
			}
		
			
		}
	}

	/*
	 * implementation de la methode run du thread cree pour communiquer avec le serveur 
	 */

	public void run() {
		/*
		 * lecture continue jusqu'a la reception de "Bye" de la part du serveur.
		 */
		String responseLine;
		String filename = null;
		byte[] ipfile = null;
		FileOutputStream fos = null;
		BufferedOutputStream bos = null;
		File directory_name = null;
		String full_path;
		String dir_name = "Received_Files";

		try {


			while ((responseLine = (String) is.readObject()) != null)  {

				if (responseLine.equals("Repertoire cree pour recevoir des fichiers"))
				{
					//creation de la directoire de reception des fichiers */

					directory_name = new File((String) dir_name);

					if (!directory_name.exists())
					{
						directory_name.mkdir();

						System.out.println("Directoire de reception de fichier cree successivement!!");

					}

					else
					{
						System.out.println("Directoire de reception de fichier existe deja!!");
					}
				}

				else if (responseLine.startsWith("Sending_File"))
				{

					try
					{
						filename = responseLine.split(":")[1];
						full_path = directory_name.getAbsolutePath()+"/"+filename; 
						ipfile = (byte[]) is.readObject();
						fos = new FileOutputStream(full_path);
						bos = new BufferedOutputStream(fos);
						bos.write(ipfile);
						bos.flush();
						System.out.println("Fichier recu.");
					}
					finally
					{
						if (fos != null) fos.close();
						if (bos != null) bos.close();
					}

				}

				/* messages */

				else
				{
					System.out.println(responseLine);
				}


				/* quitter l'application */

				if (responseLine.indexOf("*** Au revoir") != -1)
				
					break;
			}

			closed = true;
			System.exit(0);

		} catch (IOException | ClassNotFoundException e) {

			System.err.println("Processus serveur arrêté!!");

		}
	}
}