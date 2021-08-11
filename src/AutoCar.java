import java.awt.*;

public class AutoCar {

    private float posX, posY, v, vMax, vMin, alpha, l, route, dr, dl, da, xl, xr, yl, yr, xa, ya;
    private int width, height, sizeX, sizeY, x, y, x1, y1, x2, y2, x3, y3, x4, y4;
    private double theta;
    private boolean[][] track;
    private boolean dead = false;
    private float[] steerValue;

    public AutoCar(float posX, float posY, int sizeX, int sizeY, float v, int theta, boolean[][] track, float[] steerValue) {
        this.theta = theta;
        this.v = v;
        vMax = 250;
        vMin = -20;
        this.sizeX = sizeX;
        this.sizeY = sizeY;
        this.posX = posX;
        this.posY = posY;
        x = sizeX/2;
        y = sizeY/2;
        l = (float)Math.sqrt(x*x + y*y);
        alpha = (float)Math.asin(((float)sizeX/2 / l));
        this.track = track;
        this.steerValue = steerValue;
    }

    public void update(float delta) {
        posX += Math.cos(theta) * v * delta / 1000;
        posY += Math.sin(theta) * v * delta / 1000;

        //UPDATE ROUTE
        route += v * delta / 1000;

        //UPDATE VERTICES
        x1 = Math.round(l * (float)Math.cos(alpha + theta)); y1 = Math.round(l * (float)Math.sin(alpha + theta));
        x2 = Math.round(l * (float)Math.cos(Math.PI - alpha + theta)); y2 = Math.round(l * (float)Math.sin(Math.PI - alpha + theta));
        x3 = Math.round(l * (float)Math.cos(Math.PI + alpha + theta)); y3 = Math.round(l * (float)Math.sin(Math.PI + alpha + theta));
        x4 = Math.round(l * (float)Math.cos(-alpha + theta)); y4 = Math.round(l * (float)Math.sin(-alpha + theta));

        checkDistance();
        float distance;

        if(track[(int) Math.floor((y1 + posY) / Panel.BLOCKSIZE)][(int) Math.floor((x1 + posX) / Panel.BLOCKSIZE)] ||
           track[(int) Math.floor((y4 + posY) / Panel.BLOCKSIZE)][(int) Math.floor((x4 + posX) / Panel.BLOCKSIZE)]) {
            v = 0;
            dead = true;
        }

        //IF THE CAR IS STILL RUNNING
        //CHECK THE STEERVALUE TO DECIDE THE ACTIONS
        if(!dead) {
            if (dr < steerValue[0])
                rotate((float) Math.PI / 45);
            if (dl < steerValue[1])
                rotate((float) -Math.PI / 45);

            float diff = dl - dr;
            if (Math.abs(diff) > steerValue[2]) {
                if (diff / Math.abs(diff) > 0)
                    rotate((float) -Math.PI / 45);
                else
                    rotate((float) Math.PI / 45);
            }

            float motion = da / v;
            if(motion > steerValue[3])
                accelerate(steerValue[5]);   //TURBO
            if(motion < steerValue[4])
                accelerate(-steerValue[6]); //BRAKES
        }

    }

    public void draw(Graphics2D g2d) {

        //DRAW CAR
        g2d.setColor(Color.lightGray);
        if(dead)
            g2d.setColor(Color.black);
        g2d.fillPolygon(new int[]{x1 + Math.round(posX), x2 + Math.round(posX), x3 + Math.round(posX)}, new int[]{y1 + Math.round(posY), y2 + Math.round(posY), y3 + Math.round(posY)}, 3);
        g2d.fillPolygon(new int[]{x1 + Math.round(posX), x3 + Math.round(posX), x4 + Math.round(posX)}, new int[]{y1 + Math.round(posY), y3 + Math.round(posY), y4 + Math.round(posY)}, 3);
        g2d.setColor(Color.blue);
        g2d.drawLine(x1 + Math.round(posX), y1 + Math.round(posY), x2 + Math.round(posX), y2 + Math.round(posY));
        g2d.drawLine(x2 + Math.round(posX), y2 + Math.round(posY), x3 + Math.round(posX), y3 + Math.round(posY));
        g2d.drawLine(x3 + Math.round(posX), y3 + Math.round(posY), x4 + Math.round(posX), y4 + Math.round(posY));
        g2d.drawLine(x4 + Math.round(posX), y4 + Math.round(posY), x1 + Math.round(posX), y1 + Math.round(posY));
        //DRAW SENSORS
//        g2d.drawLine(x1 + Math.round(posX), y1 + Math.round(posY), Math.round(xr * dr + posX), Math.round(yr * dr + posY));
//        g2d.drawLine(x4 + Math.round(posX), y4 + Math.round(posY), Math.round(xl * dl + posX), Math.round(yl * dl + posY));
//        g2d.drawLine(x1 + Math.round(posX) - sizeX/2, y1 + Math.round(posY) - sizeY/2, Math.round(xa * da + posX - sizeX/2), Math.round(ya * da + posY - sizeY/2));
    }

    public void checkDistance() {
        xr = (float)Math.cos( alpha + theta) * v; yr = (float)Math.sin( alpha + theta) * v;
        xl = (float)Math.cos(-alpha + theta) * v; yl = (float)Math.sin(-alpha + theta) * v;
        xa = (float)Math.cos(         theta) * v; ya = (float)Math.sin(         theta) * v;
        float lr = (float)Math.sqrt(xr * xr + yr * yr); float ll = (float)Math.sqrt(xl * xl + yl * yl);
        xr /= lr; yr /= lr;
        xl /= ll; yl /= ll;
        xa /=  v; ya /=  v;
        dr = 1; dl = 1; da = 1;

        try {
            while (!track[(int) Math.floor((yr * dr + posY) / Panel.BLOCKSIZE)][(int) Math.floor((xr * dr + posX) / Panel.BLOCKSIZE)])
                dr += 0.1f;

            while (!track[(int) Math.floor((yl * dl + posY) / Panel.BLOCKSIZE)][(int) Math.floor((xl * dl + posX) / Panel.BLOCKSIZE)])
                dl += 0.1f;

            while (!track[(int) Math.floor((ya * da + posY) / Panel.BLOCKSIZE)][(int) Math.floor((xa * da + posX) / Panel.BLOCKSIZE)])
                da += 0.1f;
        } catch (Exception e) {
            dr = 1;
            dl = 1;
            da = 1;
        }
    }

    public void rotate(float delta) {
        theta += delta * (1 - v/vMax);
    }

    public void accelerate(float a) {
        v += a;
        if(v < vMin)
            v = vMin;
        else if(v > vMax)
            v = vMax;
    }

    public boolean isDead() {
        return dead;
    }

    public float getRoute() {
         return route;
    }

    public float[] getSteerValue() {
        return steerValue;
    }

    public void setSteerValue(float[] steerValue) {
        this.steerValue = steerValue;
    }

    public void setPos(int x, int y) {
        posX = x;
        posY = y;
    }

}
