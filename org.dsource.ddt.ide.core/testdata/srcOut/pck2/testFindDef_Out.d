module testFindDef_Out; //incorrect decl

import pack.sample;

class Foo {
	int foox;

	static class Inner { 
		int innerx;
	}
}

Foo foo;

NotFound notfound;

SampleClass sampleClass; // in another editor