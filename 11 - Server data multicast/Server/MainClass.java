import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Pattern;
import java.net.DatagramPacket;

/*
Università di Pisa - Laboratorio del corso di Reti di Calcolatori - A.A. 2018-2019
Assignment #11 - TimeServer Multicast
Esercizio svolto singolarmente da Alessandro Antonelli, matricola 507264
*/

/*
Il programma costituisce il server di un'applicazione TimeServer.
*/

public class MainClass {
	
	final static private String parametri = "indirizzoIP";
	
	/*
	 *		SINTASSI / PARAMETRI ACCETTATI DAL MAIN:
	 *
	 *	MainClass indirizzoIP
	 *
	 *		indirizzoIP		(Stringa)		Indirizzo IP del gruppo di multicast dategroup su cui inviare la data e l'ora.
	 *										Può essere un indirizzo IPv4 o IPv6, ma deve appartenere alla classe di indirizzi
	 *										riservata al multicast.
	*/
	public static void main(String[] args) {
		//======= LEGGO I PARAMETRI =======
		if(args.length != 1) ErroreFatale("al programma deve essere passato esattamente un parametro!", true);
		
		final String indirizzoIP = args[0];
		final long intervallo = 3000; //Intervallo di tempo (fisso e sempre uguale) tra un invio della data e il successivo, in millisecondi

		// Controllo che il parametro fornito sia un indirizzo IP ben formato
		if(indirizzoIP == null || indirizzoIP.isEmpty()) ErroreFatale("Il parametro non può essere vuoto!", true);
		
		final String ipv4Pattern = "(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])";
		final String ipv6Pattern = "([0-9a-f]{1,4}:){7}([0-9a-f]){1,4}";
		final Boolean isIPv4 = Pattern.compile(ipv4Pattern, Pattern.CASE_INSENSITIVE).matcher(indirizzoIP).matches();
		final Boolean isIPv6 = Pattern.compile(ipv6Pattern, Pattern.CASE_INSENSITIVE).matcher(indirizzoIP).matches();
		
		if(!isIPv4 && !isIPv6) ErroreFatale(indirizzoIP + " non è un indirizzo IP versione 4 o versione 6 ben formato!", true);
		
		// Controllo che l'indirizzo IP fornito sia un indirizzo riservato al multicast
		if(isIPv4) try
		{
			final int primoOttetto = Integer.parseInt(indirizzoIP.split("\\.")[0]);
			if(primoOttetto < 224 || primoOttetto > 239)
				ErroreFatale("L'indirizzo IPv4 fornito (" + indirizzoIP + ") non è un indirizzo riservato al multicast!\n"+
							"Inserire un indirizzo di classe D (224.x.x.x - 239.x.x.x)", false);
		}
		catch (ArrayIndexOutOfBoundsException e) { ErroreFatale(indirizzoIP + " non è un indirizzo IP ben formato!", false); }
		catch (NumberFormatException e) { ErroreFatale(indirizzoIP + " non è un indirizzo IP ben formato!", false); }
		
		if(isIPv6)
			if(indirizzoIP.toLowerCase().charAt(0) != 'f' || indirizzoIP.toLowerCase().charAt(1) != 'f')
				ErroreFatale("L'indirizzo IPv6 fornito (" + indirizzoIP + ") non è un indirizzo riservato al multicast!\n"+
							"Inserire un indirizzo multicast (FF...)", false);
		
		//======= IMPOSTO GRUPPO MULTICAST =======
		System.out.println("Server: avviato. Il messaggio sarà inviato in multicast sull'indirizzo " + indirizzoIP +
							" ogni " + intervallo + " millisecondi\n" +
					"        Per terminare l'esecuzione del TimeServer, premi un tasto qualunque");
		
		try
		{
			InetAddress addr = InetAddress.getByName(indirizzoIP);
			final int port = 6789;
			DatagramSocket sock = new DatagramSocket(port);
			sock.setReuseAddress(true);
	
			InputStreamReader terminale = new InputStreamReader(System.in); //necessario per intercettare il comando di terminazione del server
			Boolean shutDown = false;
			byte[] dati;

		//======= INVIO I MESSAGGI SUL GRUPPO MULTICAST =======
			while(!shutDown)
			{
				String DataEOra = new SimpleDateFormat("HH:mm:ss dd/MM/yyyy").format(new Date());
				dati = DataEOra.getBytes();

				DatagramPacket pacchetto = new DatagramPacket(dati, dati.length, addr, port);
				
				sock.send(pacchetto);
				System.out.println("Inviato il messaggio: " + DataEOra);
			
				// Attendo un intervallo di tempo fisso e sempre uguale tra un invio e il successivo
				try { Thread.sleep(intervallo); } catch (InterruptedException e) { e.printStackTrace(); }

				// Se l'utente ha premuto un tasto qualunque, esco dal ciclo e termino
				try { if (terminale.ready()) shutDown = true; }
				catch (IOException e) { e.printStackTrace(); }
			}
			
			sock.close();
			terminale.close();
		}
		catch (UnknownHostException e) { e.printStackTrace(); }
		catch (SocketException e) { e.printStackTrace(); }
		catch (IOException e) { e.printStackTrace(); }
		
		//======= TERMINO =======
		System.out.println("Server: esecuzione terminata");
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
