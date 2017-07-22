package main;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class CardSelector {

  private List<Card> cards;
  private List<Card> result = new ArrayList<>();

  private int double1;
  private int double2;
  private int triple;
  private int quad;

  private int[] flushIndex;
  private int flushSize;

  private int straightIndex = 0;
  private int straightSize = 0;

  public CardSelector(List<Card> cards) {
    this.cards = cards;
  }

  public void reset() {
    result.clear();
    double1 = -1;
    double2 = -1;
    triple = -1;
    quad = -1;
    flushSize=0;
    straightSize=0;

  }

  public List<Card> select() {

    reset();
    cards.sort(Comparator.comparingInt(Card::getValue));

    printCards();
    markDoubleTriple();
    //printFound();
    if (cards.get(0).getValue() == -1) {
      //joker
      System.out.println("FOUND: JOKER");
      checkJoker();
    } else {
      checkNormal();
    }

    printKeptCards();

    cards.sort(Comparator.comparingInt(Card::getOrder));

    return result;
  }

  public void printCards() {
    System.out.println("############HAND");
    for (Card card : cards) {
      System.out.println(card);
    }
    System.out.println("############");
  }


  public void printKeptCards() {
    System.out.println("############KEPT");
    if(result.isEmpty()){
      System.out.println("NOTHING");
    }
    for (Card card : result) {
      System.out.println(card);
    }
    System.out.println("############");
  }

  public void markDoubleTriple() {
    int index = 0;
    int same = 0;
    int offset = 0;

    for (int i = offset; i < 4; i++) {
      if (cards.get(i).getValue() == cards.get(i + 1).getValue()) {
        same++;
      } else {
        if (same > 0) {
          if (same == 1) {
            if (double1 == -1) {
              double1 = index;
            } else {
              double2 = index;
            }
          } else if (same == 2) {
            triple = index;
          } else if (same == 3) {
            quad = index;
          }
        }
        same = 0;
        index = i + 1;
      }
    }
    if (same > 0) {
      if (same == 1) {
        if (double1 == -1) {
          double1 = index;
        } else {
          double2 = index;
        }
      } else if (same == 2) {
        triple = index;
      } else if (same == 3) {
        quad = index;
      }
    }
  }

  public void checkJoker() {
    if (!addDoubleTripleQuad()) {
      if (checkFlush(3) || checkStraight()) {

        if (flushSize == 4) {
          //full flush
          System.out.println("FOUND: 4 FLUSH");
          result.addAll(cards);
          return;
        }
        if (straightSize == 4) {
          //full straight
          System.out.println("FOUND: 4 STRAIGHT");
          result.addAll(cards);
          return;
        }
        if(straightSize==3){
          //4 straight
          System.out.println("FOUND: 3 STRAIGHT");
          for (int i = 0; i < straightSize; i++) {
            result.add(cards.get(straightIndex + i));
          }
        }
        else{
          //4 flush
          System.out.println("FOUND: 3 FLUSH");
          for (int i = 0; i < flushSize; i++) {
            result.add(cards.get(flushIndex[i]));
          }
        }
      }
    }
    result.add(cards.get(0));

  }

  public void checkNormal() {
    if (!addDoubleTripleQuad()) {

      if (checkFlush(4) || (checkStraight() && straightSize>3)) {
        if (flushSize == 5) {
          //full flush
          System.out.println("FOUND: FULL FLUSH");
          result.addAll(cards);
          return;
        }
        if (straightSize == 5) {
          //full straight
          System.out.println("FOUND: FULL STRAIGHT");
          result.addAll(cards);
          return;
        }
        if(straightSize==4){
          //4 straight
          System.out.println("FOUND: 4 STRAIGHT");
          for (int i = 0; i < straightSize; i++) {
            result.add(cards.get(straightIndex + i));
          }
        }
        else{
          //4 flush
          System.out.println("FOUND: 4 FLUSH");
          for (int i = 0; i < flushSize; i++) {
            result.add(cards.get(flushIndex[i]));
          }
        }
      }
    }
  }


  public boolean addDoubleTripleQuad() {
    if (triple != -1) {
      if (double1 != -1) {
        //full house
        System.out.println("FOUND: FULL HOUSE");
        for (int i = 0; i < 5; i++) {
          result.add(cards.get(i));
        }
        return true;

      } else {
        //only triple
        System.out.println("FOUND: TRIPLE");
        for (int i = 0; i < 3; i++) {
          result.add(cards.get(triple + i));
        }
        return true;
      }
    }

    if (double1 != -1) {
      //single double
      System.out.println("FOUND: PAIR");
      result.add(cards.get(double1));
      result.add(cards.get(double1 + 1));

      if (double2 != -1) {
        //double double
        System.out.println("FOUND: DOUBLE PAIR");
        result.add(cards.get(double2));
        result.add(cards.get(double2 + 1));
      }
      return true;
    }

    if (quad != -1) {
      //quad
      System.out.println("FOUND: QUAD");
      for (int i = 0; i < 4; i++) {
        result.add(cards.get(quad + i));
      }
      return true;
    }
    return false;
  }


  public boolean checkStraight() {
    boolean found;

    //length 5, 4, 3
    for (int j = 0; j < 3; j++) {
      //possible place inside string
      for(int k=0;k<j+1;k++){
        found = true;
        for (int i = 0; i < 4 - j; i++) {
          if (cards.get(i + k + 1).getValue() - cards.get(i + k).getValue() != 1) {
            found = false;
            break;
          }
        }
        if (found) {
          straightIndex = k;
          straightSize = 5 - j;
          return true;
        }
      }
    }
    return false;
  }

  public boolean checkFlush(int bound) {
    int[] index = new int[5];
    flushIndex = new int[5];
    flushSize = 0;
    int count = 0;

    for (Suit suit : Suit.values()) {
      for (int i = 0; i < 5; i++) {
        if (cards.get(i).getSuit() == suit) {
          index[count] = i;
          count++;
        }
      }

      if (count > flushSize) {
        flushSize = count;
        flushIndex = index.clone();
      }
      count = 0;
    }

    if (flushSize >= bound) {
      //flush found
      return true;
    } else {
      return false;
    }
  }

}
