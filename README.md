
## Setup Instructions for the Grove sensor

1. Follow the instructions here: https://wiki.seeedstudio.com/Grove-GSR_Sensor/
2. Follow instructions here: https://wiki.seeedstudio.com/Grove_Base_Hat_for_Raspberry_Pi/#installation
    You need to cd into grove.py and run `pip install`

If you run into an error with the sensor location like this: `Check whether I2C enabled and   Grove Base Hat RPi  or  Grove Base Hat RPi Zero  inserted`

To fix this, you'll need to change the address, as detailed here: https://forum.seeedstudio.com/t/airquality-sensor-on-grove-pi-at-facing-check-whether-i2c-enabled-and-grove-base-hat-rpi-or-grove-base-hat-rpi-zero-inserted/259371/6

Make sure you are changing the code in your python dist-packages. The location should be something like this: /usr/local/lib/python3.7/dist-packages/grove
