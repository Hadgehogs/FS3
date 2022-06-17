package Lesson3;

import com.google.gson.*;

import javax.xml.crypto.Data;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class TTrafficStatistics {

    static enum TDirection {tDirectionInput, tDirectionOut}

    ;

    static class TDataTableRow {
        int regionNumber;
        TDirection direction;
        String carNumber;
        int carRegionCode = 0;
    }

    static ArrayList<TDataTableRow> dataTable;

    public static void GetTopPopularRegions() {
        HashMap<Integer, Integer> countOfRegionMove = new HashMap<>();
        // Мы пройдем по таблице данных, отбросим данные о выездах и в карту (соотвествие) будем вставлять номер региона и
        // количество въездов в него.
        for (TDataTableRow currentRow : dataTable) {
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
        System.out.println("Топ 5 регионов: " + topRegionNumbers.toString());
        // Обойдем эти регионы
        for (int topRegionNumber : topRegionNumbers) {
            // Аналогично строим соответсвия по количеству въездов машин
            HashMap<Integer, Integer> countOfincomingCars = new HashMap<>();
            for (TDataTableRow currentRow : dataTable) {
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
            int maxRegionNumber = 0;
            int maxCarIncoming = 0;

            // Перебираем соответствие и находим максимум на поличеству машин, сохраняя номер региона в пром.
            // переменной.

            for (Map.Entry<Integer, Integer> currentKeyValue : countOfincomingCars.entrySet()) {
                if (currentKeyValue.getValue() > maxCarIncoming) {
                    maxCarIncoming = currentKeyValue.getValue();
                    maxRegionNumber = currentKeyValue.getKey();
                }
            }
            System.out.println(topRegionNumber + " - больше всего въехало из региона " + maxRegionNumber + " всего машин " + maxCarIncoming);
        }

    }

    public static void GetSpecialNumbers() {
        int countOfSpecialNumbers = 0;

        // Тут все просто, даже непонятно, зачем оно нужно.
        for (TDataTableRow currentRow : dataTable) {
            String currentCarNumber = currentRow.carNumber;
            String beginPart = currentCarNumber.substring(0, 1);
            String endPart = currentCarNumber.substring(currentCarNumber.length() - 2, currentCarNumber.length());
            if ((beginPart.equals("М")) && (endPart.equals("АВ"))) {
                {
                    countOfSpecialNumbers++;
                }

            }
        }
        System.out.println("Количество спецномеров = " + countOfSpecialNumbers);
    }

    public static void ExtractData(Map<Integer, Map<String, String[]>> rawData) {
        dataTable = new ArrayList<>();

        for (Map.Entry<Integer, Map<String, String[]>> customRecord : rawData.entrySet()) {
            int regionNumber = customRecord.getKey();
            Map<String, String[]> subData = customRecord.getValue();
            for (Map.Entry<String, String[]> carRecords : subData.entrySet()) {
                String direction = carRecords.getKey();
                String[] carNumbers = carRecords.getValue();
                for (String carNumber : carNumbers) {
                    TDataTableRow dataTableRow = new TDataTableRow();
                    dataTableRow.direction = TDirection.tDirectionInput;
                    if (direction.toLowerCase(Locale.ROOT).equals("out")) {
                        dataTableRow.direction = TDirection.tDirectionOut;
                    }
                    dataTableRow.carNumber = carNumber;
                    dataTableRow.regionNumber = regionNumber;

                    String PatternString = "([а-яА-Я])(\\d{3})([а-яА-Я]{2})(\\d{2,3})"; // Код региона у нас в 4 группе
                    Pattern carNumberPattern = Pattern.compile(PatternString);
                    Matcher carNumberMatcher = carNumberPattern.matcher(carNumber);
                    if (carNumberMatcher.find()) {
                        String sRegionCode = carNumberMatcher.group(4);
                        int regionCode = Integer.parseInt(sRegionCode);
                        dataTableRow.carRegionCode = regionCode;
                    }
                    dataTable.add(dataTableRow);
                }
            }
        }
    }

    public static void main(String[] args) {
        Map<Integer, Map<String, String[]>> rawData = GeneratorExpertHomework.getData();
        ExtractData(rawData);
        GetTopPopularRegions();
        GetSpecialNumbers();
    }
}



