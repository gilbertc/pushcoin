/*
  Copyright (c) 2013 PushCoin Inc

  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.
  
  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.
  
  You should have received a copy of the GNU General Public License
  along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package com.pushcoin.icebreaker;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.NoSuchAlgorithmException;

public class CryptoHelper
{
	static public class DsaKeyPair
	{
		PrivateKey privateKey;
		PublicKey publicKey;
	}

	static public DsaKeyPair generateDsaKeyPair() 
	{
		try 
		{
			// Get the DSA key factory
			KeyPairGenerator keyGen = KeyPairGenerator.getInstance("DSA");
			keyGen.initialize(Conf.DSA_KEY_LENGTH);
			KeyPair keypair = keyGen.genKeyPair();

			DsaKeyPair pair = new DsaKeyPair();

			// Get the private key
			pair.privateKey = keypair.getPrivate();
			pair.publicKey = keypair.getPublic();
			return pair;
		}
		catch( NoSuchAlgorithmException e ) {
			throw new RuntimeException( e );
		}
	}
}

