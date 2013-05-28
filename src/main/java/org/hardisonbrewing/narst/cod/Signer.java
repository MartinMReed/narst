package org.hardisonbrewing.narst.cod;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.math.BigInteger;
import java.net.URL;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.DSAPrivateKeySpec;
import java.util.Properties;

import org.apache.commons.codec.binary.Hex;
import org.codehaus.plexus.util.IOUtil;

public class Signer implements org.hardisonbrewing.narst.Signer {

    public static final byte[] DEFAULT_SALT = { 18, 63, 75, 30, 90, -86, -115, 58 };

    private static final long SERVER_RESPONSE_SIZE = 128;
    private static final long SIGNATURE_HEADER_SIZE = 8;

    private URL url;
    private String signerId;
    private String clientId;
    private String password;
    private File input;
    private String salt;
    private String privateKey;
    private File output;
    private Properties response;

    public boolean sign() throws Exception {

        if ( url == null ) {
            throw new IllegalStateException( "URL is null" );
        }
        if ( signerId == null ) {
            throw new IllegalStateException( "Signer ID is null" );
        }
        if ( clientId == null ) {
            throw new IllegalStateException( "Client ID is null" );
        }
        if ( password == null ) {
            throw new IllegalStateException( "Password is null" );
        }
        if ( input == null ) {
            throw new IllegalStateException( "Input is null" );
        }
        if ( privateKey == null ) {
            throw new IllegalStateException( "PrivateKey is null" );
        }

        PrivateKey privatekey = privateKey();

        byte[] hash = hash( input );
        if ( hash == null ) {
            throw new IllegalStateException( "Exception generating COD hash" );
        }

        SignedProperties signedProperties = new SignedProperties();
        signedProperties.setProperty( "Version", "0" );
        signedProperties.setProperty( "Command", "Signature Request" );
        signedProperties.setProperty( "SignerID", signerId );
        signedProperties.setProperty( "ClientID", clientId );
        signedProperties.setProperty( "Hash", new String( hash ) );
        signedProperties.sign( privatekey );

        Sender sender = null;
        try {
            sender = new Sender( url );
            response = sender.send( signedProperties );
        }
        finally {
            sender.disconnect();
        }

        byte[] signature = getSignature();
        if ( signature != null ) {
            System.out.println( Hex.encodeHex( signature ) );
        }
        return signature != null;
    }

    public byte[] getSignature() {

        if ( response == null ) {
            throw new IllegalStateException( "No response available" );
        }

        String s1 = response.getProperty( "Version" );
        if ( s1 == null ) {
            throw new IllegalStateException( "Version of server incompatible." );
        }
        String s2 = response.getProperty( "Response" );
        if ( s2 == null || !s2.equals( "Signature Response" ) ) {
            throw new IllegalStateException( "Response command invalid." );
        }
        String s3 = response.getProperty( "Confirm" );
        if ( s3 == null ) {
            String s4 = response.getProperty( "Error" );
            if ( s4 == null ) {
                s4 = response.getProperty( "Unknown" );
                if ( s4 == null ) {
                    throw new IllegalStateException( "No confirm or error string in response." );
                }
                else {
                    throw new IllegalStateException( "There was an unknown error sent back from the server." );
                }
            }
            else {
                throw new IllegalStateException( "Error string received." );
            }
        }
        else {
            String s5 = response.getProperty( "Signature" );
            if ( s5 == null ) {
                throw new IllegalStateException( "Signature from server is invalid." );
            }
            return B.b( s5.getBytes() );
        }
    }

    public void write() {

        if ( output == null ) {
            throw new IllegalStateException( "No output specified" );
        }

        byte[] signature = getSignature();
        if ( signature == null ) {
            return;
        }

        boolean write = write( signature );
        if ( write ) {
            System.out.println( "Signature success" );
        }
    }

    private boolean write( byte[] signature ) {

        RandomAccessFile randomaccessfile = null;
        try {
            randomaccessfile = new RandomAccessFile( output, "rw" );
            randomaccessfile.skipBytes( 36 );
            int j1 = randomaccessfile.readUnsignedShort();
            j1 = ( j1 & 0xff ) << 8 | j1 >>> 8;
            if ( j1 < 74 ) {
                throw new IllegalStateException( "Version number incompatible." );
            }
            int k1 = randomaccessfile.readUnsignedShort();
            k1 = ( k1 & 0xff ) << 8 | k1 >>> 8;
            int i2 = randomaccessfile.readUnsignedShort();
            i2 = ( i2 & 0xff ) << 8 | i2 >>> 8;
            randomaccessfile.skipBytes( 2 );
            randomaccessfile.skipBytes( k1 + i2 );
            if ( (long) i2 + SERVER_RESPONSE_SIZE + SIGNATURE_HEADER_SIZE > 64988L ) {
                throw new IllegalStateException( "Appending a signature to the following file will cause it to be larger than the maximum sibling cod file size. Signing will abort." );
            }
            do {
                int j2 = randomaccessfile.readUnsignedByte();
                int l2 = randomaccessfile.readUnsignedByte();
                int i3 = ( l2 << 8 ) + j2;
                int j3 = randomaccessfile.readUnsignedByte();
                int k3 = randomaccessfile.readUnsignedByte();
                int l3 = ( k3 << 8 ) + j3;
                if ( i3 == 1 ) {
                    char ac1[] = new char[4];
                    int i4 = 0;
                    ac1[0] = (char) randomaccessfile.readByte();
                    if ( ac1[0] != 0 ) {
                        i4++;
                    }
                    ac1[1] = (char) randomaccessfile.readByte();
                    if ( ac1[1] != 0 ) {
                        i4++;
                    }
                    ac1[2] = (char) randomaccessfile.readByte();
                    if ( ac1[2] != 0 ) {
                        i4++;
                    }
                    ac1[3] = (char) randomaccessfile.readByte();
                    if ( ac1[3] != 0 ) {
                        i4++;
                    }
                    randomaccessfile.skipBytes( l3 - 4 );
                }
                else {
                    randomaccessfile.skipBytes( l3 );
                }
            }
            while (true);
        }
        catch (EOFException eofexception) {
            // do nothing, this is expected HAHA
        }
        catch (IOException ioexception) {
            return false;
        }
        try {
            randomaccessfile.write( 1 );
            randomaccessfile.write( 0 );
            randomaccessfile.write( signature.length + 4 & 0xff );
            randomaccessfile.write( signature.length + 4 >> 8 & 0xff );
            char ac[] = signerId.toCharArray();
            for (int k2 = 0; k2 < 4; k2++) {
                if ( k2 < ac.length ) {
                    randomaccessfile.write( ac[k2] );
                }
                else {
                    randomaccessfile.write( 0 );
                }
            }
            System.out.println( "Signature length is : " + signature.length );
            randomaccessfile.write( signature );
            randomaccessfile.close();
            return true;
        }
        catch (IOException ioexception1) {
            return false;
        }
    }

    private byte[] hash( File file ) throws NoSuchAlgorithmException, IOException {

        InputStream inputStream = null;

        try {

            inputStream = new FileInputStream( file );

            MessageDigest messageDigest = MessageDigest.getInstance( "SHA" );
            DataInputStream datainputstream = new DataInputStream( inputStream );

            byte[] thirtySix = new byte[36];
            datainputstream.read( thirtySix );
            messageDigest.update( thirtySix );

            int vn1 = datainputstream.readUnsignedByte();
            int vn2 = datainputstream.readUnsignedByte();
            messageDigest.update( (byte) vn1 );
            messageDigest.update( (byte) vn2 );
            int versionNumber = vn2 << 8 | vn1;

            if ( versionNumber < 74 ) {
                throw new IllegalStateException( "Version number incompatible" );
            }

            int cs1 = datainputstream.readUnsignedByte();
            int cs2 = datainputstream.readUnsignedByte();
            messageDigest.update( (byte) cs1 );
            messageDigest.update( (byte) cs2 );
            int codeSize = cs2 << 8 | cs1;

            int ds1 = datainputstream.readUnsignedByte();
            int ds2 = datainputstream.readUnsignedByte();
            messageDigest.update( (byte) ds1 );
            messageDigest.update( (byte) ds2 );
            int dataSize = ds2 << 8 | ds1;

            System.out.println( "Version = " + Integer.toString( versionNumber ) );
            System.out.println( "CodeSize = " + Integer.toString( codeSize ) );
            System.out.println( "DataSize = " + Integer.toString( dataSize ) );

            byte[] two = new byte[2];
            datainputstream.read( two );
            messageDigest.update( two );

            byte[] codeSection = new byte[codeSize];
            datainputstream.read( codeSection );
            messageDigest.update( codeSection );

            byte[] dataSection = new byte[dataSize];
            datainputstream.read( dataSection );
            messageDigest.update( dataSection );

            byte[] digest = new byte[messageDigest.getDigestLength()];
            digest = messageDigest.digest();

            return B.a( digest );
        }
        finally {
            IOUtil.close( inputStream );
        }
    }

    private PrivateKey privateKey() {

        byte[] salt = null;
        if ( this.salt != null ) {
            salt = B.b( this.salt.getBytes() );
        }
        else {
            salt = DEFAULT_SALT;
        }

        try {

            MessageDigest messageDigest = MessageDigest.getInstance( "SHA" );
            PrivateKeyDigest privateKeyDigest = new PrivateKeyDigest( messageDigest );
            byte[] digest = privateKeyDigest.digest( password, salt, 1000, 256 );
            byte[] b = B.b( privateKey.getBytes() );
            A.a( digest, b, 0, b.length );

            BigInteger bi = new BigInteger( b );
            BigInteger bi1 = new BigInteger( "fd7f53811d75122952df4a9c2eece4e7f611b7523cef4400c31e3f80b6512669455d402251fb593d8d58fabfc5f5ba30f6cb9b556cd7813b801d346ff26660b76b9950a5a49f9fe8047b1022c24fbba9d7feb7c61bf83b57e7c6a8a6150f04fb83f6d3c51ec3023554135a169132f675f3ae2b61d72aeff22203199dd14801c7", 16 );
            BigInteger bi2 = new BigInteger( "9760508f15230bccb292b982a2eb840bf0581cf5", 16 );
            BigInteger bi3 = new BigInteger( "f7e1a085d69b3ddecbbcab5c36b857b97994afbbfa3aea82f9574c0b3d0782675159578ebad4594fe67107108180b449167123e84c281613b7cf09328cc8a6e13c167a8b547c8d28e0a3ae1e2bb3a675916ea37f0bfa213562f1fb627a01243bcca4f1bea8519089a883dfe15ae59f06928b665e807b552564014c3bfecf492a", 16 );

            KeyFactory keyfactory = KeyFactory.getInstance( "DSA" );
            return keyfactory.generatePrivate( new DSAPrivateKeySpec( bi, bi1, bi2, bi3 ) );
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new IllegalStateException( "Unable to locate algorithm" );
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

    public void setOutput( File output ) {

        this.output = output;
    }
}
