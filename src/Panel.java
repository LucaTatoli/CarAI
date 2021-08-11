import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class Panel extends JPanel {

    private int width, height;
    private boolean[][] track;
    public final static int BLOCKSIZE = 5;
    private ArrayList<AutoCar> car;
    private int running, brushRadius = 1;
    private float timer = 0;
    private final float MAXTIME = 120000;
    //CAR INITIAL VALUE
    int carSizeX = 10, carSizeY = 15, startX = 1 * BLOCKSIZE + carSizeX, startY = 1 * BLOCKSIZE + carSizeY, v = 100, theta = 0;

    public Panel() {
        width  = 1200;
        height = 800;

        track = new boolean[height/BLOCKSIZE][width/BLOCKSIZE];

        car = new ArrayList<>();
        Random r = new Random();
        for(int i = 0; i < 100; i++)
            car.add(new AutoCar(startX, startY, carSizeX, carSizeY, v, theta, track,
                    new float[]{r.nextInt(100), r.nextInt(100), r.nextInt(100), r.nextInt(10), r.nextInt(10), r.nextInt(50), r.nextInt(50)}));
        running = 0;
        setPreferredSize(new Dimension(width + 1, height + 1));
        setListeners();
    }

    public void update(float delta) {
        running = 0;

        //UPDATES EACH CAR AND CHECK HOW MANY ARE STILL RUNNING
        car.forEach(c -> {
            c.update(delta);
            running = c.isDead() ? running : running + 1;
        });

        timer += delta;

        //IF NO ONE'S RUNNING
        if(running == 0 || timer > MAXTIME) {
            //SLEEP 1s
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (Exception e) {}

            //RESET TIMER
            timer = 0;

            //SORT THE CARS FROM THE BEST TO THE WORST BY ROUTE VALUE
            car.sort((c1, c2) -> Math.round(c1.getRoute() * 100 - c2.getRoute() * 100));
            //PRINT THE BEST CAR STEER VALUE
            StringBuilder sb = new StringBuilder();
            sb.append("Best car steer value: < ");
            for(float f : car.get(0).getSteerValue())
                sb.append(f + ", ");
            sb.replace(sb.lastIndexOf(", "), sb.length(), " >");
            System.out.println(sb);

            //SET TOTAL ROUTE, SO I CAN SET THE REPRODUCTION PROBABILITY OF EACH CAR AS car.getRoute()/totRoute
            float totRoute = 0;
            for(AutoCar c : car)
                totRoute += c.getRoute();

            ArrayList<AutoCar> children = new ArrayList<>();
            Random r = new Random();

            //GENERATE CHILDREN
            for(int i = 0; i < car.size()/2; i++) {

                float[] p1 = null, p2 = null; //PARENTS STEERVALUE

                //GENERATE A RANDOM VALUE FOR THE PARENTS SELECTION
                float index1 = r.nextFloat() * totRoute;
                float index2 = r.nextFloat() * totRoute;

                float temp = 0; //TEMP VARIABLE FOR PARENTS SELECTION

                //SELECT THE PARENTS
                for(AutoCar c : car) {
                    temp += c.getRoute();
                    if(index1 <= temp && p1 == null)
                        p1 = c.getSteerValue();
                    else if(index2 <= temp && p2 == null)
                        p2 = c.getSteerValue();
                    else if(p1 != null && p2 != null)
                        break;
                }

                //SOMETIMES index1 = index2 = THE LAST CAR, SO I SELECT P2 AS THE BEST CAR WHEN THIS HAPPENS
                if(p2 == null)
                    p2 = car.get(0).getSteerValue();

                //MIX THE STEERVALUE OF EACH PARENT AND GENERATE TWO CHILDREN
                float[] c1 = new float[]{p1[0], p1[1], p1[2], p2[3], p2[4], p2[5], p2[6]}; float[] c2 = new float[]{p2[0], p2[1], p2[2], p1[3], p1[4], p1[5], p1[6]};

                //MUTATION
                if(r.nextFloat() < 0.1f)
                    c1[r.nextInt(7)] *= r.nextFloat() * 0.4f + 0.8f;
                if(r.nextFloat() < 0.1f)
                    c2[r.nextInt(7)] *= r.nextFloat() * 0.4f + 0.8f;

                //ADD CHILDREN TO ARRAYLIST
                children.add(new AutoCar(startX, startY, carSizeX, carSizeY, v, theta, track, c1));
                children.add(new AutoCar(startX, startY, carSizeX, carSizeY, v, theta, track, c2));
            }
            car = children;
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        g2d.setBackground(Color.DARK_GRAY);
        g2d.clearRect(0, 0, width, height);
        g2d.setColor(Color.black);

        //DRAW TRACK
        for(int j = 0; j < height/BLOCKSIZE; j++)
            for(int i = 0; i < width/BLOCKSIZE; i++) {
                if(track[j][i])
                    g2d.fillRect(i * BLOCKSIZE, j * BLOCKSIZE, BLOCKSIZE, BLOCKSIZE);
            }

        //DRAW CAR
        car.forEach(c -> c.draw(g2d));


    }

    public void setSpawn(int x, int y) {
        if(timer == 0)
            car.forEach(c -> c.setPos(x, y));
        startX = x;
        startY = y;
        System.out.println("SPAWN POINT: " + x + "  " + y);
    }

    private void setListeners() {
        addMouseMotionListener(new MouseMotionListener() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if(SwingUtilities.isLeftMouseButton(e)) {
                    for (int j = 0; j < brushRadius; j++)
                        for (int i = 0; i < brushRadius; i++)
                            try {
                                track[e.getY() / BLOCKSIZE + j][e.getX() / BLOCKSIZE + i] = false;
                            } catch (Exception exception) {
                            }
                }

                else if (SwingUtilities.isRightMouseButton(e))
                        for (int j = 0; j < brushRadius; j++)
                            for (int i = 0; i < brushRadius; i++)
                                try {
                                    track[e.getY() / BLOCKSIZE + j][e.getX() / BLOCKSIZE + i] = true;
                                } catch (Exception exception) {}
                }

            @Override
            public void mouseMoved(MouseEvent e) {

            }
        });

        addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if(SwingUtilities.isRightMouseButton(e))
                    setSpawn(e.getX(), e.getY());
                else if(SwingUtilities.isMiddleMouseButton(e)) {
                    for(int j = 0; j < track.length; j++)
                        for(int i = 0; i < track[0].length; i++)
                            track[j][i] = true;
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {

            }

            @Override
            public void mouseReleased(MouseEvent e) {

            }

            @Override
            public void mouseEntered(MouseEvent e) {

            }

            @Override
            public void mouseExited(MouseEvent e) {

            }
        });
    }

    public void setBrushRadius(int i) {
        brushRadius = i;
    }

    public int getBrushRadius() {
        return brushRadius;
    }

}
