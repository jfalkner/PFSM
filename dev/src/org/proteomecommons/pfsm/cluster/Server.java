/*
 *    Copyright 2004 Jayson Falkner
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.proteomecommons.pfsm.cluster;

import java.io.*;
import java.beans.*;
import java.util.zip.*;
import java.net.*;
import java.nio.*;
import java.nio.charset.*;
import java.nio.channels.*;
import java.util.*;

import javax.xml.parsers.*;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.*;

import org.proteomecommons.jaf.*;
import org.proteomecommons.io.*;
import org.proteomecommons.pfsm.util.*;
import org.proteomecommons.pfsm.*;

/**
 * 
 * @author Jayson Falkner - jfalkner@umich.edu
 * 
 * A single-threaded server for a cluster implementation of this code.
 */
public class Server extends Thread {
//	private static int port = 9999;
//
//	private static int maxBufferSize = 20000000;
//
//	private boolean run = true;
//
//	// abstraction for comman signals
//	public static final int PROTEIN_MATCH = 0;
//
//	public static final int FINISHED = -1;
//
//	// how long to give a client before dropping the request
//	private static int workRequestTimeout = 5000;
//
//	// a queue for the jobs
//	private LinkedList queue = new LinkedList();
//
//	// a queue for writes
//	Hashtable writeQueue = new Hashtable();
//
//	//	private LinkedList writeQueue = new LinkedList();
//	// a thread to run jobs
//	private SearchThread searchThread = new SearchThread(queue);
//
//	// the selector
//	Selector selector;
//
//	ServerSocketChannel channel;
//
//	// hash of connections with buffers
//	Hashtable buffers = new Hashtable();
//
//	/**
//	 * The server is run by bootstrapping the latest code.
//	 * 
//	 * @param args
//	 * @throws Exception
//	 */
//	public static void main(String[] args) {
//		// bootstrap flag
//		boolean bootstrap = true;
//		// project url
//		String bootstrapURL = "http://www.proteomecommons.org/archive/FalkModel/FalkModel.jar";
//
//		//		if (bootstrap) {
//
//		//		}
//		// unless told otherwise, bootstrap
//		//		else {
//		System.out.println("Starting up the server.");
//		// run a new server
//		Server server = new Server();
//		server.start();
//		System.out.println("Server running, kill the thread.");
//		//		}
//
//	}
//
//	// register's the search with the write queue
//	public void addToWriteQueue(WriteQueueEntry entry) {
//		// get the socket's queue
//		LinkedList queue = (LinkedList) writeQueue.get(entry.chan);
//		if (queue == null) {
//			queue = new LinkedList();
//			writeQueue.put(entry.chan, queue);
//		}
//		// add to the queue
//		queue.add(entry);
//
//		try {
//			// register the channel's write op
//			entry.chan.register(selector, SelectionKey.OP_WRITE);
//			// wake up the selector
//			selector.wakeup();
//		} catch (Exception e) {
//			// if there is a problem, remove the queue item
//			queue.removeLast();
//			// noop
//			e.printStackTrace();
//		}
//	}
//
//	public Server() {
//		searchThread.start();
//	}
//
//	public void shutdown() {
//		// if it is terminated, exit
//		if (!this.run) {
//			return;
//		}
//
//		// toggle off the run
//		this.run = false;
//	}
//
//	public synchronized void run() {
//		try {
//			// make a new selector
//			selector = Selector.open();
//			// create the server's socket
//			channel = ServerSocketChannel.open();
//			channel.configureBlocking(false);
//			InetSocketAddress isa = new InetSocketAddress(port);
//			channel.socket().bind(isa);
//
//			// Register interest in when connection
//			channel.register(selector, SelectionKey.OP_ACCEPT);
//
//			// Wait for something of interest to happen
//			while (run) {
//				// select the keys
//				selector.select(500);
//				// Get set of ready objects
//				Set readyKeys = selector.selectedKeys();
//				Iterator readyItor = readyKeys.iterator();
//
//				// Walk through set
//				while (readyItor.hasNext()) {
//					// Get key from set
//					SelectionKey key = (SelectionKey) readyItor.next();
//
//					// Remove current entry
//					readyItor.remove();
//
//					// skip invalid keys
//					if (!key.isValid()) {
//						System.out.println("Key is invalid!");
//						continue;
//					}
//
//					// handle problems for each key
//					try {
//
//						// accept requests
//						if (key.isAcceptable()) {
//							// Get channel
//							ServerSocketChannel keyChannel = (ServerSocketChannel) key
//									.channel();
//
//							// Accept request
//							SocketChannel socket = (SocketChannel) keyChannel
//									.accept();
//
//							socket.configureBlocking(false);
//							SelectionKey another = socket.register(selector,
//									SelectionKey.OP_READ);
//							//					SelectionKey another = socket.register(selector,
//							// SelectionKey.OP_READ);
//						}
//						// handle writes
//						if (key.isWritable()) {
//							// get the channel
//							SocketChannel keyChannel = (SocketChannel) key
//									.channel();
//
//							// get the buffer
//							LinkedList queue = (LinkedList) writeQueue
//									.get(keyChannel);
//
//							// if the queue is empty, remove the key
//							if (queue.size() == 0) {
//								// unregister write
//								keyChannel.register(selector, 0);
//								continue;
//							}
//
//							// get the queue entry
//							WriteQueueEntry wqe = (WriteQueueEntry) queue
//									.removeFirst();
//
//							// try to write the item - TODO: add timeout, or
//							// non-blocking
//							while (wqe.content.remaining() > 0) {
//								// write as much as you can
//								keyChannel.write(wqe.content);
//							}
//
//							continue;
//						}
//						// get content from clients
//						if (key.isReadable()) {
//							// Get channel
//							SocketChannel keyChannel = (SocketChannel) key
//									.channel();
//
//							// try to get the buffer for this archive
//							ByteBuffer archive = (ByteBuffer) buffers
//									.get(keyChannel.socket());
//							// if it hasnt been initialized, do so
//							if (archive == null) {
//								byte[] sizeBuf = new byte[4];
//								ByteBuffer sizeByteBuf = ByteBuffer
//										.wrap(sizeBuf);
//
//								// read the size
//								boolean connectionIsClosed = false;
//								while (sizeByteBuf.remaining() > 0) {
//									// catch closed connections
//									try {
//										int bytesRead = keyChannel
//												.read(sizeByteBuf);
//										if (bytesRead == -1) {
//											throw new Exception("It is closed.");
//										}
//									} catch (Exception e) {
//										connectionIsClosed = true;
//										key.cancel();
//										keyChannel.close();
//										break;
//									}
//								}
//								sizeByteBuf.flip();
//
//								// make a new byte buffer of the right siez
//								int bufSize = sizeByteBuf.getInt();
//								if (bufSize > maxBufferSize) {
//									System.out
//											.println("Buffer size too large! "
//													+ bufSize);
//									continue;
//								}
//								archive = ByteBuffer.allocate(bufSize);
//								buffers.put(keyChannel.socket(), archive);
//							}
//
//							try {
//								// write more to the buffer
//								keyChannel.read(archive);
//							} catch (Exception e) {
//								System.out.println("Ack!");
//								e.printStackTrace();
//							}
//
//							//check if we've got the whole file
//							if (archive.remaining() == 0) {
//								// prepare the file for reading.
//								archive.flip();
//
//								// make a zip input stream from the archive
//								byte[] bytes = archive.array();
//								// debug
//								FileOutputStream todelete = new FileOutputStream(
//										"todelete.zip");
//								todelete.write(bytes);
//								todelete.flush();
//								todelete.close();
//
//								ByteArrayInputStream bais = new ByteArrayInputStream(
//										bytes);
//								ZipInputStream zis = new ZipInputStream(bais);
//
//								// get all the needed info for a search
//								ModelConfiguration config = null;
//								ArrayList peaklists = new ArrayList();
//								// do cluster work for this computer
//								ClusterConfigHandler cch = new ClusterConfigHandler(
//										keyChannel.socket().getLocalAddress()
//												.getHostAddress());
//
//								// populate search info
//								//									System.out.println("Entries in array");
//								for (ZipEntry entry = zis.getNextEntry(); entry != null; entry = zis
//										.getNextEntry()) {
//									//System.out.println(entry.getName());
//									// parse config file
//									if (entry.getName().equals(
//											"default-config.xml")) {
//										try {
//											// Why do I need to double
//											// buffer this? The SAXParser
//											// won't play nicely if I
//											// don't....
//											ByteArrayOutputStream tempOut = new ByteArrayOutputStream();
//											byte[] tempBuf = new byte[512];
//											for (int bytesRead = zis
//													.read(tempBuf); bytesRead > 0; bytesRead = zis
//													.read(tempBuf)) {
//												tempOut.write(tempBuf, 0,
//														bytesRead);
//											}
//											ByteArrayInputStream tempIn = new ByteArrayInputStream(
//													tempOut.toByteArray());
//
//											// parse the config file from the
//											// buffer
//											config = new ModelConfiguration(
//													tempIn);
//
//											// reset the input and use it to
//											// parse cluster info
//											tempIn = new ByteArrayInputStream(
//													tempOut.toByteArray());
//
//											// init a parser (make sure you've
//											// got Xerces in the classpath)
//											SAXParserFactory factory = SAXParserFactory
//													.newInstance();
//											try {
//												SAXParser parser = factory
//														.newSAXParser();
//												parser.parse(tempIn, cch);
//											} catch (Exception e) {
//												e.printStackTrace();
//											}
//
//										} catch (Exception e) {
//											e.printStackTrace();
//										}
//										continue;
//									}
//									// try to load peaklists
//									else {
//										try {
//											// Any way to do this without
//											// buffering?
//											ByteArrayOutputStream tempOut = new ByteArrayOutputStream();
//											byte[] tempBuf = new byte[512];
//											for (int bytesRead = zis
//													.read(tempBuf); bytesRead > 0; bytesRead = zis
//													.read(tempBuf)) {
//												tempOut.write(tempBuf, 0,
//														bytesRead);
//											}
//											ByteArrayInputStream tempIn = new ByteArrayInputStream(
//													tempOut.toByteArray());
//
//											// read in the peak list
//											PeakList peaklist = PeakListReader
//													.read(tempIn, entry
//															.getName());
//											// add the peak list to the list of
//											// valids
//											peaklists.add(peaklist);
//										} catch (Exception e) {
//											System.out
//													.println("Can't read peak list "
//															+ entry.getName()
//															+ ", skipping.");
//										}
//									}
//								}
//
//								//								// debug what was read
//								//								System.out.println("Config: " + config + ", "
//								//										+ (config == null));
//								//								System.out.println("Dataset: " +
//								// cch.dataset);
//								//								for (int pIndex = 0; pIndex <
//								// peaklists.size(); pIndex++) {
//								//									System.out
//								//											.println("Peaklist: "
//								//													+ ((PeakList) peaklists
//								//															.get(pIndex)).reader
//								//															.getName());
//								//								}
//
//								// make a search group
//								// make an appropriate search group
//								CleavedFASTASearchGroup[] searchGroups = new CleavedFASTASearchGroup[1];
//								try {
//									searchGroups[0] = new CleavedFASTASearchGroup(
//											new File(cch.dataset));
//								} catch (Exception e) {
//									System.out
//											.println("Can't make search group, skipping!");
//									throw new RuntimeException(
//											"Can't make search group!");
//								}
//								// set peak lists/offsets
//								searchGroups[0].startPosition = cch.startPosition;
//								searchGroups[0].endPosition = cch.endPosition;
//								searchGroups[0].put(SearchGroup.MSMS_PEAKLISTS,
//										peaklists);
//
//								// add a job to the queue
//								ServerSearch job = new ServerSearch(config,
//										searchGroups, keyChannel, this);
//								queue.add(job);
//								// notify the thread
//								if (queue.size() == 1) {
//									// wake up the search thread
//									synchronized (searchThread) {
//										searchThread.notifyAll();
//									}
//								}
//
//								// remove the buffer from the queue
//								buffers.remove(keyChannel.socket());
//							}
//						}
//
//					} catch (RuntimeException e) {
//						System.err.println("Can't handle key! Cleaning up.");
//						e.printStackTrace();
//
//						// try to kill the socket and clean up resources
//						try {
//							// handle the sockets
//							if (key.channel() instanceof SocketChannel) {
//								SocketChannel sc = (SocketChannel) key
//										.channel();
//								// remove any entries in the hashes
//								buffers.remove(sc);
//								// try to close the connection
//								sc.close();
//								// cancel the key
//								key.cancel();
//							}
//						} catch (Exception ee) {
//							// noop
//						}
//					}
//				}
//			}
//
//			// close the socket
//			channel.close();
//
//		} catch (IOException e) {
//			e.printStackTrace();
//			System.out.println("Critical error, stopping server.");
//			// try to terminate the server
//			try {
//				channel.close();
//			} catch (Exception ee) {
//				//noop
//			}
//			return;
//		} finally {
//			System.out.println("Executing finally clause.");
//		}
//		System.out.println("Server terminated.");
//	}
//}
//
////a thread to do searches
//
//class SearchThread extends Thread implements SearchListener {
//	LinkedList queue;
//
//	// buffer the hits
//	LinkedList hits = new LinkedList();
//
//	// get reference to everything that is needed
//	public SearchThread(LinkedList queue) {
//		this.queue = queue;
//	}
//
//	/**
//	 * Remove the job and finish it.
//	 * 
//	 * @see org.proteomecommons.pfsm.util.FASTASequenceMatcherListener#searchFinished()
//	 */
//	public void searchFinished(Search search) {
//		ServerSearch job = (ServerSearch) queue.removeFirst();
//
//		try {
//			//			System.out.println("Writing FINISH");
//			// buffer the final packet
//			ByteBuffer buffer = ByteBuffer.allocate(8);
//			buffer.putInt(4);
//			buffer.putInt(Server.FINISHED);
//			buffer.flip();
//			// make a new entry
//			WriteQueueEntry writeQueueEntry = new WriteQueueEntry(
//					job.socketChannel, buffer);
//			// register this as a completed search, send content back to the
//			// client
//			job.server.addToWriteQueue(writeQueueEntry);
//		} catch (Exception e) {
//			System.out.println("Error while sending results, skipping.");
//			e.printStackTrace();
//		}
//
//		//		System.out.println("Finished the search.");
//
//	}
//
//	/**
//	 * Initialize and prepare for the job.
//	 * 
//	 * @see org.proteomecommons.pfsm.util.FASTASequenceMatcherListener#searchStarted()
//	 */
//	public void searchStarted(Search search) {
//		hits.clear();
//
//	}
//
//	/**
//	 * Handle matched proteins.
//	 * 
//	 * @see org.proteomecommons.pfsm.util.FASTASequenceMatcherListener#sequenceMatched(long)
//	 */
//	public void proteinMatched(Search search, ProteinMatch pm) {
//		ServerSearch job = (ServerSearch) queue.getFirst();
//
//		// send the hit to the clieint
//		try {
//			//			System.out.println("Writing PROTEIN_MATCH");
//			// buffer the final packet
//			ByteArrayOutputStream baos = new ByteArrayOutputStream();
//			ObjectOutputStream oos = new ObjectOutputStream(baos);
//			oos.writeObject(pm);
//			oos.flush();
//			oos.close();
//
//			//			XMLEncoder encoder = new XMLEncoder(baos);
//			//			encoder.writeObject(pm);
//			//			encoder.flush();
//			//			encoder.close();
//			//			XMLDecoder decoder = new XMLDecoder(new
//			// ByteArrayInputStream(baos.toByteArray()));
//			//			ProteinMatch foo = (ProteinMatch)decoder.readObject();
//			//			System.out.println("Name: "+pm.getName());
//			//			System.out.println("Sequence: "+pm.getSequence());
//			//			System.out.println("Sending: "+new String(baos.toByteArray()));
//			// buffer and send in the queue
//			ByteBuffer buffer = ByteBuffer.allocate(baos.size() + 8);
//			buffer.putInt(baos.size() + 4);
//			buffer.putInt(Server.PROTEIN_MATCH);
//			buffer.put(baos.toByteArray());
//			buffer.flip();
//			// make a new entry
//			WriteQueueEntry writeQueueEntry = new WriteQueueEntry(
//					job.socketChannel, buffer);
//			// register this as a completed search, send content back to the
//			// client
//			job.server.addToWriteQueue(writeQueueEntry);
//		} catch (Exception e) {
//			System.out.println("Couldn't send protein match.");
//			e.printStackTrace();
//		}
//	}
//
//	public synchronized void run() {
//		while (true) {
//			// if no jobs to do, sleep
//			if (queue.size() == 0) {
//				try {
//					wait();
//				} catch (Exception e) {
//					// noop
//				}
//			}
//			// get a job
//			ServerSearch job = (ServerSearch) queue.getFirst();
//
//			try {
//				// run the search
//				PeptideFragmentPossibilityCache cache = new PeptideFragmentPossibilityCache(
//						job.config);
//				
//				if (true) {
//					throw new RuntimeException("Fix me, you tweaked the search engine.");
//				}
////				FASTASequenceMatcher search = new FASTASequenceMatcher(
////						job.searchGroups, job.config, cache);
////
////				//register the listener
////				search.addSearchListener(this);
////
////				search.run();
//
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//		}
//	}
//}
//
////a thread to do searches
//
//class ServerSearch {
//	ModelConfiguration config;
//	CleavedFASTASearchGroup[] searchGroups;
//
//	// the socket this search's results should be sent to.
//	SocketChannel socketChannel;
//
//	// the output buffer
//	ByteBuffer toWrite;
//
//	// reference to parent server
//	Server server;
//
//	// get reference to everything that is needed
//	public ServerSearch(ModelConfiguration config,
//			CleavedFASTASearchGroup[] searchGroups,
//			SocketChannel socketChannel, Server server) {
//
//		this.searchGroups = searchGroups;
//		this.server = server;
//		this.config = config;
//		this.socketChannel = socketChannel;
//	}
//}
//
//class ClusterConfigHandler extends DefaultHandler {
//	long startPosition = 0;
//
//	long endPosition = 0;
//
//	String dataset = "/server/nr";
//
//	String ip;
//
//	public ClusterConfigHandler(String ip) {
//		// translate any 0.0.0.0 to 127.0.0.1, speed up localhost
//		if (ip.equals("0.0.0.0")) {
//			ip = "127.0.0.1";
//		}
//		this.ip = ip;
//	}
//
//	/*
//	 * (non-Javadoc)
//	 * 
//	 * @see org.xml.sax.ContentHandler#startElement(java.lang.String,
//	 *      java.lang.String, java.lang.String, org.xml.sax.Attributes)
//	 */
//	public void startElement(String uri, String localname, String qname,
//			Attributes attributes) throws SAXException {
//		// handle split searches
//		if (qname.equals("SplitSearch")) {
//
//			System.out.println("ip: " + ip + ", versus: "
//					+ attributes.getValue("ip"));
//			// check ip
//			String workForIP = attributes.getValue("ip");
//
//			if (!ip.equals(workForIP)) {
//				return;
//			}
//			// parse the info as work
//			startPosition = Long
//					.parseLong(attributes.getValue("startPosition"));
//			endPosition = Long.parseLong(attributes.getValue("endPosition"));
//		}
//		// handle database configs
//		else if (qname.equals("SequenceData")) {
//			// get the new database
//			dataset = attributes.getValue("uri");
//		}
//		// a cheap server terminator -- TODO: this is really lame
//		else if (qname.equals("HardReboot")) {
//			System.out.println("Hard-Rebooting.");
//			System.exit(1);
//		}
//	}
//}
//
//// queue entries
//
//class WriteQueueEntry {
//	ByteBuffer content;
//
//	SocketChannel chan;
//
//	public WriteQueueEntry(SocketChannel chan, ByteBuffer content) {
//		this.chan = chan;
//		this.content = content;
//	}
}