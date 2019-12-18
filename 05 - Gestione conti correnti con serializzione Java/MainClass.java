import java.io.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/*
Università di Pisa - Laboratorio del corso di Reti di Calcolatori - A.A. 2018-2019
Assignment #5 - Gestione conti correnti
Studente Alessandro Antonelli, matricola 507264
*/

/*
Programma per la gestione di conti correnti tramite serializzazione JAVA.

Può leggere i conti correnti da un file già esistente (avviando il programma con due parametri)
oppure generando il file casualmente (avviandolo con tre parametri).

I file che contengono la serializzazione dei conti correnti sono costituiti da un oggetto di tipo Integer,
che rappresenta il numero X dei conti correnti contenuti nel file stesso, seguito da X oggetti di tipo ContoCorrente.

 NB: il parametro sulla verbosità delle stampe può essere utilizzato per "rallentare" artificialmente le operazioni di conteggio
 e quindi aumentare la dimensione del pool di thread che vengono avviati. 
*/

public class MainClass {

	// Accetta i seguenti parametri da riga di comando:
	//NELLA MODALITA' FILE ESISTENTE:
	// 1) path del file da cui leggere i conti correnti serializzati (Stringa)
	// 2) verbosità delle stampe: inserire "si" per effettuare una stampa ogni volta che un movimento viene letto e conteggiato;
	//		inserire "no" per non effettuarle
	//IN ALTERNATIVA, NELLA MODALITA' FILE GENERATO CASUALMENTE:
	// 1) Numero di conti correnti da generare nel file casuale (intero strettamente positivo)
	// 2) Limite superiore al numero di movimenti da generare per ogni conto corrente (intero strettamente positivo)
	// 3) verbosità delle stampe: inserire "si" per effettuare una stampa ogni volta che un movimento viene letto e conteggiato;
	//		inserire "no" per non effettuarle
	public static void main(String[] args)
	{
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
			pathContiCorrenti = "/tmp/ContiCorrentiCasuali";
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
			
			Random rand = new Random();
			DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
			
			//Elenco di nomi e cognomi con cui generare intestatari dei conti casuali
			String[] NomiItaliani = {"Antonio", "Davide", "Edoardo", "Federico", "Giuseppe", "Leonardo", "Mario", "Nicola", "Roberto", "Samuele", "Tommaso", "Valerio"};
			String[] CognomiItaliani = {"Amato", "Bianchi", "Colombo", "De Luca", "Esposito", "Ferrari", "Greco", "Lombardi", "Marino", "Parisi", "Rossi", "Santoro", "Testa", "Verdi"};
			
			try (FileOutputStream stream = new FileOutputStream(fileContiCorrenti))
			{
				ObjectOutputStream output = new ObjectOutputStream(stream);

				output.writeObject(new Integer(nConti));
				for(int i = 0; i<nConti; i++)
				{
					String IntestatarioCasuale = NomiItaliani[rand.nextInt(NomiItaliani.length)] + ' ' + CognomiItaliani[rand.nextInt(CognomiItaliani.length)];
					ContoCorrente ContoCasuale = new ContoCorrente(IntestatarioCasuale);

					int nMovimenti = rand.nextInt(MaxMovimenti) + 1;
					System.out.printf("%s: Inizia la generazione casuale del conto %d su %d, con all'interno %d movimenti\n",
							Thread.currentThread().getName(), i+1, nConti, nMovimenti);
					for(int j=0; j<nMovimenti; j++)
					{
						MovimentoCC.Causale CausaleCasuale = MovimentoCC.Causale.values()[rand.nextInt(5)];
						Date DataCasuale = new Date();
						try { DataCasuale = dateFormat.parse(rand.nextInt(31) + "/" + rand.nextInt(12) + "/" + (1850 + rand.nextInt(168)) +
								" " + rand.nextInt(23) + ":" + rand.nextInt(59) + ":" + rand.nextInt(59)); }
						catch (ParseException e) { e.printStackTrace(); }
						
						MovimentoCC movimento = new MovimentoCC(DataCasuale, CausaleCasuale);
						ContoCasuale.AddMovimento(movimento);
					}
					
					System.out.printf("%s: Inizia la scrittura su disco del conto %d su %d, intestato a %s\n", Thread.currentThread().getName(), i+1, nConti, ContoCasuale.getIntestatario());
					try { output.writeObject(ContoCasuale);	}
					catch (java.lang.OutOfMemoryError e)
					{
						System.out.printf("%s: Errore! Memoria heap insufficiente alla serializzazione dei conti correnti casuali. Riduci il numero di conti correnti da generare e/o il limite dei movimenti!\n", Thread.currentThread().getName());
						System.exit(1);
					}
				}
				
				output.close();
				stream.close();
			}
			catch(FileNotFoundException e) { e.printStackTrace(); }
			catch(IOException e) { e.printStackTrace(); }
			
			System.out.printf("%s: Tutti i conti correnti sono stati generati e serializzati!\n", Thread.currentThread().getName());
		}
		
		//======= AVVIO THREAD RILETTORE e POOL DI THREAD CONTEGGIATORI =======
		
		//un thread legge dal file gli oggetti "conto corrente"
		
		//Un pool di thread riceve i task dal lettore e conteggia quante volte ogni causale è stata utilizzata nel conto corrente.
		//Alla fine del conto corrente, aggiunge i valori parziali al contatore globale.

		ContatoreCausali contatore = new ContatoreCausali();
		
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
		System.out.printf("%s: Tutti i thread conteggiatori del pool hanno terminato l'esecuzione!\n", Thread.currentThread().getName());
		
		//======= STAMPO CONTEGGIO FINALE =======
		System.out.printf("%s: Rilettura e conteggio terminata!\n", Thread.currentThread().getName());

		System.out.printf("%s: E' stato ottenuto il seguente conteggio finale:\n", Thread.currentThread().getName());
		System.out.println(contatore.getTotals());
		
		//======= TERMINO =======
		if(args.length == 3) fileContiCorrenti.delete();
		return;
	}
}
