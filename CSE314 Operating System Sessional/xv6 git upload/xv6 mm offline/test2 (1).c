#include "types.h"
#include "stat.h"
#include "user.h"

int main(int argc, char *argv[])
{

    if (argc != 2)
    {
        exit();
    }

    int size = atoi(argv[1]);
    printf(1, "size: %d\n", size);

    int allocsize = size * 4096;

    printf(1, "malloc start\n");
    uint *p = (uint *)malloc(allocsize);
    printf(1, "malloc end\n");

    fork();

    for (uint i = 0; i < size * 1024; i++)
    {
        p[i] = i;
    }
    int ok = 1;
    for (uint i = 0; i < size * 1024; i++)
    {
        if (p[i] != i)
        {
            ok = 0;
            break;
        }
    }
    if (ok)
    {
        printf(2, "all ok\n");
    }
    else
    {
        printf(2, "Not ok\n");
    }
    // for(uint i=0;i<size*1024;i++) {
    //     printf(1,"%p %p\n",i,p[i]);
    // }
    // printf(1,"\n");

    wait();
    exit();
}