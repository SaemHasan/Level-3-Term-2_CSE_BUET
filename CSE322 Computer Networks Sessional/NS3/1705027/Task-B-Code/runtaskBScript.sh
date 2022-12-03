./waf --run "scratch/taskB --algo=TcpWestwoodBR";
./waf --run "scratch/taskB --algo=TcpWestwood";
./waf --run "scratch/taskB --algo=TcpNewReno";
python comparison_algo.py;
python comparision_code.py;