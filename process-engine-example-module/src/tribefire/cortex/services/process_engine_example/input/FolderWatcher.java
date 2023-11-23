// ============================================================================
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2022
// 
// This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
// 
// This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
// 
// You should have received a copy of the GNU Lesser General Public License along with this library; See http://www.gnu.org/licenses/.
// ============================================================================
package tribefire.cortex.services.process_engine_example.input;

import java.io.File;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.function.Consumer;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.logging.Logger;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.processing.worker.api.Worker;
import com.braintribe.model.processing.worker.api.WorkerContext;
import com.braintribe.model.processing.worker.api.WorkerException;

/**
 * a watcher that controls a directory and transfers all files that are created (and accessible) to a receiver instance.
 * 
 * @author pit
 * @author dirk
 *
 */
public class FolderWatcher implements Worker {
	private Logger logger = Logger.getLogger(FolderWatcher.class);
	private File directory;
	private Set<File> filesToProcess = new HashSet<File>();
	private Object monitor = new Object();
	private Consumer<File> fileReceiver;
	private long lockPollIntervallInMillies = 100;
	private GenericEntity workerIdentification;

	private Future<?> watcherFuture;
	private Future<?> processorFuture;

	public void setLockPollIntervallInMillies(long lockPollIntervallInMillies) {
		this.lockPollIntervallInMillies = lockPollIntervallInMillies;
	}

	@Configurable
	@Required
	public void setWorkerIdentification(GenericEntity workerIdentification) {
		this.workerIdentification = workerIdentification;
	}

	@Configurable
	@Required
	public void setFileReceiver(Consumer<File> fileReceiver) {
		this.fileReceiver = fileReceiver;
	}

	@Configurable
	@Required
	public void setDirectory(File directory) {
		this.directory = directory;
	}

	@Override
	public void start(WorkerContext workerContext) throws WorkerException {
		logger.debug("Start Folder Watcher");
		watcherFuture = workerContext.submit(new Watcher());
		processorFuture = workerContext.submit(new FileProcessor());
	}

	@Override
	public void stop(WorkerContext workerContext) throws WorkerException {
		watcherFuture.cancel(true);
		processorFuture.cancel(true);
	}

	/**
	 * put the files into a file list and notify the worker thread via the monitor object.
	 */
	public void put(File... files) {
		synchronized (filesToProcess) {
			for (File file : files) {
				filesToProcess.add(file);
			}
			synchronized (monitor) {
				monitor.notify();
			}
		}
	}

	/**
	 * the watcher - use the WatchService to react to create files and put them into the file list.
	 * 
	 * @author pit
	 *
	 */
	private class Watcher implements Runnable {
		@Override
		public void run() {
			Path watchDirectory = Paths.get(directory.getAbsolutePath());

			WatchService watchService = null;
			try {
				watchService = watchDirectory.getFileSystem().newWatchService();
				watchDirectory.register(watchService, StandardWatchEventKinds.ENTRY_CREATE);

			} catch (IOException e) {
				logger.error("cannot start watcher on directory [" + directory + "] as " + e, e);
			}
			for (;;) {
				try {
					WatchKey watchKey = watchService.take();
					for (WatchEvent<?> event : watchKey.pollEvents()) {
						if (event.kind() == StandardWatchEventKinds.ENTRY_CREATE) {
							Path path = (Path) event.context();
							File file = new File(directory, path.toString());
							put(file);
						}
					}
					watchKey.reset();
				} catch (InterruptedException e) {
					// shutdown
					break;
				}
			}
			// stop watching
			try {
				watchService.close();
			} catch (IOException e) {
				logger.error("cannot close watcher on directory [" + directory + "] as " + e, e);
			}
		}
	}

	/**
	 * the worker -> process the list of detected files and give them to the receiver if accessible
	 * 
	 * @author pit
	 *
	 */
	private class FileProcessor implements Runnable {
		@Override
		public void run() {
			try {
				for (;;) {
					synchronized (monitor) {
						monitor.wait();
					}

					for (;;) {
						synchronized (filesToProcess) {
							Iterator<File> it = filesToProcess.iterator();

							if (it.hasNext()) {
								File file = it.next();
								if (file.exists() == false) {
									synchronized (filesToProcess) {
										filesToProcess.remove(file);
									}
									continue;
								}
								long fileLength = file.length();
								if (fileLength > 0) {
									try {
										FileChannel channel = FileChannel.open(FileSystems.getDefault().getPath(file.getAbsolutePath()),
												StandardOpenOption.WRITE);

										// RandomAccessFile raf = new RandomAccessFile(file, "rw");

										FileLock lock = channel.tryLock();// raf.getChannel().tryLock();

										if (lock != null) {
											lock.close();
											lock = null;
											// ugly fix to make sure that we only process files that don't grow anymore
											// (i.e. are in the process of being copied into the directory)
											// needs a better method..
											do {
												long lengthNow = file.length();
												if (lengthNow != fileLength) {
													fileLength = lengthNow;
													Thread.sleep(10);
												} else {
													break;
												}
											} while (true);

											try {
												fileReceiver.accept(file);
												// delete

												if (file.delete() == false) {
													logger.error("cannot delete file [" + file + "]");
												}

												synchronized (filesToProcess) {
													filesToProcess.remove(file);
												}
											} catch (Exception e) {
												logger.error("error while processing file " + file + " from watched folder", e);
											} finally {
												channel.close();
											}
										}
									} catch (IOException e) {
										// ignore
										logger.warn("error while accessing file " + file + " from watched folder, requeuing file", e);
									}
								}
							} else {
								break;
							}
						}

						Thread.sleep(lockPollIntervallInMillies);
					}

				}
			} catch (InterruptedException e) {
				// shutdown -> end loop;
				return;
			}

		}
	}

	@Override
	public boolean isSingleton() {
		return true;
	}

	@Override
	public GenericEntity getWorkerIdentification() {
		return workerIdentification;
	}
}
