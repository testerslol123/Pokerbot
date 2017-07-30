package main;

import java.awt.AWTException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.Scanner;

public class Main {

  public static String img = "./src/img";

  public static void main(String[] args) throws AWTException, IOException {

    Properties properties = new Properties();
    Bot bot;

    //if no argument is given default settings will be used
    if (args.length > 0) {
      String filename = args[0];
      InputStream input = new FileInputStream(new File(filename));
      try {
        if (input == null) {
          System.out.println("ERROR: "+filename+" file not found in current folder");
          System.out.println("Using default settings");
        } else {
          properties = new Properties();
          //load a properties
          properties.load(input);

          //todo control settings

        }
      } catch (IOException ex) {
        System.out.println("Error while reading file");
      }
    }

    new Thread(new Bot(properties)).start();
  }
}
