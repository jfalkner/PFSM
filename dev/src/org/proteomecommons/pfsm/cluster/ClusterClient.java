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

import java.util.zip.*;
import java.beans.*;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

import javax.xml.parsers.*;
import org.proteomecommons.jaf.*;

import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;
import org.w3c.dom.*;
import org.proteomecommons.io.*;
import org.proteomecommons.pfsm.*;
import org.proteomecommons.pfsm.util.*;

import java.util.*;

/**
 * @author Jayson Falkner - jfalkner@umich.edu
 * 
 * A cluster implementation of the search engine.
 */
public class ClusterClient  {
//	// reference to the archive
//	byte[] archive;
//
//	// reference to FASTA file
//	RandomAccessFile raf;
//
//	// list of servers
//	NodeList servers;
//
//	long startTime = 0;
//
//	long stopTime = 0;
//
//	// configuration
//	private ModelConfiguration config;
//
//	// ref dfa
//	private DFAState DFA;
//
//	// progress
//	private double progress = 0;
//
//	// a method to register listeners
//	private LinkedList searchListeners = new LinkedList();
//
//	public SearchGroup[] getSearchGroups() {
//		if (true){
//			throw new RuntimeException ("Fix this, return the proper search groups.");
//		}
//		return null;
//	}
//	
//	/**
//	 * Attempts to free as much memory as possible.
//	 */
//	public long freeMemory() {
//		return 0;
//	}
//
//	/**
//	 * Returns the configuration used for this cluster search.
//	 */
//	public ModelConfiguration getConfiguration() {
//		return config;
//	}
//
//	/**
//	 * Returns the DFA used during this search. It is up to the severs to
//	 * generate this DFA on their own, and currently there are no checks to
//	 * ensure the same DFA is used by all servers.
//	 */
//	public DFAState getFalkModel() {
//		return DFA;
//	}
//
//	/**
//	 * Returns the progress.
//	 */
//	public double getProgress() {
//		return progress;
//	}
//
//	/**
//	 * A method to register search listeners
//	 * 
//	 * @param listener
//	 *            The listener to register.
//	 */
//	public void addSearchListener(SearchListener listener) {
//		this.searchListeners.add(listener);
//	}
//
//	public static void main(String[] args) throws Exception {
//		// check arguments
//		if (args.length < 2) {
//			System.out
//					.println("Usage: java ClusterClient <config-file.xml> <fasta file> <peaklist(s)>");
//			return;
//		}
//
//		// print the run message
//		System.out.println("*** Falk Model Project Cluster Client ***");
//
//		// reference the FASTA file
//		System.out.print("Checking FASTA file: ");
//		File fastaFile = new File(args[1]);
//		if (!fastaFile.exists() || !fastaFile.canRead()) {
//			System.out.println("Failed!\n");
//			System.out
//					.println("The FASTA file you specified either does not exist or can't be read by this program.");
//			return;
//		}
//		System.out.println("OK");
//
//		// parse the config
//		System.out.print("Checking config file: ");
//		DocumentBuilderFactory dbp = null;
//		DocumentBuilder db = null;
//		Document config = null;
//		try {
//			dbp = DocumentBuilderFactory.newInstance();
//			db = dbp.newDocumentBuilder();
//			config = db.parse(new FileInputStream(args[0]));
//		} catch (Throwable e) {
//			System.out.println("Failed!");
//			System.out
//					.println("\nThe configuration file you specified is invalid.");
//			e.printStackTrace();
//			return;
//		}
//		System.out.println("OK");
//
//		// use a list, keep only the valid peak list files
//		LinkedList validPeakLists = new LinkedList();
//		LinkedList pls = new LinkedList();
//		// write each peaklist
//		for (int plIndex = 2; plIndex < args.length; plIndex++) {
//			PeakList pl = PeakListReader.read(args[plIndex]);
//			if (pl == null) {
//				continue;
//			}
//			// add the peak list
//			pls.add(pl);
//			// add the files
//			validPeakLists.add(new File(args[plIndex]));
//		}
//		File[] peakListFiles = (File[]) validPeakLists.toArray(new File[0]);
//
//		// if none matched, skip the search
//		if (pls.size()==0){
//			System.out.println("None of the peak lists you specified are valid, skipping.");
//			return;
//		}
//		
//		// display results -- have to make DFA, etc
//		ModelConfiguration mc = new ModelConfiguration(args[0]);
//		PeptideFragmentPossibilityCache cache = new PeptideFragmentPossibilityCache(
//				mc);
//
//		//make the DFA
////		System.out.println("Peak Lists: "+pls.size());
////		DFAState DFA = FASTAProteinMatcher.mergePeakLists((PeakList[]) pls
////				.toArray(new PeakList[0]), mc, cache);
//
//		// make a command-line listener
//		DefaultSearchListener def = new DefaultSearchListener();
//
//		// make the cluster client
//		ClusterClient cc = new ClusterClient(config, fastaFile, peakListFiles);
//		// set the refs needed - TODO: there must be a better way
//		cc.config = mc;
////		cc.DFA = DFA;
//		// register the listener
//		cc.addSearchListener(def);
//		// run the search
//		cc.run();
//
//		// close the file
//		cc.raf.close();
//		// ensure all threads are closed
//		System.exit(1);
//	}
//
//	/**
//	 * Public constructor for a cluster client.
//	 * 
//	 * @param config
//	 *            A DOM of the config file.
//	 * @param fastaFile
//	 *            A file reference to a valid FASTA file.
//	 * @param peakListFiles
//	 *            An array of file references to valid peak list files.
//	 * @throws Exception
//	 *             Poorly designed, general-purpose exception.
//	 */
//	public ClusterClient(Document config, File fastaFile, File[] peakListFiles)
//			throws Exception {
//
//		// make FASTA reader ref
//		raf = new RandomAccessFile(fastaFile, "rw");
//
//		startTime = System.currentTimeMillis();
//
//		// count how many servers we've got
//		servers = config.getElementsByTagName("SplitSearch");
//		//		System.out.println("SplitSearch size " + servers.getLength());
//
//		// modify the cluster search section
//		for (int serverCount = 0; serverCount < servers.getLength(); serverCount++) {
//			Element splitSearchElement = (Element) servers.item(serverCount);
//			// ref the index file
//			File indexFile = new File(fastaFile.getAbsoluteFile()
//					+ ".sequences");
//			// update the start/end offsets
//			long startPosition = indexFile.length() * serverCount
//					/ servers.getLength();
//			long endPosition = startPosition + indexFile.length() * 1
//					/ servers.getLength();
//			System.out.println("Server: " + serverCount + "; start: "
//					+ startPosition + ", end: " + endPosition);
//			splitSearchElement.setAttribute("startPosition", Long
//					.toString(startPosition));
//			splitSearchElement.setAttribute("endPosition", Long
//					.toString(endPosition));
//		}
//
//		// serialize the config file
//		ByteArrayOutputStream serializedConfig = new ByteArrayOutputStream();
//		TransformerFactory tf = TransformerFactory.newInstance();
//		Transformer autobot = tf.newTransformer();
//		autobot.transform(new DOMSource(config), new StreamResult(
//				serializedConfig));
//		//System.out.println("Config File: "+new
//		// String(serializedConfig.toByteArray()));
//
//		// make the archive -- buffered in memory
//		ByteArrayOutputStream archive = new ByteArrayOutputStream();
//		ZipOutputStream zos = new ZipOutputStream(archive);
//		// write the config
//		ZipEntry configEntry = new ZipEntry("default-config.xml");
//		zos.putNextEntry(configEntry);
//		zos.write(serializedConfig.toByteArray());
//		zos.closeEntry();
//		// write each peaklist
//		for (int plIndex = 0; plIndex < peakListFiles.length; plIndex++) {
//			// reference the file
//			File peakListFile = peakListFiles[plIndex];
//			// name the entry something generic
//			ZipEntry peakListEntry = new ZipEntry("peaklist-" + plIndex
//					+ ".mgf");
//			zos.putNextEntry(peakListEntry);
//			// read in the peak list
//			PeakList peaklist = PeakListReader.read(peakListFile
//					.getAbsolutePath());
//			// write it out as MGF
//			PeakListWriter.write(peaklist, zos, ".mgf");
//			// end the entry
//			zos.closeEntry();
//		}
//		zos.flush();
//		zos.close();
//
//		// set the archive
//		this.archive = archive.toByteArray();
//	}
//
//	// run the search
//	public synchronized void run() {
//		// notify listeners of the start
//		for (Iterator i = searchListeners.iterator(); i.hasNext();) {
//			SearchListener listener = (SearchListener) i.next();
//			listener.searchStarted(this);
//		}
//
//		try {
//			// buffers for results
//			Hashtable writeBuffers = new Hashtable();
//			// buffers for sending work
//			Hashtable readBuffers = new Hashtable();
//			// the server socket channels
//			Hashtable connections = new Hashtable();
//
//			// buffer the job
//			ByteArrayOutputStream out = new ByteArrayOutputStream();
//			// write the size
//			int size = (int) this.archive.length;
//			// set the size
//			out.write((byte) (size >> 24));
//			out.write((byte) (size >> 16));
//			out.write((byte) (size >> 8));
//			out.write((byte) size);
//
//			out.write(archive);
//			// convert to a byte[]
//			byte[] job = out.toByteArray();
//
//			// make a selector
//			Selector selector = Selector.open();
//			// start up each server connection
//			for (int serverIndex = 0; serverIndex < servers.getLength(); serverIndex++) {
//				Element server = (Element) servers.item(serverIndex);
//				try {
//					// connect to the server and try it out
//					SocketChannel channel = SocketChannel
//							.open(new InetSocketAddress(server
//									.getAttribute("ip"), 9999));
//
//					// check if nll
//					if (channel == null) {
//						System.out.println("Can't connect to "
//								+ server.getAttribute("ip"));
//						continue;
//					}
//					// make non-blocking
//					channel.configureBlocking(false);
//
//					// make a write buffer
//					writeBuffers.put(channel, ByteBuffer.wrap(job));
//
//					// register read/write
//					channel.register(selector, SelectionKey.OP_READ
//							| SelectionKey.OP_WRITE);
//					connections.put(channel, channel);
//
//				} catch (Exception e) {
//					System.out.println("Can't connect to "
//							+ server.getAttribute("ip"));
//					//e.printStackTrace();
//				}
//			}
//
//			// handle connections, reads, and writes - stop when no more are
//			// registered
//			while (selector.keys().size() > 0) {
//				// select keys
//				selector.select(500);
//
//				// get iterator
//				Set readyKeys = selector.selectedKeys();
//				Iterator readyItor = readyKeys.iterator();
//
//				// iterate
//				while (readyItor.hasNext()) {
//					// get a key from the set
//					SelectionKey key = (SelectionKey) readyItor.next();
//					// remove the key
//					readyItor.remove();
//
//					// skip invalid keys
//					if (!key.isValid()) {
//						continue;
//					}
//
//					// handle writes
//					if (key.isWritable()) {
//						// get the channel
//						SocketChannel channel = (SocketChannel) key.channel();
//						// try to write the rest of the job
//						ByteBuffer toWrite = (ByteBuffer) writeBuffers
//								.get(channel);
//
//						// if the job is completely sent, remove the buffer
//						if (toWrite.remaining() == 0) {
//							// register only the read
//							channel.register(selector, SelectionKey.OP_READ);
//							// remove the buffer
//							writeBuffers.remove(channel);
//							continue;
//						}
//
//						// write as much of the job as possible
//						channel.write(toWrite);
//					}
//
//					// handle reads
//					if (key.isReadable()) {
//						// get the channel
//						SocketChannel channel = (SocketChannel) key.channel();
//						// get the read buffer
//						ByteBuffer toRead = (ByteBuffer) readBuffers
//								.get(channel);
//						// make sure we've got a buffer
//						if (toRead == null) {
//							byte[] sizeBuf = new byte[4];
//							ByteBuffer sizeByteBuf = ByteBuffer.wrap(sizeBuf);
//							// read the size
//							while (sizeByteBuf.remaining() > 0) {
//								channel.read(sizeByteBuf);
//							}
//							sizeByteBuf.flip();
//
//							// make a new buffer of the expected size
//							toRead = ByteBuffer.allocate(sizeByteBuf.getInt());
//							// put in the hash
//							readBuffers.put(channel, toRead);
//						}
//
//						// read as much as possible
//						channel.read(toRead);
//
//						// if we've read it all, finish the job
//						if (toRead.remaining() == 0) {
//							// display the results
//							//							System.out.println("Adding results from "
//							//									+ channel.socket().getInetAddress());
//							toRead.flip();
//
//							// get the type
//							switch (toRead.getInt()) {
//							case Server.FINISHED: {
//								//									System.out.println("Cancelling the
//								// key."+selector.keys().size());
//								// cancel further reads
//								key.cancel();
//								break;
//							}
//							case Server.PROTEIN_MATCH: {
//								//									System.out.println("Protein Match Found;
//								// size:"+toRead.capacity()+", fill:
//								// "+toRead.limit());
//								// buffer the content
//								byte[] content = new byte[toRead.remaining()];
//								toRead.get(content);
//								//									System.out.println("Content: "+new
//								// String(content));
//								ByteArrayInputStream bais = new ByteArrayInputStream(
//										content);
//								ObjectInputStream ois = new ObjectInputStream(
//										bais);
//								ProteinMatch pm = (ProteinMatch) ois
//										.readObject();
//
//								// notify listeners of the start
//								for (Iterator i = searchListeners.iterator(); i.hasNext();) {
//									SearchListener listener = (SearchListener) i.next();
//									listener.proteinMatched(this, pm);
//								}
//
//								// clear the buffer
//								readBuffers.remove(channel);
//								break;
//							}
//							}
//						}
//					}
//				}
//
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//
//		// notify listeners of the start
//		for (Iterator i = searchListeners.iterator(); i.hasNext();) {
//			SearchListener listener = (SearchListener) i.next();
//			listener.searchFinished(this);
//		}
//	}
}