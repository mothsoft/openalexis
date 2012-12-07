/*   Copyright 2012 Tim Garrett, Mothsoft LLC
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.mothsoft.alexis.web.faces;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;

import org.apache.commons.io.FileUtils;

/**
 * Converts a number of bytes specified into KB notation
 * 
 * @author tgarrett
 * 
 */
public class HumanReadableBytesConverter implements Converter {

    public HumanReadableBytesConverter() {
        // default constructor
    }

    public Object getAsObject(FacesContext context, UIComponent component, String string) throws ConverterException {
        final String[] split = string.split(" ");
        final Integer result = Integer.valueOf(split[0]) * 1024;
        return result;
    }

    public String getAsString(FacesContext context, UIComponent component, Object object) throws ConverterException {
        final Integer value = (Integer) object;
        return FileUtils.byteCountToDisplaySize(value);
    }

}
