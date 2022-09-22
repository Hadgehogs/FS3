package Lesson6.Expert;

public class Human {

    static class HumanBuilder{
        private Human fHuman;

        private HumanBuilder() {
            this.fHuman = new Human();
        }

        protected HumanBuilder name(String value){
            fHuman.setName(value);
            return this;
        }
        HumanBuilder age(int value){
            fHuman.setAge(value);
            return this;
        }
        HumanBuilder weight(int value){
            fHuman.setWeight(value);
            return this;
        }

        Human build(){
            return fHuman;
        }
    }

    private String name;
    private int age;
    private int weight;

    public String getName() {
        return name;
    }

    public int getAge() {
        return age;
    }

    public int getWeight() {
        return weight;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    static HumanBuilder builder(){
        return new HumanBuilder();
    }

    String info(){
        StringBuilder result=new StringBuilder();
        result.append(this.getName());
        result.append(" --- возраст:");
        result.append(this.getAge());
        result.append(" , вес:");
        result.append(this.getWeight());
        return result.toString();
    }
}
