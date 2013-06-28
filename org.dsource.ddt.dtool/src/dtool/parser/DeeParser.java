/*******************************************************************************
 * Copyright (c) 2013, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Bruno Medeiros - initial API and implementation
 *******************************************************************************/
package dtool.parser;

import static melnorme.utilbox.core.Assert.AssertNamespace.assertFail;
import static melnorme.utilbox.core.Assert.AssertNamespace.assertTrue;

import java.util.ArrayList;

import dtool.ast.ASTNode;
import dtool.parser.LexElement.MissingLexElement;

/**
 * Concrete D Parser class
 */
public class DeeParser 
//It's not very elegant, but inheritance is used here just for the purpose of namespace importing:
	extends DeeParser_Statements 
{
	
	public static DeeParserResult parseSource(String source, String defaultModuleName) {
		DeeParser deeParser = new DeeParser(source);
		return deeParser.parseUsingRule(null, defaultModuleName);
	}
	
	public DeeParserResult parseUsingRule(ParseRuleDescription parseRule) {
		return parseUsingRule(parseRule, "__unnamed_module");
	}
	public DeeParserResult parseUsingRule(ParseRuleDescription parseRule, String defaultModuleName) {
		NodeResult<? extends ASTNode> nodeResult;
		if(parseRule == null) {
			nodeResult = parseModule(defaultModuleName);
		} else if(parseRule == DeeParser.RULE_EXPRESSION) {
			nodeResult = parseExpression();
		} else if(parseRule == DeeParser.RULE_REFERENCE) {
			nodeResult = parseTypeReference();
		} else if(parseRule == DeeParser.RULE_DECLARATION) {
			nodeResult = parseDeclaration();
		} else if(parseRule == RULE_TYPE_OR_EXP) {
			nodeResult = parseTypeOrExpression(true);
		} else if(parseRule == DeeParser.RULE_INITIALIZER) {
			nodeResult = parseInitializer();
		} else if(parseRule == DeeParser.RULE_STATEMENT) {
			nodeResult = parseStatement();
		} else if(parseRule == DeeParser.RULE_STRUCT_INITIALIZER) {
			nodeResult = parseStructInitializer();
		} else {
			throw assertFail();
		}
		assertTrue(enabled);
		if(nodeResult.node != null) {
			nodeResult.node.doSimpleAnalysisOnTree();
		}
		return new DeeParserResult(nodeResult, this);
	}
	
	
	protected final String source;
	protected LexElementSource lexSource;
	protected ArrayList<ParserError> lexerErrors = new ArrayList<>();
	protected boolean enabled;
	
	public DeeParser(String source) {
		this(new DeeLexer(source));
	}
	
	public DeeParser(DeeLexer deeLexer) {
		this.source = deeLexer.getSource();
		this.lexSource = new LexElementSource(new DeeLexElementProducer().produceLexTokens(deeLexer));
		this.enabled = true;
	}
	
	@Override
	protected final DeeParser thisParser() {
		return this;
	}
	
	@Override
	public final String getSource() {
		return source;
	}
	
	public final class DeeLexElementProducer extends LexElementProducer {
		
		@Override
		protected void tokenCreated(Token token) {
			DeeTokenSemantics.checkTokenErrors(token, lexerErrors);
		}
		
	}
	
	public LexElementSource getEnabledLexSource() {
		assertTrue(enabled);
		return lexSource;
	}
	
	protected LexElementSource getLexSource() {
		return lexSource;
	}
	
	@Override
	public void setEnabled(boolean enabled) {
		assertTrue(this.enabled == !enabled);
		this.enabled = enabled;
	}
	
	@Override
	public boolean isEnabled() { // There should be no reason to use this other than for contract checks only
		return enabled;
	}
	
	@Override
	public int getSourcePosition() {
		return getLexSource().getSourcePosition();
	}
	
	@Override
	public LexElement lookAheadElement(int laIndex) {
		return getEnabledLexSource().lookAheadElement(laIndex);
	}
	
	@Override
	public LexElement lastLexElement() {
		return getLexSource().lastLexElement();
	}
	
	@Override
	public final LexElement consumeLookAhead() {
		return getEnabledLexSource().consumeInput();
	}
	
	@Override
	public MissingLexElement consumeSubChannelTokens() {
		return getEnabledLexSource().consumeSubChannelTokens();
	}
	
	public DeeParserState saveParserState() {
		LexElementSource lexSource = getEnabledLexSource().saveState();
		return new DeeParserState(lexSource, enabled);
	}
	
	public void restoreOriginalState(DeeParserState savedState) {
		this.lexSource.resetState(savedState.lexSource);
		this.enabled = savedState.enabled;
	}
	
	public class DeeParserState {
		
		protected final LexElementSource lexSource;
		protected final boolean enabled;
		
		public DeeParserState(LexElementSource lexSource, boolean enabled) {
			this.lexSource = lexSource;
			this.enabled = enabled;
		}
		
	}
	
}