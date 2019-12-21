import java.io.IOException;
import java.net.BindException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;


/*
Università di Pisa - Laboratorio del corso di Reti di Calcolatori - A.A. 2018-2019
Assignment #8 - NIO FTP
Esercizio svolto singolarmente da Alessandro Antonelli, matricola 507264
*/

/*
			SERVER
			
un server che implementi il servizio di FTP
			
Programma che... TODO  
*/

public class MainClass {
	
	/*
	 *		SINTASSI / PARAMETRI ACCETTATI DAL MAIN:
	 *
	 *	MainClass porta
	 *
	 *		porta	(intero compreso tra 1025 e 65535)	Numero della porta pubblica sulla quale ascoltare le richieste di connessione.
	*/
	public static void main(String[] args) {
		//======= LEGGO I PARAMETRI =======
		
		final int porta;
		
		if(args.length != 1)
		{
			System.out.println("Errore: al programma deve essere passato esattamente un parametro.\n"+
								"Sintassi: MainClass porta\n" +
								"Vedi commento in MainClass.java");
			System.exit(1);
		}
		
		porta = Integer.parseInt(args[1]);
		
		if(porta <= 1024 || porta > 65535)
		{
			System.out.println("Errore: il numero di porta fornito deve essere compreso tra 1025 e 65535!");
			System.exit(1);
		}
		
		//Controlla che alla porta non sia già associato un altro servizio
		try { new ServerSocket(porta); }
		catch (BindException e1)
		{
			System.out.printf("Errore: il numero di porta fornito (%d) è già in uso da un altro processo!\n", porta);
			System.exit(1);
		}
		catch (IOException e) { e.printStackTrace(); }



		
		//======= INIZIO ASCOLTO DELLE RICHIESTE DI CONNESSIONE =======
		try
		{
			ServerSocketChannel ChannelAscolto = ServerSocketChannel.open();
			ChannelAscolto.socket().bind(new InetSocketAddress(porta));
			ChannelAscolto.configureBlocking(false);
		
		
		//======= SERVO LE CONNESSIONI =======
		
			Boolean hoFinito = false;
		
			while(!hoFinito)
			{
				SocketChannel clientSock =  ChannelAscolto.accept();
				
				if(clientSock != null)
				{
					//comunica con clientSock
				}
				else
				{
					//fai altro
				}
			}
		
		}
		catch (IOException e) { e.printStackTrace(); }
		
		
		// quando il server riceve il nome del file, apre un FileChannel per leggere il file
		// quindi registra il canale verso il client con un selettore per inviare il file
		// preleva i dati ricevuti dal FileChannel e li invia al client mediante il SocketChannel
		
		//======= TERMINO =======
		return;
	}

}
