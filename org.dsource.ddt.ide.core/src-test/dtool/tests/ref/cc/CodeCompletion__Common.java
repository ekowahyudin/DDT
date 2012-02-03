package dtool.tests.ref.cc;

import static dtool.tests.MiscDeeTestUtils.fnDefUnitToStringAsElement;
import static melnorme.utilbox.core.Assert.AssertNamespace.assertTrue;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import melnorme.utilbox.core.Function;
import mmrnmhrm.tests.ITestResourcesConstants;
import mmrnmhrm.tests.SampleMainProject;

import org.eclipse.dltk.core.IBuffer;
import org.eclipse.dltk.core.ISourceModule;
import org.eclipse.dltk.core.ModelException;
import org.junit.After;

import dtool.ast.definitions.DefUnit;
import dtool.contentassist.CompletionSession;
import dtool.contentassist.CompletionSession.ECompletionSessionResults;
import dtool.refmodel.PrefixDefUnitSearch;
import dtool.refmodel.PrefixDefUnitSearch.IDefUnitMatchAccepter;
import dtool.refmodel.PrefixSearchOptions;
import dtool.tests.DToolBaseTest;

public class CodeCompletion__Common extends DToolBaseTest {
	
	protected final ISourceModule srcModule;
	protected ICodeCompletionTester ccTester;
	
	public CodeCompletion__Common(String testFilePath) {
		this.srcModule = SampleMainProject.getSourceModule(ITestResourcesConstants.TR_CA, testFilePath);
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
	
	protected PrefixDefUnitSearch testUnavailableCompletion(int offset, ECompletionSessionResults caResult) 
			throws ModelException {
		PrefixDefUnitSearch search;
		CompletionSession session = new CompletionSession();
		search = PrefixDefUnitSearch.doCompletionSearch(offset, srcModule, srcModule.getSource(), 
				session, new DefUnitArrayListCollector());
		assertTrue(session.resultCode == caResult);
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
		
		DefUnitArrayListCollector defUnitAccepter = new DefUnitArrayListCollector();
		
		CompletionSession session = new CompletionSession();
		PrefixDefUnitSearch completionSearch = PrefixDefUnitSearch.doCompletionSearch(
				repOffset, srcModule, srcModule.getSource(), session, defUnitAccepter);
		
		if(expectedProposals == null) {
			assertTrue(session.resultCode != ECompletionSessionResults.RESULT_OK);
		} else {
			assertTrue(session.resultCode == ECompletionSessionResults.RESULT_OK, "Code Completion Unavailable");
			assertTrue(completionSearch.searchOptions.rplLen == repLen);
			
			checkProposals(defUnitAccepter.results, expectedProposals, removeObjectIntrinsics);
		}
	}
	
	protected class DefUnitArrayListCollector implements IDefUnitMatchAccepter {
		public final ArrayList<DefUnit> results;
		
		public DefUnitArrayListCollector() {
			this.results = new ArrayList<DefUnit>();
		}
		
		@Override
		public void accept(DefUnit defUnit, PrefixSearchOptions searchOptions) {
			results.add(defUnit);
		}
		
	}
	
	
	public static String[] INTRINSIC_DEFUNITS = new String[] {
			"bit", "size_t", "ptrdiff_t", "hash_t", "string", "wstring", "dstring",
			"printf(char*, ...)", "trace_term()", "Object", "Interface", "ClassInfo",
			"OffsetTypeInfo", "TypeInfo",
			"TypeInfo_Typedef", "TypeInfo_Enum", "TypeInfo_Pointer", "TypeInfo_Array",
			"TypeInfo_StaticArray", "TypeInfo_AssociativeArray", "TypeInfo_Function", "TypeInfo_Delegate",
			"TypeInfo_Class", "TypeInfo_Interface", "TypeInfo_Struct", "TypeInfo_Tuple", "TypeInfo_Const",
			"TypeInfo_Invariant",
			"MemberInfo", "MemberInfo_field", "MemberInfo_function", "Exception", "Error"
	};
	
	public static Set<String> INTRINSIC_DEFUNITS_SET = unmodifiable(hashSet(INTRINSIC_DEFUNITS));
	
	
	public static Function<DefUnit, String> defUnitStringAsElementMapper = new Function<DefUnit, String>() {
		@Override
		public String evaluate(DefUnit obj) {
			return obj == null ? null : obj.toStringAsElement();
		}
	};
	
	public static void checkProposals(List<DefUnit> results, String[] expectedProposalsArr, boolean removeIntrinsics) {
		
		HashSet<String> expectedProposals = hashSet(expectedProposalsArr);
		HashSet<String> resultProposals = hashSet(strmap(results, fnDefUnitToStringAsElement(0)));
		
		if(removeIntrinsics) {
			// Don't remove intrinsics which are explicitly expected
			HashSet<String> intrinsicsProposals = hashSet(INTRINSIC_DEFUNITS);
			Set<String> intrinsicsProposalsToRemove = removeAllCopy(intrinsicsProposals, expectedProposals);
			resultProposals.removeAll(intrinsicsProposalsToRemove);
		}
		resultProposals.remove(null);
		
		assertEqualSet(resultProposals, expectedProposals);
		
	}
	
}