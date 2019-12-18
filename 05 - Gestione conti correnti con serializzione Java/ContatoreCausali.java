
public class ContatoreCausali {

	private long countBonifico;
	private long countAccredito;
	private long countBollettino;
	private long countF24;
	private long countPagoBancomat;
	
	public ContatoreCausali()
	{
		countBonifico = 0;
		countAccredito = 0;
		countBollettino = 0;
		countF24 = 0;
		countPagoBancomat = 0;
	}
	
	public synchronized void aggiungiParziali(long BonificiParziali, long AccreditiParziali, long BollettiniParziali,
			long F24Parziali, long PagoBancomatParziali)
	{
		if(BonificiParziali < 0 || AccreditiParziali < 0 || BollettiniParziali < 0 || F24Parziali < 0 || PagoBancomatParziali < 0)
			throw new IllegalArgumentException();
		
		countBonifico += BonificiParziali;
		countAccredito += AccreditiParziali;
		countBollettino += BollettiniParziali;
		countF24 += F24Parziali;
		countPagoBancomat += PagoBancomatParziali;
	}
	
	public synchronized String getTotals()
	{
		return new String(countBonifico + " bonifici; " + countAccredito + " accrediti; " + countBollettino + " bollettini; " + 
							countF24 + " F24; " + countPagoBancomat + " PagoBancomat.");
	}
}
