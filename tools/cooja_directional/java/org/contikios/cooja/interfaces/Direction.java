/*
 * Copyright (c) 2015, Vishwesh Rege.
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

package org.contikios.cooja.interfaces;

import org.contikios.cooja.*;
import org.contikios.cooja.contikimote.ContikiMoteInterface;
import org.contikios.cooja.interfaces.Position;
import org.contikios.cooja.interfaces.PolledAfterActiveTicks;
import org.contikios.cooja.interfaces.MoteID;
import org.contikios.cooja.interfaces.Clock;
import org.contikios.cooja.mote.memory.VarMemory;

//import java.lang.math;
import java.text.NumberFormat;
import java.util.*;
import javax.swing.*;
import org.apache.log4j.Logger;
import org.jdom.Element;

import org.contikios.cooja.*;

import java.io.*;
import java.util.*;
import java.lang.Double;

/**
 * Antenna direction.
 *
 * <p>
 * This observable notifies when the direction is changed.
 *
 * @author Vishwesh Rege
 */
@ClassDescription("Direction")
public class Direction extends MoteInterface implements PolledAfterActiveTicks {
  private static Logger logger = Logger.getLogger(Direction.class);
  private Mote mote = null;
  private VarMemory moteMem = null;

  private double orientationDegrees;
  private double beamwidthDegrees;
  private double xcoordinate;
  private double ycoordinate;
  private boolean omni; 
  

  public Direction(Mote mote) {
    this.mote = mote;
    this.moteMem = new VarMemory(mote.getMemory());
    orientationDegrees = 42;
    beamwidthDegrees = 60;
    xcoordinate = 0.0;
    ycoordinate = 0.0;
    omni = false; 
    System.out.println("Direction initialized with ori: "+orientationDegrees);
  }

  public static String[] getCoreInterfaceDependencies() {
    return new String[]{"dir_interface"};
  }

  public void setAntennaType (int type) {
    if(type == 0) {
	    omni = true;
	    orientationDegrees = -1.0;
    }
    else {
	    omni = false;
    }

    System.out.println("Changed Antenna type to: "+omni);
    this.setChanged();
    this.notifyObservers(mote);
  }

  public boolean getAntennaType () {
	return this.omni;
  }

  public void setOrientation (double orientationDegrees) {
    System.out.println("Changed Ori to: "+orientationDegrees);
    orientationDegrees = orientationDegrees;
    this.setChanged();
    this.notifyObservers(mote);
  }


  public double getOrientation () {
          
	if(this.omni==true)
		return -1.0;
	else
		return orientationDegrees;
  }
  
  public double getBeamWidth (){
    return beamwidthDegrees;
  }


 
 public double getGain (Position destPos) {
	Position sourcePos = this.mote.getInterfaces().getPosition();
	/*double beamwidthRadians = beamwidthDegrees*Math.PI/180;
	double cosin = Math.cos(beamwidthRadians/4);
	double logan = Math.log10(cosin);
	double exp = -3/(20*logan);
	double gain = Math.pow(Math.cos(getAngle(destPos)), exp);
	//double gain = Math.pow(Math.cos(getAngle(destPos)),2);	//Dipole
	*/
	//Reading from file to Hashmap
	HashMap<Double,Double> map = new HashMap<Double,Double>();
	String line = "";
	try
	{
	File fileRelative = new File("../java/org/contikios/cooja/interfaces/rad_pattern_RPA.txt");
	FileReader fr = new FileReader(fileRelative);
	BufferedReader br = new BufferedReader(fr);
		while((line=br.readLine()) != null)
		{
			String parts[ ] = line.split(",");
			map.put(Double.valueOf(parts[0]),Double.valueOf(parts[1]));
			//System.out.println("parts[0]:"+parts[0]);
			//System.out.println("parts[1]:"+parts[1]);
		}
			fr.close();
	}
	catch(IOException e)
	{
		e.printStackTrace();
	}
	double r = getAngle(destPos);
	//System.out.println("angle - ori:"+r);
	int deg = (int)(Math.round(r*180)/Math.PI);
	//System.out.println("ang -ori in deg:"+deg);
	
	double val = 0;
	if(deg>=0)
	{
		val = deg;
	}
	else
	{
	if(deg< 0 && deg >-180)
	{
		val = deg;
	}
	else if(deg<=-180)
	{
		val = 360+deg;
	}
	
	}
	//System.out.println("val:"+val);
	double gain=map.get(val);
    System.out.println("The Value mapped to deg:"+val+"is:"+ gain+" omni "+omni);
	
	if (omni) {
		return 1.0;
	}
	else {
		return gain;
	}
  }

  public double getAngle (Position destPos) {	// get angle of dest w.r.t mySelf
	Position sourcePos = this.mote.getInterfaces().getPosition();
	double srcxcord = sourcePos.getXCoordinate();
	//xcoordinate = srcxcord;
	double srcycord = sourcePos.getXCoordinate();
        ycoordinate = srcycord ;
	double x = destPos.getXCoordinate() - sourcePos.getXCoordinate();
	double y = destPos.getYCoordinate() - sourcePos.getYCoordinate();
	int nodeid = this.mote.getInterfaces().getMoteID().getMoteID();
	long clock = this.mote.getInterfaces().getClock().getTime();
	//System.out.println("Source nodeid:"+nodeid+"Xcoordinate:"+xcoordinate+"Ycoordinate:"+ycoordinate);
	//System.out.println("Current time:"+clock);
	//System.out.println("Source X co-ord:"+sourcePos.getXCoordinate()+"Source Y co-ord:"+sourcePos.getYCoordinate());
	//System.out.println("Dest X co-ord:"+destPos.getXCoordinate()+"Source Y co-ord:"+destPos.getYCoordinate());
	double angle = Math.atan2(y,x);	// returns angle between -PI/2 to PI/2???
	double res = angle - getOrientation()*Math.PI/180;
	//System.out.println("angle:"+angle+"angle-ori:"+res);
	return res;
  }
  

  public void doActionsAfterTick() {
  
    boolean changed;

    int nodeid = this.mote.getInterfaces().getMoteID().getMoteID();
   
    Position sourcePos = this.mote.getInterfaces().getPosition();
    double srcxcord = sourcePos.getXCoordinate();
    double srcycord = sourcePos.getYCoordinate();
	
    int newBwDegValue = moteMem.getIntValueOf("simBwDegValue"); 
    int newOriDegValue = moteMem.getIntValueOf("simOriDegValue");
    this.omni = (moteMem.getIntValueOf("antennaType") == 0);
    
    moteMem.setIntValueOf("xcor",(int) srcxcord);
    int newxcoordinate = moteMem.getIntValueOf("xcor");
    //System.out.println("xcoordinate:"+newxcoordinate);
    moteMem.setIntValueOf("ycor",(int) srcycord);
    int newycoordinate = moteMem.getIntValueOf("ycor");
    //System.out.println("ycoordinate:"+newycoordinate);
    
    //System.out.println("Source nodeid:"+nodeid+"	Xcoordinate:"+newxcoordinate+"	Ycoordinate:"+newycoordinate);
	
    if (newBwDegValue != (int)this.beamwidthDegrees && newOriDegValue != (int)this.orientationDegrees) {
      changed = true;
    } else {
      changed = false;
    }
	//if (changed) { (should be enabled to use ori-set func especially used for beam/sector switching)
    //if (changed) {
    this.beamwidthDegrees = (double) newBwDegValue;
    this.orientationDegrees = (double) newOriDegValue;
    this.xcoordinate = (double) newxcoordinate;
    //System.out.println("xcoordinate:"+xcoordinate);
    this.ycoordinate = (double) newycoordinate;
    this.setChanged();
    this.notifyObservers(mote);
    //}
	//} (should be enabled to use ori-set func especially used for beam/sector switching)
  }

  public JPanel getInterfaceVisualizer() {
    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
    final NumberFormat form = NumberFormat.getNumberInstance();

    final JLabel directionLabel = new JLabel();
    directionLabel.setText("phi=" + form.format(getOrientation()));

    panel.add(directionLabel);

    Observer observer;
    this.addObserver(observer = new Observer() {
      public void update(Observable obs, Object obj) {
        directionLabel.setText("phi=" + form.format(getOrientation()));
      }
    });

    // Saving observer reference for releaseInterfaceVisualizer
    panel.putClientProperty("intf_obs", observer);

    return panel;
  }

  public void releaseInterfaceVisualizer(JPanel panel) {
    Observer observer = (Observer) panel.getClientProperty("intf_obs");
    if (observer == null) {
      logger.fatal("Error when releasing panel, observer is null");
      return;
    }

    this.deleteObserver(observer);
  }

  public Collection<Element> getConfigXML() {
    Vector<Element> config = new Vector<Element>();
    Element element;

    // X coordinate
    element = new Element("orientationDegrees");
    element.setText(Double.toString(getOrientation()));
    config.add(element);

    return config;
  }

  public void setConfigXML(Collection<Element> configXML, boolean visAvailable) {
    double orientationDegrees = 0;

    for (Element element : configXML) {
      if (element.getName().equals("orientationDegrees")) {
        orientationDegrees = Double.parseDouble(element.getText());
      }
    }

    setOrientation(orientationDegrees);
  }

  public String toString() {
    return "Mote interface : Direction";
  }

}




