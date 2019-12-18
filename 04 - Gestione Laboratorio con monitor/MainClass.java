/*
Università di Pisa - Laboratorio del corso di Reti di Calcolatori - A.A. 2018-2019
Assignment #4 - Gestione Laboratorio con monitor
Studente Alessandro Antonelli, matricola 507264
*/


/*
Programma per la gestione di un laboratorio di informatica. Implementazione con lock implicite e monitor.

Gli utenti sono stati implementati tramite thread, mentre il gestore del laboratorio è stato implementato tramite un monitor
(istanza della classe LabMonitor), un oggetto che viene condiviso tra tutti i thread utenti.

Per dare priorità ai tesisti rispetto agli studenti, la classe monitor tiene conto dei PC sui quali dei tesisti hanno avanzato
richieste, e la procedura di acquisizione dei PC è stata scritta in modo da non assegnare mai a uno studente un PC che un tesista
sta aspettando di utilizzare; se tutti i PC sono occupati o richiesti da tesisti, lo studente si sospende con una wait().

Per evitare lo starving dei professori, è stato inserita nella classe monitor una variabile "ProfInAttesa" e la procedura ChiediUsoPC
è stata scritta in modo che, quando ProfInAttesa>0, tutte le richieste dei thread studenti o tesisti vengano sospese (in modo che,
alla terminazione degli utenti già dentro, il laboratorio rimanga vuoto e possa essere usato dai prof).

Se attivato (tramite l'ottava opzione da riga di comando), il programma stampa periodicamente delle statistiche
sullo stato attuale dei thread. Il significato delle abbreviazioni è:
U = sta usando il laboratorio. P = in pausa. C = in coda per usare il laboratorio. X = terminato.
 */

public class MainClass {

	// Accetta i seguenti parametri da riga di comando:
	// 1) numero di studenti che utilizzano il lab (intero positivo o nullo)
	// 2) numero di tesisti che utilizzano il lab (intero positivo o nullo)
	// 3) numero di professori che utilizzano il lab (intero positivo o nullo)
	// 4) numero di PC nel laboratorio (intero positivo)
	// 5) Limite superiore alla scelta casuale di k (il numero di volte che un utente utilizza il lab)
	// 6) Limite superiore al tempo di utilizzo di un PC del laboratorio da parte di un utente (millisecondi)
	// 7) Limite superiore all'attesa casuale tra un utilizzo e l'altro del laboratorio da parte di un utente (millisecondi)
	// 8) Quanto spesso mostrare la stampa delle statistiche (in millisecondi). 0 = non stampa mai le statistiche.
	public static void main(String[] args)
	{
		//======= LEGGO I PARAMETRI =======
		if(args.length != 8)
		{
			System.out.println("Errore: al programma devono essere passati esattamente otto parametri. Vedi commento in MainClass.java");
			System.exit(1);
		}
				
		final int NumStudenti, NumTesisti, NumProfessori, NumPC, MaxAttesa, MaxUtilizzo, MaxNumAccessi, IntervalloStat;
		NumStudenti = Integer.parseInt(args[0]);
		NumTesisti = Integer.parseInt(args[1]);
		NumProfessori = Integer.parseInt(args[2]);
		NumPC = Integer.parseInt(args[3]);
		MaxNumAccessi = Integer.parseInt(args[4]);
		MaxUtilizzo = Integer.parseInt(args[5]);
		MaxAttesa = Integer.parseInt(args[6]);
		IntervalloStat = Integer.parseInt(args[7]);
				
		if (NumStudenti < 0 || NumTesisti < 0 || NumProfessori < 0 || NumPC <= 0 || MaxNumAccessi <= 0 || MaxUtilizzo <= 0 || MaxAttesa <= 0 || IntervalloStat < 0)
		{
			System.out.println("Errore: i parametri devono essere interi positivi. Vedi commento in MainClass.java");
			System.exit(1);
		}
				

		//======= AVVIO SIMULAZIONE =======
		System.out.printf("%s: Avvio una simulazione di laboratorio con %d PC, %d studenti, %d tesisti e %d professori.\n" +
		"Il numero massimo di utilizzi è %d, la durata massima di ciascuno è %.2f secondi, e la pausa massima tra due utilizzi è %.2f secondi.\n",
		Thread.currentThread().getName(), NumPC, NumStudenti, NumTesisti, NumProfessori, MaxNumAccessi, MaxUtilizzo/1000f, MaxAttesa/1000f);
				
		//creo l'oggetto monitor del laboratorio
		LabMonitor monitor = new LabMonitor(NumPC);
		
		//creo i task degli utenti e attivo i relativi thread
		System.out.printf("%s: Avvio i thread degli utenti!\n", Thread.currentThread().getName());
		
		//vettore che contiene i puntatori ai thread studenti (serve per attendere la loro terminazione)
		Thread ThreadPointers[] = new Thread[NumStudenti+NumTesisti+NumProfessori];
		//Vettore che contiene lo stato di ogni thread. Usato per stampare statistiche in tempo reale.
		//U = sta usando il laboratorio. P = in pausa. C = in coda per usare il lab. X = terminato.
		char ThreadStatus[] = new char[NumStudenti+NumTesisti+NumProfessori];
		
		for(int i = 0; i<NumStudenti; i++)
		{
			LabUser studente = new LabUser(NumPC, LabUser.UserType.Studente, i, monitor, MaxNumAccessi, MaxAttesa, MaxUtilizzo, ThreadStatus, i);
			Thread t = new Thread(studente);
			ThreadPointers[i] = t;
			t.start();
		}
		
		for(int i = 0; i<NumTesisti; i++)
		{
			LabUser tesista = new LabUser(NumPC, LabUser.UserType.Tesista, i, monitor, MaxNumAccessi, MaxAttesa, MaxUtilizzo, ThreadStatus, i+NumStudenti);
			Thread t = new Thread(tesista);
			ThreadPointers[NumStudenti+i] = t;
			t.start();
		}
		
		for(int i = 0; i<NumProfessori; i++)
		{
			LabUser professore = new LabUser(NumPC, LabUser.UserType.Professore, i, monitor, MaxNumAccessi, MaxAttesa, MaxUtilizzo, ThreadStatus, i+NumStudenti+NumTesisti);
			Thread t = new Thread(professore);
			ThreadPointers[NumStudenti+NumTesisti+i] = t;
			t.start();
		}  
		
		
		//======= ATTENDO TERMINAZIONE =======
		for(int i=0; i<ThreadPointers.length; i++)
		{
			while(ThreadPointers[i].isAlive())
			{
				if(IntervalloStat != 0)
				{// Stampa statistiche attiva
					try {
						ThreadPointers[i].join(IntervalloStat);
					} catch (InterruptedException e) { e.printStackTrace();	}
							
					StampaStato(ThreadPointers, NumStudenti, NumTesisti, ThreadStatus);	
				}
				else
				{// Stampa statistiche NON attiva
					try {
						ThreadPointers[i].join();
					} catch (InterruptedException e) { e.printStackTrace();	}
				}
			}
		}
		
		System.out.printf("%s: Tutti i %d utenti hanno terminato il loro utilizzo del laboratorio. Il programma termina!\n",
													Thread.currentThread().getName(), ThreadPointers.length);
		
		return;
	}
	
	
	// Procedura ausiliare per la stampa periodica delle statistiche (invocata dal main)
	public static void StampaStato(Thread[] ThreadPointers, int NumStudenti, int NumTesisti, char[] ThreadStatus)
	{
		int StudEsec = 0, StudTerm = 0, TesisEsec = 0, TesisTerm = 0, ProfEsec = 0, ProfTerm = 0;
		
		for (int i=0; i<NumStudenti; i++)
		{
			if(ThreadPointers[i].isAlive()) StudEsec++;
			else StudTerm++;
		}
		for (int i=NumStudenti; i<(NumStudenti+NumTesisti); i++)
		{
			if(ThreadPointers[i].isAlive()) TesisEsec++;
			else TesisTerm++;
		}
		for (int i=NumStudenti+NumTesisti; i<ThreadPointers.length; i++)
		{
			if(ThreadPointers[i].isAlive()) ProfEsec++;
			else ProfTerm++;
		}
		
		System.out.printf("%s: *****Statistiche: %d studenti attivi (%d terminati), %d tesisti attivi (%d terminati), " + 
		"%d prof attivi (%d terminati)*****\n", Thread.currentThread().getName(), StudEsec, StudTerm,
		TesisEsec, TesisTerm, ProfEsec, ProfTerm);
		
		String StringaStato = "Stato thread: ";
		for(int i=0; i<ThreadStatus.length; i++)
			StringaStato = StringaStato + i + " " + ThreadStatus[i] + "|";
		System.out.printf("%s: *****%s*****\n", Thread.currentThread().getName(), StringaStato);
	}
}
