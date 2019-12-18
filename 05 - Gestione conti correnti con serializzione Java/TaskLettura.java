import java.io.*;
import java.util.concurrent.*;

public class TaskLettura implements Runnable {
	
	private ThreadPoolExecutor PoolConsumatori;
	private File fileInput;
	private ContatoreCausali contatore;
	private Boolean stampeMovimenti;


	
	public TaskLettura(ThreadPoolExecutor PoolConsumatori, File fileInput, ContatoreCausali contatore, Boolean stampeMovimenti)
	{
		this.PoolConsumatori = PoolConsumatori;
		this.fileInput = fileInput;
		this.contatore = contatore;
		this.stampeMovimenti = stampeMovimenti;
	}
	

	
	//thread rilettore. legge dal file gli oggetti "conto corrente" e li passa al pool consumatore
	
	@Override
	public void run() {
		Thread.currentThread().setName("Lettore");
		
		try (FileInputStream stream = new FileInputStream(fileInput))
		{
			ObjectInputStream input = new ObjectInputStream(stream);

			Integer nConti = (Integer) input.readObject();
			System.out.printf("%s: Aperto il file da deserializzare. Contiene %d conti correnti.\n", Thread.currentThread().getName(), nConti);
			for(int i=0; i<nConti; i++)
			{
				System.out.printf("%s: Inizio lettura del conto %d su %d\n", Thread.currentThread().getName(), i+1, nConti);
				try
				{
					ContoCorrente ContoLettodafile = (ContoCorrente) input.readObject();

					TaskConteggio conteggio = new TaskConteggio(ContoLettodafile, contatore, stampeMovimenti);
					PoolConsumatori.execute(conteggio);
				}
				catch (java.lang.OutOfMemoryError e)
				{
					System.out.printf("%s: Errore! Memoria heap insufficiente alla deserializzazione del file. Riduci il numero dei conti correnti o dei movimenti dentro il file!\n", Thread.currentThread().getName());
					System.exit(1);
				}
			}
			System.out.printf("%s: Lettura di tutti i %d conti completata! Il thread termina\n", Thread.currentThread().getName(), nConti);
			
			input.close();
			stream.close();
		}
		catch (FileNotFoundException e) { e.printStackTrace(); }
		catch (IOException e) {	e.printStackTrace(); }
		catch (ClassNotFoundException e) { e.printStackTrace(); System.exit(1); }
		
		return;
	}

}
