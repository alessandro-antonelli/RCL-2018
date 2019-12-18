
public class Pitask implements Runnable {
	
	private float accuracy;
	private float stima;
	private int denominatore;
	private int segno;

	public void run() {
		System.out.printf("%s: avviato!\n", Thread.currentThread().getName());
		while(true)
		{			
			stima = stima + ((4f / denominatore) * segno);
			System.out.printf("Iterazione %d: stima calcolata %f, scarto %f\n", (denominatore-1)/2, stima, Math.abs(stima - Math.PI));
			
			denominatore = denominatore + 2;
			segno = 0 - segno;
			
			if(Math.abs(stima - Math.PI) < accuracy)
			{
				System.out.printf("%s: esco dal ciclo, accuratezza raggiunta\n", Thread.currentThread().getName());
				break;
			}
			if(Thread.interrupted() == true)
			{
				System.out.printf("%s: esco dal ciclo, ricevuta interruzione\n", Thread.currentThread().getName());
				break;
			}
		}
		return;
	}
	
	public float getStima()
	{
		return stima;
	}
	
	public Pitask(float accuracy)
	{
		this.accuracy = accuracy;
		stima = 4;
		denominatore = 3;
		segno = -1;
		return;
	}

}
