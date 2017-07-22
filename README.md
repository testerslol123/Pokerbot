# Granblue Fantasy Pokerbot

This bot is meant to be used with the browser version of the game in chrome (http://game.granbluefantasy.jp/).

The bot makes use of screenshots to determine its next move (no http request at all) and moves the cursor to emulate a reel human bean, making it pretty much undetectable. 

During testing I ran the bot for 15 hours straight, earning about 3.4M coins an hour. Every 4 hours or so I had to fill in the captcha (the bot freezes when the captcha pops up) to keep going. It should be safe to bot for 1-2 hours but I don't recommend doing more than that.

It should run fine on any pc (need some feedback on this), on my 6 year old i5 2500k with only browser+bot running my cpu usage sits at 5-10%.

# Setup
Ingame settings (you MUST do this or the bot won't work at all):
1. Go to settings
2. click on "Animation/Resolution settings"
3. Set "Animations Settings" to "Lite" (recommended if your game lags often) OR set "Resolution Settings"  to "Lite" and save changes

![Step 3 settings](/src/img/readme/settings3.jpg)

4. Go back to settings, click on "Browser Version Settings"
5. Set "Window Size" to "Large"
6. Set "Bottom Menu" to "OFF"
7. Set "Automatic Resizing" to "OFF" and save changes

![Step 4 settings](/src/img/readme/settings4.jpg)


Using the bot:
1. Download the zipfile and extract it
2. Run the "Pokerbot.bat" file
3. In the cmd window that pops up, type in how long you want the bot to run (minutes) and press enter
4. Open your game in chrome and start a poker game
5. Make sure that the whole gameboard is visible (scroll all the way up and resize window if necessary)

![Step 5 setup](/src/img/readme/step5.jpg)

6. Press enter on the cmd window, the bot will now try to find the gameboard on your screen (DON'T MOVE YOUR BROWSER WINDOW AFTER THIS STEP)
7. The bot will start working and will stop after the amount of minutes in step 3. (If you want to stop it earlier just close the cmd window)

If you get an error after 6 (gameboard not found):
* Make sure your ingame settings are correct and you saved changes.
* Make sure you can see the whole gameboard as shown on the picture of step 5. Also don't cover the gameboard with your mouse.
* Press enter again to restart the process on the cmd window.
