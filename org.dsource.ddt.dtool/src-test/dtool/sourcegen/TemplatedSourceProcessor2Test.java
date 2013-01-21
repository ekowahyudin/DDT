/*******************************************************************************
 * Copyright (c) 2012, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Bruno Medeiros - initial API and implementation
 *******************************************************************************/

package dtool.sourcegen;

import static melnorme.utilbox.core.Assert.AssertNamespace.assertFail;
import static melnorme.utilbox.core.Assert.AssertNamespace.assertTrue;


import org.junit.Test;

import dtool.sourcegen.AnnotatedSource.MetadataEntry;
import dtool.sourcegen.TemplatedSourceProcessor2.TemplatedSourceException;
import dtool.tests.CommonTestUtils;

public class TemplatedSourceProcessor2Test extends CommonTestUtils {
	
	public void testSourceProcessing(String marker, String source, GeneratedSourceChecker... checkers) {
		TemplatedSourceProcessor2 tsp = new TemplatedSourceProcessor2() { 
			@Override
			protected void reportError(int offset) throws TemplatedSourceException {
				assertFail();
			};
		};
		visitContainer(tsp.processSource_unchecked(marker, source), checkers);
	}
	
	public void testSourceProcessing(String marker, String source, int errorOffset) {
		try {
			TemplatedSourceProcessor2.processTemplatedSource(marker, source);
			assertFail();
		} catch(TemplatedSourceException tse) {
			assertTrue(tse.errorOffset == errorOffset);
		}
	}
	
	protected abstract class GeneratedSourceChecker implements Visitor<AnnotatedSource> {} 
	protected GeneratedSourceChecker checkMD(final String expSource, final MetadataEntry... expMetadataArray) {
		return new GeneratedSourceChecker () {
			@Override
			public void visit(AnnotatedSource genSource) {
				assertEquals(genSource.source, expSource);
				assertEquals(genSource.metadata.size(), expMetadataArray.length);
				for (int i = 0; i < expMetadataArray.length; i++) {
					checkMetadata(genSource.metadata.get(i), expMetadataArray[i]);
				}
			}
		};
	}
	
	public static final String DONT_CHECK = new String("NO_CHECK");
	
	protected void checkMetadata(MetadataEntry mde1, MetadataEntry expMde) {
		assertAreEqual(mde1.name, expMde.name);
		assertAreEqual(mde1.value, expMde.value);
		if(expMde.associatedSource != DONT_CHECK)
			assertAreEqual(mde1.associatedSource, expMde.associatedSource);
		assertAreEqual(mde1.offset, expMde.offset);
	}
	
	
	@Test
	public void testSplit() throws Exception { testSplit$(); }
	public void testSplit$() throws Exception {
		for (String splitMarker : array("#:SPLIT", "━━", "▂▂", "▃▃")) {
			testSplit(splitMarker);
		}
		
		for (String headerMarker : array("#:HEADER", "Ⓗ━━", "☒▂▂", "Ⓗ▃▃")) {
			testHeaderSplit(headerMarker, "#:SPLIT", "━━");
		}
	}
	
	public void testSplit(String splitMarker) {
		testSourceProcessing("#", 
			splitMarker+" ___________________\ncase1\nasdfasdf"+
			splitMarker+" comment\ncase ##2\nblahblah\n#:SPLIT comment\r\n"+ 
			splitMarker+"\n case ##:3\nblahblah\n"
			,
			checkMD("case1\nasdfasdf"),
			checkMD("case #2\nblahblah\n"),
			checkMD(""),
			checkMD(" case #:3\nblahblah\n")
		);
		
		
		testSourceProcessing("#", 
			"case ##1\nasdfasdf"+
				splitMarker+" comment\ncase ##2\nblahblah\n"
			,
			checkMD("case #1\nasdfasdf"),
			checkMD("case #2\nblahblah\n")
		);
		
		testSourceProcessing("#", 
			splitMarker+" _____\ncase1\na#:XPLIT sdfasdf"+
			splitMarker+"\n case3\nblahblah\n"
			,
			8
		);
	}
	
	public void testHeaderSplit(String headerMarker, String splitMarker1, String splitMarker2) {
		testSourceProcessing("#", 
			headerMarker+" ___________________\ncase1\nasdfasdf"+
			splitMarker1+" comment\ncase ##2\ncase2.\n#:SPLIT comment\r\n"+ 
			headerMarker+" comment\ncase ##4\nblahblah\n"+splitMarker2+"comment2\r\ncase5"+ 
			splitMarker1+"\n case ##:6\nxxxxxxx\n"
			,
			checkMD("case #2\ncase2.\n"),
			checkMD(""),
			checkMD("case5"),
			checkMD(" case #:6\nxxxxxxx\n")
		);
	}
	
	@Test
	public void testExpansion() throws Exception { testExpansion$(); }
	public void testExpansion$() throws Exception {
		
		testSourceProcessing("#", 
			"asdf ## #{,#},#,,##, ,line}==",
			
			checkMD("asdf # =="),
			checkMD("asdf # }=="),
			checkMD("asdf # ,=="),
			checkMD("asdf # #=="),
			checkMD("asdf #  =="),
			checkMD("asdf # line==")
		);
		
		testSourceProcessing("#", 
			"asdf #{,#},## #{a,xxx#}#,},last}==",
			
			checkMD("asdf =="),
			checkMD("asdf }=="),
			checkMD("asdf # a=="),
			checkMD("asdf # xxx},=="),
			checkMD("asdf last==")
		);
		
		testSourceProcessing("#!", 
			"asdf #ok #!{,#!},#!#! #!{a,xxx#!}#!,},last#}!==",
			
			checkMD("asdf #ok !=="),
			checkMD("asdf #ok }!=="),
			checkMD("asdf #ok #! a!=="),
			checkMD("asdf #ok #! xxx},!=="),
			checkMD("asdf #ok last#!==")
		);
		
		// Syntax errors:
		testSourceProcessing("#", "> #,", 3); 
		testSourceProcessing("#", "> #}", 3); 
		
		testSourceProcessing("#", "foo #@{", 7); 
		testSourceProcessing("#", "foo #@==", 6);
		testSourceProcessing("#", "foo #@!", 7);
		testSourceProcessing("#", "foo #@EXPANSION1", 16); // no data
		testSourceProcessing("#", "foo #@EXPANSION1{", 17); 
		testSourceProcessing("#", "foo #@EXPANSION1{12,}:", 22);
		testSourceProcessing("#", "foo #@EXPANSION1{12,}:EXP)", 22+3);
		testSourceProcessing("#", "foo #@EXPANSION1{12,}(EXP:", 22+3);
		
		testSourceProcessing("#", "foo #@EXPANSION1{12#:SPLIT\n}", 19);
		testSourceProcessing("#", "foo #@EXPANSION1{12#:END:\n}", 20);
		
		testSourceProcessing("#", "foo #@EXPANSION1{12}(#:SPLIT\n)", 21);
		testSourceProcessing("#", "foo #@EXPANSION1{12}(xxx:END:\n)", 21+3);
		
		
		for (int i = 0; i < TemplatedSourceProcessor2.OPEN_DELIMS.length; i++) {
			String openDelim = TemplatedSourceProcessor2.OPEN_DELIMS[i];
			if(openDelim.equals("{")) 
				continue;
			testExpansion_ArgumentDelimiters(openDelim, TemplatedSourceProcessor2.CLOSE_DELIMS[i]);
		}
	}
	
	public void testExpansion_ArgumentDelimiters(String open, String close) {
		String source = prepString("asdf #@EXP►,}◙► #◄,last#◙}◄==", open, close);
		
		testSourceProcessing("#", source,
			
			checkMD(prepString("asdf ,}==", open, close)),
			checkMD(prepString("asdf ► ◄,last●}==", open, close))
		);
		
		testSourceProcessing("#", prepString("asdf #► ", open, close), 6);
	}
	
	public static String prepString(String source, String openDelim, String closeDelim) {
		source = source.replaceAll("►", openDelim);
		source = source.replaceAll("◄", closeDelim);
		source = source.replaceAll("◙", "●");
		return source;
	}
	
	@Test
	public void testMetadata() throws Exception { testMetadata$(); }
	public void testMetadata$() throws Exception {
		testSourceProcessing("#", "foo1 ## #error_EXP(asfd,3,4){xxx}==",
			checkMD("foo1 # xxx==", new MetadataEntry("error_EXP", "asfd,3,4", "xxx", 7))
		);
		
		testSourceProcessing("#", 
			"asdf ## #error(info1)==", 
			checkMD("asdf # ==", new MetadataEntry("error", "info1", null, 7))
		);
		
		testSourceProcessing("#", 
			"asdf ## #error==",
			checkMD("asdf # ==", new MetadataEntry("error", null, null, 7))
		);
		
		testSourceProcessing("#", 
			"asdf ## #error{xxx}==",
			checkMD("asdf # xxx==", new MetadataEntry("error", null, "xxx", 7))
		);
		
		
		testSourceProcessing("#", 
			"foo1 ## #error_EXP:asfd_ad{xxx}==",
			checkMD("foo1 # xxx==", new MetadataEntry("error_EXP", "asfd_ad", "xxx", 7))
		);
		testSourceProcessing("#", 
			"asdf ## #error:info1==", 
			checkMD("asdf # ==", new MetadataEntry("error", "info1", null, 7))
		);
		
		// Syntax errors
		testSourceProcessing("#", "badsyntax #foo(=={", 18);
		testSourceProcessing("#", "badsyntax #foo(==){asdf", 18+5);
		testSourceProcessing("#", "badsyntax #foo:", 15);
		testSourceProcessing("#", "badsyntax #foo: ", 15);
		
		testSourceProcessing("#", "badsyntax #foo(==#:SPLIT\n)", 17);
		testSourceProcessing("#", "badsyntax #foo(==#:END:", 18);
		testSourceProcessing("#", "badsyntax #foo(){xxx#:SPLIT\n)", 17+3);
		testSourceProcessing("#", "badsyntax #foo(){xxx#:END:", 17+3+1);
		
		
		for (int i = 0; i < TemplatedSourceProcessor2.OPEN_DELIMS.length; i++) {
			String open = TemplatedSourceProcessor2.OPEN_DELIMS[i];
			String close = TemplatedSourceProcessor2.CLOSE_DELIMS[i];
			if(open.equals("{")) 
				continue;
			
			testSourceProcessing("#", prepString("asdf #foo(arg)►,}◙► #◄,xxx}◄==", open, close),
				
				checkMD(prepString("asdf ,}◙► ◄,xxx}==", open, close), 
					new MetadataEntry("foo", "arg", prepString(",}◙► ◄,xxx}", open, close), 5))
			);
		}
		
		
		//multineLine MD syntax
		
		testSourceProcessing("#", "multilineMD #error(arg1):", 25);
		testSourceProcessing("#", "multilineMD #error(arg1): \n", 25);
		
		// boundary
		testSourceProcessing("#", 
			"multilineMD #error(arg1,arg2,arg3):\n",
			
			checkMD("multilineMD ", new MetadataEntry("error", "arg1,arg2,arg3", "", -1))
		);
		
		// #:END delim
		testSourceProcessing("#", 
			"multilineMD #error(arg1,arg2,arg3):\n line1\nline2\nline3\n#:END:\nlineOther4\n",
			
			checkMD("multilineMD lineOther4\n", 
				new MetadataEntry("error", "arg1,arg2,arg3", " line1\nline2\nline3\n", -1))
		);
		
		// split interaction
		testSourceProcessing("#", 
			"multilineMD #error(arg1,arg2,arg3):\n line1\nline2\nline3\n#:SPLIT:\nlineOther4\n",
			
			checkMD("multilineMD ", 
				new MetadataEntry("error", "arg1,arg2,arg3", " line1\nline2\nline3\n", -1)),
			checkMD("lineOther4\n")
		);
		
		// nested MDs
		testSourceProcessing("#", 
			"blah before #multiline1:\n blah1\n#multiline2:\n blah2\n#tag(arg1) blah2-cont",
			
			checkMD("blah before ", 
				new MetadataEntry("multiline1", null, " blah1\n", -1),
				new MetadataEntry("multiline2", null, " blah2\n blah2-cont", -1),
				new MetadataEntry("tag", "arg1", null, 7))
		);
		
		// All together
		testSourceProcessing("#", 
			"foo1 ## #error_EXP(asdf,3,4){xxx}=="+
			"asdf ## #error(info1)=="+
			"asdf ## #error=="+
			"asdf ## #error{xxx}=="+
			"multilineMD #error(arg1,arg2,arg3):\n line1\nline2#tagInMD(blah){xxx}\nline3\n#:END:\nlineOther4\n",
			
			checkMD(
				"foo1 # xxx=="+
				"asdf # =="+
				"asdf # =="+
				"asdf # xxx=="+
				"multilineMD lineOther4\n",
				new MetadataEntry("error_EXP", "asdf,3,4", "xxx", 7),
				new MetadataEntry("error", "info1", null, 7 +5+7),
				new MetadataEntry("error", null, null, 7 +5+7 +2+7),
				new MetadataEntry("error", null, "xxx", 7 +5+7 +2+7 +2+7),
				new MetadataEntry("error", "arg1,arg2,arg3", " line1\nline2xxx\nline3\n", -1),
				new MetadataEntry("tagInMD", "blah", "xxx", 12)
			)
		);
	}
	
	@Test
	public void testMetadata_Interactions() throws Exception { testMetadata_Interactions$(); }
	public void testMetadata_Interactions$() throws Exception {
		testSourceProcessing("#", 
			"asdf #{#}#tag_A(asfd,3,4){xxx},abc###tag_B(arg1,arg2,arg3){sourceValue2}}==#{1,xxx}",
			
			checkMD("asdf }xxx==1", new MetadataEntry("tag_A", "asfd,3,4", "xxx", 6)),
			checkMD("asdf }xxx==xxx", new MetadataEntry("tag_A", "asfd,3,4", "xxx", 6)),
			checkMD("asdf abc#sourceValue2==1", new MetadataEntry("tag_B", "arg1,arg2,arg3", "sourceValue2", 9)),
			checkMD("asdf abc#sourceValue2==xxx", new MetadataEntry("tag_B", "arg1,arg2,arg3", "sourceValue2", 9))
		);
		
		testSourceProcessing("#", 
			"#{1,xxx}asdf #{#}#tag_A(asfd,3,4){xxx},###tag_B(arg1,arg2,arg3){sourceValue2}}==",
			
			checkMD("1asdf }xxx==", new MetadataEntry("tag_A", "asfd,3,4", "xxx", 7)),
			checkMD("1asdf #sourceValue2==", new MetadataEntry("tag_B", "arg1,arg2,arg3", "sourceValue2", 7)),
			checkMD("xxxasdf }xxx==", new MetadataEntry("tag_A", "asfd,3,4", "xxx", 9)),
			checkMD("xxxasdf #sourceValue2==", new MetadataEntry("tag_B", "arg1,arg2,arg3", "sourceValue2", 9))
		);
		
		testSourceProcessing("#", 
			"foo1 ## #error_EXP(asdf,3,4){xxx}=="+
			"asdf ## #error(info1)=="+
			"#:SPLIT ____\n"+
			"asdf ## #error=="+
			"asdf ## #error{xxx}=="+
			"#:SPLIT ____\n"+
			"multilineMD #error(arg1,arg2,arg3):\n line1\nline2\nline3\n#:END:\nlineOther4\n",
			
			checkMD(
				"foo1 # xxx=="+
				"asdf # ==",
				new MetadataEntry("error_EXP", "asdf,3,4", "xxx", 7),
				new MetadataEntry("error", "info1", null, 7 +5+7)
			),
			checkMD(
				"asdf # =="+
				"asdf # xxx==",
				new MetadataEntry("error", null, null, 7),
				new MetadataEntry("error", null, "xxx", 7 +2+7)
			),
			checkMD(
				"multilineMD lineOther4\n",
				new MetadataEntry("error", "arg1,arg2,arg3", " line1\nline2\nline3\n", -1)
			)
		);
		
		// Metadata in header:
		testSourceProcessing("#",  "#:HEADER ____\n"+"> #@{A,B,C}", 2);
		
		// Performance test:
		AnnotatedSource[] processTemplatedSource = TemplatedSourceProcessor2.processTemplatedSource("#", 
			"#:HEADER ____\n"+
			">#@N{X#tag(arg){xxx} #tag2(arg){xxx} #tag3(arg){xxx}}"+
			" #@N2!{a#@(N),b#@(N),c#@(N),d#@(N),e#@(N),f#@(N)),g#@(N),h#@(N),k#@(N),l#@(N)}"+
			" #@N3{#@(N2),#@(N2),#@(N2),#@(N2),#@(N2),#@(N2)),#@(N2),#@(N2),#@(N2),#@(N2)}"+
			" #@N4{#@(N2),#@(N2),#@(N2),#@(N2),#@(N2),#@(N2)),#@(N2),#@(N2),#@(N2),#@(N2)}"+
			" #@N5{#@(N2),#@(N2),#@(N2),#@(N2),#@(N2),#@(N2)),#@(N2),#@(N2),#@(N2),#@(N2)}"+
			" #@N6{#@(N2),#@(N2),#@(N2),#@(N2),#@(N2),#@(N2)),#@(N2),#@(N2),#@(N2),#@(N2)}"+
			" #@N7{#@(N2),#@(N2),#@(N2),#@(N2),#@(N2),#@(N2)),#@(N2),#@(N2),#@(N2),#@(N2)}"+
			"==");
		
		System.out.println(processTemplatedSource.length);
	}
	
	@Test
	public void testExpansionInMetadata() throws Exception { testExpansionInMetadata$(); }
	public void testExpansionInMetadata$() throws Exception {
		
		testSourceProcessing("#", 
			"> #@EXPANSION1{var1,var2,var3xxx} #tag(arg1,arg2,arg3){mdsource:#@(EXPANSION1)}",
			
			checkMD("> var1 mdsource:var1", new MetadataEntry("tag", "arg1,arg2,arg3", "mdsource:var1", 7)),
			checkMD("> var2 mdsource:var2", new MetadataEntry("tag", "arg1,arg2,arg3", "mdsource:var2", 7)),
			checkMD("> var3xxx mdsource:var3xxx", new MetadataEntry("tag", "arg1,arg2,arg3", "mdsource:var3xxx", 10))
		);
		
		testSourceProcessing("#", 
			"> #tag(arg1){mdsource: #@EXPANSION1{var1,var2,var3xxx} -- #@{A,B,C}(EXPANSION1)}",
			
			checkMD("> mdsource: var1 -- A", new MetadataEntry("tag", "arg1", "mdsource: var1 -- A", 2)),
			checkMD("> mdsource: var2 -- B", new MetadataEntry("tag", "arg1", "mdsource: var2 -- B", 2)),
			checkMD("> mdsource: var3xxx -- C", new MetadataEntry("tag", "arg1", "mdsource: var3xxx -- C", 2))
		);
		
		
		testSourceProcessing("#", 
			"> #tag(arg){mdsource: #@EXPANSION1{var1,var2,var3xxx} -- #nestedMD{nestedMDsrc #@{A,B,C}(EXPANSION1)}}",
			
			checkMD("> mdsource: var1 -- nestedMDsrc A", 
				new MetadataEntry("tag", "arg", "mdsource: var1 -- nestedMDsrc A", 2),
				new MetadataEntry("nestedMD", null, "nestedMDsrc A", 20)),
			checkMD("> mdsource: var2 -- nestedMDsrc B", 
				new MetadataEntry("tag", "arg", "mdsource: var2 -- nestedMDsrc B", 2),
				new MetadataEntry("nestedMD", null, "nestedMDsrc B", 20)),
			checkMD("> mdsource: var3xxx -- nestedMDsrc C", 
				new MetadataEntry("tag", "arg", "mdsource: var3xxx -- nestedMDsrc C", 2),
				new MetadataEntry("nestedMD", null, "nestedMDsrc C", 23))
		);
		
		testSourceProcessing("#", 
			"> #@EXP{AA,B,CCCC} #tag(arg):\ntagMD #nestedMD{xxx}",
			
			checkMD("> AA ", 
				new MetadataEntry("tag", "arg", "tagMD xxx", -1),
				new MetadataEntry("nestedMD", null, "xxx", 6))
				,
			checkMD("> B ", 
				new MetadataEntry("tag", "arg", "tagMD xxx", -1),
				new MetadataEntry("nestedMD", null, "xxx", 6))
				,
			checkMD("> CCCC ", 
				new MetadataEntry("tag", "arg", "tagMD xxx", -1),
				new MetadataEntry("nestedMD", null, "xxx", 6)
				)
		);
	}
	
	@Test
	public void testPairedExpansion() throws Exception { testPairedExpansion$(); }
	public void testPairedExpansion$() throws Exception {
		
		testSourceProcessing("#", 
			"foo #@EXPANSION1{var1,var2#,,var3##}==#@{A,B,C}:EXPANSION1:",
			
			checkMD("foo var1==A"),
			checkMD("foo var2,==B"),
			checkMD("foo var3#==C")
		);
		
		// Activeness
		testSourceProcessing("#", 
			"#@EXPANSION1!{var1,var2,var3}"+
			">#@!(EXPANSION1) #@{A,B,C}(EXPANSION1) -- #@(EXPANSION1)",
			
			checkMD("> A -- var1"),
			checkMD("> B -- var2"),
			checkMD("> C -- var3")
		);
		testSourceProcessing("#", "#@EXPANSION1!{a,b,c} #@{A,B,C}(EXPANSION1)", 1); // Error: non active ref

		testSourceProcessing("#", 
			"#@EXPANSION1!{var1,var2,var3}==#@!(EXPANSION1) #@{A,B,C}(EXPANSION1)",
			
			checkMD("== A"),
			checkMD("== B"),
			checkMD("== C")
		);
		
		//Error: Mismatched argument count:
		testSourceProcessing("#", "> #@EXPANSION1{a,b} -- #@{a}(EXPANSION1)", 7); 
		testSourceProcessing("#", "> #@EXPANSION1{a,b} -- #@{a,b,c}(EXPANSION1)", 7);
		
		testSourceProcessing("#", "foo #@:EXPANSION1:", 4); // Error: undefined ref
		testSourceProcessing("#", "foo #@{A,B,C}(EXPANSION1)", 4); // Error: undefined ref
		testSourceProcessing("#", "foo #@EXPANSION1{a,b} -- #@EXPANSION1{a,b}", 9); //Error: redefined
		testSourceProcessing("#", "foo #@EXPANSION1{a,#@EXPANSION1{a,b}}", 4); //Error: redefined
		
		testSourceProcessing("#", 
			"#@EXP1{var1,var2,var3}"+
			"#@EXP2!{z1,z2,z3}"+
			"> #@EXP2(EXP1) -- #@{A,B,C}(EXP1)",
			
			checkMD("var1> z1 -- A"),
			checkMD("var2> z2 -- B"),
			checkMD("var3> z3 -- C")
		);
		
		// ---
		
		testSourceProcessing("#", 
			"foo #@EXPANSION1{var1,var2,var3}==#@EXP1ALT{VAR1,VAR2,VAR3}(EXPANSION1) ||"+
			" #@(EXPANSION1) == #@(EXP1ALT)",
			
			checkMD("foo var1==VAR1 || var1 == VAR1"),
			checkMD("foo var2==VAR2 || var2 == VAR2"),
			checkMD("foo var3==VAR3 || var3 == VAR3")
		);
		
		
		testSourceProcessing("#", 
			"foo #@EXPANSION1{var1,var2,var3} == #{a,xxx} -- #@(EXPANSION1)",
			
			checkMD("foo var1 == a -- var1"),
			checkMD("foo var1 == xxx -- var1"),
			checkMD("foo var2 == a -- var2"),
			checkMD("foo var2 == xxx -- var2"),
			checkMD("foo var3 == a -- var3"),
			checkMD("foo var3 == xxx -- var3")
		);
		
		//Visibility of referrals:
		testSourceProcessing("#", ">#@{#@INNER_EXP{A,B,C},#@INNER_EXP{A,B,C}}", 
			checkMD(">A"),checkMD(">B"),checkMD(">C"),
			checkMD(">A"),checkMD(">B"),checkMD(">C"));
		testSourceProcessing("#", "> #@{#@INNER_EXP{A,B,C}, #@(INNER_EXP)}", 3); // Error: undefined ref
		testSourceProcessing("#", "> #@{#@INNER_EXP{A,B,C}, } #@(INNER_EXP)", 4); // Error: undefined ref
		
		// Nesting of expansions
		testSourceProcessing("#", 
			">#@EXPA!{A,B,C} #@X{#@(EXPA),x} #@(X)",
			
			checkMD("> A A"), checkMD("> B B"), checkMD("> C C"), checkMD("> x x")
		);
		
		testSourceProcessing("#", 
			"> #@X{#@EXPA{A,B,C},x} #@(X)",
			
			checkMD("> A A"), checkMD("> B B"), checkMD("> C C"), checkMD("> x x")
		);
	}
	
	@Test
	public void testPairedExpansionWithSplit() throws Exception { testPairedExpansionWithSplit$(); }
	public void testPairedExpansionWithSplit$() throws Exception {
		
		testSourceProcessing("#", 
			"#:SPLIT ____\n"+"#@EXPANSION1{var1,var2#,,var3##}"+
			"#:SPLIT\n> #@(EXPANSION1)",
			2 // Not defined
		);
		
		testSourceProcessing("#", 
			"#:HEADER ____header____\n"+
			"#@EXPANSION1{var1,var2,var3}"+
			"#@EXPANSION2{A,BB,CCC}"+
			"#:SPLIT ___\n> #@EXPANSION2{xxxA,xxxb,xxxc}",
			2 // Redefined
		);
		
		
		testSourceProcessing("#", 
			"#:HEADER ____\n"+"#@EXPANSION1{var1,var2,var3}"+
			"#:SPLIT\n> #@(EXPANSION1)",
			
			checkMD("> var1"),
			checkMD("> var2"),
			checkMD("> var3")
		);
		
		testSourceProcessing("#", 
			"#:HEADER ____\n"+"#@EXPANSION1{var1,var2,var3}"+
			"#:SPLIT\n> #@(EXPANSION1) == #@{A,B,C}(EXPANSION1)",
			
			checkMD("> var1 == A"),
			checkMD("> var2 == B"),
			checkMD("> var3 == C")
		);
		
		testSourceProcessing("#", 
			"#:HEADER ____\n"+"#@EXPANSION1{var1,var2,var3}"+
			"#:SPLIT\n> #@(EXPANSION1) == #@(EXPANSION1)",
			
			checkMD("> var1 == var1"),
			checkMD("> var2 == var2"),
			checkMD("> var3 == var3")
		);
		
		// Activate only
		testSourceProcessing("#", 
			"#:HEADER ____\n"+"#@EXPANSION1{var1,var2,var3}"+
			"#:SPLIT\n>#@!(EXPANSION1) #@{A,B,C}(EXPANSION1) -- #@(EXPANSION1)",
			
			checkMD("> A -- var1"),
			checkMD("> B -- var2"),
			checkMD("> C -- var3")
		);
		
		// Across cases
		testSourceProcessing("#", 
			"#:HEADER ____header____\n"+
			"#@EXPAN_X{X,ZZ}"+
			"#@EXPANSION1{var1,var2,var3}"+
			"#:SPLIT ___\n1: #@!(EXPANSION1)#@EXPANSION3{xxxA,xxxB,xxxC}:EXPANSION1: == #@(EXPANSION1)"+
			"#:SPLIT ___\n2: #@!(EXPANSION1)"+
			"#@(EXPAN_X) _ #@EXPANSION3{xA,xxB,xxxC}:EXPANSION1: == #@{a,bb}(EXPAN_X)",
			
			checkMD("1: xxxA == var1"),
			checkMD("1: xxxB == var2"),
			checkMD("1: xxxC == var3"),
			
			checkMD("2: X _ xA == a"),
			checkMD("2: ZZ _ xA == bb"),
			checkMD("2: X _ xxB == a"),
			checkMD("2: ZZ _ xxB == bb"),
			checkMD("2: X _ xxxC == a"),
			checkMD("2: ZZ _ xxxC == bb")
		);
		
		
		// Nested
		testSourceProcessing("#", 
			"#:HEADER ____\n"+"#@EXPANSION1{var1,var2,var3}"+
			"#:SPLIT\n> #@OUTER{.#@(EXPANSION1).,B} -- #@(EXPANSION1)",
			
			checkMD("> .var1. -- var1"),
			checkMD("> .var2. -- var2"),
			checkMD("> .var3. -- var3"),
			checkMD("> B -- var1"), checkMD("> B -- var2"), checkMD("> B -- var3")
		);
		
		testSourceProcessing("#", 
			"#:HEADER ____\n"+"#@EXPANSION1{var1,var2,var3}"+
			"#:SPLIT\n> #@OUTER{.#@(EXPANSION1).,~#@(EXPANSION1)~} -- #@(EXPANSION1)",
			
			checkMD("> .var1. -- var1"),
			checkMD("> .var2. -- var2"),
			checkMD("> .var3. -- var3"),
			checkMD("> ~var1~ -- var1"),
			checkMD("> ~var2~ -- var2"),
			checkMD("> ~var3~ -- var3")
		);
		
		testSourceProcessing("#", 
			"#:HEADER ____\n"+"#@EXPANSION1{var1,var2,var3}"+
			"#:SPLIT\n> #@(EXPANSION1) -- #@OUTER{.#@(EXPANSION1).,B}",
			
			checkMD("> var1 -- .var1."), checkMD("> var1 -- B"), 
			checkMD("> var2 -- .var2."), checkMD("> var2 -- B"),
			checkMD("> var3 -- .var3."), checkMD("> var3 -- B")
		);
		
	}
	
	@Test
	public void testIfElseExpansion() throws Exception { testIfElseExpansion$(); }
	public void testIfElseExpansion$() throws Exception {
		testSourceProcessing("#", 
			"> #@{A,B#var(Bactive)} #?var{IF}",
			
			checkMD("> A "),
			checkMD("> B IF", new MetadataEntry("var", "Bactive", null, 3))
		);
		
		testSourceProcessing("#", 
			"> #@{A,B#var(Bactive)} #?var{IF,ELSE}",
			
			checkMD("> A ELSE"),
			checkMD("> B IF", new MetadataEntry("var", "Bactive", null, 3))
		);
		
		testSourceProcessing("#", "> #?{IF,ELSE, INVALID}", 4);
		testSourceProcessing("#", "> #@{A ,B #var(Bactive) } #?var{IF,ELSE, INVALID}", 49);

		
		testSourceProcessing("#", 
			"#:HEADER ____\n"+"#@EXPANSION1{1#var1,2#var2,3#var3}"+
			"#:SPLIT\n>#@!(EXPANSION1) #?var1{IF,ELSE} #@{A,B,C}(EXPANSION1) -- #@(EXPANSION1) "+
			"#?var1{IF,ELSE}#?var2{var2}",
			
			checkMD("> ELSE A -- 1 IF", new MetadataEntry("var1", null, null, 13)),
			checkMD("> ELSE B -- 2 ELSEvar2", new MetadataEntry("var2", null, null, 13)),
			checkMD("> ELSE C -- 3 ELSE", new MetadataEntry("var3", null, null, 13))
		);
		
		// Test conditional exp when conditional is inside referred MD
		testSourceProcessing("#", 
			"#parentMD【> #@{A,B#var(Bactive)} #?var{IF} #?parentMD{parentMDActive}】",
			
			checkMD("> A  parentMDActive",
				new MetadataEntry("parentMD", null, DONT_CHECK, 0)
			),
			checkMD("> B IF parentMDActive", 
				new MetadataEntry("parentMD", null, DONT_CHECK, 0),
				new MetadataEntry("var", "Bactive", null, 3)
			)
		);
	}
	
}