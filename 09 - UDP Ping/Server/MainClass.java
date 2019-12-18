import java.io.IOException;
import java.net.BindException;
import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.net.SocketException;
import java.util.Random;

/*
Università di Pisa - Laboratorio del corso di Reti di Calcolatori - A.A. 2018-2019
Assignment #9 - UDP Ping
Esercizio svolto singolarmente da Alessandro Antonelli, matricola 507264
*/

/*
Il programma costituisce il server di un'utility Ping che utilizza una connessione UDP.
*/

public class MainClass {
	
	final static private String parametri = "portaServer";
	
	/*
	 *		SINTASSI / PARAMETRI ACCETTATI DAL MAIN:
	 *
	 *	MainClass portaServer
	 *
	 *		portaServer		(int)			Numero di porta sul quale rendere disponibile il servizio di Ping
	*/

	public static void main(String[] args) {
		//======= LEGGO I PARAMETRI =======
		if(args.length != 1) ErroreFatale("al programma deve essere passato esattamente un parametro!", true);
		int portaServer = 0;
		
		try
		{
			portaServer = Integer.parseInt(args[0]);
			if(portaServer < 1025 || portaServer > 65535) ErroreFatale("La porta deve essere compresa tra 1025 e 65535!", false);
			
			DatagramSocket sock = new DatagramSocket(portaServer);
			byte[] buffer = new byte[80];
			DatagramPacket pacchettoInEntrata = new DatagramPacket(buffer, buffer.length);
		
			Random rand = new Random();
			
			System.out.println("[Server] Inizio fase di ascolto dei ping");
			while(true)
			{
				sock.receive(pacchettoInEntrata);
				String messaggioRicevuto = new String(buffer, 0, pacchettoInEntrata.getLength());
				System.out.printf("\n[Server] Ricevuto pacchetto dall'host %s dalla porta %d\nMessaggio: %s\n",
						pacchettoInEntrata.getAddress().toString(),pacchettoInEntrata.getPort(), messaggioRicevuto);
				
				if(rand.nextInt(3) == 0)
				{
					// Scelgo di "far perdere" il pacchetto
					System.out.println("Decido di non inviare il ping");
					continue;
				}
				else
				{
					// Scelgo di ricevere il pacchetto
					long ritardo = (long) rand.nextInt(1999);
					System.out.printf("Decido di ritardare il ping di %d millisecondi\n", ritardo);
					Thread.sleep(ritardo); //Simulo latenza della rete
					DatagramPacket pacchettoInUscita = new DatagramPacket(buffer, pacchettoInEntrata.getLength(),
							pacchettoInEntrata.getAddress(), pacchettoInEntrata.getPort());
					
					sock.send(pacchettoInUscita);
				}	
			}
		}
		catch (NumberFormatException e) { ErroreFatale("La porta deve essere un numero intero!", false); }
		catch (BindException e) { ErroreFatale("La porta inserita (" + portaServer + ") è già usata da un altro servizio, oppure è riservata!", false); }
		catch (SocketException e) { e.printStackTrace(); }
		catch (IOException e) { e.printStackTrace(); }
		catch (InterruptedException e) { e.printStackTrace(); }
		
		System.out.println("[Server] Termino l'esecuzione");
		return;
	}
	
	private static void ErroreFatale(String testo, Boolean stampaSintassi)
	//Funzione di supporto al main, usata per stampare messaggi di debug in caso di errore non risolvibile
	{		
		System.err.println("Errore fatale: " + testo);
		if(stampaSintassi)
			System.err.println("Sintassi: MainClass " + parametri + "\nVedi commento in MainClass.java"); 
		System.exit(1);
	}
	
}
