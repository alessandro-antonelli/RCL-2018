import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;


/*
Università di Pisa - Laboratorio del corso di Reti di Calcolatori - A.A. 2018-2019
Assignment #8 - NIO FTP
Esercizio svolto singolarmente da Alessandro Antonelli, matricola 507264
*/

/*
 		CLIENT
 		
un client che si connette al server per scaricare uno o più files
Programma che... TODO  
*/

public class MainClass {
	
	/*
	 *		SINTASSI / PARAMETRI ACCETTATI DAL MAIN:
	 *
	 *	MainClass indirizzo porta nomeFile
	 *
	 *		indirizzo	(String)	Indirizzo IP del server FTP al quale connettersi, in formato "dotted quad".
	 *		porta		(int)		Numero di porta del server FTP al quale connettersi.
	 *		nomeFile	(String)	Nome del file da richiedere al server FTP.
	*/
	public static void main(String[] args) {
		//======= LEGGO I PARAMETRI =======
		
		if(args.length != 3)
		{
			System.out.println("Errore: al programma devono essere passati esattamente tre parametri.\n"+
								"Sintassi: MainClass indirizzo porta nomeFile\n" +
								"Vedi commento in MainClass.java");
			System.exit(1);
		}
		
		final String indirizzo = args[0];
		final int porta = Integer.parseInt(args[1]);
		final String nomeFile = args[2];
		
		if(indirizzo.isEmpty())
		{
			System.out.println("Errore: l'indirizzo IP del server non può essere vuoto!");
			System.exit(1);
		}
		if(porta < 0 || porta > 65535)
		{
			System.out.println("Errore: il numero di porta deve essere compreso tra 0 e 65535!");
			System.exit(1);
		}
		if(nomeFile.isEmpty())
		{
			System.out.println("Errore: il nome del file non può essere vuoto!");
			System.exit(1);
		}
		//TODO mancano altri controlli sull'esistenza del file, e della coppia <IP server, porta server>
		
		
		//======= MI CONNETTO AL SERVER =======
		// il client si connette al server mediante un SocketChannel ed invia il nome di un file
		
		try
		{
			SocketChannel sock = SocketChannel.open();
			sock.connect(new InetSocketAddress(indirizzo, porta));
			sock.configureBlocking(false);
		}
		catch (IOException e) { e.printStackTrace(); }
		
		//======= FACCIO LA RICHIESTA =======
		
		//======= ATTENDO LA RISPOSTA =======
		
		//======= TERMINO =======
		return;
	}

}
