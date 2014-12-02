package mmrnmhrm.ui.views;

import static dtool.ast.declarations.PackageNamespace.createPartialDefUnits;
import melnorme.lang.tooling.context.EmptySemanticResolution;
import melnorme.lang.tooling.engine.scoping.ResolutionLookup;
import melnorme.lang.tooling.symbols.INamedElement;
import mmrnmhrm.ui.CommonDeeUITest;

import org.junit.Test;

import dtool.ast.declarations.ModuleProxy;
import dtool.ddoc.TextUI;
import dtool.engine.analysis.DeeLanguageIntrinsics;

public class DeeElementLabelProvider_Test extends CommonDeeUITest {
	
	@Test
	public void testBasic() throws Exception { testBasic$(); }
	public void testBasic$() throws Exception {
		
		INamedElement defElement;
		defElement = new ModuleProxy("foo", null, null);
		assertEquals(TextUI.getLabelForHoverSignature(defElement), "foo");
		assertEquals(DeeElementLabelProvider.getLabelForContentAssistPopup(defElement), "foo");
		
		defElement = new ModuleProxy("pack.mod", null, null);
		assertEquals(TextUI.getLabelForHoverSignature(defElement), "pack.mod");
		assertEquals(DeeElementLabelProvider.getLabelForContentAssistPopup(defElement), "mod");
		
		defElement = new ModuleProxy("pack.sub.mod", null, null);
		assertEquals(TextUI.getLabelForHoverSignature(defElement), "pack.sub.mod");
		assertEquals(DeeElementLabelProvider.getLabelForContentAssistPopup(defElement), "mod");
		
		
		defElement = createPartialDefUnits(array("pack"), new ModuleProxy("modA", null, null), null);
		assertEquals(TextUI.getLabelForHoverSignature(defElement), "pack");
		assertEquals(DeeElementLabelProvider.getLabelForContentAssistPopup(defElement), "pack");

		defElement = createPartialDefUnits(array("pack", "sub"), new ModuleProxy("modA", null, null), null);
		assertEquals(TextUI.getLabelForHoverSignature(defElement), "pack");
		assertEquals(DeeElementLabelProvider.getLabelForContentAssistPopup(defElement), "pack");
		
		
		ResolutionLookup search = new ResolutionLookup("int", null, -1, true, new EmptySemanticResolution());
		search.evaluateScope(DeeLanguageIntrinsics.D2_063_intrinsics.primitivesScope);
		defElement = search.getMatchedElements().iterator().next();
		
		assertEquals(TextUI.getLabelForHoverSignature(defElement), "int");
		assertEquals(DeeElementLabelProvider.getLabelForContentAssistPopup(defElement), "int");
		
	}
	
}