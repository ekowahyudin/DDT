package dtool.tests.ref.cc;

import static melnorme.utilbox.core.Assert.AssertNamespace.assertTrue;
import mmrnmhrm.core.codeassist.DeeCompletionEngine;
import mmrnmhrm.tests.ITestResourcesConstants;
import mmrnmhrm.tests.SampleMainProject;

import org.eclipse.dltk.core.IBuffer;
import org.eclipse.dltk.core.ISourceModule;
import org.eclipse.dltk.core.ModelException;
import org.junit.After;

import dtool.resolver.CompareDefUnits;
import dtool.resolver.DefUnitCollector;
import dtool.resolver.PrefixDefUnitSearch;
import dtool.resolver.api.PrefixDefUnitSearchBase;
import dtool.resolver.api.PrefixDefUnitSearchBase.ECompletionResultStatus;
import dtool.tests.DToolBaseTest;

public class CodeCompletion__Common extends DToolBaseTest {
	
	protected final ISourceModule srcModule;
	protected ICodeCompletionTester ccTester;
	
	public CodeCompletion__Common(String testFilePath) {
		this(SampleMainProject.getSourceModule(ITestResourcesConstants.TR_CA, testFilePath));
	}
	
	public CodeCompletion__Common(ISourceModule srcModule) {
		this.srcModule = srcModule;
		this.ccTester = new ICodeCompletionTester() {
			@Override
			public void testComputeProposalsWithRepLen(int repOffset, int prefixLen,
					int repLen, boolean removeObjectIntrinsics, String... expectedProposals) throws ModelException {
				testComputeProposalsDo(repOffset, repLen, removeObjectIntrinsics, expectedProposals);
			}
			
			@Override
			public void runAfters() {
			}
		};
	}
	
	@After
	public void runAfters() {
		ccTester.runAfters();
	}
	
	protected int getMarkerEndOffset(String marker) throws ModelException {
		return srcModule.getSource().indexOf(marker) + marker.length();
	}
	
	protected int getMarkerStartOffset(String marker) throws ModelException {
		return srcModule.getSource().indexOf(marker);
	}
	
	protected IBuffer getDocument() throws ModelException {
		return srcModule.getBuffer();
	}
	
	protected PrefixDefUnitSearchBase testUnavailableCompletion(int offset, ECompletionResultStatus caResult) 
			throws ModelException {
		PrefixDefUnitSearch search = DeeCompletionEngine.doCompletionSearch(offset, srcModule, 
			new DefUnitCollector());
		assertTrue(search.getResultCode() == caResult);
		return search;
	}
	
	public void testComputeProposalsWithRepLen(int repOffset, int prefixLen,
			int repLen, boolean removeObjectIntrinsics, String... expectedProposals) throws ModelException {
		ccTester.testComputeProposalsWithRepLen(repOffset, prefixLen, repLen, removeObjectIntrinsics, expectedProposals);
	}
	
	public final void testComputeProposals(int repOffset, int prefixLen, boolean removeObjectIntrinsics,
			String... expectedProposals) throws ModelException {
		testComputeProposalsWithRepLen(repOffset, prefixLen, 0, removeObjectIntrinsics, expectedProposals);
	}
	
	
	protected void testComputeProposalsDo(int repOffset, int repLen, boolean removeObjectIntrinsics,
			String[] expectedProposals) throws ModelException {
		
		DefUnitCollector defUnitAccepter = new DefUnitCollector();
		
		PrefixDefUnitSearch completionSearch = DeeCompletionEngine.doCompletionSearch(
			repOffset, srcModule, defUnitAccepter);
		
		if(expectedProposals == null) {
			assertTrue(completionSearch.getResultCode() != ECompletionResultStatus.RESULT_OK);
		} else {
			assertTrue(completionSearch.getResultCode() == ECompletionResultStatus.RESULT_OK);
			assertTrue(completionSearch.searchOptions.rplLen == repLen);
			
			CompareDefUnits.checkResults(defUnitAccepter.results, expectedProposals, removeObjectIntrinsics);
		}
	}
	
}