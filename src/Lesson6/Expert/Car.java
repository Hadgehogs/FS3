package Lesson6.Expert;

public class Car {

    public Car(int price) {

        this.setPrice(price);
    }

    static class eBadPrice extends Exception{
        public eBadPrice(String message) {
            super(message);
        }
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public int getPrice() {
        return price;
    }

    private int price;

    String showPrice() throws eBadPrice {
        if (this.getPrice()<0) {
            throw new eBadPrice("Цена мне неизвеста");
        }
       return Integer.toString(this.getPrice());
    }

}
