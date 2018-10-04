# Fitbit Open Source Android App
![alt text](https://raw.githubusercontent.com/seemoo-lab/fitness-app/master/app/src/main/res/mipmap-hdpi/ic_launcher.png)


This app is based on the work of Jiska Classen, Matthias Hanreich, Steffen Kreis, Tobias Krichel, Johannes Riedel and Sven Ströher.

## Features
* Scan for BLE Fitbit devices
* Authenticate against Fitbit server like the original app
* Obtain authentication credentials from the Fitbit server for authentication replay¹
* Get activity dumps¹ and decrypt them²
* Access live mode¹ 
* Dump memory including the complete firmware²
* Flash custom firmware³ that modifies step counts, reads out raw accelerometer data, etc.

## Workflow
* Use official Fitbit app and associate a tracker you own physically with your user account.
* With a regularly associated tracker you can access all features that require authentication (live mode¹, memory readout²).
* Build any custom firmware using our Nexmon-based project https://github.com/seemoo-lab/fitness-firmware or use one of the pre-compiled images.


## Models
1. all models including Ionic smart watch
2. probably Fitbit Flex, Charge, Charge HR, One with firmware < October 2017 only, ideally buy a new tracker and never update it
3. Fitbit Flex only
