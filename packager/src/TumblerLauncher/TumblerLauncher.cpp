/*
* Copyright 2010-2011 Research In Motion Limited.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
#include <stdio.h>
#include <stdlib.h>
#include <tchar.h>
#include <malloc.h>
#include <process.h>
#include <msxml.h>


// Macro that calls a COM method returning HRESULT value.
#define CHK_HR(stmt)        do { hr=(stmt); if (FAILED(hr)) goto CleanUp; } while(0)

// Macro to verify memory allcation.
#define CHK_ALLOC(p)        do { if (!(p)) { hr = E_OUTOFMEMORY; goto CleanUp; } } while(0)

// Macro that releases a COM object if not NULL.
#define SAFE_RELEASE(p)     do { if ((p)) { (p)->Release(); (p) = NULL; } } while(0)

// A counter to track the errors when dealing with bbwp.properties
int errors = 0;


// Helper function to create a VT_BSTR variant from a null terminated string.
HRESULT VariantFromString(PCWSTR wszValue, VARIANT &Variant)
{
	HRESULT hr = S_OK;
	BSTR bstr = SysAllocString(wszValue);
	CHK_ALLOC(bstr);

	V_VT(&Variant)   = VT_BSTR;
	V_BSTR(&Variant) = bstr;

CleanUp:
	return hr;
}

// Helper function to create a DOM instance.
HRESULT CreateAndInitDOM(IXMLDOMDocument **ppDoc)
{
	HRESULT hr = CoCreateInstance(__uuidof(DOMDocument), NULL, CLSCTX_INPROC_SERVER, IID_PPV_ARGS(ppDoc));
	if (SUCCEEDED(hr))
	{
		// these methods should not fail so don't inspect result
		(*ppDoc)->put_async(VARIANT_FALSE);
		(*ppDoc)->put_validateOnParse(VARIANT_FALSE);
		(*ppDoc)->put_resolveExternals(VARIANT_FALSE);
	}
	return hr;
}

// Helper function to display parse error.
// It returns error code of the parse error.
HRESULT ReportParseError(IXMLDOMDocument *pDoc, char *szDesc)
{
	HRESULT hr = S_OK;
	HRESULT hrRet = E_FAIL; // Default error code if failed to get from parse error.
	IXMLDOMParseError *pXMLErr = NULL;
	BSTR bstrReason = NULL;

	CHK_HR(pDoc->get_parseError(&pXMLErr));
	CHK_HR(pXMLErr->get_errorCode(&hrRet));
	CHK_HR(pXMLErr->get_reason(&bstrReason));
	printf("%-12s\t%s\n", "[ERROR]", szDesc);
	errors++;

CleanUp:
	SAFE_RELEASE(pXMLErr);
	SysFreeString(bstrReason);
	return hrRet;
}

BSTR getJavaHome(WCHAR wsBBWPExe[])
{
	HRESULT hr = S_OK;
	IXMLDOMDocument *pXMLDom = NULL;
	IXMLDOMParseError *pXMLErr = NULL;
	IXMLDOMNode *pNode = NULL;

	BSTR bstrJavaHome = NULL;
	BSTR bstrNodeName = NULL;
	BSTR bstrNodeValue = NULL;
	WCHAR wsBBWPProperties[MAX_PATH];

	BSTR bstrXML = NULL;
	BSTR bstrErr = NULL;
	VARIANT_BOOL varStatus;
	VARIANT varFileName;
	VariantInit(&varFileName);

	CHK_HR(CreateAndInitDOM(&pXMLDom));

	// XML file name to load

	wcscpy_s(wsBBWPProperties, MAX_PATH, wsBBWPExe);
	wcscat_s(wsBBWPProperties, MAX_PATH, L"bin\\bbwp.properties");

	CHK_HR(VariantFromString(wsBBWPProperties, varFileName));
	CHK_HR(pXMLDom->load(varFileName, &varStatus));
	if (varStatus != VARIANT_TRUE)
	{
		// Failed to load xml, get last parsing error
		CHK_HR(pXMLDom->get_parseError(&pXMLErr));
		CHK_HR(pXMLErr->get_reason(&bstrErr));
		printf("%-12s\t%s\n", "[ERROR]", "Failed to load DOM from bbwp.properties");
		errors++;
	}

	bstrJavaHome = SysAllocString(L"//wcp/java");
	CHK_ALLOC(bstrJavaHome);
	CHK_HR(pXMLDom->selectSingleNode(bstrJavaHome, &pNode));

	if (pNode)
	{
		CHK_HR(pNode->get_text(&bstrNodeValue));
		SysFreeString(bstrNodeValue);
		SAFE_RELEASE(pNode);
	}
	else
	{
		CHK_HR(ReportParseError(pXMLDom, "Error while locating <java> in the bbwp.properties."));
	}

CleanUp:
	SAFE_RELEASE(pXMLDom);
	SAFE_RELEASE(pXMLErr);
	SysFreeString(bstrXML);
	SysFreeString(bstrErr);
	SysFreeString(bstrJavaHome);
	SysFreeString(bstrNodeName);
	SysFreeString(bstrNodeValue);
	VariantClear(&varFileName);

	return bstrNodeValue;
}


void _tmain(int argc, _TCHAR* argv[])
{
	int i;
	WCHAR wsBBWPExe[MAX_PATH];
	WCHAR wsJavaExe[MAX_PATH];
	WCHAR wsJavaExeParams[4096];

	BSTR bstrJavaHome = NULL;
	STARTUPINFO si;
	PROCESS_INFORMATION pi;

	// initialize
	ZeroMemory( &si, sizeof(si) );
	si.cb = sizeof(si);
	ZeroMemory( &pi, sizeof(pi) );

	if 	(SearchPath(NULL,
		L"bbwp.exe",
		NULL,
		MAX_PATH,
		wsBBWPExe,
		NULL)) {
			wcsncpy_s(wsBBWPExe, wsBBWPExe, wcslen(wsBBWPExe) - wcslen(L"bbwp.exe"));
	} else {
		printf("%-12s\t%s\n", "[ERROR]", "Failed to locate bbwp.exe.");
		exit(1);
	}

	HRESULT hr = CoInitialize(NULL);
	if(SUCCEEDED(hr))
	{
		bstrJavaHome = getJavaHome(wsBBWPExe);
		if (bstrJavaHome && SysStringLen(bstrJavaHome) > 0)
		{
			wcscpy_s(wsJavaExe, MAX_PATH, bstrJavaHome);
			wcscat_s(wsJavaExe, MAX_PATH, L"\\bin\\java.exe");
		}
		else
		{
			SearchPath(NULL,
				L"java.exe",
				NULL,
				MAX_PATH,
				wsJavaExe,
				NULL);

		}

		SysFreeString(bstrJavaHome);
		CoUninitialize();
	}

	if (errors > 0 ) exit(1);

	wcscpy_s(wsJavaExeParams, 4096, L" -jar \"");
	wcscat_s(wsJavaExeParams, 4096, wsBBWPExe);
	wcscat_s(wsJavaExeParams, 4096, L"bin\\bbwp.jar\"");

	for (i = 1; i <= argc; i++) {
		if (argv[i] != NULL) {
			wcscat_s(wsJavaExeParams, 4096, L" \"");
			wcscat_s(wsJavaExeParams, 4096, argv[i]);
			wcscat_s(wsJavaExeParams, 4096, L"\"");
		}
	}

	// Start the child process.
	if( !CreateProcess(
		wsJavaExe,   				// No module name (use command line)
		wsJavaExeParams,			// Command line
		NULL,           			// Process handle not inheritable
		NULL,           			// Thread handle not inheritable
		FALSE,          			// Set handle inheritance to FALSE
		0,              			// No creation flags
		NULL,           			// Use parent's environment block
		NULL,           			// Use parent's starting directory
		&si,            			// Pointer to STARTUPINFO structure
		&pi )           			// Pointer to PROCESS_INFORMATION structure
		)
	{
		printf("%-12s\t%s\n", "[ERROR]", "Failed to run launch java.exe.");
		return;
	}

	// Wait until child process exits.
	WaitForSingleObject( pi.hProcess, INFINITE );

	// Close process and thread handles.
	CloseHandle( pi.hProcess );
	CloseHandle( pi.hThread );
}
