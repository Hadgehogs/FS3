package Lesson6.Expert;

public class loader {
    public static void main(String[] args) {
        System.out.println("------------------------");
        System.out.println("Первая задача:");
        Human human = Human.builder().name("Петр").age(20).weight(80).build();
        System.out.println(human.info());
        System.out.println("------------------------");

        System.out.println("Вторая задача:");
        Car goodCar=new Car(6000);
        CarShop carShop=new CarShop(goodCar);
        carShop.sellCar();
        System.out.println("------------------------");

        Car badCar=new Car(-6000);
        carShop=new CarShop(badCar);
        carShop.sellCar();
    }
}
