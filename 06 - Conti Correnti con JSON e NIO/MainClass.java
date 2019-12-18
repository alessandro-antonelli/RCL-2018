import java.io.File;
import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/*
Università di Pisa - Laboratorio del corso di Reti di Calcolatori - A.A. 2018-2019
Assignment #6 - Gestione conti correnti con JSON e NIO
Studente Alessandro Antonelli, matricola 507264
*/

/*
Programma per la gestione di conti correnti tramite serializzazione JSON e NewI/O.

Può leggere i conti correnti da un file già esistente (avviando il programma con due parametri)
oppure generando il file casualmente (avviandolo con tre parametri).

I file che contengono la serializzazione dei conti correnti sono costituiti da un oggetto di tipo Integer,
che rappresenta il numero X dei conti correnti contenuti nel file stesso, seguito da X oggetti di tipo ContoCorrente.

 NB: il parametro sulla verbosità delle stampe può essere utilizzato per "rallentare" artificialmente le operazioni di conteggio
 e quindi aumentare la dimensione del pool di thread che vengono avviati.
*/

public class MainClass {

	/*
				SINTASSI
				
	Nella modalità file JSON esstente:
		MainClass path verbosità
		
	 			OPPURE
	Nella modalità file generato casualmente:
		MainClass numeroConti maxMovimenti verbosità
	
	path		(Stringa)	path del file da cui leggere i conti correnti serializzati
	verbosità	(Stringa)	verbosità delle stampe: inserire "si" per effettuare una stampa ogni volta
							che un movimento viene letto e conteggiato; inserire "no" per non effettuarle
							
	numeroConti		(intero strettamente positivo)		Numero di conti correnti da generare nel file casuale
	maxMovimenti	(intero strettamente positivo)		Limite superiore al numero di movimenti da generare per ogni conto corrente
	*/
	public static void main(String[] args) {
		//======= LEGGO I PARAMETRI =======
		if(args.length != 2 && args.length != 3)
		{
			System.out.println("Errore: al programma devono essere passati due parametri oppure tre parametri.");
			System.out.println("Sintassi:\nexec PathFileConti si/no\nOPPURE\nexec NumConti MaxMovimenti si/no\nVedi commento in MainClass.java");
			System.exit(1);
		}
		
		final String pathContiCorrenti;
		final File fileContiCorrenti;
		Boolean stampeMovimenti = false;
		
		if(args.length == 2)
		{
			// Siamo nella modalità in cui il file di input è già esistente
			if(args[1].equals("si")) stampeMovimenti = true;
			else if(args[1].equals("no")) stampeMovimenti = false;
			pathContiCorrenti = args[0];
			fileContiCorrenti = new File(pathContiCorrenti);
			
			if(!fileContiCorrenti.exists())
			{
				System.out.println("Errore: il file passato come parametro non esiste!");
				System.exit(1);
			}
			
			System.out.printf("%s: Programma avviato in modalità \"file esistente\". Rileggo i conti correnti dal file al percorso: %s\n",
					Thread.currentThread().getName(), pathContiCorrenti);
		}
		else
		{
			// Siamo nella modalità in cui il file di input va generato casualmente
			
			final int nConti, MaxMovimenti;
			
			if(args[2].equals("si")) stampeMovimenti = true;
			else if(args[2].equals("no")) stampeMovimenti = false;
			nConti = Integer.parseInt(args[0]);
			MaxMovimenti = Integer.parseInt(args[1]);
			pathContiCorrenti = "/tmp/ContiCorrentiCasuali.json";
			fileContiCorrenti = new File(pathContiCorrenti);
			
			if(nConti <= 0 || MaxMovimenti <= 0)
			{
				System.out.println("Errore: i parametri devono essere interi strettamente positivi. Vedi commento in MainClass.java");
				System.exit(1);
			}
			if(fileContiCorrenti.exists()) fileContiCorrenti.delete();
			try { fileContiCorrenti.createNewFile(); } catch (IOException e) { e.printStackTrace(); }
			
			System.out.printf("%s: Programma avviato in modalità \"input generato casualmente\". Genero %d conti correnti casuali " + 
					"con al più %d movimenti ciascuno.\nIl file sarà generato al percorso: %s\n", Thread.currentThread().getName(),
					nConti, MaxMovimenti, pathContiCorrenti);
		

		//======= GENERO e SERIALIZZO/SCRIVO SU FILE I CONTI CORRENTI =======
			System.out.printf("%s: Avvio la generazione casuale e la serializzazione/scrittura su disco di %d conti correnti\n", Thread.currentThread().getName(), nConti);
			
			GeneratoreConti generatore = new GeneratoreConti(fileContiCorrenti, nConti, MaxMovimenti);
			try { generatore.Genera(); }
			catch (IOException e) { e.printStackTrace(); }
			
			System.out.printf("%s: Tutti i conti correnti sono stati generati e serializzati!\n", Thread.currentThread().getName());
		}
		
		//======= AVVIO THREAD RILETTORE e POOL DI THREAD CONTEGGIATORI =======
		long tempoInizio = System.currentTimeMillis();
		
		ContatoreCausali contatore = new ContatoreCausali();
		//Oggetto condiviso tra i thread rilettori, che mantiene il parziale del conteggio delle causali incontrate finora
		
		ThreadPoolExecutor ThreadPool = (ThreadPoolExecutor) Executors.newCachedThreadPool();
		
		TaskLettura lettura = new TaskLettura(ThreadPool, fileContiCorrenti, contatore, stampeMovimenti);
		Thread lettore = new Thread(lettura);
		lettore.start();
		System.out.printf("%s: Thread lettore avviato!\n", Thread.currentThread().getName());
		
		//======= ATTENDO TERMINAZIONE THREAD =======
		try { lettore.join(); } catch (InterruptedException e) { e.printStackTrace(); }
		System.out.printf("%s: Thread lettore terminato!\n", Thread.currentThread().getName());
		ThreadPool.shutdown();
		while(!ThreadPool.isTerminated())
			try {
				ThreadPool.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
			} catch (InterruptedException e) { e.printStackTrace(); }
		long tempoFine = System.currentTimeMillis();
		System.out.printf("%s: Tutti i thread conteggiatori del pool hanno terminato l'esecuzione!\n", Thread.currentThread().getName());
		
		//======= STAMPO CONTEGGIO FINALE =======
		System.out.printf("%s: Rilettura e conteggio terminata!\n", Thread.currentThread().getName());

		System.out.printf("%s: E' stato ottenuto il seguente conteggio finale:\n", Thread.currentThread().getName());
		System.out.println(contatore.getTotals());
		
		System.out.printf("%s: Il file JSON letto aveva dimensione pari a %.2f kilobyte\n", Thread.currentThread().getName(), fileContiCorrenti.length() / 1024F);
		
		System.out.printf("%s: Il tempo totale impiegato dal parsing è stato pari a %.2f secondi\n", Thread.currentThread().getName(), ((float) (tempoFine - tempoInizio) / 1000));
		
		//======= TERMINO =======
		if(args.length == 3) fileContiCorrenti.delete(); //cancello il file dei conti correnti random generati dal programma 
		return;
	}

}
