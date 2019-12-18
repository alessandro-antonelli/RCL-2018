import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.io.IOException;
import java.io.InterruptedIOException;

/*
Università di Pisa - Laboratorio del corso di Reti di Calcolatori - A.A. 2018-2019
Assignment #9 - UDP Ping
Esercizio svolto singolarmente da Alessandro Antonelli, matricola 507264
*/

/*
Il programma costituisce il client di un'utility Ping che utilizza una connessione UDP.
*/

public class MainClass {
	
	final static private String parametri = "nomeServer portaServer";
	
	/*
	 *		SINTASSI / PARAMETRI ACCETTATI DAL MAIN:
	 *
	 *	MainClass nomeServer portaServer
	 *
	 *		nomeServer		(Stringa)		Nome simbolico o indirizzo IP del server ping da contattare
	 *		portaServer		(int)			Numero di porta del server ping da contattare
	*/

	public static void main(String[] args) {
		//======= LEGGO I PARAMETRI =======
		if(args.length != 2) ErroreFatale("al programma devono essere passati esattamente due parametri!", true);
		
		final String nomeServer = args[0];
		
		try
		{
			// Controllo che il nome/indirizzo del server sia valido
			if(nomeServer == null || nomeServer.isEmpty()) ErroreFatale("Il nome/indirizzo del server non può essere vuoto!", true);
			InetAddress indirizzoServer = InetAddress.getByName(nomeServer);
		
			// Controllo che la porta sia valida
			final int portaServer = Integer.parseInt(args[1]);
			if(portaServer < 0 || portaServer > 65535) ErroreFatale("La porta deve essere compresa tra 0 e 65535!", false);
			
			//======= INVIO I PING E ASPETTO L'ECO =======
			long tempoRTT[] = new long[10];
			long invioPing, ricezioneEco;
			
			DatagramSocket sock = new DatagramSocket();
			sock.setSoTimeout(2000);
			DatagramPacket pacchettoInUscita = null;
			byte[] buffer = new byte[80];
			DatagramPacket pacchettoInIngresso = new DatagramPacket(buffer, buffer.length);
			
			for(int seqno=0; seqno<10; seqno++)
			{
				String messaggioInUscita = "PING " + seqno + " " + System.currentTimeMillis();
				
				if(pacchettoInUscita == null)
					pacchettoInUscita = new DatagramPacket(messaggioInUscita.getBytes(), messaggioInUscita.length(), indirizzoServer, portaServer);
				else
					pacchettoInUscita.setData(messaggioInUscita.getBytes(), 0, messaggioInUscita.length());
				
				sock.send(pacchettoInUscita);
				invioPing = System.currentTimeMillis();
				System.out.println("[Client] " + messaggioInUscita);
				
				try
				{
					sock.receive(pacchettoInIngresso);
					ricezioneEco = System.currentTimeMillis();
					String messaggioInIngresso = new String(buffer, 0, pacchettoInIngresso.getLength());
					if(messaggioInIngresso.equals(messaggioInUscita))
					{
						tempoRTT[seqno] = ricezioneEco - invioPing;
						System.out.println("[Client] ricevuta risposta in " + tempoRTT[seqno] + " millisecondi");
					}
					else ErroreFatale("[Client] C'è qualcun altro che sta mandando al client pacchetti UDP estranei sulla stessa porta!", false);
				}
				catch (InterruptedIOException e)
				{
					tempoRTT[seqno] = -1;
					System.out.println("[Client] *   (timeout scaduto)");
				}
			}
			sock.close();
			
			//======= CALCOLO E STAMPO STATISTICHE =======
			int riusciti = 0;
			float RTTmedio;
			long RTTmax = 0, RTTmin = Long.MAX_VALUE, sommaRTT = 0;
			for(int i=0; i<10; i++)
			{
				if(tempoRTT[i] != -1)
				{
					riusciti++;
					sommaRTT += tempoRTT[i];
					if(tempoRTT[i] > RTTmax) RTTmax = tempoRTT[i];
					if(tempoRTT[i] < RTTmin) RTTmin = tempoRTT[i];
				}
			}
			
			System.out.println("\n---- Statistiche Ping ----");
			System.out.printf("%d pacchetti trasmessi, %d pacchetti ricevuti, %.0f%% di pacchetti persi\n", 10, riusciti, (10-riusciti) / 10f * 100);
			if(riusciti > 0)
			{
				RTTmedio = ((float) sommaRTT) / ((float) riusciti);
				System.out.printf("Round Trip Time (millisecondi): min/medio/max = %d / %.2f / %d", RTTmin, RTTmedio, RTTmax);
			}
			return;
		}
		catch (NumberFormatException e) { ErroreFatale("La porta deve essere un numero intero!", false); }
		catch (UnknownHostException e) { ErroreFatale("Il server specificato (" + nomeServer + ") è inesistente!", false); }
		catch (SocketException e) { e.printStackTrace(); }
		catch (IOException e) { e.printStackTrace(); }
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
