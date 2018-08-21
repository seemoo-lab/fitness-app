#pragma NEXMON targetregion "patch"

#include <firmware_version.h>
#include <patcher.h>
#include <wrapper.h>
#include "stm32.h"

#define ACCELERATOR_BUFFER_ADR 0x200048DC


//#define SET_ACCEL_LIVE_MODE_ADDR 0x20001100 
#define SET_ACCEL_LIVE_MODE_ADDR 0x200000a0 

void
reset_swd_pins() {
    // enable clock
    RCC->AHBENR |= RCC_AHBENR_GPIOAEN;

    // clear bits for GPIO mode
    GPIOA->MODER &= ~(GPIO_MODER_MODER14 | GPIO_MODER_MODER13);

    // set bits for port 13 (SWDIO) and port 14 (SWCLK)
    GPIOA->MODER |= (GPIO_MODER_MODER14_1 | GPIO_MODER_MODER13_1);

    // clear pull up / pull down bits
    GPIOA->PUPDR &= ~(GPIO_PUPDR_PUPDR13 | GPIO_PUPDR_PUPDR14);
}

void
open_command_prompt_loop_hook() {
    //TODO disable debugging for now
    reset_swd_pins();
    open_command_prompt_loop();
}

__attribute__((at(0x8026020, "", CHIP_VER_FITBIT, FW_VER_FITBIT)))
GenericPatch4(command_promt_addr, open_command_prompt_loop_hook+1);

int
hook__bt_send__live_mode() {

  unsigned char *setAccelLiveMode =  (unsigned char *) SET_ACCEL_LIVE_MODE_ADDR;
  unsigned char *accelPacketBuffer =  (unsigned char *) ACCELERATOR_BUFFER_ADR;

  char temp[15] = {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 
                   0x00, 0x00, 0x00, 0x00, 0x00, 0xC1, 0xAC};

  if(*setAccelLiveMode == 1) {

    temp[0] = accelPacketBuffer[0];
    temp[1] = accelPacketBuffer[1];

    temp[3] = accelPacketBuffer[2];
    temp[4] = accelPacketBuffer[3];

    temp[6] = accelPacketBuffer[4];
    temp[7] = accelPacketBuffer[5];

    prepare_packet((int)temp,15,1);
    send_packet((int) 0x64);
  }

  return  _bt_send__live_mode();
}

__attribute__((at(0x08013578, "", CHIP_VER_FITBIT, FW_VER_FITBIT)))
BLPatch(hook__bt_send__live_mode1, hook__bt_send__live_mode);

__attribute__((at(0x080137BC, "", CHIP_VER_FITBIT, FW_VER_FITBIT)))
BLPatch(hook__bt_send__live_mode2, hook__bt_send__live_mode);

__attribute__((at(0x080139CA, "", CHIP_VER_FITBIT, FW_VER_FITBIT)))
BLPatch(hook__bt_send__live_mode3, hook__bt_send__live_mode);


int
hook_app_parse_command(int start, unsigned int length, int bluetooth_process2, char *response_bytes) {
    char command = *((char *)start + 1);

    unsigned char *setAccelLiveMode =  (unsigned char *) SET_ACCEL_LIVE_MODE_ADDR;


    //Place custom commands here
    if(command == 0x44) {
        print_string("Swicht Live Mode Output\n");
        reset_swd_pins();


        if(*setAccelLiveMode == 0){

          *setAccelLiveMode = 1;
          
        } else {
          *setAccelLiveMode = 0;
          
        }
    }
    

    return app_parse_command(start, length, bluetooth_process2, response_bytes);
}

__attribute__((at(0x800CE9E, "", CHIP_VER_FITBIT, FW_VER_FITBIT)))
BLPatch(hook_app_parse_command, hook_app_parse_command);

int
hook_check_auth() {
    return 1;
}

__attribute__((at(0x800C6C4, "", CHIP_VER_FITBIT, FW_VER_FITBIT)))
BLPatch(hook_check_auth1, hook_check_auth);
__attribute__((at(0x800C704, "", CHIP_VER_FITBIT, FW_VER_FITBIT)))
BLPatch(hook_check_auth2, hook_check_auth);
__attribute__((at(0x800CA50, "", CHIP_VER_FITBIT, FW_VER_FITBIT)))
BLPatch(hook_check_auth3, hook_check_auth);
__attribute__((at(0x8013102, "", CHIP_VER_FITBIT, FW_VER_FITBIT)))
BLPatch(hook_check_auth4, hook_check_auth);

int
hook_app_check_crypto_required(char situation) {
    return 0;
}

__attribute__((at(0x800A42E, "", CHIP_VER_FITBIT, FW_VER_FITBIT)))
BLPatch(hook_app_check_crypto_required1, hook_app_check_crypto_required);
__attribute__((at(0x800A500, "", CHIP_VER_FITBIT, FW_VER_FITBIT)))
BLPatch(hook_app_check_crypto_required2, hook_app_check_crypto_required);
__attribute__((at(0x800A958, "", CHIP_VER_FITBIT, FW_VER_FITBIT)))
BLPatch(hook_app_check_crypto_required3, hook_app_check_crypto_required);
__attribute__((at(0x800B8A0, "", CHIP_VER_FITBIT, FW_VER_FITBIT)))
BLPatch(hook_app_check_crypto_required4, hook_app_check_crypto_required);
__attribute__((at(0x800C030, "", CHIP_VER_FITBIT, FW_VER_FITBIT)))
BLPatch(hook_app_check_crypto_required5, hook_app_check_crypto_required);
