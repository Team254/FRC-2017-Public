// NOTE: This file is available at http://algs4.cs.princeton.edu/14analysis/PolynomialRegression.java.html
package com.team254.lib.util.math;

/******************************************************************************
 *  Compilation:  javac -cp .:jama.jar PolynomialRegression.java
 *  Execution:    java  -cp .:jama.jar PolynomialRegression
 *  Dependencies: jama.jar StdOut.java
 * 
 *  % java -cp .:jama.jar PolynomialRegression
 *  0.01 n^3 + -1.64 n^2 + 168.92 n + -2113.73 (R^2 = 0.997)
 *
 ******************************************************************************/

import Jama.Matrix;
import Jama.QRDecomposition;

/**
 * The {@code PolynomialRegression} class performs a polynomial regression on an set of <em>N</em> data points (
 * <em>y<sub>i</sub></em>, <em>x<sub>i</sub></em>). That is, it fits a polynomial <em>y</em> = &beta;<sub>0</sub> +
 * &beta;<sub>1</sub> <em>x</em> + &beta;<sub>2</sub> <em>x</em><sup>2</sup> + ... + &beta;<sub><em>d</em></sub>
 * <em>x</em><sup><em>d</em></sup> (where <em>y</em> is the response variable, <em>x</em> is the predictor variable, and
 * the &beta;<sub><em>i</em></sub> are the regression coefficients) that minimizes the sum of squared residuals of the
 * multiple regression model. It also computes associated the coefficient of determination <em>R</em><sup>2</sup>.
 * <p>
 * This implementation performs a QR-decomposition of the underlying Vandermonde matrix, so it is not the fastest or
 * most numerically stable way to perform the polynomial regression.
 *
 * @author Robert Sedgewick
 * @author Kevin Wayne
 */
public class PolynomialRegression {
    private int degree; // degree of the polynomial regression
    private Matrix beta; // the polynomial regression coefficients
    private double sse; // sum of squares due to error
    private double sst; // total sum of squares

    public PolynomialRegression(double[][] xy, int degree) {
        double[] x = new double[xy.length];
        double[] y = new double[xy.length];
        for (int i = 0; i < xy.length; ++i) {
            x[i] = xy[i][0];
            y[i] = xy[i][1];
        }
        solve(x, y, degree);
    }

    /**
     * Performs a polynomial regression on the data points {@code (y[i], x[i])}.
     *
     * @param x
     *            the values of the predictor variable
     * @param y
     *            the corresponding values of the response variable
     * @param degree
     *            the degree of the polynomial to fit
     */
    public PolynomialRegression(double[] x, double[] y, int degree) {
        solve(x, y, degree);
    }

    private void solve(double[] x, double[] y, int degree) {
        this.degree = degree;

        int n = x.length;
        QRDecomposition qr = null;
        Matrix matrixX = null;

        // in case Vandermonde matrix does not have full rank, reduce degree until it does
        while (true) {

            // build Vandermonde matrix
            double[][] vandermonde = new double[n][this.degree + 1];
            for (int i = 0; i < n; i++) {
                for (int j = 0; j <= this.degree; j++) {
                    vandermonde[i][j] = Math.pow(x[i], j);
                }
            }
            matrixX = new Matrix(vandermonde);

            // find least squares solution
            qr = new QRDecomposition(matrixX);
            if (qr.isFullRank())
                break;

            // decrease degree and try again
            this.degree--;
        }

        // create matrix from vector
        Matrix matrixY = new Matrix(y, n);

        // linear regression coefficients
        beta = qr.solve(matrixY);

        // mean of y[] values
        double sum = 0.0;
        for (int i = 0; i < n; i++)
            sum += y[i];
        double mean = sum / n;

        // total variation to be accounted for
        for (int i = 0; i < n; i++) {
            double dev = y[i] - mean;
            sst += dev * dev;
        }

        // variation not accounted for
        Matrix residuals = matrixX.times(beta).minus(matrixY);
        sse = residuals.norm2() * residuals.norm2();
    }

    /**
     * Returns the {@code j}th regression coefficient.
     *
     * @param j
     *            the index
     * @return the {@code j}th regression coefficient
     */
    public double beta(int j) {
        // to make -0.0 print as 0.0
        if (Math.abs(beta.get(j, 0)) < 1E-4)
            return 0.0;
        return beta.get(j, 0);
    }

    /**
     * Returns the degree of the polynomial to fit.
     *
     * @return the degree of the polynomial to fit
     */
    public int degree() {
        return degree;
    }

    /**
     * Returns the coefficient of determination <em>R</em><sup>2</sup>.
     *
     * @return the coefficient of determination <em>R</em><sup>2</sup>, which is a real number between 0 and 1
     */
    public double R2() {
        if (sst == 0.0)
            return 1.0; // constant function
        return 1.0 - sse / sst;
    }

    /**
     * Returns the expected response {@code y} given the value of the predictor variable {@code x}.
     *
     * @param x
     *            the value of the predictor variable
     * @return the expected response {@code y} given the value of the predictor variable {@code x}
     */
    public double predict(double x) {
        // horner's method
        double y = 0.0;
        for (int j = degree; j >= 0; j--)
            y = beta(j) + (x * y);
        return y;
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        int j = degree;

        // ignoring leading zero coefficients
        while (j >= 0 && Math.abs(beta(j)) < 1E-5)
            j--;

        // create remaining terms
        while (j >= 0) {
            if (j == 0)
                s.append(String.format("%.2f ", beta(j)));
            else if (j == 1)
                s.append(String.format("%.2f x + ", beta(j)));
            else
                s.append(String.format("%.2f x^%d + ", beta(j), j));
            j--;
        }
        s = s.append("  (R^2 = " + String.format("%.3f", R2()) + ")");
        return s.toString();
    }
}
