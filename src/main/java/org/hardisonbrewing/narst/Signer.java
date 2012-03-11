package org.hardisonbrewing.narst;

import java.io.File;
import java.net.URL;

public interface Signer {

    public boolean sign() throws Exception;

    public void setUrl( URL url );

    public void setSignerId( String signerId );

    public void setClientId( String clientId );

    public void setPassword( String password );

    public void setInput( File input );

    public void setSalt( String salt );

    public void setPrivateKey( String privateKey );
}
