#ifndef ASSERTIONS_H_
#define ASSERTIONS_H_

#include <stdio.h>
#include <stdlib.h>
#include <assert.h>

#define DevAssert(c) assert(c)
#define DevCheck(c, v1, v2, v3) assert(c)
#define DevParam(c, v1, v2) assert(c)

#endif
