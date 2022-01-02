
class Passenger{
    public:
        int pid;
        bool isVIP;
        bool hasBoardingPass;

        Passenger(int pid){
            this->pid = pid;
            this->isVIP = false;
            hasBoardingPass = false;
        }
};