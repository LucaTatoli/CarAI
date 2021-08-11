import javax.swing.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.concurrent.TimeUnit;

public class CarAI extends JFrame {

    private Panel panel;
    private boolean sleepFlag = false, pauseFlag = true;

    public CarAI() {
        panel = new Panel();
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        add(panel);
        pack();
        setResizable(false);
        setVisible(true);
    }

    public void update(float delta) {
        if(!pauseFlag)
            panel.update(delta);
        repaint();
    }

    public static void main(String[] args) {
        CarAI carAI = new CarAI();
        carAI.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
                if(e.getKeyChar() == 'x')
                    carAI.sleepFlag = !carAI.sleepFlag;
                if(e.getKeyChar() == 's')
                    carAI.pauseFlag = false;
                if(e.getKeyChar() == 'd')
                    carAI.panel.setBrushRadius(carAI.panel.getBrushRadius() + 1);
                if(e.getKeyChar() == 'a')
                    carAI.panel.setBrushRadius(carAI.panel.getBrushRadius() - 1);
            }

            @Override
            public void keyPressed(KeyEvent e) {

            }

            @Override
            public void keyReleased(KeyEvent e) {

            }
        });

        int delta = 1000/60;
        while(true) {
            carAI.update(delta);
            if(carAI.sleepFlag)
            try {
                TimeUnit.MILLISECONDS.sleep(delta);
            } catch (Exception e) {}
        }
    }
}
