package CasiControladores;

import Vistas.SerialSettings;
import com.fazecast.jSerialComm.SerialPort;

import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import static CasiControladores.Estudiant.mySerial;
import static java.lang.Thread.sleep;

public class SettingsController implements ActionListener, PopupMenuListener,Runnable {

    private SerialSettings settingsView;

    public SettingsController(SerialSettings settingsView){
        this.settingsView = settingsView;
        new Thread(this).start();
    }

    private void openPort() {
        mySerial.openPort();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        switch (e.getActionCommand()){
            case "Update":
                settingsView.updateCOMComboBox();
                break;
            case "Submit":
                boolean abort = false;

                if(mySerial != null) {
                    mySerial.closePort();
                    ExitManager.removePort(mySerial);
                }

                mySerial = settingsView.getSerialPort();

                if(mySerial == null){
                    abort = true;
                }

                if(!abort) {
                    ExitManager.addPort(mySerial);
                    mySerial.setBaudRate(settingsView.getBaudRate());

                    int value = SerialPort.TIMEOUT_NONBLOCKING;
                    int read = 0;
                    int write = 0;

                    if (settingsView.usingTimeOut()) {
                        switch (settingsView.getTimeOut()) {
                            case "TIMEOUT_READ_SEMI_BLOCKING":
                                value = SerialPort.TIMEOUT_READ_SEMI_BLOCKING;
                                break;
                            case "TIMEOUT_READ_BLOCKING":
                                value = SerialPort.TIMEOUT_READ_BLOCKING;
                                break;
                            case "TIMEOUT_SCANNER":
                                value = SerialPort.TIMEOUT_SCANNER;
                                break;
                        }
                        read = settingsView.getReadTimeOut();
                        write = settingsView.getWriteTimeOut();

                    }
                    mySerial.setComPortTimeouts(value, read, write);

                    openPort();
                }else System.out.println("ERROR! No s'ha pogut obtenir el port especificat. Contacta amb l'Arroyo.");
                break;
            default:
        }
    }

    @Override
    public void popupMenuWillBecomeVisible(PopupMenuEvent e) {

    }

    @Override
    public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
        Object source = e.getSource();
        if(source instanceof JComboBox){
            JComboBox combo = ((JComboBox)source);
            String option = (String)combo.getSelectedItem();
            if(option != null)
            if(option.equals("TIMEOUT_NONBLOCKING")){
                settingsView.clearFields();
            }
        }
    }

    @Override
    public void popupMenuCanceled(PopupMenuEvent e) {

    }

    @Override
    public void run() {
        while(true){
            try {
                if(mySerial != null){
                    settingsView.updateConnectionState(mySerial.isOpen());
                }else{
                    sleep(2000);
                }
                sleep(250);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
