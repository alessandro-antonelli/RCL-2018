import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.rmi.*;
import java.rmi.registry.*;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

/*
Università di Pisa - Laboratorio del corso di Reti di Calcolatori - A.A. 2018-2019
Assignment #10 - Gestione congresso con NIO
Esercizio svolto singolarmente da Alessandro Antonelli, matricola 507264
*/

/*
Il programma costituisce il client di un'applicazione client/server per la gestione di registrazioni di interventi
ad un congresso. Si connette al server, cercando il servizio di gestione delle registrazioni degli interventi al
numero di porta specificato da riga di comando, e ottiene un'istanza dell'oggetto remoto che implementa l'interfaccia
InterfacciaCongresso.
Dopodiché entra in un ciclo infinito in cui attende l'interazione dell'utente via riga di comando: opportuni comandi
permettono di registrare uno speaker, visualizzare il programma o terminare l'esecuzione del client.
*/

public class MainClass {

	final static private String parametri = "porta";
	
	/*
	 *		SINTASSI / PARAMETRI ACCETTATI DAL MAIN:
	 *
	 *	MainClass porta
	 *
	 *		porta		(int)		Numero della porta del server alla quale è associato il servizio Congresso, al quale ci si vuole connettere.
	*/
	
	public static void main(String[] args) {
		//======= LEGGO I PARAMETRI =======

		if(args.length != 1) ErroreFatale("al programma deve essere passato esattamente un parametro!", true);
		final int porta = Integer.parseInt(args[0]);
		
		if(porta <= 1024 || porta > 65535) ErroreFatale("il numero di porta fornito deve essere compreso tra 1025 e 65535!", false);			
		
		//======= MI CONNETTO AL SERVER =======
		System.out.println("Client: avvato. Tento connessione al server sulla porta " + porta);
		Remote remoteObject;
		try {
			Registry r = LocateRegistry.getRegistry(porta);
			remoteObject = r.lookup("Server gestione congresso");
			InterfacciaCongresso servizio = (InterfacciaCongresso) remoteObject;
			System.out.println("Client: connessione al server riuscita!");
			
		//======= ESEGUO LE RICHIESTE DELL'UTENTE =======
			Boolean shutDown = false;
			BufferedReader terminale = new BufferedReader(new InputStreamReader(System.in));
			while(!shutDown)
			{
				System.out.println("\nClient: digita il comando relativo all'operazione che vuoi eseguire:\n" +
									"r NomeSpeaker NumGiornata NumSessione <--- Registra uno speaker\n" +
									"s                                     <--- Stampa il programma del congresso\n" +
									"q                                     <--- Termina l'esecuzione del client\n");
				
		        String s = terminale.readLine();
		        if(s == null || s.isEmpty()) { System.err.println("Comando non valido"); continue; }
		        	
		        switch (s.charAt(0))
		        {
		        	case 'r':
		        		{
		        			StringTokenizer tokenizer = new StringTokenizer(s);
		        			try {
		        				tokenizer.nextToken(); //è la 'r' che contraddistingue il comando (non mi interessa, lo so già)
		        				final String nome = tokenizer.nextToken();
		        				final int giornata = Integer.parseInt(tokenizer.nextToken());
		        				final int sessione = Integer.parseInt(tokenizer.nextToken());

		        				servizio.registraSpeaker(giornata, sessione, nome);
		        				System.out.printf("Client: registrazione dello speaker %s per la sessione %d del giorno %d "+
		        												"eseguita con successo!\n", nome, sessione, giornata);
		        			}
		        			catch (NoSuchElementException e) //una nextToken() ha fallito: l'utente ha dimenticato uno dei tre parametri
	        					{ System.err.println("Comando non valido. Devi usare la seguente sintassi:\n"+
	        								"r NomeSpeaker NumeroGiornata NumeroSessione"); continue; }
		        			catch (NumberFormatException e) //una parseInt() ha fallito: l'utente ha digitato cose che non sono numeri
		        				{ System.err.println("Comando non valido:\nNumeroGiornata e NumeroSessione devono "+
		        						"essere numeri interi!\nNomeSpeaker non può contenere spazi!"); continue; }
		        			catch (Exception e) //il server ha restituito errore, per qualche motivo la richiesta non poteva essere soddisfatta
		        				{ System.err.println("Client: errore, il server non ha potuto eseguire "+
		        						"l'operazione richiesta:\n" + e.getMessage()); continue; }
		        			
		        			continue;
		        		}
		        	case 's':
		        		{
		        			System.out.printf(servizio.getProgramma());
		        			continue;
		        		}
		        	case 'q':
		        		{
		        			shutDown = true;
		        			continue;
		        		}
		        	default:
		        		{
		        			System.err.println("Comando non valido");
		        			continue; 
		        		}
		        }
			}
			terminale.close();
		}
		catch (RemoteException e) { System.err.println("Client: errore nella comunicazione con il server!"); e.printStackTrace(); }
		catch (NotBoundException e)
		{ System.err.println("Client: non è stato possibile trovare nel registry il servizio richiesto!"); e.printStackTrace(); }
		catch (IOException e) {	e.printStackTrace(); }
		
		//======= TERMINO =======
		System.out.println("Client: esecuzione terminata");
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
