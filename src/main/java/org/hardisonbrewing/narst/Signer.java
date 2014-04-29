/**
 * Copyright (c) 2012 Martin M Reed
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
