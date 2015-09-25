/*******************************************************************************
 * Copyright (c) 2014 Bruno Medeiros and other Contributors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Bruno Medeiros - initial API and implementation
 *******************************************************************************/
package dtool.dub;

import java.io.IOException;
import java.nio.file.attribute.FileTime;
import java.util.concurrent.ExecutionException;

import dtool.dub.DubBundle.DubBundleException;
import melnorme.utilbox.concurrency.ITaskAgent;
import melnorme.utilbox.core.CommonException;
import melnorme.utilbox.core.fntypes.ICallable;
import melnorme.utilbox.misc.StringUtil;
import melnorme.utilbox.process.ExternalProcessHelper;
import melnorme.utilbox.process.ExternalProcessHelper.ExternalProcessResult;

/**
 * Helper code to run DUB commands.
 */
public class DubHelper {
	
	public static final String DUB_PATH_OVERRIDE = System.getProperty("DTool.DubPath");
	
	static {
		if(DUB_PATH_OVERRIDE != null) {
			System.out.println(":::: DubPathOverride: " + DUB_PATH_OVERRIDE);	
		}
	}
	
	public static String getDubPath(String dubPath) {
		if(dubPath != null && !dubPath.isEmpty()) {
			return dubPath;
		}
		return "dub";
	}
	
	public static DubBundleDescription runDubDescribe(BundlePath bundlePath, String dubPath) 
			throws IOException, InterruptedException {
		return runDubDescribe(bundlePath, dubPath, false);
	}
	
	public static DubBundleDescription runDubDescribe(BundlePath bundlePath, String dubPath, boolean allowDepDownload) 
			throws IOException, InterruptedException 
	{
		dubPath = getDubPath(dubPath);
		
		ProcessBuilder pb = allowDepDownload ? 
				new ProcessBuilder(dubPath, "describe") : 
				new ProcessBuilder(dubPath, "describe", "--nodeps");
		
		pb.directory(bundlePath.getLocation().toFile());
		
		ExternalProcessResult processResult = new ExternalProcessHelper(pb).awaitTerminationAndResult();
		
		return parseDubDescribe(bundlePath, processResult);
	}
	
	public static DubBundleDescription parseDubDescribe(BundlePath bundlePath, ExternalProcessResult processResult) {
		String describeOutput = processResult.stdout.toString(StringUtil.UTF8);
		
		int exitValue = processResult.exitValue;
		if(exitValue != 0) {
			DubBundleException error = new DubDescribeFailure(processResult);
			return new DubBundleDescription(new DubBundle(bundlePath, DubBundleDescription.BUNDLE_NAME_ERROR, error));
		}
		
		// Trim leading characters. 
		// They shouldn't be there, but sometimes dub outputs non JSON text if downloading packages
		describeOutput = StringUtil.substringFromMatch('{', describeOutput);
		
		return DubDescribeParser.parseDescription(bundlePath, describeOutput);
	}
	
	@SuppressWarnings("serial")
	public static class DubDescribeFailure extends DubBundleException {
		
		protected final ExternalProcessResult processResult;
		
		public DubDescribeFailure(ExternalProcessResult processResult) {
			super("dub returned non-zero status: " + processResult.exitValue);
			this.processResult = processResult;
		}
		
		public String getStdOut() {
			return processResult.getStdOutBytes().toString();
		}
		
		public String getStdErr() {
			return processResult.getStdErrBytes().toString();
		}
		
	}
	
	public static class RunDubDescribeCallable implements ICallable<DubBundleDescription, Exception> {
		
		protected final BundlePath bundlePath;
		protected final String dubPath;
		protected final boolean allowDepDownload;
		
		protected volatile FileTime startTimeStamp = null;
		
		public RunDubDescribeCallable(BundlePath bundlePath, String dubPath, boolean allowDepDownload) {
			this.bundlePath = bundlePath;
			this.dubPath = dubPath;
			this.allowDepDownload = allowDepDownload;
		}
		
		@Override
		public DubBundleDescription call() throws IOException, InterruptedException {
			startTimeStamp = FileTime.fromMillis(System.currentTimeMillis());
			return DubHelper.runDubDescribe(bundlePath, dubPath, allowDepDownload);
		}
		
		public FileTime getStartTimeStamp() {
			return startTimeStamp;
		}
		
		public DubBundleDescription submitAndGet(ITaskAgent processAgent) throws CommonException {
			try {
				return processAgent.submit(this).get();
			} catch (InterruptedException e) {
				throw new CommonException("Error running `dub describe`, operation interrupted.");
			} catch (ExecutionException e) {
				throw new CommonException("Error running `dub describe`:", e.getCause());
			}
		}
		
	}
	
}