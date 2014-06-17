/*
 *  Copyright 2014 by The Cogchar Project (www.cogchar.org).
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

package org.cogchar.blob.absorb

/**
 * @author Stu B. <www.texpedient.com>
 */
/*
 * Absorbers are opaque entry point objects accepting glue.ai data records.
 * They make data available for abstract routing across glue.ai.
 * They are a kind of sink, from the point of view of the caller = data supplier.
 * However, even after data disappears into the sink, the caller/supplier often
 * wants to keep a reference to it.  This is done using an opaque receipt result.
 * An absorber can provide a receipt for each record or for the starting point of a stream of records.
 * The receipt is then usable to track the data for followup and debugging, when needed.

 * Absorbtion is a technique facilitating sensor fusion by systems scanning the absorbed data.
 * 
 * Absorbtion is not appropriate conveyance for commercial transactions, medical data, business records,
 * or otherwise sensitive data.  
 * 
 * Absorbtion should almost always succeed, however it could in practice throw exceptions.
 * 
 * Absorber is autonomous in how it treats the inbound data.   It may discard it.
 * 
  * Absorbers are a kind of matcher, and may delegate to other absorbers.

 * Issues that subclasses of absorber deal with:
 
 * Normally the data is flat and presumed to be unchanging after absorption.
 * Depending on when first serialization of the data occurs, this assumption may
 * be questioned.
 */
class AbsorbersProto {

}
/**
 * Does not provide a receipt.
 */
trait QuietAbsorber[In] {
	def absorbQuietly(inPiece : In)
}
/**
 * Provides a receipt.
 */

trait ConfirmedAbsorber[In,Rcpt] {
	def absorbAndProduceReceipt(inPiece : In) : Rcpt 
	
	def absorbQuietly(inPiece : In) {
		absorbAndProduceReceipt(inPiece)  // Naive impl:  Simply ignore result value!
	}
}

class UuidRcpt {
	
}
abstract class AbsorberYieldingUuid

// StreamDesc is anything that we need to know when the input absorbption stream starts, including info about
// the stream records.
class StreamStartAbsorber[StreamDesc] extends ConfirmedAbsorber[StreamDesc, UuidRcpt] {
	// Effectively registers a new stream of absorption, keyed by UuidRcpt.
	override def absorbAndProduceReceipt(streamDesc : StreamDesc) : UuidRcpt = {
		// u should represent a stream start, usable in future absorptions (by other absorbers)
		val u = new UuidRcpt
		u
	}
	
}
/**
 * Verbose in the sense that it produces UuidRcpts for all records received on the stream.
 * In this form we do *not* statically know the implied StreamDesc type, but presumably we 
 * can dynamically go find data that tells us what we need to know in order to absorb streamRecs.
 */
class VerboseStreamingAbsorber[StreamRec](val myStreamRcpt : UuidRcpt) extends ConfirmedAbsorber[StreamRec, UuidRcpt] {

	override def absorbAndProduceReceipt(streamRec : StreamRec) : UuidRcpt = {
		// Absorb the streamRec in the context of stream indicated by myStreamRcpt.
		val u = new UuidRcpt
		u		
	}
}
/**
 * Porky collects and keep all records supplied.  Ooooh, gonna be fat in a HURRY!	
 */
class PorkyAbsorber {
	
}

/**
 * Can access Avro metadata to efficiently absorb data in avro-compliant records.
 * ("Efficiently" compared to, say, Java reflection).
 */
class AvroAwareAbsorber {
	
}