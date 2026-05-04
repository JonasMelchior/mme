#ifndef FILE_COMMON_TYPES_SEEN
#define FILE_COMMON_TYPES_SEEN

#include <stdint.h>
#include <stdbool.h>
#include <arpa/inet.h>

#include "3gpp_23.003.h"
#include "3gpp_24.007.h"
#include "3gpp_24.008.h"
// #include "3gpp_23.401.h" // Too many circular dependencies for S1AP lib
#include "3gpp_33.401.h"
#include "3gpp_36.401.h"
// #include "3gpp_24.301.h" // Includes 23.401
#include "security_types.h"
#include "common_dim.h"

typedef uint16_t sctp_stream_id_t;
typedef uint32_t sctp_assoc_id_t;
typedef uint32_t teid_t;
typedef uint64_t imsi64_t;

#define TAI_LIST_MAX_SIZE 16

// Essential missing typedefs from suppressed headers
typedef uint8_t qci_t;
typedef struct {
    uint32_t br_ul;
    uint32_t br_dl;
} ambr_t;

#endif
