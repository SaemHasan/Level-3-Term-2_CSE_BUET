// Network topology
//
//       n0    n1  n2   n3  n4  n5
//       |     |   |    |   |   |
//       =====================
//        WSN (802.15.4)

//          n8 n7 n6 n5
//          |  |  |  |
//          *  *  *  *
//   WSN (802.15.4) 2001 : 1 ::
//          *  *  *  *
//          |  |  |  |
//          n1 n2 n3 n4
//
// - ICMPv6 echo request flows from n0 to n1 and back with ICMPv6 echo reply
// - DropTail queues
// - Tracing of queues and packet receptions to file "wsn-ping6.tr"
//
// This example is based on the "ping6.cc" example.

#include <fstream>
#include "ns3/core-module.h"
#include "ns3/internet-module.h"
#include "ns3/sixlowpan-module.h"
#include "ns3/lr-wpan-module.h"
#include "ns3/internet-apps-module.h"
#include "ns3/mobility-module.h"
#include "ns3/applications-module.h"
#include "ns3/flow-monitor-module.h"
#include "ns3/netanim-module.h"
#include "ns3/single-model-spectrum-channel.h"

using namespace ns3;

NS_LOG_COMPONENT_DEFINE("taskA2");

std::string TYPE, SPEED;
uint32_t totalFlows, NUMBEROFNODES, PACKETSPS;

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

  // std::ofstream flowout("taskA2-flow.dat", std::ios::out);

  monitor->CheckForLostPackets();
  Ptr<Ipv6FlowClassifier> classifier = DynamicCast<Ipv6FlowClassifier>(flowmon->GetClassifier6());
  FlowMonitor::FlowStatsContainer stats = monitor->GetFlowStats();
  for (std::map<FlowId, FlowMonitor::FlowStats>::const_iterator i = stats.begin(); i != stats.end(); ++i)
  {
    Ipv6FlowClassifier::FiveTuple t = classifier->FindFlow(i->first);

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

    // flowout << i->first << " " << calthroughput << std::endl;
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
  NS_LOG_UNCOND("Total End to End Delay : " << totalDelay.GetSeconds() / (ReceivedPackets * 1.0) << "s");
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

int main(int argc, char **argv)
{
  bool firstTime = false;
  uint32_t nNodes = 20; // number of nodes
  uint32_t nFlows = 10; // number of flows
  std::string nodeSpeed = "5.0";
  bool verbose = false;
  uint32_t packetSize = 512;
  uint32_t packetsPS = 100; // packets per second
  Time stopTime = Seconds(20.0);
  std::string type = "NODE";

  CommandLine cmd(__FILE__);
  cmd.AddValue("first", "run this type of variation first time", firstTime);
  cmd.AddValue("nNodes", "Number of wifi STA devices", nNodes);
  cmd.AddValue("nFlows", "Number of Flows", nFlows);
  cmd.AddValue("type", "parameter want to vary", type);
  cmd.AddValue("nodeSpeed", "Number of wifi STA devices", nodeSpeed);
  cmd.AddValue("packetsPerSec", "Number of packets per second", packetsPS);
  cmd.AddValue("verbose", "turn on log components", verbose);
  cmd.Parse(argc, argv);

  if (verbose)
  {
    //   LogComponentEnable ("Ping6WsnExample", LOG_LEVEL_INFO);
    LogComponentEnable("Ipv6EndPointDemux", LOG_LEVEL_ALL);
    LogComponentEnable("Ipv6L3Protocol", LOG_LEVEL_ALL);
    LogComponentEnable("Ipv6StaticRouting", LOG_LEVEL_ALL);
    LogComponentEnable("Ipv6ListRouting", LOG_LEVEL_ALL);
    LogComponentEnable("Ipv6Interface", LOG_LEVEL_ALL);
    LogComponentEnable("Icmpv6L4Protocol", LOG_LEVEL_ALL);
    //   LogComponentEnable ("Ping6Application", LOG_LEVEL_ALL);
    LogComponentEnable("NdiscCache", LOG_LEVEL_ALL);
    LogComponentEnable("SixLowPanNetDevice", LOG_LEVEL_ALL);
  }

  std::cout << "type: " << type << " Nodes : " << nNodes << " Flows : " << nFlows
            << " Speed : " << nodeSpeed << " packet per sec: " << packetsPS << std::endl;

  outputFileName = type + "_data.csv";
  totalFlows = nFlows;
  TYPE = type;
  NUMBEROFNODES = nNodes;
  PACKETSPS = packetsPS;
  SPEED = nodeSpeed;
  uint32_t dataRate = packetsPS * packetSize;

  NS_LOG_INFO("Create nodes.");
  NodeContainer nodes;
  nodes.Create(nNodes);

  // Set seed for random numbers
  SeedManager::SetSeed(167);

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

  mobility.Install(nodes);

  NS_LOG_INFO("Create channels.");
  LrWpanHelper lrWpanHelper;
  // Add and install the LrWpanNetDevice for each node
  // lrWpanHelper.EnableLogComponents();
  NetDeviceContainer devContainer = lrWpanHelper.Install(nodes);
  lrWpanHelper.AssociateToPan(devContainer, 10);

  std::cout << "Created " << devContainer.GetN() << " devices" << std::endl;
  std::cout << "There are " << nodes.GetN() << " nodes" << std::endl;

  /* Install IPv4/IPv6 stack */
  NS_LOG_INFO("Install Internet stack.");
  InternetStackHelper internetv6;
  internetv6.SetIpv4StackInstall(false);
  internetv6.Install(nodes);

  // Install 6LowPan layer
  NS_LOG_INFO("Install 6LoWPAN.");
  SixLowPanHelper sixlowpan;
  NetDeviceContainer six1 = sixlowpan.Install(devContainer);

  NS_LOG_INFO("Assign addresses.");
  Ipv6AddressHelper ipv6;
  ipv6.SetBase(Ipv6Address("2001:1::"), Ipv6Prefix(64));
  Ipv6InterfaceContainer interfaces = ipv6.Assign(six1);

  NS_LOG_INFO("Create Applications.");

  ApplicationContainer sinkApp;

  for (uint32_t i = 0; i < nNodes / 2; i++)
  {
    PacketSinkHelper sinkHelper("ns3::TcpSocketFactory", Inet6SocketAddress(Ipv6Address::GetAny(), 9));
    sinkApp.Add(sinkHelper.Install(nodes.Get(i)));
  }
  /* Install TCP/UDP Transmitter on the station */
  ApplicationContainer serverApp;
  uint32_t sinkNode = 0;
  uint32_t serverNode = nNodes / 2;
  for (uint32_t j = 0; j < nFlows / 2; j++)
  {
    OnOffHelper server("ns3::TcpSocketFactory", (Inet6SocketAddress(interfaces.GetAddress(sinkNode, 1), 9)));
    server.SetAttribute("PacketSize", UintegerValue(packetSize));
    server.SetAttribute("OnTime", StringValue("ns3::ConstantRandomVariable[Constant=1]"));
    server.SetAttribute("OffTime", StringValue("ns3::ConstantRandomVariable[Constant=0]"));
    server.SetAttribute("DataRate", DataRateValue(DataRate(dataRate)));
    serverApp.Add(server.Install(nodes.Get(serverNode)));
    serverNode = (serverNode + 1) % (nNodes / 2) + (nNodes / 2);
    sinkNode = (sinkNode + 1) % (nNodes / 2);
  }

  /* Start Applications */
  sinkApp.Start(Seconds(0.0));
  serverApp.Start(Seconds(1.0));
  sinkApp.Stop(stopTime + Seconds(5.0));
  serverApp.Stop(stopTime);

  dir = "taskA2_result/" + std::string("result") + "/";
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

  NS_LOG_INFO("Run Simulation.");
  Simulator::Stop(stopTime + Seconds(5.0));
  //   AnimationInterface anim ("taskxml2.xml");
  Simulator::Run();
  //   flowmon.SerializeToXmlFile("taskA2.flowmonitor", true, true);
  // Print per flow statistics
  printFlow(&flowmon, monitor);
  Simulator::Destroy();
  NS_LOG_INFO("Done.");
}
