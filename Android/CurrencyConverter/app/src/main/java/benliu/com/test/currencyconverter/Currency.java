package benliu.com.test.currencyconverter;

/**
 * Created by beliu on 2017-06-15.
 */

public class Currency {
    private  String name;

    public double getRate() {
        return rate;
    }

    public void setRate(double rate) {
        this.rate = rate;
    }

    private double rate;
    public Currency(String name, double rate){
        this.name = name;
        this.rate = rate;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return getName();
    }
}
