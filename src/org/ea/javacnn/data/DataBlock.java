package org.ea.javacnn.data;

import java.util.Arrays;
import java.util.Random;

/**
 * Holding all the data handled by the network. So a layer will receive
 * this class and return a similar block as a output that will be used
 * by the next layer in the chain.
 *
 * @author Daniel Persson (mailto.woden@gmail.com)
 */
public class DataBlock {
    private int sx;
    private int sy;
    private int depth;
    private double[] w;
    private double[] dw;

    public DataBlock(int sx, int sy, int depth) {
        this(sx, sy, depth, -1.0);
    }

    public DataBlock(int sx, int sy, int depth, double c) {
        this.sx = sx;
        this.sy = sy;
        this.depth = depth;
        int n = sx * sy * depth;

        this.w = new double[n];
        this.dw = new double[n];
        if(c != -1.0) {
            Arrays.fill(this.w, c);
        } else {
            double scale = Math.sqrt(1.0 / n);
            Random r = new Random();
            for(int i=0; i<n; i++) {
                this.w[i] = r.nextDouble() * scale;
            }
        }
        Arrays.fill(this.dw, 0);
    }

    public int getSX() {
        return sx;
    }

    public int getSY() {
        return sy;
    }

    public int getDepth() {
        return depth;
    }

    public double getWeight(int idx) {
        return this.w[idx];
    }

    public double getWeight(int x, int y, int depth) {
        int ix = ((this.sx * y) + x) * this.depth + depth;
        return this.w[ix];
    }

    public void setWeight(int x, int y, int depth, double val) {
        int ix = ((this.sx * y) + x) * this.depth + depth;
        this.w[ix] = val;
    }

    public void addWeight(int x, int y, int depth, double val) {
        int ix = ((this.sx * y) + x) * this.depth + depth;
        this.w[ix] += val;
    }

    public double getGradient(int x, int y, int depth) {
        int ix = ((this.sx * y) + x) * this.depth + depth;
        return this.dw[ix];
    }

    public void setGradient(int x, int y, int depth, double val) {
        int ix = ((this.sx * y) + x) * this.depth + depth;
        this.dw[ix] = val;
    }

    public void addGradient(int x, int y, int depth, double val) {
        int ix = ((this.sx * y) + x) * this.depth + depth;
        this.dw[ix] += val;
    }

    public DataBlock cloneAndZero() {
        return new DataBlock(this.sx, this.sy, this.depth, 0.0);
    }

    public DataBlock clone() {
        DataBlock db = new DataBlock(this.sx, this.sy, this.depth, 0.0);
        for(int i=0; i<this.w.length; i++) {
            db.w[i] = this.w[i];
        }
        return db;
    }

    public void addFrom(DataBlock db) {
        for(int i=0; i<this.w.length; i++) {
            this.w[i] = db.w[i];
        }
    }

    public void addFromScaled(DataBlock db, double a) {
        for(int i=0; i<this.w.length; i++) {
            this.w[i] = db.w[i] * a;
        }
    }

    public void setConst(double a) {
        for(int i=0; i<this.w.length; i++) {
            this.w[i] = a;
        }
    }
}
