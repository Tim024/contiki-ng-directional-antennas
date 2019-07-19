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
/**
 * \addtogroup dev
 * @{
 */

/**
 * \defgroup dir DIR API
 *
 * The DIR API defines a set of functions for accessing DIR interface for
 * Contiki plaforms with DIR support.
 *
 * A platform with DIR support must implement this API.
 * @{
 */

#ifndef DIR_H_
#define DIR_H_

/* Allow platform to override DIR */
#include "contiki-conf.h"

void dir_init(void);

int bw_get(void);
void bw_set(int beamwidthDegrees);
int ori_get(void);
void ori_set(int orientationDegrees);

void dir_arch_init(void);
int bw_arch_get(void);
void bw_arch_set(int beamwidthDegrees);
int ori_arch_get(void);
void ori_arch_set(int orientationDegrees);

void set_xcoordinate(int xcoordinate);
int get_xcoordinate(void);
void set_ycoordinate(int ycoordinate);
int get_ycoordinate(void);

 
int get_xpos(void);
void set_xpos(int xcoordinate);
int get_ypos(void);
void set_ypos(int ycoordinate);

#endif /* DIR_H_ */

/** @} */
/** @} */
