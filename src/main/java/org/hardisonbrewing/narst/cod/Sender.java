package org.hardisonbrewing.narst.cod;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Properties;

import org.codehaus.plexus.util.IOUtil;

class Sender {

    private HttpURLConnection httpURLConnection;

    private final URL url;

    public Sender(URL url) {

        this.url = url;
    }

    private void connect() throws IOException {

        httpURLConnection = (HttpURLConnection) url.openConnection();
        httpURLConnection.setRequestMethod( "POST" );
        httpURLConnection.setUseCaches( false );
        httpURLConnection.setDoInput( true );
        httpURLConnection.setDoOutput( true );
        httpURLConnection.connect();
    }

    public Properties send( Properties properties ) throws IOException {

        connect();

        OutputStream outputStream = null;
        try {
            outputStream = httpURLConnection.getOutputStream();
            properties.store( outputStream, "CSC File according to the specs for the Signature Tool." );
        }
        finally {
            IOUtil.close( outputStream );
        }

        return receive();
    }

    private Properties receive() throws IOException {

        InputStream inputStream = null;
        try {
            inputStream = httpURLConnection.getInputStream();
            int responseCode = httpURLConnection.getResponseCode();
            if ( responseCode != 200 ) {
                throw new IOException( "Bad response code: " + responseCode );
            }
            Properties properties = new Properties();
            properties.load( inputStream );
            return properties;
        }
        finally {
            IOUtil.close( inputStream );
        }
    }

    public void disconnect() {

        if ( httpURLConnection != null ) {
            try {
                httpURLConnection.disconnect();
            }
            catch (Exception e) {
                // do nothing
            }
            httpURLConnection = null;
        }
    }
}
