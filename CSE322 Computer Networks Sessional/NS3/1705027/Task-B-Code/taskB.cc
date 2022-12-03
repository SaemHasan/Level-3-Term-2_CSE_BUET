/* -*- Mode:C++; c-file-style:"gnu"; indent-tabs-mode:nil; -*- */
/*
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 2 as
 * published by the Free Software Foundation;
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

#include "ns3/core-module.h"
#include "ns3/point-to-point-module.h"
#include "ns3/network-module.h"
#include "ns3/applications-module.h"
#include "ns3/mobility-module.h"
#include "ns3/csma-module.h"
#include "ns3/internet-module.h"
#include "ns3/yans-wifi-helper.h"
#include "ns3/ssid.h"
#include "ns3/flow-monitor-module.h"

// Network Topology
//
//  Wifi 10.1.3.0
//                 AP
//  *    *    *
//  |    |    |    10.1.1.0
// n6   n7   n0 -------------- n1   n2   n3
//                   point-to-point  |    |    |
//                                   ===========
//                                   LAN 10.1.2.0

using namespace ns3;

NS_LOG_COMPONENT_DEFINE("TaskBExample");

std::string dir;
std::string dirToSave;
std::string outputFileName, outputFileName2;

uint32_t *prev;
Time prevTime = Seconds(0);

// Calculate throughput
static void
TraceThroughput(Ptr<FlowMonitor> monitor)
{
    FlowMonitor::FlowStatsContainer stats = monitor->GetFlowStats();
    int cnt = 0;
    int total = 0;
    uint32_t total_cum_throughput = 0;
    Time curTime = Now();

    for (auto itr = stats.begin(); itr != stats.end(); itr++)
    {
        total += (itr->second.txBytes - prev[cnt]);
        total_cum_throughput += itr->second.txBytes;
        prev[cnt] = itr->second.txBytes;
        cnt++;
    }
    std::ofstream thr(dir + outputFileName, std::ios::out | std::ios::app);

    thr << curTime.GetSeconds() << "," << 8 * (total) / (1000 * (curTime.GetSeconds() - prevTime.GetSeconds()))
        << "," << 8 * total_cum_throughput / (1000 * curTime.GetSeconds()) << std::endl;

    std::cout << "Time : " << curTime.GetSeconds() << "s --> Throughput: " << 8 * (total) / (1000 * (curTime.GetSeconds() - prevTime.GetSeconds())) << "kbps"
              << "\tCumlative throughput: " << 8 * total_cum_throughput / (1000 * curTime.GetSeconds()) << "kbps" << std::endl;

    prevTime = curTime;

    Simulator::Schedule(Seconds(0.1), &TraceThroughput, monitor);
}

int main(int argc, char *argv[])
{
    bool verbose = true;
    uint32_t nCsma = 2;
    uint32_t nWifi = 2;
    std::string algo = "TcpWestwoodBR";
    bool tracing = false;
    Time stopTime = Seconds(50.0);

    outputFileName = "_data.csv";
    outputFileName2 = "finalData.csv";

    CommandLine cmd(__FILE__);
    cmd.AddValue("algo", "Tcp Algorithm want to apply", algo);
    cmd.AddValue("nCsma", "Number of \"extra\" CSMA nodes/devices", nCsma);
    cmd.AddValue("nWifi", "Number of wifi STA devices", nWifi);
    cmd.AddValue("verbose", "Tell echo applications to log if true", verbose);
    cmd.AddValue("tracing", "Enable pcap tracing", tracing);
    cmd.AddValue("stopTime", "Stop time for applications / simulation time will be stopTime + 1", stopTime);

    cmd.Parse(argc, argv);

    // The underlying restriction of 18 is due to the grid position
    // allocator's configuration; the grid layout will exceed the
    // bounding box if more than 18 nodes are provided.
    if (nWifi > 18)
    {
        std::cout << "nWifi should be 18 or less; otherwise grid layout exceeds the bounding box" << std::endl;
        return 1;
    }

    if (verbose)
    {
        // LogComponentEnable ("UdpEchoClientApplication", LOG_LEVEL_INFO);
        // LogComponentEnable ("UdpEchoServerApplication", LOG_LEVEL_INFO);
    }

    // set TCP WESTWOOD
    if (algo == "TcpWestwoodBR")
    {
        Config::SetDefault("ns3::TcpL4Protocol::SocketType", TypeIdValue(TcpWestwoodBR::GetTypeId()));
    }
    if (algo == "TcpWestwood")
    {
        Config::SetDefault("ns3::TcpL4Protocol::SocketType", TypeIdValue(TcpWestwood::GetTypeId()));
        Config::SetDefault("ns3::TcpWestwood::ProtocolType", EnumValue(TcpWestwood::WESTWOOD));
    }
    if (algo == "TcpNewReno")
    {
        Config::SetDefault("ns3::TcpL4Protocol::SocketType", TypeIdValue(TcpNewReno::GetTypeId()));
    }

    std::cout << "algo : " << algo << std::endl;

    NodeContainer p2pNodes;
    p2pNodes.Create(2);

    float error_p = 0.0001;
    Ptr<UniformRandomVariable> uv = CreateObject<UniformRandomVariable>();
    uv->SetStream(50);
    RateErrorModel error_model;
    error_model.SetRandomVariable(uv);
    error_model.SetUnit(RateErrorModel::ERROR_UNIT_PACKET);
    error_model.SetRate(error_p);

    PointToPointHelper pointToPoint;
    pointToPoint.SetDeviceAttribute("DataRate", StringValue("5Mbps"));
    pointToPoint.SetChannelAttribute("Delay", StringValue("2ms"));
    pointToPoint.SetDeviceAttribute("ReceiveErrorModel", PointerValue(&error_model));

    NetDeviceContainer p2pDevices;
    p2pDevices = pointToPoint.Install(p2pNodes);

    NodeContainer csmaNodes;
    csmaNodes.Add(p2pNodes.Get(1));
    csmaNodes.Create(nCsma);

    CsmaHelper csma;
    csma.SetChannelAttribute("DataRate", StringValue("100Mbps"));
    csma.SetChannelAttribute("Delay", TimeValue(NanoSeconds(6560)));

    NetDeviceContainer csmaDevices;
    csmaDevices = csma.Install(csmaNodes);

    NodeContainer wifiStaNodes;
    wifiStaNodes.Create(nWifi);
    NodeContainer wifiApNode = p2pNodes.Get(0);

    YansWifiChannelHelper channel = YansWifiChannelHelper::Default();
    YansWifiPhyHelper phy;
    phy.SetChannel(channel.Create());

    WifiHelper wifi;
    wifi.SetRemoteStationManager("ns3::AarfWifiManager");

    WifiMacHelper mac;
    Ssid ssid = Ssid("ns-3-ssid");
    mac.SetType("ns3::StaWifiMac",
                "Ssid", SsidValue(ssid),
                "ActiveProbing", BooleanValue(false));

    NetDeviceContainer staDevices;
    staDevices = wifi.Install(phy, mac, wifiStaNodes);

    mac.SetType("ns3::ApWifiMac",
                "Ssid", SsidValue(ssid));

    NetDeviceContainer apDevices;
    apDevices = wifi.Install(phy, mac, wifiApNode);

    MobilityHelper mobility;

    mobility.SetPositionAllocator("ns3::GridPositionAllocator",
                                  "MinX", DoubleValue(0.0),
                                  "MinY", DoubleValue(0.0),
                                  "DeltaX", DoubleValue(1.0),
                                  "DeltaY", DoubleValue(1.0),
                                  "GridWidth", UintegerValue(3),
                                  "LayoutType", StringValue("RowFirst"));

    mobility.SetMobilityModel("ns3::RandomWalk2dMobilityModel",
                              "Bounds", RectangleValue(Rectangle(-50, 50, -50, 50)));
    mobility.Install(wifiStaNodes);

    mobility.SetMobilityModel("ns3::ConstantPositionMobilityModel");
    mobility.Install(wifiApNode);

    InternetStackHelper stack;
    stack.Install(csmaNodes);
    stack.Install(wifiApNode);
    stack.Install(wifiStaNodes);

    Ipv4AddressHelper address;

    address.SetBase("10.1.1.0", "255.255.255.0");
    Ipv4InterfaceContainer p2pInterfaces;
    p2pInterfaces = address.Assign(p2pDevices);

    address.SetBase("10.1.2.0", "255.255.255.0");
    Ipv4InterfaceContainer csmaInterfaces;
    csmaInterfaces = address.Assign(csmaDevices);

    address.SetBase("10.1.3.0", "255.255.255.0");
    Ipv4InterfaceContainer wifiStaInterfaces = address.Assign(staDevices);
    Ipv4InterfaceContainer wifiApinterfaces = address.Assign(apDevices);

    uint32_t nFlows = 2;
    prev = new uint32_t[nFlows];
    for (int i = 0; i < int(nFlows); i++)
    {
        prev[i] = 0;
    }

    uint16_t port = 27;

    PacketSinkHelper sink("ns3::TcpSocketFactory", InetSocketAddress(Ipv4Address::GetAny(), port));
    ApplicationContainer sinkApps = sink.Install(csmaNodes.Get(nCsma - 1));
    sinkApps.Start(Seconds(0.0));
    sinkApps.Stop(stopTime);

    BulkSendHelper source("ns3::TcpSocketFactory", InetSocketAddress(csmaInterfaces.GetAddress(nCsma - 1), port));
    source.SetAttribute("MaxBytes", UintegerValue(0));
    ApplicationContainer sourceApps = source.Install(wifiStaNodes.Get(nWifi - 1));
    sourceApps.Start(Seconds(0.0));
    sourceApps.Stop(stopTime);

    Ipv4GlobalRoutingHelper::PopulateRoutingTables();

    FlowMonitorHelper flowmon;
    Ptr<FlowMonitor> monitor = flowmon.InstallAll();

    Simulator::Stop(stopTime + Seconds(1.0));

    outputFileName = algo + outputFileName;
    dir = "taskB_result/" + std::string("result") + "/";
    dirToSave = "mkdir -p " + dir;
    if (system(dirToSave.c_str()) == -1)
    {
        exit(1);
    }

    std::ofstream out(dir + outputFileName, std::ios::out);
    out.clear();
    out << "time(s), "
        << "throughput(kbps), "
        << "Cumulative throughput (kbps)" << std::endl;
    out.flush();
    out.close();

    if (algo == "TcpWestwoodBR")
    {
        std::ofstream outp(dir + outputFileName2, std::ios::out);
        outp.clear();
        outp << "Tcp Algorithm, "
             << "Avg. Throughput (kbps), "
             << "Packet Loss Ratio (%), "
             << "End to End Delay (s)" << std::endl;
        outp.flush();
        outp.close();
    }

    Simulator::Schedule(Seconds(1.0 + 0.000001), &TraceThroughput, monitor);

    Simulator::Run();

    int j = 0;
    float AvgThroughput = 0;
    Time Delay;
    uint32_t SentPackets = 0;
    uint32_t ReceivedPackets = 0;
    uint32_t LostPackets = 0;

    Ptr<Ipv4FlowClassifier> classifier = DynamicCast<Ipv4FlowClassifier>(flowmon.GetClassifier());
    FlowMonitor::FlowStatsContainer stats = monitor->GetFlowStats();

    for (auto iter = stats.begin(); iter != stats.end(); ++iter)
    {
        Ipv4FlowClassifier::FiveTuple t = classifier->FindFlow(iter->first);
        NS_LOG_UNCOND("----Flow ID:" << iter->first);
        NS_LOG_UNCOND("Src Addr" << t.sourceAddress << " -- Dst Addr " << t.destinationAddress);
        NS_LOG_UNCOND("Sent Packets=" << iter->second.txPackets);
        NS_LOG_UNCOND("Received Packets =" << iter->second.rxPackets);
        NS_LOG_UNCOND("Lost Packets =" << iter->second.txPackets - iter->second.rxPackets);
        NS_LOG_UNCOND("Packet delivery ratio =" << iter->second.rxPackets * 100.0 / iter->second.txPackets << "%");
        NS_LOG_UNCOND("Packet loss ratio =" << (iter->second.txPackets - iter->second.rxPackets) * 100.0 / iter->second.txPackets << "%");

        NS_LOG_UNCOND("Delay =" << iter->second.delaySum);
        NS_LOG_UNCOND("Throughput =" << iter->second.rxBytes * 8.0 / (iter->second.timeLastRxPacket.GetSeconds() - iter->second.timeFirstTxPacket.GetSeconds()) / 1000 << "Kbps");

        SentPackets = SentPackets + (iter->second.txPackets);
        ReceivedPackets = ReceivedPackets + (iter->second.rxPackets);
        LostPackets = LostPackets + (iter->second.txPackets - iter->second.rxPackets);
        AvgThroughput = AvgThroughput + (iter->second.rxBytes * 8.0 / (iter->second.timeLastRxPacket.GetSeconds() - iter->second.timeFirstTxPacket.GetSeconds()) / 1000);
        Delay = Delay + (iter->second.delaySum);

        j = j + 1;
    }

    AvgThroughput = AvgThroughput / j;

    std::ofstream out2(dir + outputFileName2, std::ios::out | std::ios::app);
    out2 << algo << "," << AvgThroughput << "," << ((LostPackets * 100.00) / SentPackets) << "," << Delay.GetSeconds() / (ReceivedPackets * 1.0) << std::endl;

    NS_LOG_UNCOND("--------Total Results of the simulation----------" << std::endl);
    NS_LOG_UNCOND("Total sent packets  =" << SentPackets);
    NS_LOG_UNCOND("Total Received Packets =" << ReceivedPackets);
    NS_LOG_UNCOND("Total Lost Packets =" << LostPackets);
    NS_LOG_UNCOND("Packet Loss ratio =" << ((LostPackets * 100.00) / SentPackets) << "%");
    NS_LOG_UNCOND("Packet delivery ratio =" << ((ReceivedPackets * 100.00) / SentPackets) << "%");
    NS_LOG_UNCOND("Average Throughput =" << AvgThroughput << "Kbps");
    NS_LOG_UNCOND("End to End Delay =" << Delay.GetSeconds() / (ReceivedPackets * 1.0));

    NS_LOG_UNCOND("Total Flow id " << j);

    Simulator::Destroy();
    return 0;
}
