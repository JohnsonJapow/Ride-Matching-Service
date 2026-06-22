package org.core;

import java.math.BigDecimal;
import java.util.Scanner;

public class Utility {
    protected static int readValidOption(Scanner sc, String label) {
        while (true) {
            System.out.print(label);
            String input = sc.nextLine().trim();

            if (null == input || input.isEmpty()) {
                System.out.println("[Error] Option cannot be empty. Please enter a number between 1 and 5.");
                continue;
            }

            try {
                int option = Integer.parseInt(input);

                if (option >= 1 && option <= 5) {
                    return option;
                }
                System.out.println("[Error] Invalid input. Please enter a number between 1 and 5.");

            } catch (NumberFormatException e) {
                System.out.println("[Error] Invalid format! Please enter a valid integer number.");
            }
        }
    }

    protected static String readValidString(Scanner sc, String label) {
        while (true) {
            System.out.print(label + ": ");
            String input = sc.nextLine().trim();

            if (null != input && !input.isEmpty()) {
                return input;
            }
            System.out.println("[Error] This field cannot be empty. Please try again.");
        }
    }

    protected static String readCoordinate(Scanner sc, String label) {
        while (true) {
            System.out.print(label + ": ");
            String input = sc.nextLine().trim();
            if (null == input || input.isEmpty()) {
                System.out.println("[Error] Location cannot be empty. Please enter a number.");
                continue;
            }
            try {
                new BigDecimal(input);
                return input;
            } catch (NumberFormatException e) {
                System.out.println("[Error] Invalid format! Please enter a valid decimal number.");
            }
        }
    }

    protected static int readPositiveNumber(Scanner sc, String label) {
        while (true) {
            System.out.print(label + ": ");
            String input = sc.nextLine().trim();
            if (null == input || input.isEmpty()) {
                System.out.println("[Error] Input cannot be empty. Please enter a positive number.");
                continue;
            }
            try {
                int result = Integer.parseInt(input);
                if (result > 0) {
                    return result;
                } else {
                    System.out.println("[Error] Please enter a positive number.");
                }
            } catch (NumberFormatException e) {
                System.out.println("[Error] Invalid format! Please enter a positive number.");
            }
        }
    }
}
