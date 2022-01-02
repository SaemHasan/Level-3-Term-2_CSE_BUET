#include<iostream>
#include<cstdio>
#include<pthread.h>
#include<semaphore.h>
#include<queue>
#include <unistd.h>
#include<random>
#include<set>
#include "passenger.h"

#define MAX_NUMBER_THREAD 6

using namespace std;


default_random_engine generator;
poisson_distribution<int> distribution(4.1);

//semaphores
sem_t mKiosk;
sem_t *securityBelts;
sem_t passengerQueueCount;
sem_t securityQueueCount;
sem_t boardingQueueCount;
//mutex
pthread_mutex_t boardingPass;
pthread_mutex_t passengerQueueMtx;
pthread_mutex_t securityQueueMtx;
pthread_mutex_t boardingQueueMtx;

//queue
queue<Passenger> passengerQueue;
queue<Passenger> securityQueue;
queue<Passenger> boardingQueue;

//variables
int m,n,p;
int w,x,y,z;



void initSemaphore(int m, int n, int p){
    securityBelts = new sem_t[n];
    sem_init(&mKiosk, 0, m);
    for(int i=0;i<n;i++)
        sem_init(&securityBelts[i], 0, p);
    sem_init(&passengerQueueCount, 0,0);
    sem_init(&securityQueueCount, 0,0);
    sem_init(&boardingQueueCount, 0,0);
}

void initMutex(){
    pthread_mutex_init(&boardingPass, NULL);
    pthread_mutex_init(&passengerQueueMtx, NULL);
    pthread_mutex_init(&securityQueueMtx, NULL);
    pthread_mutex_init(&boardingQueueMtx, NULL);
}


void lock(pthread_mutex_t &mtx){
    pthread_mutex_lock(&mtx);
}

void unlock(pthread_mutex_t &mtx){
    pthread_mutex_unlock(&mtx);
}

void semIncrease(sem_t &var){
    sem_post(&var);
}

void semDecrease(sem_t &var){
    sem_wait(&var);
}
