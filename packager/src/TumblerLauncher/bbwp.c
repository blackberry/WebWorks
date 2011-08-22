/*
* Copyright 2010 Research In Motion Limited.
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
#include <limits.h>
#include <string.h>

#ifdef  __cplusplus
extern "C" {
#endif

#ifndef PATH_MAX
  #define PATH_MAX 1024
#endif

#define LEN_MAX	4096 

#define FALSE 0
#define TRUE 1

#ifdef MAC
#include <mach-o/dyld.h>
#define BIN_BBWP_JAR	"/bin/bbwp.jar"
#define JAVA_EXE		"java"
#define JRE_BIN_JAVA	"/bin/java"
#define BBWP_PROPERTIES	"/bin/bbwp.properties"
#define FAILED_TO_FIND	"Failed to find java"
#define PATH_DIVIDER	':'
#define FOLDER_SLASH_DQ	"/"
#define FOLDER_SLASH_SQ '/'
#else
#define BIN_BBWP_JAR	"\\bin\\bbwp.jar"
#define JAVA_EXE		"java.exe"
#define JRE_BIN_JAVA	"\\jre\\bin\\java.exe"
#define BBWP_PROPERTIES	"\\bin\\bbwp.properties"
#define FAILED_TO_FIND	"Failed to find java.exe"
#define PATH_DIVIDER	';'
#define FOLDER_SLASH_DQ	"\\"
#define FOLDER_SLASH_SQ '\\'
#endif

char g_base_dir[PATH_MAX + 3] = ""; //the absolute path of the running application

/*
*Desc: print an error string in an uniform format
*Param: char *s--the string to be printed
*Return: None.
*/
void printErr(char *s) {
	if (s == NULL) {
		return;
	}

	printf("%-12s\t%s%s\n", "[ERROR]", "<bbwp.exe> ", s);
}

/*
*Desc: secure version of strlen().
          a substitution of s_strnlen() in case gcc for windows doesn't have this function.
*Param: char *s--the string to be trimmed
*           int max--The size of the string buffer.
*Return: string length.
*/
static size_t s_strnlen(const char *s, size_t max) {
    register const char *p;
    for(p = s; *p && max--; ++p);
    return(p - s);
}

/*
*Desc: trim a string
*Param: char *s--the string to be trimmed
*           int strsize--The size of the string buffer.
*Return: the trimmed string. Actually returns the input parameter string s itself.
*/
char *trim(char *s, int strsize) {
	char *ptr = NULL;     
	if (s == NULL) {
		return NULL; // handle NULL string     
	}
	if (!*s) {
		return s;    // handle empty string     
	}

	for (ptr = s + s_strnlen(s, strsize) - 1; (ptr >= s) && ((*ptr)==0x20); --ptr);
	ptr[1] = '\0';
	return s; 
}

/*
*Desc: Used to surround paths with quotes.
*Param: char * desStr --output parameter containing the string surrounded with quotes
*          char* path_spaces -- input parameter containing the path to be quoted.
*          int path_spaces_size -- The size of path_spaces.
*Note: desStr and path_spaces cannot be the same.
*Return: process result. TRUE for success, FALSE for failure.
*/
int surround_quotes(char * desStr, char* path_spaces, int path_spaces_size) {
    if( desStr == NULL ) {
        return FALSE;
    }
    if ( path_spaces == NULL ) {
        return FALSE;
    }
	if(desStr == path_spaces) {
        return FALSE;
	}
	else {
		trim(path_spaces, path_spaces_size);
		strncpy(desStr, "\"", 1);
		strncat(desStr, path_spaces, s_strnlen(path_spaces, path_spaces_size));
		strncat(desStr, "\"", 1);
		strncat(desStr, "\0", 1);
	}
    return TRUE;
}

/*
*Desc:Takes the path of the running application and chops off the name of the application.
*      Thus returning the base directory of tumbler. 
*Param: char *desStr -- output parameter containing the base directory of the running application.
*Return: process result. TRUE for success, FALSE for failure.
*/
int getBaseDir(char *desStr) {
 	char *ptrDir = NULL;
	int newLength = 0;

    if( desStr == NULL ) {
        return FALSE;
    }
#ifdef MAC
	char * _pgmptr = malloc(PATH_MAX);
	char * tmp = malloc(PATH_MAX);
	int size=PATH_MAX;
	_NSGetExecutablePath(tmp,&size);
	realpath(tmp,_pgmptr);
#endif
	ptrDir = strrchr(_pgmptr, FOLDER_SLASH_SQ);
	if(ptrDir == NULL) {
		printErr("Could not get application path.");
		return FALSE;
	}
	newLength = ( (int)ptrDir ) - ( (int)_pgmptr );
	strncpy(desStr, _pgmptr, newLength);
    desStr[newLength] = '\0';
#ifdef MAC
	free(_pgmptr);
	free(tmp);
#endif
	return TRUE;
}

/*
*Desc: Resolves a relative tumbler path to an absolute path.
*Param: char *desStr -- output parameter containing the absolute path.
        char *relative_path -- input augument containing the relative tumbler path
        int relative_path_size -- The size of relative_path. 
*Return: process result. TRUE for success, FALSE for failure.
*/
int to_tumbler_base(char *desStr, const char *relative_path, int relative_path_size) {
    if ( desStr == NULL || relative_path == NULL ) {
        return FALSE;
    }

	strncpy( desStr, g_base_dir, s_strnlen(g_base_dir, sizeof(g_base_dir)) );
	strncat( desStr, relative_path, s_strnlen(relative_path, relative_path_size) );
	strncat(desStr, "\0", 1);
	
	return TRUE;
}

/*
*Desc: Finds the absolute path to java.exe when it is a system variable
*Param: char *desStr -- output parameter containing the absolute path to java.
*Return: process result. TRUE for success, FALSE for failure.
*/
int get_java_from_sys(char *desStr) {
	const char cstJavaExe[] = JAVA_EXE;
    char *java_home = NULL;
	char *java_home_t = NULL;
	size_t java_home_len;
    char *substr1 = NULL;
    char *substr2 = NULL;
    char tmpstr[PATH_MAX];
    FILE *fp;
    int bFlag = FALSE; //return value

	java_home_t = getenv("PATH");
	if (java_home_t == NULL)
	{
	  printErr("Failed to allocate memory");
	  return FALSE;
	}
	java_home_len = s_strnlen(java_home_t, PATH_MAX);
	java_home = (char*)malloc(java_home_len+1);

	strncpy(java_home,java_home_t,java_home_len);
	// Get the value of the PATH environment variable.
    if( java_home == NULL) {
        return FALSE;
    }

    //Search in the environment variable PATH inversly to find the java path
    substr1 = &(java_home[java_home_len]);
    substr2 = java_home;
    while(substr2 != NULL) {
        substr2 = strrchr(java_home, PATH_DIVIDER);
        memset(tmpstr, 0, sizeof(tmpstr));
        if( substr2 != NULL ) {
            strncpy(tmpstr, substr2+1, substr1 - substr2);
			java_home_len = substr2 - java_home;
            java_home[java_home_len] = '\0';
            substr1 = substr2;
        }
        else {
            strncpy(tmpstr, java_home, java_home_len);
        }

        if(tmpstr[s_strnlen(tmpstr, sizeof(tmpstr)) - 1] != FOLDER_SLASH_SQ) {
            strncat(tmpstr, FOLDER_SLASH_DQ, 1);
        }
        strncat(tmpstr, cstJavaExe, strlen(cstJavaExe));
        if( (fp=fopen(tmpstr, "rb")) != NULL ) {
            fclose(fp);
            fp = NULL;
            strncpy(desStr, tmpstr, s_strnlen(tmpstr, sizeof(tmpstr)));
            desStr[s_strnlen(tmpstr, sizeof(tmpstr))] = '\0';
            bFlag = TRUE;
            break;
        }
   }
   if (java_home != NULL) free(java_home);
   return bFlag;
}

/*
*Desc: Finds the absolute path to java.exe incase it isn't a system variable
*Param: char *desStr -- output parameter containing the absolute path to java.
*       int desStr_size -- The size of desStr.
*Return: process result. TRUE for success, FALSE for failure.
*/
int get_java_home(char *desStr, int desStr_size) {
	const char cstJavaPath[] = JRE_BIN_JAVA;
	const char cstPropPath[] = BBWP_PROPERTIES;

	char path_to_bbwp_properties[PATH_MAX + 3];
	char *input = NULL;
    int bFlag = FALSE; //bool flag as return value
	FILE *fp = NULL;

    if ( desStr == NULL ) {
        return FALSE;
    }

	memset(path_to_bbwp_properties, 0, sizeof(path_to_bbwp_properties));
	
    to_tumbler_base(path_to_bbwp_properties, cstPropPath, strlen(cstPropPath));
    fp = fopen( path_to_bbwp_properties, "r" );
	if(fp == NULL) {
		printErr("Failed to find bbwp.properties");
        return FALSE;
	}

    // Read the entire file into a string.
    while(1) {
	    long buffersize = 0;

	    // Get the size of the file.
        if (fseek(fp, 0L, SEEK_END) != 0) {
            printErr("Error seeking end of the file bbwp.properties");
            break;
        }
		buffersize = ftell(fp);
	    if (buffersize == -1) { 
		    printErr("bbwp.properties is invalid");
            break;
	    }
	    // Allocate the buffer to that size.
	    input = malloc(sizeof(char) * (buffersize + 1));

	    // Go back to the start of the file.
	    fseek(fp, 0L, SEEK_SET);

	    // Read the entire file into the string.
        {
	        unsigned int newLen = (unsigned int)fread(input, sizeof(char), buffersize, fp);
	        if ( newLen == 0 ) {
		        printErr("Error reading file bbwp.properties");
                input = "";
                break;
	        } else {
		        input[++newLen] = '\0';
	        }
        }

        // Read file successfully
        bFlag = TRUE;
        break;
    }//while end
    fclose(fp);
    fp = NULL;
	
    // Get java home path from bbwp.properties
    if( bFlag == TRUE ) {
	    char *temp = NULL;
        char *tmp_javahome = NULL;
	    char *end = NULL;

	    temp = strstr(input, "<wcp>");
		if(temp != NULL){
			tmp_javahome = strstr(temp, "<java>");
            if (tmp_javahome != NULL) {
			    tmp_javahome += strlen("<java>");
			    end = strstr(tmp_javahome, "</java>");
			    if (end != NULL) {
                    int pathLen = (int)end - (int)tmp_javahome;
				    *end = '\0';
			        strncpy(desStr, tmp_javahome, pathLen);
			        desStr[pathLen] = '\0';
			    }
                else { //<java> is found but </java> is not found.
		            bFlag = FALSE;
                    printErr("Invalid bbwp.properties: </java> was not found.");
                }
            }
            else { //<java> is not found
                end = strstr(temp, "<java") ;
                if( end != NULL
					&& strstr(end + strlen("<java"), "/>") != NULL) { //treat "<java/>" as empty path string
                    desStr[0] = '\0';
                }
                else { //neither <java> nor <java/> is found.
			        bFlag = FALSE;
                    printErr("Invalid bbwp.properties: <java> or <java/> was not found.");
                }
            }
		}
		else {
			bFlag = FALSE;
            printErr("Invalid bbwp.properties: <wcp> was not found.");
		}
    }

	if(input != NULL)
	    free(input); 

     // Varify if java.exe is available
    if( bFlag == TRUE ) {
        trim(desStr, desStr_size);
	    if ( desStr[0] != '\0' ) { // java home path is indicated in bbwp.properties
	        strncat(desStr, cstJavaPath, strlen(cstJavaPath));
            strncat(desStr, "\0", 1);
            // varify if java.exe is available
            if( (fp=fopen(desStr, "rb")) == NULL ) {
                printErr(FAILED_TO_FIND);
                bFlag = FALSE;
            }
            else {
                fclose(fp);
                fp = NULL;
            }
	    }
        else { // java path in envirionment variable
            if (get_java_from_sys(desStr) == FALSE) {
                printErr(FAILED_TO_FIND);
                bFlag = FALSE;
            }
        }
    }

    return bFlag;
}

/*
*Desc: Concatenates a string which calls java -jar on bbwp.jar + parameters
*      All paths with spaces are surrounded with quotes so they are safe to call in system().
*Param: char *desStr -- output parameter containing the system command string.
*       int length -- input parameter containing the number of application arguments.
*       char **strArray -- input parameter containing the application arguments.
*Return: process result. TRUE for success, FALSE for failure.
*/
int generate_system_call(char *desStr, int length, char **strArray) {
    const char cststrJar[] = " -jar ";

	char java_path[PATH_MAX + 3];
	char full_java_path[PATH_MAX + 3];
	char path_to_bbwp_jar[PATH_MAX + 3];
    char path_to_bbwp_jar_tmp[PATH_MAX + 3];
	int totalSize = 0;
	char *temp = NULL;
    int i = 0;
    FILE *fp = NULL;

	memset(java_path, 0, sizeof(java_path));
	memset(full_java_path, 0, sizeof(full_java_path));
	memset(path_to_bbwp_jar, 0, sizeof(path_to_bbwp_jar));
	memset(path_to_bbwp_jar_tmp, 0, sizeof(path_to_bbwp_jar_tmp));
	
	// Get home path of java.exe
    if ( get_java_home(java_path, sizeof(java_path)) == FALSE ) {
        return FALSE;
    }
    if ( surround_quotes( full_java_path, java_path, s_strnlen(java_path, sizeof(java_path)) ) == FALSE ) {
        printErr("Failde to surround parameter with quotes.");
        return FALSE;
    }
    // Get home path of bbwp.jar
    to_tumbler_base( path_to_bbwp_jar_tmp, BIN_BBWP_JAR, strlen(BIN_BBWP_JAR) );
    // Validate if bbwp.jar is available
    fp = fopen(path_to_bbwp_jar_tmp, "rb");
    if ( fp == NULL ) {
        printErr("Failed to find bbwp.jar");
        return FALSE;
    }
    else {
        fclose(fp);
        fp = NULL;
        if( surround_quotes( path_to_bbwp_jar, path_to_bbwp_jar_tmp, sizeof(path_to_bbwp_jar_tmp) ) == FALSE ) {
            printErr("Failde to surround parameter with quotes.");
            return FALSE;
        }
    }

    totalSize = s_strnlen(full_java_path, sizeof(full_java_path)) + strlen(cststrJar) + s_strnlen(path_to_bbwp_jar, sizeof(path_to_bbwp_jar)) +1 ; //Size of the string plus a null character
	temp = malloc( sizeof(char) * totalSize );
    memset(temp, 0, sizeof(char) * totalSize);

    strncpy(temp, full_java_path, s_strnlen(full_java_path, sizeof(full_java_path))); //"java.exe"
	strncat(temp, cststrJar, strlen(cststrJar));                //" -jar "
	strncat(temp, path_to_bbwp_jar, s_strnlen(path_to_bbwp_jar, sizeof(path_to_bbwp_jar))); //"bbwp.jar"

    //add arguments to the command string
	for ( i=1; i<length; i++ ) {
		char args[PATH_MAX + 3] = "";
		if( surround_quotes( args, strArray[i], PATH_MAX ) == FALSE) {
			printErr("Failde to surround parameter with quotes.");
			return FALSE;
		}
		totalSize += s_strnlen(args, PATH_MAX-1) + 1; //Size of the string plus a space character.
		temp = (char*)realloc(temp, sizeof(char)*totalSize);
		if(temp == NULL) {
			printErr("Could not reallocate memory.");
			return FALSE;
		}
		strncat(temp," ", 1);
		strncat(temp, args, s_strnlen(args, sizeof(args)));
	}
	temp[totalSize - 1] = '\0';
#ifdef MAC
	strncpy(desStr,temp, totalSize);
#else
    if ( surround_quotes(desStr, temp, sizeof(char)*totalSize) == FALSE ) {
		printErr("Failde to surround command with quotes.");
		return FALSE;
    }
#endif
    if( temp != NULL )
        free(temp);

	return TRUE;
}

int main(int argc, char **argv)
{
	char system_call[LEN_MAX] = ""; 

	memset(system_call, 0, sizeof(system_call));
	
	//set value of the global variable -- home path of current running application.
    if ( getBaseDir(g_base_dir) == FALSE ) {
        return -1;
    }

	//Generate command like this: 
	//"java" -jar "C:\Program Files\Research In Motion\bin\bbwp.jar" "E:\Package\VibrateTest.zip"
    if ( generate_system_call(system_call, argc, argv) == FALSE ) {
        return -1;
    }
    if( system_call[0] == '\0' ) {
        printErr("Invalid command");
        return -1;
    }

    // Execute the command
    system(system_call);
    return 0;
}

#ifdef  __cplusplus
}
#endif
