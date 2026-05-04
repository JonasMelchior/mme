#ifndef FILE_COMMON_DEF_SEEN
#define FILE_COMMON_DEF_SEEN

#define SUCCESS 0
#define FAILURE -1

#define INT_TO_OCTET_STRING(iINT, oOCTETSTRING)          \
do {                                                     \
    (oOCTETSTRING)->buf = malloc(sizeof(uint32_t));      \
    (oOCTETSTRING)->size = sizeof(uint32_t);             \
    *((uint32_t*)(oOCTETSTRING)->buf) = htonl(iINT);     \
} while(0)

#endif
