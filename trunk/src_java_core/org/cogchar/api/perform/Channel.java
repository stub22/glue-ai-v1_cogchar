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
package org.cogchar.api.perform;

import org.appdapter.core.item.Ident;
import java.util.List;

/**
 * @author Stu B. <www.texpedient.com>
 */
public interface Channel<M extends Media, Time> // , C extends Channel<M, Time, C>> {
{
	public enum Status {
		INIT,
		IDLE,
		PERFORMING,
		ERROR
	}
	public Status getStatus();
	
	public Ident getIdent();
	public String getName();
	
	public int	getMaxAllowedPerformances();
	
	public Performance<M, Time> makePerformanceForMedia(M media);
	
	public boolean schedulePerfAction(Performance<M, Time> perf, Performance.Action action, Time actionTime);


	public interface Text<Time> extends Channel<Media.Text, Time> {
		
	}
	public interface Framed<Time, F> extends Channel<Media.Framed<F>, Time> {
		
	}
	
	public class Bank<Time> {
		public	List<Channel<?, Time>>	myWildcardChannels;
		public void test(Text<Time> textChan) { 
			myWildcardChannels.add(textChan);
		}
	}
}
