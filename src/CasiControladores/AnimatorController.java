package CasiControladores;

import CasiControladores.FilesManager.Arxiu;
import CasiControladores.FilesManager.ArxiuManager;
import CasiControladores.FilesManager.MalformedJsonFileException;
import Model.RobotFrame;

import Vistas.FinderWindow;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.MouseInputListener;
import java.awt.event.*;
import java.io.IOException;
import java.util.LinkedList;

import static java.lang.Thread.sleep;

public class AnimatorController implements ActionListener, ChangeListener, MouseInputListener,Runnable,KeyListener {

    private double animationSpeed;
    private static Animator animator;
    private ArxiuManager manager;

    private FinderWindow finder;

    private int actualFrame;
    private LinkedList<RobotFrame> frames;

    private boolean animationRunning;
    private long lastSend = 0;

    public AnimatorController(Animator animator, FinderWindow fw) {
        AnimatorController.animator = animator;
        finder = fw;

        frames = new LinkedList<>();
        for(int i = 0; i < 10;i ++) frames.add(new RobotFrame());
        animator.representaFrame(frames.get(actualFrame));

        manager = new ArxiuManager();
        Thread a = new Thread(this);
        a.start();
        ExitManager.addThread(a);
        animationSpeed = 1;
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        switch (e.getActionCommand()){
            case "startAnimation":
                animationRunning = true;
                break;
            case "updateFrameRate":

                String text = animator.getUpdateRate();
                if(text.isEmpty()) {
                    finder.showDialog("Error setting animation fps",
                            "Expected a positive double number",
                            "error");
                    animationSpeed = 0.0f;
                }else animationSpeed = Double.valueOf(text);

                break;
            case "stopAnimation":
                animationRunning = false;
                break;
            case "restartAnimation":
                animationRunning = false;
                actualFrame = 0;
                sendLifeData();
                animator.representaFrame(frames.get(actualFrame));
                animator.updateSlider(actualFrame);
                break;
            case "nextFrame":
                nextFrame();
                break;
            case "saveAnimation":
                saveAnimation();
                break;
            case "loadAnimation":
                animationRunning = false;
                finder.setVisible(true);
                updateJSONList();
                break;
            case "moreFrames":
                frames.add(new RobotFrame());
                animator.updateDuracioTotal(frames.size());
                break;
            case "lessFrames":
                if(frames.size() > 1){
                    frames.removeLast();
                    animator.updateDuracioTotal(frames.size());
                }
                break;
            //El boto actualitzar s'ha premut
            case "Actualitzar":

                //Es refresca la llista d'animacions
                updateJSONList();
                break;

            //El boto carregar s'ha premut
            case "Carregar":
                //Es carrega l'arxiu selecionat al animador
                carregar();
                break;
        }
        animator.focusCanvas();
    }

    private void nextFrame() {
        actualFrame ++;
        if(actualFrame >= frames.size()) actualFrame = 0;
        sendLifeData();
        animator.representaFrame(frames.get(actualFrame));
        animator.updateSlider(actualFrame);
    }

    private void prevFrame() {
        actualFrame --;
        if(actualFrame < 0) actualFrame = frames.size() - 1;
        sendLifeData();
        animator.representaFrame(frames.get(actualFrame));
        animator.updateSlider(actualFrame);
    }

    private void sendLifeData(){
        if(animator.isWrite()){
            Estudiant.writeMode(frames.get(actualFrame).getValues()[0],frames.get(actualFrame).getValues()[1]);
        }
    }

    private void updateJSONList(){
        //Es refresca la llista del LSFinder
        manager.clearArxiuList();
        buscarArxiusJson();
    }

    @Override
    public void stateChanged(ChangeEvent e) {

        JSlider slider = (JSlider) e.getSource();
        if(slider.getValueIsAdjusting()) {
            actualFrame = slider.getValue();
            sendLifeData();
            animator.representaFrame(frames.get(actualFrame));
            animator.updateSlider(actualFrame);
            animator.focusCanvas();
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {

    }

    @Override
    public void mousePressed(MouseEvent e) {
        animator.mousePress(true);
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        animator.mousePress(false);
    }

    @Override
    public void mouseEntered(MouseEvent e) {}

    @Override
    public void mouseExited(MouseEvent e) {}

    @Override
    public void mouseDragged(MouseEvent e) {
        updateMouse(e);
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        updateMouse(e);
    }

    private void updateMouse(MouseEvent e){
        if(animator.mouseMoved(e.getX(),e.getY())){
            if(System.currentTimeMillis() - lastSend >= Estudiant.WRITE_PERIOD){
                sendLifeData();
                lastSend = System.currentTimeMillis();
            }
        }
    }

    /**
     * buscarArxiusJson busca tots els arxius en format .json de la carpeta data.
     * Un cop troba un arxiu, el guarda. En cas de detectar que l'arxiu té un format json erroni,
     * guarda l'arxiu indicant que aquest es corrupte/erroni.
     */

    private void buscarArxiusJson(){

        StringBuilder errors = new StringBuilder();
        boolean error = false;
        int numberErrors = 0;

        //Va buscant arxius fins a trobar-ne tots. En cas de trobar un d'incorrecte, salta l'excepcio MalformedJsonFileException
        do {
            try {
                manager.lookForJsonFiles();
            } catch (MalformedJsonFileException e) {
                error = true;
                errors.append(e.getArxiu().getNom());
                errors.append(System.lineSeparator());
                numberErrors++;
                manager.addArxiu(e.getArxiu());
            }

        }while(!manager.estanElsArxiusCarregats());

        if(error){

            if(numberErrors > 1)
                //S'indica al usuari amb un error que el arxiu json trobat conté un error o varis errors
                finder.showDialog("Error en la recerca d'arxius .json",
                        "S'han trobat " + numberErrors + " fitxers amb errors:\n" + errors.toString(),
                        "error");
            else
                //S'indica al usuari amb un error que el arxiu json trobat conté un error o varis errors
                finder.showDialog("Error en la recerca d'arxius .json",
                        "S'ha trobat un fitxer amb errors:\n" + errors.toString(),
                        "error");
        }

        //Amb el seguent codi es refresca la llista d'arxius del LSFinder
        finder.resetList();
        for(Arxiu arxiu : manager.getArxius()){
            finder.addToList(arxiu);
        }
    }

    private void saveAnimation() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("{");
        stringBuilder.append("\"totalFrames\":");
        stringBuilder.append("\"").append(frames.size()).append("\",");

        stringBuilder.append("\"duracioTotal\":");
        stringBuilder.append("\"").append(animationSpeed).append("\",");

        int size = frames.size();
        stringBuilder.append("\"keyFrames\":[");
        for(int i = 0; i < size; i++) stringBuilder.append(frames.get(i).toJSON(i == size - 1));
        stringBuilder.append("]}");


        try {
            if (animator.getFileName().length() == 0){
               finder.showDialog("No name file","FileName has to be 1+ char long","error");
            }else{
                manager.createFile(animator.getFileName().contains(".json") ? animator.getFileName() : animator.getFileName() + ".json",stringBuilder.toString());
            }
        } catch (IOException | MalformedJsonFileException e) {
            e.printStackTrace();
        }
    }

    /**
     * Gestiona la funcio del boto carregar de LSFinder.
     */

    private void carregar(){

        //En el cas de haver selecionat un arxiu i que aquest sigui correcte, el carreguem a LSParser
        if (!finder.isSelectionEmpty() && finder.getSelecio().getJsonObject() != null) {

            manager.setArxiuSelecionat(finder.getSelecio());

            JSONObject novaAnimacio = manager.getArxiuSelecionat().getJsonObject();

            double tempsDeAnimacio = Double.valueOf((String)novaAnimacio.get("duracioTotal"));
            int totalNumberOfFrames = Integer.valueOf((String)novaAnimacio.get("totalFrames"));

            JSONArray newFramesData = novaAnimacio.getJSONArray("keyFrames");
            frames.clear();

            int[] buffer = new int[2];

            for(int iFrame = 0; iFrame < totalNumberOfFrames; iFrame++){

                JSONObject newFrameJSON = ((JSONObject) newFramesData.get(iFrame));

                for (int motor = 0; motor < 2; motor++) {
                    buffer[motor] = Byte.valueOf((String) newFrameJSON.get(motor + ""));
                }

                frames.add(new RobotFrame(buffer));
            }

            actualFrame = 0;
            animator.updateDuracioTotal(totalNumberOfFrames);

            animationSpeed = tempsDeAnimacio;
            animator.representaFrame(frames.get(actualFrame));

            finder.setVisible(false);

        }else if(finder.isSelectionEmpty()){
            //S'indica al usuari amb un error que el arxiu redactat a la textArea conté errors
            finder.showDialog("Informacio",
                    "Per a poder carregar un arxiu, has de selecionar-lo",
                    "info");
        }
    }

    @Override
    public void run() {
        try {
            while(true) {
                if (animationRunning) {
                    nextFrame();
                    sleep((long)(1000.0 / animationSpeed));
                } else {
                    sleep(1000);
                }
            }
        } catch (InterruptedException e) {
            System.out.println("Closing AnimatorController thread...");
        }
    }

    public void keyTyped(KeyEvent e) {}

    public void keyPressed(KeyEvent e) {}

    public void keyReleased(KeyEvent e) {
        switch (e.getKeyCode()){
            case KeyEvent.VK_W:
                //Live mode
                animator.toogleWrite();
                break;
            case KeyEvent.VK_DELETE:
            case KeyEvent.VK_BACK_SPACE:
                //New frame
                frames.set(actualFrame, new RobotFrame());
                animator.representaFrame(frames.get(actualFrame));
                sendLifeData();
                break;
            case KeyEvent.VK_C:
                if(actualFrame == 0){
                    frames.set(actualFrame, RobotFrame.copy(frames.get(frames.size() - 1)));
                }else{
                    frames.set(actualFrame, RobotFrame.copy(frames.get(actualFrame - 1)));
                }
                animator.representaFrame(frames.get(actualFrame));
                sendLifeData();
                break;

            case KeyEvent.VK_LEFT:
                prevFrame();
                break;

            case KeyEvent.VK_RIGHT:
                nextFrame();
                break;
//            case KeyEvent.VK_R:
//                animationRunning = false;
//                animator.toogleRead();
//                break;
        }
    }

    public static boolean readMode() {
        return animator.isreadMode();
    }

    public static void updateMotors(int motor1, int motor2) {
        animator.getActualFrame().updateValue(motor1,0);
        animator.getActualFrame().updateValue(motor2,1);
    }
}
