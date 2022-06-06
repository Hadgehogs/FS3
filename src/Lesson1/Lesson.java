package Lesson1;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Lesson {
    public static void main(String[] args) {

    String firstSample="<client>(Какие то данные)<data>79991113344;test@yandex.ru;Иванов Иван Иванович</data></client>";
    String secondSample="<client>(Какие то данные)<data></data></client>";
    String thirdSample="<client>(Какие то данные)<data>Иванов Иван Иванович;79991113344</data></client>";

    String firstResult = HideUserData_NewVersion(firstSample);
    String secondResult = HideUserData_NewVersion(secondSample);
    String thirdResult = HideUserData_NewVersion(thirdSample);
    System.out.print(firstSample+"  ---->  "+firstResult+"\n");
    System.out.print(secondSample+"  ---->  "+secondResult+"\n");
    System.out.print(thirdSample+"  ---->  "+thirdResult+"\n");

    }
    //Урок со звездочкой

    public static String HideUserData_NewVersion(String indata)
    {
        String result=indata;
        String PatternString="<data>.*</data>";
        // Нельзя просто взять и начать менять в исходном тексте, так как мы можем затронуть 'какие-то данные', пройдем внутрь тега <data>
        Pattern dataTagPattern = Pattern.compile(PatternString);
        Matcher dataTagMatcher = dataTagPattern.matcher(indata);
        if (dataTagMatcher.find()!=true)
        {
            return result;
        }
        String dataValue=dataTagMatcher.group();
        PatternString="\\d+"; // Шаблон на телефон
        Pattern telephonePattern = Pattern.compile(PatternString);
        Matcher telephoneMatcher = telephonePattern.matcher(dataValue);
        if (telephoneMatcher.find()==true)
        {
            String telNumber=telephoneMatcher.group();
            PatternString="(....)(...)(....)"; //Разобьем на группы оставим как есть 1 и 3 части, а 2 часть заменим
            Pattern telephoneModifyPattern = Pattern.compile(PatternString);
            Matcher telephoneModifyMatcher=telephoneModifyPattern.matcher(telNumber);
            if (telephoneModifyMatcher.find())
            {
            telNumber=telephoneModifyMatcher.group(1)+"*".repeat(telephoneModifyMatcher.group(2).length())+telephoneModifyMatcher.group(3);
                dataValue=telephoneMatcher.replaceAll(telNumber);
                //dataValue=dataTagMatcher.replaceAll(dataValue);
            }
        }
        PatternString="\\w+@\\w+\\.\\w{2,3}"; // Шаблон на почтовый ящик
        Pattern emailPattern = Pattern.compile(PatternString);
        Matcher emailMatcher = emailPattern.matcher(dataValue);
        if (emailMatcher.find()==true)
        {
            String email=emailMatcher.group();
            PatternString="(.*)(.)(@)(.*)(\\..{2,3})"; //Разобьем на группы, оставим 1,3,5 части и заменим 2 и 4 части
            Pattern emailModifyPattern = Pattern.compile(PatternString);
            Matcher emailModifyMatcher=emailModifyPattern.matcher(email);
            if (emailModifyMatcher.find())
            {
                email=emailModifyMatcher.group(1)+"*".repeat(emailModifyMatcher.group(2).length())+emailModifyMatcher.group(3)+"*".repeat(emailModifyMatcher.group(4).length())+emailModifyMatcher.group(5);
                dataValue=emailMatcher.replaceAll(email);
                //dataValue=dataTagMatcher.replaceAll(dataValue);
            }
        }
        PatternString="[а-яА-Я\\s]+"; // Шаблон на ФИО
        Pattern userIDPattern = Pattern.compile(PatternString);
        Matcher userIDMatcher = userIDPattern.matcher(dataValue);
        if (userIDMatcher.find()==true)
        {
            String userID=userIDMatcher.group();
            PatternString="(.)(.*)(.\\s.*\\s.)(.*)"; //Разобьем на группы, оставим 1,3 части и заменим 2. Отбросим 4 часть
            Pattern userIDModifyPattern = Pattern.compile(PatternString);
            Matcher userIDModifyMatcher=userIDModifyPattern.matcher(userID);
            if (userIDModifyMatcher.find())
            {
                userID=userIDModifyMatcher.group(1)+"*".repeat(userIDModifyMatcher.group(2).length())+userIDModifyMatcher.group(3)+".";
                dataValue=userIDMatcher.replaceAll(userID);
                //dataValue=dataTagMatcher.replaceAll(dataValue);
            }
        }
        dataValue=dataTagMatcher.replaceAll(dataValue);
     return dataValue;
    }


    public static String HideUserData_OldVersion(String indata)
    {
        if ((indata.contains("<data>")==false)|(indata.contains("</data>")==false))
        {throw new RuntimeException("Во входящих данных не найден раздел <data> "); // Хорошо бы завести record с полями result, errordescription, errorcode, и возвращать его функцией, но я пока не нашел, как делать структуры в Java, поэтому перехватите exception выше по вызову
        }

        String[] firstPartsOfIndata=indata.split("<data>"); // Разделим строку по тегу 'data', первую часть сохраним в результат
        String result=firstPartsOfIndata[0]+"<data>";
        String secondPartOfData=firstPartsOfIndata[1]; // 2 часть будем снова делить по закрывающему тегу
        String[] secondPartsOfData=secondPartOfData.split("</data>");
        String primaryData=secondPartsOfData[0]; // Внутри раздела
        if (primaryData.equals(""))
        {
            return indata;
        }

        String[] primaryDataParts=primaryData.split(";");

//        if (primaryDataParts.length!=3) {
//            throw new RuntimeException("Формат раздела <data> не соответствует требуемому формату <>;<>;<>");
//        }
        String outData=new String("");
        String fillPattern=new String("*");
        for (byte counter =0;counter<primaryDataParts.length;counter++)
        {
            String suffix=";";
            if (counter==primaryDataParts.length-1)
            {suffix="";}
            String currentData=primaryDataParts[counter];
            if (currentData.matches("[0-9]+")) // Все цифры в строке, это телефон
            {
                outData=outData+currentData.substring(0,4)+fillPattern.repeat(3)+currentData.substring(currentData.length()-4);
                outData=outData+suffix;

            }
            else if (currentData.contains("@")) // Это мыло
            {
                String[] partsOfEmail=currentData.split("@");
                String emailLogin=partsOfEmail[0];
                String emailDomain=partsOfEmail[1];
                String[] partsOfemailDomain=emailDomain.split("\\.");
                if ((partsOfemailDomain.length)!=2)
                {
                    throw new RuntimeException("Домен электронной почты пользователя не соответствует требуемому формату domain.ex");
                }
                String emailDomainName=partsOfemailDomain[0];
                String emailDomainExtension=partsOfemailDomain[1];
                outData=outData+emailLogin.substring(0,emailLogin.length()-1)+fillPattern+"@";
                outData=outData+fillPattern.repeat(emailDomainName.length())+".";
                outData=outData+emailDomainExtension;
                outData=outData+suffix;

            }
            else // ФИО
            {
                String[] partsOfUserName=currentData.split(" ");
                if ((partsOfUserName.length)!=3)
                {
                    throw new RuntimeException("Имя пользователя не соответствует требуемому формату Фамилия Имя Отчество");
                }
                String userSurname=partsOfUserName[0];
                String userName=partsOfUserName[1];
                String userMidName=partsOfUserName[2];
                outData=outData+userSurname.substring(0,1)+fillPattern.repeat(userSurname.length()-2)+userSurname.substring(userSurname.length()-1)+" ";
                outData=outData+userName+" ";
                outData=outData+userMidName.substring(0,1)+".";
                outData=outData+suffix;

            }

        }
        result=result+outData+"</data>"+secondPartsOfData[1];
        return result;
    }
}
