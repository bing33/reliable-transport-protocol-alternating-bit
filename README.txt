/*
 * README file For: CS 4480 Computer Networks 
 * 	            Programming Assignment: 2
 *                  Reliable Transport Protocol
 * By: xrawlinson
 * Last Modified Date: 3/27/2016
 */


README:
1. I did Java version. To run the program, please import the source file into Eclipse via the import function.
2. The details of each event will show up first. Once all packets have been transmitted and acknowledged, the statistics will show up. I display them in various sections as the following:


******STATISTICS*****

*****Application Layer Statistics*****
1.	Application Layer Sent Packets Amount;
2.	Application Layer Received Packets Amount;
3.	Dropped Packets (Packet Was Dropped When There Was Already A Message In Transit).
Note: Excluding The Last One That Will Never Send (eg. If 10 is entered for the number of messages, 9 will be sent and received.)

*****Transport Protocol Statistics*****
1.	Transport Protocol Received (From Application Layer) Packets Amount;
2.	Transport Protocol Sent (To Application Layer) Packets Amount.

*****A-Side Statistics*****
1.	Sent Packets Amount (Doesn't Include Retransmission);
2.	Retransmitted Amount Caused By Packets Loss/Corruption From A-Side to B-Side;
3.	Retransmitted Amount Caused By ACKs Loss/Corruption From B-Side to A-Side;
4.	Received ACK Amount.

*****B-Side Statistics*****
1.	Received Packets Amount;
2.	Corrupted Amount of The Received Packets;
3.	Sent ACK Amount (Doesn't Include Retransmission);
4.	Retransmitted ACK Amount (Because ACKs Lost/Corrupted From B-Side to A-Side).

*****Total Statistics*****
1.	Total Packets Sent By A-Side And ACKs Sent By B-Side (Doesn't Include Retransmission);
2.	Total Retransmitted Packets Amount Caused By Loss/Corruption of Both A and B Sides;
3.	Total Corrupted Amount;
4.	Total Lost Amount.

*****Average RTT & Lost And Corrupted Percentage*****
1.	Average RTT In Seconds;
2.	Corrupted Percentage;
3.	Lost Percentage.




