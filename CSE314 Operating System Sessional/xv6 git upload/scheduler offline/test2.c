#include "types.h"
#include "stat.h"
#include "user.h"
#include "pstat.h"

int random(int max)
{

    if (max <= 0)
    {
        return 1;
    }

    static int z1 = 12345; // 12345 for rest of zx
    static int z2 = 12345; // 12345 for rest of zx
    static int z3 = 12345; // 12345 for rest of zx
    static int z4 = 12345; // 12345 for rest of zx

    int b;
    b = (((z1 << 6) ^ z1) >> 13);
    z1 = (((z1 & 4294967294) << 18) ^ b);
    b = (((z2 << 2) ^ z2) >> 27);
    z2 = (((z2 & 4294967288) << 2) ^ b);
    b = (((z3 << 13) ^ z3) >> 21);
    z3 = (((z3 & 4294967280) << 7) ^ b);
    b = (((z4 << 3) ^ z4) >> 12);
    z4 = (((z4 & 4294967168) << 13) ^ b);

    // if we have an argument, then we can use it
    int rand = ((z1 ^ z2 ^ z3 ^ z4)) % max;

    if (rand < 0)
    {
        rand = rand * -1;
    }

    return rand;
}

void showStat()
{
    struct pstat p;

    getpinfo(&p);

    for (int i = 0; i < NPROC; i++)
    {
        if (!p.inuse[i])
            continue;
        printf(1, "inuse : %d\t pid: %d\t tickets : %d\t ticks : %d\n", p.inuse[i], p.pid[i], p.tickets[i], p.ticks[i]);
    }
}

void loop()
{
    while (1)
        ;
}

int createChild(int number)
{
    int pid = fork();

    if (pid < 0)
    {
        printf(1, "Error in fork\n");
        return -1;
    }
    else if (pid == 0)
    {
        //child process
        settickets(number);

        loop();

        return -1;
    }
    else
    {
        //parent process
        return pid;
    }
}

int main(int argc, char **argv)
{
    settickets(5000);

    printf(1, "checking test2\n");

    int i, sleepLength;
    int numOfChild = 5;

    if (argc >= 2)
    {
        numOfChild = atoi(argv[1]);
    }
    printf(1, "number of child : %d\n", numOfChild);

    int pids[numOfChild], tickets[numOfChild];

    for (i = 0; i < numOfChild; i++)
    {
        tickets[i] = random(numOfChild * 2) + 1;
    }

    for (i = 0; i < numOfChild; i++)
    {
        pids[i] = createChild(tickets[i]);
    }

    sleepLength = 1000;
    if (argc >= 3)
    {
        sleepLength = atoi(argv[2]);
    }
    printf(1, "sleep length : %d\n", sleepLength);

    sleep(sleepLength);

    showStat();

    for (i = 0; i < numOfChild; i++)
    {
        kill(pids[i]);
    }

    for (i = 0; i < numOfChild; i++)
    {
        wait();
    }

    exit();
}