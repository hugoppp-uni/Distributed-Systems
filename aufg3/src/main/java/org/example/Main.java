package org.example;

import java.util.Timer;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class Main {

    private static final int DATA_SOURCE_OUTPUT_PERIOD = 100;
    private static final int DATA_SOURCE_INITIAL_DELAY = 100;

    private static void printUsage() {
        System.out.println("Usage:");
        System.out.println("\tinterface name");
        System.out.println("\tmulticast address");
        System.out.println("\treceive port");
        System.out.println("\tstation class");
        System.out.println("\tutc offset ms");
        System.exit(0);
    }
    public static void main(String[] args) {
        if(args.length != 5) printUsage();

        // schedule data source
//        MockDataSource dataSource = new MockDataSource();
//        Timer outputTimer = new Timer(true);
//        outputTimer.scheduleAtFixedRate(dataSource, DATA_SOURCE_INITIAL_DELAY, DATA_SOURCE_OUTPUT_PERIOD);

        //args
        String interfaceName = args[0];
        String mcastAddress = args[1];
        short receivePort = Short.parseShort(args[2]);
        StationClass stationClass = args[3].equals("A") ? StationClass.A : StationClass.B;
        long utcOffsetMs = Long.parseLong(args[4]);

        // init station
        Station station = new Station(interfaceName, mcastAddress, receivePort, stationClass, utcOffsetMs);
        station.activate();
    }
}