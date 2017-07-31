package main;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class CardSelector {

  private List<Card> cards;
  private List<Card> result = new ArrayList<>();

  //duplicate
  private int double1;
  private int double2;
  private int triple;
  private int quad;

  //flush
  private int[] flushIndex;
  private int flushSize;

  //straight
  private int straightIndex = 0;
  private int straightSize = 0;
  private boolean straightGap = false;

  public CardSelector(List<Card> cards) {
    this.cards = cards;
  }

  public void reset() {
    result.clear();
    double1 = -1;
    double2 = -1;
    triple = -1;
    quad = -1;
    flushSize = 0;
    straightSize = 0;

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
    if (result.isEmpty()) {
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
      //no duplicates, check for STRAIGHT or FLUSH

      //first try for full hands

      checkFlush(3);
      if (flushSize == 4) {
        //full flush with joker
        System.out.println("FOUND: 4 FLUSH");
        result.addAll(cards);
        return;
      }

      checkStraightB();
      if (straightSize == 4) {
        printStraight();
        result.addAll(cards);
        return;
      }

      //if no full hands, keep old straight values and check using ace
      int oldStraightIndex = straightIndex;
      //this is at most 3
      int oldStraightSize = straightSize;

      //only do this if there is an ace
      //always in second slot if you have joker
      Card aceCard = cards.get(1);
      if (aceCard.getValue() == 1) {
        //set ace value to 14 and try again, sort list again and check for straight
        aceCard.setValue(14);
        cards.sort(Comparator.comparingInt(Card::getValue));
        checkStraightB();

        //check full first
        if (straightSize == 4) {
          printStraight();
          result.addAll(cards);
          return;
        }

        //check 3 otherwise
        if (straightSize == 3) {
          printStraight();
          for (int i = 0; i < straightSize; i++) {
            result.add(cards.get(straightIndex + i));
          }
          //add the joker
          result.add(cards.get(0));
          return;
        }

        //restore everything
        aceCard.setValue(1);
        cards.sort(Comparator.comparingInt(Card::getValue));
        straightSize=oldStraightSize;
        straightIndex=oldStraightIndex;
      }

      //if no straight found with ace, check old one
      if (straightSize == 3) {
        printStraight();
        for (int i = 0; i < straightSize; i++) {
          result.add(cards.get(straightIndex + i));
        }
        //add the joker
        result.add(cards.get(0));
        return;

      }

      //last mesure, check if the flushsize was 3
      if (flushSize == 3) {
        System.out.println("FOUND: 3 FLUSH");
        for (int i = 0; i < flushSize; i++) {
          result.add(cards.get(flushIndex[i]));
        }
        //add the joker
        result.add(cards.get(0));
        return;
      }
    }
    //always keep the joker
    result.add(cards.get(0));
  }

  public void printStraight() {
    if (straightGap) {
      System.out.println("FOUND: " + straightSize + " STRAIGHT (GAP)");
    } else {
      System.out.println("FOUND: " + straightSize + " STRAIGHT");
    }
  }

  public void checkNormal() {
    if (!addDoubleTripleQuad()) {
      //no duplicates, check for straight or flush

      //first try for full hands

      checkFlush(4);
      if (flushSize == 5) {
        //full flush
        System.out.println("FOUND: FULL FLUSH");
        result.addAll(cards);
        return;
      }

      checkStraightB();
      if (straightSize == 5) {
        printStraight();
        result.addAll(cards);
        return;
      }

      //if no full hands, keep old straight values and check using ace
      int oldStraightIndex = straightIndex;
      //this is at most 3 or 4
      int oldStraightSize = straightSize;

      //only do this if there is an ace
      Card aceCard = cards.get(0);
      if (aceCard.getValue() == 1) {
        //set ace value to 14 and try again, sort list again and check for straight
        aceCard.setValue(14);
        cards.sort(Comparator.comparingInt(Card::getValue));
        checkStraightB();

        //check full first
        if (straightSize == 5) {
          printStraight();
          result.addAll(cards);
          return;
        }

        //check 4 otherwise
        if (straightSize == 4) {
          printStraight();
          for (int i = 0; i < straightSize; i++) {
            result.add(cards.get(straightIndex + i));
          }
          return;
        }

        //reset changes in list order
        aceCard.setValue(1);
        cards.sort(Comparator.comparingInt(Card::getValue));
      }

      //if no straight found with ace, check old one
      if (oldStraightSize == 4) {
        printStraight();
        for (int i = 0; i < oldStraightSize; i++) {
          result.add(cards.get(oldStraightIndex + i));
        }
        return;

      }

      //last mesure, check if the flushsize was 4
      if (flushSize == 4) {
        System.out.println("FOUND: 4 FLUSH");
        for (int i = 0; i < flushSize; i++) {
          result.add(cards.get(flushIndex[i]));
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

  //old version
  /*public boolean checkStraight() {
    boolean found;

    //length 5, 4, 3
    for (int j = 0; j < 3; j++) {
      //possible place inside string
      for (int k = 0; k < j + 1; k++) {
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
  }*/

  //assumes no duplicates
  public boolean checkStraightB() {

    //this should never get called
    if (double1 != -1 || triple != -1 || quad != -1) {
      System.out.println("found trip or dub");
      return false;
    }
    straightSize = -1;
    straightIndex = -1;

    //sum values to check
    //no gap = length-1
    //gap = length
    int diff = arrayStartEndDiff(0, 5);

    //length 5
    if (diff == 4) {
      straightIndex = 0;
      straightSize = 5;
      straightGap = false;
      return true;
    }

    //length 4
    for (int i = 0; i < 2; i++) {
      diff = arrayStartEndDiff(i, 4);
      if (diff == 3) {
        //no gap (better)
        straightIndex = i;
        straightSize = 4;
        straightGap = false;
        return true;
      }
      if (diff == 4) {
        //gap
        straightIndex = i;
        straightSize = 4;
        straightGap = true;
        return true;
      }
    }

    //length 3 (only for joker)
    for (int i = 0; i < 3; i++) {
      diff = arrayStartEndDiff(i, 3);
      if (diff == 2) {
        //no gap (better)
        straightIndex = i;
        straightSize = 3;
        straightGap = false;
        return true;
      }
      if (diff == 3) {
        //gap
        straightIndex = i;
        straightSize = 3;
        straightGap = true;
        return true;
      }
    }

    return false;
  }

  public int arrayStartEndDiff(int startIndex, int length) {
    int first = cards.get(startIndex).getValue();
    if (first == -1) {
      return -1;
    } else {
      return cards.get(startIndex + length - 1).getValue() - first;
    }

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
