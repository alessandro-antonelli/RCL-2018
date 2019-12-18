import java.security.InvalidParameterException;
import java.util.concurrent.*;

public class UfficioPostale {
	
	private ThreadPoolExecutor esecutore;

	public UfficioPostale(int sportelli, int DimSalaInterna) throws InvalidParameterException
	{
		if(sportelli <= 0)	throw new InvalidParameterException();
		
		ArrayBlockingQueue<Runnable> CodaInterna = new ArrayBlockingQueue<Runnable>(DimSalaInterna);
		esecutore = new ThreadPoolExecutor(sportelli, sportelli, 0L, TimeUnit.MILLISECONDS, CodaInterna);
		
		System.out.printf("%s: Istituito un ufficio postale con %d sportelli e sala interna ampia %d!\n",
				Thread.currentThread().getName(), sportelli, DimSalaInterna);
		StampaStatistiche();
		
		return;
	}
	
	public void AccogliCliente(ClientePostale cliente) throws InvalidParameterException, InterruptedException
	{
		if(cliente == null)	throw new InvalidParameterException();
		
		//Se c'è posto, sottomette il task all'esecutore con la execute;
		//se non c'è, lo sottomette direttamente con un inserimento nella coda interna,
		//sospendendosi finché non si libera un posto. Questa soluzione permette di evitare l'attesa attiva
		try {
			esecutore.execute(cliente);
		} catch (RejectedExecutionException e) { esecutore.getQueue().put(cliente); }
		
		return;
	}
	
	public void StampaStatistiche()
	{
		String stat = Thread.currentThread().getName() + ": ";
		if (esecutore.isShutdown() == false)
		{
			stat = stat + "Ufficio postale aperto, ci sono ";
			stat = stat + esecutore.getPoolSize() + " sportelli aperti ";
			stat = stat + "di cui " + esecutore.getActiveCount() + " impegnati, e ";
			stat = stat + esecutore.getQueue().size() + " clienti nella sala d'attesa interna. ";
			stat = stat + "Totale clienti serviti " + esecutore.getCompletedTaskCount();
		}
		else
		{
			if (esecutore.isTerminated() == false)
			{
				stat = stat + "Ufficio postale in chiusura, ci sono ancora ";
				stat = stat + esecutore.getPoolSize() + " sportelli aperti ";
				stat = stat + "di cui " + esecutore.getActiveCount() + " impegnati, e ";
				stat = stat + esecutore.getQueue().size() + " clienti da smaltire. ";
				stat = stat + "Totale clienti serviti " + esecutore.getCompletedTaskCount();
			} else
			{
				stat = stat + "Ufficio postale chiuso, ci sono ";
				stat = stat + esecutore.getPoolSize() + " sportelli aperti e ";
				stat = stat + esecutore.getQueue().size() + " clienti nella sala d'attesa interna. ";
				stat = stat + "Totale clienti serviti " + esecutore.getCompletedTaskCount();
			}
			
		}
		
		System.out.printf("%s\n", stat);
	}
	
	//Chiusura ordinaria dell'ufficio postale
	public void ChiudiUfficio()
	{
		esecutore.shutdown();
	}
	
	//Chiusura rapida dell'ufficio postale
	public void EvacuaUfficio()
	{
		esecutore.shutdownNow();
	}
	
	//Indica se tutti gli sportelli dell'ufficio hanno terminato la loro esecuzione
	public boolean HaChiuso()
	{
		return esecutore.isTerminated();
	}
	
	//Sospende l'esecuzione fino alla chiusura dell'ufficio postale, con un timeout in millisecondi
	public void AttendiChiusura(long timeout) throws InvalidParameterException, InterruptedException
	{
		if(timeout <= 0)	throw new InvalidParameterException();
		esecutore.awaitTermination(timeout, TimeUnit.MILLISECONDS);
	}
}
