/* -*- Mode:C++; c-file-style:"gnu"; indent-tabs-mode:nil; -*- */
/*
 * Copyright (c) 2013 ResiliNets, ITTC, University of Kansas
 *
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
 *
 * Authors: Siddharth Gangadhar <siddharth@ittc.ku.edu>,
 *          Truc Anh N. Nguyen <annguyen@ittc.ku.edu>,
 *          Greeshma Umapathi
 *
 * James P.G. Sterbenz <jpgs@ittc.ku.edu>, director
 * ResiliNets Research Group  http://wiki.ittc.ku.edu/resilinets
 * Information and Telecommunication Technology Center (ITTC)
 * and Department of Electrical Engineering and Computer Science
 * The University of Kansas Lawrence, KS USA.
 *
 * Work supported in part by NSF FIND (Future Internet Design) Program
 * under grant CNS-0626918 (Postmodern Internet Architecture),
 * NSF grant CNS-1050226 (Multilayer Network Resilience Analysis and Experimentation on GENI),
 * US Department of Defense (DoD), and ITTC at The University of Kansas.
 */

#include "tcp-westwood-br.h"
#include "ns3/log.h"
#include "ns3/simulator.h"
#include "rtt-estimator.h"
#include "tcp-socket-base.h"

NS_LOG_COMPONENT_DEFINE("TcpWestwoodBR");

namespace ns3
{

    NS_OBJECT_ENSURE_REGISTERED(TcpWestwoodBR);

    TypeId
    TcpWestwoodBR::GetTypeId(void)
    {
        static TypeId tid = TypeId("ns3::TcpWestwoodBR")
                                .SetParent<TcpNewReno>()
                                .SetGroupName("Internet")
                                .AddConstructor<TcpWestwoodBR>()
                                .AddAttribute("FilterType", "Use this to choose no filter or Tustin's approximation filter",
                                              EnumValue(TcpWestwoodBR::TUSTIN), MakeEnumAccessor(&TcpWestwoodBR::m_fType),
                                              MakeEnumChecker(TcpWestwoodBR::NONE, "None", TcpWestwoodBR::TUSTIN, "Tustin"))
                                .AddTraceSource("EstimatedBW", "The estimated bandwidth",
                                                MakeTraceSourceAccessor(&TcpWestwoodBR::m_currentBW),
                                                "ns3::TracedValueCallback::Double");
        return tid;
    }

    TcpWestwoodBR::TcpWestwoodBR(void) : TcpNewReno(),
                                         m_currentBW(0),
                                         m_lastSampleBW(0),
                                         m_lastBW(0),
                                         m_ackedSegments(0),
                                         m_IsCount(false),
                                         m_lastAck(0),
                                         control_P(1.0),
                                         Level(1)
    {
        NS_LOG_FUNCTION(this);
    }

    TcpWestwoodBR::TcpWestwoodBR(const TcpWestwoodBR &sock) : TcpNewReno(sock),
                                                              m_currentBW(sock.m_currentBW),
                                                              m_lastSampleBW(sock.m_lastSampleBW),
                                                              m_lastBW(sock.m_lastBW),
                                                              // m_pType(sock.m_pType),
                                                              m_fType(sock.m_fType),
                                                              m_IsCount(sock.m_IsCount),
                                                              control_P(sock.control_P),
                                                              Level(sock.Level)
    {
        NS_LOG_FUNCTION(this);
        NS_LOG_LOGIC("Invoked the copy constructor");
    }

    TcpWestwoodBR::~TcpWestwoodBR(void)
    {
    }

    void
    TcpWestwoodBR::PktsAcked(Ptr<TcpSocketState> tcb, uint32_t packetsAcked,
                             const Time &rtt)
    {
        NS_LOG_FUNCTION(this << tcb << packetsAcked << rtt);

        if (rtt.IsZero())
        {
            NS_LOG_WARN("RTT measured is zero!");
            return;
        }

        m_ackedSegments += packetsAcked;

        CongestionLevel(rtt, tcb);

        EstimateBW(rtt, tcb);
    }

    void
    TcpWestwoodBR::EstimateBW(const Time &rtt, Ptr<TcpSocketState> tcb)
    {
        NS_LOG_FUNCTION(this);

        NS_ASSERT(!rtt.IsZero());

        m_currentBW = m_ackedSegments * tcb->m_segmentSize / rtt.GetSeconds();

        Time currentAck = Simulator::Now();

        m_currentBW = m_ackedSegments * tcb->m_segmentSize / (currentAck - m_lastAck).GetSeconds();

        // std::cout << "diff : " << (currentAck - m_lastAck).GetSeconds() << std::endl;

        m_lastAck = currentAck;

        m_ackedSegments = 0;
        NS_LOG_LOGIC("Estimated BW: " << m_currentBW);
        // std::cout << "in westwood br : Estimated BW: " << m_currentBW << "\t Max Rtt : " << tcb->m_maxRtt << "\t Min Rtt : " << tcb->m_minRtt << std::endl;

        // Filter the BW sample

        double alpha = 0.9;

        if (m_fType == TcpWestwoodBR::NONE)
        {
        }
        else if (m_fType == TcpWestwoodBR::TUSTIN)
        {
            double sample_bwe = m_currentBW;
            m_currentBW = (alpha * m_lastBW) + ((1 - alpha) * ((sample_bwe + m_lastSampleBW) / 2));
            m_lastSampleBW = sample_bwe;
            m_lastBW = m_currentBW;
        }

        NS_LOG_LOGIC("Estimated BW after filtering: " << m_currentBW);
    }

    uint32_t
    TcpWestwoodBR::GetSsThresh(Ptr<const TcpSocketState> tcb,
                               uint32_t bytesInFlight)
    {
        NS_UNUSED(bytesInFlight);
        NS_LOG_LOGIC("CurrentBW: " << m_currentBW << " minRtt: " << tcb->m_minRtt << " ssthresh: " << m_currentBW * static_cast<double>(tcb->m_minRtt.GetSeconds()));

        return std::max(2 * tcb->m_segmentSize,
                        uint32_t(m_currentBW * static_cast<double>(tcb->m_minRtt.GetSeconds())));
    }

    Ptr<TcpCongestionOps>
    TcpWestwoodBR::Fork()
    {
        return CreateObject<TcpWestwoodBR>(*this);
    }

    // new added
    void
    TcpWestwoodBR::UpdateWindow(Ptr<TcpSocketState> tcb)
    {
        // std::cout << "here in TcpWestwoodBR::Update window\n\n";
        if (tcb->m_inDUPACK)
        {
            if (tcb->m_DUPACKCOUNT == 2 && (Level == 3 || Level == 4))
            {
                tcb->m_cWnd = tcb->m_cWnd * control_P;
                if (tcb->m_cWnd > tcb->m_ssThresh)
                    tcb->m_cWnd = tcb->m_ssThresh;
            }

            if ((tcb->m_DUPACKCOUNT == 3) && Level > 2)
            {
                tcb->m_cWnd = tcb->m_cWnd * control_P;
                if (tcb->m_cWnd > tcb->m_ssThresh)
                    tcb->m_cWnd = tcb->m_ssThresh;
            }
        }
    }

    void
    TcpWestwoodBR::IncreaseWindow(Ptr<TcpSocketState> tcb, uint32_t segmentsAcked)
    {
        // std::cout << "tcpw-br here in increasewindow. segack : " << segmentsAcked << std::endl;

        NS_LOG_FUNCTION(this << tcb << segmentsAcked);

        if (tcb->m_cWnd < tcb->m_ssThresh)
        {
            segmentsAcked = SlowStart(tcb, segmentsAcked);
        }

        if (tcb->m_cWnd >= tcb->m_ssThresh)
        {
            CongestionAvoidance(tcb, segmentsAcked);
        }

        // new add
        if (tcb->m_DUPACKCOUNT > 0)
        {
            // std::cout << "tcpw-br level>2. cwnd : " << tcb->m_cWnd << std::endl;
            tcb->m_cWnd = tcb->m_cWnd * control_P;
            // std::cout << "changed to cwnd : " << tcb->m_cWnd << std::endl;
        }
    }

    uint32_t
    TcpWestwoodBR::SlowStart(Ptr<TcpSocketState> tcb, uint32_t segmentsAcked)
    {
        NS_LOG_FUNCTION(this << tcb << segmentsAcked);

        if (segmentsAcked >= 1)
        {
            // std::cout << " tcpw-br here in slow start\n";
            tcb->m_cWnd += tcb->m_segmentSize;
            NS_LOG_INFO("In SlowStart, updated to cwnd " << tcb->m_cWnd << " ssthresh " << tcb->m_ssThresh);
            return segmentsAcked - 1;
        }

        return 0;
    }

    void
    TcpWestwoodBR::CongestionAvoidance(Ptr<TcpSocketState> tcb, uint32_t segmentsAcked)
    {
        NS_LOG_FUNCTION(this << tcb << segmentsAcked);

        if (segmentsAcked > 0)
        {
            // std::cout << "tcpw-br here in congestion avoidance\n";
            double adder = static_cast<double>(tcb->m_segmentSize * tcb->m_segmentSize) / tcb->m_cWnd.Get();

            // new add
            if (Level == 1 || Level == 2)
                adder = adder * control_P;

            adder = std::max(1.0, adder);
            tcb->m_cWnd += static_cast<uint32_t>(adder);
            NS_LOG_INFO("In CongAvoid, updated to cwnd " << tcb->m_cWnd << " ssthresh " << tcb->m_ssThresh);
        }
    }

    std::string
    TcpWestwoodBR::GetName() const
    {
        return "TcpWestwoodBR";
    }

    int
    TcpWestwoodBR::CongestionLevel(const Time &rtt, Ptr<TcpSocketState> tcb)
    {
        // std::cout << "rtt : " << rtt << "max : " << tcb->m_maxRtt << " min : " << tcb->m_minRtt << std::endl;
        double F = (tcb->m_maxRtt - tcb->m_minRtt).GetMilliSeconds();

        double d = (rtt - tcb->m_minRtt).GetMilliSeconds();

        double R = (d * 1.0) / F;

        // std::cout << "F :" << F << " d : " << d << std::endl;
        // std::cout << "\ntcpw-br R : " << R << std::endl;

        if (R >= 0 && R <= 0.25)
        {
            Level = 1;
            control_P = 1;
        }
        else if (R <= 0.5)
        {
            Level = 2;
            control_P = 0.867;
        }
        else if (R <= 0.75)
        {
            Level = 3;
            control_P = 0.5;
        }
        else
        {
            Level = 4;
            control_P = 0.4;
        }

        // std::cout << "\nP : " << control_P << " Level : " << Level << std::endl;

        return Level;
    }

} // namespace ns3
