CC = gcc
CFLAGS = -fPIC -I. -D_DEFAULT_SOURCE -DASN_DISABLE_OER_SUPPORT -w -g -O0
LDFLAGS = -shared

SRCS = $(filter-out converter-sample.c, $(wildcard *.c))
OBJS = $(SRCS:.c=.o)

libs1ap.so: $(OBJS)
	$(CC) $(LDFLAGS) -o $@ $(OBJS)

%.o: %.c
	$(CC) $(CFLAGS) -c $< -o $@

clean:
	rm -f $(OBJS) libs1ap.so
