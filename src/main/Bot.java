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
import java.util.Properties;
import java.util.Random;
import java.util.Scanner;
import javax.imageio.ImageIO;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

public class Bot implements Runnable {

  //settings
  private int runtime;
  private int safeRound;
  private int HLBound;
  private int clickDelay;
  private int delayNormal;
  private int delayHL;
  private int sound;
  private boolean medium;


  public Bot(Properties properties) {

    System.out.println("############READING SETTINGS");
    //runtime
    runtime = checkProperty(properties, "runtime", 60);
    if (runtime < 0) {
      runtime = 60;
    }
    System.out.println("runtime=" + runtime);

    int med = checkProperty(properties, "medium", 0);
    medium = !(med == 0);

    //delayNormal
    delayNormal = checkProperty(properties, "delayNormal", 3000);
    if (delayNormal < 0) {
      delayNormal = 3000;
    }
    if (delayNormal < 3000) {
      System.out.println("WARNING: delayNormal below 3000ms may cause the bot to desync");
    }
    System.out.println("delayNormal=" + delayNormal);

    //delayHL
    delayHL = checkProperty(properties, "delayHL", 2500);
    if (delayHL < 0) {
      delayHL = 2500;
    }
    if (delayHL < 2500) {
      System.out.println("WARNING: delayHL below 2500ms may cause the bot to desync");
    }
    System.out.println("delayHL=" + delayHL);

    //clickDelay
    clickDelay = checkProperty(properties, "clickDelay", 150);
    if (clickDelay < 0) {
      clickDelay = 150;
    }
    if (clickDelay < 150) {
      System.out.println("WARNING: clickDelay below 150ms causes the bot to act too quickly, possibly flagging you for making too many actions. Not recommended");
    }
    System.out.println("clickDelay=" + clickDelay);

    safeRound = checkProperty(properties, "safeRound", 7);
    System.out.println("safeRound=" + safeRound);

    HLBound = checkProperty(properties, "HLBound", 1);
    System.out.println("HLBound=" + HLBound);

    String[] soundString = new String[]{"Lyria singing", "Ifrit screaming", "Sagitarius warning"};
    sound = checkProperty(properties, "sound", 1);
    if (sound < 1 || sound > 3) {
      sound = 1;
    }
    System.out.println("Warning sound =" + (soundString[sound - 1]));
  }

  public int checkProperty(Properties properties, String name, int def) {
    int result;
    try {
      result = Integer.parseInt(properties.getProperty(name));
    } catch (NumberFormatException e) {
      System.out.println("Could not parse " + name + ", using default value");
      result = def;
    }
    return result;
  }

  //screenshot being used in current iteration
  private BufferedImage currentSS;

  //Large Settings
  private BufferedImage[] numberImages = new BufferedImage[10];
  private BufferedImage[] bigNumberImages = new BufferedImage[13];
  private BufferedImage[] letterImages = new BufferedImage[3];

  private Suit[] blackSuits = new Suit[]{Suit.SPADES, Suit.CLUBS, Suit.SPADES, Suit.CLUBS};
  private BufferedImage[] blackIcons;

  private Suit[] redSuits = new Suit[]{Suit.HEARTS, Suit.DIAMONDS, Suit.HEARTS, Suit.DIAMONDS};
  private BufferedImage[] redIcons;

  //Medium Settings
  private BufferedImage[] heartCards = new BufferedImage[13];
  private BufferedImage[] spadeCards = new BufferedImage[13];
  private BufferedImage[] diamondCards = new BufferedImage[13];
  private BufferedImage[] clubCards = new BufferedImage[13];


  private Loc start;

  private List<Card> cards;

  private CardSelector selector;

  private Random rand = new Random();

  //STAGE
  private int actionDelay;
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
  private int higherThan8 = 0;
  private int lowerThan8 = 0;

  public void testSuits() throws AWTException, IOException, InterruptedException {
    medium = true;

    init();
    System.out.println(start);
    clickFast(start);
    //identifyStage1();
  //  System.out.println(newStage);
    //initCards();
    while (true) {
      System.out.println("PRES ENTER");
      Scanner scanner = new Scanner(System.in);
      scanner.nextLine();
      currentSS = new Robot().createScreenCapture(new Rectangle(Toolkit.getDefaultToolkit().getScreenSize()));
      identifyStage1();
      System.out.println(newStage);
      /*identifyCardsMed();
      for (Card card : cards) {
        System.out.println(card);
      }*/
    }

  }

  public void run() {
    medium=true;
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
        long end = current + runtime * 60 * 1000;

        //random delay in ms between actions
        int delay;
        actionDelay = delayNormal;

        System.out.println("############STARTING BOT");

        //main loop
        while (current < end) {

          //take screenshot
          currentSS = new Robot().createScreenCapture(new Rectangle(Toolkit.getDefaultToolkit().getScreenSize()));

          if (choosing) {
            //search for WIN/LOSE
            actionDelay = delayNormal;
            identifyStage1();
          } else {
            actionDelay = delayHL;
            identifyStage2();
          }
          System.out.println("########################");
          System.out.println("FOUND STAGE:" + newStage);
          handle(newStage);

          //actionDelay+random delay in ms
          delay = rand.nextInt(300);
          Thread.sleep(actionDelay + delay);
          current = System.currentTimeMillis();
        }

      } else {
        System.out.println("ERROR: Could not find gameboard, make sure size is set to full and the whole gameboard is visible");
      }
    } catch (Exception e) {
      System.err.println(e);
    }

  }

  //handle current stage, usually 1 click
  private void handle(Stage stage) throws AWTException, InterruptedException, IOException {
    switch (stage) {
      case SELECT:
        otherRepeat = 0;
        identifyCards();
        selectCards();
        click(centerButton);
        break;

      case WIN:
        otherRepeat = 0;
        //reset values in case of desync
        currentCard = -1;
        nextCard = -1;
        currentRound = 1;
        choosing = false;
        click(rightButton);
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
        } else if (currentCard > 8) {
          click(rightButton);
        } else {
          //handling 8
          //look at the amount of cards that were lower/higher than 8 during this game
          if (higherThan8 > lowerThan8) {
            //click "lower" if there were less cards lower than 8 throughout this game
            click(rightButton);
          } else {
            //click higher otherwise
            click(leftButton);
          }
        }
        break;

      case WINHL:
        otherRepeat = 0;
        currentRound++;
        System.out.println("CURRENT ROUND=" + currentRound);
        identifyValueHL(false);
        System.out.println("NEXT CARD=" + nextCard);

        if (currentCard > 8) {
          higherThan8++;
        } else if (currentCard < 8) {
          lowerThan8++;
        }

        if (currentRound <= safeRound) {
          //always keep playing during unsafe rounds
          currentCard = nextCard;
          click(rightButton);
        } else {
          //during safe round check if card is within bound
          int diff = valueDiff(nextCard);
          if (diff > HLBound) {
            //continure if high diff
            currentCard = nextCard;
            click(rightButton);
          } else {
            //stop if low diff
            endHL();
            click(leftButton);
          }
        }
        break;

      case LOSEHL:
        otherRepeat = 0;
        endHL();
        click(centerButton);
        actionDelay = delayNormal;
        break;

      case DEAL:
        click(centerButton);
        break;

      case OTHER:
        otherRepeat++;
        if (otherRepeat > 5 && otherRepeat < 20) {
          choosing = !choosing;
          //otherRepeat = 0;
          //click(centerButton);
        }
        if (otherRepeat >= 20) {

          //play sound
          try {
            Clip clip = AudioSystem.getClip();
            AudioInputStream inputStream = AudioSystem.getAudioInputStream(
                getClass().getClassLoader().getResource("img/sound/alert" + sound + ".wav"));
            clip.open(inputStream);
            clip.start();
          } catch (Exception e) {
            System.out.println("COULD NOT PLAY SOUND");
          }

          System.out.println("########################");
          System.out.println("ERROR: the bot is unable to identify the current stage");
          System.out.println("Possible causes:");
          System.out.println("- Browser was moved");
          System.out.println("- Captcha popped up");
          System.out.println("- Something is covering the gameboard");
          System.out.println();
          System.out.println("Press ENTER to resume the bot");
          Scanner scanner = new Scanner(System.in);
          scanner.nextLine();

          otherRepeat = 0;
        }

        actionDelay = 500;
        break;
    }
  }

  //called after quitting HL to reset values
  private void endHL() {
    choosing = true;
    currentCard = -1;
    nextCard = -1;
    currentRound = 1;
    lowerThan8 = 0;
    higherThan8 = 0;
  }

  //used for detecting error
  private int otherRepeat = 0;

  private void init() throws AWTException, IOException, InterruptedException {
    System.out.println("############LOADING ASSETS");
    BufferedImage gameboard = null;

    //load base picture and icons
    try {

      //load card icons
      if (medium) {
        System.out.println("Using MEDIUM settings");
        for (int i = 1; i <= 13; i++) {
          heartCards[i - 1] = ImageIO.read(getClass().getClassLoader().getResource("img/medium/h/" + i + "h.png"));
          clubCards[i - 1] = ImageIO.read(getClass().getClassLoader().getResource("img/medium/c/" + i + "c.png"));
          diamondCards[i - 1] = ImageIO.read(getClass().getClassLoader().getResource("img/medium/d/" + i + "d.png"));
          spadeCards[i - 1] = ImageIO.read(getClass().getClassLoader().getResource("img/medium/s/" + i + "s.png"));
        }
        //big numbers for hl
        for (int i = 1; i < 14; i++) {
          bigNumberImages[i - 1] = ImageIO.read(getClass().getClassLoader().getResource("img/medium/bigNumber/" + i + ".png"));
        }

      } else {

        System.out.println("Using LARGE settings");

        //regular icons
        BufferedImage spadesIcon = ImageIO.read(getClass().getClassLoader().getResource("img/large/suit/spadeIcon.png"));
        BufferedImage clubsIcon = ImageIO.read(getClass().getClassLoader().getResource("img/large/suit/clubIcon.png"));
        BufferedImage heartsIcon = ImageIO.read(getClass().getClassLoader().getResource("img/large/suit/heartIcon.png"));
        BufferedImage diamondsIcon = ImageIO.read(getClass().getClassLoader().getResource("img/large/suit/diamondIcon.png"));

        //letter icons
        BufferedImage spadesLetterIcon = ImageIO.read(getClass().getClassLoader().getResource("img/large/suit/spadeLetterIcon.png"));
        BufferedImage clubsLetterIcon = ImageIO.read(getClass().getClassLoader().getResource("img/large/suit/clubLetterIcon.png"));
        BufferedImage heartsLetterIcon = ImageIO.read(getClass().getClassLoader().getResource("img/large/suit/heartLetterIcon.png"));
        BufferedImage diamondsLetterIcon = ImageIO.read(getClass().getClassLoader().getResource("img/large/suit/diamondLetterIcon.png"));

        //icons by color
        blackIcons = new BufferedImage[]{spadesIcon, clubsIcon, spadesLetterIcon, clubsLetterIcon};
        redIcons = new BufferedImage[]{heartsIcon, diamondsIcon, heartsLetterIcon, diamondsLetterIcon};

        //load small number images
        for (int i = 1; i < 11; i++) {
          numberImages[i - 1] = ImageIO.read(getClass().getClassLoader().getResource("img/large/number/" + i + "b.png"));
        }
        letterImages[0] = ImageIO.read(getClass().getClassLoader().getResource("img/large/number/jb.png"));
        letterImages[1] = ImageIO.read(getClass().getClassLoader().getResource("img/large/number/qb.png"));
        letterImages[2] = ImageIO.read(getClass().getClassLoader().getResource("img/large/number/kb.png"));

        //big numbers for hl
        for (int i = 1; i < 14; i++) {
          bigNumberImages[i - 1] = ImageIO.read(getClass().getClassLoader().getResource("img/large/bigNumber/" + i + ".png"));
        }
      }

      //stage icons
      String size = (medium) ? "medium" : "large";
      BufferedImage dealIcon = ImageIO.read(getClass().getClassLoader().getResource("img/" + size + "/stage/dealIcon.png"));
      BufferedImage selectIcon = ImageIO.read(getClass().getClassLoader().getResource("img/" + size + "/stage/selectIcon.png"));
      BufferedImage winIcon = ImageIO.read(getClass().getClassLoader().getResource("img/" + size + "/stage/winIcon.png"));
      BufferedImage loseIcon = ImageIO.read(getClass().getClassLoader().getResource("img/" + size + "/stage/loseIcon.png"));
      BufferedImage selectHLIcon = ImageIO.read(getClass().getClassLoader().getResource("img/" + size + "/stage/selectHLIcon.png"));
      BufferedImage winHLIcon = ImageIO.read(getClass().getClassLoader().getResource("img/" + size + "/stage/winHLIcon.png"));
      BufferedImage loseHLIcon = ImageIO.read(getClass().getClassLoader().getResource("img/" + size + "/stage/loseHLIcon.png"));

      stageIcons1 = new BufferedImage[]{selectIcon, winIcon, loseIcon, dealIcon};
      stageIcons2 = new BufferedImage[]{selectHLIcon, winHLIcon, loseHLIcon};
      System.out.println("############FINISHED LOADING");

      //gameboard hook
      gameboard = ImageIO.read(getClass().getClassLoader().getResource("img/" + size + "/gameboard.png"));


    } catch (IOException e) {
      System.out.println("############ERROR WHILE LOADING ASSETS");
    }

    System.out.println();
    System.out.println("The bot is set to run for " + runtime + " MINUTES");
    System.out.println("This can be changed in the settings.txt file");
    Scanner scanner = new Scanner(System.in);

    while (start == null) {
      Thread.sleep(1000);
      System.out.println();
      System.out.println("-Make sure the poker gameboard is fully visible and the game resolution is set to \"Lite\"");
      System.out.println("-Position your browser as close as you can to the top left corner of your screen");
      System.out.println("-If you are using viramate, disable the improved fonts");
      System.out.println();
      System.out.println("Press ENTER to start searching for the gameboard (may take 1 or 2 mins)");
      scanner.nextLine();
      System.out.println("############SEARCHING GAMEBOARD");
      //take ss
      currentSS = new Robot().createScreenCapture(new Rectangle(Toolkit.getDefaultToolkit().getScreenSize()));
      //find base pic in screenshot
      start = findMatches(currentSS, gameboard, 5, 25);

      if (start == null) {
        System.out.println("############GAMEBOARD NOT FOUND");
      } else {
        //adjust to old values
        if (!medium) {
          start.setY(start.getY() - 196);
        }

      }
    }

    //init buttons
    if (medium) {
      centerButton = new Loc(start, new Loc(226, 111));
      leftButton = new Loc(start, new Loc(153, 109));
      rightButton = new Loc(start, new Loc(300, 109));
    } else {
      centerButton = new Loc(start, new Loc(324, 726));
      leftButton = new Loc(start, new Loc(240, 726));
      rightButton = new Loc(start, new Loc(400, 726));
    }
  }

  //diff with 8
  private int valueDiff(int a) {
    return Math.abs(a - 8);
  }

  //identify stage outside of hl
  private void identifyStage1() throws AWTException {
    Loc stageLoc;
    BufferedImage stageIcon;
    if (medium) {
      stageLoc = new Loc(start, new Loc(167, -233));
      stageIcon = currentSS.getSubimage(stageLoc.getX(), stageLoc.getY(), 150, 100);
    } else    {
      stageLoc = new Loc(start, new Loc(285, 270));
      stageIcon = currentSS.getSubimage(stageLoc.getX(), stageLoc.getY(), 80, 50);
    }
    for (int i = 0; i < stageIcons1.length; i++) {
      Loc r = findMatches(stageIcon, stageIcons1[i], 10, 50);
      if (r != null) {
        newStage = stages1[i];
        return;
      }
    }
    newStage = Stage.OTHER;
  }

  //identify stage during hl
  private void identifyStage2() throws AWTException {
    Loc stageLoc;
    BufferedImage stageIcon;
    if (medium) {
      stageLoc = new Loc(start, new Loc(151, -273));
      stageIcon = currentSS.getSubimage(stageLoc.getX(), stageLoc.getY(), 150, 150);
    } else    {
      stageLoc = new Loc(start, new Loc(270, 220));
      stageIcon = currentSS.getSubimage(stageLoc.getX(), stageLoc.getY(), 100, 70);
    }
    //Loc stageLoc = new Loc(start, new Loc(270, 220));
   // BufferedImage stageIcon = currentSS.getSubimage(stageLoc.getX(), stageLoc.getY(), 100, 70);
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

    int baseX = 38;
    int baseY = 412;
    int cardDistance = 112;
    if (medium) {
      baseX = 15;
      baseY = -151;
      cardDistance = 84;
    }

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

  private void identifyCardsMed() {
    for (Card card : cards) {
      BufferedImage cardNum = currentSS.getSubimage(card.getLocation().getX(), card.getLocation().getY(), 40, 50);
      boolean found = false;
      if (isBlack(cardNum)) {
        //spade
        for (int i = 0; i < 13; i++) {
          Loc r = findMatches(cardNum, spadeCards[i], 15, 50);
          if (r != null) {
            card.setValue(i + 1);
            card.setSuit(Suit.SPADES);
            found = true;
            break;
          }
        }
        if (found) {
          continue;
        }
        //club
        for (int i = 0; i < 13; i++) {
          Loc r = findMatches(cardNum, clubCards[i], 15, 50);
          if (r != null) {
            card.setValue(i + 1);
            card.setSuit(Suit.CLUBS);
            found = true;
            break;
          }
        }
        if (found) {
          continue;
        }
        //joker
        card.setValue(-1);
        card.setSuit(Suit.JOKER);
      } else {
        //heart
        for (int i = 0; i < 13; i++) {
          Loc r = findMatches(cardNum, heartCards[i], 15, 50);
          if (r != null) {
            card.setValue(i + 1);
            card.setSuit(Suit.HEARTS);
            break;
          }
        }
        if (found) {
          continue;
        }
        //dia
        for (int i = 0; i < 13; i++) {
          Loc r = findMatches(cardNum, diamondCards[i], 15, 50);
          if (r != null) {
            card.setValue(i + 1);
            card.setSuit(Suit.DIAMONDS);
            break;
          }
        }
      }
      if (card.getValue() == 0) {
        System.out.println("not found number for card " + card.getOrder());
      }
    }
  }

  private void identifyCards() {
    if(medium){
      identifyCardsMed();
    }
    else{
      for (Card card : cards) {
        identifySuit(card);
      }
    }
  }

  //used for large settings
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
      Loc r = findMatches(cardIcon, icons[i], 9, 50);
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

  //used for large settings
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
    Loc l;
    int offset;
    if(medium){
      offset=147;
      l = new Loc(start, new Loc(95, -193));
    }else{
      offset=195;
      l = new Loc(start, new Loc(140, 306));
    }


    if (first) {
      cardNum = currentSS.getSubimage(l.getX(), l.getY(), 70, 100);
    } else {
      cardNum = currentSS.getSubimage(l.getX() + offset, l.getY(), 100, 100);
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
    x += rand.nextInt(30) - 15;
    y += rand.nextInt(30) - 15;

    PointerInfo a = MouseInfo.getPointerInfo();
    Point b = a.getLocation();
    int mx = (int) b.getX();
    int my = (int) b.getY();

    double t = clickDelay + rand.nextInt(250);
    double n = 50;
    double dx = (x - mx) / n;
    double dy = (y - my) / n;
    double dt = t / n;

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