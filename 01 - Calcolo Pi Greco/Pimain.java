
public class Pimain {
	public static void main(String[] args)
	{
		if(args.length != 2)
		{
			System.out.printf("Errore: il programma deve essere invocato con esattamente due argomenti!\n");
			return;
		}
			
		float accuracy = Float.parseFloat(args[0]);
		long timeout = Integer.parseInt(args[1]);
		
		System.out.printf("%s: avvio calcolo di pi con accuratezza %.4f e timeout %d millisecondi\n", Thread.currentThread().getName(), accuracy, timeout);
		
		Pitask task = new Pitask (accuracy);
		Thread worker = new Thread(task);
		worker.start();
		
		try {
			worker.join(timeout);
		} catch (InterruptedException e1) {}
		worker.interrupt();
		
		System.out.printf("%s: calcolo terminato. La stima calcolata è %f\n", Thread.currentThread().getName(), task.getStima());
		return;
	}
}
