package CasiControladores;

import Model.RobotFrame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.geom.Ellipse2D;

import static java.lang.Thread.sleep;

public class Animator extends JFrame implements Runnable {

    public final static Color limitColor = new Color(80, 80, 80, 150);
    private final static int UPDATE_RATE = 50;
    private final static int height = 300;
    private final static int knobSize = 10;

    private JButton startAnimation;
    private JButton restartAnimation;
    private JButton stopAnimation;
    private JButton nextFrame;

    private JPanel representacio;
    private JSlider posicioEnElTemps;

    private JButton saveAnimation;
    private JButton loadAnimaton;
    private JTextField animationName;
    private JButton moreFrames;
    private JButton lessFrames;

    private JButton updateFrameRate;
    private JTextField frameRate;

    private JLabel actualFrameNumber;

    private int majorTickHelper;

    private RobotFrame actualFrame;

    private Arm brazo;
    private Arm mano;

    private boolean editing;
    private boolean write;
    private boolean read;

    public Animator(int posX) {
        majorTickHelper = 1;

        write = false;
        read = true;
        editing = false;

        setTitle("Dum-E - Animator");
        setSize(400, 589);

        setResizable(false);

        setLocation(posX, 0);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

        addExitManagement();
        getContentPane().add(createVisual());

        double w = (double) getWidth() / 5.0, h = 3;
        double y = 260;

        mano = new Arm(getWidth() / 2.0 - h / 2 + w, y, w, h, Color.YELLOW, 360, 180, 270, true);
        brazo = new Arm(getWidth() / 2.0 - h / 2, y, w, h, Color.red, 360, 180, 270, false);
    }

    private JPanel createVisual() {
        JPanel main = new JPanel();
        main.setLayout(new BoxLayout(main, BoxLayout.Y_AXIS));

        startAnimation = new JButton("➤");
        stopAnimation = new JButton("✕");
        restartAnimation = new JButton("◀◀");
        nextFrame = new JButton("⇉");

        JPanel le = new JPanel();
        le.setLayout(new BoxLayout(le, BoxLayout.X_AXIS));

        le.add(Box.createHorizontalStrut(2));
        le.add(fullScreen(startAnimation));
        le.add(fullScreen(stopAnimation));
        le.add(fullScreen(restartAnimation));
        le.add(fullScreen(nextFrame));

        main.add(Box.createVerticalStrut(2));

        main.add(le);

        updateFrameRate = new JButton("Update fps");
        frameRate = new JTextField();

        JPanel lel = new JPanel();
        lel.setLayout(new BoxLayout(lel, BoxLayout.X_AXIS));

        lel.add(updateFrameRate);
        lel.add(frameRate);

        main.add(Box.createVerticalStrut(2));
        main.add(lel);

        representacio = new JPanel();
        representacio.setPreferredSize(new Dimension(representacio.getPreferredSize().width, height));

        main.add(Box.createVerticalStrut(2));
        main.add(representacio);
        main.add(Box.createVerticalStrut(10));

        lessFrames = new JButton("Remove frame");
        moreFrames = new JButton("Add frame");

        actualFrameNumber = new JLabel("Frame " + 0);
        actualFrameNumber.setHorizontalAlignment(SwingConstants.CENTER);

        posicioEnElTemps = new JSlider(0, 9, 0);
        posicioEnElTemps.setMinorTickSpacing(1);
        posicioEnElTemps.setMajorTickSpacing(1);
        posicioEnElTemps.setAlignmentY(Component.CENTER_ALIGNMENT);
        posicioEnElTemps.setPaintTicks(true);
        posicioEnElTemps.setPaintLabels(true);

        main.add(fullScreen(actualFrameNumber));
        main.add(fullScreen(posicioEnElTemps));

        JPanel div = new JPanel();
        div.setLayout(new BoxLayout(div, BoxLayout.X_AXIS));

        div.add(fullScreen(moreFrames));
        div.add(Box.createHorizontalStrut(2));
        div.add(fullScreen(lessFrames));

        main.add(Box.createVerticalStrut(2));
        main.add(div);
        main.add(Box.createVerticalStrut(2));

        loadAnimaton = new JButton("Load animation");
        main.add(fullScreen(loadAnimaton));
        main.add(Box.createVerticalStrut(2));

        saveAnimation = new JButton("Save animation");
        animationName = new JTextField();

        JLabel aux = new JLabel("Animation name");
        aux.setHorizontalAlignment(SwingConstants.LEFT);

        JPanel div2 = new JPanel();
        div2.setLayout(new BoxLayout(div2, BoxLayout.X_AXIS));
        div2.add(Box.createHorizontalStrut(2));
        div2.add(aux);
        div2.add(Box.createHorizontalStrut(5));
        div2.add(fullScreen(animationName));

        main.add(Box.createVerticalStrut(2));
        main.add(div2);
        main.add(Box.createVerticalStrut(2));
        main.add(fullScreen(saveAnimation));
        return main;
    }

    private JPanel fullScreen(Component c) {
        JPanel a = new JPanel(new BorderLayout());
        a.add(c, BorderLayout.CENTER);
        return a;
    }

    private void addExitManagement() {
        ExitManager.addJFrame(this);
        addWindowListener(new WindowListener() {
            @Override
            public void windowClosing(WindowEvent e) {
                ExitManager.exit();
            }

            @Override
            public void windowOpened(WindowEvent e) {
            }

            @Override
            public void windowClosed(WindowEvent e) {

            }

            @Override
            public void windowIconified(WindowEvent e) {

            }

            @Override
            public void windowDeiconified(WindowEvent e) {

            }

            @Override
            public void windowActivated(WindowEvent e) {

            }

            @Override
            public void windowDeactivated(WindowEvent e) {

            }
        });
    }

    @Override
    public void run() {
        try {
            while (true) {
                Image img = representacio.createImage(getWidth(), height);
                if (img != null) {
                    Graphics2D g = (Graphics2D) img.getGraphics();

                    render(g);

                    Graphics2D g2 = (Graphics2D) representacio.getGraphics();
                    g2.drawImage(img, 0, 0, null);
                }
                sleep(UPDATE_RATE);
            }
        } catch (Exception e) {
            System.out.println("Closing animator thread...");
        }
    }

    private void render(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, getWidth(), height);

        if (actualFrame != null) {
            String angles = mano.getAngle() + " º " + brazo.getAngle() + " º";

            g.setColor(Color.white);
            text("Angles: " + angles, 2, 0, Color.white, g);

            text("Writing:", getWidth() - g.getFontMetrics().stringWidth("Writing:") - 38, 0, Color.white, LEFT_ALIGNMENT, g);
            g.setColor(write ? Color.green : Color.red);
            g.fillOval(getWidth() - 30, g.getFontMetrics().getHeight() / 2 - 3, 14, 14);

            text("Reading:", getWidth() - g.getFontMetrics().stringWidth("Reading:") - 38, 20, Color.white, LEFT_ALIGNMENT, g);
            g.setColor(read ? Color.green : Color.red);
            g.fillOval(getWidth() - 30, g.getFontMetrics().getHeight() / 2 + 17, 14, 14);

            renderFrame(g);
        } else {
            text("No animation loaded", getWidth() / 2, height / 2, Color.red, CENTER_ALIGNMENT, g);
        }
    }

    private void text(String data, int x, int y, Color color, Graphics2D g) {
        text(data, x, y, color, LEFT_ALIGNMENT, g);
    }

    private void text(String data, int x, int y, Color color, float alignment, Graphics2D g) {
        g.setColor(color);
        FontMetrics fm = g.getFontMetrics();
        if (alignment == LEFT_ALIGNMENT) {
            g.drawString(data, x, y + fm.getHeight());
        } else {
            g.drawString(data, x - fm.stringWidth(data) / 2, y + fm.getHeight());
        }
    }

    public void addController(AnimatorController ac) {

        updateFrameRate.setActionCommand("updateFrameRate");
        updateFrameRate.addActionListener(ac);

        representacio.addKeyListener(ac);

        saveAnimation.setActionCommand("saveAnimation");

        saveAnimation.addActionListener(ac);

        moreFrames.setActionCommand("moreFrames");
        moreFrames.addActionListener(ac);

        lessFrames.setActionCommand("lessFrames");
        lessFrames.addActionListener(ac);

        posicioEnElTemps.addChangeListener(ac);

        representacio.addMouseListener(ac);
        representacio.addMouseMotionListener(ac);

        loadAnimaton.addActionListener(ac);
        loadAnimaton.setActionCommand("loadAnimation");

        startAnimation.addActionListener(ac);
        stopAnimation.addActionListener(ac);
        restartAnimation.addActionListener(ac);
        nextFrame.addActionListener(ac);

        startAnimation.setActionCommand("startAnimation");
        stopAnimation.setActionCommand("stopAnimation");
        restartAnimation.setActionCommand("restartAnimation");
        nextFrame.setActionCommand("nextFrame");
    }

    public void updateDuracioTotal(int valor) {
        posicioEnElTemps.setMaximum(valor - 1);

        majorTickHelper = valor / 10 + 1;
        posicioEnElTemps.setLabelTable(null);
        posicioEnElTemps.setMajorTickSpacing(majorTickHelper);
    }

    public void representaFrame(RobotFrame frame) {
        actualFrame = frame;
        int[] angles = actualFrame.getValues();

        brazo.updateAngle(angles[1]);
        mano.update(brazo.getEndX(), brazo.getEndY());

        mano.updateAngle(angles[0]);
    }

    public void updateSlider(int nex) {
        actualFrameNumber.setText("Frame " + nex);
        posicioEnElTemps.setValue(nex);
    }

    private void renderFrame(Graphics2D g) {

        if (!editing) {
            representaFrame(actualFrame);
        }

        brazo.render(g);
        mano.render(g);

        g.setColor(brazo.getBallColor());
        g.fill(new Ellipse2D.Double(brazo.getBallX(10), brazo.getBallY(10), 10, 10));

        g.setColor(mano.getBallColor());
        g.fill(new Ellipse2D.Double(mano.getBallX(10), mano.getBallY(10), 10, 10));
    }


    public boolean mouseMoved(int x, int y) {
        boolean hasAnUpdate = false;

        if (brazo.hover(x, y, knobSize)) {
            hasAnUpdate = brazo.move(x, y);
            mano.update(brazo.getEndX(), brazo.getEndY());
        }

        if (mano.hover(x, y, knobSize)) {
            hasAnUpdate = mano.move(x, y);
        }
        if(hasAnUpdate){
            actualFrame.updateValue(mano.getConvertedAngle(), 0);
            actualFrame.updateValue(brazo.getConvertedAngle(), 1);
        }
        return hasAnUpdate;
    }

    public void focusCanvas() {
        representacio.requestFocus();
    }

    public void mousePress(boolean state) {

        editing = state;

        focusCanvas();
        mano.mousePress(state);
        actualFrame.updateValue(mano.getConvertedAngle(), 0);

        brazo.mousePress(state);
        actualFrame.updateValue(brazo.getConvertedAngle(), 1);
    }


    public String getFileName() {
        return animationName.getText();
    }

    public void toogleWrite() {
        write = !write;
    }

    public boolean isWrite() {
        return write;
    }

    public RobotFrame getActualFrame() {
        return actualFrame;
    }

    public String getUpdateRate() {
        return frameRate.getText();
    }

    public void toogleRead() {
        this.read = !read;
    }

    public boolean isreadMode() {
        return read;
    }
}
