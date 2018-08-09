# Fitbit Open Source Android App

This app is based on the work of Jiska Classen, Matthias Hanreich, Steffen Kreis, Tobias Krichel, Johannes Riedel and Sven Ströher.

## Features
* Scan for BLE Fitbit devices
* Authenticate against Fitbit Server like the original App
* Authentication Replay
* Get Microdump, Megadump
* Dump APP, BSL, Start of the firmware

## Usage
* Use official Fitbit app and associate a tracker you own physically with your user account.
* Online -> Authenticate: get (reusable!) authentication credentials for that tracker.
* Dump -> Key: get (permanent!) device encryption key from that tracker.
* Dump -> Flash: get firmware from tracker that is currently installed.
* Modify that firmware using our Nexmon-based project https://github.com/seemoo-lab/fitness-firmware
* Online -> Upload and encrypt firmware flash binary: re-install the firmware, assumes a flash.bin file that contains a complete flash image.
