import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;


public class TaskConteggio implements Runnable {
	
	private final ContoCorrente contoInput;
	private final ContatoreCausali ContatoreGlobale;
	private final Boolean stampeMovimenti;
	
	private long countBonifico;
	private long countAccredito;
	private long countBollettino;
	private long countF24;
	private long countPagoBancomat;
	
	public TaskConteggio(ContoCorrente contoInput, ContatoreCausali counter, Boolean stampeMovimenti)
	{
		if(counter == null || contoInput == null) throw new IllegalArgumentException();
		
		this.contoInput = contoInput;
		this.ContatoreGlobale = counter;
		this.stampeMovimenti = stampeMovimenti;
		
		countBonifico = 0;
		countAccredito = 0;
		countBollettino = 0;
		countF24 = 0;
		countPagoBancomat = 0;
	}

	//Un pool di thread riceve i task dal lettore e conteggia quante volte ogni causale è stata utilizzata nel conto corrente.
	//Alla fine del conto corrente, aggiunge i valori parziali al contatore globale.
	
	@Override
	public void run() {		
		List<MovimentoCC> movimenti = contoInput.getMovimenti();
		
		System.out.printf("%s: Inizia il conteggio del conto corrente intestato a %s, che contiene %d movimenti\n",
				Thread.currentThread().getName(), contoInput.getIntestatario(), movimenti.size());
		DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
		
		for(MovimentoCC movimento : movimenti)
		{
			if(stampeMovimenti == true)
				System.out.printf("%s: Conto %s: letto %s in data %s\n", Thread.currentThread().getName(), contoInput.getIntestatario(),
				movimento.getCausale(), dateFormat.format(movimento.getDate()));
			
			switch (movimento.getCausale())
			{
				case Bonifico :		countBonifico++; break;
				case Accredito :	countAccredito++; break;
				case Bollettino :	countBollettino++; break;
				case F24 :			countF24++; break;
				case PagoBancomat :	countPagoBancomat++; break;
			}
		}
		ContatoreGlobale.aggiungiParziali(countBonifico, countAccredito, countBollettino, countF24, countPagoBancomat);
		
		System.out.printf("%s: Concluso il conteggio del conto corrente di %s\n",
				Thread.currentThread().getName(), contoInput.getIntestatario());
		return;
	}

}
