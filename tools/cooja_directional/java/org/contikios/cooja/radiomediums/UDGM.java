/*
 * Copyright (c) 2009, Swedish Institute of Computer Science.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the Institute nor the names of its contributors
 *    may be used to endorse or promote products derived from this software
 *    without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE INSTITUTE AND CONTRIBUTORS ``AS IS'' AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED.  IN NO EVENT SHALL THE INSTITUTE OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 *
 */

package org.contikios.cooja.radiomediums;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Observable;
import java.util.Observer;
import java.util.Random;

import org.apache.log4j.Logger;
import org.jdom.Element;

import org.contikios.cooja.ClassDescription;
import org.contikios.cooja.Mote;
import org.contikios.cooja.MoteInterface;
import org.contikios.cooja.RadioConnection;
import org.contikios.cooja.SimEventCentral.MoteCountListener;
import org.contikios.cooja.Simulation;
import org.contikios.cooja.interfaces.MoteID;
import org.contikios.cooja.interfaces.Position;
import org.contikios.cooja.interfaces.Direction;
import org.contikios.cooja.interfaces.Radio;
import org.contikios.cooja.plugins.Visualizer;
import org.contikios.cooja.plugins.skins.UDGMVisualizerSkin;

import java.io.*;
import java.util.*;
import java.lang.Double;


/**
 * The Unit Disk Graph Radio Medium abstracts radio transmission range as circles.
 * 
 * It uses two different range parameters: one for transmissions, and one for
 * interfering with other radios and transmissions.
 * 
 * Both radio ranges grow with the radio output power indicator.
 * The range parameters are multiplied with [output power]/[maximum output power].
 * For example, if the transmission range is 100m, the current power indicator 
 * is 50, and the maximum output power indicator is 100, then the resulting transmission 
 * range becomes 50m.
 * 
 * For radio transmissions within range, two different success ratios are used [0.0-1.0]:
 * one for successful transmissions, and one for successful receptions.
 * If the transmission fails, no radio will hear the transmission.
 * If one of receptions fail, only that receiving radio will not receive the transmission,
 * but will be interfered throughout the entire radio connection.  
 * 
 * The received radio packet signal strength grows inversely with the distance to the
 * transmitter.
 *
 * @see #SS_STRONG
 * @see #SS_WEAK
 * @see #SS_NOTHING
 *
 * @see DirectedGraphMedium
 * @see UDGMVisualizerSkin
 * @author Fredrik Osterlind
 */
@ClassDescription("Unit Disk Graph Medium (UDGM): Distance Loss")
public class UDGM extends AbstractRadioMedium {
  private static Logger logger = Logger.getLogger(UDGM.class);

  public double SUCCESS_RATIO_TX = 1.0; /* Success ratio of TX. If this fails, no radios receive the packet */
  public double SUCCESS_RATIO_RX = 1.0; /* Success ratio of RX. If this fails, the single affected receiver does not receive the packet */
  public double TRANSMITTING_RANGE = 50; /* Transmission range. */
  public double INTERFERENCE_RANGE = 100; /* Interference range. Ignored if below transmission range. */
  public double RX_SENS = -95;
  public double INTF_TH = -100;
  private double freq = 2.4;
  private double c_speed = 299792458;

  private DirectedGraphMedium dgrm; /* Used only for efficient destination lookup */

  private Random random = null;
  int nodeid = 0;
  long clock = 0;
  private Mote mote = null;
  //double s_ori = 0;

  public UDGM(Simulation simulation) {
    super(simulation);
    random = simulation.getRandomGenerator();
    dgrm = new DirectedGraphMedium() {
      protected void analyzeEdges() {
        /* Create edges according to distances.
         * XXX May be slow for mobile networks */
        clearEdges();
        for (Radio source: UDGM.this.getRegisteredRadios()) {
          Position sourcePos = source.getPosition();
	  Direction sourceDir = source.getDirection();
          for (Radio dest: UDGM.this.getRegisteredRadios()) {
            Position destPos = dest.getPosition();
            Direction destDir = dest.getDirection();
            /* Ignore ourselves */
            if (source == dest) {
              continue;
            }
            double distance = sourcePos.getDistanceTo(destPos);		// Add direction as parameter along with distance
            if (distance < Math.max(TRANSMITTING_RANGE, INTERFERENCE_RANGE)) {	//Add Antenna effects here
              /* Add potential destination */
              addEdge(
                  new DirectedGraphMedium.Edge(source, 
                      new DGRMDestinationRadio(dest)));
            }
          }
        }
        super.analyzeEdges();
      }
    };

    /* Register as position observer.
     * If any positions change, re-analyze potential receivers. */
    final Observer positionObserver = new Observer() {
      public void update(Observable o, Object arg) {
        dgrm.requestEdgeAnalysis();
      }
    };
    /* Re-analyze potential receivers if radios are added/removed. */
    simulation.getEventCentral().addMoteCountListener(new MoteCountListener() {
      public void moteWasAdded(Mote mote) {
	UDGM.this.mote = mote;
        UDGM.this.mote.getInterfaces().getPosition().addObserver(positionObserver);
  	nodeid = UDGM.this.mote.getInterfaces().getMoteID().getMoteID();
	clock = UDGM.this.mote.getInterfaces().getClock().getTime();
        dgrm.requestEdgeAnalysis();
      }
      public void moteWasRemoved(Mote mote) {
        mote.getInterfaces().getPosition().deleteObserver(positionObserver);
        dgrm.requestEdgeAnalysis();
      }
    });
    for (Mote mote: simulation.getMotes()) {
      mote.getInterfaces().getPosition().addObserver(positionObserver);
    }
    dgrm.requestEdgeAnalysis();

    /* Register visualizer skin */
    Visualizer.registerVisualizerSkin(UDGMVisualizerSkin.class);
  }

  public void removed() {
  	super.removed();
  	
		Visualizer.unregisterVisualizerSkin(UDGMVisualizerSkin.class);
  }
  
  public void setTxRange(double r) {
    TRANSMITTING_RANGE = r;
    dgrm.requestEdgeAnalysis();
  }

  public void setInterferenceRange(double r) {
    INTERFERENCE_RANGE = r;
    dgrm.requestEdgeAnalysis();
  }

  public RadioConnection createConnections(Radio sender) {
    RadioConnection newConnection = new RadioConnection(sender);

    /* Fail radio transmission randomly - no radios will hear this transmission */
    if (getTxSuccessProbability(sender) < 1.0 && random.nextDouble() > getTxSuccessProbability(sender)) {
      return newConnection;
    }

    /* Calculate ranges: grows with radio output power */
    double moteTransmissionRange = TRANSMITTING_RANGE
    * ((double) sender.getCurrentOutputPowerIndicator() / (double) sender.getOutputPowerIndicatorMax());
    double moteInterferenceRange = INTERFERENCE_RANGE
    * ((double) sender.getCurrentOutputPowerIndicator() / (double) sender.getOutputPowerIndicatorMax());

    /* Get all potential destination radios */
    DestinationRadio[] potentialDestinations = dgrm.getPotentialDestinations(sender);
    if (potentialDestinations == null) {
      return newConnection;
    }

    /* Loop through all potential destinations */
    Position senderPos = sender.getPosition();
    for (DestinationRadio dest: potentialDestinations) {
      Radio recv = dest.radio;

      /* Fail if radios are on different (but configured) channels */ 
      if (sender.getChannel() >= 0 &&
          recv.getChannel() >= 0 &&
          sender.getChannel() != recv.getChannel()) {

        /* Add the connection in a dormant state;
           it will be activated later when the radio will be
           turned on and switched to the right channel. This behavior
           is consistent with the case when receiver is turned off. */
        newConnection.addInterfered(recv);

        continue;
      }
      Position recvPos = recv.getPosition();

      /* Fail if radio is turned off */
//      if (!recv.isReceiverOn()) {
//        /* Special case: allow connection if source is Contiki radio, 
//         * and destination is something else (byte radio).
//         * Allows cross-level communication with power-saving MACs. */
//        if (sender instanceof ContikiRadio &&
//            !(recv instanceof ContikiRadio)) {
//          /*logger.info("Special case: creating connection to turned off radio");*/
//        } else {
//          recv.interfereAnyReception();
//          continue;
//        }
//      }
	
	double PL = 0.0;
      	double distance = senderPos.getDistanceTo(recvPos);
	double Ptx = sender.getCurrentOutputPower();
	double Gtx = 20*Math.log10(sender.getDirection().getGain(recv.getPosition()));
	double Grx = 20*Math.log10(recv.getDirection().getGain(sender.getPosition()));
	
	 PL = 20*Math.log10(distance) + 20*Math.log10(freq) + 180 + 20*Math.log10(4*Math.PI/c_speed);
	
        double signalStrength = Ptx + Gtx + Grx - PL;
      if (signalStrength >= RX_SENS) {	//(distance <= moteTransmissionRange) //Add antenna effects here
        /* Within transmission range */

        if (!recv.isRadioOn()) {	//In case radio turned ON later(?), interferes reception
          newConnection.addInterfered(recv);
          recv.interfereAnyReception();
        } else if (recv.isInterfered()) {
          /* Was interfered: keep interfering */
          newConnection.addInterfered(recv);
        } else if (recv.isTransmitting()) {
          newConnection.addInterfered(recv);
        } else if (recv.isReceiving()) { // || (random.nextDouble() > getRxSuccessProbability(sender, recv))) {	//Randomly interfere(add to intf), i.e. randomly fail tx/rx
          /* Was receiving, or reception failed: start interfering */
          newConnection.addInterfered(recv);
          recv.interfereAnyReception();

          /* Interfere receiver in all other active radio connections */
          for (RadioConnection conn : getActiveConnections()) {
            if (conn.isDestination(recv)) {	// v/s recv.isReceiving()??? What is active radio connection?
              conn.addInterfered(recv);
            }
          }

        } else {
          /* Success: radio starts receiving */
          newConnection.addDestination(recv);
        }
      } else if (signalStrength >= INTF_TH && signalStrength < RX_SENS) {	//Add antenna effects here
        /* Within interference range */
        newConnection.addInterfered(recv);
        recv.interfereAnyReception();
      }
    }

    return newConnection;
  }
  
  public double getSuccessProbability(Radio source, Radio dest) {
  	return getTxSuccessProbability(source) * getRxSuccessProbability(source, dest);
  }
  public double getTxSuccessProbability(Radio source) {
    return SUCCESS_RATIO_TX;
  }
  public double getRxSuccessProbability(Radio source, Radio dest) {	//Add antenna effects here
  	double distance = source.getPosition().getDistanceTo(dest.getPosition());
    double distanceSquared = Math.pow(distance,2.0);
    double distanceMax = TRANSMITTING_RANGE * 
    ((double) source.getCurrentOutputPowerIndicator() / (double) source.getOutputPowerIndicatorMax());
    if (distanceMax == 0.0) {
      return 0.0;
    }
    double distanceMaxSquared = Math.pow(distanceMax,2.0);
    double ratio = distanceSquared / distanceMaxSquared;
    if (ratio > 1.0) {
    	return 0.0;
    }
    return 1.0 - ratio*(1.0-SUCCESS_RATIO_RX);
  }

  public void updateSignalStrengths() {
    /* Override: uses distance as signal strength factor */
    
    /* Reset signal strengths */
    for (Radio radio : getRegisteredRadios()) {
      radio.setCurrentSignalStrength(getBaseRssi(radio));
    }

    /* Set signal strength to below strong on destinations */
    RadioConnection[] conns = getActiveConnections();
    for (RadioConnection conn : conns) {
      if (conn.getSource().getCurrentSignalStrength() < SS_STRONG) {	//Source Signal Strength???
        conn.getSource().setCurrentSignalStrength(SS_STRONG);
      }
      for (Radio dstRadio : conn.getDestinations()) {
        if (conn.getSource().getChannel() >= 0 &&
            dstRadio.getChannel() >= 0 &&
            conn.getSource().getChannel() != dstRadio.getChannel()) {
          continue;
        }
	
	double PL =0.0;
        double dist = conn.getSource().getPosition().getDistanceTo(dstRadio.getPosition());
	double xk = conn.getSource().getPosition().getXCoordinate(); //src X co-ordinate
	double yk = conn.getSource().getPosition().getYCoordinate(); //src Y co-ordinate
	double xd = dstRadio.getPosition().getXCoordinate(); //Actual dest X co-ordinate
	double yd = dstRadio.getPosition().getYCoordinate(); //Actual dest Y co-ordinate 
	double sender_orientation = conn.getSource().getDirection().getOrientation(); //src node orientation 
	double recv_orientation = dstRadio.getDirection().getOrientation(); //dst node orientation

        double maxTxDist = TRANSMITTING_RANGE
        * ((double) conn.getSource().getCurrentOutputPowerIndicator() / (double) conn.getSource().getOutputPowerIndicatorMax());
        double distFactor = dist/maxTxDist;	// dst Radio dist : maxTxDist

	double Ptx = conn.getSource().getCurrentOutputPower();
	double Gtx = 20*Math.log10(conn.getSource().getDirection().getGain(dstRadio.getPosition()));
	double Grx = 20*Math.log10(dstRadio.getDirection().getGain(conn.getSource().getPosition()));
	if(dist >= TRANSMITTING_RANGE)
	{
	 PL = 20*Math.log10(dist)*distFactor + 20*Math.log10(freq) + 155 + 20*Math.log10(4*Math.PI/c_speed)+0.37*dist;
	}
	else
	{
	 PL = 20*Math.log10(dist)*distFactor + 20*Math.log10(freq) + 155 + 20*Math.log10(4*Math.PI/c_speed)+0.22*dist; //2.4GHz: 20*log(2.4) + 20*log(10^9)
	}	 
	//205 highest PL factor for -95dBm & 155 lowest PL factor for -10dBm
        double signalStrength = Ptx + Gtx + Grx - PL;		

	double ori_radians = Math.toRadians(recv_orientation); //convert degree to radians to be passed as argument for cos & sin functions

	
        if (dstRadio.getCurrentSignalStrength() < signalStrength) {
          dstRadio.setCurrentSignalStrength(signalStrength);
        }
      }
    }

    /* Set signal strength to below weak on interfered */
    for (RadioConnection conn : conns) {	//conns = getActiveConnections();
      for (Radio intfRadio : conn.getInterfered()) {
        if (conn.getSource().getChannel() >= 0 &&
            intfRadio.getChannel() >= 0 &&
            conn.getSource().getChannel() != intfRadio.getChannel()) {
          continue;
        }
	double PL = 0.0;
        double dist = conn.getSource().getPosition().getDistanceTo(intfRadio.getPosition());

        double maxTxDist = TRANSMITTING_RANGE
        * ((double) conn.getSource().getCurrentOutputPowerIndicator() / (double) conn.getSource().getOutputPowerIndicatorMax());
        double distFactor = dist/maxTxDist;	//intf radio dist : maxTxDist

	double Ptx = conn.getSource().getCurrentOutputPower();
	double Gtx = 20*Math.log10(conn.getSource().getDirection().getGain(intfRadio.getPosition()));	//intfRadio instead of dstRadio
	double Grx = 20*Math.log10(intfRadio.getDirection().getGain(conn.getSource().getPosition()));	//intfRadio instead of dstRadio
	if(dist >= TRANSMITTING_RANGE)
	{
	 PL = 20*Math.log10(dist)*distFactor + 20*Math.log10(freq) + 155 + 20*Math.log10(4*Math.PI/c_speed)+0.37*dist;
	}
	else
	{
	 PL = 20*Math.log10(dist)*distFactor + 20*Math.log10(freq) + 155 + 20*Math.log10(4*Math.PI/c_speed)+0.22*dist; //2.4GHz: 20*log(2.4) + 20*log(10^9)
	}	
	
	// PL = 20*Math.log10(dist)*distFactor + 20*Math.log10(freq) + 155 + 20*Math.log10(4*Math.PI/c_speed)+0.22*dist; //2.4GHz: 20*log(2.4) + 20*log(10^9)
        double signalStrength = Ptx + Gtx + Grx - PL; //SS_STRONG + distFactor*(SS_WEAK - SS_STRONG);	//Add antenna effects here

        //if (distFactor < 1) {	// intf radio within Tx range
	//signalStrength defined here initially
          //if (intfRadio.getCurrentSignalStrength() < signalStrength) {
            //intfRadio.setCurrentSignalStrength(signalStrength);	//But intfRadio can't receive???
          //}
        //} else {
          intfRadio.setCurrentSignalStrength(SS_WEAK);	//signalStrength always WEAK beyond maxTxDist for intfRadio
          //if (intfRadio.getCurrentSignalStrength() < SS_WEAK) {	//--> signalStrength < SS_WEAK???
            //intfRadio.setCurrentSignalStrength(SS_WEAK);	//???
          //}
        //}

        if (!intfRadio.isInterfered()) {	// But intfRadio : conn.getInterfered()???
          /*logger.warn("Radio was not interfered: " + intfRadio);*/
          intfRadio.interfereAnyReception();
        }
      }
    }
  }

  public Collection<Element> getConfigXML() {
    Collection<Element> config = super.getConfigXML();
    Element element;

    /* Transmitting range */
    element = new Element("transmitting_range");
    element.setText(Double.toString(TRANSMITTING_RANGE));
    config.add(element);

    /* Interference range */
    element = new Element("interference_range");
    element.setText(Double.toString(INTERFERENCE_RANGE));
    config.add(element);

    /* Transmission success probability */
    element = new Element("success_ratio_tx");
    element.setText("" + SUCCESS_RATIO_TX);
    config.add(element);

    /* Reception success probability */
    element = new Element("success_ratio_rx");
    element.setText("" + SUCCESS_RATIO_RX);
    config.add(element);

    return config;
  }

  public boolean setConfigXML(Collection<Element> configXML, boolean visAvailable) {
    super.setConfigXML(configXML, visAvailable);
    for (Element element : configXML) {
      if (element.getName().equals("transmitting_range")) {
        TRANSMITTING_RANGE = Double.parseDouble(element.getText());
      }

      if (element.getName().equals("interference_range")) {
        INTERFERENCE_RANGE = Double.parseDouble(element.getText());
      }

      /* Backwards compatibility */
      if (element.getName().equals("success_ratio")) {
        SUCCESS_RATIO_TX = Double.parseDouble(element.getText());
        logger.warn("Loading old Cooja Config, XML element \"sucess_ratio\" parsed at \"sucess_ratio_tx\"");
      }

      if (element.getName().equals("success_ratio_tx")) {
        SUCCESS_RATIO_TX = Double.parseDouble(element.getText());
      }

      if (element.getName().equals("success_ratio_rx")) {
        SUCCESS_RATIO_RX = Double.parseDouble(element.getText());
      }
    }
    return true;
  }

}
