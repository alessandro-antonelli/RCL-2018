
public class ClientePostale implements Runnable {
	
	private String nome;
	private long TempoPerServirlo;

	@Override
	public void run() {
		System.out.printf("%s: è il turno di %s, saranno necessari %.1f secondi\n",
				Thread.currentThread().getName(), nome, TempoPerServirlo / 1000f);
		
		try {
			Thread.sleep(TempoPerServirlo);
		} catch (InterruptedException e) {e.printStackTrace();}
		
		System.out.printf("%s: %s servito con successo!\n", Thread.currentThread().getName(), nome);
	}

	public ClientePostale(String nome, long durata)
	{
		this.nome = nome;
		this.TempoPerServirlo = durata;
		return;
	}
	
	public String GetName()
	{
		return nome;
	}
}
