#include "ns3/core-module.h"
#include "ns3/point-to-point-module.h"
#include "ns3/network-module.h"
#include "ns3/applications-module.h"
#include "ns3/mobility-module.h"
#include "ns3/csma-module.h"
#include "ns3/internet-module.h"
#include "ns3/yans-wifi-helper.h"
#include "ns3/ssid.h"
#include "ns3/flow-monitor-helper.h"
#include "ns3/ipv4-flow-classifier.h"

// Default Network Topology
//
//   Wifi 10.1.3.0
//                 AP
//  *    *    *    *
//  |    |    |    |    10.1.1.0
// n5   n6   n7   n0 -------------- n1   n2   n3   n4
//                   point-to-point  |    |    |    |
//                                   *    *    *    *
//                                     Wifi 10.1.2.0

using namespace ns3;

NS_LOG_COMPONENT_DEFINE("TaskA-1-ScriptExample");

std::string TYPE, SPEED;
uint32_t totalFlows, NUMBEROFNODES, PACKETSPS;
uint32_t *prev;
Time prevTime = Seconds(0);

std::string dir;
std::string dirToSave;
std::string outputFileName;

void printFlow(FlowMonitorHelper *flowmon, Ptr<FlowMonitor> monitor)
{
    Time totalDelay;
    float throughPut = 0;
    uint32_t SentPackets = 0;
    uint32_t ReceivedPackets = 0;
    uint32_t LostPackets = 0;
    int j = 0;

    std::ofstream flowout("taskA1-flow.dat", std::ios::out);

    monitor->CheckForLostPackets();
    Ptr<Ipv4FlowClassifier> classifier = DynamicCast<Ipv4FlowClassifier>(flowmon->GetClassifier());
    FlowMonitor::FlowStatsContainer stats = monitor->GetFlowStats();
    for (std::map<FlowId, FlowMonitor::FlowStats>::const_iterator i = stats.begin(); i != stats.end(); ++i)
    {
        Ipv4FlowClassifier::FiveTuple t = classifier->FindFlow(i->first);
        std::cout << "Flow " << i->first << " (" << t.sourceAddress << "(" << t.sourcePort << ")"
                  << " -> " << t.destinationAddress << "(" << t.destinationPort << ")"
                  << ")\n";
        std::cout << "  Tx Packets: " << i->second.txPackets << "\n";
        std::cout << "  Tx Bytes:   " << i->second.txBytes << "\n";
        std::cout << "  TxOffered:  " << i->second.txBytes * 8.0 / 10.0 / 1000 << " kbps\n";
        std::cout << "  Rx Packets: " << i->second.rxPackets << "\n";
        std::cout << "  Rx Bytes:   " << i->second.rxBytes << "\n";
        std::cout << "  Lost Packets:   " << i->second.lostPackets << "\n";
        std::cout << "  End to end Delay:   " << i->second.delaySum.GetSeconds() << "s\n";
        std::cout << "  Packet Delivery Ratio:   " << (i->second.rxPackets * 100.0) / i->second.txPackets << "\n";
        std::cout << "  Packet Loss Ratio:   " << (i->second.lostPackets * 100.0) / i->second.txPackets << "\n";

        double calthroughput = i->second.rxBytes * 8.0 / (i->second.timeLastRxPacket.GetSeconds() - i->second.timeFirstTxPacket.GetSeconds()) / 1000;

        std::cout << "  Throughput: " << calthroughput << " kbps\n";

        SentPackets = SentPackets + (i->second.txPackets);
        ReceivedPackets = ReceivedPackets + (i->second.rxPackets);
        LostPackets = LostPackets + i->second.lostPackets;
        throughPut += calthroughput;
        totalDelay += i->second.delaySum;

        j++;

        flowout << i->first << " " << calthroughput << std::endl;
    }

    std::ofstream out(dir + outputFileName, std::ios::out | std::ios::app);

    float avgthroughPut = throughPut / totalFlows;
    NS_LOG_UNCOND("\n\n--------Total Results of the simulation----------" << std::endl);
    NS_LOG_UNCOND("Total sent packets  : " << SentPackets);
    NS_LOG_UNCOND("Total Received Packets : " << ReceivedPackets);
    NS_LOG_UNCOND("Total Lost Packets : " << LostPackets);
    NS_LOG_UNCOND("Packet Loss ratio : " << ((LostPackets * 100.0) / SentPackets) << "%");
    NS_LOG_UNCOND("Packet delivery ratio : " << ((ReceivedPackets * 100.0) / SentPackets) << "%");
    NS_LOG_UNCOND("Average throughput : " << avgthroughPut << " kbps");
    NS_LOG_UNCOND("End to End Delay : " << totalDelay.GetSeconds() / (ReceivedPackets * 1.0) << "s");
    NS_LOG_UNCOND("Total Flows : " << j);

    if (TYPE == "NODE")
    {
        out << NUMBEROFNODES << ",";
    }
    else if (TYPE == "FLOW")
    {
        out << totalFlows << ",";
    }
    else if (TYPE == "SPEED")
    {
        out << SPEED << ",";
    }
    else
    {
        out << PACKETSPS << ",";
    }

    out << throughPut << "," << avgthroughPut << "," << totalDelay.GetSeconds() / (ReceivedPackets * 1.0) << ","
        << ((ReceivedPackets * 100.0) / SentPackets) << "," << ((LostPackets * 100.0) / SentPackets) << std::endl;
}

// Calculate throughput
static void
TraceThroughput(Ptr<FlowMonitor> monitor)
{
    FlowMonitor::FlowStatsContainer stats = monitor->GetFlowStats();
    int cnt = 0;
    int total = 0;
    Time curTime = Now();

    for (auto itr = stats.begin(); itr != stats.end(); itr++)
    {
        total += (itr->second.txBytes - prev[cnt]);
        prev[cnt] = itr->second.txBytes;
        cnt++;
    }
    std::ofstream thr("taskA1.dat", std::ios::out | std::ios::app);

    thr << curTime.GetSeconds() << " " << 8 * (total) / (1000 * (curTime.GetSeconds() - prevTime.GetSeconds())) << std::endl;

    std::cout << "Time : " << curTime.GetSeconds() << "s --> " << 8 * (total) / (1000 * (curTime.GetSeconds() - prevTime.GetSeconds())) << "kbps" << std::endl;

    prevTime = curTime;

    Simulator::Schedule(Seconds(0.1), &TraceThroughput, monitor);
}

int main(int argc, char *argv[])
{
    bool firstTime = false;
    bool verbose = true;
    uint32_t nNodes = 20; // number of nodes
    uint32_t nWifi = nNodes / 2;
    uint32_t nFlows = 10;          // number of flows
    std::string nodeSpeed = "5.0"; // speed of node
    std::string type = "NODE";
    bool tracing = false;
    uint16_t port = 50000;
    uint32_t packetSize = 10000;
    uint32_t packetsPS = 100; // packets per second

    CommandLine cmd(__FILE__);
    // cmd.AddValue("nCsma", "Number of \"extra\" CSMA nodes/devices", nCsma);
    cmd.AddValue("first", "run this type of variation first time", firstTime);
    cmd.AddValue("nNodes", "Number of wifi STA devices", nNodes);
    cmd.AddValue("nFlows", "Number of Flows", nFlows);
    cmd.AddValue("type", "parameter want to vary", type);
    cmd.AddValue("nodeSpeed", "Number of wifi STA devices", nodeSpeed);
    cmd.AddValue("packetsPerSec", "Number of packets per second", packetsPS);
    cmd.AddValue("verbose", "Tell echo applications to log if true", verbose);
    cmd.AddValue("tracing", "Enable pcap tracing", tracing);

    cmd.Parse(argc, argv);

    // The underlying restriction of 18 is due to the grid position
    // allocator's configuration; the grid layout will exceed the
    // bounding box if more than 18 nodes are provided.

    std::cout << "type: " << type << " Nodes : " << nNodes << " Flows : " << nFlows
              << " Speed : " << nodeSpeed << " packet per sec: " << packetsPS << std::endl;

    nWifi = nNodes / 2;
    uint32_t dataRate = packetsPS * packetSize;
    outputFileName = type + "_data.csv";
    totalFlows = nFlows;
    TYPE = type;
    NUMBEROFNODES = nNodes;
    PACKETSPS = packetsPS;
    SPEED = nodeSpeed;

    if (nWifi > 50)
    {
        std::cout << "nWifi should be 100 or less; otherwise grid layout exceeds the bounding box" << std::endl;
        return 1;
    }

    if (verbose)
    {
        // LogComponentEnable("UdpEchoClientApplication", LOG_LEVEL_INFO);
    }

    // set TCP WESTWOOD
    // Config::SetDefault("ns3::TcpL4Protocol::SocketType", TypeIdValue(TcpWestwood::GetTypeId()));
    // Config::SetDefault("ns3::TcpWestwood::ProtocolType", EnumValue(TcpWestwood::WESTWOOD));

    // Config::SetDefault("ns3::TcpL4Protocol::SocketType", TypeIdValue(TcpWestwoodBR::GetTypeId()));

    NodeContainer p2pNodes;
    p2pNodes.Create(2);

    PointToPointHelper pointToPoint;
    pointToPoint.SetDeviceAttribute("DataRate", StringValue("2Mbps"));
    pointToPoint.SetChannelAttribute("Delay", StringValue("2ms"));

    NetDeviceContainer p2pDevices;
    p2pDevices = pointToPoint.Install(p2pNodes);

    NodeContainer wifiStaNodes2;
    wifiStaNodes2.Create(nWifi);
    NodeContainer wifiApNode2 = p2pNodes.Get(1);

    YansWifiChannelHelper channel2 = YansWifiChannelHelper::Default();
    YansWifiPhyHelper phy2;
    phy2.SetChannel(channel2.Create());

    WifiHelper wifi2;
    wifi2.SetRemoteStationManager("ns3::AarfWifiManager");

    WifiMacHelper mac2;
    Ssid ssid2 = Ssid("ns-3-ssid");
    mac2.SetType("ns3::StaWifiMac",
                 "Ssid", SsidValue(ssid2),
                 "ActiveProbing", BooleanValue(false));

    NetDeviceContainer staDevices2;
    staDevices2 = wifi2.Install(phy2, mac2, wifiStaNodes2);

    mac2.SetType("ns3::ApWifiMac",
                 "Ssid", SsidValue(ssid2));

    NetDeviceContainer apDevices2;
    apDevices2 = wifi2.Install(phy2, mac2, wifiApNode2);

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
                                  "DeltaX", DoubleValue(0.5),
                                  "DeltaY", DoubleValue(1.2),
                                  "GridWidth", UintegerValue(3),
                                  "LayoutType", StringValue("RowFirst"));

    mobility.SetMobilityModel("ns3::RandomWalk2dMobilityModel",
                              "Speed", StringValue("ns3::ConstantRandomVariable[Constant=" + nodeSpeed + "]"),
                              "Bounds", RectangleValue(Rectangle(-50, 50, -50, 50)));

    mobility.Install(wifiStaNodes);

    mobility.SetMobilityModel("ns3::ConstantPositionMobilityModel");
    mobility.Install(wifiApNode);

    MobilityHelper mobility2;

    mobility2.SetPositionAllocator("ns3::GridPositionAllocator",
                                   "MinX", DoubleValue(5.0),
                                   "MinY", DoubleValue(0.0),
                                   "DeltaX", DoubleValue(0.5),
                                   "DeltaY", DoubleValue(1.2),
                                   "GridWidth", UintegerValue(3),
                                   "LayoutType", StringValue("RowFirst"));

    mobility2.SetMobilityModel("ns3::RandomWalk2dMobilityModel",
                               "Speed", StringValue("ns3::ConstantRandomVariable[Constant=" + nodeSpeed + "]"),
                               "Bounds", RectangleValue(Rectangle(-50, 50, -50, 50)));

    mobility2.Install(wifiStaNodes2);

    mobility2.SetMobilityModel("ns3::ConstantPositionMobilityModel");
    mobility.Install(wifiApNode2);

    InternetStackHelper stack;
    stack.Install(wifiApNode2);
    stack.Install(wifiStaNodes2);
    stack.Install(wifiApNode);
    stack.Install(wifiStaNodes);

    Ipv4AddressHelper address;

    address.SetBase("10.1.1.0", "255.255.255.0");
    Ipv4InterfaceContainer p2pInterfaces;
    p2pInterfaces = address.Assign(p2pDevices);

    address.SetBase("10.1.2.0", "255.255.255.0");
    Ipv4InterfaceContainer staInterfaces2;
    staInterfaces2 = address.Assign(staDevices2);
    address.Assign(apDevices2);

    address.SetBase("10.1.3.0", "255.255.255.0");
    Ipv4InterfaceContainer staInterfaces;
    staInterfaces = address.Assign(staDevices);
    address.Assign(apDevices);

    prev = new uint32_t[nFlows];
    for (int i = 0; i < int(nFlows); i++)
    {
        prev[i] = 0;
    }

    Address sinkLocalAddress(InetSocketAddress(Ipv4Address::GetAny(), port));
    PacketSinkHelper packetSinkHelper("ns3::TcpSocketFactory", sinkLocalAddress);
    ApplicationContainer sinkApps;
    for (uint32_t i = 0; i < nWifi; ++i)
    {
        sinkApps.Add(packetSinkHelper.Install(wifiStaNodes2.Get(i)));
    }
    sinkApps.Start(Seconds(0.0));
    sinkApps.Stop(Seconds(10.0));

    ApplicationContainer sourceApps;

    uint selectNode = 0;

    for (uint j = 0; j < nFlows / 2; j++)
    {

        AddressValue remoteAddress(InetSocketAddress(staInterfaces2.GetAddress(selectNode), port));

        OnOffHelper sourceHelper("ns3::TcpSocketFactory", Address());
        sourceHelper.SetAttribute("Remote", remoteAddress);
        sourceHelper.SetAttribute("MaxBytes", UintegerValue(0));
        sourceHelper.SetAttribute("OnTime", StringValue("ns3::ConstantRandomVariable[Constant=1]"));
        sourceHelper.SetAttribute("OffTime", StringValue("ns3::ConstantRandomVariable[Constant=0]"));
        sourceHelper.SetAttribute("PacketSize", UintegerValue(packetSize));
        sourceHelper.SetAttribute("DataRate", DataRateValue(DataRate(dataRate)));

        sourceApps.Add(sourceHelper.Install(wifiStaNodes.Get(selectNode)));

        selectNode = (selectNode + 1) % nWifi;
    }

    sourceApps.Start(Seconds(1.0));
    sourceApps.Stop(Seconds(10.0));

    Ipv4GlobalRoutingHelper::PopulateRoutingTables();

    Simulator::Stop(Seconds(10.0));

    dir = "taskA1_result/" + std::string("result") + "/";
    dirToSave = "mkdir -p " + dir;
    if (system(dirToSave.c_str()) == -1)
    {
        exit(1);
    }
    if (firstTime)
    {
        std::ofstream out(dir + outputFileName, std::ios::out);
        out.clear();
        out << type + ", "
            << "Network throughput(kbps), "
            << "Network throughput per flow(kbps), "
            << "End to End Delay (s), "
            << "Packet Delivery Ratio (%), "
            << "Packet Drop Ratio (%)" << std::endl;
        out.flush();
        out.close();
    }
    // Flow monitor
    FlowMonitorHelper flowmon;
    Ptr<FlowMonitor> monitor = flowmon.InstallAll();

    // std::ofstream thr("taskA1.dat", std::ios::out);
    // Simulator::Schedule(Seconds(1.0 + 0.000001), &TraceThroughput, monitor);

    Simulator::Run();

    // flowmon.SerializeToXmlFile("taskA1.flowmonitor", true, true);
    // Print per flow statistics
    printFlow(&flowmon, monitor);

    Simulator::Destroy();
    return 0;
}