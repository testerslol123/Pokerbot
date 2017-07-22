package main;

import java.util.Random;

public class Card {

  private int order;
  private int value;
  private Suit suit;
  private Loc location;

  public Card(int order, Loc location){
    this.order=order;
    this.location = location;
  }

  public Card(int order, int value, Suit suit){
    this.order=order;
    this.value=value;
    this.suit= suit;
  }

  //random card
  public Card(int order){
    Suit[] suits=new Suit[]{Suit.DIAMONDS,Suit.CLUBS,Suit.HEARTS,Suit.SPADES};
    int[] vals = new int[13];
    Random random = new Random();
    value= random.nextInt(13)+1;
    suit= suits[random.nextInt(4)];
    this.order=order;
  }

  public int getValue() {
    return value;
  }

  public void setValue(int value) {
    this.value = value;
  }

  public Suit getSuit() {
    return suit;
  }

  public void setSuit(Suit suit) {
    this.suit = suit;
  }

  public int getOrder() {
    return order;
  }

  public Loc getLocation() {
    return location;
  }

  public String toString(){
    return "order="+order+" suit="+suit+" value="+value;
  }
}
