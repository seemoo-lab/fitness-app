#pragma NEXMON targetregion "patch"

#include <firmware_version.h>
#include <patcher.h>
#include <wrapper.h>
#include "stm32.h"

int
hook_get_steps() {
    int steps = *((int *) 0x20003B54);
    steps = steps * 100;
    return steps;
}

__attribute__((at(0x8014304, "", CHIP_VER_FITBIT, FW_VER_FITBIT)))
BPatch(hook_get_steps, hook_get_steps);
