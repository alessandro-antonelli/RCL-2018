import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;


public class ContoCorrente implements Serializable {
	
	private static final long serialVersionUID = 1L;
	private String intestatario; // Nome del correntista
	private List<MovimentoCC> ListaMovimenti;  // Lista dei movimenti

	
	public ContoCorrente(String nomeIntestatario)
	{
		if(nomeIntestatario == null || nomeIntestatario == "") throw new IllegalArgumentException();
		
		this.intestatario = nomeIntestatario;
		this.ListaMovimenti = new LinkedList<MovimentoCC>();
	}
	
	
	public void AddMovimento(MovimentoCC movimento)
	{
		if(movimento == null) throw new NullPointerException();
		
		ListaMovimenti.add(movimento);
	}
	
	public String getIntestatario()
	{
		return intestatario;
	}
	
	public List<MovimentoCC> getMovimenti()
	{
		return Collections.unmodifiableList(ListaMovimenti);
	}
}
