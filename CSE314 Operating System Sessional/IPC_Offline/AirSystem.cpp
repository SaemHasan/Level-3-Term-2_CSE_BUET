#include "utils.h"



void *boarding(void* arg){
    //cout<<(char*)arg<<endl;
    semDecrease(boardingQueueCount);

    lock(boardingQueueMtx);
    Passenger p = boardingQueue.front();
    boardingQueue.pop();
    unlock(boardingQueueMtx);

    cout<<"Passenger "<<p.pid<<" has started waiting to be boarded"<<endl;

    lock(boardingPass);
    cout<<"Passenger "<<p.pid<<" has started boarding the plane"<<endl;
    sleep(y);
    cout<<"Passenger "<<p.pid<<" has boarded the plane"<<endl;
    unlock(boardingPass);
}


//here in security check
void *securityCheck(void *arg){
    //cout<<(char*)arg<<endl;
    semDecrease(securityQueueCount);

    lock(securityQueueMtx);
    Passenger p = securityQueue.front();
    securityQueue.pop();
    unlock(securityQueueMtx);

    int beltNum = rand() % n;
    cout<<"Passenger "<<p.pid<<" has started waiting for security check in belt "<<beltNum+1<<endl;

    semDecrease(securityBelts[beltNum]);
    cout<<"Passenger "<<p.pid<<" has started the security check"<<endl;
    sleep(x);
    cout<<"Passenger "<<p.pid<<" has crossed the security check"<<endl;
    semIncrease(securityBelts[beltNum]);

    //adding to boarding queue
    lock(boardingQueueMtx);
    boardingQueue.push(p);
    unlock(boardingQueueMtx);

    semIncrease(boardingQueueCount);

    pthread_t thread1;
    pthread_create(&thread1, NULL, boarding, (void*)"Boarding check");
    pthread_join(thread1, NULL);
}

//checking at kiosk
void *check_in_at_kiosk(void* arg){
    //cout<<(char*)arg<<endl;
    semDecrease(passengerQueueCount); //decreasing passenger count of queue
    
    lock(passengerQueueMtx); //lock queue
    Passenger p = passengerQueue.front();
    passengerQueue.pop();
    unlock(passengerQueueMtx); //unlock queue
    
    semDecrease(mKiosk); //decrease kiosk count or using the kiosk
    
    cout<<"Passenger "<<p.pid<<" has started self-check in at kiosk"<<endl;
    sleep(w);
    cout<<"Passenger "<<p.pid<<" has finished self-check in at kiosk"<<endl;
    semIncrease(mKiosk); //increase kiosk count or freeing the kiosk

    //add to security queue
    lock(securityQueueMtx);
    securityQueue.push(p);
    unlock(securityQueueMtx);

    semIncrease(securityQueueCount);

    pthread_t thread1;
    pthread_create(&thread1, NULL, securityCheck, (void*)"Security Check");
    pthread_join(thread1, NULL);
}


void check_in(){
    pthread_t thread1;
    //cout<<"creating thread for check in\n";
    pthread_create(&thread1, NULL, check_in_at_kiosk, (void *) "Check in at kiosk");
    pthread_join(thread1, NULL);
}

//at airport
void * addPassengerToQueue(void * pid){
    int *num = (int*) pid;
    //cout<<"num : "<<*num<<endl;
    Passenger p = Passenger(*num);
    
    lock(passengerQueueMtx); //locking queue
    passengerQueue.push(p);
    cout<<"Passenger "<< p.pid <<" has arrived at the airport"<<endl;
    unlock(passengerQueueMtx);//unlocking queue
    
    semIncrease(passengerQueueCount); //increasing passenger count in queue

    check_in(); //check in at kiosk

    return 0;
}



int main(){
    freopen("input.txt", "r", stdin);//input from file
    
    set<int> passengers;// for unique id
    pthread_t threads[MAX_NUMBER_THREAD];
    int threadCount = 0;
    
    int num;

    cin>>m>>n>>p; //reading m,n,p
    cout<<"Number of kiosks : "<<m<<endl;
    cout<<"Number of Security Belts : "<<n<<".\t Passenger per belt: "<<p<<endl;

    cin>>w>>x>>y>>z; //reading w, x, y,

    initSemaphore(m,n,p); //initializing semaphores
    initMutex(); //initialize mutex

    while(threadCount<MAX_NUMBER_THREAD){
        num = distribution(generator);
        
        if(passengers.find(num) == passengers.end()){
            passengers.insert(num);
            //cout<<"Generating passanger id : "<<num<<endl;
            pthread_create(&threads[threadCount++],NULL , addPassengerToQueue, (void *)&num);
            
            sleep(2);//2 second delay for generating next passanger
        }
        
    }

    for(int i=0;i<threadCount;i++){
        pthread_join(threads[i], NULL);
    }

    while(1);
    return 0;
}