package Client;

import javax.swing.SwingUtilities;

/**Main del client*/
public class ClientMain implements Runnable{
	public static void main(String[] args){
		SwingUtilities.invokeLater(new ClientMain());
	}

	@Override
	public void run() {
		ClientGui gui = new ClientGui();
		gui.setVisible(true);
	}
}