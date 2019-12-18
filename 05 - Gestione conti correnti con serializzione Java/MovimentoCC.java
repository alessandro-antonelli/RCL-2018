import java.io.Serializable;
import java.util.*;

public class MovimentoCC implements Serializable {
	
	private static final long serialVersionUID = 1L;
	Date data;	// Data del movimento
	private Causale causale;	// Causale del movimento

	public enum Causale
	{
		Bonifico,
		Accredito,
		Bollettino,
		F24,
		PagoBancomat
	}

	
	public MovimentoCC(Date data, Causale causale)
	{
		this.data = data;
		this.causale = causale;
	}


	
	public Causale getCausale()
	{
		return causale;
	}
	
	public Date getDate()
	{
		return data;
	}
}
