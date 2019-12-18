import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Random;
import org.json.simple.*;

public class GeneratoreConti {
	
	private File fileContiCorrenti;
	private int nConti;
	private int MaxMovimenti;

	public GeneratoreConti(File fileContiCorrenti, int nConti, int MaxMovimenti)
	{
		if(fileContiCorrenti == null || nConti <= 0 || MaxMovimenti <= 0) throw new IllegalArgumentException();
		
		this.fileContiCorrenti = fileContiCorrenti;
		this.nConti = nConti;
		this.MaxMovimenti = MaxMovimenti;
	}
	
	public void Genera() throws FileNotFoundException, IOException
	{		
		Random rand = new Random();
		DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
		
		//Elenco di nomi e cognomi con cui generare intestatari dei conti casuali
		String[] NomiItaliani = {"Antonio", "Davide", "Edoardo", "Federico", "Giuseppe", "Leonardo", "Mario", "Nicola", "Roberto", "Samuele", "Tommaso", "Valerio"};
		String[] CognomiItaliani = {"Amato", "Bianchi", "Colombo", "De Luca", "Esposito", "Ferrari", "Greco", "Lombardi", "Marino", "Parisi", "Rossi", "Santoro", "Testa", "Verdi"};
		
		JSONObject obj = new JSONObject();
		JSONArray conti = new JSONArray();
		obj.put("NumeroConti", new Integer(nConti));
		
		for(int i = 0; i<nConti; i++)
		{
			JSONObject conto = new JSONObject();
			
			String IntestatarioCasuale = NomiItaliani[rand.nextInt(NomiItaliani.length)] + ' ' + CognomiItaliani[rand.nextInt(CognomiItaliani.length)];
			conto.put("intestatario", IntestatarioCasuale);
			
			int nMovimenti = rand.nextInt(MaxMovimenti) + 1;
			System.out.printf("%s: Inizia la generazione casuale del conto %d su %d, intestato a %s, con all'interno %d movimenti\n",
					Thread.currentThread().getName(), i+1, nConti, IntestatarioCasuale, nMovimenti);
			
			JSONArray movimenti = new JSONArray();
			
			for(int j=0; j<nMovimenti; j++)
			{
				JSONObject movimento = new JSONObject();
				movimento.put("causale", (String) MovimentoCC.Causale.values()[rand.nextInt(5)].toString());
				movimento.put("data", (String) (rand.nextInt(31) + "/" + rand.nextInt(12) + "/" + (1850 + rand.nextInt(168)) +
						" " + rand.nextInt(23) + ":" + rand.nextInt(59) + ":" + rand.nextInt(59)));
				
				movimenti.add(movimento);
			}
			conto.put("movimenti", movimenti);
			conti.add(conto);
		}
		
		System.out.printf("%s: Inizia la serializzazione dei conti su file JSON\n", Thread.currentThread().getName());
		obj.put("Conti", conti);
		
		FileWriter charstream = new FileWriter(fileContiCorrenti);
		charstream.write(obj.toJSONString());
		
		charstream.flush();
		charstream.close();
		return;
	}

}
