import java.io.File;
import java.io.FileNotFoundException;

/*
Università di Pisa - Laboratorio del corso di Reti di Calcolatori - A.A. 2018-2019
Assignment #7 - Web log parsing
Esercizio svolto singolarmente da Alessandro Antonelli, matricola 507264
*/

/*
Programma che prende in input un file di log e lo stampa a schermo, dopo aver sostituito gli indirizzi IP numerici
con il relativo nome simbolico.

I due comportamenti single-threaded e multi-threaded, richiesti dalla consegna, sono implementati e incorporati entrambi
in questo unico programma; la scelta di quale versione eseguire avviene tramite il secondo parametro a linea di comando.

Nel caso di esecuzione multi-threaded, si è optato per un FixedThreadPool con numero di thread fisso e pari a 10;
tale valore può essere cambiato modificando il valore della costante poolSize (definita in MainClass.java).

Nei casi in cui l'indirizzo IP non venga risolto (per "host sconosciuto") oppure che venga risolto ma non viene tradotto
(viene cioè restituito l'IP numerico di input), il programma stampa a schermo la riga originale del log, senza modificarla.
Se l'opzione "verbose" è attivata, il programma stamperà anche una postilla, esplicitando l'esito della risoluzione.  
*/

public class MainClass {

	/*
	 *		SINTASSI / PARAMETRI ACCETTATI DAL MAIN:
	 *
	 *	MainClass logPath IsMultithreaded UseCache verbose
	 *
	 *		logPath		 		(Stringa)	Path del file che contiene il log da analizzare.
	 *		IsMultithreaded		(Stringa)	Inserire "single" per eseguire la versione single-threaded del programma;
	 *										inserire "multi" per eseguire la versione multi-threaded.
	 *		UseCache			(Stringa)	Inserire "si" per usare la cache del supporto Java nelle interrogazioni
	 *										al DNS, "no" per disattivarla.
	 *		verbose				(Stringa)	Inserire "si" per effettuare una stampa per ogni riga del log processata, altrimenti "no".
	*/
	public static void main(String[] args) {
		//======= LEGGO I PARAMETRI =======
		
		if(args.length != 4)
		{
			System.out.println("Errore: al programma devono essere passati esattamente quattro parametri.\n"+
								"Sintassi: MainClass path single/multi si/no si/no\n" +
								"Vedi commento in MainClass.java");
			System.exit(1);
		}
		
		final String logPath;
		final Boolean multithreaded;
		final Boolean verbose;
		
		
		//costante che indica il numero di thread che compongono il pool, quando viene eseguita la versione multi-threaded del programma
		final int poolSize = 10;
		
		
		//Percorso file log
		logPath = args[0];
		
		File logFile = new File(logPath);
		if(!logFile.exists())
		{
			System.out.printf("Errore: non esiste nessun file al percorso specificato! (%s)", logFile.getAbsolutePath());
			System.exit(1);
		}
		if(!logFile.canRead())
		{
			System.out.printf("Errore: non si dispongono dei permessi necessari per leggere il file al percorso specificato! (%s)",
																		logFile.getAbsolutePath());
			System.exit(1);
		}
		
		
		//Single vs multi threaded
		if(args[1].equals("single"))
		{
			multithreaded = false;
			System.out.printf("%s: programma avviato in modalità single-threaded!\n", Thread.currentThread().getName());
		}
		else
		{
			if(args[1].equals("multi"))
			{
				multithreaded = true;
				System.out.printf("%s: programma avviato in modalità multi-threaded, con un pool di %d thread!\n",
																Thread.currentThread().getName(), poolSize);
			}
			else
			{
				System.out.println("Errore: il secondo parametro deve contenere la parola \"single\" oppure \"multi\" (senza apici).\n"+
									"Vedi commento in MainClass.java");
				multithreaded = false;
				System.exit(1);
			}
		}
		
		//Cache DNS
		if(args[2].equals("si"))
		{
			//Imposto la validità di ogni entry nella cache DNS a 200 secondi
			java.security.Security.setProperty("networkaddress.cache.ttl", "200");
			java.security.Security.setProperty("networkaddress.cache.negative.ttl", "200");
		}
		else
		{
			if(args[2].equals("no"))
			{
				//Disattivo la cache DNS
				java.security.Security.setProperty("networkaddress.cache.ttl", "0");
				java.security.Security.setProperty("networkaddress.cache.negative.ttl", "0");
			}
			else
			{
				System.out.println("Errore: il terzo parametro deve contenere la parola \"si\" oppure \"no\" (senza apici).\n"+
									"Vedi commento in MainClass.java");
				System.exit(1);
			}
		}
		
		//verbosità
		if(args[3].equals("si"))
		{
			verbose = true;
		}
		else
		{
			if(args[3].equals("no"))
			{
				verbose = false;
			}
			else
			{
				System.out.println("Errore: il quarto parametro deve contenere la parola \"si\" oppure \"no\" (senza apici).\n"+
									"Vedi commento in MainClass.java");
				verbose = false;
				System.exit(1);
			}
		}
		
		
		//======= ESEGUO LA LETTURA DEL LOG E LA RISOLUZIONE DEGLI INDIRIZZI =======
		LogParser parser = new LogParser(logFile, multithreaded, poolSize, verbose);
		
		long tempoInizio = System.currentTimeMillis();
		//La chiamata al metodo avvia() include l'attesa che l'operazione di parsing sia terminata (sia nel caso single che multi threaded)
		try { parser.avvia(); } catch (FileNotFoundException e) { e.printStackTrace(); }
		
		//======= TERMINO =======
		long tempoFine = System.currentTimeMillis();
		
		System.out.printf("\n\n\n%s: Lettura del log e risoluzione degli indirizzi terminata!\n", Thread.currentThread().getName());
		
		System.out.printf("%s: Il tempo totale impiegato è stato pari a %.2f secondi\n", Thread.currentThread().getName(),
																			((float) (tempoFine - tempoInizio) / 1000));
		
		StringBuilder StampaImpostazioni = new StringBuilder();
		StampaImpostazioni.append("[Programma eseguito ");
		
		if(args[2].equals("si")) StampaImpostazioni.append("con caching attivato, ");
		else StampaImpostazioni.append("con caching disattivato, ");
		
		if(multithreaded == true) StampaImpostazioni.append("in versione multi-threaded con dimensione pool pari a " + poolSize + "]");
							else StampaImpostazioni.append("in versione single-threaded]");
		
		System.out.println(StampaImpostazioni);
		return;
	}

}
