package org.example;


import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Random;
import java.util.TimerTask;

public class MockDataSource extends TimerTask {

    private final Random random;

    public MockDataSource() {
        super();
        random = new Random();
    }

    private String getRandomStationNr() {
        int firstDigit = random.nextInt(0, 2 + 1);
        int bound = firstDigit == 2 ? 5 : 9;
        int secondDigit = random.nextInt(0, bound + 1);
        return String.valueOf(firstDigit) + String.valueOf(secondDigit);
    }

    @Override
    public void run() {
        // station name
        byte[] stationName = ("team 01-" + getRandomStationNr()).getBytes(StandardCharsets.UTF_8);

        // random user data
        byte[] randomData = new byte[14];
        random.nextBytes(randomData);

        // assemble
        byte[] userData = Arrays.copyOf(stationName, stationName.length + randomData.length);
        System.arraycopy(randomData, 0, userData, stationName.length, randomData.length);

        // output
        try {
            System.out.write(userData);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
