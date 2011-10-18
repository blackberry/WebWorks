package net.rim.tumbler.os;

/**
 * Utility methods for determining the running operating system.
 */
public final class OperatingSystems {

    /**
     * Determines whether the running operating system is a Microsoft Windows OS.
     * 
     * @return true if the <code>os.name</code> system property contains "win".
     */
    public static final boolean isWindows() {

        String osName = System.getProperty( "os.name" ).toLowerCase();
        return osName.contains( "win" ) && !osName.contains( "darwin" );
    }
}
