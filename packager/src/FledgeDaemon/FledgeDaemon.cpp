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
#include <windows.h>
#include <oleacc.h>
#include <comdef.h>
#include <tchar.h>
#include <string>

using namespace std;

// Helper function that returns the text on the cursor
string GetLabelAtPoint()
{
	POINT pt;
	LPTSTR g_pszText = NULL;
	string label;

	if (GetCursorPos(&pt))
	{
		IAccessible *pacc;
		VARIANT vtChild;

		if (SUCCEEDED(AccessibleObjectFromPoint(pt, &pacc, &vtChild)))
		{
			BSTR bsName = NULL;
			BSTR bsValue = NULL;
			pacc->get_accName(vtChild, &bsName);
			pacc->get_accValue(vtChild, &bsValue);
			LPTSTR pszResult = NULL;
			if (bsName)
				label = _bstr_t(bsName);
		    SysFreeString(bsName);
		    SysFreeString(bsValue);
		    VariantClear(&vtChild);
			pacc->Release();
		}
	}

	return label;
}

// This function kills the fledge process
void KillFledge(HWND fledge)
{
	DWORD pid = 0;
	HANDLE fledgeProcess = NULL;
	GetWindowThreadProcessId(fledge, &pid);
	fledgeProcess = OpenProcess(PROCESS_TERMINATE, false, pid);
	TerminateProcess(fledgeProcess, 0);
	PostThreadMessage(GetCurrentThreadId(), WM_QUIT, 0, 0); // cleanup and exit the program
}


HWINEVENTHOOK g_hook;
bool polling = false;
string lastSeenText;

SECURITY_ATTRIBUTES sa;
DWORD threadID;
HANDLE snooper;

DWORD WINAPI SnoopFunction(LPVOID lpParam)
{
	MSG msg;
	do
	{
		PeekMessage(&msg, NULL, 0, 0, PM_REMOVE);
		lastSeenText = GetLabelAtPoint();
		Sleep(100);
	}
	while (msg.message != WM_QUIT);

	polling = false;
	return 0;
}

void StartSnoopThread()
{
	ZeroMemory(&sa, sizeof(sa));
	sa.nLength = sizeof(sa);
	sa.bInheritHandle = false;

	snooper = CreateThread(&sa, 0, SnoopFunction, NULL, NULL, &threadID);
}
// Callback for events
void CALLBACK HandleWinEvent(HWINEVENTHOOK hook, DWORD event, HWND hwnd,
                             LONG idObject, LONG idChild,
                             DWORD dwEventThread, DWORD dwmsEventTime)
{
	char *buf;
	if (event == EVENT_SYSTEM_CAPTUREEND)
	{
		char fledgeTitle[256];
		GetWindowText(GetAncestor(hwnd, GA_ROOTOWNER), (LPWSTR) fledgeTitle, 255);
		if ((strstr(fledgeTitle, "BlackBerry") != NULL) && (strstr(fledgeTitle, "Simulator") != NULL))
		{
			// handle menu items
			if (polling)
			{
				if (lastSeenText == "Exit")
				{
					KillFledge(hwnd);
				}
			}

			// handle main window buttons
			if (GetLabelAtPoint() == "Close")
			{
				KillFledge(hwnd);
			}
		}
	}
	if (event == EVENT_SYSTEM_MENUSTART)
	{
		char fledgeTitle[256];
		GetWindowText(GetAncestor(hwnd, GA_ROOTOWNER), (LPWSTR) fledgeTitle, 255);
		if ((strstr(fledgeTitle, "BlackBerry") != NULL) && (strstr(fledgeTitle, "Simulator") != NULL))
		{
			polling = true;
			StartSnoopThread();
		}
	}
	if (event == EVENT_SYSTEM_MENUEND)
	{
		TerminateThread(snooper, 0);
	}

	return;
}

// Init message listener
void InitializeMSAA()
{
    CoInitialize(NULL);
    g_hook = SetWinEventHook(
        EVENT_SYSTEM_MENUSTART, EVENT_SYSTEM_CAPTUREEND,
        NULL,
        HandleWinEvent,
        0, 0,
        WINEVENT_OUTOFCONTEXT);
}

// Clean up stuff nicely and shut down COM
void ShutdownMSAA()
{
    UnhookWinEvent(g_hook);
    CoUninitialize();
}

int APIENTRY _tWinMain(HINSTANCE hInstance,
                     HINSTANCE hPrevInstance,
                     LPTSTR    lpCmdLine,
                     int       nCmdShow)
{
	InitializeMSAA();

	MSG msg;
	while (GetMessage(&msg, NULL, 0, 0))
	{
		if (msg.message == WM_QUIT)
		{
			break;
		}
		TranslateMessage(&msg);
		DispatchMessage(&msg);
	}

	ShutdownMSAA();

	return (int) msg.wParam;
}
