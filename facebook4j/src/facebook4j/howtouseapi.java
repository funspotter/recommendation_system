package facebook4j;

import facebook4j.auth.AccessToken;

public class howtouseapi {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		// Generate facebook instance.
		Facebook facebook = new FacebookFactory().getInstance();
		// Use default values for oauth app id.
		facebook.setOAuthAppId("", "");
		// Get an access token from: 
		// https://developers.facebook.com/tools/explorer
		// Copy and paste it below.
		String accessTokenString = "CAACEdEose0cBABVSEfLdmPCVpIjZCTlce5alSf0DDfhGaJEzLfKB4Acl6TJN74lNTxUxVmZBSZBRq10hRUmjLcHWzgqRNeMiM6g3bbG86zx248S12ZAA3ojvt1dERfo3scIxKk2jnYWfqVZBCcc5wfChRS6cvZCbxDAipIRBpZAZCRoZA77YU6glarPzA7ZC2dElchJbaYo7ogIwZDZD";
		AccessToken at = new AccessToken(accessTokenString);
		// Set access token.
		facebook.setOAuthAccessToken(at);

		// We're done.
		// Write some stuff to your wall.
		try {
			facebook.postStatusMessage("Wow, it works...");
		} catch (FacebookException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
