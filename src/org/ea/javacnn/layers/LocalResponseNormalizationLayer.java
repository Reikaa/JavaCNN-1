package org.ea.javacnn.layers;

import org.ea.javacnn.data.BackPropResult;
import org.ea.javacnn.data.DataBlock;
import org.ea.javacnn.data.OutputDefinition;

import java.util.ArrayList;
import java.util.List;

/**
 * This layer normalize the result from the convolution layer so all
 * weight values are positive, this will help the learning process and
 * shape the result.
 *
 * @author Daniel Persson (mailto.woden@gmail.com)
 */
public class LocalResponseNormalizationLayer implements Layer {
    private int out_depth, out_sx, out_sy;

    private DataBlock in_act, out_act, S_cache_;

    public LocalResponseNormalizationLayer(OutputDefinition def) {
        this.k = opt.k;
        this.n = opt.n;
        this.alpha = opt.alpha;
        this.beta = opt.beta;

        // computed
        this.out_sx = def.getOutX();
        this.out_sy = def.getOutY();
        this.out_depth = def.getDepth();

        // checks
        if(this.n%2 == 0) {
            System.out.println("WARNING n should be odd for LRN layer");
        }

    }

    @Override
    public DataBlock forward(DataBlock db, boolean training) {
        this.in_act = db;

        DataBlock A = db.cloneAndZero();
        this.S_cache_ = db.cloneAndZero();
        double n2 = Math.floor(this.n/2);
        for(int x=0;x<db.getSX();x++) {
            for(int y=0;y<db.getSY();y++) {
                for(int i=0;i<db.getDepth();i++) {

                    double ai = db.getWeight(x,y,i);

                    // normalize in a window of size n
                    double den = 0.0;
                    for(int j=(int)Math.max(0,i-n2);j<=Math.min(i+n2,db.getDepth()-1);j++) {
                        double aa = db.getWeight(x,y,j);
                        den += aa*aa;
                    }
                    den *= this.alpha / this.n;
                    den += this.k;
                    this.S_cache_.setWeight(x,y,i,den); // will be useful for backprop
                    den = Math.pow(den, this.beta);
                    A.setWeight(x,y,i,ai/den);
                }
            }
        }

        this.out_act = A;
        return this.out_act; // dummy identity function for now
    }

    @Override
    public void backward() {
        // evaluate gradient wrt data
        DataBlock V = this.in_act; // we need to set dw of this
        V.clearGradient();
        DataBlock A = this.out_act; // computed in forward pass

        int n2 = (int)Math.floor(this.n/2);
        for(int x=0;x<V.getSX();x++) {
            for(int y=0;y<V.getSY();y++) {
                for(int i=0;i<V.getDepth();i++) {

                    double chain_grad = this.out_act.getGradient(x,y,i);
                    double S = this.S_cache_.getWeight(x,y,i);
                    double SB = Math.pow(S, this.beta);
                    double SB2 = SB*SB;

                    // normalize in a window of size n
                    for(int j=(int)Math.max(0,i-n2);j<=Math.min(i+n2,V.getDepth()-1);j++) {
                        double aj = V.getWeight(x,y,j);
                        double g = -aj*this.beta*Math.pow(S,this.beta-1)*this.alpha/this.n*2*aj;
                        if(j==i) g+= SB;
                        g /= SB2;
                        g *= chain_grad;
                        V.addGradient(x,y,j,g);
                    }

                }
            }
        }
    }

    @Override
    public List<BackPropResult> getBackPropagationResult() {
        return new ArrayList<BackPropResult>();
    }
}
