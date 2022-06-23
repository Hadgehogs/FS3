package Lesson5.Expert;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class TrowData {
    String name;
    Double debet;
    Double credit;
    Date operationDate;
    Date operationMounth;
}

public class TfinReport {
    static ArrayList<TrowData> dataTable = new ArrayList<>();
    static void LoadFromFiles(URI resPath) {
        File dir = new File(resPath);
        FilenameFilter filter = new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                Boolean result = false;
                if (name.toUpperCase().endsWith(".TXT")) {
                    result = true;
                }
                return result;
            }
        };

        File[] txtFiles = dir.listFiles(filter);
        for (File txtFile : txtFiles) {
            String currentFileName = txtFile.getAbsolutePath();
            try {
                FileReader reader = new FileReader(currentFileName);
                BufferedReader bufReader = new BufferedReader(reader);
                Boolean firstLine = true;
                while (bufReader.ready()) {
                    String currentLine = bufReader.readLine();
                    if (firstLine) {
                        firstLine = false;
                        continue;
                    }
                    String[] arrayData = currentLine.split(";");
                    TrowData rowData = new TrowData();
                    rowData.name = arrayData[0];
                    rowData.debet = Double.parseDouble(arrayData[1]);
                    rowData.credit = Double.parseDouble(arrayData[2]);
                    SimpleDateFormat dateFormater = new SimpleDateFormat("dd/M/yyyy");
                    rowData.operationDate = dateFormater.parse(arrayData[3]);
                    Calendar calendar = new GregorianCalendar();
                    calendar.setTime(rowData.operationDate);
                    calendar.set(Calendar.DAY_OF_MONTH, 1);
                    rowData.operationMounth = calendar.getTime();
                    dataTable.add(rowData);
                }
            } catch (IOException fileExсpt) {
                System.out.println(String.format("Не удалось прочитать файл %s по причине %s", currentFileName, fileExсpt.getMessage()));
                return;
            } catch (ParseException dateExсpt) {
                System.out.println(String.format("Не удалось преобразовать данные о дате операции в формат даты по причине %s", dateExсpt.getMessage()));
                return;
            }
        }
    }

    static void getTotalsPerMonthByShop(String shopName) {
        Map<Date, Double> tmpResult = dataTable.stream().
                filter(rowData -> rowData.name.equals(shopName)).
                sorted(Comparator.comparing(rowData -> rowData.operationMounth)).
                collect(Collectors.groupingBy(rowData -> rowData.operationMounth, Collectors.summingDouble(rowData -> rowData.debet - rowData.credit)));

        LinkedHashMap<Date, Double> sortedResult=tmpResult.entrySet().stream().sorted(Comparator.comparing(Map.Entry::getKey)).
                collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                (e1, e2) -> e1, LinkedHashMap::new));

        System.out.println(String.format("Отчет о итоговой прибыли за каждый месяц по магазину %s",shopName));
        Calendar calendar=new GregorianCalendar();
        for (Map.Entry<Date, Double> rowData : sortedResult.entrySet()) {
            calendar.setTime((Date)rowData.getKey());
            String userFriendlyDate=String.format("%02d", calendar.get(Calendar.MONTH)+1)+"."+Integer.toString(calendar.get(Calendar.YEAR));
            DecimalFormat dF = new DecimalFormat( "#.00" );
            String userFriendlyValue=dF.format(rowData.getValue());
            System.out.println(userFriendlyDate + " : " + userFriendlyValue);
        }
    }
    static void getTotalsCreditsByShops() {
        Map<Object, Double> tmpResult = dataTable.stream().
                collect(Collectors.groupingBy(rowData -> rowData.name, Collectors.summingDouble(rowData -> rowData.credit)));

        System.out.println("Отчет о расходах всех магазинов за весь период по магазинам");
        for (Map.Entry<Object, Double> rowData : tmpResult.entrySet()) {
            String caption=String.format("Расходы %s за весь период",rowData.getKey());
            System.out.println(caption + " : " + rowData.getValue());
        }
    }

    public static void main(String[] args) throws IOException, URISyntaxException {
        DataGenerator.createReports();
        URL resURI = TfinReport.class.getClassLoader().getResource("");
        String resPath = resURI.toString();
        LoadFromFiles(resURI.toURI());
        String shopName = "pyterochka";
        getTotalsPerMonthByShop(shopName);
        System.out.println("                ----------------------------------------------              ");
        getTotalsCreditsByShops();
    }
}
