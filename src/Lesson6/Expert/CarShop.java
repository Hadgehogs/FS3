package Lesson6.Expert;

public class CarShop {

    public Car getCar() {
        return fCar;
    }

    public void setCar(Car value) {
        this.fCar = value;
    }

    private Car fCar;

    public CarShop(Car fCar) {
        this.fCar = fCar;
    }

    void sellCar(){

        StringBuilder result=new StringBuilder();
        result.append("А до Чикого довезет? Довезет до Сан-Франциско и обратно. Молодой человек, мы, русские, не обманываем друг друга. Цена:\n");
        try {
            result.append(this.getCar().showPrice()+"\n");
            result.append("Хочешь купить авто?");

        }
        catch (Car.eBadPrice excp){
            result.append(excp.getMessage()+"\n");
            result.append("Давайте посмотрим другое авто?");
        }
        System.out.println(result.toString());

    }
}
