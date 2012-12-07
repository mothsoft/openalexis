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

import java.util.ResourceBundle;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.queryParser.MultiFieldQueryParser;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.util.Version;

public class LuceneSearchExpressionValidator implements Validator {

    public void validate(FacesContext context, UIComponent component, Object value) throws ValidatorException {
        final String queryString = (String) value;

        final String[] fields = new String[] { "title", "description", "content.text" };
        final MultiFieldQueryParser parser = new MultiFieldQueryParser(Version.LUCENE_31, fields, new StandardAnalyzer(
                Version.LUCENE_31));

        try {
            parser.parse(queryString);
        } catch (ParseException e) {
            final String messageBundle = FacesContext.getCurrentInstance().getApplication().getMessageBundle();
            final String stringMessage = ResourceBundle.getBundle(messageBundle).getString("validator.searchExpression");

            final FacesMessage facesMessage = new FacesMessage(stringMessage);
            facesMessage.setSeverity(FacesMessage.SEVERITY_WARN);

            throw new ValidatorException(facesMessage);
        }

    }

}
