/*
 * Provided By Instructor For: CS 4480 Computer Networks 
 * 							   Programming Assignment: 2
 * 							   Reliable Transport Protocol
 * Modified By: xrawlinson
 * Last Modified Date: 3/27/2016
 * Class: StudentNetworkSimulator
 * Modifications: 
 * 		1. added needed variables;
 * 		2. added needed functions: computeChecksum, sameChecksum, and displayStats;
 * 		3. filled the give functions.
 * Note: 
 * 		1. this one is modified for Part A - The Alternating-Bit-Protocol Version;
 * 		2. also added two lines of code in the class "NetworkSimulator" 
 * 		   to display statistics as the following: 
 * 		   		Line 45: protected abstract void displayStats();
 * 				Line 170: displayStats();
 */

public class StudentNetworkSimulator extends NetworkSimulator
{
    /*
     * Predefined Constants (static member variables):
     *
     *   int MAXDATASIZE : the maximum size of the Message data and
     *                     Packet payload
     *
     *   int A           : a predefined integer that represents entity A
     *   int B           : a predefined integer that represents entity B
     *
     *
     * Predefined Member Methods:
     *
     *  void stopTimer(int entity): 
     *       Stops the timer running at "entity" [A or B]
     *  void startTimer(int entity, double increment): 
     *       Starts a timer running at "entity" [A or B], which will expire in
     *       "increment" time units, causing the interrupt handler to be
     *       called.  You should only call this with A.
     *  void toLayer3(int callingEntity, Packet p)
     *       Puts the packet "p" into the network from "callingEntity" [A or B]
     *  void toLayer5(int entity, String dataSent)
     *       Passes "dataSent" up to layer 5 from "entity" [A or B]
     *  double getTime()
     *       Returns the current time in the simulator.  Might be useful for
     *       debugging.
     *  void printEventList()
     *       Prints the current event list to stdout.  Might be useful for
     *       debugging, but probably not.
     *
     *
     *  Predefined Classes:
     *
     *  Message: Used to encapsulate a message coming from layer 5
     *    Constructor:
     *      Message(String inputData): 
     *          creates a new Message containing "inputData"
     *    Methods:
     *      boolean setData(String inputData):
     *          sets an existing Message's data to "inputData"
     *          returns true on success, false otherwise
     *      String getData():
     *          returns the data contained in the message
     *  Packet: Used to encapsulate a packet
     *    Constructors:
     *      Packet (Packet p):
     *          creates a new Packet that is a copy of "p"
     *      Packet (int seq, int ack, int check, String newPayload)
     *          creates a new Packet with a sequence field of "seq", an
     *          ack field of "ack", a checksum field of "check", and a
     *          payload of "newPayload"
     *      Packet (int seq, int ack, int check)
     *          chreate a new Packet with a sequence field of "seq", an
     *          ack field of "ack", a checksum field of "check", and
     *          an empty payload
     *    Methods:
     *      boolean setSeqnum(int n)
     *          sets the Packet's sequence field to "n"
     *          returns true on success, false otherwise
     *      boolean setAcknum(int n)
     *          sets the Packet's ack field to "n"
     *          returns true on success, false otherwise
     *      boolean setChecksum(int n)
     *          sets the Packet's checksum to "n"
     *          returns true on success, false otherwise
     *      boolean setPayload(String newPayload)
     *          sets the Packet's payload to "newPayload"
     *          returns true on success, false otherwise
     *      int getSeqnum()
     *          returns the contents of the Packet's sequence field
     *      int getAcknum()
     *          returns the contents of the Packet's ack field
     *      int getChecksum()
     *          returns the checksum of the Packet
     *      int getPayload()
     *          returns the Packet's payload
     *
     */

	
/**************************************START OF NEEDED VARIABLES*****************************************************/
    // Add any necessary class variables here.  Remember, you cannot use
    // these variables to send messages error free!  They can only hold
    // state information for A or B.
    // Also add any necessary methods (e.g. checksum of a String)

	//a packet for A-Side
	Packet apacket;
	//a state for A-Side
	protected int stateOfA;
	//checks if there is a message currently in transit
	protected boolean msgInTran;
	
	//a packet for for B-Side
	Packet bpacket;
	//a state for B-Side
	protected int stateOfB;
	
	//keeps track of everything for displaying statistics
	int appSentPackCount = 0;//packet sent by application layer
	int appReceivedPackCount = 0;//packet received by application layer
	int droppedPackCount = 0;//dropped when there is a message in transit
	
	int protocolSentPackCount = 0;//packet sent by the transport protocol
	int protocolReceivedPackCount = 0;//packet received by the transport protocol
	
	int corruptedPackCount = 0;//corrupted packet
	int lostPackCount = 0;//lost packet
	int restranPackCount = 0;//retransmitted packet
	
	int aSentPackCount = 0;//A-Side sent packet
	int receivedAckCount = 0;//received ACK	
	
	int bReceivedPackCount = 0;//B-Side received packet
	int bReceivedCurruptFromA = 0;//received packets from A, but corrupted
	int sentAckCount = 0;//sent ACK	
	int bResentAckCount = 0;//resent ACK 	
	
	//will be used to calculate average RTT
	long startTime; 
	long endTime;
	long TotalRTT = 0;
/**************************************END OF NEEDED VARIABLES*************************************************/
	
	
/*************************************START OF ADDED FUNCTIONS*************************************************/
	//computes checksum
	protected int computeChecksum(Packet p)
	{
		//adds sequence, ack field value, and hash value of the payload to be the checksum
		int checksum = p.getSeqnum() + p.getAcknum() + p.getPayload().hashCode();

		////System.out.println("*****COMPUTED CHECK SUM: "+checksum);//used for debugging
		return checksum;
	}
	
	//verifies the checksum, returns true if the same, returns false otherwise
	protected boolean sameChecksum(Packet p)
	{
		boolean sameChecksum = true;
		//returns false if not the same, and increment packCorruptedCount by one to count how many packets are corrupted
		if(computeChecksum(p)!= p.getChecksum())
		{
			corruptedPackCount++;
			return false;
		}
		
		return sameChecksum;
	}//end of sameChecksum
    
    //displays the statistics
    protected void displayStats()
    {
    	//since the simulator will only send number-1 messages, deducts one from the following
    	appSentPackCount = appSentPackCount - 1;
    	protocolReceivedPackCount = protocolReceivedPackCount - 1;
    	protocolSentPackCount = protocolSentPackCount -1;
    	aSentPackCount = aSentPackCount-1;
    	
    	//total packets sent by both a and b (not include retransmission)
    	int totalSent = protocolSentPackCount+sentAckCount;

    	//packet lost is calculated from retransmitted packets - corrupted packets
    	lostPackCount = restranPackCount - corruptedPackCount;	
    	
    	//calculates the average RTT, convert nanosecond to second
    	double avgRTT = TotalRTT/receivedAckCount/1000000000.0;
    	
    	//display the details
    	System.out.println("\n\n******STATISTICS*****");
    	//for application layer
    	System.out.println("\n*****Application Layer Statistics*****");
    	System.out.println("Application Layer Sent Packets Amount: " + appSentPackCount);
    	System.out.println("Application Layer Received Packets Amount: " + appReceivedPackCount);
    	System.out.println("Dropped Packets (Packet Was Dropped When There Was Already A Message In Transit): " + droppedPackCount);
    	System.out.println("Note: Excluding The Last One That Will Never Send "
    			+ "(eg. If 10 is entered for the number of messages, 9 will be sent and received.)");
    	//for transport protocol
    	System.out.println("\n*****Transport Protocol Statistics*****");  	
    	System.out.println("Transport Protocol Received (From Application Layer) Packets Amount: " + protocolReceivedPackCount);
    	System.out.println("Transport Protocol Sent (To Application Layer) Packets Amount: " + protocolSentPackCount);
    	//for A-Side
    	System.out.println("\n*****A-Side Statistics*****");
    	System.out.println("Sent Packets Amount (Doesn't Include Retransmission): " + aSentPackCount);
    	System.out.println("Retransmitted Amount Caused By Packets Loss/Corruption From A-Side to B-Side: " 
    			+(restranPackCount-bResentAckCount));
    	System.out.println("Retransmitted Amount Caused By ACKs Loss/Corruption From B-Side to A-Side: " + bResentAckCount);
    	System.out.println("Received ACK Amount: " + receivedAckCount);
    	//for B-Side
    	System.out.println("\n*****B-Side Statistics*****");
    	System.out.println("Received Packets Amount: " + bReceivedPackCount);
    	System.out.println("Corrupted Amount of The Received Packets: " + bReceivedCurruptFromA);
    	System.out.println("Sent ACK Amount (Doesn't Include Retransmission): " + sentAckCount);
    	System.out.println("Retransmitted ACK Amount (Because ACKs Lost/Corrupted From B-Side to A-Side): " + bResentAckCount); 
    	//total
    	System.out.println("\n*****Total Statistics*****");
    	System.out.println("Total Packets Sent By A-Side And ACKs Sent By B-Side (Doesn't Include Retransmission): "+ totalSent);
    	System.out.println("Total Retransmitted Packets Amount Caused By Loss/Corruption of Both A and B Sides: " + restranPackCount);
    	System.out.println("Total Corrupted Amount: " + corruptedPackCount);
    	System.out.println("Total Lost Amount: " + lostPackCount);   	
    	//average RTT & lost and corrupted percentage
    	System.out.println("\n*****Average RTT & Lost And Corrupted Percentage*****");
    	System.out.println("Average RTT In Seconds: " + avgRTT);   	
    	System.out.println("Corrupted Percentage: "+(corruptedPackCount*1.0/(totalSent+restranPackCount)*100)+"%");
    	System.out.println("Lost Percentage: "+(lostPackCount*1.0/(totalSent+restranPackCount)*100)+"%");
    }//end of displayStats
/**********************************END OF ADDED FUNCTIONS********************************************************/
    
    
/****************************START OF GIVEN FUNCTIONS THAT I FILLED UP*******************************************/
    
    // This is the constructor.  Don't touch!
    public StudentNetworkSimulator(int numMessages,
                                   double loss,
                                   double corrupt,
                                   double avgDelay,
                                   int trace,
                                   long seed)
    {
        super(numMessages, loss, corrupt, avgDelay, trace, seed);
    }

    // This routine will be called whenever the upper layer at the sender [A]
    // has a message to send.  It is the job of your protocol to insure that
    // the data in such a message is delivered in-order, and correctly, to
    // the receiving upper layer.
    protected void aOutput(Message message)
    {
    	//increments origTranPack by one every time A-Side receives a packet from layer 5
    	appSentPackCount++;
    	
    	//only takes actions when there is no message in transit; 
    	//otherwise, ignores (drops) the data being passed to the A output() routine.
    	if(!msgInTran)
    	{
    		//increment protocolSentPackCount by one, this is to count however packets received from application layer
    		protocolReceivedPackCount++;
    		
    		//fills up apacket (all four parameters in the constructor: public Packet(int seq, int ack, int check, String newPayload))
    		apacket.setSeqnum(stateOfA);
    		////System.out.println("*****APACKET SEQ NUMBER: " + apacket.getSeqnum());//used for debugging
    		
    		apacket.setAcknum(stateOfA);
    		////System.out.println("*****APACKET ACK NUMBER: " + apacket.getAcknum());//used for debugging

    		apacket.setPayload(message.getData());
    		System.out.println("\n*****Received Payload From The Application Layer: " + apacket.getPayload());//used for debugging

    		apacket.setChecksum(computeChecksum(apacket));   		
    		////System.out.println("*****APACKET CHECK SUM: " + apacket.getChecksum());//used for debugging
    		
    		System.out.println("*****Timer Started*****");//used for debugging
    		//starts the timer 
        	startTimer(0,20);
        	
        	//gets the start time of sending a packet, will be used to calculate average RTT
        	startTime = System.nanoTime();
        	////System.out.println("&&&&& Start Time: " + startTime);//used for debugging
        	
        	System.out.println("*****Sending APACKET To The Network*****");//used for debugging
        	//sends apacket to the network, and increments protocolSentPackCount and aSentPackCount by one
        	toLayer3(0,apacket);
        	aSentPackCount++;
        	protocolSentPackCount++;
        	
    		//since awaiting for an ACK, sets msgInTran to true 
        	msgInTran = true;  		
    	}
    	else
    	{
    		System.out.println("*****Dropped Packet When There Was Already A Message In Transit*****");//used for debugging
    		//increment droppedPackCount by one, this is to count how many packets were dropped
    		droppedPackCount++;   		
    	}
    }//end of aOutput
    
    // This routine will be called whenever a packet sent from the B-side 
    // (i.e. as a result of a toLayer3() being done by a B-side procedure)
    // arrives at the A-side.  "packet" is the (possibly corrupted) packet
    // sent from the B-side.
    protected void aInput(Packet packet)
    {
    	////System.out.println("*****AWAITING FOR ACK NUMBER: " + stateOfA);//used for debugging
    	////System.out.println("*****GOT ACK NUMBER: " + packet.getAcknum());//used for debugging    	
    	////System.out.println("*****PASSED IN PACKET CHECK SUM IN aInput: " + computeChecksum(packet));//used for debugging
  	
		//receives the non-corrupted ACK that is awaiting for, stops the timer, sets msgInTran back to false, 
    	//increments receivedAckCount by one, and flips the bit for stateOfA
    	if(sameChecksum(packet)&& packet.getAcknum()==stateOfA)
    	{
    		System.out.println("*****Stop The Timer*****");//used for debugging
    		stopTimer(0);
    		
    		//gets the end time of receiving the ACK, will be used to calculate average RTT
    		endTime = System.nanoTime();    	
    		////System.out.println("&&&&& End Time: " + endTime);//used for debugging
    		
    		//gets the duration from sending a packet to receiving the ACK
    		long duration = (endTime - startTime);
    		////System.out.println("&&&&& Duration: " + duration);//used for debugging
    		
    		//gets to total RTT, will be used to calculate average RTT
    		TotalRTT += duration;
    		////System.out.println("&&&&& Total RTT: " + TotalRTT);//used for debugging
    		
    		//no message in transmission, counts received ACKs, and flips the bit for state of A
        	msgInTran = false;
        	receivedAckCount++;
        	stateOfA = 1 - stateOfA;
    	}
    }//end of aInput
    
    // This routine will be called when A's timer expires (thus generating a 
    // timer interrupt). You'll probably want to use this routine to control 
    // the retransmission of packets. See startTimer() and stopTimer(), above,
    // for how the timer is started and stopped. 
    protected void aTimerInterrupt()
    {
    	System.out.println("*****A's Timer Expired*****");//used for debugging
    	System.out.println("*****Timer Retarted*****");//used for debugging
    	
		//starts the timer 
    	startTimer(0,20);
    	
    	System.out.println("*****Resending APACKET To The Network*****");//used for debugging
    	//resends the packet
    	toLayer3(0,apacket);
    	
		//since awaiting for an ACK, sets msgInTran to true 
    	msgInTran = true; 
    	
    	//increments restranPackCount by one every time A-Side resends the packet
    	restranPackCount++;  	
    }//end of aTimerInterrupt
    
    // This routine will be called once, before any of your other A-side 
    // routines are called. It can be used to do any required
    // initialization (e.g. of member variables you add to control the state
    // of entity A).
    protected void aInit()
    {
    	//initializes an empty packet for A-side, everything(seq, ack, checksum) starts at 0, newPayload starts at ""
    	apacket = new Packet(0,0,0,"");
    	
    	//starts in the state for 0
    	stateOfA = 0;

    	//initializes msgInTran to false
    	msgInTran = false;
    }//end of aInit
    
    // This routine will be called whenever a packet sent from the B-side 
    // (i.e. as a result of a toLayer3() being done by an A-side procedure)
    // arrives at the B-side.  "packet" is the (possibly corrupted) packet
    // sent from the A-side.
    protected void bInput(Packet packet)
    {
    	//increment bReceivedPackCount by one,
    	//this is for counting how many packets received from A-Side (possibly corrupted)
    	bReceivedPackCount++;
    	
    	////System.out.println("*****AWAITING FOR PACKET WITH SEQUENCE NUMBER: " + stateOfB);//used for debugging
    	////System.out.println("*****GOT PACKET WITH SEQUENCE NUMBER: " + packet.getAcknum());//used for debugging
		////System.out.println("*****PASSED IN PACKET CHECK SUM IN bInput: " + computeChecksum(packet));//used for debugging
  	
    	//only takes actions when receives the non-corrupted packet; 
    	//otherwise, ignores (drops) the data being passed in,
    	//counts how many being dropped in else.
    	if(sameChecksum(packet))
    	{
    		//if it is the correct packet that is waiting for, sends the packet to the layer 5, 
    		//increments appReceivedPackCount by one sets up the ACK packet that to be sent back to A-Side, 
    		//increments sentAckCount by one,and flips the bit for stateOfB
    		if(packet.getSeqnum()==stateOfB)
			{			
    			System.out.println("*****Sending The Payload To The Application Layer: " + packet.getPayload());//used for debugging
    			toLayer5(1, packet.getPayload());
    			appReceivedPackCount++;
    			
    			packet.setAcknum(stateOfB);
    			packet.setChecksum(computeChecksum(packet));
    		  	
    			//put the packet into bpacket, which is the new ACK packet that will be sent back to A-Side
    			bpacket = packet;
    			
    			System.out.println("*****Sending ACK Back To A-Side*****");
    			toLayer3(1, bpacket);
    			    			
    			sentAckCount++;
    			stateOfB = 1 - stateOfB;
    			
      			////System.out.println("*****BPACKET ACK NUMBER: " + bpacket.getAcknum());//used for debugging  			
    			////System.out.println("*****BPACKET SEQ NUMBER: " + bpacket.getSeqnum());//used for debugging  				
    			////System.out.println("*****BPACKET PAYLOAD: " + bpacket.getPayload());//used for debugging
    			////System.out.println("*****BPACKET CHECK SUM: " + bpacket.getChecksum());//used for debugging	
			}
    		//wrong packet, resends the previous ACK back to A-Side
			else
			{
				System.out.println("*****Resending The ACK Back to A-Side");//used for debugging
				toLayer3(1, bpacket);
	
				//increments bResentAckCount by one, this counts resending of ACKs
				bResentAckCount++;
			}
    	}
    	else
    	{
    		//increments bReceivedCurruptFromA by one,
    		//this counts the packets that made to B-Side, but corrupted
    		bReceivedCurruptFromA++;
    	}
    }//end of bInput
    
    // This routine will be called once, before any of your other B-side 
    // routines are called. It can be used to do any required
    // initialization (e.g. of member variables you add to control the state
    // of entity B).
    protected void bInit()
    {
    	//initializes an empty packet for B-side, everything(seq, ack, checksum) starts at 0, and newPayload as ""
    	bpacket = new Packet(0,0,0,"");
    	
    	//starts in the state for 0 
    	stateOfB = 0;
    }//end of bInit
/****************************END OF GIVEN FUNCTIONS THAT I FILLED UP*******************************************/
}//end of class
