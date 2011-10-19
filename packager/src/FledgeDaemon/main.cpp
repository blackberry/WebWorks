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
#include <psapi.h>
#include <atlstr.h>

#define HOOK_DLLNAME "FledgeHook.dll"
#define MODULE_ARRAY 100
HWND fledgeWindow = NULL;
LPTHREAD_START_ROUTINE pfnLoadLibrary;

// Check to see if fledge is already patched
BOOL IsAlreadyPatched(DWORD pid)
{
	HANDLE fledge = OpenProcess(PROCESS_ALL_ACCESS, false, pid);
	HMODULE *hmArray = (HMODULE *) malloc(sizeof(HMODULE)*MODULE_ARRAY);
	DWORD needed;

	EnumProcessModules(fledge, hmArray, sizeof(hmArray), &needed);

	if (needed > sizeof(hmArray))
	{
		// we need more memory apparently
		free(hmArray);
		hmArray = (HMODULE *) malloc(needed);
		EnumProcessModules(fledge, hmArray, sizeof(hmArray)*MODULE_ARRAY, &needed);
	}

	CString fileName;
	for (int i = 0; i<needed / sizeof(HMODULE); i++)
	{
		GetModuleFileNameEx(fledge, hmArray[i], fileName.GetBuffer(MAX_PATH + 1), MAX_PATH);
		if (fileName.Find(CString(HOOK_DLLNAME)) > -1)
		{
			free(hmArray);
			return true;
		}
	}

	if (hmArray) free(hmArray);

	CloseHandle(fledge);

	return false;
}

DWORD pidToAttach = 0;

BOOL CALLBACK FindFledge(HWND hwnd, LPARAM lParam)
{
	DWORD pid;
	CString windowTitle;

	GetWindowText(hwnd, windowTitle.GetBuffer(50), 49);
	if ((windowTitle.Find("BlackBerry") > -1) && (windowTitle.Find("Simulator") > -1))
	{
		DWORD pid;
		GetWindowThreadProcessId(hwnd, &pid);

		if (!lParam) lParam = pid;

		if (!IsAlreadyPatched(pid))
		{
			if (lParam == pid)
			{
				fledgeWindow = hwnd;
				return false;
			}
			else
				return false;
		}
		else
			return true;
	}
	else
		return true;
}

typedef BOOL (WINAPI *LPFN_ISWOW64PROCESS) (HANDLE, PBOOL);

LPFN_ISWOW64PROCESS fnIsWow64Process;

BOOL IsWow64()
{
    BOOL bIsWow64 = FALSE;

    //IsWow64Process is not available on all supported versions of Windows.
    //Use GetModuleHandle to get a handle to the DLL that contains the function
    //and GetProcAddress to get a pointer to the function if available.

    fnIsWow64Process = (LPFN_ISWOW64PROCESS) GetProcAddress(
        GetModuleHandleA("kernel32"),"IsWow64Process");

    if(NULL != fnIsWow64Process)
    {
        if (!fnIsWow64Process(GetCurrentProcess(),&bIsWow64))
        {
            //handle error
        }
    }
    return bIsWow64;
}

int WINAPI WinMain(      
    HINSTANCE hInstance,
    HINSTANCE hPrevInstance,
    LPSTR lpCmdLine,
    int nCmdShow
)
{
	SECURITY_ATTRIBUTES sa;
	sa.bInheritHandle = true;
	sa.nLength = sizeof(SECURITY_ATTRIBUTES);
	sa.lpSecurityDescriptor = NULL;

	if (!IsWow64())
		return 1;

	if (!CString(lpCmdLine).IsEmpty())
		pidToAttach = atoi(lpCmdLine);

	HMODULE hKernel32 = LoadLibrary("KERNEL32.DLL");
	pfnLoadLibrary = (LPTHREAD_START_ROUTINE)GetProcAddress(hKernel32, ("LoadLibraryA"));

	char *pDataRemote = 0;

	DWORD numBytes = 0;
	HANDLE fledgeProcess = NULL;

	int numTries = 30;

	while (numTries)
	{
		EnumWindows(FindFledge, pidToAttach);
		if (fledgeWindow != NULL)
			break;
		Sleep(1000);
		numTries--;
	}

	if (fledgeWindow == NULL)
	return 1;

	GetWindowThreadProcessId(fledgeWindow, &pidToAttach);

	fledgeProcess = OpenProcess(PROCESS_ALL_ACCESS, false, pidToAttach);

	CStringA curDir;
	GetCurrentDirectoryA(MAX_PATH, curDir.GetBuffer(MAX_PATH + 1));
	CString param = curDir.GetBuffer();
	param.Append("\\");
	param.Append(HOOK_DLLNAME);

	pDataRemote = (char*) VirtualAllocEx(fledgeProcess, 0, param.GetAllocLength(), MEM_COMMIT, PAGE_READWRITE);
	if (pDataRemote == NULL)
		return 1;
	if (!WriteProcessMemory(fledgeProcess, pDataRemote, param.GetBuffer(), param.GetAllocLength(), &numBytes))
		return 1;

	DWORD threadID;
	HANDLE fledgeThread = CreateRemoteThread(fledgeProcess, NULL, 0, (LPTHREAD_START_ROUTINE) pfnLoadLibrary, pDataRemote, 0, &threadID);

	CStringA eventName;
	eventName.Format("%s%d", "Global\\FledgeHook", pidToAttach);

	HANDLE hEvent = CreateEventA(NULL, false, false, eventName);

	WaitForSingleObject(hEvent, INFINITE);

	DWORD WM_FLEDGEHOOKDATA = RegisterWindowMessageA("FledgeHookDataEvent");

	PostThreadMessageA(threadID, WM_FLEDGEHOOKDATA, 0, (LPARAM)fledgeWindow);

	curDir.ReleaseBuffer();
	param.ReleaseBuffer();

	CloseHandle(fledgeProcess);

	return 0;
}
