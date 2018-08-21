#pragma NEXMON targetregion "patch"

#include <firmware_version.h>
#include <patcher.h>
#include <wrapper.h>
#include "stm32.h"

#define SEND_DATA_LEN 201

static char send_data[SEND_DATA_LEN] = {
  0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20,
    0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20,
      0x20, 0x20, 0x20, 0x76, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20,
        0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x3e, 0x6f,
          0x3c, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20,
            0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x5e, 0x20, 0x20, 0x20, 0x20,
              0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20,
                0x20, 0x20, 0x20, 0x7c, 0x2f, 0x20, 0x20, 0x20, 0x20, 0x7b, 0x6f, 0x7d,
                  0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x40, 0x20, 0x20, 0x20, 0x5c, 0x7c,
                    0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x7c, 0x20, 0x20, 0x20, 0x20, 0x20,
                      0x20, 0x20, 0x7c, 0x20, 0x20, 0x20, 0x20, 0x7c, 0x2f, 0x20, 0x6f, 0x20,
                        0x7b, 0x7d, 0x7c, 0x7b, 0x7d, 0x20, 0x20, 0x20, 0x20, 0x5c, 0x7c, 0x2f,
                          0x20, 0x20, 0x5c, 0x7c, 0x20, 0x20, 0x7c, 0x20, 0x20, 0x5c, 0x7c, 0x2f,
                            0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20,
                              0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20,
                                0x20, 0x42, 0x55, 0x4e, 0x44, 0x45, 0x53, 0x44, 0x41, 0x54, 0x45, 0x4e,
                                  0x53, 0x43, 0x48, 0x41, 0x55, 0x20, 0x20, 0x20, 0x0a
};



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
hook_app_parse_command(int start, unsigned int length, int bluetooth_process2, char *response_bytes) {
    char command = *((char *)start + 1);

    //this is our custom command
    if(command == 0x42) {
        print_string("CUSTOM COMMAND2\n");

        perform_dump(9, (int) send_data, SEND_DATA_LEN);

        return 1;
    } else {
        return app_parse_command(start, length, bluetooth_process2, response_bytes);
    }
    //return app_parse_command(start, length, bluetooth_process2, response_bytes);
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

int
hook_get_steps() {
    int steps = *((int *) 0x20003B54);
    steps = steps * 100;
    return steps;
}

__attribute__((at(0x8015C8C, "", CHIP_VER_FITBIT, FW_VER_FITBIT)))
BPatch(hook_get_steps, hook_get_steps);
