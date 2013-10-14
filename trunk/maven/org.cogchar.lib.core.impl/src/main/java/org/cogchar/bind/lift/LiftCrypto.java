/*
 *  Copyright 2012 by The Cogchar Project (www.cogchar.org).
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.cogchar.bind.lift;

import java.security.MessageDigest;
import java.security.SecureRandom;
import org.slf4j.Logger;
import org.appdapter.core.log.BasicDebugger;

/**
 * A class for basic cryptography utilities, mainly for handling password hashes at this point
 * 
 * I believe there are more "Lifty" ways to do these tasks which I may switch to eventually.
 *
 * @author Ryan Biggs <rbiggs@hansonrobokind.com>
 */


public class LiftCrypto extends BasicDebugger {
	
	private static final String ENCODING_SCHEME = "UTF-8";
	private static final String HASH_TYPE = "SHA-256";
	private static final int SALT_BITS = 256; // Should match length of hash output and be an even number of bytes
	private static final String RNG_ALGORITHM = "SHA1PRNG";
	
	private static final Logger theLogger = getLoggerForClass(LiftCrypto.class);
	
	private SecureRandom myGenerator;
	
	// Main method is a utility to compute password salts and hashes
	public static void main(String[] args) {
		String STRING_TO_HASH = "password";
		String toHash = STRING_TO_HASH;
		if (args.length != 0) {
			toHash = args[0];
		}
		LiftCrypto aLiftCrypto = new LiftCrypto();
		String salt = aLiftCrypto.getSalt();
		byte[] hash = LiftCrypto.getHash(toHash, salt);
		String hashString = LiftCrypto.getStringFromBytes(hash);
		System.out.println("Hashing string: " + toHash + ": Salt is " + salt + "; salted and hashed string is " + hashString);
		//System.out.println("String length is " + toHash.length() + "; salt length is " + salt.length() + "; hashed string length is " + hashString.length()); // TEST ONLY
	}
	
	static byte[] getHash(String plainText, String salt) {
		byte[] output = null;
		try {
			String text = salt + plainText;
			MessageDigest md = MessageDigest.getInstance(HASH_TYPE);
			md.update(text.getBytes(ENCODING_SCHEME));
			output = md.digest();
		} catch (Exception e) {
			theLogger.error("Exception attempting to compute hash from string: {}", e);
		}
		return output;
	}
	
	static String getStringFromBytes(byte[] bytes) {
		String hexString = "";
		String byteString = null;
		for (int i=0; i<bytes.length; i++) {
			Byte nextByte = bytes[i];
			byteString = Integer.toHexString(nextByte.intValue() & 0xFF);
			if (byteString.length() == 1) {
				byteString = "0" + byteString;
			}
			//logInfo("For byte " + i + ", prepending a string " + byteString); // TEST ONLY
			hexString = byteString + hexString;
		}
		return hexString;
	}
	
	String getSalt() {
		String salt = "";
		try {
			if (myGenerator == null) {
				myGenerator = SecureRandom.getInstance(RNG_ALGORITHM);
			}
		} catch (Exception e) {
			logError("Exception attempting to create Cryptographically Secure Pseudo-Random Number Generator: " + e);
		}
		if (myGenerator != null) {
			int numBytes = SALT_BITS / 8; // SALT_BITS should be a multiple of 8, otherwise this will round down to next even # of bytes
			byte[] saltBytes = new byte[numBytes];
			myGenerator.nextBytes(saltBytes);
			salt = getStringFromBytes(saltBytes);
		}
		return salt;
	}
	
}
