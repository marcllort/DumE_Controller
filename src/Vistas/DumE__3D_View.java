package Vistas;

import CasiControladores.*;

import processing.core.PApplet;
import processing.core.PShape;

public class DumE__3D_View extends PApplet{

    private PShape base, shoulder, upArm;
    private float rotX, rotY;
    private static float pitch;
    private static float yaw;

    public static void main(String[] args) {
        pitch = 3 * PI / 2;
        yaw = PI / 2;
        PApplet.main("Vistas.DumE__3D_View");
    }

    public void settings(){
        size(400, 600, P3D);
    }

    public void setup(){

        String folder = "robot_parts/";

        shoulder = loadShape(folder + "r1.obj");
        upArm = loadShape(folder + "r2.obj");
        base = loadShape(folder + "r3.obj");

        shoulder.disableStyle();
        upArm.disableStyle();
        base.disableStyle();
    }

    public void draw(){
        if (frameCount == 1) {
            mainSecundari();
        }

        background(33);

        smooth();
        lights();
        directionalLight(51, 102, 126, -1, 0, 0);

        noStroke();
        translate(width / 2.0f,height / 2.0f);
        rotateX(rotX);
        rotateY(-rotY);

        scale(-3);

        //Color del robot
        fill(255,0,0);
        translate(0,-40,0);
        shape(base);

        fill(0,255,0);
        translate(0, 4, 0);
        rotateY(- yaw + 3 * PI / 2);
        shape(shoulder);

        fill(0,0,255);
        translate(0, 25, 0);
        rotateY(PI);
        rotateX(pitch);
        shape(upArm);
    }

    public void mouseDragged(){
        rotY -= (mouseX - pmouseX) * 0.005;
        rotX -= (mouseY - pmouseY) * 0.005;
    }

    private void mainSecundari(){
        surface.setLocation(751,0);

        ExitManager.init();

        SerialSettings ss = new SerialSettings(0,0);

        SettingsController sc = new SettingsController(ss);
        ss.addController(sc);

        FinderWindow fw = new FinderWindow();

        Animator animator = new Animator(351);
        AnimatorController ac = new AnimatorController(animator, fw);

        //Haha q lleig
        new Reciever();

        animator.addController(ac);
        fw.configController(ac);

        animator.setVisible(true);
        ss.setVisible(true);

        Thread a = new Thread(animator);
        a.start();
        ExitManager.addThread(a);
    }

    public static void updatePitch(float newPitch){
        pitch = newPitch;
    }

    public static void updateYaw(float newYaw){
        yaw = newYaw;
    }
}

