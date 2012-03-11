package org.hardisonbrewing.narst.bar;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;

public class Signer implements org.hardisonbrewing.narst.Signer {

    private URL url;
    private String signerId;
    private String clientId;
    private String password;
    private File input;
    private String salt;
    private String privateKey;

    public boolean sign() throws Exception {

        if ( url != null ) {
            throw new IllegalStateException( "URL is not supported" );
        }
        if ( signerId == null ) {
            throw new IllegalStateException( "Signer ID is null" );
        }
        if ( clientId != null ) {
            throw new IllegalStateException( "Client ID is not supported" );
        }
        if ( password == null ) {
            throw new IllegalStateException( "Password is null" );
        }
        if ( input == null ) {
            throw new IllegalStateException( "Input is null" );
        }
        if ( salt != null ) {
            throw new IllegalStateException( "Salt is not supported" );
        }
        if ( privateKey != null ) {
            throw new IllegalStateException( "PrivateKey is not supported" );
        }

        writeDescriptor();
        executePackager();

        if ( executeSigner() ) {
            return verify();
        }

        return false;
    }

    private String trimInt( String str ) {

        return Integer.toString( Integer.parseInt( str ) );
    }

    private void writeDescriptor() {

        InputStream inputStream = null;
        OutputStream outputStream = null;

        try {

            inputStream = getClass().getResourceAsStream( "/bar-descriptor.xml" );

            String currentTimeMillis = Long.toString( System.currentTimeMillis() );

            StringBuffer versionNumberBuffer = new StringBuffer();
            versionNumberBuffer.append( trimInt( currentTimeMillis.substring( 0, 3 ) ) );
            versionNumberBuffer.append( "." );
            versionNumberBuffer.append( trimInt( currentTimeMillis.substring( 3, 6 ) ) );
            versionNumberBuffer.append( "." );
            versionNumberBuffer.append( trimInt( currentTimeMillis.substring( 6, 9 ) ) );

            String descriptorXml = IOUtil.toString( inputStream );
            String versionNumber = versionNumberBuffer.toString();
            String buildId = currentTimeMillis.substring( 9 );

            descriptorXml = descriptorXml.replace( "$versionNumber", versionNumber );
            descriptorXml = descriptorXml.replace( "$buildId", trimInt( buildId ) );

            File file = new File( input.getParent(), "bar-descriptor.xml" );
            if ( file.exists() ) {
                file.delete();
            }
            file.createNewFile();

            outputStream = new FileOutputStream( file );
            outputStream.write( descriptorXml.getBytes() );
        }
        catch (Exception e) {
            throw new IllegalStateException( e );
        }
        finally {
            IOUtil.close( outputStream );
            IOUtil.close( inputStream );
        }
    }

    private boolean executePackager() {

        try {
            Commandline commandLine = new Commandline();
            commandLine.setExecutable( "blackberry-nativepackager" );
            commandLine.setWorkingDirectory( input.getParent() );
            commandLine.createArg().setValue( "-package" );
            commandLine.createArg().setValue( input.getPath() + ".bar" );
            commandLine.createArg().setValue( "bar-descriptor.xml" );
            commandLine.createArg().setValue( input.getPath() );
            System.out.println( commandLine.toString() );
            return CommandLineUtils.executeCommandLine( commandLine, new SystemStreamConsumer(), new SystemStreamConsumer() ) == 0;
        }
        catch (Exception e) {
            return false;
        }
    }

    private boolean executeSigner() {

        try {
            Commandline commandLine = new Commandline();
            commandLine.setExecutable( "blackberry-signer" );
            commandLine.setWorkingDirectory( input.getParent() );
            commandLine.createArg().setValue( "-storepass" );
            commandLine.createArg().setValue( password );
            commandLine.createArg().setValue( input.getPath() + ".bar" );
            System.out.println( commandLine.toString() );
            return CommandLineUtils.executeCommandLine( commandLine, new SystemStreamConsumer(), new SystemStreamConsumer() ) == 0;
        }
        catch (Exception e) {
            return false;
        }
    }

    private boolean verify() {

        try {
            Commandline commandLine = new Commandline();
            commandLine.setExecutable( "blackberry-signer" );
            commandLine.setWorkingDirectory( input.getParent() );
            commandLine.createArg().setValue( "-verify" );
            commandLine.createArg().setValue( input.getPath() + ".bar" );
            System.out.println( commandLine.toString() );
            return CommandLineUtils.executeCommandLine( commandLine, new SystemStreamConsumer(), new SystemStreamConsumer() ) == 0;
        }
        catch (Exception e) {
            return false;
        }
    }

    public void setUrl( URL url ) {

        this.url = url;
    }

    public void setSignerId( String signerId ) {

        this.signerId = signerId;
    }

    public void setClientId( String clientId ) {

        this.clientId = clientId;
    }

    public void setPassword( String password ) {

        this.password = password;
    }

    public void setInput( File input ) {

        this.input = input;
    }

    public void setSalt( String salt ) {

        this.salt = salt;
    }

    public void setPrivateKey( String privateKey ) {

        this.privateKey = privateKey;
    }
}
