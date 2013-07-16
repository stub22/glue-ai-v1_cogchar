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

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import org.appdapter.bind.rdf.jena.assembly.DynamicCachingComponentAssembler;
import org.appdapter.bind.rdf.jena.assembly.ItemAssemblyReader;
import org.appdapter.bind.rdf.jena.model.JenaLiteralUtils;
import org.appdapter.core.item.Item;
import org.appdapter.core.item.JenaResourceItem;
import org.appdapter.core.log.Debuggable;
import org.appdapter.core.name.FreeIdent;
import org.appdapter.core.name.Ident;
import org.cogchar.api.thing.ThingActionFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.assembler.Assembler;
import com.hp.hpl.jena.assembler.Mode;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;

/**
 * Used by Jena, not meant to be created and used directly.
 * @author logicmoo
 */
public class ThingActionFilterBuilder<MKC extends ThingActionFilter> extends DynamicCachingComponentAssembler<MKC> {

	public static Class<ThingActionFilterImpl> TAClass = ThingActionFilterImpl.class;
	static ThingActionFilterBuilder oneInstance;
	Resource builderConfResource;
	public static Logger theLogger = LoggerFactory.getLogger(ThingActionFilterBuilder.class);

	public ThingActionFilterBuilder(Resource builderConfRes) {
		super(builderConfRes);
		builderConfResource = builderConfRes;
		oneInstance = this;
	}

	public static ThingActionFilter makeTAFilter(Ident mainIdent) {
		return oneInstance.findOrCreate(mainIdent);
	}

	public ThingActionFilter findOrCreate(Ident compID) {
		ThingActionFilter taf = getCache().getCachedComponent(compID);
		if (taf != null)
			return taf;
		getLogger().debug("Assembler[{}] is opening component at: {}", this, compID);
		Resource res = builderConfResource.getModel().createResource(compID.getAbsUriString());
		JenaResourceItem wrapperItem = new JenaResourceItem(res);
		MKC comp = fetchOrMakeComponent(wrapperItem, wrapperItem, this, Mode.REUSE);
		return comp;
	}

	private Model getModel() {
		return builderConfResource.getModel();
	}

	@Override protected Class<MKC> decideComponentClass(Ident ident, Item item) {
		return (Class<MKC>) TAClass;
	}

	/**
	 * This extracts the data from the data source and injects it into a spec
	 * object.
	 * @param thingActionFilter the spec that is being populated with data
	 * @param item provides identity of item from data source
	 * @param asmblr unused parameter
	 * @param mode unused parameter
	 */
	@Override protected void initExtendedFieldsAndLinks(MKC thingActionFilterImpl, Item item, Assembler asmblr, Mode mode) {
		ItemAssemblyReader reader = getReader();
		Class tafc = thingActionFilterImpl.getClass();
		JenaResourceItem resourceItem = null;
		if (item instanceof JenaResourceItem) {
			resourceItem = (JenaResourceItem) item;
			Map<Property, List<RDFNode>> properties = resourceItem.getPropertyMap();
			for (Map.Entry<Property, List<RDFNode>> e : properties.entrySet()) {
				//rdf:type is used by the jena assembler and we should ignore it                
				if("type".equals(e.getKey().getLocalName())){
                    continue;
                }
				try {
					setObjectFieldValue(thingActionFilterImpl, tafc, e.getKey().getLocalName(), e.getValue(), asmblr, mode, true, true);
				} catch (Throwable t) {
					t.printStackTrace();
					throw Debuggable.reThrowable(t);
				}
			}
			return;
		}
		BeanInfo info;
		try {
			info = Introspector.getBeanInfo(tafc);
			Ident mainIdent = item.getIdent();

			for (PropertyDescriptor pd : info.getPropertyDescriptors()) {
				String pdn = pd.getName();
				pdn = pdn.substring(0, 1).toLowerCase() + pdn.substring(1);
				Class pdt = pd.getPropertyType();
				setObjectField(thingActionFilterImpl, item, reader, pd.getWriteMethod(), pdn, pdt, asmblr, mode);
			}
		} catch (Throwable e) {
			throw Debuggable.reThrowable(e);
		}
	}

	private void setObjectFieldValue(Object object, Class c, String localName,
			List<RDFNode> e, Assembler assembler, Mode mode, boolean replaceCollections, boolean okIfFieldNotFound) {

		try {
			Field f = getDeclaredField(c, localName);
			f.setAccessible(true);
			Object value = JenaLiteralUtils.convertList(e, f.getType());
			f.set(object, value);
		} catch (NoSuchFieldException nsf) {
			if (okIfFieldNotFound)
				return;
			throw Debuggable.reThrowable(nsf);
		} catch (Throwable t) {
			throw Debuggable.reThrowable(t);
		}
	}

	private Field getDeclaredField(Class c, String name) throws SecurityException, NoSuchFieldException {
		NoSuchFieldException nsf = null;
		try {
			return c.getField(name);
		} catch (SecurityException e) {
		} catch (NoSuchFieldException e) {
			nsf = e;
		}
		while (c != null) {
			try {
				return c.getDeclaredField(name);
			} catch (SecurityException se) {
				throw se;
			} catch (NoSuchFieldException nsf2) {
				c = c.getSuperclass();
				continue;
			}
		}
		throw nsf;
	}

	private void setObjectField(MKC thingActionFilterImpl, Item item, ItemAssemblyReader reader, Method writeMethod, String pdn, Class pdt, Assembler asmblr, Mode mode) throws IllegalAccessException,
			InvocationTargetException {
		if (pdt == null) {
			if (writeMethod != null) {
				pdt = writeMethod.getParameterTypes()[0];
			} else {
				pdt = Object.class;
			}
		}
		pdt = JenaLiteralUtils.nonPrimitiveTypeFor(pdt);
		String sv = reader.readConfigValString(item.getIdent(), pdn, item, null);
		if (sv == null) {
			List<Object> res = reader.findOrMakeLinkedObjects(item, pdn, asmblr, mode, null);
			return;
		}
		if (writeMethod == null) {
			theLogger.warn("Missing write method on field: " + pdn + " type " + pdt.getSimpleName() + " = " + sv);
			return;
		}
		theLogger.warn("Setting field: " + pdn + " type " + pdt.getSimpleName() + " = " + sv);
		if (pdt == String.class) {
			writeMethod.invoke(thingActionFilterImpl, sv);
		} else if (pdt == Ident.class) {
			writeMethod.invoke(thingActionFilterImpl, new FreeIdent(sv));
		} else {

		}
	}
}
