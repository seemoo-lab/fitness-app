/*
Copyright (c) 2007-2009, Yusuke Yamamoto
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:
    * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright
      notice, this list of conditions and the following disclaimer in the
      documentation and/or other materials provided with the distribution.
    * Neither the name of the Yusuke Yamamoto nor the
      names of its contributors may be used to endorse or promote products
      derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY Yusuke Yamamoto ``AS IS'' AND ANY
EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL Yusuke Yamamoto BE LIABLE FOR ANY
DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
package com.fitbit.api.client.http;

import com.fitbit.api.FitbitAPIException;

/**
 * Follows the naming convention used at
 *
 * http://tools.ietf.org/html/draft-hammer-oauth-10#section-2.1
 *
 * to represent a set of temporary credentials obtained from the server by making an authenticated HTTP
 * "POST" request to the Temporary Credential Request endpoint.
 *
 * 1. The client obtains a set of temporary credentials from the server (in the form of an identifier and
 *    shared-secret). The temporary credentials are used to identify the access request throughout the authorization
 *    process.
 * 2. The resource owner authorizes the server to grant the client's access request (identified by the temporary
 *    credentials).
 * 3. The client uses the temporary credentials to request a set of token credentials from the server, which will
 *    enable it to access the resource owner's protected resources.
 */
public class TempCredentials extends OAuthToken {
    private HttpClient httpClient;
    private static final long serialVersionUID = -8214365845469757952L;

    TempCredentials(Response res, HttpClient httpClient) throws FitbitAPIException {
        super(res);
        this.httpClient = httpClient;
    }

    TempCredentials(String token, String tokenSecret) {
        super(token, tokenSecret);
    }

    public String getAuthorizationURL() {
        return httpClient.getAuthorizationURL() + "?oauth_token=" + getToken();
    }

    public String getAuthenticationURL() {
        return httpClient.getAuthenticationRL() + "?oauth_token=" + getToken();
    }

    public AccessToken getAccessToken() throws FitbitAPIException {
        return httpClient.getOAuthAccessToken(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        TempCredentials that = (TempCredentials) o;

        return !(httpClient != null ? !httpClient.equals(that.httpClient) : that.httpClient != null);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (httpClient != null ? httpClient.hashCode() : 0);
        return result;
    }
}