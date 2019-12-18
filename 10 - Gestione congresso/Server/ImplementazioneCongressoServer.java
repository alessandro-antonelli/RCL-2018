import java.rmi.RemoteException;
import java.rmi.server.RemoteServer;


public class ImplementazioneCongressoServer extends RemoteServer implements InterfacciaCongresso {
	
	private static final long serialVersionUID = 1L;
	
	private SessioneCongresso[][] congresso;
	private int giornate;
	private int sessioniGiornaliere;

	
	public ImplementazioneCongressoServer(int giornate, int sessioniGiornaliere, int maxSpeaker)
	{
		if(giornate <= 0 || sessioniGiornaliere <= 0 || maxSpeaker <= 0) throw new IllegalArgumentException();
		this.giornate = giornate;
		this.sessioniGiornaliere = sessioniGiornaliere;
		
		congresso = new SessioneCongresso[giornate][sessioniGiornaliere];
		for(int i=0; i<giornate; i++)
			for(int j=0; j<sessioniGiornaliere; j++)
				congresso[i][j] = new SessioneCongresso(maxSpeaker);
	}

	@Override
	public String getProgramma() throws RemoteException
	{
		StringBuilder programma = new StringBuilder();
		
		for(int i=0; i<giornate; i++)
		{
			programma.append("==========\nGiornata " + (i+1) + "\n\n");
			for(int j=0; j<sessioniGiornaliere; j++)
			{
				programma.append("** Sessione " + (j+1) + "**\n");
				programma.append(congresso[i][j].getNames() + "\n\n");
			}
			programma.append("\n");
		}
	
		return programma.toString();
	}

	@Override
	public void registraSpeaker(int giornata, int sessione, String nome) throws Exception
	{
		if(giornata < 1 || sessione < 1 || giornata > giornate || sessione > sessioniGiornaliere)
			throw new IllegalArgumentException("La sessione o la giornata indicata non esistono! (NB: la numerazione parte da 1)");
		if(congresso[giornata-1][sessione-1].isFull())
			throw new Exception("Nella sessione indicata tutti gli spazi d'intervento sono già stati occupati!");
		if(nome == null || nome.isEmpty()) throw new IllegalArgumentException("Il nome dello speaker non può essere né vuoto né nullo!");
		
		congresso[giornata-1][sessione-1].RegistraSpeaker(nome);
	}

}
