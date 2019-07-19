/*
 * Copyright (c) 2016, Vishwesh Rege.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the Institute nor the names of its contributors
 *    may be used to endorse or promote products derived from this software
 *    without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE INSTITUTE AND CONTRIBUTORS ``AS IS'' AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED.  IN NO EVENT SHALL THE INSTITUTE OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 *
 */

#include "dev/dir.h"
#include "lib/simEnvChange.h"

const struct simInterface dir_interface;

// COOJA variables
int simBwDegValue = 90;
int simOriDegValue = 0;
int xcor = 10;
int ycor = 0;
int gvar = 0;

/*-----------------------------------------------------------------------------------*/
void dir_arch_init() {
  simBwDegValue = 60;
  simOriDegValue = 1;
  xcor = 0;
  ycor = 0;
  gvar = 0;
}
/*-----------------------------------------------------------------------------------*/
int bw_arch_get() {
  return simBwDegValue;
}
/*-----------------------------------------------------------------------------------*/
int ori_arch_get() {
  return simOriDegValue;
}
/*-----------------------------------------------------------------------------------*/
void bw_arch_set(int bw) {
    simBwDegValue = bw;
}
/*-----------------------------------------------------------------------------------*/
void ori_arch_set(int ori) {
    simOriDegValue = ori;
}
/*-----------------------------------------------------------------------------------*/
int get_xpos() {
  return xcor;
}
/*-----------------------------------------------------------------------------------*/
void set_xpos(int xcordi) {
  xcor = xcordi;
}
/*-----------------------------------------------------------------------------------*/
int get_ypos() {
  return ycor;
}
/*-----------------------------------------------------------------------------------*/
void set_ypos(int ycordi) {
  ycor = ycordi;
}
/*-----------------------------------------------------------------------------------*/
static void
doInterfaceActionsBeforeTick(void)
{
}
/*-----------------------------------------------------------------------------------*/
static void
doInterfaceActionsAfterTick(void)
{
}
/*-----------------------------------------------------------------------------------*/

SIM_INTERFACE(dir_interface,
	      doInterfaceActionsBeforeTick,
	      doInterfaceActionsAfterTick);
