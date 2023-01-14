#include "types.h"
#include "stat.h"
#include "user.h"

int main(int argc, char *argv[])
{
    int sz = 4096 * 17;
    int *mem = (int *)malloc(sz);
    int pid = fork();
    int k = 17;
    if (pid == 0)
    {
        k = 17;
    }
    else
    {
        k = 13;
    }

    for (int i = 0; i < sz / 4; i++)
    {
        mem[i] = k * i;
    }
    // int pid = fork();
    sleep(250);
    int ok = 1;
    for (int i = 0; i < sz / 4; i++)
    {
        if (mem[i] != k * i)
        {
            printf(2, "failed at : %d %d %d\n", i, k * i, mem[i]);
            ok = 0;
            break;
        }
    }
    sleep(250);
    if (!ok)
    {
        printf(2, "Failed\n");
    }
    else
    {
        printf(2, "Success\n");
    }
    free((void *)mem);
    if (pid != 0)
    {
        wait();
    }
    exit();
}