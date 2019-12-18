import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.regex.Pattern;

/*
Università di Pisa - Laboratorio del corso di Reti di Calcolatori - A.A. 2018-2019
Assignment #11 - TimeServer Multicast
Esercizio svolto singolarmente da Alessandro Antonelli, matricola 507264
*/

/*
Il programma costituisce il client di un'applicazione TimeServer.
*/

public class MainClass {

	final static private String parametri = "indirizzoIP";
	
	/*
	 *		SINTASSI / PARAMETRI ACCETTATI DAL MAIN:
	 *
	 *	MainClass indirizzoIP
	 *
	 *		indirizzoIP		(Stringa)		Indirizzo IP del gruppo di multicast dategroup dal quale ricevere la data e l'ora.
	 *										Può essere un indirizzo IPv4 o IPv6, ma deve appartenere alla classe di indirizzi
	 *										riservata al multicast.
	*/
	public static void main(String[] args) {
		//======= LEGGO I PARAMETRI =======
		if(args.length != 1) ErroreFatale("al programma deve essere passato esattamente un parametro!", true);
		
		final String indirizzoIP = args[0];

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
		
		//======= IMPOSTO IL DATEGROUP =======
		System.out.println("Client: avviato. Mi unisco al dategroup all'indirizzo " + indirizzoIP);
		
		try
		{
			final int port = 6789;
			MulticastSocket sock = new MulticastSocket(port);
			sock.setReuseAddress(true);
			InetAddress addr = InetAddress.getByName(indirizzoIP);
			sock.joinGroup(addr);
			System.out.println("Client: inizio l'ascolto dei dieci messaggi con data e ora");
			
		//======= RICEVO E STAMPO LA DATA E L'ORA =======
			byte[] buf = new byte[1024];
			DatagramPacket pacchetto = new DatagramPacket(buf, buf.length);
			
			for(int i=0; i<10; i++)
			{
				sock.receive(pacchetto);
				String messaggio = new String(pacchetto.getData(), pacchetto.getOffset(), pacchetto.getLength());
				System.out.println("Client: ricevuto il messaggio numero " + (i+1) + "\nContenuto: " + messaggio);
			}
			
			sock.close();
		}
		catch(IOException e) { e.printStackTrace(); }
		
		//======= TERMINO =======
		System.out.println("Client: ricevuti tutti i dieci messaggi. L'esecuzione termina");
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
