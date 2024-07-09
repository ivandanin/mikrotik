package com.logger.mikrotik;

import jsat.ARFFLoader;
import jsat.DataSet;
import jsat.classifiers.DataPoint;
import me.legrange.mikrotik.ApiConnection;
import me.legrange.mikrotik.MikrotikApiException;
import org.springframework.stereotype.Service;

import javax.net.SocketFactory;
import java.io.*;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

abstract class Connector {

    private static ApiConnection connection;
    private Map<String, Double> labelToNumber = new HashMap<>();
    private Map<Double, String> numberToLabel = new HashMap<>();
    private double currentLabel = 0.0;

    protected void connect() throws MikrotikApiException {
        connection = ApiConnection.connect(SocketFactory.getDefault(), Config.HOST, ApiConnection.DEFAULT_PORT, 2000);
        connection.login(Config.USERNAME, Config.PASSWORD);
    }

    protected void disconnect() throws Exception {
        connection.close();
    }

    protected void read(Scanner scanner) throws MikrotikApiException, IOException {
        System.out.println("Please, insert a command you want to be executed!");
        while (!scanner.nextLine().equals("end")) {
            List<Map<String, String>> results = connection.execute(scanner.nextLine());
            for (Map<String, String> result : results) {
                System.out.println(result);
            CustomMetricsExporter.exportMetrics(result);
            }
            learn(results);
        }
    }

    protected void learn(List<Map<String, String>> results) throws IOException {
        File arffFile = new File("output.arff");

        // Check if the file exists
        fileExists(arffFile);

        // Write data to ARFF file
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(arffFile, true))) {
            // Check if the file is empty - if is - write the header, attributes and data annotations
            if (arffFile.length() == 0) {
                // Write ARFF header
                writer.write("@relation SampleData\n");

                // Extract attribute names from the first map in the list
                Map<String, String> firstDataMap = results.get(0);
                firstDataMap.keySet().remove(".id");
                firstDataMap.keySet().remove("time");
                for (String attributeName : firstDataMap.keySet()) {
                    writer.write("@attribute " + attributeName + " numeric\n");
                }

                writer.write("@data\n");
            }
            // Write data instances
            for (Map<String, String> dataMap : results) {
                StringBuilder line = new StringBuilder();
                String previousValue = null;
                //remove learning irrelevant data
                dataMap.remove(".id");
                dataMap.remove("time");
                for (String attributeValue : dataMap.values()) {
                    // check if the text is already contained in the arff file
                    String encodedValues = String.valueOf(encode(attributeValue));
                    String previousAndCurrentValue = previousValue + "," + encodedValues;
                    if (!containsTextInFile(arffFile, previousAndCurrentValue)) {
                        line.append(encodedValues).append(",");
                    }
                    previousValue = attributeValue;
                }
                if (line.length() > 0) {
                    line.setLength(line.length() - 1); // Remove the last comma
                    writer.write(line.toString() + "\n");
                    // Flushes the  written line from the writer. That way the line is added to the file before the application ends
                    // (that helps the containsTextInFile method to verify that the line is already present in the arff file)
                    writer.flush();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        DataSet dataSet = ARFFLoader.loadArffFile(arffFile);
    }

    private void fileExists(File file) throws IOException {
        if (!Files.exists(file.toPath())) {
            try {
                Files.createFile(file.toPath());
            } catch (FileAlreadyExistsException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean containsTextInFile(File file, String searchText) {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains(searchText)) {
                    return true; // Found the text in the file
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false; // Text not found in the file
    }

    public double encode(String label) {
        if (!labelToNumber.containsKey(label)) {
            labelToNumber.put(label, currentLabel);
            numberToLabel.put(currentLabel, label);
            currentLabel++;
        }
        return labelToNumber.get(label);
    }

    public String decode(double number) {
        return numberToLabel.get(number);
    }
}