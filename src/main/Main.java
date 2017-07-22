package main;

import java.awt.AWTException;
import java.io.IOException;
import java.util.Scanner;

public class Main {

  public static String img= "./src/img";

  public static void main(String[] args) throws AWTException {
    int minutes=-1;
    int waitTime=3000; //ms
    int safeTurns=7;

    String input="";
    Scanner sc = new Scanner(System.in);

    System.out.println("Type in the amount of minutes the bot should run, then press ENTER");
    while(minutes<1){
      input = sc.nextLine();
      try
      {
        minutes =  Integer.parseInt(input);
      }
      catch(NumberFormatException nfe)
      {
        System.out.println("Invalid input. Try again");
        continue;
      }
      if(minutes<1){
        System.out.println("Value must be bigger than 0");
      }
    }
    System.out.println("The bot will run for "+minutes+" minutes");
    System.out.println();

    new Thread(new Bot(minutes,safeTurns,waitTime)).start();
  }
}
