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

uint32_t prev = 0;
Time prevTime = Seconds(0);

void printFlow(FlowMonitorHelper *flowmon, Ptr<FlowMonitor> monitor)
{
    float throughPut = 0;
    uint32_t SentPackets = 0;
    uint32_t ReceivedPackets = 0;
    uint32_t LostPackets = 0;
    int j = 0;

    monitor->CheckForLostPackets();
    Ptr<Ipv4FlowClassifier> classifier = DynamicCast<Ipv4FlowClassifier>(flowmon->GetClassifier());
    FlowMonitor::FlowStatsContainer stats = monitor->GetFlowStats();
    for (std::map<FlowId, FlowMonitor::FlowStats>::const_iterator i = stats.begin(); i != stats.end(); ++i)
    {
        Ipv4FlowClassifier::FiveTuple t = classifier->FindFlow(i->first);
        std::cout << "Flow " << i->first << " (" << t.sourceAddress << " -> " << t.destinationAddress << ")\n";
        std::cout << "  Tx Packets: " << i->second.txPackets << "\n";
        std::cout << "  Tx Bytes:   " << i->second.txBytes << "\n";
        std::cout << "  TxOffered:  " << i->second.txBytes * 8.0 / 10.0 / 1000 << " kbps\n";
        std::cout << "  Rx Packets: " << i->second.rxPackets << "\n";
        std::cout << "  Rx Bytes:   " << i->second.rxBytes << "\n";
        std::cout << "  Throughput: " << i->second.rxBytes * 8.0 / 10.0 / 1000 << " kbps\n";

        SentPackets = SentPackets + (i->second.txPackets);
        ReceivedPackets = ReceivedPackets + (i->second.rxPackets);
        LostPackets = LostPackets + (i->second.txPackets - i->second.rxPackets);
        throughPut += (i->second.rxBytes * 8.0 / 10.0 / 1000);

        j++;
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

// void shFlow(FlowMonitorHelper *flowmon, Ptr<FlowMonitor> monitor, std::string flow_file)
// {
//     AsciiTraceHelper ascii;
//     static Ptr<OutputStreamWrapper> flowStream = ascii.CreateFileStream("tcp-nice-drop.dat");

//     *flowStream->GetStream() << "Session DeliveryRatio LossRatio\n";
//     uint32_t SentPackets = 0;
//     uint32_t ReceivedPackets = 0;
//     uint32_t LostPackets = 0;
//     int j = 0;
//     Ptr<Ipv4FlowClassifier> classifier = DynamicCast<Ipv4FlowClassifier>(flowmon->GetClassifier());
//     std::map<FlowId, FlowMonitor::FlowStats> stats = monitor->GetFlowStats();

//     for (std::map<FlowId, FlowMonitor::FlowStats>::const_iterator iter = stats.begin(); iter != stats.end(); ++iter)
//     {
//         Ipv4FlowClassifier::FiveTuple t = classifier->FindFlow(iter->first);

//         NS_LOG_UNCOND("----Flow ID:" << iter->first);
//         NS_LOG_UNCOND("Src Addr" << t.sourceAddress << "Dst Addr " << t.destinationAddress);
//         NS_LOG_UNCOND("Sent Packets=" << iter->second.txPackets);
//         NS_LOG_UNCOND("Received Packets =" << iter->second.rxPackets);
//         NS_LOG_UNCOND("Lost Packets =" << iter->second.txPackets - iter->second.rxPackets);
//         NS_LOG_UNCOND("Packet delivery ratio =" << iter->second.rxPackets * 100.0 / iter->second.txPackets << "%");
//         NS_LOG_UNCOND("Packet loss ratio =" << (iter->second.txPackets - iter->second.rxPackets) * 100.0 / iter->second.txPackets << "%");

//         *flowStream->GetStream() << t.sourceAddress << "-" << t.destinationAddress << " " << iter->second.rxPackets * 100.0 / iter->second.txPackets << " " << (iter->second.txPackets - iter->second.rxPackets) * 100.0 / iter->second.txPackets << "\n";

//         SentPackets = SentPackets + (iter->second.txPackets);
//         ReceivedPackets = ReceivedPackets + (iter->second.rxPackets);
//         LostPackets = LostPackets + (iter->second.txPackets - iter->second.rxPackets);

//         j++;
//     }
//     NS_LOG_UNCOND("--------Total Results of the simulation----------" << std::endl);
//     NS_LOG_UNCOND("Total sent packets  =" << SentPackets);
//     NS_LOG_UNCOND("Total Received Packets =" << ReceivedPackets);
//     NS_LOG_UNCOND("Total Lost Packets =" << LostPackets);
//     NS_LOG_UNCOND("Packet Loss ratio =" << ((LostPackets * 100.0) / SentPackets) << "%");
//     NS_LOG_UNCOND("Packet delivery ratio =" << ((ReceivedPackets * 100.0) / SentPackets) << "%");
//     NS_LOG_UNCOND("Total Flod id " << j);
// }

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
    Simulator::Schedule(Seconds(0.2), &TraceThroughput, monitor);
}

int main(int argc, char *argv[])
{
    bool verbose = true;
    uint32_t nCsma = 5;
    uint32_t nWifi = 5;
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
    pointToPoint.SetDeviceAttribute("DataRate", StringValue("5Mbps"));
    pointToPoint.SetChannelAttribute("Delay", StringValue("2ms"));

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

    Ipv4GlobalRoutingHelper::PopulateRoutingTables();

    // Simulator::Stop(Seconds(10.0));

    uint16_t port = 50000;
    uint32_t maxBytes = 1000;

    // BulkSendHelper source("ns3::TcpSocketFactory",
    //                       InetSocketAddress(staInterfaces.GetAddress(nWifi - 1), port));
    // // Set the amount of data to send in bytes.  Zero is unlimited.
    // source.SetAttribute("MaxBytes", UintegerValue(maxBytes));
    // ApplicationContainer sourceApps = source.Install(csmaNodes.Get(nCsma));
    // sourceApps.Start(Seconds(0.0));
    // sourceApps.Stop(Seconds(10.0));

    // //
    // // Create a PacketSinkApplication and install it on node 1
    // //
    // PacketSinkHelper sink("ns3::TcpSocketFactory",
    //                       InetSocketAddress(Ipv4Address::GetAny(), port));
    // ApplicationContainer sinkApps = sink.Install(wifiStaNodes.Get(nWifi - 1));
    // sinkApps.Start(Seconds(0.0));
    // sinkApps.Stop(Seconds(10.0));

    for (uint16_t i = 0; i < nCsma; i++)
    {
        BulkSendHelper source("ns3::TcpSocketFactory",
                              InetSocketAddress(csmaInterfaces.GetAddress(i), port));
        // Set the amount of data to send in bytes.  Zero is unlimited.
        source.SetAttribute("MaxBytes", UintegerValue(maxBytes));
        ApplicationContainer sourceApps = source.Install(wifiStaNodes.Get(i));
        sourceApps.Start(Seconds(0.0));
        sourceApps.Stop(Seconds(10.0));

        //
        // Create a PacketSinkApplication and install it on node 1
        //
        PacketSinkHelper sink("ns3::TcpSocketFactory",
                              InetSocketAddress(Ipv4Address::GetAny(), port));
        ApplicationContainer sinkApps = sink.Install(csmaNodes.Get(i));
        sinkApps.Start(Seconds(0.0));
        sinkApps.Stop(Seconds(10.0));
    }

    for (uint16_t i = 0; i < nCsma; i++)
    {
        BulkSendHelper source("ns3::TcpSocketFactory",
                              InetSocketAddress(staInterfaces.GetAddress(i), port));
        // Set the amount of data to send in bytes.  Zero is unlimited.
        source.SetAttribute("MaxBytes", UintegerValue(maxBytes));
        ApplicationContainer sourceApps = source.Install(csmaNodes.Get(i));
        sourceApps.Start(Seconds(0.0));
        sourceApps.Stop(Seconds(10.0));

        //
        // Create a PacketSinkApplication and install it on node 1
        //
        PacketSinkHelper sink("ns3::TcpSocketFactory",
                              InetSocketAddress(Ipv4Address::GetAny(), port));
        ApplicationContainer sinkApps = sink.Install(wifiStaNodes.Get(i));
        sinkApps.Start(Seconds(0.0));
        sinkApps.Stop(Seconds(10.0));
    }

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

    Simulator::Schedule(Seconds(0 + 0.000001), &TraceThroughput, monitor);

    Simulator::Stop(Seconds(10.0));
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
