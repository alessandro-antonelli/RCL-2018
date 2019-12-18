import java.util.Random;

public class LabUser implements Runnable {
	
	private UserType TipoUtente = null; // Indica se il task rappresenta studente, tesista o prof
	
	private int ID; //Indice del thread utente
	
	// Indice del PC del laboratorio assegnato all'utente.
	// Per gli studenti è impostato dal tutor (al momento della concessione), per i tesisti dall'utente. Per i professori non è significativo.
	private int IndPC = -1;
	
	//E' il valore di k, cioè il numero di accessi al lab da eseguire
	private int NumAccessi;
	
	//Limite superiore alla scelta casuale del tempo da attendere tra un accesso e l'altro (millisecondi)
	private int MaxAttesa;
	
	//Limite superiore alla scelta causale del tempo di utilizzo di un PC del laboratorio (millisecondi)
	private int MaxUtilizzo;
	
	private LabMonitor monitor;
	
	private char[] ThreadStatus;
	private int IndStatus;
	
	public enum UserType
	{
		  Studente,
		  Tesista,
		  Professore
	}

	@Override
	public void run() {
		if(TipoUtente != UserType.Tesista)
			Thread.currentThread().setName(TipoUtente.toString() + ID);
		else
			Thread.currentThread().setName(TipoUtente.toString() + ID + " (suo PC è " + IndPC + ")");
		Random rand = new Random();
		
		for (int i=0; i<NumAccessi; i++)
		{
			// Chiedo l'uso del lab e attendo che mi venga concesso
			ThreadStatus[IndStatus] = 'C';
			System.out.printf("%s: mi metto in coda per usare il laboratorio per la %d° volta su %d\n",
													Thread.currentThread().getName(), i+1, NumAccessi);
			monitor.ChiediUsoPC(this);
			
			// Uso il lab per il tempo che mi è necessario
			ThreadStatus[IndStatus] = 'U';
			int casuale = rand.nextInt(MaxUtilizzo) + 1;
			
			if(TipoUtente != UserType.Professore)
				System.out.printf("%s: ho occupato il PC n. %d, mi serve per %.2f secondi\n",
						Thread.currentThread().getName(), IndPC, casuale/1000f);
			else
				System.out.printf("%s: ho occupato l'intero laboratorio, mi serve per %.2f secondi\n",
						Thread.currentThread().getName(), casuale/1000f);
			
			try { Thread.sleep(casuale); }
				catch (InterruptedException e) { e.printStackTrace(); }
			
			// Rilascio il laboratorio
			ThreadStatus[IndStatus] = 'P';
			monitor.RilasciaPC(this);
			
			if(TipoUtente != UserType.Professore)
				System.out.printf("%s: rilascio il PC n. %d\n", Thread.currentThread().getName(), IndPC);
			else
				System.out.printf("%s: rilascio l'intero laboratorio\n", Thread.currentThread().getName());
			
			// Faccio pausa prima del successivo utilizzo (solo se non è l'ultima iterazione)
			if(i != NumAccessi-1)
				try { Thread.sleep(rand.nextInt(MaxAttesa) + 1);	}
					catch (InterruptedException e) { e.printStackTrace(); }
		}
		
		ThreadStatus[IndStatus] = 'X';
		System.out.printf("%s: ho terminato tutti i %d accessi al laboratorio. Il thread termina\n", Thread.currentThread().getName(), NumAccessi);
	}
	
	public LabUser(int NumPC, UserType tipo, int ID, LabMonitor monitor, int MaxAccessi, int MaxAttesa, int MaxUtilizzo, char[] ThreadStatus, int IndStatus)
	{
		if(tipo == null) throw new NullPointerException();
		if (MaxAccessi <= 0 || MaxAttesa <= 0 || MaxUtilizzo <= 0) throw new IllegalArgumentException();
		
		Random casuale = new Random();
		this.monitor = monitor;
		this.ID = ID;
		TipoUtente = tipo;
		this.MaxAttesa = MaxAttesa;
		this.MaxUtilizzo = MaxUtilizzo;
		NumAccessi = casuale.nextInt(MaxAccessi) + 1;
		
		this.ThreadStatus = ThreadStatus;
		this.IndStatus = IndStatus;
		ThreadStatus[IndStatus] = '_';

		//Genero indice del PC personale del tesista
		if(tipo == UserType.Tesista) IndPC = casuale.nextInt(NumPC);
	}

	public UserType GetType()
	{
		return TipoUtente;
	}
	
	public int GetPC()
	{
		return IndPC;
	}
	
	public void SetPC(int i)
	{
		IndPC = i;
	}

}
