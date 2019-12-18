
public class SessioneCongresso {
	
	private String[] nomiSpeakers;
	private final int maxSpeaker;
	private int speakerRegistrati;

	public SessioneCongresso(int maxSpeaker)
	{
		if(maxSpeaker <= 0) throw new IllegalArgumentException();
		
		this.maxSpeaker = maxSpeaker;
		this.speakerRegistrati = 0;
		
		this.nomiSpeakers = new String[maxSpeaker];
		for(int i=0; i<maxSpeaker; i++) this.nomiSpeakers[i] = null;
	}
	
	public void RegistraSpeaker(String nome) throws Exception
	{
		if (this.isFull()) throw new Exception();
		if(nome == null || nome.isEmpty()) throw new IllegalArgumentException();
		
		nomiSpeakers[speakerRegistrati] = nome;
		speakerRegistrati++;
	}
	
	public Boolean isFull()
	{
		return (speakerRegistrati == maxSpeaker);
	}
	
	public String getNames()
	{
		if(speakerRegistrati == 0)
			return "Nessun speaker ha ancora registrato il suo intervento in questa sessione!";
		else
		{
			StringBuilder programma = new StringBuilder();
			for(int i=0; i<speakerRegistrati; i++) programma.append("Intervento " + (i+1) + ": " + nomiSpeakers[i] + ". ");
		
			return programma.toString();
		}
	}
}
