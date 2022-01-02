#include <iostream>
#include <cstdio>
#include <pthread.h>
#include <semaphore.h>
#include <queue>
#include <unistd.h>
#include <random>
#include <string>
#include "passenger.h"
#include <cstdlib>
#include <ctime>
#include <chrono>

#define MAX_NUMBER_THREAD 10000

using namespace std;
using namespace chrono;

default_random_engine generator;
poisson_distribution<int> distribution(6.5);

//semaphores
sem_t mKiosk;
sem_t *securityBelts;
sem_t passengerQueueCount;
sem_t securityQueueCount;
sem_t boardingQueueCount;
sem_t vipQueueCount;
sem_t lostQueueCount;
sem_t specialkioskQueueCount;

//mutex
pthread_mutex_t boardingPass;
pthread_mutex_t passengerQueueMtx;
pthread_mutex_t securityQueueMtx;
pthread_mutex_t boardingQueueMtx;
pthread_mutex_t printMtx;
pthread_mutex_t vipQueueMtx;
pthread_mutex_t vipCountMtx;
pthread_mutex_t right_left_Mtx;
pthread_mutex_t left_right_Mtx;
pthread_mutex_t lostQueueMtx;
pthread_mutex_t specialKioskQueueMtx;
pthread_mutex_t specialKioskMtx;

//queue
queue<Passenger> passengerQueue;
queue<Passenger> securityQueue;
queue<Passenger> boardingQueue;
queue<Passenger> vipQueue;
queue<Passenger> lostPassengerQueue;
queue<Passenger> specialKioskQueue;

//variables
int m, n, p;
int w, x, y, z;
int vipPassengerCount = 0;
int timeCount = 0;

auto start = high_resolution_clock::now();

void initSemaphore(int m, int n, int p)
{
    securityBelts = new sem_t[n];
    sem_init(&mKiosk, 0, m);
    for (int i = 0; i < n; i++)
        sem_init(&securityBelts[i], 0, p);
    sem_init(&passengerQueueCount, 0, 0);
    sem_init(&securityQueueCount, 0, 0);
    sem_init(&boardingQueueCount, 0, 0);
    sem_init(&vipQueueCount, 0, 0);
    sem_init(&lostQueueCount, 0, 0);
    sem_init(&specialkioskQueueCount, 0, 0);
}

void initMutex()
{
    pthread_mutex_init(&boardingPass, NULL);
    pthread_mutex_init(&passengerQueueMtx, NULL);
    pthread_mutex_init(&securityQueueMtx, NULL);
    pthread_mutex_init(&boardingQueueMtx, NULL);
    pthread_mutex_init(&printMtx, NULL);
    pthread_mutex_init(&vipQueueMtx, NULL);
    pthread_mutex_init(&vipCountMtx, NULL);
    pthread_mutex_init(&right_left_Mtx, NULL);
    pthread_mutex_init(&left_right_Mtx, NULL);
    pthread_mutex_init(&lostQueueMtx, NULL);
    pthread_mutex_init(&specialKioskQueueMtx, NULL);
    pthread_mutex_init(&specialKioskMtx, NULL);
}

void lock(pthread_mutex_t &mtx)
{
    pthread_mutex_lock(&mtx);
}

void unlock(pthread_mutex_t &mtx)
{
    pthread_mutex_unlock(&mtx);
}

void semIncrease(sem_t &var)
{
    sem_post(&var);
}

void semDecrease(sem_t &var)
{
    sem_wait(&var);
}

void PRINT(string pid, string stat, string where, int t)
{
    lock(printMtx);
    auto end = high_resolution_clock::now();
    cout << "Passenger " << pid << " has " << stat << " at " << where << " at time " << duration_cast<seconds>(end - start).count() << endl;
    unlock(printMtx);
}

bool isVIPassign()
{
    int i = rand() % 4;
    if (i == 3)
    {
        return true;
    }
    return false;
}

bool isLostPass()
{
    int i = rand() % 4;
    if (i == 3)
    {
        return true;
    }
    return false;
}