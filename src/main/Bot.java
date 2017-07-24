package main;

import java.awt.AWTException;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.PointerInfo;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.InputEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.imageio.ImageIO;

public class Bot implements Runnable {

  //iterations
  private int minutes;
  private int waitTime;
  private int safeTurns;

  //screenshot being used in current iteration
  private BufferedImage currentSS;

  private BufferedImage[] numberImages = new BufferedImage[10];
  private BufferedImage[] bigNumberImages = new BufferedImage[13];
  private BufferedImage[] letterImages = new BufferedImage[3];

  private Suit[] blackSuits = new Suit[]{Suit.SPADES, Suit.CLUBS, Suit.SPADES, Suit.CLUBS};
  private BufferedImage[] blackIcons;

  private Suit[] redSuits = new Suit[]{Suit.HEARTS, Suit.DIAMONDS, Suit.HEARTS, Suit.DIAMONDS};
  private BufferedImage[] redIcons;

  private Loc start;

  private List<Card> cards;

  private CardSelector selector;

  private Random rand = new Random();

  //STAGE
  private boolean choosing;
  private Stage newStage;
  private Loc centerButton;
  private Loc rightButton;
  private Loc leftButton;
  private BufferedImage[] stageIcons1;
  private BufferedImage[] stageIcons2;
  private Stage[] stages1 = new Stage[]{Stage.SELECT, Stage.WIN, Stage.LOSE, Stage.DEAL};
  private Stage[] stages2 = new Stage[]{Stage.SELECTHL, Stage.WINHL, Stage.LOSEHL};

  //HL
  private int currentRound = 0;
  private int currentCard = -1;
  private int nextCard = -1;

  public Bot(int minutes, int safeTurns, int waitTime) {
    this.minutes = minutes;
    this.safeTurns=safeTurns;
    this.waitTime=waitTime;
  }

  public void test() throws AWTException {

  }

  public void run() {
    try {
      init();
      if (start != null) {
        //found game board
        System.out.println("############GAMEBOARD FOUND");
        //init card images
        initCards();

        //false during higher-lower, true otherwise
        choosing = true;

        long current = System.currentTimeMillis();
        long end = current + minutes*60*1000;

        //random delay in ms between actions
        int delay;

        System.out.println("############STARTING BOT");

        //main loop
        while (current<end){
          //take screenshot
          currentSS = new Robot().createScreenCapture(new Rectangle(Toolkit.getDefaultToolkit().getScreenSize()));

          if (choosing) {
            //search for WIN/LOSE
            identifyStage1();
          } else {
            identifyStage2();
          }
          System.out.println("########################");
          System.out.println("FOUND STAGE:" + newStage);
          handle(newStage);

          //waitTime in ms
          delay = rand.nextInt(500);
          Thread.sleep(waitTime+delay);
          current = System.currentTimeMillis();
        }

      } else {
        System.out.println("ERROR: Could not find gameboard, make sure size is set to full and the whole gameboard is visible");
      }
    } catch (Exception e) {
      System.out.println(e);
    }

  }

  //handle current stage, usually 1 click
  private void handle(Stage stage) throws AWTException, InterruptedException {
    switch (stage) {
      case SELECT:
        otherRepeat = 0;
        identifyCards();
        selectCards();
        click(centerButton);
        break;

      case WIN:
        otherRepeat = 0;
        click(rightButton);
        currentCard = -1;
        nextCard = -1;
        currentRound = 0;
        choosing = false;
        waitTime=2500;
        break;

      case LOSE:
        otherRepeat = 0;
        click(centerButton);
        break;

      case SELECTHL:
        otherRepeat = 0;
        if (currentCard == -1) {
          identifyValueHL(true);
        }
        System.out.println("CURRENT CARD = " + currentCard);
        if (currentCard == 1) {
          click(rightButton);
        } else if (currentCard < 8) {
          click(leftButton);
        } else {
          //TODO better 8 handling
          click(rightButton);
        }
        break;

      case WINHL:
        otherRepeat = 0;
        currentRound++;
        System.out.println("CURRENT ROUND=" + currentRound);
        identifyValueHL(false);
        System.out.println("NEXT CARD=" + nextCard);
        if (currentRound < 7) {
          //always keep playing if low round
          currentCard = nextCard;
          click(rightButton);
        } else {
          int diff = valueDiff(nextCard);
          if (diff > 2) {
            //continure if high diff
            currentCard = nextCard;
            click(rightButton);
          } else {
            //stop if low diff
            choosing = true;
            currentCard = -1;
            nextCard = -1;
            currentRound = 0;
            click(leftButton);
          }
        }
        break;

      case LOSEHL:
        otherRepeat = 0;
        choosing = true;
        currentCard = -1;
        nextCard = -1;
        currentRound = 0;
        click(centerButton);
        waitTime=3000;
        break;

      case DEAL:
        click(centerButton);
        break;

      case OTHER:
        otherRepeat++;
        if (otherRepeat > 3) {
          choosing = !choosing;
          otherRepeat = 0;
          click(centerButton);
        }
        waitTime=3000;
        break;
    }
  }

  private int otherRepeat = 0;

  private void init() throws AWTException, IOException {
    System.out.println("############LOADING ASSETS");
    BufferedImage gameboard = null;

    //load base picture and icons
    try {
      gameboard = ImageIO.read(getClass().getClassLoader().getResource("img/gameboard.png"));
      System.out.println("LOADING CARD SUIT ICONS");
      //regular icons
      BufferedImage spadesIcon = ImageIO.read(getClass().getClassLoader().getResource("img/suit/spadeIcon.png"));
      BufferedImage clubsIcon = ImageIO.read(getClass().getClassLoader().getResource("img/suit/clubIcon.png"));
      BufferedImage heartsIcon = ImageIO.read(getClass().getClassLoader().getResource("img/suit/heartIcon.png"));
      BufferedImage diamondsIcon = ImageIO.read(getClass().getClassLoader().getResource("img/suit/diamondIcon.png"));

      //letter icons
      BufferedImage spadesLetterIcon = ImageIO.read(getClass().getClassLoader().getResource("img/suit/spadeLetterIcon.png"));
      BufferedImage clubsLetterIcon = ImageIO.read(getClass().getClassLoader().getResource("img/suit/clubLetterIcon.png"));
      BufferedImage heartsLetterIcon = ImageIO.read(getClass().getClassLoader().getResource("img/suit/heartLetterIcon.png"));
      BufferedImage diamondsLetterIcon = ImageIO.read(getClass().getClassLoader().getResource("img/suit/diamondLetterIcon.png"));

      //icons by color
      blackIcons = new BufferedImage[]{spadesIcon, clubsIcon, spadesLetterIcon, clubsLetterIcon};
      redIcons = new BufferedImage[]{heartsIcon, diamondsIcon, heartsLetterIcon, diamondsLetterIcon};

      System.out.println("LOADING CARD VALUE ICONS");
      //load small number images
      for (int i = 1; i < 11; i++) {
        numberImages[i - 1] = ImageIO.read(getClass().getClassLoader().getResource("img/number/" + i + "b.png"));
      }
      letterImages[0] = ImageIO.read(getClass().getClassLoader().getResource("img/number/jb.png"));
      letterImages[1] = ImageIO.read(getClass().getClassLoader().getResource("img/number/qb.png"));
      letterImages[2] = ImageIO.read(getClass().getClassLoader().getResource("img/number/kb.png"));

      //big numbers
      for (int i = 1; i < 14; i++) {
        bigNumberImages[i - 1] = ImageIO.read(getClass().getClassLoader().getResource("img/bigNumber/" + i + ".png"));
      }


      System.out.println("LOADING STAGE ICONS");
      //stage icons
      BufferedImage dealIcon = ImageIO.read(getClass().getClassLoader().getResource("img/stage/dealIcon.png"));
      BufferedImage selectIcon = ImageIO.read(getClass().getClassLoader().getResource("img/stage/selectIcon.png"));
      BufferedImage winIcon = ImageIO.read(getClass().getClassLoader().getResource("img/stage/winIcon.png"));
      BufferedImage loseIcon = ImageIO.read(getClass().getClassLoader().getResource("img/stage/loseIcon.png"));
      BufferedImage selectHLIcon = ImageIO.read(getClass().getClassLoader().getResource("img/stage/selectHLIcon.png"));
      BufferedImage winHLIcon = ImageIO.read(getClass().getClassLoader().getResource("img/stage/winHLIcon.png"));
      BufferedImage loseHLIcon = ImageIO.read(getClass().getClassLoader().getResource("img/stage/loseHLIcon.png"));


      stageIcons1 = new BufferedImage[]{selectIcon, winIcon, loseIcon, dealIcon};
      stageIcons2 = new BufferedImage[]{selectHLIcon, winHLIcon, loseHLIcon};
      System.out.println("############FINISHED LOADING");

    } catch (IOException e) {
      System.out.println(e);
    }

    while(start==null){
      System.out.println();
      System.out.println("Make sure the poker gameboard is fully visible and the game resolution is set to \"Lite\" , then press ENTER");
      System.in.read();
      System.out.println("############SEARCHING GAMEBOARD");
      //take ss
      currentSS = new Robot().createScreenCapture(new Rectangle(Toolkit.getDefaultToolkit().getScreenSize()));
      //find base pic in screenshot
      start = findMatches(currentSS, gameboard, 30, 50);
      if (start == null) {
        System.out.println("############GAMEBOARD NOT FOUND");
      }
    }

    //init buttons
    centerButton = new Loc(start, new Loc(324, 726));
    leftButton = new Loc(start, new Loc(240, 726));
    rightButton = new Loc(start, new Loc(400, 726));


  }

  //diff with 8
  private int valueDiff(int a) {
    return Math.abs(a - 8);
  }

  private void identifyStage1() throws AWTException {
    Loc stageLoc = new Loc(start, new Loc(285, 270));
    BufferedImage stageIcon = currentSS.getSubimage(stageLoc.getX(), stageLoc.getY(), 80, 50);
    for (int i = 0; i < stageIcons1.length; i++) {
      Loc r = findMatches(stageIcon, stageIcons1[i], 10, 50);
      if (r != null) {
        newStage = stages1[i];
        return;
      }
    }
    newStage = Stage.OTHER;
  }

  private void identifyStage2() throws AWTException {
    Loc stageLoc = new Loc(start, new Loc(270, 220));
    BufferedImage stageIcon = currentSS.getSubimage(stageLoc.getX(), stageLoc.getY(), 100, 70);
    for (int i = 0; i < stageIcons2.length; i++) {
      Loc r = findMatches(stageIcon, stageIcons2[i], 10, 50);
      if (r != null) {
        newStage = stages2[i];
        return;
      }
    }
    newStage = Stage.OTHER;
  }

  private void selectCards() throws AWTException {
    selector = new CardSelector(cards);
    List<Card> cardsToSelect = selector.select();
    for (Card card : cardsToSelect) {
      click(card);
    }
  }


  //initialize values for the 5 cards in hand
  private void initCards() throws AWTException {

    int baseY = 412;
    int baseX = 38;
    int cardDistance = 112;

    cards = new ArrayList<>();
    Card card1 = new Card(1, new Loc(start, new Loc(baseX, baseY)));
    Card card2 = new Card(2, new Loc(start, new Loc(baseX + cardDistance + 1, baseY)));
    Card card3 = new Card(3, new Loc(start, new Loc(baseX + cardDistance * 2, baseY)));
    Card card4 = new Card(4, new Loc(start, new Loc(baseX + cardDistance * 3, baseY)));
    Card card5 = new Card(5, new Loc(start, new Loc(baseX + cardDistance * 4, baseY)));

    cards.add(card1);
    cards.add(card2);
    cards.add(card3);
    cards.add(card4);
    cards.add(card5);
  }

  private void identifyCards() {
    for (Card card : cards) {
      identifySuit(card);
    }
  }

  private void identifySuit(Card card) {
    //subimage of card suit icon
    BufferedImage cardIcon = currentSS.getSubimage(card.getLocation().getX(), card.getLocation().getY(), 25, 25);
    boolean isBlack = isBlack(cardIcon);

    BufferedImage[] icons;
    Suit[] suits;
    //pick icon array depending on color
    icons = (isBlack) ? blackIcons : redIcons;
    suits = (isBlack) ? blackSuits : redSuits;

    for (int i = 0; i < icons.length; i++) {
      Loc r = findMatches(cardIcon, icons[i], 10, 50);
      if (r != null) {
        card.setSuit(suits[i]);
        //first 2 elements in icons array are number suit icons, last are for letters
        if (i < 2) {
          identifyValue(card, numberImages, 1);
        } else {
          identifyValue(card, letterImages, 11);
        }
        return;
      }
    }

    //this is only reached when no match is found
    if (isBlack) {
      card.setSuit(Suit.JOKER);
      card.setValue(-1);
    } else {
      card.setSuit(suits[3]);
      identifyValue(card, letterImages, 11);
    }
  }

  //identifies the value of cards outside of HL
  //offset is 10 for letter cards, 1 for numbers
  private void identifyValue(Card card, BufferedImage[] images, int offset) {
    BufferedImage cardNum = currentSS.getSubimage(card.getLocation().getX(), card.getLocation().getY() - 30, 25, 35);
    for (int i = 0; i < images.length; i++) {
      Loc r = findMatches(cardNum, images[i], 15, 50);
      if (r != null) {
        card.setValue(i + offset);
      }
    }
    if (card.getValue() == 0) {
      System.out.println("not found number for card " + card.getOrder());
    }
  }

  //identifies the value of cards during HL
  //if first is true, looks at the left card, otherwise looks at the right card
  private void identifyValueHL(boolean first) {
    BufferedImage cardNum;
    Loc l = new Loc(start, new Loc(140, 306));
    if (first) {
      cardNum = currentSS.getSubimage(l.getX(), l.getY(), 70, 100);
    } else {
      cardNum = currentSS.getSubimage(l.getX() + 195, l.getY(), 100, 100);
    }
    for (int i = 0; i < bigNumberImages.length; i++) {
      Loc r = findMatches(cardNum, bigNumberImages[i], 20, 50);
      if (r != null) {
        if (first) {
          currentCard = i + 1;
        } else {
          nextCard = i + 1;
        }
        return;
      }
    }
    //should never happen, used for testing
    if (first) {
      System.out.println("not found number for FIRST CARD");
    } else {
      System.out.println("not found number for SECOND CARD");
    }


  }

  //return true if img has black in it
  private boolean isBlack(BufferedImage img) {
    for (int y = 0; y < img.getHeight(); y++) {
      for (int x = 0; x < img.getWidth(); x++) {
        if (((img.getRGB(x, y)) & 0xff) < 10 && ((img.getRGB(x, y) >> 8) & 0xff) < 10 && ((img.getRGB(x, y) >> 16) & 0xff) < 10) {
          return true;
        }
      }
    }
    return false;
  }

  //calls countMisses for each possible position inside bigImage
  //return null if not found. rerturns upperleft pixel location if found
  private Loc findMatches(BufferedImage bigImage, BufferedImage target, int maxMiss, int maxDiff) {
    for (int y = 0; y < bigImage.getHeight() - target.getHeight(); y++) {
      for (int x = 0; x < bigImage.getWidth() - target.getWidth(); x++) {
        int miss = countMisses(bigImage, x, y, target, maxMiss, maxDiff);
        if (miss < maxMiss) {
          return new Loc(x, y);
        }
      }
    }
    return null;
  }

  //search for target image inside bigImg by comparing pixel rbg value
  //maxDiff is max difference in rgb value between 2 pixels to consider it a miss
  //bound is max number of misses
  //bx and by are the start coordinates inside bigImg
  private int countMisses(BufferedImage bigImg, int bx, int by, BufferedImage target, int bound, int maxDiff) {
    int miss = 0;
    for (int y = 0; y < target.getHeight(); y++) {
      for (int x = 0; x < target.getWidth(); x++) {
        if (miss > bound) {
          return miss;
        }
        int val1 = (target.getRGB(x, y)) & 0xff;
        int val2 = (bigImg.getRGB(bx + x, by + y)) & 0xff;
        int diff = Math.abs(val1 - val2);
        if (diff > maxDiff) {
          miss++;
        }
      }
    }
    return miss;
  }

  //randomize the spot that is clicked when clicking cards
  private void click(Card card) throws AWTException {
    click(new Loc(card.getLocation(), new Loc(rand.nextInt(20) + 30, rand.nextInt(20) + 30)));
  }


  public void click(Loc loc) throws AWTException {
    click(loc.getX(), loc.getY());
  }

  //move mouse instantly and click
  public void clickFast(Loc loc) throws AWTException {
    Robot bot = new Robot();
    bot.mouseMove(loc.getX(), loc.getY());
  }

  //move mouse to location x,y and click
  //location and speed slightly randomized
  public void click(int x, int y) throws AWTException {
    x+=rand.nextInt(40)-20;
    y+=rand.nextInt(40)-20;

    PointerInfo a = MouseInfo.getPointerInfo();
    Point b = a.getLocation();
    int mx = (int) b.getX();
    int my = (int) b.getY();

    double t = 250+rand.nextInt(250);
    double n = 50;
    double dx = (x - mx) / n;
    double dy = (y - my) / n;
    double dt = t /  n;

    try {
      Robot bot = new Robot();
      for (int i = 0; i < n; i++) {
        Thread.sleep((int) dt);
        bot.mouseMove((int) (mx + dx * i), (int) (my + dy * i));
      }
      bot.mousePress(InputEvent.BUTTON1_MASK);
      bot.mouseRelease(InputEvent.BUTTON1_MASK);
    } catch (InterruptedException e) {
      System.out.println(e);
    }

  }
}