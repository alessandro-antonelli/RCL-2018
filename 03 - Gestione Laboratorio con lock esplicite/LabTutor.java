import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.Condition;


public class LabTutor {
	// Lock per l'accesso in mutua esclusione ad OccupazionePC[], NumLiberi e ProfInAttesa
	private ReentrantLock LockBookKeeping = new ReentrantLock();
	
	// Var. di condizione che segnala che NumLiberi sia diventato > 0
	private Condition NuovoPCDisponibile = LockBookKeeping.newCondition();
	
	// Var. di condizione che segnala che tutti i PC del lab sono diventati liberi
	private Condition InteroLabDisponibile = LockBookKeeping.newCondition();
	
	// Array di supporto che mantiene lo stato di occupazione di ogni PC
	private Boolean[] OccupazionePC;
	
	// Numero di PC liberi nel laboratorio
	private int NumLiberi;
	
	// Indica il numero di prof in attesa di utilizzare il laboratorio
	private int ProfInAttesa = 0;
	
	// Array che contiene le lock per l'accesso in mutua esclusione ai PC 
	private ReentrantLock[] LockPC;
	
	// Array che contiene le var. di condizione che indicano quando ciascun singolo PC diventa libero
	private Condition[] PCDiventatoLibero;
	
	
	public LabTutor(int NumPC)
	{
		if(NumPC <= 0) throw new IllegalArgumentException();
		
		NumLiberi = NumPC;
		LockPC = new ReentrantLock[NumPC];
		PCDiventatoLibero = new Condition[NumPC];
		OccupazionePC = new Boolean[NumPC]; 
		for(int i=0; i<NumPC; i++)
		{
			LockPC[i] = new ReentrantLock();
			PCDiventatoLibero[i] = LockBookKeeping.newCondition(); // ERA: LockPC[i].newCondition();
			OccupazionePC[i] = false;
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
			LockBookKeeping.lock();
			while(NumLiberi < 1 || ProfInAttesa > 0)
			{
				try {
					NuovoPCDisponibile.await();
				} catch (InterruptedException e) { e.printStackTrace(); }
			}
			
			int PCassegnato = -1;
			for(int i=0; i<OccupazionePC.length; i++)
				if(OccupazionePC[i] == false) { PCassegnato = i; break; }
			
			OccupazionePC[PCassegnato] = true;
			NumLiberi--;
			utente.SetPC(PCassegnato);
			LockBookKeeping.unlock();
			
			
			LockPC[PCassegnato].lock();
			return;
		}
		
		// Assegna PC al tesista
		if(utente.GetType() == LabUser.UserType.Tesista)
		{
			LockBookKeeping.lock();
			while(OccupazionePC[utente.GetPC()] == true || ProfInAttesa > 0)
			{
				try {
					PCDiventatoLibero[utente.GetPC()].await();
				} catch (InterruptedException e) { e.printStackTrace(); }
			}
			
			OccupazionePC[utente.GetPC()] = true;
			NumLiberi--;
			LockBookKeeping.unlock();
			
			LockPC[utente.GetPC()].lock();
			return;
		}
		
		// Assegna PC al prof
		if(utente.GetType() == LabUser.UserType.Professore)
		{
			LockBookKeeping.lock();
			ProfInAttesa++;
			while(NumLiberi < OccupazionePC.length)
			{
				try {
					InteroLabDisponibile.await();
				} catch (InterruptedException e) { e.printStackTrace(); }
			}
			ProfInAttesa--;
			
			for(int i=0; i<OccupazionePC.length; i++)
			{
				LockPC[i].lock();
				OccupazionePC[i] = true;
				NumLiberi--;
			}
			LockBookKeeping.unlock();
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
			LockBookKeeping.lock();
			
			for(int i=0; i<OccupazionePC.length; i++)
			{
				LockPC[i].unlock();
				OccupazionePC[i] = false;
				NumLiberi++;
				if(ProfInAttesa == 0)
				{
					PCDiventatoLibero[i].signal();
					NuovoPCDisponibile.signal();
				}
			}
			if(ProfInAttesa > 0) InteroLabDisponibile.signal();
			LockBookKeeping.unlock();
		}
		// Rilascia PC occupato da studente o tesista
		else {
			LockBookKeeping.lock();
			LockPC[utente.GetPC()].unlock();
			OccupazionePC[utente.GetPC()] = false;
			NumLiberi++;
			if(NumLiberi == OccupazionePC.length) InteroLabDisponibile.signal();
			NuovoPCDisponibile.signal();
			PCDiventatoLibero[utente.GetPC()].signal();
			LockBookKeeping.unlock();
		}
		return;
	}
}
