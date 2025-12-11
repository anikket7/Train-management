package com.trainApp;

import java.util.*;

public class TrainSystem {

    static final int MAX_SEATS = 5;
    static final int WAITING_LIMIT = 3;
    static final int TRAIN_COUNT = 11;

    // Train details: {Train Name, From, To}
    static String[][] trains = {
        {"Patna Express", "Patna", "Delhi"},
        {"Rajdhani", "Delhi", "Mumbai"},
        {"Goa Superfast", "Mumbai", "Goa"},
        {"Chennai Mail", "Chennai", "Bangalore"},
        {"Punjab Mail", "Amritsar", "Delhi"},
        {"Himalayan Queen", "Shimla", "Delhi"},
        {"Garib Rath", "Kolkata", "Lucknow"},
        {"Duronto Express", "Hyderabad", "Pune"},
        {"Shatabdi", "Bhopal", "Indore"},
        {"Intercity", "Ahmedabad", "Surat"},
        {"Intercity", "phagwara", "delhi"}
    };

    static class TrainData {
        String[] names = new String[MAX_SEATS];
        int[] ages = new int[MAX_SEATS];
        int[] pnrs = new int[MAX_SEATS];
        int seatCount = 0;

        Queue<String> waitNames = new LinkedList<>();
        Queue<Integer> waitAges = new LinkedList<>();
        Queue<Integer> waitPnrs = new LinkedList<>();
    }

    static TrainData[] trainData = new TrainData[TRAIN_COUNT];

    static {
        for (int i = 0; i < TRAIN_COUNT; i++) {
            trainData[i] = new TrainData();
        }
    }

    // Convert to lowercase
    static String toLower(String s) {
        return s.toLowerCase();
    }

    // PNR generator
    static int generatePNR() {
        Random rand = new Random();
        int pnr;
        boolean unique;

        do {
            unique = true;
            pnr = rand.nextInt(90000) + 10000;

            for (TrainData t : trainData) {

                for (int i = 0; i < t.seatCount; i++) {
                    if (t.pnrs[i] == pnr) {
                        unique = false;
                        break;
                    }
                }
                if (!unique) break;

                for (int w : t.waitPnrs) {
                    if (w == pnr) {
                        unique = false;
                        break;
                    }
                }
                if (!unique) break;
            }

        } while (!unique);

        return pnr;
    }

    // Search train
    static int searchTrain(String from, String to) {
        from = toLower(from);
        to = toLower(to);

        for (int i = 0; i < TRAIN_COUNT; i++) {
            if (toLower(trains[i][1]).equals(from) &&
                    toLower(trains[i][2]).equals(to)) {
                return i;
            }
        }
        return -1;
    }

    // Book ticket
    static void bookTicket(Scanner sc) {
        System.out.print("Enter From Station: ");
        String from = sc.next();

        System.out.print("Enter To Station: ");
        String to = sc.next();

        int idx = searchTrain(from, to);
        if (idx == -1) {
            System.out.println("No train found.");
            return;
        }

        System.out.println("Train Found: " + trains[idx][0]);

        System.out.print("Confirm booking (y/n)? ");
        char c = sc.next().charAt(0);

        if (c != 'y' && c != 'Y') {
            System.out.println("Booking cancelled.");
            return;
        }

        System.out.print("Enter Passenger Name: ");
        String name = sc.next();

        System.out.print("Enter Age: ");
        int age = sc.nextInt();

        int pnr = generatePNR();
        TrainData t = trainData[idx];

        if (t.seatCount < MAX_SEATS) {
            t.names[t.seatCount] = name;
            t.ages[t.seatCount] = age;
            t.pnrs[t.seatCount] = pnr;
            t.seatCount++;

            System.out.println("Ticket Booked. PNR: " + pnr);
        } else if (t.waitNames.size() < WAITING_LIMIT) {
            t.waitNames.add(name);
            t.waitAges.add(age);
            t.waitPnrs.add(pnr);

            System.out.println("Seats full. Added to waiting list. PNR: " + pnr);
        } else {
            System.out.println("Waiting list full. Cannot book.");
        }
    }

    // Cancel ticket
    static void cancelTicket(Scanner sc) {
        System.out.print("Enter PNR to cancel: ");
        int pnr = sc.nextInt();

        boolean found = false;

        for (int i = 0; i < TRAIN_COUNT; i++) {
            TrainData t = trainData[i];

            for (int j = 0; j < t.seatCount; j++) {
                if (t.pnrs[j] == pnr) {
                    found = true;
                    System.out.println("Ticket Cancelled for PNR " + pnr +
                            " on " + trains[i][0]);

                    for (int k = j; k < t.seatCount - 1; k++) {
                        t.names[k] = t.names[k + 1];
                        t.ages[k] = t.ages[k + 1];
                        t.pnrs[k] = t.pnrs[k + 1];
                    }
                    t.seatCount--;

                    if (!t.waitNames.isEmpty()) {
                        t.names[t.seatCount] = t.waitNames.poll();
                        t.ages[t.seatCount] = t.waitAges.poll();
                        t.pnrs[t.seatCount] = t.waitPnrs.poll();
                        t.seatCount++;

                        System.out.println("Waiting passenger confirmed.");
                    }
                    return;
                }
            }

            Queue<String> newWN = new LinkedList<>();
            Queue<Integer> newWA = new LinkedList<>();
            Queue<Integer> newWP = new LinkedList<>();

            boolean waitingFound = false;

            while (!t.waitPnrs.isEmpty()) {
                int wp = t.waitPnrs.poll();
                String wn = t.waitNames.poll();
                int wa = t.waitAges.poll();

                if (wp == pnr) {
                    waitingFound = true;
                    found = true;
                    System.out.println("Waiting ticket cancelled for PNR " + pnr);
                } else {
                    newWN.add(wn);
                    newWA.add(wa);
                    newWP.add(wp);
                }
            }

            t.waitNames = newWN;
            t.waitAges = newWA;
            t.waitPnrs = newWP;

            if (waitingFound) return;
        }

        if (!found) {
            System.out.println("PNR not found.");
        }
    }

    // Show all passengers
    static void showPassengers() {
        System.out.println("===== CONFIRMED PASSENGERS =====");

        boolean any = false;

        for (int i = 0; i < TRAIN_COUNT; i++) {
            TrainData t = trainData[i];

            if (t.seatCount > 0) {
                System.out.println("-- " + trains[i][0] + " --");

                for (int j = 0; j < t.seatCount; j++) {
                    any = true;
                    System.out.println("Passenger " + (j + 1));
                    System.out.println("Name: " + t.names[j]);
                    System.out.println("Age: " + t.ages[j]);
                    System.out.println("PNR: " + t.pnrs[j]);
                    System.out.println();
                }
            }
        }

        if (!any) System.out.println("No confirmed passengers.");

        System.out.println("\n===== WAITING LIST =====");

        boolean anyWait = false;

        for (int i = 0; i < TRAIN_COUNT; i++) {
            TrainData t = trainData[i];

            if (!t.waitNames.isEmpty()) {
                anyWait = true;
                System.out.println("-- " + trains[i][0] + " WAITING --");

                Queue<String> n = new LinkedList<>(t.waitNames);
                Queue<Integer> a = new LinkedList<>(t.waitAges);
                Queue<Integer> p = new LinkedList<>(t.waitPnrs);

                int c = 1;
                while (!n.isEmpty()) {
                    System.out.println("Passenger " + c++);
                    System.out.println("Name: " + n.poll());
                    System.out.println("Age: " + a.poll());
                    System.out.println("PNR: " + p.poll());
                    System.out.println();
                }
            }
        }

        if (!anyWait) System.out.println("No waiting passengers.");
    }

    // List trains
    static void viewTrains() {
        for (int i = 0; i < TRAIN_COUNT; i++) {
            System.out.println((i + 1) + ". " + trains[i][0] +
                    " (" + trains[i][1] + " → " + trains[i][2] + ")");
        }
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int ch;

        do {
            System.out.println("\n===== TRAIN MANAGEMENT SYSTEM =====");
            System.out.println("1. Search & Book Ticket");
            System.out.println("2. Cancel Ticket");
            System.out.println("3. Show Passengers");
            System.out.println("4. View All Trains");
            System.out.println("5. Exit");
            System.out.print("Enter choice: ");

            ch = sc.nextInt();

            switch (ch) {
                case 1 -> bookTicket(sc);
                case 2 -> cancelTicket(sc);
                case 3 -> showPassengers();
                case 4 -> viewTrains();
                case 5 -> System.out.println("Goodbye!");
                default -> System.out.println("Invalid choice.");
            }

        } while (ch != 5);

        sc.close();
    }
}