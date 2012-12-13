package dtool.parser;

public class Token {
	
	public final DeeTokens tokenType;
	public final int start;
	public final String value; //TODO, don't store this.
	
	public Token(DeeTokens tokenCode, CharSequence source, int start, int end) {
		this.start = start;
		this.value = source.subSequence(start, end).toString();
		this.tokenType = tokenCode;
	}
	
	public final DeeTokens getTokenType() {
		return tokenType;
	}
	
	public final int getStartPos() {
		return start;
	}
	
	public final int getLength() {
		return value.length();
	}
	
	public final int getEndPos() {
		return start + value.length();
	}
	
	public String getSourceValue() {
		return value;
	}
	
	public static class ErrorToken extends Token {
		
		protected final String errorMessage;
		protected final DeeTokens originalToken;
		
		public ErrorToken(CharSequence source, int start, int end, DeeTokens originalToken, String errorMessage) {
			super(DeeTokens.ERROR, source, start, end);
			this.originalToken = originalToken;
			this.errorMessage = errorMessage;
		}
		
	}
	
}