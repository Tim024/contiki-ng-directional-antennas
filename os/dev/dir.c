/*
 * Copyright (c) 2016, Vishwesh Rege
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
 * This file is part of the Contiki operating system.
 *
 */

#include "dev/dir.h"
#include "sys/clock.h"
//#include "sys/energest.h"

//static int ori;
//static int bw;
/*---------------------------------------------------------------------------*/
void
dir_init(void)
{
  dir_arch_init();
}
/*---------------------------------------------------------------------------*/
int
bw_get(void) {
  return bw_arch_get();
}
/*---------------------------------------------------------------------------*/
void
bw_set(int bw)
{
  bw_arch_set(bw);
}
/*---------------------------------------------------------------------------*/
int
ori_get(void) {
  return ori_arch_get();
}
/*---------------------------------------------------------------------------*/
void
ori_set(int ori)
{
  ori_arch_set(ori);
}
/*---------------------------------------------------------------------------*/
int
get_xcoordinate(void) {
  return get_xpos();
}
/*---------------------------------------------------------------------------*/
void
set_xcoordinate(int xcordi)
{
  set_xpos(xcordi);
}
/*---------------------------------------------------------------------------*/
int
get_ycoordinate(void) {
  return get_ypos();
}
/*---------------------------------------------------------------------------*/
void
set_ycoordinate(int ycordi)
{
  set_ypos(ycordi);
}
/*---------------------------------------------------------------------------*/

