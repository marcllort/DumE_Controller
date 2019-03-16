package CasiControladores;

public class Reciever extends Thread {

    public Reciever(){
        ExitManager.addThread(this);
        this.start();
    }

    @Override
    public void run() {
        while (true){
            try {
                if(AnimatorController.readMode()){
                    int [] motor = Estudiant.readMode();
                    AnimatorController.updateMotors(motor[0], motor[1]);

                    sleep(Estudiant.READ_PERIOD);
                }else{
                    sleep(100);
                }
            }catch (InterruptedException e){
                System.out.println("Closing reciever thread...");
            }catch (Exception e2){
                e2.printStackTrace();
                System.out.println("Timeout mal construit en RECIEVER");
            }
        }
    }
}
