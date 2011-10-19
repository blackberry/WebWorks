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
// dllmain.cpp : Defines the entry point for the DLL application.
#include <windows.h>
#include <atlstr.h>
#include <string>

using namespace std;

#define MNU_RESET 6030
#define MNU_EXIT 6040

long oldWndProc = 0;
PROCESS_INFORMATION psInfo;
STARTUPINFO startInfo;
HWND fledgeWindow = NULL;

LRESULT CALLBACK WndProc(HWND hwnd, UINT message, WPARAM wParam, LPARAM lParam)
{
	int wmID = 0;
    switch (message)
    {
		case WM_CLOSE:
		case WM_DESTROY:
		case WM_QUIT:
			TerminateProcess(GetCurrentProcess(), 0);
		break;
		case WM_COMMAND:
			wmID = LOWORD(wParam);
			if (wmID == MNU_RESET)
			{
				TerminateProcess(GetCurrentProcess(), 0);				
				break;
			}
			if (wmID == MNU_EXIT)
			{
				TerminateProcess(GetCurrentProcess(), 0);
				break;
			}
		default:
		break;
    }

	return CallWindowProc((WNDPROC) oldWndProc, hwnd, message, wParam, lParam);
}

BOOL APIENTRY DllMain( HMODULE hModule,
                       DWORD  ul_reason_for_call,
                       LPVOID lpReserved
					 )
{
	DWORD WM_FLEDGEHOOKDATA = RegisterWindowMessageA("FledgeHookDataEvent");

	switch (ul_reason_for_call)
	{
	case DLL_PROCESS_ATTACH:

		CStringA eventName;
		eventName.Format("%s%d", "Global\\FledgeHook", GetCurrentProcessId());
		HANDLE hEvent = CreateEventA(NULL, false, false, eventName);
		SetEvent(hEvent);

		MSG msg;

		while (GetMessage(&msg, NULL, 0, WM_FLEDGEHOOKDATA))
		{
			if (msg.message == WM_FLEDGEHOOKDATA)
			{
				fledgeWindow = (HWND) msg.lParam;
				break;
			}
		}

		oldWndProc = GetWindowLong(fledgeWindow, GWL_WNDPROC);
		SetWindowLong(fledgeWindow, GWL_WNDPROC, (long)WndProc);
		break;
	}

	return TRUE;
}
