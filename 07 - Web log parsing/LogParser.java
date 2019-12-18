import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.Vector;

public class LogParser {

private File logFile;
private boolean multithreaded;
private int poolSize;
private Boolean verbose;

	public LogParser(File logFile, boolean IsMultithreaded, int poolSize, Boolean verbose)
	{
		this.logFile = logFile;
		this.multithreaded = IsMultithreaded;
		this.poolSize = poolSize;
		this.verbose = verbose;
	}
	
	public void avvia() throws FileNotFoundException
	{
		//LETTURA DEL LOG E CREAZIONE DEI TASK
		System.out.printf("%s: Iniziata lettura del file di log\n", Thread.currentThread().getName());
		
		//Collezione che contiene i task relativi alla risoluzione degli indirizzi di ciascuna riga del log
		Vector<TaskRisoluzioneRiga> arrayTask = new Vector<TaskRisoluzioneRiga>();
		
		BufferedReader reader;
		reader = new BufferedReader(new FileReader(logFile));
		
        try {
        	int i = 0;
            String lineaLetta = "";
			while ((lineaLetta = reader.readLine()) != null)
			{
				i++;
				if(lineaLetta.isEmpty()) continue;
				TaskRisoluzioneRiga nuovoTask = new TaskRisoluzioneRiga(new String(lineaLetta), i, verbose);
				arrayTask.add(nuovoTask);
			}
	        reader.close();
		}
        catch (IOException e1) { e1.printStackTrace(); }
		
		System.out.printf("%s: Terminata lettura del file di log\n", Thread.currentThread().getName());

		
		//ESECUZIONE DEI TASK (RISOLUZIONE DEGLI INDIRIZZI)
		System.out.printf("%s: Avvio risoluzione degli indirizzi.\n*** Segue la stampa del log con gli indirizzi "+
										"convertiti in nomi simbolici ***\n\n\n", Thread.currentThread().getName());
		
		if(multithreaded == false)
			//Risoluzione multithreaded
			for(TaskRisoluzioneRiga task : arrayTask) task.run();
		else
		{
			//Risoluzione singlethreaded
			
			ThreadPoolExecutor pool = (ThreadPoolExecutor) Executors.newFixedThreadPool(poolSize);

			for(TaskRisoluzioneRiga task : arrayTask) pool.execute(task);

			pool.shutdown();
			while (!pool.isTerminated())
				try { pool.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS); }
				catch (InterruptedException e) { e.printStackTrace(); }
		}
	}
}
