/**
 * Copyright (c) 2010-2015, openHAB.org and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.canopen.internal;

import org.openhab.binding.canopen.CANOpenBindingProvider;
import org.openhab.binding.canopen.internal.CANOpenItemConfig;
import org.openhab.core.items.Item;
import org.openhab.core.library.items.NumberItem;
import org.openhab.model.item.binding.AbstractGenericBindingProvider;
import org.openhab.model.item.binding.BindingConfigParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * This class is responsible for parsing the binding configuration.
 * 
 * @author Jens Geisler
 * @since 1.7.0
 */
public class CANOpenGenericBindingProvider extends AbstractGenericBindingProvider implements CANOpenBindingProvider {
	static final Logger logger = LoggerFactory.getLogger(CANOpenGenericBindingProvider.class);

	/**
	 * {@inheritDoc}
	 */
	public String getBindingType() {
		return "canopen";
	}

	/**
	 * @{inheritDoc}
	 */
	@Override
	public void validateItemType(Item item, String bindingConfig) throws BindingConfigParseException {
//		if (item.getClass() == SwitchItem.class)
//			return;
//		if (item.getClass() == ContactItem.class)
//			return;
//		if (item.getClass() == NumberItem.class)
//			return;
//
//		throw new BindingConfigParseException("item '" + item.getName()
//				+ "' is of type '" + item.getClass().getSimpleName()
//				+ "', only Switch, Contact or Number are allowed - please check your *.items configuration");
		if (!(item instanceof NumberItem)) {
			throw new BindingConfigParseException("item '" + item.getName()
					+ "' is of type '" + item.getClass().getSimpleName()
					+ "', only NumberItems are allowed - please check your *.items configuration");
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void processBindingConfiguration(String context, Item item, String bindingConfig) throws BindingConfigParseException {
		super.processBindingConfiguration(context, item, bindingConfig);
		if (bindingConfig != null) {
			CANOpenItemConfig config = new CANOpenItemConfig(item, bindingConfig);
			addBindingConfig(item, config);		
			logger.debug("processBindingConfiguration " + config);
		}
		else {
			logger.warn("bindingConfig is NULL (item=" + item + ") -> processing bindingConfig aborted!");
		}
	}
	
	@Override
	public CANOpenItemConfig getItemConfig(String itemName) {
		return (CANOpenItemConfig) bindingConfigs.get(itemName);
	}

}
