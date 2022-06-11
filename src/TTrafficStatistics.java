import com.google.gson.*;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


class TResult {

    String ErrorDescription;
    Boolean Result;
    int ErrorCode;
    public TResult() {
        this.ErrorDescription = "";
        this.Result = true;
        this.ErrorCode = 0;
    }
}

public class TTrafficStatistics {
    static class TLoadFromFileResult extends TResult {
        String textData;

    }
    ;

    static enum TDirection {tDirectionInput, tDirectionOut}

    ;

    static class TDataTableRow {
        int regionNumber;
        TDirection direction;
        String carNumber;
        int carRegionCode = 0;
    }

    static class TCustomPosts {
        ArrayList<TDataTableRow> dataTable;
    }

    static TCustomPosts CustomPosts;

    public static TLoadFromFileResult LoadFromFile(String FileName) {
        TLoadFromFileResult Result = new TLoadFromFileResult();

        FileReader trafficTxtReader = null;
        char[] readBuffer = new char[4096];
        Result.textData = new String("");
        try {
            trafficTxtReader = new FileReader(FileName);
        } catch (FileNotFoundException currentException) {
            Result.ErrorDescription = currentException.getMessage();
            Result.Result = false;
            return Result;
        }
        try {
            int bytesRead;
            while (true) {
                Arrays.fill(readBuffer, '0');
                bytesRead = trafficTxtReader.read(readBuffer);
                if (bytesRead <= 0) {
                    break;
                }
                readBuffer = Arrays.copyOf(readBuffer, bytesRead);
                String addData = String.valueOf(readBuffer);
                Result.textData = Result.textData + addData;
            }
            trafficTxtReader.close();
        } catch (IOException currentException) {
            Result.ErrorDescription = currentException.getMessage();
            Result.Result = false;
            return Result;
        }
        return Result;
    }

    static class TCustomPostsDeserializer implements JsonDeserializer<TCustomPosts> {
        @Override
        public TCustomPosts deserialize(JsonElement json, Type typeOfT,
                                        JsonDeserializationContext context) throws JsonParseException {
            TCustomPosts CustomPosts = new TCustomPosts();

            // У нас будет простая советская плоская таблица данных - список объектов класса TDataTableRow
            // С ней и будем дальше работать, а пока извлекем в нее данные
            ArrayList<TDataTableRow> dataTable = new ArrayList<>();

            JsonObject jCustomPosts = json.getAsJsonObject();
            for (String customNumber : jCustomPosts.keySet()) {
                JsonObject jCustomPost = jCustomPosts.getAsJsonObject(customNumber);
                for (String currentDirection : jCustomPost.keySet()) {
                    //JsonObject jDirection=jCustomPost.getAsJsonObject(currentDirection);
                    JsonArray jCars = jCustomPost.getAsJsonArray(currentDirection);
                    int dummy = 0;
                    for (int counter = 0; counter < jCars.size(); counter++) {
                        JsonElement jcar = jCars.get(counter);
                        String carNumber = jcar.getAsString();
                        TDataTableRow DataTableRow = new TDataTableRow();
                        DataTableRow.carNumber = carNumber;

                        String PatternString = "([а-яА-Я])(\\d{3})([а-яА-Я]{2})(\\d{2,3})"; // Код региона у нас в 4 группе
                        Pattern carNumberPattern = Pattern.compile(PatternString);
                        Matcher carNumberMatcher = carNumberPattern.matcher(carNumber);
                        if (carNumberMatcher.find()) {
                            String sRegionCode = carNumberMatcher.group(4);
                            int regionCode = Integer.parseInt(sRegionCode);
                            DataTableRow.carRegionCode = regionCode;
                        }
                        DataTableRow.direction = TDirection.tDirectionInput;
                        if (currentDirection.toLowerCase(Locale.ROOT).equals("out")) {
                            DataTableRow.direction = TDirection.tDirectionOut;
                        }
                        DataTableRow.regionNumber = Integer.parseInt(customNumber);
                        dataTable.add(DataTableRow);
                    }

                }
            }
            CustomPosts.dataTable = dataTable;
            return CustomPosts;
        }
    }

    public static void GetTopPopularRegions() {
        HashMap<Integer, Integer> countOfRegionMove = new HashMap<>();
        // Мы пройдем по таблице данных, отбросим данные о выездах и в карту (соотвествие) будем вставлять номер региона и
        // количество въездов в него.
        for (TDataTableRow currentRow : CustomPosts.dataTable) {
            if (currentRow.direction == TDirection.tDirectionOut) {
                continue;
            }
            int currentCount = 0;
            if (countOfRegionMove.containsKey(currentRow.regionNumber)) {
                currentCount = countOfRegionMove.get(currentRow.regionNumber);
            }
            currentCount++;
            countOfRegionMove.put(currentRow.regionNumber, currentCount);
        }

        //Выгрузим значения карты (соотвествия) в список, чтобы их отсортировать
        ArrayList<Integer> topCountOfRegionMoveTemp = new ArrayList<>(countOfRegionMove.values());
        topCountOfRegionMoveTemp.sort(null);
        // Порядок сортировки задать нельзя, но можно инвертировать список
        Collections.reverse(topCountOfRegionMoveTemp);
        ArrayList<Integer> topCountOfRegionMove = new ArrayList<>();
        int arrayBorder = topCountOfRegionMoveTemp.size();
        if (arrayBorder > 5) {
            arrayBorder = 5;
        }
        // Получим список из 5 элементов с максимальными количествами въездов.
        for (int counter = 0; counter < arrayBorder; counter++) {
            topCountOfRegionMove.add(topCountOfRegionMoveTemp.get(counter));
        }
        ArrayList<Integer> topRegionNumbers = new ArrayList<>();

        // Итого, у нас получился список из 5 элементов, в котором храняться наибольшее количество въездов по регионам.
        // Найдем эти регионы.

        // Обойдем список из 5 элементов. Мы не можем использовать метод contains() списка, так как у нас могут быть регионы
        // с одинаковым количеством заездов и количество подходящих регионово будет больше 5. Поэтому, приоритетно мы
        // выберем регионы и наибольшим числом заездов. Да, никаких merge_join, суровый nested_loop
        for (int currentCountRegionMove : topCountOfRegionMove) {
            countOfRegionMove.forEach((key, value) ->
            {
                if ((value == currentCountRegionMove) && (topRegionNumbers.size() < 5)) {
                    topRegionNumbers.add(key);
                }
            });
            if (topRegionNumbers.size() >= 5) {
                break;
            }
        }
        // Получили топ 5 регионов
        System.out.println("Топ 5 регионов: "+topRegionNumbers.toString());
        // Обойдем эти регионы
        for (int topRegionNumber : topRegionNumbers) {
            // Аналогично строим соответсвия по количеству въездов машин
            HashMap<Integer, Integer> countOfincomingCars = new HashMap<>();
            for (TDataTableRow currentRow : CustomPosts.dataTable) {
                if (!(currentRow.regionNumber == topRegionNumber)) {
                    continue; // Это не топовый текущий регион
                }
                if ((currentRow.direction == TDirection.tDirectionOut)) {
                    continue; // Это выезд машины, пропустим его
                }
                int currentCountOfincoming = 0;
                if (countOfincomingCars.containsKey(currentRow.carRegionCode)) {
                    currentCountOfincoming = countOfincomingCars.get(currentRow.carRegionCode);
                }
                currentCountOfincoming++;
                countOfincomingCars.put(currentRow.carRegionCode, currentCountOfincoming);
            }
            int maxRegionNumber=0;
            int maxCarIncoming=0;

            // Перебираем соответствие и находим максимум на поличеству машин, сохраняя номер региона в пром.
            // переменной.

            for(Map.Entry<Integer, Integer> currentKeyValue : countOfincomingCars.entrySet()) {
                if (currentKeyValue.getValue()>maxCarIncoming){
                    maxCarIncoming= currentKeyValue.getValue();
                    maxRegionNumber= currentKeyValue.getKey();
                }
            }
            System.out.println(topRegionNumber+" - больше всего въехало из региона "+maxRegionNumber+" всего машин "+maxCarIncoming);
        }

    }

    public static void GetSpecialNumbers() {
        int countOfSpecialNumbers=0;

        // Тут все просто, даже непонятно, зачем оно нужно.
        for (TDataTableRow currentRow : CustomPosts.dataTable) {
            String currentCarNumber=currentRow.carNumber;
            String beginPart=currentCarNumber.substring(0,1);
            String endPart=currentCarNumber.substring(currentCarNumber.length()-2,currentCarNumber.length());
            if ((beginPart.equals("М"))&&(endPart.equals("АВ"))){
            {
                countOfSpecialNumbers++;
            }

            }
        }
        System.out.println("Количество спецномеров = "+countOfSpecialNumbers);
    }

    public static TResult ExtractData(String textData) {
        TResult Result = new TResult();
        //Читаем файл через GSON, через свой десериализатор
        Gson gson = new GsonBuilder().registerTypeAdapter(TCustomPosts.class, new TCustomPostsDeserializer()).create();
        try {
            CustomPosts = gson.fromJson(textData, TCustomPosts.class);
        } catch (NumberFormatException e) {
            Result.Result = false;
            Result.ErrorDescription = "Json файл содержит символьные значения числовых полей, не допускающие преобразования к числам.";
            return Result;
        } catch (JsonParseException e) {
            Result.Result = false;
            Result.ErrorDescription = "Json файл содержит данные, которые не смог десериализовать компонент Gson компании Google";
            return Result;
        }

        return Result;
    }


    public static void main(String[] args) {

        TLoadFromFileResult loadResult = LoadFromFile("C:\\Java_Projects\\data.txt");
        if (!loadResult.Result) {
            System.out.print("Не удалось считать данные с файла по причине:" + loadResult.ErrorDescription);
            return;
        }

        TResult extractDataResult = ExtractData(loadResult.textData);
        if (!extractDataResult.Result) {
            System.out.print("Не удалось извлечь таблицу данных из исходных данных файла по причине:" + extractDataResult.ErrorDescription);
            return;
        }

        GetTopPopularRegions();
        GetSpecialNumbers();

    }
}



