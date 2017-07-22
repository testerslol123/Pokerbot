package main;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class Loc {

  private int x;
  private int y;

  public Loc(int x, int y) {
    this.x = x;
    this.y = y;
  }

  public Loc(Loc l1, Loc l2){
    this.x = l1.x + l2.x;
    this.y = l1.y + l2.y;
  }

  public int getX() {
    return x;
  }

  public void setX(int x) {
    this.x = x;
  }

  public int getY() {
    return y;
  }

  public void setY(int y) {
    this.y = y;
  }

  public void add(Loc loc){
    x+=loc.getX();
    y+=loc.getY();
  }

  public String toString(){
    return "x: "+x +" , y="+y;
  }

}
