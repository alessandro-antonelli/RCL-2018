import java.io.IOException;
import java.io.InputStreamReader;
import java.net.BindException;
import java.net.ServerSocket;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.*;

/*
Università di Pisa - Laboratorio del corso di Reti di Calcolatori - A.A. 2018-2019
Assignment #10 - Gestione congresso con NIO
Esercizio svolto singolarmente da Alessandro Antonelli, matricola 507264
*/

/*
Il programma costituisce il server di un'applicazione client/server per la gestione di registrazioni di interventi
ad un congresso.

Il programma crea un'istanza dell'oggetto remoto ImplementazioneCongressoServer, che implementa l'interfaccia InterfacciaCongresso
e realizza il servizio di gestione delle registrazioni degli interventi. Dopodiché lo esporta al numero di porta specificato
da riga di comando, e viene implicitamente avviato il thread che ascolta le invocazioni di metodi remoti.

La terminazione del server avviene su richesta dell'utente (il main attende la pressione di un tasto qualunque).
*/

public class MainClass {
	
	final static private String parametri = "porta";
	
	/*
	 *		SINTASSI / PARAMETRI ACCETTATI DAL MAIN:
	 *
	 *	MainClass porta
	 *
	 *		porta		(int)		Numero di porta sulla quale il server deve offrire il servizio.
	*/
	public static void main(String[] args) {
		//======= LEGGO I PARAMETRI =======
		
		if(args.length != 1) ErroreFatale("al programma deve essere passato esattamente un parametro!", true);
		final int porta = Integer.parseInt(args[0]);
		
		if(porta <= 1024 || porta > 65535) ErroreFatale("il numero di porta fornito deve essere compreso tra 1025 e 65535!", false);
		
		//Controlla che alla porta non sia già associato un altro servizio
		try { new ServerSocket(porta); }
		catch (BindException e) { ErroreFatale("il numero di porta fornito (" + porta + ") è già in uso da un altro processo!", false); }
		catch (IOException e) { e.printStackTrace(); }
		
		//======= AVVIO SERVIZIO REMOTO =======
		System.out.println("Server: avviato, tento di istituire il servizio sulla porta " + porta);
		try {
			//Creo un'istanza dell'oggetto remoto
			ImplementazioneCongressoServer servizio = new ImplementazioneCongressoServer(3, 12, 5);
			
			//Esporto l'oggetto remoto (tramite porta standard RMI)
			InterfacciaCongresso stub = (InterfacciaCongresso) UnicastRemoteObject.exportObject(servizio, 0);
		
			//Creo un registry sulla porta
			LocateRegistry.createRegistry(porta);
			Registry r = LocateRegistry.getRegistry(porta);
		
			//Pubblico lo stub nel registry
			r.rebind("Server gestione congresso", stub);
			
			System.out.println("Server: avvio del servizio riuscito! Sono pronto a ricevere connessioni!");
		
		//======= TERMINO =======
			System.out.println("Server: premi un tasto qualunque per terminare l'esecuzione del server");
		
			//Mi blocco fino alla pressione di un tasto qualunque
			InputStreamReader terminale = new InputStreamReader(System.in);
			try { terminale.read();	terminale.close(); } catch (IOException e) { e.printStackTrace(); }
					
			UnicastRemoteObject.unexportObject(servizio, true);
			System.out.println("Server: esecuzione terminata");
		}
		catch (RemoteException e) { System.err.println("Errore di comunicazione: " + e.toString()); }
		
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
