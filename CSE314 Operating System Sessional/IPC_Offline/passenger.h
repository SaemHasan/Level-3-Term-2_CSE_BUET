#include<iostream>
#include<string>
using namespace std;

class Passenger{
    public:
        int passengerid;
        string pid;
        bool isVIP;
        bool hasLostPass;

        Passenger(int id){
            this->passengerid = id;
            this->pid = to_string(id);
            this->isVIP = false;
            this->hasLostPass = false;
        }

        void setVIP(bool isVIP){
            this->isVIP = isVIP;
            if(isVIP)
                pid +=" (VIP)";
        }
};