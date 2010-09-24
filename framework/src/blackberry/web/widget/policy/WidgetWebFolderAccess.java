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
package blackberry.web.widget.policy;

import java.util.Hashtable;

import net.rim.device.api.web.WidgetAccess;


/**
 * Models a searchable collection of WidgetAccess elements
 */
public class WidgetWebFolderAccess {   
    // Folder structure 
    private Hashtable _pathCollection;
    
    // Depth of longest file path
    private int _maxPathLength;
    
    // Constructor
    // Takes in url of the host
    public WidgetWebFolderAccess() {
        // Assign fields
        _maxPathLength = 0;       
        _pathCollection = new Hashtable();
    }
    
    // Adds WidgetElement to the structure by using the folder path as a key
    // Folder path must not include the scheme or the host
    public void addWidgetAccess(String folderPath, WidgetAccess accessElement){
        // Trim surrounding slashes for consistency
        // The root "/" is a special case that does not need this trimming
        if(!folderPath.equals("/")){
            folderPath = "/" + trimSurroundingSlashes(folderPath);        
        }
        _pathCollection.put(folderPath, accessElement);
        
        // Determine the depth of the path
        _maxPathLength = Math.max(_maxPathLength, determineDepth(folderPath));
    }
    
    
    // Retrieves the access element assigned to the folder path, if it exists
    // Folder path must not include the scheme or the host
    private WidgetAccess fetchWidgetAccess(String folderPath){
        try{
            WidgetAccess accessElement = (WidgetAccess)_pathCollection.get(folderPath);
            return accessElement;
        }
        catch(Exception e){
            // Return null if any problem occurs
            return null;
        }
    }
    
    // Retrieves the access element assigned to the folder path, if it exists
    // Folder path must not include the scheme or the host
    public WidgetAccess getWidgetAccess(String folderPath){     
       String pathOnly = folderPath;
       
       // Remove filename from the path if it exists
       if(folderPath.indexOf('.') != -1){
            pathOnly = excludeFilenameFromPath(folderPath);
       }
       
       int depth = determineDepth(pathOnly);
       return getWidgetAccessRecursively(pathOnly, depth);
    }
    
    private WidgetAccess getWidgetAccessRecursively(String folderPath, int pathLength){
        
        // Check folder path if an entry exists for the full path
        if(_pathCollection.containsKey(folderPath)){
            return fetchWidgetAccess(folderPath);            
        }
        else if(folderPath.equals("")){
            return null;
        }
        else {
            // Truncate the end portion of the path and try again
            int newPathLength = Math.min(_maxPathLength, pathLength-1);
            String newPath = getPath(folderPath, newPathLength);            
            return getWidgetAccessRecursively(newPath, newPathLength);
        }
    }
    
    // Determines the depth of the given path
    // Folder path must not include the scheme or the host
    private int determineDepth(String folderPath){        
             
                     
        int depthCount = 0;
        
        // Replace all backslashes with forward slash
        folderPath = folderPath.replace('\\', '/');       
        
        // Special case: "/" is the given path
        if(folderPath.equals("/")){
            return 0;
        }
        
        folderPath = trimSurroundingSlashes(folderPath);
        
        // Count slashes remaining
        while(folderPath.indexOf("/") != -1 ){
            depthCount += 1;
            
            // Add 1 to skip the slash
            folderPath = folderPath.substring(folderPath.indexOf("/") + 1);
        }
        
        // Add one more for the remaining folder
        depthCount += 1;
        
        return depthCount;

    }
    
    // Parse a folder path up to the desired depth
    private String getPath(String folderPath, int desiredDepth){
       
        int depthCount = 0;
        String builtPath = "";
        
        // Special case: Desired depth is 0
        if(desiredDepth == 0){
            return "/";
        }
       
        // Replace all backslashes with forward slash
        folderPath = folderPath.replace('\\', '/');       
                
        folderPath = trimSurroundingSlashes(folderPath);
        
        // Count slashes remaining
        while(depthCount < desiredDepth){
            depthCount += 1;
            
            // Add 1 to skip the slash
            builtPath += "/" + folderPath.substring(0, folderPath.indexOf('/'));
            folderPath = folderPath.substring(folderPath.indexOf('/') + 1);
        }
        
        return builtPath;
          
    }
    
    // Exclude the filename from the path
    private String excludeFilenameFromPath(String fullPath){
        String folderPath = fullPath;
        
        // Replace all backslashes with forward slash
        folderPath = folderPath.replace('\\', '/');               
        
        // root folder
        if(folderPath.lastIndexOf('/') == 0) {
            return "/";
        }
        else if(folderPath.indexOf('/') != -1){
            folderPath = folderPath.substring(0, folderPath.lastIndexOf('/'));
        }
        return folderPath;
    }
    
    // Removes the start and end slashes from the path
    private String trimSurroundingSlashes(String path){
        
          // Trim starting slash
        if(path.startsWith("/")){
            path = path.substring(1);
        }        
        
        // Trim ending slash
        if(path.endsWith("/")){
            path = path.substring(0, path.length()-1);
        }       
        
        return path;
    }
}



