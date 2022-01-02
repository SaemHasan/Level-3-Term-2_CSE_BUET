#include "utils.h"

void *returnUsingVipChannel(void *arg);
void *vipChannel(void *arg);

void *boarding(void *arg)
{
    //cout<<(char*)arg<<endl;
    semDecrease(boardingQueueCount);

    lock(boardingQueueMtx);
    Passenger p = boardingQueue.front();
    boardingQueue.pop();
    unlock(boardingQueueMtx);

    p.hasLostPass = isLostPass();

    if (!p.hasLostPass)
    {
        PRINT(p.pid, "started waiting to be boarded", "boarding", 0);

        lock(boardingPass);
        PRINT(p.pid, "started boarding", "the plane", 0);
        sleep(y);
        PRINT(p.pid, "boarded", "the plane", 0);
        unlock(boardingPass);
    }
    else
    {
        PRINT(p.pid, "lost boarding pass", "boarding", 0);
        //adding to lost passenger queue
        lock(lostQueueMtx);
        lostPassengerQueue.push(p);
        unlock(lostQueueMtx);

        semIncrease(lostQueueCount);

        pthread_t thread1;
        pthread_create(&thread1, NULL, returnUsingVipChannel, (void *)"Returning for Boarding Pass at special kiosk");
        pthread_join(thread1, NULL);
    }
}

void *special_kiosk(void *arg)
{
    semDecrease(specialkioskQueueCount);

    lock(specialKioskQueueMtx);
    Passenger p = specialKioskQueue.front();
    specialKioskQueue.pop();
    unlock(specialKioskQueueMtx);

    PRINT(p.pid, "started waiting for boarding pass", "special kiosk", 0);

    lock(specialKioskMtx);
    PRINT(p.pid, "started check in ", "special kiosk", 0);
    sleep(w);
    PRINT(p.pid, "got boarding pass", "special kiosk", 0);
    unlock(specialKioskMtx);

    //add to vip queue
    lock(vipQueueMtx);
    vipQueue.push(p);
    unlock(vipQueueMtx);
    semIncrease(vipQueueCount);

    pthread_t thread1;
    pthread_create(&thread1, NULL, vipChannel, (void *)"VIP Channel entered");
    pthread_join(thread1, NULL);
}

void *returnUsingVipChannel(void *arg)
{
    //cout<<(char*)arg<<endl;
    semDecrease(lostQueueCount);

    lock(lostQueueMtx);
    Passenger p = lostPassengerQueue.front();
    lostPassengerQueue.pop();
    unlock(lostQueueMtx);

    p.hasLostPass = false;

    PRINT(p.pid, "started waiting to be returned", "VIP Channel", 0);

    //
    lock(right_left_Mtx);
    unlock(right_left_Mtx);

    lock(left_right_Mtx);
    PRINT(p.pid, "started walking for boarding pass", "VIP Channel", 0);
    sleep(z);
    PRINT(p.pid, "crossed the VIP channel  & returned for boarding pass", "special kiosk", 0);
    unlock(left_right_Mtx);

    //add to special kiosk queue
    lock(specialKioskQueueMtx);
    specialKioskQueue.push(p);
    unlock(specialKioskQueueMtx);

    semIncrease(specialkioskQueueCount);

    pthread_t thread1;
    pthread_create(&thread1, NULL, special_kiosk, (void *)"Special Kiosk");
    pthread_join(thread1, NULL);
}

//left to right(preference)
void *vipChannel(void *arg)
{
    //cout<<(char*)arg<<endl;
    semDecrease(vipQueueCount);

    lock(vipQueueMtx);
    Passenger p = vipQueue.front();
    vipQueue.pop();
    unlock(vipQueueMtx);

    PRINT(p.pid, "started waiting", "VIP Channel", 0);

    lock(left_right_Mtx); //can be locked in return vip channel
    unlock(left_right_Mtx);

    lock(vipCountMtx);
    vipPassengerCount++;
    if (vipPassengerCount == 1)
        lock(right_left_Mtx);
    unlock(vipCountMtx);

    PRINT(p.pid, "started walking the channel", "VIP Channel", 0);
    sleep(z);
    PRINT(p.pid, "crossed the channel", "VIP Channel", 0);

    lock(vipCountMtx);
    vipPassengerCount--;
    if (vipPassengerCount == 0)
        unlock(right_left_Mtx);
    unlock(vipCountMtx);

    //adding to boarding queue
    lock(boardingQueueMtx);
    boardingQueue.push(p);
    unlock(boardingQueueMtx);

    semIncrease(boardingQueueCount);

    pthread_t thread1;
    pthread_create(&thread1, NULL, boarding, (void *)"Boarding check");
    pthread_join(thread1, NULL);
}

//here in security check
void *securityCheck(void *arg)
{
    //cout<<(char*)arg<<endl;
    semDecrease(securityQueueCount);

    lock(securityQueueMtx);
    Passenger p = securityQueue.front();
    securityQueue.pop();
    unlock(securityQueueMtx);

    int beltNum = rand() % n; // randomly select security belt
    PRINT(p.pid, "started waiting for security check", "belt " + to_string(beltNum + 1), 0);

    semDecrease(securityBelts[beltNum]);
    PRINT(p.pid, "started the security check", "belt", 0);
    sleep(x);
    PRINT(p.pid, "crossed the security check", "belt", 0);
    semIncrease(securityBelts[beltNum]);

    //adding to boarding queue
    lock(boardingQueueMtx);
    boardingQueue.push(p);
    unlock(boardingQueueMtx);

    semIncrease(boardingQueueCount);

    pthread_t thread1;
    pthread_create(&thread1, NULL, boarding, (void *)"Boarding check");
    pthread_join(thread1, NULL);
}

//checking at kiosk
void *check_in_at_kiosk(void *arg)
{
    //cout<<(char*)arg<<endl;
    semDecrease(passengerQueueCount); //decreasing passenger count of queue

    lock(passengerQueueMtx); //lock queue
    Passenger p = passengerQueue.front();
    passengerQueue.pop();
    unlock(passengerQueueMtx); //unlock queue

    semDecrease(mKiosk); //decrease kiosk count or using the kiosk
    PRINT(p.pid, "started self-check in", "kiosk", 0);
    sleep(w);
    PRINT(p.pid, "finished self-check in", "kiosk", 0);
    semIncrease(mKiosk); //increase kiosk count or freeing the kiosk

    pthread_t thread1;
    //add to security queue if passenger is not VIP
    if (!p.isVIP)
    {
        lock(securityQueueMtx);
        securityQueue.push(p);
        unlock(securityQueueMtx);
        semIncrease(securityQueueCount);

        pthread_create(&thread1, NULL, securityCheck, (void *)"Security Check");
    }

    //add to VIP channel is passenger is VIP
    else
    {
        lock(vipQueueMtx);
        vipQueue.push(p);
        unlock(vipQueueMtx);
        semIncrease(vipQueueCount);

        pthread_create(&thread1, NULL, vipChannel, (void *)"VIP Channel entered");
    }
    pthread_join(thread1, NULL);
}

void check_in()
{
    pthread_t thread1;
    //cout<<"creating thread for check in\n";
    pthread_create(&thread1, NULL, check_in_at_kiosk, (void *)"Check in at kiosk");
    pthread_join(thread1, NULL);
}

//at airport
void *addPassengerToQueue(void *pid)
{
    int *num = (int *)pid;
    //cout<<"num : "<<*num<<endl;
    Passenger p = Passenger(*num);
    p.setVIP(isVIPassign()); // randomly assigning vip passenger

    lock(passengerQueueMtx); //locking queue
    passengerQueue.push(p);
    PRINT(p.pid, "arrived", "the airport", 0);
    unlock(passengerQueueMtx); //unlocking queue

    semIncrease(passengerQueueCount); //increasing passenger count in queue

    check_in(); //check in at kiosk

    return 0;
}

int main()
{
    srand(time(0));
    freopen("input.txt", "r", stdin);   //input from file
    freopen("output.txt", "w", stdout); //output to file

    pthread_t threads[MAX_NUMBER_THREAD];
    int threadCount = 0;

    int num;

    cin >> m >> n >> p; //reading m,n,p
    cout << "Number of kiosks : " << m << endl;
    cout << "Number of Security Belts : " << n << "\t Passenger per belt: " << p << endl;

    cin >> w >> x >> y >> z; //reading w, x, y, z

    initSemaphore(m, n, p); //initializing semaphores
    initMutex();            //initialize mutexes

    start = high_resolution_clock::now(); //start clock for timing info

    while (1)
    {
        num = distribution(generator);
        sleep(num); //delay for generating next passanger
        //cout<<"Generating passanger id : "<<threadCount+1<<endl;
        pthread_create(&threads[threadCount++], NULL, addPassengerToQueue, (void *)&threadCount);
    }

    while (1)
        ;
    return 0;
}