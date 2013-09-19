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

import java.io.DataInputStream;
import java.nio.*;
import java.nio.channels.*;
import java.io.*;
import java.net.*;
import java.util.*;

/**
 * @author Jayson Falkner - jfalkner@umich.edu
 * 
 * TODO Add a description here.
 */
public class ClusterClientTest {
	public static void main(String[] args) {
		try {
			// clients to distribute work to
			String[] servers = new String[]{"192.168.1.204", "127.0.0.1"};
			//		String[] servers = new String[]{"127.0.0.1"};
			// buffers for results
			Hashtable writeBuffers = new Hashtable();
			// buffers for sending work
			Hashtable readBuffers = new Hashtable();
			// the server socket channels
			Hashtable connections = new Hashtable();

			// buffer the job
			File file = new File(
					"/root/workspace/pfsm/search-archive/search.zip");
			FileInputStream fis = new FileInputStream(file);

			// buffer the job
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			// write the size
			int size = (int) file.length();
			// set the size
			out.write((byte) (size >> 24));
			out.write((byte) (size >> 16));
			out.write((byte) (size >> 8));
			out.write((byte) size);
			// write out the content
			byte[] buf = new byte[1024];
			for (int bytesRead = fis.read(buf); bytesRead > 0; bytesRead = fis
					.read(buf)) {
				out.write(buf, 0, bytesRead);
			}

			// convert to a byte[]
			byte[] job = out.toByteArray();

			// make a selector
			Selector selector = Selector.open();
			// start up each server connection
			for (int serverIndex = 0; serverIndex < servers.length; serverIndex++) {
				try {
					// connect to the server and try it out
					SocketChannel channel = SocketChannel
							.open(new InetSocketAddress(servers[serverIndex],
									9999));

					// check if nll
					if (channel == null) {
						System.out.println("Can't connect to "
								+ servers[serverIndex]);
						continue;
					}
					// make non-blocking
					channel.configureBlocking(false);

					// make a write buffer
					writeBuffers.put(channel, ByteBuffer.wrap(job));

					// register read/write
					channel.register(selector, SelectionKey.OP_READ
							| SelectionKey.OP_WRITE);
					connections.put(channel, channel);

				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			// handle connections, reads, and writes - stop when no more are
			// registered
			while (selector.keys().size() > 0) {
				//				System.out.println("Keys Left: "+selector.keys().size());
				// select keys
				selector.select(500);

				// get iterator
				Set readyKeys = selector.selectedKeys();
				Iterator readyItor = readyKeys.iterator();

				// iterate
				while (readyItor.hasNext()) {
					// get a key from the set
					SelectionKey key = (SelectionKey) readyItor.next();
					// remove the key
					readyItor.remove();

					// skip invalid keys
					if (!key.isValid()) {
						continue;
					}

					// handle writes
					if (key.isWritable()) {
						// get the channel
						SocketChannel channel = (SocketChannel) key.channel();
						// try to write the rest of the job
						ByteBuffer toWrite = (ByteBuffer) writeBuffers
								.get(channel);

						// if the job is completely sent, remove the buffer
						if (toWrite.remaining() == 0) {
							// register only the read
							channel.register(selector, SelectionKey.OP_READ);
							// remove the buffer
							writeBuffers.remove(channel);
							continue;
						}

						// write as much of the job as possible
						channel.write(toWrite);
					}

					// handle reads
					if (key.isReadable()) {
						// get the channel
						SocketChannel channel = (SocketChannel) key.channel();
						// get the read buffer
						ByteBuffer toRead = (ByteBuffer) readBuffers
								.get(channel);
						// make sure we've got a buffer
						if (toRead == null) {
							byte[] sizeBuf = new byte[4];
							ByteBuffer sizeByteBuf = ByteBuffer.wrap(sizeBuf);
							// read the size
							while (sizeByteBuf.remaining() > 0) {
								System.out.println("Padding size: "
										+ channel.read(sizeByteBuf));
							}
							sizeByteBuf.flip();

							// make a new buffer of the expected size
							toRead = ByteBuffer.allocate(sizeByteBuf.getInt());
							// put in the hash
							readBuffers.put(channel, toRead);
						}

						// read as much as possible
						channel.read(toRead);

						// if we've read it all, finish the job
						if (toRead.remaining() == 0) {
							// display the results
							System.out.println("Results from "
									+ channel.socket().getInetAddress());
							toRead.flip();
							System.out.println("Buffer size: "
									+ toRead.capacity());
							while (toRead.remaining() > 3) {
								System.out.println("Protein Offset: "
										+ toRead.getLong());
							}

							// cancel further reads
							key.cancel();
						}
					}
				}

			}

			System.out.println("Done.");
		} catch (Exception e) {
			e.printStackTrace();
		}

		//		// display server's response
		//		System.out.println("Response "+servers[serverIndex]);
		//		DataInputStream dis = new DataInputStream(is);
		//		// get the number of hits
		//		int proteinHits = dis.readInt();
		//		// read each hit
		//		for (int hitCount = 0; hitCount < proteinHits; hitCount++) {
		//			System.out.println("protein offset: " + dis.readLong());
		//		}
		//		// close the connection
		//		client.close();

		System.out.println("Client test done.");
		System.exit(1);
	}

}