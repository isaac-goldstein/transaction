package com.acme.transaction;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class TransactionGenerator {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final Random RANDOM = new Random();

    public static void main(String[] args) {
        LocalDate startDate = LocalDate.of(2025, 9, 5);
        int durationDays = 90;
        LocalTime startTime = LocalTime.of(9, 0);
        LocalTime endTime = LocalTime.of(17, 0);
        int abaPoolSize = 100;
        int accountsPerAba = 5;
        String outputDir = "/";
        Map<String, List<String>> abaToAccountsMap = generateAbaToAccountMap(abaPoolSize, accountsPerAba);

        List<String> transactions = new ArrayList<>();
        for (int i = 0; i < durationDays; i++) {
            LocalDate currentDate = startDate.plusDays(i);
            generateDailyTransactions(currentDate, startTime, endTime, abaToAccountsMap, transactions);

        }
        writeToFile(transactions,  outputDir + "transactions.csv");
    }

    private static void generateDailyTransactions(LocalDate date, LocalTime start, LocalTime end,
                                                  Map<String, List<String>> abaToAccountsMap,
                                                  List<String> transactionList) {
        int transactionsPerDay = 50 + RANDOM.nextInt(100);
        List<String> abaPool = new ArrayList<>(abaToAccountsMap.keySet());

        for (int i = 0; i < transactionsPerDay; i++) {
            LocalTime transactionTime = generateRandomTime(start, end);

            String fromAba = abaPool.get(RANDOM.nextInt(abaPool.size()));
            List<String> associatedFromAccounts = abaToAccountsMap.get(fromAba);
            String fromAccountNo = associatedFromAccounts.get(RANDOM.nextInt(associatedFromAccounts.size()));

            String toAba;
            String toAccountNo;
            do {
                toAba = abaPool.get(RANDOM.nextInt(abaPool.size()));
                List<String> associatedToAccounts = abaToAccountsMap.get(toAba);
                toAccountNo = associatedToAccounts.get(RANDOM.nextInt(associatedToAccounts.size()));
            } while (toAba.equals(fromAba) && toAccountNo.equals(fromAccountNo));

            int amountInCents = 100 + RANDOM.nextInt(49900);
            // Format amount as dollars and cents for CSV
            String amount = String.format("%.2f", amountInCents / 100.0);

            String record = formatTransaction(date, transactionTime, fromAba, toAba, fromAccountNo, toAccountNo, amount);
            transactionList.add(record);
        }
    }

    private static LocalTime generateRandomTime(LocalTime start, LocalTime end) {
        int startSeconds = start.toSecondOfDay();
        int endSeconds = end.toSecondOfDay();
        int randomSeconds = startSeconds + RANDOM.nextInt(endSeconds - startSeconds);
        return LocalTime.ofSecondOfDay(randomSeconds);
    }

    private static String formatTransaction(LocalDate date, LocalTime time, String fromAba, String toAba,
                                            String fromAccountNo, String toAccountNo, String amount) {
        // Format the output as a CSV row
        return String.join(",",
                date.format(DATE_FORMATTER) + ":" + time.format(TIME_FORMATTER),
                fromAba,
                toAba,
                fromAccountNo,
                toAccountNo,
                amount,
                UUID.randomUUID().toString());
    }

    private static Map<String, List<String>> generateAbaToAccountMap(int abaCount, int accountsPerAba) {
        Map<String, List<String>> map = new HashMap<>();
        int accountCounter = 1;
        for (int i = 0; i < abaCount; i++) {
            long abaNum = 100000000L + (long) (RANDOM.nextDouble() * 900000000L);
            String abaStr = String.valueOf(abaNum);
            List<String> accounts = new ArrayList<>();
            for (int j = 0; j < accountsPerAba; j++) {
                accounts.add(String.format("%010d", accountCounter++));
            }
            map.put(abaStr, accounts);
        }
        return map;
    }

    private static void writeToFile(List<String> data, String filename) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(filename))) {
            // Write CSV header
            pw.println("Timestamp,FromABA,ToABA,FromAccount,ToAccount,Amount,TransactionID");
            for (String record : data) {
                pw.println(record);
            }
            System.out.println("\nSuccessfully wrote " + data.size() + " records to " + filename);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
