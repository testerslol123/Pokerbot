## Granblue Fantasy Pokerbot
This is a free bot that plays Granblue Fantasy's poker game for you.

* This bot is meant to be used with the browser version of the game in chrome (http://game.granbluefantasy.jp/).

* The bot makes use of screenshots (image recognition) to determine its next move (no http requests at all or anything like that) and moves/clicks with the cursor to emulate a human, making it pretty much undetectable. 

* Earns about 3.4M coins an hour, note that the bot goes for "big" wins, meaning that it's possible you won't be seeing gains for a while (usually 1m every ~20 minutes).

* The recommended runtime for this bot is 1-2 hours. Anything more than 2 hours is dangerous and not recommended.

* If you decide to bot for more than 2 hours, keep in mind every ~3 hours or so you will have to fill in the captcha (the bot freezes when the captcha pops up and plays a warning sound) to keep going. Not filling in the captcha may get your account suspended.

* System requirements: runs on practically anything. On my i5 2500k, cpu usage sits at 2-10%.
* Requires [java 8](https://java.com/en/download/) installed. 

[1. Compilation](#1-compilation) <br />
[2. Ingame settings](#2-ingame-settings) <br />
[3. Starting the bot](#3-starting-the-bot) <br />
[4. Bot settings](#4-bot-settings) <br />
[5. Feedback](#5-feedback) <br />
 


# 1. Compilation
## (note: this is NOT necessary, only do this if you don't trust the .jar that I already generated or you are changing the code yourself)

* Windows: Simply run compile.bat to compile the jar (requires [jdk](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html) installed ).

# 2. Ingame settings 
## you MUST do this or the bot won't work at all
1. Go to settings (click top right on "menu", the red button "settings" is on the bottom right)
2. Click on "Animation/Resolution settings"
3. Set "Version Settings" to "Beta"
4. Set "Animations Settings" to "Lite" (recommended if your game lags often) OR set "Resolution Settings"  to "Lite" and save changes

![Step 3 settings](/src/img/readme/settings3.jpg)

5. Go back to settings, click on "Browser Version Settings"
6. Set "Window Size" to "Large"
7. Set "Bottom Menu" to "OFF"
8. Set "Automatic Resizing" to "OFF" and save changes

![Step 4 settings](/src/img/readme/settings4.jpg)

# 3. Starting the bot
1. Download the zipfile and extract it
2. Adjust settings as needed (see: [Bot settings](#4-bot-settings) )
3. 
* Windows: Run the "Pokerbot.bat" file (if you get an error here, make sure you have the latest version of java installed https://java.com/en/download/). A cmd window will open. The bot will show you the current settings and start loading assets.
* Linux: open a console. Type "java -version", the version should be 1.8.* (* is any number) or 1.9.*, if you get an error or your java is outdated, search in google how to update/download java for your system.<br />
If you have the right java version, run the following commands (replace PATH with the full path to the directory where you unpacked the zip):<br />
cd PATH<br />
java -jar Pokerbot.jar settings.txt<br />

4. Open your game in chrome and start a poker game
5. Now you need to make sure that the gameboard is visible.
If your screen is big enough just do it like this (expand your browser so the whole board is visible).

![Step 5 setup](/src/img/readme/step5.jpg)

For people who have small screens, you can make your browser a bit smaller:

![Step 5 setup small](/src/img/readme/step5new.jpg)

This is the ABSOLUTE MINIMUM that must be visible (just above the "full house x10" line and the buttons on the bottom of the gameboard fully visible). If you show any less than this the bot will not work.

![Step 5 setup minimum](/src/img/readme/step5minimum.jpg)

6. Place your browser window as close to the top left of the screen as you can (this makes the next step faster)
7. Press enter on the cmd window, the bot will now try to find the gameboard on your screen (DON'T MOVE YOUR BROWSER WINDOW AFTER THIS STEP)
8. The bot will start working and will stop after the amount of minutes in the settings file. (If you want to stop it earlier just close the cmd window)

If you get an error after 7 ("GAMEBOARD NOT FOUND"):
* Make sure your ingame settings are correct and you saved changes.
* Make sure you can see the gameboard as shown on the pictures of step 5. Also don't cover the gameboard with your mouse.
* Press enter again to restart the process on the cmd window.



# 4. Bot settings
To change the bot settings, open the "settings.txt" file and change the values as needed.
Here is an overview of the settings you can change. I recommend leaving everything as is and simply changing the runtime.

## Runtime
The most important setting is "runtime" on line 6 of the settings file ("runtime=60"). Change the 60 to the amount of minutes you want the bot to run.

Recommended value: 30 to 90

If you want to use the default (recommended) settings, just skip the rest of this section and read "Starting the bot" section.

## Higher-or-Lower settings
The following 2 settings will influence when the bot will stop playing Higher-or-Lower (HL).
### safeRound (line 13)
* after this round, the bot will start to "play safe" (= the bot won't continue HL if the odds are too low)
* value between 1 and 10
* a value of 1 will make it so the bot always plays safe
* a value of 10 will make it so the bot never plays safe (= always goes for 10 round wins)

Recommended value: 7-8 (for longer runs, runtime 60+ minutes), 5-6 (quick wins, useful when you are just short of being able to buy something) or 10 (big wins, runtime 90+ minutes, it could take a while for you to see gains if you get unlucky, but it should be ok in the long run)

### HLBound (line 17)
* this is the value used to determine whether the odds are too low or not, it is only used when "playing safe"
* the bot will continue playing HL only if the absolute difference between the current card and 8 is HIGHER than the HLBound value
* example: HLBound=1: the bot will stop if the next card is 7,8,9. for HLBound=2: the bot will stop if the next card is 6,7,8,9,10.

Recommended value: 1-2 (what I usually use is 2, any higher values are not efficient)

Here is an example of how it works:

* using safeRound=7, HLBound=2
* First 7 rounds: when asked to play another round (to double up again), the bot will always click "YES".
* After round 7: when asked to play another round, the bot will look at the next card that will be used.
* if the next card value is 6,7,8,9,10 the bot will click "NO".
* if it is any other card, the bot will click "YES".

## Delay settings
The bot uses a base "delay" between actions (while waiting for animations to finish, it does nothing). These values can be changed if your animations take longer (due to lag) or less (I play from Europe so my game lags from time to time compared to other people).

Also note that these value are not the exact values that will be used: all the delays are slightly randomized (0-500ms added randomly to every delay) to avoid automatic detection.

WARNING: changing these values may cause the bot to not work properly and desync (not the end of the world, the bot will still work but it will pause from time to time). ONLY CHANGE THESE SETTINGS IF YOUR GAME LAGS A LOT OR IF YOU FEEL LIKE IT COULD GO FASTER.

All values are in ms (millisecond).

### delayNormal (line 26)
* this is the delay while picking what cards to keep / dealing hand

Recommended value: 3000

### delayHL (line 29)
* this is the delay during the Higher-or-Lower segment
* animations during these segments are shorter, the delay used should be delayNormal-500

Recommended value: 2500

### clickDelay (line 32)
* How long it takes to move the cursor from one point to another
* The randomized value added to this delay is 0-250ms
* WARNING: don't set this value TOO low, the bot will try to click too fast and some of the clicks might not register. It may also trigger some detection flags if you do too many actions in a short period of time.

Recommended value: 150-200. 

## Other settings
### Sound warning
When the bot stops working (something blocks the gameboard / captcha shows up), a warning sound will play. You can change the warning sound in the settings (line 38).
These are the current available sounds:
1. Lyria singing
2. Ifrit screaming
3. Sagitarius warning

You can test this out by blocking the gameboard for a few seconds while the bot is running.

# 5. Feedback
There may be some bugs left that I overlooked, if you notice anything weird while the bot is running let me know by making an issue (https://github.com/tsuntsuntsuntsun/Pokerbot/issues).

For people feeling generous, you can donate here

[![paypal](https://www.paypalobjects.com/en_US/i/btn/btn_donateCC_LG.gif)](https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=N6YUUYVD4A32Y)
