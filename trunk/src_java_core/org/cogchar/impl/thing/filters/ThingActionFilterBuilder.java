/*
 *  Copyright 2013 by The Friendularity Project (www.friendularity.org).
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
package org.cogchar.impl.thing.filters;

import org.appdapter.bind.rdf.jena.assembly.CachingComponentAssembler;
import org.appdapter.bind.rdf.jena.assembly.DynamicCachingComponentAssembler;
import org.appdapter.bind.rdf.jena.assembly.ItemAssemblyReader;
import org.appdapter.core.component.MutableKnownComponent;
import org.appdapter.core.item.Item;
import org.appdapter.core.name.Ident;
import org.cogchar.api.thing.ThingActionFilter;

import com.hp.hpl.jena.assembler.Assembler;
import com.hp.hpl.jena.assembler.Mode;
import com.hp.hpl.jena.rdf.model.Resource;

/**
 * Used by Jena, not meant to be created and used directly.
 * @author logicmoo
 */
public class ThingActionFilterBuilder<MKC extends ThingActionFilter
> extends DynamicCachingComponentAssembler<MKC> {

	public ThingActionFilterBuilder(Resource builderConfRes) {
		super(builderConfRes);
	}

	@Override
	protected Class<MKC> decideComponentClass(Ident ident, Item item) {
		return (Class<MKC>) ThingActionFilterImpl.class;
	}

	/**
	 * This extracts the data from the data source and injects it into a spec
	 * object.
	 * @param thingActionFilter the spec that is being populated with data
	 * @param item provides identity of item from data source
	 * @param asmblr unused parameter
	 * @param mode unused parameter
	 */
	@Override
	protected void initExtendedFieldsAndLinks(MKC thingActionFilterImpl, Item item, Assembler asmblr, Mode mode) {
		ItemAssemblyReader reader = getReader();

	}

}
