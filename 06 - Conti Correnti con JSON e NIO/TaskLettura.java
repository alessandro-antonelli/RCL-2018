import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.*;
import org.json.simple.*;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

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
		
		try
		{
			FileReader charstream = new FileReader(fileInput);
			
			JSONParser parser = new JSONParser();
			JSONObject obj = (JSONObject) parser.parse(charstream);

			Long nConti = (Long) obj.get("NumeroConti");
			System.out.printf("%s: Aperto il file da deserializzare. Contiene %d conti correnti.\n", Thread.currentThread().getName(), nConti);
			
			JSONArray conti = (JSONArray) obj.get("Conti");
			for(int i=0; i<nConti; i++)
			{
				System.out.printf("%s: Inizio lettura del conto %d su %d\n", Thread.currentThread().getName(), i+1, nConti);
				
				JSONObject conto = (JSONObject) conti.get(i);
				
				String intestatario = (String) conto.get("intestatario");
				ContoCorrente ContoLettodafile = new ContoCorrente(intestatario);
				
				JSONArray movimenti = new JSONArray();
				movimenti = (JSONArray) conto.get("movimenti");
				
				for(int j=0; j<movimenti.size();j++)
				{
					JSONObject movimento = (JSONObject) movimenti.get(j);
					Date dataLetta = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").parse((String) movimento.get("data"));
					MovimentoCC.Causale causaleLetta = Enum.valueOf(MovimentoCC.Causale.class, (String) movimento.get("causale"));
					MovimentoCC movimentoLetto = new MovimentoCC(dataLetta, causaleLetta);
					ContoLettodafile.AddMovimento(movimentoLetto);
				}

				TaskConteggio conteggio = new TaskConteggio(ContoLettodafile, contatore, stampeMovimenti);
				PoolConsumatori.execute(conteggio);
			}
			System.out.printf("%s: Lettura di tutti i %d conti completata! Il thread termina\n", Thread.currentThread().getName(), nConti);
			
			charstream.close();
		}
		catch (FileNotFoundException e) { e.printStackTrace(); }
		catch (IOException e) {	e.printStackTrace(); }
		catch (ParseException e) { e.printStackTrace(); } catch (java.text.ParseException e) { e.printStackTrace(); }
		
		return;
	}

}
