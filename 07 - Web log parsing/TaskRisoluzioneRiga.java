import java.net.InetAddress;
import java.net.UnknownHostException;


public class TaskRisoluzioneRiga implements Runnable {
	
	private String rigaLog;
	private int numeroRiga;
	private Boolean verbose;
	
	public TaskRisoluzioneRiga(String rigaLog, int numeroRiga, boolean verbose)
	{
		this.rigaLog = rigaLog;
		this.numeroRiga = numeroRiga;
		this.verbose = verbose;
	}

	@Override
	public void run() {		
		//Isolo la sottostringa che contiene l'indirizzo IP numerico
		int indiceSeparatore = rigaLog.indexOf(" - - ");
		if(indiceSeparatore == -1)
		{
			System.out.printf("%s: Errore fatale: il file di log è malformato!\nNon è stato possibile determinare il " +
								"separatore \" - - \" alla riga numero %d, costituita dal testo:\n%s\n",
								Thread.currentThread().getName(), numeroRiga, rigaLog);
			System.exit(1);
		}
		
		String IPstr = rigaLog.substring(0, indiceSeparatore);

		//Effettuo la risoluzione inversa
		if(verbose)
		{
			System.out.printf("%s: Iniziata la risoluzione della riga %d del log, che contiene l'indirizzo %s\n",
													Thread.currentThread().getName(), numeroRiga, IPstr);
			System.out.flush();
		}
		
		InetAddress IP;
		try {
			IP = InetAddress.getByName(IPstr);
			
			//Stampo la riga del log con il nome simbolico al posto dell'IP numerico			
			if(verbose)
			{
				if(!IPstr.equals(IP.getHostName()))
						System.out.printf("%s: indirizzo %s risolto con successo in %s\n",
												Thread.currentThread().getName(), IPstr, IP.getHostName());
				else
					System.out.printf("%s: indirizzo %s non risolto! (restituisce lo stesso IP numerico di input)\n",
							Thread.currentThread().getName(), IPstr);
			}
			
			System.out.println(rigaLog.replaceFirst(IPstr, IP.getHostName()));
			System.out.flush();
		}
		catch (UnknownHostException e)
		{
			//Se l'host è sconosciuto, stampo la riga del log senza modificarla, insieme a un messaggio di avvertimento
			System.out.println(rigaLog);
			System.out.printf("%s: Attenzione! L'indirizzo %s (riga %d) non è stato risolto per host sconosciuto!\n",
									Thread.currentThread().getName(), IPstr, numeroRiga);
			System.out.flush();
		}
	}

}
