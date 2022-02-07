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
//                                   ================
//                                     LAN 10.1.2.0

using namespace ns3;

NS_LOG_COMPONENT_DEFINE("ThirdScriptExample");

double SIMULATION_TIME = 10.0; // s
uint32_t NUMOFNODES = 8;
uint32_t WIRED_BANDWIDTH = 100;    // Mbps
uint32_t WIRED_DELAY = 30;         // ms
uint32_t BOTTLENECKBANDWIDTH = 10; // Mbps
uint32_t BOTTLENECKDELAY = 10;     // ms
uint32_t WIRELESS_BANDWIDTH = 5;   // Mbps
uint32_t PACKET_SIZE = 1000;       // Bytes
float WIRELESS_DELAY = 0.01;       // ms
uint16_t port = 50000;
uint32_t maxBytes = PACKET_SIZE;

uint32_t prev = 0;
Time prevTime = Seconds(0);

void printFlow(FlowMonitorHelper *flowmon, Ptr<FlowMonitor> monitor)
{
    float throughPut = 0;
    uint32_t SentPackets = 0;
    uint32_t ReceivedPackets = 0;
    uint32_t LostPackets = 0;
    int j = 0;

    std::ofstream flowout("mytest-flow.dat", std::ios::out);

    monitor->CheckForLostPackets();
    Ptr<Ipv4FlowClassifier> classifier = DynamicCast<Ipv4FlowClassifier>(flowmon->GetClassifier());
    FlowMonitor::FlowStatsContainer stats = monitor->GetFlowStats();
    for (std::map<FlowId, FlowMonitor::FlowStats>::const_iterator i = stats.begin(); i != stats.end(); ++i)
    {
        Ipv4FlowClassifier::FiveTuple t = classifier->FindFlow(i->first);
        std::cout << "Flow " << i->first << " (" << t.sourceAddress << " -> " << t.destinationAddress << ")\n";
        std::cout << "  Tx Packets: " << i->second.txPackets << "\n";
        std::cout << "  Tx Bytes:   " << i->second.txBytes << "\n";
        std::cout << "  TxOffered:  " << i->second.txBytes * 8.0 / SIMULATION_TIME / 1000 << " kbps\n";
        std::cout << "  Rx Packets: " << i->second.rxPackets << "\n";
        std::cout << "  Rx Bytes:   " << i->second.rxBytes << "\n";

        double calthroughput = i->second.rxBytes * 8.0 / SIMULATION_TIME / 1000;

        std::cout << "  Throughput: " << calthroughput << " kbps\n";

        SentPackets = SentPackets + (i->second.txPackets);
        ReceivedPackets = ReceivedPackets + (i->second.rxPackets);
        LostPackets = LostPackets + (i->second.txPackets - i->second.rxPackets);
        throughPut += calthroughput;

        j++;

        flowout << i->first << " " << calthroughput << std::endl;
    }

    float avgthroughPut = throughPut / j;
    NS_LOG_UNCOND("\n\n--------Total Results of the simulation----------" << std::endl);
    NS_LOG_UNCOND("Total sent packets  : " << SentPackets);
    NS_LOG_UNCOND("Total Received Packets : " << ReceivedPackets);
    NS_LOG_UNCOND("Total Lost Packets : " << LostPackets);
    NS_LOG_UNCOND("Packet Loss ratio : " << ((LostPackets * 100.0) / SentPackets) << "%");
    NS_LOG_UNCOND("Packet delivery ratio : " << ((ReceivedPackets * 100.0) / SentPackets) << "%");
    NS_LOG_UNCOND("Average throughput : " << avgthroughPut << " kbps");
    NS_LOG_UNCOND("Total Flow id : " << j);
}

// Calculate throughput
static void
TraceThroughput(Ptr<FlowMonitor> monitor)
{
    FlowMonitor::FlowStatsContainer stats = monitor->GetFlowStats();
    auto itr = stats.begin();
    Time curTime = Now();
    std::ofstream thr("mytest-throughput.dat", std::ios::out | std::ios::app);
    thr << curTime.GetSeconds() << " " << 8 * (itr->second.txBytes - prev) / (1000 * (curTime.GetSeconds() - prevTime.GetSeconds())) << std::endl;
    prevTime = curTime;
    prev = itr->second.txBytes;
    Simulator::Schedule(Seconds(0.1), &TraceThroughput, monitor);
}

int main(int argc, char *argv[])
{
    bool verbose = true;
    uint32_t nCsma = NUMOFNODES;
    uint32_t nWifi = NUMOFNODES;
    bool tracing = false;

    CommandLine cmd(__FILE__);
    cmd.AddValue("nCsma", "Number of \"extra\" CSMA nodes/devices", nCsma);
    cmd.AddValue("nWifi", "Number of wifi STA devices", nWifi);
    cmd.AddValue("verbose", "Tell echo applications to log if true", verbose);
    cmd.AddValue("tracing", "Enable pcap tracing", tracing);

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
        LogComponentEnable("BulkSendApplication", LOG_LEVEL_INFO);
    }

    // set TCP WESTWOOD
    Config::SetDefault("ns3::TcpL4Protocol::SocketType", TypeIdValue(TcpWestwood::GetTypeId()));
    Config::SetDefault("ns3::TcpWestwood::ProtocolType", EnumValue(TcpWestwood::WESTWOOD));

    NodeContainer p2pNodes;
    p2pNodes.Create(2);

    PointToPointHelper pointToPoint;
    pointToPoint.SetDeviceAttribute("DataRate", StringValue(std::to_string(BOTTLENECKBANDWIDTH) + "Mbps"));
    pointToPoint.SetChannelAttribute("Delay", StringValue(std::to_string(BOTTLENECKDELAY) + "ms"));

    NetDeviceContainer p2pDevices;
    p2pDevices = pointToPoint.Install(p2pNodes);

    NodeContainer csmaNodes;
    csmaNodes.Add(p2pNodes.Get(1));
    csmaNodes.Create(nCsma);

    CsmaHelper csma;
    csma.SetChannelAttribute("DataRate", StringValue(std::to_string(WIRED_BANDWIDTH) + "Mbps"));
    csma.SetChannelAttribute("Delay", StringValue(std::to_string(WIRED_DELAY) + "ms"));

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

    Ptr<RateErrorModel> em = CreateObject<RateErrorModel>();
    em->SetAttribute("ErrorRate", DoubleValue(.00001));

    for (uint32_t i = 1; i <= nWifi; i++)
    {
        Config::Set("/NodeList/" + std::to_string(i) +
                        "/DeviceList/0/$ns3::WifiNetDevice/Phy/$ns3::YansWifiPhy/PostReceptionErrorModel",
                    PointerValue(em));
    }

    MobilityHelper mobility;

    mobility.SetPositionAllocator("ns3::GridPositionAllocator",
                                  "MinX", DoubleValue(0.0),
                                  "MinY", DoubleValue(0.0),
                                  "DeltaX", DoubleValue(5.0),
                                  "DeltaY", DoubleValue(10.0),
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
    Ipv4InterfaceContainer staInterfaces = address.Assign(staDevices);
    Ipv4InterfaceContainer apInterfaces = address.Assign(apDevices);

    //=======first one================//

    for (uint16_t i = 0; i < nCsma; i++)
    {
        BulkSendHelper source("ns3::TcpSocketFactory",
                              InetSocketAddress(csmaInterfaces.GetAddress(i), port));
        // Set the amount of data to send in bytes.  Zero is unlimited.
        source.SetAttribute("MaxBytes", UintegerValue(maxBytes * 10));
        ApplicationContainer sourceApps = source.Install(wifiStaNodes.Get(i));
        sourceApps.Start(Seconds(0.0));
        sourceApps.Stop(Seconds(SIMULATION_TIME));

        PacketSinkHelper sink("ns3::TcpSocketFactory",
                              InetSocketAddress(Ipv4Address::GetAny(), port));
        ApplicationContainer sinkApps = sink.Install(csmaNodes.Get(i));
        sinkApps.Start(Seconds(0.0));
        sinkApps.Stop(Seconds(SIMULATION_TIME));
    }

    //=====first done========//

    // for (uint16_t i = 0; i < nCsma; i++)
    // {
    //     BulkSendHelper source("ns3::TcpSocketFactory",
    //                           InetSocketAddress(staInterfaces.GetAddress(i), port));
    //     // Set the amount of data to send in bytes.  Zero is unlimited.
    //     source.SetAttribute("MaxBytes", UintegerValue(maxBytes * 100));
    //     ApplicationContainer sourceApps = source.Install(csmaNodes.Get(nCsma - i));
    //     sourceApps.Start(Seconds(0.0));
    //     sourceApps.Stop(Seconds(SIMULATION_TIME));

    //     //
    //     // Create a PacketSinkApplication and install it on node
    //     //
    //     PacketSinkHelper sink("ns3::TcpSocketFactory",
    //                           InetSocketAddress(Ipv4Address::GetAny(), port));
    //     ApplicationContainer sinkApps = sink.Install(wifiStaNodes.Get(i));
    //     sinkApps.Start(Seconds(0.0));
    //     sinkApps.Stop(Seconds(SIMULATION_TIME));
    // }

    //=========second one=============//

    // for (uint16_t i = 0; i < 1; i++)
    // {
    //     BulkSendHelper source("ns3::TcpSocketFactory",
    //                           InetSocketAddress(csmaInterfaces.GetAddress(i), port));
    //     // Set the amount of data to send in bytes.  Zero is unlimited.
    //     source.SetAttribute("MaxBytes", UintegerValue(maxBytes));
    //     ApplicationContainer sourceApps = source.Install(wifiStaNodes.Get(i));
    //     sourceApps.Start(Seconds(0.0));
    //     sourceApps.Stop(Seconds(SIMULATION_TIME));

    //     //
    //     // Create a PacketSinkApplication and install it on node
    //     //
    //     PacketSinkHelper sink("ns3::TcpSocketFactory",
    //                           InetSocketAddress(Ipv4Address::GetAny(), port));
    //     ApplicationContainer sinkApps = sink.Install(csmaNodes.Get(i));
    //     sinkApps.Start(Seconds(0.0));
    //     sinkApps.Stop(Seconds(SIMULATION_TIME));
    // }

    // for (uint16_t i = 1; i < nCsma; i++)
    // {
    //     AddressValue remoteAddress(InetSocketAddress(csmaInterfaces.GetAddress(i), port));
    //     Config::SetDefault("ns3::TcpSocket::SegmentSize", UintegerValue(PACKET_SIZE));
    //     OnOffHelper fgSend("ns3::TcpSocketFactory", Address());
    //     fgSend.SetAttribute("Remote", remoteAddress);
    //     fgSend.SetAttribute("PacketSize", UintegerValue(750));
    //     fgSend.SetAttribute("OnTime", StringValue("ns3::ConstantRandomVariable[Constant=1]"));
    //     fgSend.SetAttribute("OffTime", StringValue("ns3::ConstantRandomVariable[Constant=0]"));
    //     fgSend.SetAttribute("DataRate", DataRateValue(DataRate("100Mbps")));
    //     ApplicationContainer sourceApp = fgSend.Install(wifiStaNodes.Get(nWifi - i));

    //     sourceApp.Start(Seconds(0.0));
    //     sourceApp.Stop(Seconds(10.0));

    //     Address sinkLocalAddress(InetSocketAddress(Ipv4Address::GetAny(), port));
    //     PacketSinkHelper sinkHelper("ns3::TcpSocketFactory", sinkLocalAddress);
    //     // sinkHelper.SetAttribute("Protocol", TypeIdValue(TcpSocketFactory::GetTypeId()));
    //     ApplicationContainer sinkApp = sinkHelper.Install(csmaNodes.Get(i));
    //     sinkApp.Start(Seconds(0.0));
    //     sinkApp.Stop(Seconds(10.0));
    // }

    //==========second done=========//

    // for (uint16_t i = 1; i < nCsma; i++)
    // {
    //     AddressValue remoteAddress(InetSocketAddress(staInterfaces.GetAddress(i), port));
    //     Config::SetDefault("ns3::TcpSocket::SegmentSize", UintegerValue(PACKET_SIZE));
    //     OnOffHelper fgSend("ns3::TcpSocketFactory", Address());
    //     fgSend.SetAttribute("Remote", remoteAddress);
    //     fgSend.SetAttribute("PacketSize", UintegerValue(1472));
    //     fgSend.SetAttribute("OnTime", StringValue("ns3::ConstantRandomVariable[Constant=1]"));
    //     fgSend.SetAttribute("OffTime", StringValue("ns3::ConstantRandomVariable[Constant=0]"));
    //     fgSend.SetAttribute("DataRate", DataRateValue(DataRate("100Mbps")));
    //     ApplicationContainer sourceApp = fgSend.Install(csmaNodes.Get(i));

    //     sourceApp.Start(Seconds(0.0));
    //     sourceApp.Stop(Seconds(10.0));

    //     Address sinkLocalAddress(InetSocketAddress(Ipv4Address::GetAny(), port));
    //     PacketSinkHelper sinkHelper("ns3::TcpSocketFactory", sinkLocalAddress);
    //     // sinkHelper.SetAttribute("Protocol", TypeIdValue(TcpSocketFactory::GetTypeId()));
    //     ApplicationContainer sinkApp = sinkHelper.Install(wifiStaNodes.Get(i));
    //     sinkApp.Start(Seconds(0.0));
    //     sinkApp.Stop(Seconds(10.0));
    // }

    Ipv4GlobalRoutingHelper::PopulateRoutingTables();

    if (tracing)
    {
        phy.SetPcapDataLinkType(WifiPhyHelper::DLT_IEEE802_11_RADIO);
        pointToPoint.EnablePcapAll("third");
        phy.EnablePcap("third", apDevices.Get(0));
        csma.EnablePcap("third", csmaDevices.Get(0), true);
    }

    // Flow monitor
    FlowMonitorHelper flowmon;
    Ptr<FlowMonitor> monitor = flowmon.InstallAll();

    std::ofstream thr("mytest-throughput.dat", std::ios::out);
    Simulator::Schedule(Seconds(1.1 + 0.000001), &TraceThroughput, monitor);

    Simulator::Stop(Seconds(SIMULATION_TIME));
    Simulator::Run();

    flowmon.SerializeToXmlFile("mytest.flowmonitor", true, true);

    // Print per flow statistics
    printFlow(&flowmon, monitor);

    // Cleanup
    Simulator::Destroy();

    // shFlow(&flowmon, monitor, "mytest-dropratio.data");
    //  NS_LOG_INFO("Run Simulation.");
    //  Simulator::Stop(Seconds(10.0));
    //  Simulator::Run();
    //  Simulator::Destroy();
    //  NS_LOG_INFO("Done.");

    std::cout << "Done." << std::endl;

    // // Ptr<PacketSink> sink1 = DynamicCast<PacketSink>(sinkApps.Get(0));
    // // std::cout << "Total Bytes Received: " << sink1->GetTotalRx() << std::endl;

    // // Simulator::Run();
    // // Simulator::Destroy();
    return 0;
}
