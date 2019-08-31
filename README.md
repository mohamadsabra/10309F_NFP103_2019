# 10309F_NFP103_2019
## Repository of the NFP103 Project  

## CNAM Chatting: une application de conversation multi-client avec transfert de fichier.  

Cette application comprend un fichier Serveur.java et un fichier Client.java représentant les programmes client et serveur de l'application de discussion.  
   
Le programme serveur utilise le protocole de connexion TCP pour écouter les clients se connectant à son socket en utilisant le port spécifié par l'utilisateur.  

### Étapes d'exécution pour Serveur.java
1. Ouvrez la ligne de commande et naviguez jusqu'au dossier où se trouve le fichier Serveur.java.
2. Tapez la commande javac Serveur.java pour la compilation du serveur.
3. Tapez la commande java Serveur 1233 pour exécuter le programme où 1233 est le port.
4. Le serveur affiche le message "Le serveur est en cours d'exécution avec le numéro de port spécifié = 1233". Ce message prouve que le programme serveur est en cours d'exécution et attend que les clients se connectent à ce serveur.
5. Si l’utilisateur ne spécifie aucun port, le serveur utilise le port par défaut 1234.

### Étapes d'exécution pour Client.java
1. Ouvrez la ligne de commande et naviguez jusqu'au dossier où se trouve le fichier Client.java.
2. Tapez la commande javac Client.java pour la compilation du serveur.
3. Tapez la commande java Client localhost 1233 pour exécuter le programme où localhost est le nom du serveur et 1233 le port.
4. Le programme client vous invite à entrer le nom d'utilisateur.
5. Une fois que le client a saisi le nom et que vous avez entré, le serveur affiche le message "Le client 1 est connecté!" confirmant que le Client1 est connecté au serveur.
6. De même, d'autres clients peuvent être connectés au serveur à l'aide du même programme Client.java. Veuillez noter que si le test du programme est effectué sur un seul ordinateur, des dossiers distincts doivent être créés pour le serveur et chaque client.
8. Si l’utilisateur ne spécifie aucun port, le client utilise localhost et le port par défaut 1234.

  
## Liens Utils
http://makemobiapps.blogspot.com/p/multiple-client-server-chat-programming.html
https://docs.oracle.com/javase/tutorial/networking/sockets/clientServer.html


