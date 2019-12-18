/*
Università di Pisa - Laboratorio del corso di Reti di Calcolatori - A.A. 2018-2019
Assignment #2 - Simulazione Ufficio Postale
Studente Alessandro Antonelli, matricola 507264
*/

import java.util.Queue;
import java.util.LinkedList;
import java.util.Random;

public class MainClass {
//Implementa un ufficio postale dove tutti i clienti si presetano e vengono fatti entrare all'apertura dell'ufficio postale.
//NON implementa la versione con flusso continuo.

	public static void main(String[] args)
	// Accetta i seguenti parametri da riga di comando:
	// 1) un intero positivo che indica il numero dei clienti dell'ufficio postale
	// 2) un intero positivo che indica il valore di k, cioè l'ampiezza della sala d'attesa più interna
	// 3) un intero positivo che indica il limite superiore al tempo richiesto per servire ciascun cliente, espresso in millisecondi
	{
		//leggo i parametri
		if(args.length != 3)
		{
			System.out.println("Errore: al programma devono essere passati esattamente tre parametri. Vedi commento in MainClass.java");
			System.exit(1);
		}
		final int NumeroClienti = Integer.parseInt(args[0]); //numero dei clienti che entrano all'inizio quando apre l'ufficio postale
		final int DimCodaInterna = Integer.parseInt(args[1]); //il valore k indicato nella consegna
		final int MaxAttesa = Integer.parseInt(args[2]); //limite superiore al tempo richiesto per servire ciascun cliente (millisecondi)
		final int NumeroSportelli = 4; //numero di sportelli dell'ufficio postale (costante data dal testo)
		
		if (NumeroClienti <= 0 || DimCodaInterna <= 0 || MaxAttesa <= 0)
		{
			System.out.println("Errore: i parametri devono essere interi positivi non nulli. Vedi commento in MainClass.java");
			System.exit(1);
		}
		
		//creo coda della sala d'attesa esterna
		Queue<ClientePostale> CodaEsterna = new LinkedList<ClientePostale>();
		
		//creo clienti (task runnable) e li inserisco tutti nella sala d'attesa esterna
		Random rand = new Random();
		for(int i = 0; i<NumeroClienti; i++)
		{
			//Creo cliente con tempo di servizio casuale tra 1 e MaxAttesa millisecondi
			long TempoPerServirlo = rand.nextInt(MaxAttesa) + 1;
			
			ClientePostale cliente = new ClientePostale("Cliente" + i ,TempoPerServirlo);
			CodaEsterna.add(cliente);
			System.out.printf("%s: %s entra nella sala d'attesa esterna\n", Thread.currentThread().getName(), cliente.GetName());
		}
		
		//creo ufficio postale con 4 sportelli, cioè creo pool
		UfficioPostale UfficioPostale = new UfficioPostale(NumeroSportelli, DimCodaInterna);
		
		//faccio entrare tutte le persone nella coda interna dell'ufficio postale, k alla volta:
		//cioè sottometto tutti i task al thread pool
		while(CodaEsterna.isEmpty() == false)
		{
			ClientePostale cliente = CodaEsterna.remove();
			
			//Tenta di inserire il cliente nella coda interna. Se c'è posto, ritorna subito; se non c'è, si sospende finché non si crea posto
			try {
				UfficioPostale.AccogliCliente(cliente);
			} catch (InterruptedException e) { }
			
			System.out.printf("%s: %s accettato nella sala d'attesa interna, %d ancora in attesa di entrarvi\n",
					Thread.currentThread().getName(), cliente.GetName(), CodaEsterna.size());
			UfficioPostale.StampaStatistiche();
		}
		
		//Chiudo l'ufficio postale
		UfficioPostale.ChiudiUfficio();
		System.out.printf("%s: Tutti i clienti sono entrati nella sala d'attesa interna! L'ufficio postale chiude l'ingresso\n",
				Thread.currentThread().getName());
		
		while(UfficioPostale.HaChiuso() == false)
		{
			UfficioPostale.StampaStatistiche();
			
			try {
			UfficioPostale.AttendiChiusura(5000);
			} catch(InterruptedException e) {}
		}
		
		//Esco
		System.out.printf("%s: Tutti i clienti sono stati serviti! Chiusura del programma in corso\n",
				Thread.currentThread().getName());
		UfficioPostale.StampaStatistiche();
		return;
	}
}
