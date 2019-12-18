import java.rmi.Remote;
import java.rmi.RemoteException;

public interface InterfacciaCongresso extends Remote {

	String getProgramma() throws RemoteException;
	
	void registraSpeaker(int giornata, int sessione, String nome) throws Exception;
}
