/***************************************************************************
 *                                                                         *
 *          ###########   ###########   ##########    ##########           *
 *         ############  ############  ############  ############          *
 *         ##            ##            ##   ##   ##  ##        ##          *
 *         ##            ##            ##   ##   ##  ##        ##          *
 *         ###########   ####  ######  ##   ##   ##  ##    ######          *
 *          ###########  ####  #       ##   ##   ##  ##    #    #          *
 *                   ##  ##    ######  ##   ##   ##  ##    #    #          *
 *                   ##  ##    #       ##   ##   ##  ##    #    #          *
 *         ############  ##### ######  ##   ##   ##  ##### ######          *
 *         ###########    ###########  ##   ##   ##   ##########           *
 *                                                                         *
 *            S E C U R E   M O B I L E   N E T W O R K I N G              *
 *                                                                         *
 * This file is part of NexMon.                                            *
 *                                                                         *
 * Copyright (c) 2016 NexMon Team                                          *
 *                                                                         *
 * NexMon is free software: you can redistribute it and/or modify          *
 * it under the terms of the GNU General Public License as published by    *
 * the Free Software Foundation, either version 3 of the License, or       *
 * (at your option) any later version.                                     *
 *                                                                         *
 * NexMon is distributed in the hope that it will be useful,               *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of          *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the           *
 * GNU General Public License for more details.                            *
 *                                                                         *
 * You should have received a copy of the GNU General Public License       *
 * along with NexMon. If not, see <http://www.gnu.org/licenses/>.          *
 *                                                                         *
 **************************************************************************/

#ifndef WRAPPER_C
#define WRAPPER_C

#include <firmware_version.h>
#include <structs.h>

#ifndef WRAPPER_H
    // if this file is not included in the wrapper.h file, create dummy functions
    #define VOID_DUMMY { ; }
    #define RETURN_DUMMY { ; return 0; }

    #define AT(CHIPVER, FWVER, ADDR) __attribute__((at(ADDR, "dummy", CHIPVER, FWVER)))
#else
    // if this file is included in the wrapper.h file, create prototypes
    #define VOID_DUMMY ;
    #define RETURN_DUMMY ;
    #define AT(CHIPVER, FWVER, ADDR)
#endif

AT(CHIP_VER_FITBIT, FW_VER_FITBIT, 0x801a5e8)
int
blinkenlights(int a1, int a2, int a3, int a4)
RETURN_DUMMY

AT(CHIP_VER_FITBIT, FW_VER_FITBIT, 0x800C62C)
int
app_parse_command(int start, unsigned int length, int bluetooth_process2, char *response_type)
RETURN_DUMMY

AT(CHIP_VER_FITBIT, FW_VER_FITBIT, 0x800EF10)
int
bt_send_live_mode(int a1, int a2, int a3, int a4, int a5, int a6)
RETURN_DUMMY

AT(CHIP_VER_FITBIT, FW_VER_FITBIT, 0x80222B0)
int 
print_string(const char *format, ...) 
RETURN_DUMMY

AT(CHIP_VER_FITBIT, FW_VER_FITBIT, 0x802241C)
void
open_command_prompt_loop()
VOID_DUMMY

AT(CHIP_VER_FITBIT, FW_VER_FITBIT, 0x800A37E)
int
app_check_crypto_required(char situation)
RETURN_DUMMY

AT(CHIP_VER_FITBIT, FW_VER_FITBIT, 0x80121EA)
int 
lib_aci_send_data(char pipe, char *data, unsigned char size)
RETURN_DUMMY

AT(CHIP_VER_FITBIT, FW_VER_FITBIT, 0x800F1F4)
int 
memcpy(char *dst, char *src, unsigned int length)
RETURN_DUMMY

AT(CHIP_VER_FITBIT, FW_VER_FITBIT, 0x800CEAC)
int
j_bt_send_bytes(char *a1, int a2, unsigned int length)
RETURN_DUMMY

AT(CHIP_VER_FITBIT, FW_VER_FITBIT, 0x80130EC)
int
_bt_send__live_mode()
RETURN_DUMMY

AT(CHIP_VER_FITBIT, FW_VER_FITBIT, 0x801A570)
int
vibrate(int a1, int a2)
RETURN_DUMMY

AT(CHIP_VER_FITBIT, FW_VER_FITBIT, 0x8017B16)
void
port_d_ext_peripherals()
VOID_DUMMY

AT(CHIP_VER_FITBIT, FW_VER_FITBIT, 0x8014F74)
int
led_cycle_pattern2(int a1)
RETURN_DUMMY

AT(CHIP_VER_FITBIT, FW_VER_FITBIT, 0x800EE56)
void
rf_bt_timer_exti_aci_command_wrapper()
VOID_DUMMY

AT(CHIP_VER_FITBIT, FW_VER_FITBIT, 0x8012DB2)
void
c_aci_command_wrapper(int a1)
VOID_DUMMY

AT(CHIP_VER_FITBIT, FW_VER_FITBIT, 0x8013CF4)
int
rf_get_mode(char *a1)
RETURN_DUMMY

AT(CHIP_VER_FITBIT, FW_VER_FITBIT, 0x8013D06)
int
rf_set_mode()
RETURN_DUMMY

AT(CHIP_VER_FITBIT, FW_VER_FITBIT, 0x800AA7A)
int 
perform_dump(char dumptype, int address, int length)
RETURN_DUMMY

AT(CHIP_VER_FITBIT, FW_VER_FITBIT, 0x08012FCC)
int 
prepare_packet(int data, char length, char a3)
RETURN_DUMMY

AT(CHIP_VER_FITBIT, FW_VER_FITBIT, 0x0800EF66)
int 
send_packet(int data)
RETURN_DUMMY

#undef VOID_DUMMY
#undef RETURN_DUMMY
#undef AT

#endif /*WRAPPER_C*/
