public class LabMonitor {
	
	// Array di supporto che mantiene lo stato di occupazione di ogni PC
	private Boolean[] OccupazionePC;
	
	// Numero di PC liberi del laboratorio
	private int NumLiberi;
	
	// Numero di PC del laboratorio contemporaneamente liberi e non richiesti da tesisti
	private int NumLiberiENonRichiestiTesisti;
	
	// Array che per ogni PC indica quanti tesisti sono in attesa di ottenerlo. Serve per dare loro priorità rispetto agli studenti. 
	private int[] TesistiInAttesa;
	
	// Indica il numero di prof in attesa di utilizzare il laboratorio
	private int ProfInAttesa = 0;


	public LabMonitor(int NumPC)
	{
		if(NumPC <= 0) throw new IllegalArgumentException();
		
		NumLiberi = NumPC;
		NumLiberiENonRichiestiTesisti = NumPC;
		OccupazionePC = new Boolean[NumPC];
		TesistiInAttesa = new int[NumPC];
		for(int i=0; i<NumPC; i++)
		{		
			OccupazionePC[i] = false;
			TesistiInAttesa[i] = 0;
		}
	}
	
	
	public void ChiediUsoPC(LabUser utente)
	{
		// Valida parametro
		if(utente == null) throw new NullPointerException();
		if(utente.GetType() == LabUser.UserType.Tesista && (utente.GetPC() < 0 || utente.GetPC() >= OccupazionePC.length ))
			throw new IllegalArgumentException();
		
		// Assegna PC allo studente
		if(utente.GetType() == LabUser.UserType.Studente)
		{
			synchronized(this) {
				while(NumLiberiENonRichiestiTesisti < 1 || ProfInAttesa > 0)
				{
					try { wait(); } catch (InterruptedException e) { e.printStackTrace(); }
				}
			
				int PCassegnato = -1;
				for(int i=0; i<OccupazionePC.length; i++)
					if(OccupazionePC[i] == false && TesistiInAttesa[i] == 0) { PCassegnato = i; break; }
			
				OccupazionePC[PCassegnato] = true;
				NumLiberi--;
				NumLiberiENonRichiestiTesisti--;
				utente.SetPC(PCassegnato);
			}
			
			return;
		}
		
		// Assegna PC al tesista
		if(utente.GetType() == LabUser.UserType.Tesista)
		{
			synchronized(this) {
				if(TesistiInAttesa[utente.GetPC()] <= 0 && OccupazionePC[utente.GetPC()] == false) NumLiberiENonRichiestiTesisti--;
				TesistiInAttesa[utente.GetPC()]++;
				
				while(OccupazionePC[utente.GetPC()] == true || ProfInAttesa > 0)
				{
					try { wait(); } catch (InterruptedException e) { e.printStackTrace(); }
				}
			
				OccupazionePC[utente.GetPC()] = true;
				NumLiberi--;
				TesistiInAttesa[utente.GetPC()]--;
			}
			
			return;
		}
		
		// Assegna PC al prof
		if(utente.GetType() == LabUser.UserType.Professore)
		{
			synchronized(this) {
				ProfInAttesa++;
				while(NumLiberi < OccupazionePC.length)
				{
					try { wait(); } catch (InterruptedException e) { e.printStackTrace(); }
				}
				ProfInAttesa--;
			
				for(int i=0; i<OccupazionePC.length; i++)
				{
					OccupazionePC[i] = true;
					NumLiberi--;
					if(TesistiInAttesa[i] <= 0) NumLiberiENonRichiestiTesisti--;
				}
			}
			
			return;
		}
	}
	
	public void RilasciaPC(LabUser utente)
	{
		// Valida parametro
		if(utente == null) throw new NullPointerException();
		if(utente.GetType() != LabUser.UserType.Professore && (utente.GetPC() < 0 || utente.GetPC() >= OccupazionePC.length ))
				throw new IllegalArgumentException();
		
		// Rilascia lab occupato dal professore
		if(utente.GetType() == LabUser.UserType.Professore)
		{
			synchronized(this) {
				for(int i=0; i<OccupazionePC.length; i++)
				{
					OccupazionePC[i] = false;
					NumLiberi++;
					if(TesistiInAttesa[i] <= 0) NumLiberiENonRichiestiTesisti++;
					notifyAll();
				}
			}
		}
		// Rilascia PC occupato da studente o tesista
		else {
			synchronized(this) {
				OccupazionePC[utente.GetPC()] = false;
				NumLiberi++;
				if(TesistiInAttesa[utente.GetPC()] <= 0) NumLiberiENonRichiestiTesisti++;
				notifyAll();
			}
		}
		
		return;
	}
}
